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
 * 간단한 테스트용 스케줄러
 * 매일 특정 시간에 실행 (기본: 매 1분마다 테스트용으로 실행)
 */
@WebListener
public class SchedulerContextListener implements ServletContextListener {
    private static boolean isInitialized = false;
    private ScheduledExecutorService scheduler;
    private SimpleSchedulerTask schedulerTask;

    // 테스트 모드: true = 10초마다 실행, false = 매일 9시 실행
    private static final boolean TEST_MODE = false;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public static void main(String[] args) {
        SchedulerContextListener listener = new SchedulerContextListener();

        System.out.println(">>> [시스템] 스케줄러 단독 실행 모드 시작");

        // 1. 초기화 실행 (sce는 null로 전달)
        listener.contextInitialized(null);

        // 2. 프로세스 종료 시 안전하게 스케줄러를 끄기 위한 셧다운 후크 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(">>> [시스템] 종료 신호 감지. 자원을 정리합니다...");
            listener.contextDestroyed(null);
        }));

        // 3. 메인 스레드 유지
        try {
            System.out.println(">>> [시스템] 스케줄러가 정상 작동 중입니다.");
            // 대기 상태 유지
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println(">>> [에러] 메인 스레드 중단됨: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void contextInitialized(ServletContextEvent sce) {

//        String contextPath = sce.getServletContext().getContextPath();
////        if (!"".equals(contextPath)) {
////            System.out.println("\n[중복 실행 차단] 현재 컨텍스트 [" + contextPath + "]는 스케줄러 실행 대상이 아닙니다.");
////            return;
////        }
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
            // 스케줄러 작업 생성
            schedulerTask = new SimpleSchedulerTask();

            // 스케줄러 생성
            scheduler = Executors.newSingleThreadScheduledExecutor();

            long initialDelay;
            long period;

            if (TEST_MODE) {
                // 테스트 모드: 10초마다 실행
                initialDelay = 10; // 10초 후 첫 실행
                period = 60; // 1분마다 반복

                System.out.println("⚠️  테스트 모드 활성화");
                System.out.println("스케줄러 등록 완료!");
                System.out.println("- 첫 실행: " + initialDelay + "초 후");
                System.out.println("- 반복 주기: " + period + "초마다");

            } else {
                // 운영 모드: 매일 한국시간 9시 실행
                initialDelay = calculateInitialDelayForKST9AM();
                period = 24 * 60 * 60; // 24시간 = 86400초

                ZonedDateTime nextRun = ZonedDateTime.now(KOREA_ZONE).plusSeconds(initialDelay);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                System.out.println("✅ 운영 모드 활성화");
                System.out.println("스케줄러 등록 완료!");
                System.out.println("- 시간대: 한국시간 (KST, UTC+9)");
                System.out.println("- 실행 시각: 매일 오전 9시");
                System.out.println("- 다음 실행: " + nextRun.format(formatter));
                System.out.println("- 첫 실행까지: " + formatDuration(initialDelay));
            }

            scheduler.scheduleAtFixedRate(
                schedulerTask,
                initialDelay,
                period,
                TimeUnit.SECONDS
            );
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

        System.out.println("========================================\n");
    }

    /**
     * 한국시간(KST) 기준 매일 오전 9시 실행을 위한 초기 지연 시간 계산
     */
    private long calculateInitialDelayForKST9AM() {
        ZonedDateTime nowKST = ZonedDateTime.now(KOREA_ZONE);
//        ZonedDateTime nextRun = nowKST.toLocalDate()
//            .atTime(LocalTime.of(9, 0))
//            .atZone(KOREA_ZONE);

        // 스케쥴링 테스트용
        ZonedDateTime nextRun = nowKST.toLocalDate()
                .atTime(LocalTime.of(9, 0))
                .atZone(KOREA_ZONE);

        // 현재 시간이 오늘 9시를 지났다면 내일 9시로 설정
        if (nowKST.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        Duration duration = Duration.between(nowKST, nextRun);
        return duration.getSeconds();
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
