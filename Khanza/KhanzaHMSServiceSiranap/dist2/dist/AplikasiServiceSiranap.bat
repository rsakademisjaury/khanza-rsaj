@echo off
cd /d "%~dp0"

set "APPJAR=KhanzaHMSServiceSiranap.jar"
if not exist "%APPJAR%" (
    echo File %APPJAR% tidak ditemukan di folder yang sama dengan BAT.
    pause
    exit /b 1
)

if not exist "lib" (
    echo Folder lib tidak ditemukan di folder dist.
    pause
    exit /b 1
)

if not exist "setting\database.xml" (
    echo File setting\database.xml tidak ditemukan.
    echo Salin folder setting ke dalam folder dist sehingga menjadi dist\setting\database.xml
    pause
    exit /b 1
)

java -cp "%APPJAR%;lib/*" khanzahmsservicesiranap.KhanzaHMSServiceSiranap

echo.
echo Program selesai atau terjadi error. Silakan baca pesan di atas.
pause
