SERVICE PANTAU FINGER BPJS v1 - RS AKADEMIS JAURY
================================================

Tujuan
------
Service ini dibuat dari pola console service Aplicare, tetapi KHUSUS membaca antrean lokal
yang dibuat tombol "Pantau Finger BPJS" pada DlgReg. Service tidak menyisir seluruh pasien
BPJS dan tidak melakukan polling massal.

Alur
----
1. Loket memilih pasien BPJS belum finger pada DlgReg dan klik Pantau Finger BPJS.
2. DlgReg menyimpan status ACTIVE ke tabel bpjs_finger_monitor.
3. Service membaca satu antrean ACTIVE yang sudah due.
4. Service melakukan maksimal 1 request BPJS pada setiap jadwal retry.
5. Bila BPJS mengembalikan kode finger = 1, service menyimpan ke sidik_jari_bpjs.
6. Refresh tabel DlgReg akan membaca sidik_jari_bpjs dan warna penanda dapat berubah.

Pengaman hit BPJS
-----------------
- Hanya antrean yang dipicu tombol loket yang diproses.
- Satu instance service saja melalui MySQL GET_LOCK.
- Satu pasien: maksimal 4 hit per sesi tombol.
- Retry: segera, kemudian 30 detik, 60 detik, 120 detik.
- Setelah gagal 4 kali: STOPPED; loket harus klik Pantau Finger lagi bila diperlukan.
- Seluruh rencana request dicatat ke bpjs_finger_hit_log; bila audit log gagal ditulis, request BPJS dibatalkan.
- Circuit breaker internal: maksimum 1000 hit per hari berdasarkan bpjs_finger_hit_log. Nilai dapat diubah pada
  MAX_HIT_HARIAN dalam FingerBPJSConsoleService.java setelah disepakati dengan BPJS.
- Bila SEP sudah terbit atau data finger sudah ada, antrean ditutup tanpa hit baru.
- Bila finger sudah valid tetapi simpan lokal gagal, status FINGER_VALID_PENDING_SAVE mengulang penyimpanan lokal tanpa request BPJS baru.

Cara memasang
-------------
1. Jalankan database/create_table_bpjs_finger_monitor.sql pada database Khanza.
2. Gunakan DlgReg_PantauFingerBPJS_v2_SimpanAntrean.zip yang sudah dibuat sebelumnya.
3. Salin file setting/database.xml dari service Aplicare yang berjalan ke folder setting/
   service ini. File setting sengaja tidak disertakan di ZIP ini agar kredensial tidak
   tersebar ulang. Key URLAPIBPJS, CONSIDAPIBPJS, SECRETKEYAPIBPJS dan USERKEYAPIBPJS
   sudah digunakan oleh source service.
4. Jalankan jalankan_service_finger.bat dari folder utama project.
5. Tes dengan satu pasien BPJS terlebih dahulu. Pantau output Command Prompt dan query:
   SELECT * FROM bpjs_finger_monitor ORDER BY id DESC;
   SELECT * FROM sidik_jari_bpjs ORDER BY validasi DESC;
   SELECT * FROM bpjs_finger_hit_log ORDER BY id DESC;

Catatan penting
---------------
- Jangan menjalankan URL Laravel /get_finger lama bersamaan dengan service baru.
- Hentikan cron/auto-refresh eksternal yang masih memanggil /get_finger.
- Respons VClaim diproses melalui decrypt AES + dekompresi LZString di BPJSApiFinger.java.
- Saya belum melakukan hit nyata ke BPJS dari lingkungan ini; validasi endpoint akhir harus
  dilakukan di server RS dengan satu pasien uji dan monitoring log.

Isi folder
----------
- dist/KhanzaHMSServiceFingerBPJS.jar : aplikasi console siap dijalankan
- dist/lib/                            : library yang diperlukan
- src/                                 : source revisi
- database/                            : SQL antrean lokal
- setting/                             : tempat menaruh database.xml milik service berjalan
