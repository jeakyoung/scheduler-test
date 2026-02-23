package com.scheduler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    @Override
    public void run() {
        executionCount++;
        String currentTime = LocalDateTime.now().format(formatter);

        System.out.println("\n========================================");
        System.out.println("스케줄 작업 실행 #" + executionCount);
        System.out.println("실행 시간: " + currentTime);
        System.out.println("========================================");

        try {
            // 스케쥴링 로직 시작
            executeTask1();

            System.out.println("모든 작업 완료!");
            System.out.println("== ======================================\n");

        } catch (Exception e) {
            System.err.println("작업 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================\n");
        }
    }

    /**
     * 작업 1: 데이터베이스 작업 시뮬레이션
     * (실제로는 Stored Procedure 호출)
     */
//    private void executeTask1() throws InterruptedException {
//        System.out.println("\n[1/2] 데이터베이스 작업 시작...");
//
//         // 2초 대기 (실제 작업 시뮬레이션)
//         Thread.sleep(2000);
//
////        Connection conn = ... (DB 연결)
////        CallableStatement stmt = conn.prepareCall("{SAFE_INVOICE_NOTICE}");
////        stmt.execute();
//
//        System.out.println("  → SP_DAILY_SCHEDULED_TASK 실행 완료 (시뮬레이션)");
//        System.out.println("  → 처리된 레코드: 150개");
//        System.out.println("[1/2] ✓ 데이터베이스 작업 완료");
//    }
    private void executeTask1() {
        System.out.println("\n[1/2] 데이터베이스 작업 시작...");

        String url = "jdbc:sqlserver://218.38.64.229:1433;databaseName=iPlusERP_Test";
        String user = "erpUser";
        String password = "erpPasswd";

        String sql = "{call SAFE_INVOICE_NOTICE(?)}";

        List<Map<String, String>> resultList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, java.sql.Types.INTEGER);

            // 프로시저 실행
            boolean hasResults = stmt.execute();

            // ResultSet으로 데이터 받기
            if (hasResults) {
                try (java.sql.ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                        Map<String, String> row = new HashMap<>();

                        // DB에서 반환하는 컬럼들 (실제 컬럼명에 맞게 수정)
                        row.put("notiGbn", rs.getString("NOTI_GBN"));           // 알림구분
                        row.put("companyCode", rs.getString("COMPANY_CODE"));   // 회사코드
                        row.put("empCode", rs.getString("EMP_CODE"));           // 직원코드
                        row.put("senderCode", rs.getString("SENDER_CODE"));     // 발신자코드
                        row.put("title", rs.getString("TITLE"));                // 제목
                        row.put("body", rs.getString("BODY"));                  // 내용

                        resultList.add(row);
                    }
                }
            }

            int processedCount = stmt.getInt(1);

            System.out.println("  → " + sql + " 실행 완료");
            System.out.println("  → 처리된 레코드: " + processedCount + "개");
            System.out.println("  → 알림 대상: " + resultList.size() + "건");
            System.out.println("[1/2] ✓ 데이터베이스 작업 완료");

            // Task2로 결과 전달
            if (!resultList.isEmpty()) {
                executeTask2(resultList);
            }

        } catch (SQLException e) {
            System.err.println("  → [SQL 오류] " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("  → [API 호출 오류] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 작업 2: .NET Core FCM API 호출
     */
    private void executeTask2(List<Map<String, String>> resultList) throws Exception {
        System.out.println("\n[2/2] API 호출 시작...");

        String apiUrl = "http://localhost:5094/api/Fcm/FcmPassivity";

        // 같은 알림 내용끼리 그룹핑 (companyCode + title + body 기준)
        Map<String, List<Map<String, String>>> grouped = new HashMap<>();

        for (Map<String, String> row : resultList) {
            String key = row.get("companyCode") + "|" +
                    row.get("notiGbn") + "|" +
                    row.get("title") + "|" +
                    row.get("body");

            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        int totalSent = 0;

        // 그룹별로 API 호출
        for (List<Map<String, String>> group : grouped.values()) {
            Map<String, String> first = group.get(0);

            // empList 생성 (같은 그룹의 직원코드들 수집)
            StringBuilder empListJson = new StringBuilder();
            for (int i = 0; i < group.size(); i++) {
                if (i > 0) empListJson.append(",");
                empListJson.append("\"").append(group.get(i).get("empCode")).append("\"");
            }

            // JSON 페이로드 생성
            String jsonPayload = String.format(
                    "{" +
                            "\"notiGbn\":\"%s\"," +
                            "\"companyCode\":\"%s\"," +
                            "\"empList\":[%s]," +
                            "\"senderCode\":\"%s\"," +
                            "\"title\":\"%s\"," +
                            "\"body\":\"%s\"" +
                            "}",
                    first.get("notiGbn"),
                    first.get("companyCode"),
                    empListJson.toString(),
                    first.get("senderCode"),
                    first.get("title"),
                    first.get("body")
            );

            System.out.println("  → 발송 JSON: " + jsonPayload);

            // API 호출
            boolean success = callFcmApi(apiUrl, jsonPayload);
            if (success) {
                totalSent += group.size();
            }

            System.out.println("  → 그룹 발송: " + group.size() + "명");
        }

        System.out.println("  → 총 FCM 발송 완료: " + totalSent + "건");
        System.out.println("[2/2] ✓ API 호출 완료");
    }

    /**
     * FCM API 실제 호출
     */
    private boolean callFcmApi(String apiUrl, String jsonPayload) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // 요청 본문 전송
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            // 응답 읽기
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();

            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            System.out.println("    → 응답 코드: " + responseCode);
            System.out.println("    → 응답 본문: " + response.toString());

            conn.disconnect();

            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            System.err.println("    → API 호출 실패: " + e.getMessage());
            return false;
        }
    }
}