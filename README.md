# 스케줄러 프로젝트

한계보유시간 초과 공정 알림 스케줄러입니다.  
SP 호출 → 업무연락 생성 → FCM 푸시 발송까지 자동으로 처리합니다.

---

## 스케줄 구성

| 클래스 | 실행 주기 | 실행 조건 |
|--------|----------|----------|
| `DaySchedulerTask` | 매일 오전 9시 | 월요일만 실행 |
| `HourSchedulerTask` | 매시 정각 · 30분 | 평일만 실행 (토/일 제외) |

---

## 프로젝트 구조

```
scheduler-test/
├── src/com/scheduler/
│   ├── SchedulerContextListener.java  스케줄러 초기화 (톰캣 기동 시 자동 실행)
│   ├── DaySchedulerTask.java          일 단위 작업 (월요일 오전 9시)
│   ├── HourSchedulerTask.java         시간 단위 작업 (평일 매 30분)
│   ├── Config.java                    환경별 설정 로더
│   └── ManualExecutor.java            수동 실행 서블릿 (/manual)
├── WEB-INF/
│   ├── web.xml
│   ├── classes/                       컴파일 결과물 (빌드 후 생성)
│   └── lib/                           라이브러리 (별도 관리)
├── config.properties.example          설정 파일 템플릿
├── build.bat                          Windows 빌드 스크립트
├── build.sh                           Linux 빌드 스크립트
└── README.md
```

---

## 환경 설정

`config.properties.example`을 복사하여 환경별 설정 파일을 생성합니다.

```
config.properties.example → config-dev.properties   (개발)
config.properties.example → config-prod.properties  (운영)
```

```properties
# DB 접속 정보
db.url=jdbc:sqlserver://[HOST];databaseName=[DB_NAME];encrypt=false;trustServerCertificate=true;
db.username=[DB_USERNAME]
db.password=[DB_PASSWORD]

# FCM API
fcm.url=http://[HOST]:[PORT]/api/Fcm/FcmPassivity
```

> `config-dev.properties`, `config-prod.properties`는 민감정보로 gitignore 처리되어 있습니다.

---

## 빌드

### Windows
```powershell
# 개발 빌드
.\build.bat dev

# 운영 빌드
.\build.bat prod
```

### Linux
```bash
# 개발 빌드
./build.sh dev

# 운영 빌드
./build.sh prod
```

빌드 완료 시 `WEB-INF/classes/` 안에 컴파일된 클래스 파일과 `config.properties`가 생성됩니다.

---

## 배포

빌드 후 `WEB-INF/classes/` 폴더를 톰캣 서버에 반영합니다.

---

## 테스트 모드

`SchedulerContextListener.java`에서 `TEST_MODE` 값을 변경합니다.

```java
// true: 10초 후 첫 실행, 이후 60초마다 반복
// false: 운영 스케줄 적용 (기본값)
private static final boolean TEST_MODE = false;
```

---

## 수동 실행

톰캣 기동 후 아래 URL로 `DaySchedulerTask`를 즉시 실행할 수 있습니다.

```
http://[HOST]:[PORT]/scheduler-test/manual
```
