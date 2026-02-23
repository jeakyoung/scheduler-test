# 스케줄러 테스트 프로젝트

SP 호출 없이 간단하게 스케줄러를 테스트할 수 있는 미니 프로젝트입니다.

## 📁 프로젝트 구조

```
scheduler-test/
├── src/
│   └── com/
│       └── scheduler/
│           ├── SchedulerContextListener.java  (스케줄러 초기화)
│           ├── SimpleSchedulerTask.java       (실제 작업 수행)
│           └── ManualExecutor.java            (수동 실행 서블릿)
├── WEB-INF/
│   ├── web.xml                                (설정 파일)
│   ├── classes/                               (컴파일된 클래스)
│   └── lib/                                   (라이브러리)
├── index.html                                 (메인 페이지)
├── build.sh                                   (빌드 스크립트)
└── README.md
```

## 🚀 실행 방법

### 방법 1: IntelliJ IDEA 사용 (추천)

1. **IntelliJ IDEA 실행**

2. **프로젝트 열기**
   - File → Open
   - `scheduler-test` 폴더 선택

3. **Tomcat 서버 추가**
   - Run → Edit Configurations
   - '+' 클릭 → Tomcat Server → Local
   - Tomcat 설치 경로 지정
   - Deployment 탭 → '+' → Artifact → scheduler-test:war exploded

4. **실행**
   - Run 버튼 클릭
   - 브라우저에서 `http://localhost:8080/scheduler-test` 접속

### 방법 2: 명령줄에서 빌드 후 Tomcat에 배포

1. **Tomcat 다운로드** (없는 경우)
   ```bash
   # Homebrew 사용
   brew install tomcat

   # 또는 수동 다운로드
   # https://tomcat.apache.org/download-90.cgi
   ```

2. **프로젝트 빌드**
   ```bash
   cd scheduler-test
   chmod +x build.sh
   ./build.sh
   ```

3. **WAR 파일 배포**
   ```bash
   # Homebrew로 설치한 경우
   cp scheduler-test.war /opt/homebrew/Cellar/tomcat/*/libexec/webapps/

   # 수동 설치한 경우
   cp scheduler-test.war /path/to/tomcat/webapps/
   ```

4. **Tomcat 시작**
   ```bash
   # Homebrew 설치
   catalina start

   # 수동 설치
   /path/to/tomcat/bin/catalina.sh start
   ```

5. **브라우저 접속**
   ```
   http://localhost:8080/scheduler-test
   ```

### 방법 3: Eclipse 사용

1. **Eclipse 실행**

2. **Dynamic Web Project 생성**
   - File → New → Dynamic Web Project
   - Project name: scheduler-test

3. **파일 복사**
   - src 폴더 내용을 Eclipse 프로젝트의 src에 복사
   - WEB-INF, index.html을 WebContent에 복사

4. **서버에 배포**
   - 프로젝트 우클릭 → Run As → Run on Server

## 📊 동작 확인

### 1. 자동 실행 확인

서버 시작 후 콘솔에서 다음과 같은 로그를 확인할 수 있습니다:

```
========================================
스케줄러 초기화 시작
========================================
스케줄러 등록 완료!
- 첫 실행: 10초 후
- 반복 주기: 60초마다
========================================

(10초 후)

========================================
📅 스케줄 작업 실행 #1
⏰ 실행 시간: 2026-01-30 15:30:00
========================================

[1/2] 데이터베이스 작업 시작...
  → SP_DAILY_SCHEDULED_TASK 실행 완료 (시뮬레이션)
  → 처리된 레코드: 150개
[1/2] ✓ 데이터베이스 작업 완료

[2/2] API 호출 시작...
  → FCM API 호출 완료 (시뮬레이션)
  → 전송된 알림: 50개
[2/2] ✓ API 호출 완료

✅ 모든 작업 완료!
========================================
```

### 2. 수동 실행 확인

1. 브라우저에서 `http://localhost:8080/scheduler-test` 접속
2. "스케줄러 수동 실행" 버튼 클릭
3. 서버 콘솔에서 즉시 실행되는 로그 확인

## ⚙️ 설정 변경

### 실행 주기 변경

`src/com/scheduler/SchedulerContextListener.java` 파일에서:

```java
// 현재: 테스트용 (10초 후 첫 실행, 60초마다 반복)
long initialDelay = 10;
long period = 60;

// 실제 운영용으로 변경하려면:
long initialDelay = calculateInitialDelay(); // 매일 9시
long period = TimeUnit.DAYS.toSeconds(1);    // 24시간마다
```

### 실제 작업 추가

`src/com/scheduler/SimpleSchedulerTask.java` 파일에서 시뮬레이션 코드를 실제 코드로 변경:

```java
private void executeTask1() throws Exception {
    // 현재: Thread.sleep(2000); (시뮬레이션)

    // 실제 운영용:
    Connection conn = ... (DB 연결)
    CallableStatement stmt = conn.prepareCall("{call SP_NAME}");
    stmt.execute();
}
```

## 🔧 문제 해결

### 포트 충돌 (8080 포트 사용중)

Tomcat 포트 변경:
```bash
# server.xml 파일 수정
vi /opt/homebrew/Cellar/tomcat/*/libexec/conf/server.xml

# 8080을 다른 포트로 변경 (예: 8081)
```

### 클래스 파일 찾을 수 없음

다시 빌드:
```bash
./build.sh
```

### 서블릿이 실행되지 않음

web.xml 확인:
- Servlet 3.0+ 버전 사용
- `@WebServlet`, `@WebListener` 어노테이션 지원

## 📚 다음 단계

1. ✅ 스케줄러 동작 확인
2. ✅ 수동 실행 테스트
3. ✅ 로그 확인
4. 실제 프로젝트에 적용:
   - DB 연결 추가
   - Stored Procedure 호출 추가
   - FCM API 연동 추가

## 💡 팁

- **개발 중**: 실행 주기를 짧게 설정 (60초)
- **운영 시**: 실행 주기를 원하는 시간으로 변경
- **로그**: 서버 콘솔을 항상 확인하세요
- **디버깅**: ManualExecutor를 사용하여 즉시 테스트
