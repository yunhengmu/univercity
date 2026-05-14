@echo off
chcp 65001 >nul
echo ========================================
echo   Nginx SSL 启动脚本
echo ========================================
echo.

REM 检查 Nginx 是否已安装
where nginx >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Nginx，请先安装 Nginx
    echo.
    echo 下载地址: https://nginx.org/en/download.html
    echo 或使用 Chocolatey: choco install nginx
    echo.
    pause
    exit /b 1
)

REM 获取当前脚本所在目录
set "SCRIPT_DIR=%~dp0"
set "CONF_DIR=%SCRIPT_DIR%conf"

echo [信息] 配置文件目录: %CONF_DIR%
echo.

REM 检查证书文件是否存在
if not exist "%CONF_DIR%\cert.pem" (
    echo [错误] 找不到证书文件: cert.pem
    pause
    exit /b 1
)

if not exist "%CONF_DIR%\key.pem" (
    echo [错误] 找不到私钥文件: key.pem
    pause
    exit /b 1
)

if not exist "%CONF_DIR%\nginx.conf" (
    echo [错误] 找不到配置文件: nginx.conf
    pause
    exit /b 1
)

echo [成功] 所有证书和配置文件已就绪
echo.

REM 测试配置文件
echo [信息] 正在测试 Nginx 配置...
nginx -t -c "%CONF_DIR%\nginx.conf"
if %errorlevel% neq 0 (
    echo.
    echo [错误] Nginx 配置测试失败，请检查配置文件
    pause
    exit /b 1
)

echo.
echo [成功] Nginx 配置测试通过
echo.

REM 检查 9002 端口是否被占用
netstat -ano | findstr :9002 >nul
if %errorlevel% equ 0 (
    echo [警告] 端口 9002 已被占用
    echo.
    echo 占用情况:
    netstat -ano | findstr :9002
    echo.
    set /p choice="是否继续启动？(Y/N): "
    if /i not "%choice%"=="Y" (
        echo [信息] 已取消启动
        pause
        exit /b 0
    )
)

echo [信息] 正在启动 Nginx...
echo.

REM 启动 Nginx
start "" nginx -c "%CONF_DIR%\nginx.conf"

if %errorlevel% equ 0 (
    echo [成功] Nginx 已启动！
    echo.
    echo HTTPS 地址: https://localhost:9002
    echo.
    echo 注意: 由于使用自签名证书，浏览器会显示安全警告，点击"继续访问"即可
    echo.
    echo 常用命令:
    echo   停止 Nginx: nginx -s stop
    echo   重载配置: nginx -s reload
    echo   查看状态: tasklist ^| findstr nginx
    echo.
) else (
    echo [错误] Nginx 启动失败
    echo.
)

pause
