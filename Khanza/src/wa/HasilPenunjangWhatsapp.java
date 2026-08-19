package wa;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fungsi.koneksiDBWA;
import fungsi.koneksiDB;
import fungsi.akses;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Pengiriman satu PDF hasil Lab/Radiologi melalui Fonnte.
 * Browser hanya dipakai sebagai popup input nomor; token Fonnte dan file PDF
 * tetap diproses di aplikasi Java, tidak pernah dikirim ke browser.
 */
public final class HasilPenunjangWhatsapp {

    private HasilPenunjangWhatsapp() {
    }

    public static final class Berkas {
        public final String id;
        public final String url;
        public final String nama;
        /** Lokasi asli pada berkas_digital_perawatan, untuk histori per berkas. */
        public final String lokasiFile;
        /** No. Rawat sumber berkas; bisa berbeda dari kunjungan aktif bila memilih riwayat. */
        public final String noRawatSumber;
        /** Kode master berkas, 005 Laboratorium atau 006 Radiologi. */
        public final String kodeBerkas;

        public Berkas(String id, String url, String nama) {
            this(id, url, nama, "", "", "");
        }

        public Berkas(String id, String url, String nama, String lokasiFile,
                String noRawatSumber, String kodeBerkas) {
            this.id = id == null ? "" : id;
            this.url = url == null ? "" : url;
            this.nama = nama == null ? "" : nama;
            this.lokasiFile = lokasiFile == null ? "" : lokasiFile;
            this.noRawatSumber = noRawatSumber == null ? "" : noRawatSumber;
            this.kodeBerkas = kodeBerkas == null ? "" : kodeBerkas;
        }
    }

    /** Status pengiriman satu berkas hasil ke pasien. */
    public static final class StatusKirim {
        public final boolean sudahTerkirim;
        public final String waktuKirim;

        public StatusKirim(boolean sudahTerkirim, String waktuKirim) {
            this.sudahTerkirim = sudahTerkirim;
            this.waktuKirim = waktuKirim == null ? "" : waktuKirim;
        }
    }

    private static final class Sesi {
        private final String noRawat;
        private final String namaPasien;
        private final String noHpAwal;
        private final String judul;
        private final Map<String, Berkas> berkas;
        private final long dibuat;

        private Sesi(String noRawat, String namaPasien, String noHpAwal, String judul, List<Berkas> daftar) {
            this.noRawat = noRawat == null ? "" : noRawat;
            this.namaPasien = namaPasien == null ? "" : namaPasien;
            this.noHpAwal = noHpAwal == null ? "" : noHpAwal;
            this.judul = judul == null ? "Hasil Pemeriksaan" : judul;
            this.berkas = new LinkedHashMap<String, Berkas>();
            if (daftar != null) {
                for (Berkas item : daftar) {
                    if (item != null && !item.id.trim().equals("") && !item.url.trim().equals("")) {
                        this.berkas.put(item.id, item);
                    }
                }
            }
            this.dibuat = System.currentTimeMillis();
        }
    }

    private static final ConcurrentHashMap<String, Sesi> SESI = new ConcurrentHashMap<String, Sesi>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Object LOCK_SERVER = new Object();
    private static volatile HttpServer server;
    private static volatile int portServer = -1;
    private static final long MASA_AKTIF_SESI = 20L * 60L * 1000L;
    private static final long BATAS_PDF_AMAN = 3_800_000L;
    private static final String TABEL_STATUS_KIRIM = "wa_hasil_penunjang_kirim";

    /** Mendaftarkan daftar hasil yang boleh dikirim dan mengembalikan token sesi acak. */
    public static String buatSesi(String noRawat, String namaPasien, String noHpAwal,
            String judul, List<Berkas> daftar) throws IOException {
        pastikanServerAktif();
        bersihkanSesiLama();
        byte[] token = new byte[18];
        RANDOM.nextBytes(token);
        String id = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        SESI.put(id, new Sesi(noRawat, namaPasien, noHpAwal, judul, daftar));
        return id;
    }

    /** Endpoint popup browser lokal. Hanya bind ke 127.0.0.1. */
    public static String getEndpointPopup() {
        if (portServer < 1) return "";
        return "http://127.0.0.1:" + portServer + "/hasil-wa/popup";
    }

    /** Alamat popup browser lokal. Hanya bind ke 127.0.0.1. */
    public static String getUrlPopup(String sesi, List<String> ids) {
        if (sesi == null || sesi.trim().equals("") || portServer < 1) return "";
        StringBuilder gabung = new StringBuilder();
        if (ids != null) {
            for (String id : ids) {
                if (id == null || id.trim().equals("")) continue;
                if (gabung.length() > 0) gabung.append(',');
                gabung.append(id.trim());
            }
        }
        try {
            return "http://127.0.0.1:" + portServer + "/hasil-wa/popup?sesi="
                    + URLEncoder.encode(sesi, "UTF-8") + "&ids="
                    + URLEncoder.encode(gabung.toString(), "UTF-8");
        } catch (Exception ex) {
            return "";
        }
    }

    private static void pastikanServerAktif() throws IOException {
        if (server != null && portServer > 0) return;
        synchronized (LOCK_SERVER) {
            if (server != null && portServer > 0) return;
            HttpServer baru = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            baru.createContext("/hasil-wa/popup", new PopupHandler());
            baru.createContext("/hasil-wa/kirim", new KirimHandler());
            baru.setExecutor(Executors.newCachedThreadPool(new ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Khanza-Hasil-WA");
                    t.setDaemon(true);
                    return t;
                }
            }));
            baru.start();
            server = baru;
            portServer = baru.getAddress().getPort();
        }
    }

    private static void bersihkanSesiLama() {
        long batas = System.currentTimeMillis() - MASA_AKTIF_SESI;
        for (Map.Entry<String, Sesi> e : SESI.entrySet()) {
            if (e.getValue() == null || e.getValue().dibuat < batas) SESI.remove(e.getKey());
        }
    }

    /**
     * Membaca histori kirim per berkas. Jika tabel belum dibuat, kembalikan status
     * kosong agar halaman hasil tetap dapat dibuka; jalankan file SQL patch sekali.
     */
    public static StatusKirim ambilStatusKirim(String noRawat, String kodeBerkas, String lokasiFile) {
        if (kosong(noRawat) || kosong(kodeBerkas) || kosong(lokasiFile)) {
            return new StatusKirim(false, "");
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection koneksi = koneksiDB.condb();
            ps = koneksi.prepareStatement(
                    "select date_format(tgl_kirim,'%d/%m/%Y %H:%i') as waktu_kirim "
                    + "from " + TABEL_STATUS_KIRIM + " "
                    + "where no_rawat=? and kode=? and berkas_hash=? "
                    + "order by tgl_kirim desc limit 1");
            ps.setString(1, noRawat.trim());
            ps.setString(2, kodeBerkas.trim());
            ps.setString(3, hashBerkas(lokasiFile));
            rs = ps.executeQuery();
            if (rs.next()) return new StatusKirim(true, rs.getString("waktu_kirim"));
        } catch (Exception ex) {
            // Tidak menghentikan tampilan hasil bila tabel histori belum di-import.
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
        }
        return new StatusKirim(false, "");
    }

    /** Menyimpan catatan sukses untuk setiap gambar yang tergabung pada PDF. */
    private static void catatBerkasTerkirim(List<Berkas> daftar, String noHp) throws Exception {
        if (daftar == null || daftar.isEmpty()) return;
        Connection koneksi = koneksiDB.condb();
        PreparedStatement update = null;
        PreparedStatement insert = null;
        try {
            update = koneksi.prepareStatement(
                    "update " + TABEL_STATUS_KIRIM + " set no_hp=?, dikirim_oleh=?, tgl_kirim=now() "
                    + "where no_rawat=? and kode=? and berkas_hash=?");
            insert = koneksi.prepareStatement(
                    "insert into " + TABEL_STATUS_KIRIM
                    + " (no_rawat,kode,lokasi_file,berkas_hash,no_hp,dikirim_oleh,tgl_kirim) "
                    + "values (?,?,?,?,?,?,now())");
            String pengirim = "";
            try { pengirim = akses.getkode(); } catch (Exception ex) {}
            for (Berkas berkas : daftar) {
                if (berkas == null || kosong(berkas.noRawatSumber) || kosong(berkas.kodeBerkas)
                        || kosong(berkas.lokasiFile)) continue;
                String hash = hashBerkas(berkas.lokasiFile);
                update.setString(1, noHp);
                update.setString(2, pengirim);
                update.setString(3, berkas.noRawatSumber);
                update.setString(4, berkas.kodeBerkas);
                update.setString(5, hash);
                int jumlah = update.executeUpdate();
                if (jumlah == 0) {
                    insert.setString(1, berkas.noRawatSumber);
                    insert.setString(2, berkas.kodeBerkas);
                    insert.setString(3, berkas.lokasiFile);
                    insert.setString(4, hash);
                    insert.setString(5, noHp);
                    insert.setString(6, pengirim);
                    insert.executeUpdate();
                }
            }
        } finally {
            try { if (update != null) update.close(); } catch (Exception ex) {}
            try { if (insert != null) insert.close(); } catch (Exception ex) {}
        }
    }

    /**
     * Mengembalikan true bila seluruh berkas pilihan sudah memiliki catatan kirim.
     * Nilai ini hanya dipakai untuk label "Kirim Ulang"; pengiriman ulang tetap diizinkan.
     */
    private static boolean semuaBerkasSudahTerkirim(List<Berkas> daftar) {
        if (daftar == null || daftar.isEmpty()) return false;
        for (Berkas berkas : daftar) {
            if (berkas == null) return false;
            StatusKirim status = ambilStatusKirim(berkas.noRawatSumber, berkas.kodeBerkas, berkas.lokasiFile);
            if (!status.sudahTerkirim) return false;
        }
        return true;
    }

    private static boolean kosong(String nilai) {
        return nilai == null || nilai.trim().equals("");
    }

    private static String hashBerkas(String lokasiFile) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] data = digest.digest((lokasiFile == null ? "" : lokasiFile.trim().replace('\\', '/'))
                .getBytes(StandardCharsets.UTF_8));
        StringBuilder hasil = new StringBuilder();
        for (byte b : data) hasil.append(String.format(Locale.US, "%02x", b & 0xff));
        return hasil.toString();
    }

    private static final class PopupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                kirimHtml(exchange, 405, halamanPesan("Metode tidak diizinkan", "Silakan tutup halaman ini lalu buka kembali dari aplikasi."));
                return;
            }
            Map<String, String> q = parseParameter(exchange.getRequestURI().getRawQuery());
            Sesi sesi = SESI.get(q.get("sesi"));
            List<Berkas> pilihan = ambilPilihan(sesi, q.get("ids"));
            if (sesi == null || pilihan.isEmpty()) {
                kirimHtml(exchange, 400, halamanPesan("Pilihan berkas tidak tersedia", "Tutup halaman ini, centang kembali berkas hasil, lalu klik Kirim WA Pasien."));
                return;
            }
            kirimHtml(exchange, 200, halamanPopup(sesi, q.get("sesi"), q.get("ids"), pilihan));
        }
    }

    private static final class KirimHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                kirimHtml(exchange, 405, halamanPesan("Metode tidak diizinkan", "Silakan tutup halaman ini."));
                return;
            }
            String body = bacaTeks(exchange.getRequestBody());
            Map<String, String> form = parseParameter(body);
            Sesi sesi = SESI.get(form.get("sesi"));
            List<Berkas> pilihan = ambilPilihan(sesi, form.get("ids"));
            String noHp = normalisasiNomor(form.get("no_hp"));
            if (sesi == null || pilihan.isEmpty()) {
                kirimHtml(exchange, 400, halamanPesan("Sesi pengiriman telah berakhir", "Tutup popup, lalu pilih berkas lagi dari halaman hasil."));
                return;
            }
            if (noHp.equals("")) {
                kirimHtml(exchange, 400, halamanPesan("Nomor WhatsApp wajib diisi", "Isi nomor tujuan lalu klik Kirim Hasil kembali."));
                return;
            }

            // Pengiriman ulang tetap diizinkan. Status kirim hanya menjadi penanda
            // bagi petugas; saat dikirim ulang waktu dan nomor tujuan terakhir diperbarui.
            boolean kirimUlang = semuaBerkasSudahTerkirim(pilihan);

            File pdf = null;
            try {
                pdf = buatPdfHasil(sesi, pilihan);
                String respons = kirimPdfFonnte(noHp, sesi, pdf, pilihan.size());
                if (statusSukses(respons)) {
                    String catatan = "";
                    try {
                        catatBerkasTerkirim(pilihan, noHp);
                    } catch (Exception exCatat) {
                        catatan = "PDF berhasil terkirim, tetapi catatan status berkas belum tersimpan. Jalankan SQL patch lalu hubungi IT bila masalah berlanjut.";
                    }
                    SESI.remove(form.get("sesi"));
                    kirimHtml(exchange, 200, halamanSukses(sesi, noHp, pilihan.size(), pdf.getName(), catatan, kirimUlang));
                } else {
                    kirimHtml(exchange, 500, halamanPesan("Gagal mengirim PDF", "Gateway WhatsApp menolak pengiriman. Detail: " + ambilPesanAman(respons)));
                }
            } catch (Exception ex) {
                kirimHtml(exchange, 500, halamanPesan("Gagal membuat atau mengirim PDF", ex.getMessage() == null ? ex.toString() : ex.getMessage()));
            } finally {
                if (pdf != null && pdf.isFile()) {
                    try { pdf.delete(); } catch (Exception ex) {}
                }
            }
        }
    }

    private static List<Berkas> ambilPilihan(Sesi sesi, String idsTeks) {
        if (sesi == null || idsTeks == null) return Collections.emptyList();
        ArrayList<Berkas> hasil = new ArrayList<Berkas>();
        String[] ids = idsTeks.split(",");
        for (String id : ids) {
            Berkas b = sesi.berkas.get(id == null ? "" : id.trim());
            if (b != null && !sudahAda(hasil, b.id)) hasil.add(b);
        }
        return hasil;
    }

    private static boolean sudahAda(List<Berkas> daftar, String id) {
        for (Berkas b : daftar) if (b.id.equals(id)) return true;
        return false;
    }

    private static String halamanPopup(Sesi sesi, String token, String ids, List<Berkas> pilihan) {
        boolean kirimUlang = semuaBerkasSudahTerkirim(pilihan);
        String judulPopup = kirimUlang ? "Kirim Ulang Hasil Pemeriksaan ke WhatsApp" : "Kirim Hasil Pemeriksaan ke WhatsApp";
        String labelKirim = kirimUlang ? "Kirim Ulang Hasil" : "Kirim Hasil";
        StringBuilder daftar = new StringBuilder();
        for (int i = 0; i < pilihan.size(); i++) {
            Berkas berkas = pilihan.get(i);
            String idCheck = "berkas_" + i;
            daftar.append("<li class='file-item'><label class='file-check' for='")
                    .append(idCheck)
                    .append("'><input id='")
                    .append(idCheck)
                    .append("' class='berkas-check' type='checkbox' value='")
                    .append(escapeHtml(berkas.id))
                    .append("' checked onchange='updatePilihan()'><span class='file-name'>")
                    .append(escapeHtml(berkas.nama))
                    .append("</span></label><a class='preview' href='")
                    .append(escapeHtml(berkas.url))
                    .append("' target='_blank' rel='noopener noreferrer' onclick='window.open(this.href,\"preview_hasil_\"+Date.now(),\"width=1000,height=720,scrollbars=yes,resizable=yes\");return false;'>Preview</a></li>");
        }
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>")
            .append("<title>").append(escapeHtml(judulPopup)).append("</title><style>")
            .append("*{box-sizing:border-box}body{margin:0;background:#eef3f7;color:#22313f;font-family:Segoe UI,Arial,sans-serif}.wrap{max-width:720px;margin:22px auto;padding:0 16px}.card{background:#fff;border:1px solid #ccd8e2;border-radius:12px;box-shadow:0 8px 26px rgba(25,55,80,.12);overflow:hidden}.head{background:#0b6ba8;color:#fff;padding:17px 20px;font-size:18px;font-weight:700}.body{padding:20px}.row{display:grid;grid-template-columns:145px 1fr;gap:10px;margin:10px 0;align-items:center}.label{font-weight:700;color:#526779}input{width:100%;padding:10px 12px;border:1px solid #b9cad7;border-radius:7px;font:14px Segoe UI,Arial}input[readonly]{background:#f2f6f8}input[type=checkbox]{width:auto;padding:0;margin:0 9px 0 0;transform:scale(1.12)}.files{margin-top:18px;background:#f5f9fc;border:1px solid #d7e5ef;border-radius:8px;padding:12px 16px}.files strong{display:block;margin-bottom:6px}.files ul{margin:6px 0 0;padding-left:0;list-style:none;max-height:210px;overflow:auto}.file-item{display:flex;align-items:center;justify-content:space-between;gap:10px;margin:6px 0;padding:8px 0;border-bottom:1px dashed #d7e5ef}.file-item:last-child{border-bottom:0}.file-check{display:flex;align-items:center;min-width:0;flex:1;cursor:pointer}.file-name{min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.preview{flex:none;background:#0b6ba8;color:#fff;text-decoration:none;border-radius:6px;padding:6px 10px;font-weight:700;font-size:12px}.preview:hover{background:#085889}.note{font-size:12px;color:#617486;margin:12px 0 0}.actions{display:flex;justify-content:flex-end;gap:10px;margin-top:20px}.btn{border:0;border-radius:7px;padding:10px 16px;font-weight:700;cursor:pointer;font-size:14px}.send{background:#13854e;color:#fff}.close{background:#e7edf1;color:#304353;text-decoration:none}.send:disabled{opacity:.65;cursor:wait}@media(max-width:560px){.row{grid-template-columns:1fr}.wrap{margin:8px auto}.body{padding:16px}.file-item{align-items:flex-start}.preview{margin-top:2px}}</style>")
            .append("<script>function updatePilihan(){var b=document.querySelectorAll('.berkas-check'),ids=[];for(var i=0;i<b.length;i++){if(b[i].checked){ids.push(b[i].value);}}var h=document.getElementById('idsTerpilih');if(h){h.value=ids.join(',');}var j=document.getElementById('jumlahDipilih');if(j){j.textContent=ids.length;}var k=document.getElementById('kirim');if(k){k.disabled=ids.length===0;}return ids.length>0;}function submitKirim(){if(!updatePilihan()){alert('Pilih minimal satu berkas yang akan dikirim.');return false;}var k=document.getElementById('kirim');if(k){k.disabled=true;k.textContent='Sedang membuat PDF & mengirim...';}return true;}document.addEventListener('DOMContentLoaded',updatePilihan);</script></head><body>")
            .append("<div class='wrap'><div class='card'><div class='head'>").append(escapeHtml(judulPopup)).append("</div><div class='body'>")
            .append("<form method='post' action='/hasil-wa/kirim' onsubmit='return submitKirim();'>")
            .append("<input type='hidden' name='sesi' value='").append(escapeHtml(token)).append("'>")
            .append("<input id='idsTerpilih' type='hidden' name='ids' value='").append(escapeHtml(ids)).append("'>")
            .append("<div class='row'><div class='label'>No. Rawat</div><input readonly value='").append(escapeHtml(sesi.noRawat)).append("'></div>")
            .append("<div class='row'><div class='label'>Nama Pasien</div><input readonly value='").append(escapeHtml(sesi.namaPasien)).append("'></div>")
            .append("<div class='row'><div class='label'>No. Telepon Tujuan</div><input name='no_hp' required autofocus value='").append(escapeHtml(sesi.noHpAwal)).append("' placeholder='Contoh: 0812xxxx'></div>")
            .append("<div class='files'><strong><span id='jumlahDipilih'>").append(pilihan.size()).append("</span> dari ").append(pilihan.size()).append(" berkas akan digabung menjadi 1 file PDF:</strong><ul>")
            .append(daftar).append("</ul></div>")
            .append("<div class='note'>Centang berkas yang ingin dikirim. Tombol Preview membuka file di jendela/tab terpisah sehingga popup ini tetap terbuka. Pasien akan menerima lampiran PDF hasil pemeriksaan melalui WhatsApp, bukan tautan berkas.</div>")
            .append("<div class='actions'><a class='btn close' href='javascript:window.close()'>Batal</a><button id='kirim' class='btn send' type='submit'>").append(escapeHtml(labelKirim)).append("</button></div>")
            .append("</form></div></div></div></body></html>");
        return html.toString();
    }

    private static String halamanSukses(Sesi sesi, String noHp, int jumlah, String namaPdf, String catatan, boolean kirimUlang) {
        String tambahan = kosong(catatan) ? "" : "<br><br><b>Catatan:</b> " + escapeHtml(catatan);
        String judul = kirimUlang ? "Hasil berhasil dikirim ulang" : "Hasil berhasil dikirim";
        String aksi = kirimUlang ? "dikirim ulang ke" : "dikirim ke";
        return halamanPesan(judul, "Sebanyak " + jumlah + " hasil telah digabung dalam satu PDF dan " + aksi + " " + noHp + ".<br><br>Nama lampiran: <b>" + escapeHtml(namaPdf) + "</b><br><br>Status setiap berkas telah diperbarui dengan waktu pengiriman terakhir." + tambahan + "<br><br>Halaman ini dapat ditutup.");
    }

    private static String halamanPesan(String judul, String pesan) {
        return "<!doctype html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>Hasil Pengiriman</title>"
                + "<style>body{margin:0;background:#eef3f7;font-family:Segoe UI,Arial,sans-serif;color:#243746}.card{max-width:620px;margin:60px auto;background:#fff;border:1px solid #ccd8e2;border-radius:12px;padding:24px;box-shadow:0 8px 26px rgba(25,55,80,.12)}h2{margin:0 0 12px;color:#0b6ba8}.btn{display:inline-block;margin-top:20px;background:#e7edf1;color:#304353;text-decoration:none;border-radius:7px;padding:10px 16px;font-weight:700}</style></head><body><div class='card'><h2>"
                + escapeHtml(judul) + "</h2><div>" + pesan + "</div><a class='btn' href='javascript:window.close()'>Tutup</a></div></body></html>";
    }

    private static File buatPdfHasil(Sesi sesi, List<Berkas> pilihan) throws Exception {
        if (pilihan == null || pilihan.isEmpty()) throw new IOException("Tidak ada berkas yang dipilih.");
        int[] batasPiksel = new int[]{1700, 1300, 1000};
        float[] kualitas = new float[]{0.80f, 0.64f, 0.50f};
        File hasilTerbaik = null;
        for (int i = 0; i < batasPiksel.length; i++) {
            File pdf = File.createTempFile("Hasil_" + namaAman(sesi.judul) + "_", ".pdf");
            try {
                tulisPdf(pdf, pilihan, batasPiksel[i], kualitas[i]);
                if (hasilTerbaik != null) hasilTerbaik.delete();
                hasilTerbaik = pdf;
                if (pdf.length() <= BATAS_PDF_AMAN) return pdf;
            } catch (Exception ex) {
                pdf.delete();
                throw ex;
            }
        }
        if (hasilTerbaik == null) throw new IOException("PDF tidak dapat dibuat.");
        if (hasilTerbaik.length() > BATAS_PDF_AMAN) {
            long kb = Math.round(hasilTerbaik.length() / 1024.0d);
            hasilTerbaik.delete();
            throw new IOException("Ukuran PDF sekitar " + kb + " KB, melewati batas aman WhatsApp Gateway. Kurangi jumlah hasil yang dipilih.");
        }
        return hasilTerbaik;
    }

    private static void tulisPdf(File tujuan, List<Berkas> pilihan, int batasPiksel, float kualitas) throws Exception {
        ArrayList<FotoPdf> foto = new ArrayList<FotoPdf>();
        for (Berkas berkas : pilihan) {
            BufferedImage sumber = bacaGambar(berkas);
            BufferedImage siap = kecilkanDanRatakkan(sumber, batasPiksel);
            foto.add(new FotoPdf(siap.getWidth(), siap.getHeight(), jpg(siap, kualitas)));
        }
        if (foto.isEmpty()) throw new IOException("Tidak ada gambar hasil yang dapat dimasukkan ke PDF.");

        ArrayList<byte[]> objek = new ArrayList<byte[]>();
        objek.add(null); // indeks 0 tidak digunakan
        objek.add(teks("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder pages = new StringBuilder("<< /Type /Pages /Kids [");
        for (int i = 0; i < foto.size(); i++) pages.append(5 + (i * 3)).append(" 0 R ");
        pages.append("] /Count ").append(foto.size()).append(" >>");
        objek.add(teks(pages.toString()));

        for (int i = 0; i < foto.size(); i++) {
            FotoPdf gambar = foto.get(i);
            int nomorImage = 3 + (i * 3);
            int nomorContent = nomorImage + 1;
            String nama = "Im" + (i + 1);
            String imageHeader = "<< /Type /XObject /Subtype /Image /Width " + gambar.lebar
                    + " /Height " + gambar.tinggi + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + gambar.jpg.length + " >>\nstream\n";
            objek.add(streamObjek(imageHeader, gambar.jpg));

            double maxW = 555d;
            double maxH = 792d;
            double rasio = Math.min(maxW / gambar.lebar, maxH / gambar.tinggi);
            double w = gambar.lebar * rasio;
            double h = gambar.tinggi * rasio;
            double x = (595d - w) / 2d;
            double y = (842d - h) / 2d;
            String isi = "q\n" + angkaPdf(w) + " 0 0 " + angkaPdf(h) + " " + angkaPdf(x) + " " + angkaPdf(y) + " cm\n/" + nama + " Do\nQ\n";
            objek.add(streamObjek("<< /Length " + isi.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n", isi.getBytes(StandardCharsets.US_ASCII)));
            String halaman = "<< /Type /Page /Parent 2 0 R /Resources << /ProcSet [/PDF /ImageC] /XObject << /"
                    + nama + " " + nomorImage + " 0 R >> >> /MediaBox [0 0 595 842] /Contents " + nomorContent + " 0 R >>";
            objek.add(teks(halaman));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        out.write(new byte[]{'%', (byte)0xE2, (byte)0xE3, (byte)0xCF, (byte)0xD3, '\n'});
        ArrayList<Long> posisi = new ArrayList<Long>();
        posisi.add(0L);
        for (int i = 1; i < objek.size(); i++) {
            posisi.add((long) out.size());
            out.write((i + " 0 obj\n").getBytes(StandardCharsets.US_ASCII));
            out.write(objek.get(i));
            out.write("\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        }
        long xref = out.size();
        out.write(("xref\n0 " + objek.size() + "\n").getBytes(StandardCharsets.US_ASCII));
        out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (int i = 1; i < posisi.size(); i++) {
            out.write(String.format(Locale.US, "%010d 00000 n \n", posisi.get(i)).getBytes(StandardCharsets.US_ASCII));
        }
        out.write(("trailer\n<< /Size " + objek.size() + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));
        FileOutputStream fos = new FileOutputStream(tujuan);
        try { fos.write(out.toByteArray()); } finally { fos.close(); }
    }

    private static byte[] streamObjek(String header, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(header.getBytes(StandardCharsets.US_ASCII));
        out.write(data);
        out.write("\nendstream".getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    private static byte[] teks(String nilai) {
        return nilai.getBytes(StandardCharsets.US_ASCII);
    }

    private static String angkaPdf(double angka) {
        return String.format(Locale.US, "%.2f", angka);
    }

    private static BufferedImage bacaGambar(Berkas berkas) throws Exception {
        URLConnection conn = new URL(berkas.url).openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(45000);
        InputStream in = conn.getInputStream();
        try {
            BufferedImage img = ImageIO.read(in);
            if (img == null) throw new IOException("Berkas '" + berkas.nama + "' bukan gambar JPG/PNG yang dapat dibuat PDF.");
            return img;
        } finally {
            try { in.close(); } catch (Exception ex) {}
        }
    }

    private static BufferedImage kecilkanDanRatakkan(BufferedImage sumber, int batasPiksel) {
        int w = sumber.getWidth();
        int h = sumber.getHeight();
        double skala = 1d;
        int sisiTerbesar = Math.max(w, h);
        if (sisiTerbesar > batasPiksel) skala = batasPiksel / (double) sisiTerbesar;
        int nw = Math.max(1, (int)Math.round(w * skala));
        int nh = Math.max(1, (int)Math.round(h * skala));
        BufferedImage target = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, nw, nh);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(sumber, 0, 0, nw, nh, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    private static byte[] jpg(BufferedImage gambar, float kualitas) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageWriter writer = null;
        java.util.Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("jpeg");
        if (it.hasNext()) writer = it.next();
        if (writer == null) throw new IOException("Encoder JPEG Java tidak tersedia.");
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        try {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(kualitas);
            }
            writer.write(null, new IIOImage(gambar, null, null), param);
        } finally {
            try { ios.close(); } catch (Exception ex) {}
            writer.dispose();
        }
        return out.toByteArray();
    }

    private static String kirimPdfFonnte(String noHp, Sesi sesi, File pdf, int jumlahBerkas) throws Exception {
        String token = koneksiDBWA.TOKENWA();
        if (token == null || token.trim().equals("")) throw new IOException("Token Fonnte belum tersedia di konfigurasi aplikasi.");
        String namaLampiran = namaAman(sesi.judul) + "_" + namaAman(sesi.noRawat) + ".pdf";
        String pesan = "Salam Sehat,\n\nBerikut kami kirimkan hasil pemeriksaan " + sesi.judul
                + " an. " + sesi.namaPasien + " (No. Rawat " + sesi.noRawat + ").\n"
                + "Lampiran berisi " + jumlahBerkas + " hasil pemeriksaan dalam satu file PDF.\n\nTerima kasih.";
        String country = noHp.startsWith("0") ? "62" : "0";
        String boundary = "----KhanzaHasil" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.fonnte.com/send").openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(90000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", token.trim());
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setDoOutput(true);
        OutputStream out = conn.getOutputStream();
        try {
            tulisField(out, boundary, "target", noHp);
            tulisField(out, boundary, "message", pesan);
            tulisField(out, boundary, "countryCode", country);
            tulisField(out, boundary, "filename", namaLampiran);
            tulisFile(out, boundary, "file", namaLampiran, pdf);
            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } finally {
            try { out.close(); } catch (Exception ex) {}
        }
        int kode = conn.getResponseCode();
        InputStream in = kode >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String respons = in == null ? "HTTP " + kode : bacaTeks(in);
        conn.disconnect();
        return respons;
    }

    private static void tulisField(OutputStream out, String boundary, String nama, String nilai) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + nama + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write((nilai == null ? "" : nilai).getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static void tulisFile(OutputStream out, String boundary, String namaField, String namaFile, File file) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + namaField + "\"; filename=\"" + namaFile + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write("Content-Type: application/pdf\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        InputStream in = new java.io.FileInputStream(file);
        try {
            byte[] buffer = new byte[8192];
            int baca;
            while ((baca = in.read(buffer)) != -1) out.write(buffer, 0, baca);
        } finally {
            in.close();
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static String normalisasiNomor(String nomor) {
        if (nomor == null) return "";
        return nomor.replaceAll("[^0-9]", "").trim();
    }

    private static boolean statusSukses(String response) {
        return response != null && response.matches("(?is).*\\\"status\\\"\\s*:\\s*true.*");
    }

    private static String ambilPesanAman(String response) {
        if (response == null || response.trim().equals("")) return "Tidak ada respons dari gateway.";
        String bersih = response.replaceAll("[\\r\\n]+", " ").replaceAll("<[^>]*>", " ").trim();
        if (bersih.length() > 350) bersih = bersih.substring(0, 350) + "...";
        return escapeHtml(bersih);
    }

    private static Map<String, String> parseParameter(String teks) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        if (teks == null || teks.trim().equals("")) return map;
        String[] bagian = teks.split("&");
        for (String item : bagian) {
            int pos = item.indexOf('=');
            String key = pos >= 0 ? item.substring(0, pos) : item;
            String val = pos >= 0 ? item.substring(pos + 1) : "";
            try {
                key = URLDecoder.decode(key, "UTF-8");
                val = URLDecoder.decode(val, "UTF-8");
            } catch (Exception ex) {}
            map.put(key, val);
        }
        return map;
    }

    private static String bacaTeks(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        try { in.close(); } catch (Exception ex) {}
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void kirimHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, data.length);
        OutputStream out = exchange.getResponseBody();
        try { out.write(data); } finally { out.close(); }
    }

    private static String namaAman(String nilai) {
        String hasil = nilai == null ? "Hasil" : nilai.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return hasil.equals("") ? "Hasil" : hasil;
    }

    private static String escapeHtml(String nilai) {
        if (nilai == null) return "";
        return nilai.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static final class FotoPdf {
        private final int lebar;
        private final int tinggi;
        private final byte[] jpg;
        private FotoPdf(int lebar, int tinggi, byte[] jpg) {
            this.lebar = lebar;
            this.tinggi = tinggi;
            this.jpg = jpg;
        }
    }
}
