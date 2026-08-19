@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM ============================================================
REM  Aplikasi_robust.bat - Starter aman (x86 & x64) untuk SIMRS Khanza
REM  - Selalu start dari folder skrip
REM  - Deteksi OS 32/64-bit → pilih JRE yang tepat (.\jre32 atau .\jre)
REM  - Heap otomatis: 32-bit pakai -Xmx768m (default), 64-bit pakai -Xmx2048m (default)
REM  - Bisa override via env: KHANZA_XMX (contoh: set KHANZA_XMX=1024)
REM  - Jalankan terpisah (detached) dengan START agar updater bisa exit
REM ============================================================

cd /d "%~dp0"

REM --- Deteksi 32/64-bit OS ---
set "BIT=64"
if /i "%PROCESSOR_ARCHITECTURE%"=="x86" if "%PROCESSOR_ARCHITEW6432%"=="" set "BIT=32"

REM --- Tentukan JAVA_EXE (prioritas: jre32 utk x86, jre utk x64, lalu PATH) ---
set "JAVA_EXE="
if "%BIT%"=="32" (
    if exist "%~dp0jre32\bin\java.exe" set "JAVA_EXE=%~dp0jre32\bin\java.exe"
) else (
    if exist "%~dp0jre\bin\java.exe" set "JAVA_EXE=%~dp0jre\bin\java.exe"
)
if not defined JAVA_EXE (
    for %%J in (java.exe) do (
        if "%%~$PATH:J" neq "" set "JAVA_EXE=%%~$PATH:J"
    )
)

if not defined JAVA_EXE (
    echo [ERROR] Java tidak ditemukan.
    echo - Untuk Windows 32-bit: taruh JRE 32-bit di .\jre32\
    echo - Untuk Windows 64-bit: taruh JRE 64-bit di .\jre\
    echo - Atau pastikan JAVA ada di PATH.
    pause
    exit /b 1
)

REM --- Normalisasi nama jar (opsional) ---
if not exist "khanza.jar" (
    if exist "SIMRSKhanza.jar" ren "SIMRSKhanza.jar" "khanza.jar"
)

if not exist "khanza.jar" (
    echo [ERROR] File "khanza.jar" tidak ditemukan di: "%cd%"
    pause
    exit /b 2
)

REM --- Tentukan XMX ---
set "XMX=%KHANZA_XMX%"
if not defined XMX (
    if "%BIT%"=="32" ( set "XMX=768" ) else ( set "XMX=2048" )
)

REM --- Opsi memory Java ---
set "JAVA_OPTS=-Xss2m -Xms64m -Xmx%XMX%m"

REM --- Jalankan terpisah (detached) ---
start "" "%JAVA_EXE%" %JAVA_OPTS% -jar "khanza.jar"
exit /b 0
