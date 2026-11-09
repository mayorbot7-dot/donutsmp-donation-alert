@echo off
setlocal
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle -p "%~dp0" %*
  exit /b %ERRORLEVEL%
)
set VERSION=8.10.2
set CACHE=%USERPROFILE%\.gradle\bootstrap\gradle-%VERSION%
if not exist "%CACHE%\bin\gradle.bat" (
  set ZIP=%TEMP%\gradle-%VERSION%-bin.zip
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%TEMP%\gradle-%VERSION%-bin.zip'; Expand-Archive -Force '%TEMP%\gradle-%VERSION%-bin.zip' '%USERPROFILE%\.gradle\bootstrap'"
  if errorlevel 1 exit /b 1
)
call "%CACHE%\bin\gradle.bat" -p "%~dp0" %*
exit /b %ERRORLEVEL%
