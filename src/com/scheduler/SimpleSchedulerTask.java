package com.scheduler;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 간단한 테스트용 스케줄 작업
 * SP 호출 없이 로그만 출력
 */
public class SimpleSchedulerTask implements Runnable {

    private final DateTimeFormatter formatter;
    private int executionCount = 0;

    public SimpleSchedulerTask() {
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    // =============================================
    // DB 접속 정보
    // =============================================

    private static final String DRIVER   = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL      = "jdbc:sqlserver://218.38.64.229;databaseName=iPlusERP_Test;encrypt=false;trustServerCertificate=true;";
    private static final String USERNAME = "erpUser";
    private static final String PASSWORD = "erpPasswd";

    @Override
    public void run() {
        executionCount++;
        String currentTime = LocalDateTime.now().format(formatter);

        System.out.println("\n========================================");
        System.out.println("스케줄 작업 실행 #" + executionCount);
        System.out.println("실행 시간: " + currentTime);
        System.out.println("========================================");

        try {
            // 작업 1: 데이터베이스 작업 시뮬레이션
            executeTask1();

            // 작업 2: API 호출 시뮬레이션
            List<String> employeeNoList = executeTask1();
            executeTask2(employeeNoList);

            System.out.println("모든 작업 완료!");
            System.out.println("== ======================================\n");

        } catch (Exception e) {
            System.err.println("작업 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================\n");
        }
    }

    /**
     * 작업 1: 데이터베이스 호출
     */
    private List<String> executeTask1() throws Exception {
        Connection conn = null;
        Statement  stmt = null;
        ResultSet  rs   = null;

        List<String> employeeNoList = new ArrayList<>();

        try {
            // DB 접속
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            stmt = conn.createStatement();

            // SP 호출
            String sql = "EXEC SAFE_INVOICE_NOTICE ";

            System.out.println("\n[1/2] 데이터베이스 작업 시작...");
            System.out.println(sql);

            rs = stmt.executeQuery(sql);

            int processedCount = 0;
            while (rs.next()) {
                processedCount++;

                // EMPLOYEE_NO 수집 (실제 컬럼명으로 변경)
                String employeeNo = rs.getString("EMPLOYEE_NO");
                employeeNoList.add(employeeNo);
                System.out.println("  → 수집된 사번: " + employeeNo); //완료시 주석필수
            }
            //완료시 주석필수
            System.out.println("  → 처리된 레코드: " + processedCount + "개");
            System.out.println("  → 수집된 사번 목록: " + employeeNoList);
            System.out.println("[1/2] ✓ 데이터베이스 작업 완료");

        } finally {
            try { if (rs   != null) rs.close();   } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        return employeeNoList;
    }

    /**
     * 작업 2: API 호출 시뮬레이션
     * (실제로는 FCM API 호출)
     */
    /**
     * 작업 2: FCM API 호출
     */
    private void executeTask2(List<String> employeeNoList) throws Exception {
        System.out.println("\n[2/2] FCM API 호출 시작...");

        URL url = new URL("http://218.38.64.229:40110/api/Fcm/FcmPassivity");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
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
