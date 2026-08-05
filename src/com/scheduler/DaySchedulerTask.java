package com.scheduler;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 스케쥴링 실 운영 모드 - 일 단위 실행 (매일 오전 9시)
 */
public class DaySchedulerTask implements Runnable {
    private static boolean isTaskRunning = false;

    private final DateTimeFormatter formatter;
    private int executionCount = 0;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public DaySchedulerTask() {
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    // =============================================
    // DB 접속 정보 (config.properties에서 로드)
    // =============================================

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    @Override
    public void run() {
        // 중복 실행 체크
        synchronized (DaySchedulerTask.class) {
            if (isTaskRunning) {
                System.out.println("\n[중복 차단] 이미 다른 작업이 진행 중입니다. 이번 실행은 무시합니다.");
                return;
            }
            isTaskRunning = true;
        }
        try {
            ZonedDateTime nowKST = ZonedDateTime.now(KOREA_ZONE);
            DayOfWeek week = nowKST.getDayOfWeek();

            if (week != DayOfWeek.MONDAY) {
                System.out.println("\n[일 단위] [스킵] " + nowKST.format(formatter) + " - 월요일이 아니므로 실행하지 않습니다.");
                return;
            }

        executionCount++;
        String currentTime = LocalDateTime.now().format(formatter);

        System.out.println("\n========================================");
        System.out.println("[일 단위] 스케줄 작업 실행 #" + executionCount);
        System.out.println("실행 시간: " + currentTime);
        System.out.println("========================================");

            // 작업 1: 데이터베이스 작업 시뮬레이션
            List<String> employeeNoList = executeTask1();

            // 작업 2: API 호출 시뮬레이션
            executeTask2(employeeNoList);

            System.out.println("모든 작업 완료!");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("작업 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================\n");
        } finally {
            //작업이 끝나면 중복 호출 해제
            synchronized (DaySchedulerTask.class) {
                isTaskRunning = false;
            }
        }
    }

    /**
     * 작업 1: 데이터베이스 호출
     */
    private List<String> executeTask1() throws Exception {
        List<String> employeeNoList = new ArrayList<>();

        Class.forName(DRIVER);

        try (Connection conn = DriverManager.getConnection(Config.getDbUrl(), Config.getDbUsername(), Config.getDbPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("EXEC SP_WORK_LIMIT_LIST ")) {

            System.out.println("\n[1/2] 데이터베이스 작업 시작...");
            System.out.println("쿼리 실행: EXEC SP_WORK_LIMIT_LIST ");

            int processedCount = 0;
            while (rs.next()) {
                processedCount++;
                String employeeNo = rs.getString("EMPLOYEE_NO");
                employeeNoList.add(employeeNo);
            }

            System.out.println("  → 처리된 레코드: " + processedCount + "개");
            System.out.println("  → 수집된 사번 목록: " + employeeNoList);
            System.out.println("[1/2] ✓ 데이터베이스 작업 완료");

        } catch (Exception e) {
            System.err.println("DB 작업 중 상세 에러: " + e.getMessage());
            throw e;
        }

        return employeeNoList;
    }

    /**
     * 작업 2: FCM API 호출
     */
    private void executeTask2(List<String> employeeNoList) throws Exception {
        System.out.println("\n[2/2] FCM API 호출 시작...");

        URL url = new URL(Config.getFcmUrl());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("SECURITY_CODE", "4000");
        conn.setDoOutput(true);

        // EmpList 가공
        StringBuilder empList = new StringBuilder();
        for (int i = 0; i < employeeNoList.size(); i++) {
            empList.append("\"").append(employeeNoList.get(i)).append("\"");
            if (i < employeeNoList.size() - 1) empList.append(",");
        }

        String body = "{"
                + "\"notiGbn\":\"8\","
                + "\"companyCode\":\"000001\","
                + "\"empList\":[" + empList + "],"
                + "\"senderCode\":\"0000\","
                + "\"title\":\"업무연락 알림\","
                + "\"body\":\"확인해야할 업무연락 알림이 도착했습니다.\""
                + "}";

        System.out.println("  → 요청 Body: " + body);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        System.out.println("  → 응답 코드: " + responseCode);
        System.out.println(responseCode == 200 ? "  → FCM 전송 성공" : "  → FCM 전송 실패");

        conn.disconnect();
        System.out.println("[2/2] ✓ FCM API 호출 완료");
    }
}
