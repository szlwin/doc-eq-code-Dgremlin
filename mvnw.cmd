@echo off
setlocal
set BASE_DIR=%~dp0
set PROPS=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties
for /f "tokens=2 delims==" %%A in ('findstr /b "distributionUrl=" "%PROPS%"') do set DIST_URL=%%A
for %%A in (%DIST_URL%) do set FILE_NAME=%%~nxA
set MAVEN_VERSION=%FILE_NAME:apache-maven-=%
set MAVEN_VERSION=%MAVEN_VERSION:-bin.zip=%
if "%MAVEN_USER_HOME%"=="" set MAVEN_USER_HOME=%USERPROFILE%\.m2
set MAVEN_HOME=%MAVEN_USER_HOME%\wrapper\dists\apache-maven-%MAVEN_VERSION%
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $u='%DIST_URL%'; $z=Join-Path $env:TEMP 'maven-wrapper.zip'; Invoke-WebRequest -Uri $u -OutFile $z; $d=Join-Path $env:TEMP 'maven-wrapper-unpack'; Remove-Item $d -Recurse -Force -ErrorAction Ignore; Expand-Archive $z $d; New-Item -ItemType Directory -Force -Path (Split-Path '%MAVEN_HOME%') | Out-Null; Move-Item (Join-Path $d 'apache-maven-%MAVEN_VERSION%') '%MAVEN_HOME%'"
  if errorlevel 1 exit /b 1
)
call "%MAVEN_HOME%\bin\mvn.cmd" -f "%BASE_DIR%pom.xml" %*
