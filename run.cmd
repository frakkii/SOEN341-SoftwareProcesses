@echo off
rem Start CareerConnect from the repo root: run (or .\run in PowerShell)
pushd "%~dp0src\backend"
call mvnw.cmd spring-boot:run %*
popd
