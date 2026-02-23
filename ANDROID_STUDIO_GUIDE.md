# 🤖 Android Studio에서 스케줄러 실행하기

## ✅ 준비 완료
- ✅ Tomcat 설치 완료: `/opt/homebrew/Cellar/tomcat/11.0.18/libexec`
- ✅ 프로젝트 열기 완료: `scheduler-test`

---

## 🚀 실행 방법 (단계별)

### 1️⃣ Tomcat 플러그인 설치

1. Android Studio에서 **File** → **Settings** (또는 ⌘ + ,)
2. 왼쪽 메뉴에서 **Plugins** 클릭
3. 검색창에 **"Smart Tomcat"** 입력
4. **Install** 클릭
5. **Restart IDE** 클릭하여 Android Studio 재시작

### 2️⃣ Run Configuration 설정

1. 상단 메뉴: **Run** → **Edit Configurations...**

2. 왼쪽 상단 **+** 버튼 클릭

3. **Smart Tomcat** 선택

4. 다음 항목 설정:
   ```
   Name: Scheduler Test

   Tomcat Server: /opt/homebrew/Cellar/tomcat/11.0.18/libexec

   Deployment Directory: /Users/f1soft/Downloads/scheduler-test

   Context Path: /scheduler-test

   Server Port: 8080

   VM options: (비워둠)
   ```

5. **OK** 클릭

### 3️⃣ 실행!

1. 상단 툴바에서 방금 만든 **"Scheduler Test"** 선택

2. **Run** 버튼 (▶) 클릭 (또는 Shift + F10)

3. **Run 탭**에서 로그 확인:
   ```
   ========================================
   스케줄러 초기화 시작
   ========================================
   스케줄러 등록 완료!
   - 첫 실행: 10초 후
   - 반복 주기: 60초마다
   ========================================
   ```

4. **브라우저 접속** (자동으로 열림):
   ```
   http://localhost:8080/scheduler-test
   ```

---

## 📊 동작 확인

### 자동 실행 (10초 후, 이후 60초마다)

Android Studio의 **Run 탭**에서 다음 로그를 확인:

```
========================================
📅 스케줄 작업 실행 #1
⏰ 실행 시간: 2026-01-30 11:30:00
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

### 수동 실행

브라우저에서:
1. `http://localhost:8080/scheduler-test` 접속
2. **"🚀 스케줄러 수동 실행"** 버튼 클릭
3. Run 탭에서 즉시 실행되는 로그 확인

---

## 🔧 문제 해결

### "Smart Tomcat 플러그인이 안 보여요"

- Android Studio를 완전히 재시작하세요
- Plugins에서 "Marketplace" 탭 확인
- 또는 직접 다운로드: https://plugins.jetbrains.com/plugin/9492-smart-tomcat

### "포트 8080이 이미 사용중입니다"

다른 프로그램이 8080 포트를 사용중입니다:

```bash
# 포트 사용 프로세스 확인
lsof -i :8080

# 프로세스 종료 (PID는 위 명령어 결과에서 확인)
kill -9 [PID]
```

또는 Run Configuration에서 Server Port를 8081로 변경

### "Tomcat Home이 올바르지 않습니다"

정확한 경로 확인:
```bash
ls /opt/homebrew/Cellar/tomcat/11.0.18/libexec
```

이 경로를 그대로 복사하여 붙여넣기

### "클래스 파일을 찾을 수 없습니다"

프로젝트 다시 빌드:
```bash
cd /Users/f1soft/Downloads/scheduler-test
javac -d WEB-INF/classes -cp "WEB-INF/lib/*" src/com/scheduler/*.java
```

---

## 🎯 다음 단계

1. ✅ 스케줄러 동작 확인
2. ✅ 자동 실행 로그 확인 (10초 후, 60초마다)
3. ✅ 수동 실행 테스트
4. 코드 수정해보기:
   - `SimpleSchedulerTask.java` 열어서 메시지 변경
   - 실행 주기 변경 (60초 → 30초)

---

## 💡 핵심 파일

- **SchedulerContextListener.java** - 스케줄러 초기화 (서버 시작 시)
- **SimpleSchedulerTask.java** - 실제 작업 수행 (60초마다)
- **ManualExecutor.java** - 수동 실행 서블릿 (버튼 클릭 시)

---

## 🎓 참고

### Tomcat이란?
- Java 웹 애플리케이션을 실행하는 서버
- Servlet, JSP 등을 실행할 수 있음

### 스케줄러란?
- 정해진 시간에 자동으로 작업을 실행
- 예: 매일 9시, 매 60초마다 등

### 이 프로젝트의 특징
- SP 호출 없이 로그만 출력 (테스트용)
- 실제 프로젝트에 적용 시 DB 연결 추가 필요

---

## 🎉 성공 화면

### Android Studio Run 탭:
```
Connected to server
[2026-01-30 11:30:00] Artifact scheduler-test:war: Artifact is being deployed, please wait...
[2026-01-30 11:30:05] Artifact scheduler-test:war: Artifact is deployed successfully
[2026-01-30 11:30:05] Artifact scheduler-test:war: Deploy took 5,234 milliseconds

========================================
스케줄러 초기화 시작
========================================
스케줄러 등록 완료!
```

### 브라우저:
```
📅 스케줄러 테스트 프로젝트

✅ 스케줄러가 실행 중입니다!

[🚀 스케줄러 수동 실행]
```

---

**문제가 생기면 말씀해주세요!** 🙌
