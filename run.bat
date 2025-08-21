@echo off
echo PDF417 条码生成器启动中...
echo.

REM 检查是否安装了Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请确保已安装Maven并添加到PATH环境变量中
    echo 您可以从 https://maven.apache.org/download.cgi 下载Maven
    pause
    exit /b 1
)

REM 检查是否安装了Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Java，请确保已安装Java 11或更高版本
    pause
    exit /b 1
)

echo 正在编译项目...
mvn compile
if %errorlevel% neq 0 (
    echo 编译失败，请检查错误信息
    pause
    exit /b 1
)

echo 启动PDF417条码生成器...
mvn exec:java

pause