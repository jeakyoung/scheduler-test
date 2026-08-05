#!/bin/bash

echo "=========================================="
echo "스케줄러 테스트 프로젝트 빌드"
echo "=========================================="

# 빌드 환경 설정 (기본값: dev)
ENV=${1:-dev}

if [ "$ENV" != "dev" ] && [ "$ENV" != "prod" ]; then
    echo "❌ 잘못된 환경값: $ENV (dev 또는 prod 만 가능)"
    exit 1
fi

echo "📦 빌드 환경: $ENV"
echo ""

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

# 3. 환경별 설정 파일 복사
echo ""
echo "설정 파일 복사 중 (config-${ENV}.properties → config.properties)..."
cp config-${ENV}.properties WEB-INF/classes/config.properties

if [ $? -eq 0 ]; then
    echo "✓ 설정 파일 복사 완료"
else
    echo "✗ 설정 파일 복사 실패"
    exit 1
fi

# 4. WAR 파일 생성
echo ""
echo "WAR 파일 생성 중..."
WAR_NAME="scheduler-${ENV}.war"
cd WEB-INF/classes
jar -cf ../../${WAR_NAME} \
    -C ../.. index.html \
    -C ../.. WEB-INF/ \
    com/

if [ $? -eq 0 ]; then
    cd ../..
    echo "✓ WAR 파일 생성 완료: ${WAR_NAME}"
else
    echo "✗ WAR 파일 생성 실패"
    exit 1
fi

echo ""
echo "=========================================="
echo "빌드 완료! [$ENV]"
echo "=========================================="
echo ""
