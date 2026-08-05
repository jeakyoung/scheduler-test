package com.scheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 스케줄러 초기화 리스너
 *
 * [등록된 스케줄]
 * 1. DaySchedulerTask  : 매일 오전 9시 1회 실행
 * 2. HourSchedulerTask : 평일 매시 정각 및 30분마다 실행 (토/일 제외는 Task 내부에서 처리)
 */
@WebListener
public class SchedulerContextListener implements ServletContextListener {
    private static boolean isInitialized = false;
    private ScheduledExecutorService scheduler;

    // 테스트 모드: true = 10초마다 실행, false = 운영 스케줄
    private static final boolean TEST_MODE = true;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public static void main(String[] args) {
        SchedulerContextListener listener = new SchedulerContextListener();

        System.out.println(">>> [시스템] 스케줄러 단독 실행 모드 시작");

        listener.contextInitialized(null);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(">>> [시스템] 종료 신호 감지. 자원을 정리합니다...");
            listener.contextDestroyed(null);
        }));

        try {
            System.out.println(">>> [시스템] 스케줄러가 정상 작동 중입니다.");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println(">>> [에러] 메인 스레드 중단됨: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void contextInitialized(ServletContextEvent sce) {

        if (sce != null && sce.getServletContext() != null) {
            String contextPath = sce.getServletContext().getContextPath();
            System.out.println("컨텍스트 경로: " + contextPath);
        } else {
            System.out.println(">>> [알림] 단독 실행 모드로 시작합니다.");
        }

        if (isInitialized) {
            System.out.println("[중복 제어] 이미 초기화되었습니다. 중복 실행을 건너뜁니다.");
            return;
        }

        isInitialized = true;

        System.out.println("\n========================================");
        System.out.println("스케줄러 초기화 시작");
        System.out.println("========================================");

        try {
            // 작업 2개 동시 실행 가능하도록 스레드 풀 2개 할당
            scheduler = Executors.newScheduledThreadPool(2);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            if (TEST_MODE) {
                // ── 테스트 모드 ──────────────────────────────
                System.out.println("⚠️  테스트 모드 활성화");

                scheduler.scheduleAtFixedRate(new DaySchedulerTask(),  10, 60, TimeUnit.SECONDS);
                scheduler.scheduleAtFixedRate(new HourSchedulerTask(), 10, 60, TimeUnit.SECONDS);

                System.out.println("- DaySchedulerTask  : 10초 후 첫 실행, 이후 60초마다");
                System.out.println("- HourSchedulerTask : 10초 후 첫 실행, 이후 60초마다");

            } else {
                // ── 운영 모드 ────────────────────────────────

                // 1. DaySchedulerTask: 매일 오전 9시
                long dailyDelay = calculateInitialDelayForKST(9, 0);
                ZonedDateTime nextDaily = ZonedDateTime.now(KOREA_ZONE).plusSeconds(dailyDelay);

                scheduler.scheduleAtFixedRate(
                    new DaySchedulerTask(),
                    dailyDelay,
                    24 * 60 * 60,   // 24시간
                    TimeUnit.SECONDS
                );

                System.out.println("✅ 운영 모드 활성화");
                System.out.println("[DaySchedulerTask]  매일 오전 9시 실행");
                System.out.println("  - 다음 실행: " + nextDaily.format(formatter));
                System.out.println("  - 첫 실행까지: " + formatDuration(dailyDelay));

                // 2. HourSchedulerTask: 매시 정각 및 30분 (주말 제외는 Task 내부에서 처리)
                long halfHourDelay = calculateInitialDelayFor30Min();
                ZonedDateTime nextHalf = ZonedDateTime.now(KOREA_ZONE).plusSeconds(halfHourDelay);

                scheduler.scheduleAtFixedRate(
                    new HourSchedulerTask(),
                    halfHourDelay,
                    30 * 60,        // 30분
                    TimeUnit.SECONDS
                );

                System.out.println("[HourSchedulerTask] 매시 정각·30분 실행 (평일만)");
                System.out.println("  - 다음 실행: " + nextHalf.format(formatter));
                System.out.println("  - 첫 실행까지: " + formatDuration(halfHourDelay));
            }

            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("스케줄러 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("\n========================================");
        System.out.println("스케줄러 종료 시작");
        System.out.println("========================================");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                System.out.println("스케줄러 정상 종료");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        isInitialized = false;
        System.out.println("========================================\n");
    }

    /**
     * 한국시간(KST) 기준 지정 시각까지의 초 계산
     */
    private long calculateInitialDelayForKST(int hour, int minute) {
        ZonedDateTime nowKST = ZonedDateTime.now(KOREA_ZONE);
        ZonedDateTime nextRun = nowKST.toLocalDate()
                .atTime(LocalTime.of(hour, minute))
                .atZone(KOREA_ZONE);

        if (nowKST.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        return Duration.between(nowKST, nextRun).getSeconds();
    }

    /**
     * 다음 정각(:00) 또는 30분(:30)까지의 초 계산
     */
    private long calculateInitialDelayFor30Min() {
        ZonedDateTime nowKST = ZonedDateTime.now(KOREA_ZONE);
        int minute = nowKST.getMinute();
        int second = nowKST.getSecond();

        // 현재 분이 30분 미만이면 → 이번 시간 30분, 아니면 → 다음 시간 정각
        int targetMinute = (minute < 30) ? 30 : 60;
        long delay = (targetMinute - minute) * 60L - second;

        return delay;
    }

    /**
     * 초 단위를 사람이 읽기 쉬운 형식으로 변환
     */
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d시간 %d분 %d초", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, secs);
        } else {
            return String.format("%d초", secs);
        }
    }
}
