package rekammedis;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import fungsi.koneksiDB;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

final class SoapCpptBerkasDigital {
    private static final String KODE_BERKAS = "138";
    private static final String FOLDER = "pages/upload/";
    private static final String JUDUL = "SOAP / CPPT";
    static final List<String> KOLOM = List.of("Tanggal / Layanan", "Dokter", "S · Subjektif", "O · Objektif",
            "A · Asesmen", "P · Rencana", "I · Instruksi", "E · Evaluasi");
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    private static final String SQL_CATATAN = sqlPemeriksaan("pemeriksaan_ralan", "Rawat Jalan", "lingkar_perut")
            + " UNION ALL " + sqlPemeriksaan("pemeriksaan_ranap", "Rawat Inap", "''")
            + " ORDER BY tgl_perawatan, jam_rawat, layanan";

    private SoapCpptBerkasDigital() {
    }

    static final class Dokter {
        final String kode;
        final String nama;

        Dokter(String kode, String nama) {
            this.kode = kode;
            this.nama = nama;
        }

        @Override
        public String toString() {
            return nama + " (" + kode + ")";
        }
    }

    static final class Kunjungan {
        final String noRawat;
        final String noRm;
        final String pasien;
        final LocalDate tanggalMasuk;
        final List<Dokter> dokter;

        Kunjungan(String noRawat, String noRm, String pasien, LocalDate tanggalMasuk, List<Dokter> dokter) {
            this.noRawat = noRawat;
            this.noRm = noRm;
            this.pasien = pasien;
            this.tanggalMasuk = tanggalMasuk;
            this.dokter = dokter;
        }
    }

    static final class Hasil {
        final String noRawat;
        final String identitas;
        final Dokter penulis;
        final LocalDate awal;
        final LocalDate akhir;
        final List<List<String>> catatan;

        Hasil(Kunjungan kunjungan, Dokter dokter, LocalDate awal, LocalDate akhir, List<List<String>> catatan) {
            noRawat = kunjungan.noRawat;
            penulis = dokter;
            this.awal = awal;
            this.akhir = akhir;
            identitas = "Pasien: " + kunjungan.pasien + "\nNo. RM: " + kunjungan.noRm
                    + "\nNo. Rawat: " + kunjungan.noRawat + "\nDokter penulis: " + dokter
                    + "\nTanggal catatan: " + TANGGAL.format(awal) + " s.d. " + TANGGAL.format(akhir)
                    + "\nJumlah catatan: " + catatan.size();
            this.catatan = List.copyOf(catatan);
        }

        String kunciDokumen(String rumahSakit) {
            StringJoiner isi = new StringJoiner("\n").add("SOAP_CPPT_JPG_V1").add(rumahSakit).add(identitas);
            for (List<String> baris : catatan) {
                for (String sel : baris) {
                    isi.add(sel.length() + ":" + sel);
                }
            }
            UUID kunci = UUID.nameUUIDFromBytes(isi.toString().getBytes(StandardCharsets.UTF_8));
            byte[] data = ByteBuffer.allocate(16).putLong(kunci.getMostSignificantBits())
                    .putLong(kunci.getLeastSignificantBits()).array();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(data, 12));
        }
    }

    static Kunjungan bacaKunjungan(Connection koneksi, String noRawat) throws SQLException, IOException {
        String sql = "SELECT r.no_rkm_medis, p.nm_pasien, r.tgl_registrasi FROM reg_periksa r "
                + "JOIN pasien p ON p.no_rkm_medis=r.no_rkm_medis WHERE r.no_rawat=?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IOException("Kunjungan pasien tidak ditemukan.");
                }
                return new Kunjungan(noRawat, rs.getString(1), rs.getString(2), rs.getDate(3).toLocalDate(),
                        bacaDokter(koneksi, noRawat));
            }
        }
    }

    private static List<Dokter> bacaDokter(Connection koneksi, String noRawat) throws SQLException {
        String sql = "SELECT d.kd_dokter, d.nm_dokter FROM dpjp_ranap dpjp "
                + "JOIN dokter d ON d.kd_dokter=dpjp.kd_dokter WHERE dpjp.no_rawat=? ORDER BY d.nm_dokter, d.kd_dokter";
        List<Dokter> dokter = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dokter.add(new Dokter(rs.getString(1), rs.getString(2)));
                }
            }
        }
        return dokter;
    }

    private static String sqlPemeriksaan(String tabel, String layanan, String lingkarPerut) {
        return "SELECT tgl_perawatan, jam_rawat, '" + layanan + "' AS layanan, "
                + "keluhan, pemeriksaan, penilaian, rtl, instruksi, evaluasi, alergi, suhu_tubuh, tensi, "
                + "nadi, respirasi, tinggi, berat, spo2, gcs, kesadaran, " + lingkarPerut + " AS lingkar_perut "
                + "FROM " + tabel + " WHERE no_rawat=? AND nip=? AND tgl_perawatan BETWEEN ? AND ?";
    }

    static Hasil cari(Connection koneksi, Kunjungan kunjungan, Dokter dokter, LocalDate awal, LocalDate akhir)
            throws SQLException {
        List<List<String>> catatan = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(SQL_CATATAN)) {
            for (int i : new int[]{1, 5}) {
                ps.setString(i, kunjungan.noRawat);
                ps.setString(i + 1, dokter.kode);
                ps.setDate(i + 2, Date.valueOf(awal));
                ps.setDate(i + 3, Date.valueOf(akhir));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    catatan.add(bacaCatatan(rs, dokter));
                }
            }
        }
        return new Hasil(kunjungan, dokter, awal, akhir, catatan);
    }

    private static List<String> bacaCatatan(ResultSet rs, Dokter dokter) throws SQLException {
        StringJoiner vitals = new StringJoiner(" | ");
        String[][] kolom = {{"Tensi", "tensi"}, {"Suhu (°C)", "suhu_tubuh"}, {"Nadi (/menit)", "nadi"},
            {"Respirasi (/menit)", "respirasi"}, {"SpO₂ (%)", "spo2"}, {"GCS", "gcs"}, {"Kesadaran", "kesadaran"},
            {"BB (kg)", "berat"}, {"TB (cm)", "tinggi"}, {"Lingkar perut (cm)", "lingkar_perut"}, {"Alergi", "alergi"}};
        for (String[] item : kolom) {
            String nilai = rs.getString(item[1]);
            if (nilai != null && !nilai.isBlank()) {
                vitals.add(item[0] + ": " + teks(nilai));
            }
        }
        String waktu = TANGGAL.format(rs.getDate("tgl_perawatan").toLocalDate())
                + "\n" + rs.getString("jam_rawat") + "\n" + rs.getString("layanan");
        String objektif = teks(rs.getString("pemeriksaan"));
        if (vitals.length() > 0) {
            objektif = "—".equals(objektif) ? vitals.toString() : objektif + "\n\n" + vitals;
        }
        return List.of(waktu, teks(dokter.nama) + "\n" + dokter.kode, teks(rs.getString("keluhan")), objektif,
                teks(rs.getString("penilaian")), teks(rs.getString("rtl")), teks(rs.getString("instruksi")),
                teks(rs.getString("evaluasi")));
    }

    private static String teks(String isi) {
        return isi == null || isi.isBlank() ? "—" : isi.replace("\r\n", "\n").replace('\r', '\n')
                .replace('\t', ' ').replaceAll("[\\p{Cc}&&[^\\n]]", "").strip();
    }

    static byte[] buatPdf(String rumahSakit, Hasil hasil) throws DocumentException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document dokumen = new Document(PageSize.A4.rotate(), 26, 26, 24, 24);
        String namaFont = "DejaVuSans.ttf";
        BaseFont font;
        try (InputStream stream = SoapCpptBerkasDigital.class.getResourceAsStream(
                "/net/sf/jasperreports/fonts/dejavu/" + namaFont)) {
            if (stream == null) {
                throw new IOException("Font laporan SOAP / CPPT tidak tersedia.");
            }
            font = BaseFont.createFont(namaFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, stream.readAllBytes(), null);
        }
        PdfWriter.getInstance(dokumen, output);
        dokumen.addTitle(JUDUL);
        dokumen.open();
        try {
            dokumen.add(new Paragraph(JUDUL + "\n" + rumahSakit, new Font(font, 13, Font.BOLD)));
            Font isi = new Font(font, 8.5f, Font.NORMAL, new Color(27, 44, 66));
            Paragraph identitas = new Paragraph(12, hasil.identitas, isi);
            identitas.setSpacingBefore(8);
            identitas.setSpacingAfter(10);
            dokumen.add(identitas);
            PdfPTable tabel = new PdfPTable(new float[]{11, 13, 16, 16, 12, 12, 11, 9});
            tabel.setWidthPercentage(100);
            tabel.setHeaderRows(1);
            tabel.setSplitRows(true);
            tabel.setSplitLate(false);
            Font judulKolom = new Font(font, 8.5f, Font.BOLD, new Color(24, 60, 103));
            for (String judul : KOLOM) {
                tabel.addCell(selPdf(judul, judulKolom, new Color(234, 243, 254)));
            }
            for (int i = 0; i < hasil.catatan.size(); i++) {
                Color latar = i % 2 == 0 ? Color.WHITE : new Color(247, 250, 254);
                for (String nilai : hasil.catatan.get(i)) {
                    tabel.addCell(selPdf(nilai, isi, latar));
                }
            }
            dokumen.add(tabel);
        } finally {
            dokumen.close();
        }
        return output.toByteArray();
    }

    private static PdfPCell selPdf(String teks, Font font, Color latar) {
        PdfPCell sel = new PdfPCell(new Phrase(11, teks, font));
        sel.setBorderWidth(0.45f);
        sel.setBorderColor(new Color(201, 216, 235));
        sel.setBackgroundColor(latar);
        sel.setPadding(5);
        sel.setVerticalAlignment(Element.ALIGN_TOP);
        return sel;
    }

    static int unggah(Hasil hasil, String rumahSakit)
            throws SQLException, IOException, DocumentException {
        String host = koneksiDB.HOSTHYBRIDWEB();
        String port = koneksiDB.PORTWEB();
        String web = koneksiDB.HYBRIDWEB();
        if (host == null || host.isBlank()) {
            throw new IOException("Alamat server Berkas Digital belum tersedia.");
        }
        String alamat = URI.create("http://" + host + (port == null || port.isBlank() ? "" : ":" + port)
                + "/" + (web == null || web.isBlank() ? "" : web.replaceAll("^/+|/+$", "") + "/")).toASCIIString();
        return unggah(hasil, rumahSakit, alamat);
    }

    private static int unggah(Hasil hasil, String rumahSakit, String alamat)
            throws SQLException, IOException, DocumentException {
        if (hasil.catatan.isEmpty()) {
            throw new IOException("Tidak ada catatan SOAP / CPPT yang bisa diunggah.");
        }
        String namaDasar = "CPPT_" + hasil.kunciDokumen(rumahSakit);
        RequestConfig konfigurasi = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(20000)
                .setRedirectsEnabled(false).build();
        try (Connection koneksi = koneksiDB.condbTransaksi();
                CloseableHttpClient http = HttpClients.custom().setDefaultRequestConfig(konfigurasi).build();
                PDDocument dokumen = PDDocument.load(buatPdf(rumahSakit, hasil))) {
            if (bacaDokter(koneksi, hasil.noRawat).stream().noneMatch(d -> d.kode.equals(hasil.penulis.kode))) {
                throw new IOException("Dokter penulis sudah tidak tercatat sebagai DPJP pada kunjungan ini.");
            }
            try (PreparedStatement ps = koneksi.prepareStatement("SELECT 1 FROM master_berkas_digital WHERE kode=?")) {
                ps.setString(1, KODE_BERKAS);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IOException("Kategori Berkas Digital 138 belum tersedia.");
                    }
                }
            }
            PDFRenderer renderer = new PDFRenderer(dokumen);
            List<String> lokasiBerkas = new ArrayList<>();
            for (int i = 0; i < dokumen.getNumberOfPages(); i++) {
                ByteArrayOutputStream gambar = new ByteArrayOutputStream();
                if (!ImageIO.write(renderer.renderImageWithDPI(i, 200, ImageType.RGB), "jpg", gambar)) {
                    throw new IOException("Format JPG tidak tersedia untuk hasil SOAP/CPPT.");
                }
                String nama = String.format(Locale.ROOT, "%s_%03d.jpg", namaDasar, i + 1);
                unggahHalaman(http, alamat, nama, gambar.toByteArray(), i + 1);
                lokasiBerkas.add(FOLDER + nama);
            }
            catatHalaman(koneksi, hasil.noRawat, lokasiBerkas);
            return lokasiBerkas.size();
        }
    }

    private static void unggahHalaman(CloseableHttpClient http, String alamat, String nama, byte[] gambar, int halaman)
            throws IOException {
        String urlBerkas = alamat + "berkasrawat/" + FOLDER + nama;
        if (verifikasiGambar(http, urlBerkas, gambar)) {
            return;
        }
        HttpPost post = new HttpPost(alamat + "upload.php?doc=berkasrawat/" + FOLDER);
        post.setEntity(MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("file", gambar, ContentType.create("image/jpeg"), nama).build());
        try (CloseableHttpResponse respons = http.execute(post)) {
            int status = respons.getStatusLine().getStatusCode();
            if (status < 200 || status >= 300) {
                throw new IOException("Unggah halaman " + halaman + " gagal (HTTP " + status + ").");
            }
        }
        if (!verifikasiGambar(http, urlBerkas, gambar)) {
            throw new IOException("Gambar halaman " + halaman + " belum dapat dibaca kembali dari Berkas Digital.");
        }
    }

    private static void catatHalaman(Connection koneksi, String noRawat, List<String> lokasiBerkas) throws SQLException {
        koneksi.setAutoCommit(false);
        try {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO berkas_digital_perawatan (no_rawat, kode, lokasi_file) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE lokasi_file=VALUES(lokasi_file)")) {
                for (String lokasi : lokasiBerkas) {
                    ps.setString(1, noRawat);
                    ps.setString(2, KODE_BERKAS);
                    ps.setString(3, lokasi);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            koneksi.commit();
        } catch (SQLException e) {
            try {
                koneksi.rollback();
            } catch (SQLException rollback) {
                e.addSuppressed(rollback);
            }
            throw e;
        }
    }

    private static boolean verifikasiGambar(CloseableHttpClient http, String url, byte[] gambar) throws IOException {
        HttpGet get = new HttpGet(url);
        get.setHeader("Cache-Control", "no-cache");
        try (CloseableHttpResponse respons = http.execute(get)) {
            if (respons.getStatusLine().getStatusCode() != 200 || respons.getEntity() == null) {
                return false;
            }
            try (InputStream input = respons.getEntity().getContent()) {
                return Arrays.equals(gambar, input.readNBytes(gambar.length + 1));
            }
        }
    }
}
