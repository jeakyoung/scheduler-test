# 🚀 빠른 시작 가이드 (Java 백엔드 초보자용)

## 준비물

1. **Java 17** ✅ (이미 설치됨)
2. **IntelliJ IDEA Community Edition** (무료)
   - 다운로드: https://www.jetbrains.com/idea/download/

## 📝 단계별 실행 방법

### 1단계: IntelliJ IDEA 설치

```bash
# Homebrew로 설치 (추천)
brew install --cask intellij-idea-ce

# 또는 위 링크에서 직접 다운로드
```

### 2단계: 프로젝트 열기

1. IntelliJ IDEA 실행
2. `Open` 클릭
3. 이 폴더 선택:
   ```
   /private/tmp/claude/-Users-f1soft-Downloads-local-atc-server-iPlusERP/f99d7f3c-45dc-47ad-a781-f875580e5645/scratchpad/scheduler-test
   ```
4. `Trust Project` 클릭

### 3단계: Tomcat 다운로드

```bash
# Homebrew로 설치 (가장 간단)
brew install tomcat

# 설치 확인
ls /opt/homebrew/Cellar/tomcat
```

### 4단계: IntelliJ에서 Tomcat 설정

#### 4-1. Run Configuration 열기
- 상단 메뉴: `Run` → `Edit Configurations...`

#### 4-2. Tomcat 추가
- 왼쪽 상단 `+` 클릭
- `Tomcat Server` → `Local` 선택
- 이름: `Tomcat` (원하는 이름)

#### 4-3. Tomcat 경로 설정
- `Configure...` 버튼 클릭
- Tomcat Home: `/opt/homebrew/Cellar/tomcat/[버전]/libexec`
  - 예: `/opt/homebrew/Cellar/tomcat/10.1.19/libexec`
- `OK` 클릭

#### 4-4. Deployment 설정
- `Deployment` 탭 클릭
- `+` 클릭 → `Artifact...` 선택
- `scheduler-test:war exploded` 선택
- Application context: `/scheduler-test`
- `OK` 클릭

### 5단계: 실행!

1. **Run 버튼** 클릭 (또는 Shift + F10)
2. **콘솔 확인**
   ```
   ==========================================
   스케줄러 초기화 시작
   ==========================================
   스케줄러 등록 완료!
   - 첫 실행: 10초 후
   - 반복 주기: 60초마다
   ==========================================
   ```
3. **브라우저 접속**
   ```
   http://localhost:8080/scheduler-test
   ```

### 6단계: 테스트

#### 자동 실행 확인
- 10초 후 콘솔에 첫 실행 로그가 나타남
- 이후 60초마다 자동으로 실행됨

#### 수동 실행 테스트
1. 브라우저에서 "스케줄러 수동 실행" 버튼 클릭
2. 콘솔에 즉시 실행 로그 확인

## 🎯 예상 로그 출력

```
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

## 🔧 문제 해결

### "Tomcat Home이 올바르지 않습니다"
```bash
# Tomcat 경로 확인
brew info tomcat

# 출력 예시:
# /opt/homebrew/Cellar/tomcat/10.1.19 (1,234 files, 15MB)
```
이 경로에 `/libexec`를 붙여서 설정

### "포트 8080이 이미 사용중입니다"
다른 Tomcat이나 서버가 실행중일 수 있습니다:
```bash
lsof -i :8080
kill -9 [PID]
```

### IntelliJ에서 Tomcat이 안 보임
- IntelliJ IDEA **Ultimate** 버전만 기본으로 Tomcat 지원
- Community Edition은 Smart Tomcat 플러그인 필요:
  1. `Preferences` → `Plugins`
  2. `Smart Tomcat` 검색 및 설치
  3. IntelliJ 재시작

## 🎓 더 간단한 방법 (플러그인 사용)

### Smart Tomcat 플러그인 사용 (Community Edition)

1. **플러그인 설치**
   - `Preferences` (⌘ + ,) → `Plugins`
   - "Smart Tomcat" 검색
   - Install 클릭
   - IntelliJ 재시작

2. **Run Configuration 설정**
   - `Run` → `Edit Configurations...`
   - `+` → `Smart Tomcat`
   - Name: `Scheduler Test`
   - Tomcat Server: `/opt/homebrew/Cellar/tomcat/[버전]/libexec`
   - Deployment Directory: `프로젝트 루트 경로`
   - Context Path: `/scheduler-test`
   - `OK`

3. **실행**
   - Run 버튼 클릭

## 📚 다음 단계

1. ✅ 스케줄러 동작 확인
2. ✅ 로그 출력 확인
3. ✅ 수동 실행 테스트
4. 코드 수정해보기:
   - `SimpleSchedulerTask.java`에서 메시지 변경
   - 실행 주기 변경 (60초 → 30초)
5. 실제 프로젝트에 적용

## 💡 핵심 개념 이해

### Servlet이란?
- 웹 요청을 처리하는 Java 클래스
- 예: `ManualExecutor.java` (수동 실행 버튼 클릭 시 동작)

### Listener란?
- 애플리케이션 이벤트를 감지하는 클래스
- 예: `SchedulerContextListener.java` (서버 시작/종료 시 동작)

### ScheduledExecutorService란?
- 특정 시간에 작업을 자동 실행하는 Java 기능
- 예: 매일 9시, 매 60초마다 등

### WAR 파일이란?
- Web Application aRchive
- 웹 애플리케이션을 배포하기 위한 압축 파일
- Tomcat의 `webapps/` 폴더에 복사하면 자동 배포

## 🎉 성공하면...

브라우저에서 이런 화면을 볼 수 있습니다:

```
📅 스케줄러 테스트 프로젝트

✅ 스케줄러가 실행 중입니다!
이 애플리케이션이 시작되면 자동으로 스케줄러가 등록됩니다.

• 첫 실행: 애플리케이션 시작 후 10초 후
• 반복 주기: 60초마다
• 실행 내용: 데이터베이스 작업 + API 호출 시뮬레이션

[🚀 스케줄러 수동 실행]
```

콘솔에서는 60초마다 자동으로 작업이 실행되는 로그를 확인할 수 있습니다!
