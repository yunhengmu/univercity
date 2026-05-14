@echo off
chcp 65001 >nul
echo ========================================
echo   Nginx 停止脚本
echo ========================================
echo.

REM 检查 Nginx 是否正在运行
tasklist | findstr nginx >nul
if %errorlevel% neq 0 (
    echo [信息] Nginx 未运行
    pause
    exit /b 0
)

echo [信息] 正在停止 Nginx...
nginx -s stop

if %errorlevel% equ 0 (
    echo [成功] Nginx 已停止
) else (
    echo [警告] 使用常规方法停止失败，尝试强制停止...
    
    REM 获取所有 nginx 进程 ID
    for /f "tokens=2" %%a in ('tasklist ^| findstr nginx') do (
        echo [信息] 终止进程: %%a
        taskkill /PID %%a /F >nul 2>&1
    )
    
    echo [成功] Nginx 已强制停止
)

echo.
pause
