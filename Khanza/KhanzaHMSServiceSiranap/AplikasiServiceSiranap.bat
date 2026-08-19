@echo off
cd /d "%~dp0"
title SIMKES Khanza Service Siranap - Live Console
color 07
mode con: cols=140 lines=40

echo ================================================================================================================
echo   SIMKES KHANZA SERVICE SIRANAP
echo   Mode      : Console Only
echo   Status    : Menjalankan service dan menampilkan log real-time di terminal
echo ================================================================================================================
echo.

setlocal EnableDelayedExpansion
set "CP=KhanzaHMSServiceSiranap.jar"

for %%F in (lib\*.jar) do (
    set "FN=%%~nxF"
    if /I not "!FN!"=="commons-codec-1.10.jar" if /I not "!FN!"=="js_commons-codec-1.3.jar" if /I not "!FN!"=="org-apache-commons-codec.jar" (
        set "CP=!CP!;%%F"
    )
)

java -Xss2m -Xms32m -Xmx1024m -cp "%CP%" khanzahmsservicesiranap.KhanzaHMSServiceSiranap

echo.
echo Program selesai atau terjadi error. Silakan baca pesan di atas.
pause
