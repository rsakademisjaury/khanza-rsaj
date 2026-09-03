#!/usr/bin/env sh
cd "$(dirname "$0")"
echo "Menjalankan Service Pantau Finger BPJS..."
java -cp "KhanzaHMSServiceFingerBPJS.jar:lib/*" khanzahmsservicefinger.KhanzaHMSServiceFingerBPJS
