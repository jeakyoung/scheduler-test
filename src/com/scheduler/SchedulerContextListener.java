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

    private ScheduledExecutorService scheduler;
    private SimpleSchedulerTask schedulerTask;

    // 테스트 모드: true = 10초마다 실행, false = 매일 9시 실행
    private static final boolean TEST_MODE = true;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
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
                 initialDelay = 5; // 5초 후 첫 실행
                 period = 10; // 10초마다 반복

//                // 운영모드 : 24시간마다 9시에 실행
//                long initialDelay = calculateInitialDelay(); // 매일 9시
//                long period = TimeUnit.DAYS.toSeconds(1);    // 24시간마다

                // System.out.println("⚠️  테스트 모드 활성화");
                System.out.println("⚠️  운영 모드 활성화");
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
