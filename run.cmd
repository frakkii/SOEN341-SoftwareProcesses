@echo off
rem Start CareerConnect from the repo root: run (or .\run in PowerShell)
rem Checks the two most common reasons the app fails to start, so the error is
rem clear instead of Maven's generic "Process terminated with exit code: 1".

rem 1. No database password (not needed when SUPABASE_DB_URL points somewhere else)
if "%SUPABASE_DB_URL%"=="" if "%SUPABASE_DB_PASSWORD%"=="" (
    echo.
    echo SUPABASE_DB_PASSWORD is not set in this terminal, so the app can't connect to the database.
    echo Set it first ^(README, step 2^), then run again:
    echo   PowerShell:      $env:SUPABASE_DB_PASSWORD='soen341proj'
    echo   Command Prompt:  set SUPABASE_DB_PASSWORD=soen341proj
    echo.
    exit /b 1
)

rem 2. Another copy of the app is already using port 8081
netstat -ano | findstr /r /c:":8081 .*LISTENING" >nul
if not errorlevel 1 (
    echo.
    echo Port 8081 is already in use, probably by another copy of the app ^(another terminal or your IDE^).
    echo Run "stop" ^(or .\stop in PowerShell^) to close it, then run again.
    echo.
    exit /b 1
)

pushd "%~dp0src\backend"
rem Full path: works even when Windows is set not to run programs from the current folder
call "%~dp0src\backend\mvnw.cmd" spring-boot:run %*
popd
