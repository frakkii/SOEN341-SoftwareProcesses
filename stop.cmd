@echo off
rem Stop CareerConnect from the repo root: stop (or .\stop in PowerShell)
rem Pass a port to stop a copy running elsewhere: stop 8082
setlocal
set PORT=%1
if "%PORT%"=="" set PORT=8081
set FOUND=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%PORT% .*LISTENING"') do (
    taskkill /PID %%p /F >nul 2>&1
    set FOUND=1
)
if defined FOUND (echo Stopped the app on port %PORT%.) else (echo Nothing is running on port %PORT%.)
endlocal
