@echo off
cd /d "%~dp0"
echo Menjalankan Service Pantau Finger BPJS...
java -cp "KhanzaHMSAnjungan.jar;lib/*" khanzahmsservicefinger.KhanzaHMSServiceFingerBPJS
pause
