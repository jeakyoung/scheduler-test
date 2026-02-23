package com.scheduler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 스케줄러 수동 실행용 서블릿
 * URL: http://localhost:8080/scheduler-test/manual
 */
@WebServlet("/manual")
public class ManualExecutor extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>스케줄러 수동 실행</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            out.println("h1 { color: #333; }");
            out.println(".success { color: green; font-weight: bold; }");
            out.println(".info { background: #f0f0f0; padding: 20px; border-radius: 5px; margin: 20px 0; }");
            out.println("button { background: #4CAF50; color: white; padding: 15px 30px; border: none; border-radius: 5px; cursor: pointer; font-size: 16px; }");
            out.println("button:hover { background: #45a049; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>스케줄러 테스트 페이지</h1>");

            // 스케줄러 실행
            SimpleSchedulerTask task = new SimpleSchedulerTask();
            task.run();

            out.println("<p class='success'>스케줄러가 수동으로 실행되었습니다!</p>");
            out.println("<div class='info'>");
            out.println("<h3>실행 내용:</h3>");
            out.println("<ul>");
            out.println("<li>데이터베이스 작업 (시뮬레이션) - 완료</li>");
            out.println("<li>API 호출 (시뮬레이션) - 완료</li>");
            out.println("</ul>");
            out.println("<p><strong>서버 콘솔을 확인하여 상세 로그를 확인하세요!</strong></p>");
            out.println("</div>");

            out.println("<button onclick='location.reload()'>다시 실행</button>");
            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            out.println("<p style='color: red;'>오류 발생: " + e.getMessage() + "</p>");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
