#!/bin/bash

echo "=========================================="
echo "스케줄러 테스트 프로젝트 빌드"
echo "=========================================="

# 1. Servlet API 다운로드 (없는 경우)
if [ ! -f "WEB-INF/lib/javax.servlet-api-3.1.0.jar" ]; then
    echo "Servlet API 다운로드 중..."
    curl -L -o WEB-INF/lib/javax.servlet-api-3.1.0.jar \
        https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar
    echo "✓ Servlet API 다운로드 완료"
fi

# 2. 컴파일
echo ""
echo "Java 파일 컴파일 중..."
javac -d WEB-INF/classes \
      -cp "WEB-INF/lib/*" \
      src/com/scheduler/*.java

if [ $? -eq 0 ]; then
    echo "✓ 컴파일 완료"
else
    echo "✗ 컴파일 실패"
    exit 1
fi

# 3. WAR 파일 생성
echo ""
echo "WAR 파일 생성 중..."
cd WEB-INF/classes
jar -cf ../../scheduler-test.war \
    -C ../.. index.html \
    -C ../.. WEB-INF/ \
    com/

if [ $? -eq 0 ]; then
    cd ../..
    echo "✓ WAR 파일 생성 완료: scheduler-test.war"
else
    echo "✗ WAR 파일 생성 실패"
    exit 1
fi

echo ""
echo "=========================================="
echo "빌드 완료!"
echo "=========================================="
echo ""
echo "다음 단계:"
echo "1. Tomcat의 webapps 폴더에 scheduler-test.war 복사"
echo "2. Tomcat 시작: catalina start"
echo "3. 브라우저 접속: http://localhost:8080/scheduler-test"
echo ""
