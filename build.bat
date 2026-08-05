@echo off

echo ==========================================
echo Scheduler Build
echo ==========================================

SET ENV=%1
IF "%ENV%"=="" SET ENV=dev

IF NOT "%ENV%"=="dev" IF NOT "%ENV%"=="prod" (
    echo ERROR: Invalid environment [%ENV%]. Use dev or prod.
    exit /b 1
)

echo ENV: %ENV%
echo.

echo [1/2] Compiling...
javac -encoding UTF-8 -d WEB-INF\classes -cp "WEB-INF\lib\*" src\com\scheduler\*.java

IF %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compile failed.
    exit /b 1
)
echo OK: Compile done.
echo.

echo [2/2] Copying config...
copy /Y config-%ENV%.properties WEB-INF\classes\config.properties > nul

IF %ERRORLEVEL% NEQ 0 (
    echo ERROR: Config copy failed.
    exit /b 1
)
echo OK: config-%ENV%.properties copied.
echo.

echo ==========================================
echo BUILD SUCCESS [%ENV%]
echo ==========================================
