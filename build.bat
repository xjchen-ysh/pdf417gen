@echo off
echo ========================================
echo PDF417 条码生成器 - 打包脚本
echo ========================================

echo 检查Maven是否已安装...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请确保Maven已安装并添加到PATH环境变量中
    pause
    exit /b 1
)

echo 检查Java是否已安装...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java，请确保Java已安装并添加到PATH环境变量中
    pause
    exit /b 1
)

echo.
echo 开始清理项目...
mvn clean

echo.
echo 开始编译项目...
mvn compile

echo.
echo 开始打包项目...
mvn package

echo.
echo 检查生成的文件...
if exist "target\PDF417Generator.jar" (
    echo ✓ 成功生成 PDF417Generator.jar
) else (
    echo ✗ 未找到 PDF417Generator.jar
)

if exist "target\PDF417Generator.exe" (
    echo ✓ 成功生成 PDF417Generator.exe
    echo.
    echo ========================================
    echo 打包完成！
    echo ========================================
    echo 可执行文件位置: target\PDF417Generator.exe
    echo 可执行JAR位置: target\PDF417Generator.jar
    echo.
    echo 用户可以直接双击 PDF417Generator.exe 运行程序
    echo （需要系统安装Java 11或更高版本）
) else (
    echo ✗ 未找到 PDF417Generator.exe
    echo 可能是Launch4j插件配置有问题
)

echo.
pause