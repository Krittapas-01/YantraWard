@echo off
REM Run YantraWard without NetBeans.
REM Prefers a modern Java (9+) if installed: it renders sharp on high-DPI screens.
REM Falls back to whatever "java" is on PATH (Java 8 works too, but may look blurry/small).
setlocal
cd /d "%~dp0"
set "JAVA_EXE=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" call :check "%JAVA_HOME%\bin\java.exe"
for /d %%D in ("%ProgramFiles%\Java\jdk-*" "%ProgramFiles%\Java\jre-*" "%ProgramFiles%\Eclipse Adoptium\*" "%ProgramFiles%\Microsoft\jdk-*" "%ProgramFiles%\Zulu\zulu-*" "%ProgramFiles%\Amazon Corretto\jdk*" "%ProgramFiles%\Android\Android Studio\jbr" "%ProgramFiles%\Apache NetBeans\jdk") do (
    if exist "%%~D\bin\java.exe" call :check "%%~D\bin\java.exe"
)
echo Using: %JAVA_EXE%
"%JAVA_EXE%" -jar YantraWard.jar
if errorlevel 1 pause
goto :eof

:check
REM skip Java 8 and older (their version string starts with "1.")
"%~1" -version 2>&1 | findstr /r /c:"version .1\." >nul
if errorlevel 1 set "JAVA_EXE=%~1"
goto :eof
