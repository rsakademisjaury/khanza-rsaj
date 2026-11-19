package simrskhanza;

import fungsi.koneksiDB;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

final class BerkasRujukBalikFolio {
    private static final String KODE = "138";
    private static final String FOLDER = "pages/upload/";
    private static final byte[] TANDA_PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};

    private BerkasRujukBalikFolio() {
    }

    static void periksaRevisi(Connection koneksi, String noRawat, int revisi) throws SQLException, SuratBerubah {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select revisi from surat_rujuk_balik where no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("revisi") != revisi) {
                    throw new SuratBerubah();
                }
            }
        }
    }

    static List<byte[]> renderPng(JasperPrint report) throws JRException, IOException {
        int jumlahHalaman = report.getPages().size();
        if (jumlahHalaman == 0) {
            throw new GagalUnggah("Report folio belum memiliki halaman.");
        }
        List<byte[]> hasil = new ArrayList<>(jumlahHalaman);
        for (int halaman = 0; halaman < jumlahHalaman; halaman++) {
            Image gambar = JasperPrintManager.printPageToImage(report, halaman, 2.0f);
            BufferedImage png = new BufferedImage(gambar.getWidth(null), gambar.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D grafis = png.createGraphics();
            try {
                grafis.drawImage(gambar, 0, 0, Color.WHITE, null);
            } finally {
                grafis.dispose();
                gambar.flush();
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            if (!ImageIO.write(png, "png", buffer)) {
                throw new GagalUnggah("Halaman " + (halaman + 1) + " gagal dibuat sebagai PNG.");
            }
            hasil.add(buffer.toByteArray());
        }
        return hasil;
    }

    static void unggah(String noRawat, int revisi, List<byte[]> halaman) throws Exception {
        String host = koneksiDB.HOSTHYBRIDWEB();
        String port = koneksiDB.PORTWEB();
        String web = koneksiDB.HYBRIDWEB();
        if (host == null || host.isBlank()) {
            throw new GagalUnggah("Alamat server Berkas Digital belum tersedia.");
        }
        String alamat = "http://" + host + (port == null || port.isBlank() ? "" : ":" + port)
                + "/" + (web == null || web.isBlank() ? "" : web.replaceAll("^/+|/+$", "") + "/");
        String id = UUID.nameUUIDFromBytes(noRawat.getBytes(StandardCharsets.UTF_8)).toString();
        String namaRevisi = "Surat_Rujuk_Balik_" + id + "_r" + revisi;

        try (Connection koneksi = koneksiDB.condbTransaksi()) {
            koneksi.setAutoCommit(false);
            try {
                kunciSuratBpjs(koneksi, noRawat, revisi);
                periksaKategori(koneksi);
                for (int i = 0; i < halaman.size(); i++) {
                    unggahHalaman(koneksi, noRawat, alamat, namaRevisi + "_p" + (i + 1) + ".png", halaman.get(i), i + 1);
                }
                koneksi.commit();
            } catch (Exception e) {
                try {
                    koneksi.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                throw e;
            }
        }
    }

    private static void unggahHalaman(Connection koneksi, String noRawat, String alamat,
            String nama, byte[] png, int halaman) throws IOException, SQLException {
        String lokasi = FOLDER + nama;
        URL urlFile = new URL(alamat + "berkasrawat/" + lokasi);
        boolean sudahTercatat = tercatat(koneksi, noRawat, lokasi);
        if (!sudahTercatat || !terverifikasi(urlFile, null)) {
            kirim(alamat + "upload.php?doc=berkasrawat/" + FOLDER, nama, png);
            if (!terverifikasi(urlFile, png)) {
                throw new GagalUnggah("PNG halaman " + halaman + " belum dapat dibaca kembali dari server Berkas Digital.");
            }
        }
        if (!sudahTercatat) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "insert into berkas_digital_perawatan (no_rawat,kode,lokasi_file) values (?,?,?)")) {
                ps.setString(1, noRawat);
                ps.setString(2, KODE);
                ps.setString(3, lokasi);
                ps.executeUpdate();
            }
        }
    }

    private static void kunciSuratBpjs(Connection koneksi, String noRawat, int revisi)
            throws SQLException, SuratBerubah, GagalUnggah {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select s.revisi,r.kd_pj from surat_rujuk_balik s "
                + "join reg_periksa r on r.no_rawat=s.no_rawat where s.no_rawat=? for update")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("revisi") != revisi) {
                    throw new SuratBerubah();
                }
                String penjamin = rs.getString("kd_pj");
                if (!DlgRujukBalik.penjaminBpjs(penjamin)) {
                    throw new GagalUnggah("Unggah folio tersedia untuk kunjungan BPJS.");
                }
            }
        }
    }

    private static void periksaKategori(Connection koneksi) throws SQLException, GagalUnggah {
        try (PreparedStatement ps = koneksi.prepareStatement("select kode from master_berkas_digital where kode=?")) {
            ps.setString(1, KODE);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new GagalUnggah("Kode 138 Berkas Lain-lain belum tersedia di master berkas.");
                }
            }
        }
    }

    private static boolean tercatat(Connection koneksi, String noRawat, String lokasi) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select 1 from berkas_digital_perawatan where no_rawat=? and kode=? and lokasi_file=?")) {
            ps.setString(1, noRawat);
            ps.setString(2, KODE);
            ps.setString(3, lokasi);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void kirim(String url, String nama, byte[] png) throws IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
        HttpConnectionParams.setSoTimeout(client.getParams(), 20000);
        HttpPost request = new HttpPost(url);
        try {
            request.setEntity(MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("file", png, ContentType.create("image/png"), nama).build());
            HttpResponse response = client.execute(request);
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            if (status < 200 || status >= 300) {
                throw new GagalUnggah("Server unggah mengembalikan HTTP " + status + ".");
            }
        } finally {
            request.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

    private static boolean terverifikasi(URL url, byte[] png) throws IOException {
        HttpURLConnection koneksi = (HttpURLConnection) url.openConnection();
        koneksi.setInstanceFollowRedirects(false);
        koneksi.setConnectTimeout(10000);
        koneksi.setReadTimeout(10000);
        koneksi.setRequestProperty("Cache-Control", "no-cache");
        try {
            if (koneksi.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }
            try (InputStream input = koneksi.getInputStream()) {
                return png == null ? Arrays.equals(TANDA_PNG, input.readNBytes(TANDA_PNG.length))
                        : Arrays.equals(png, input.readNBytes(png.length + 1));
            }
        } finally {
            koneksi.disconnect();
        }
    }

    static final class SuratBerubah extends Exception {
    }

    static final class GagalUnggah extends IOException {
        private GagalUnggah(String pesan) {
            super(pesan);
        }
    }
}
