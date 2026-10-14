package fungsi;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.renderers.SimpleDataRenderer;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.base.JRBasePrintImage;
import net.sf.jasperreports.engine.base.JRBasePrintPage;
import net.sf.jasperreports.engine.base.JRBasePrintText;
import net.sf.jasperreports.engine.type.ScaleImageEnum;

/** Lampiran ditambahkan ke JasperPrint yang sama: cetak, PDF dan Upload identik. */
public final class LampiranFotoLabPA {
    private LampiranFotoLabPA() {}

    /** Semua halaman, termasuk lampiran foto, menjadi satu JPG vertikal. */
    public static void simpanJPG(JasperPrint hasil, File tujuan) throws JRException, IOException {
        int jumlah = hasil.getPages().size();
        if (jumlah == 0) throw new IOException("Report PA tidak memiliki halaman.");
        // Hingga 300 DPI. Report panjang menyesuaikan skala agar semua halaman tetap
        // masuk satu JPEG dan buffer gabungan tidak membebani RAM workstation.
        double luas = (double) hasil.getPageWidth() * hasil.getPageHeight() * jumlah;
        float zoom = (float) Math.min(300d / 72d, Math.sqrt(24000000d / luas));
        zoom = Math.min(zoom, 65000f / ((float) hasil.getPageHeight() * jumlah));
        zoom = Math.min(zoom, 65000f / hasil.getPageWidth());
        int lebar = Math.max(1, (int) Math.ceil(hasil.getPageWidth() * zoom));
        int tinggiHalaman = Math.max(1, (int) Math.ceil(hasil.getPageHeight() * zoom));
        long tinggiTotal = (long) tinggiHalaman * jumlah;
        if (tinggiTotal > 65500 || lebar > 65500)
            throw new IOException("Ukuran report PA melebihi ukuran gambar JPG yang didukung.");
        BufferedImage gabungan = new BufferedImage(lebar, (int) tinggiTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D grafis = gabungan.createGraphics();
        try {
            grafis.setColor(Color.WHITE);
            grafis.fillRect(0, 0, lebar, (int) tinggiTotal);
            for (int i = 0; i < jumlah; i++) {
                Image halaman = JasperPrintManager.printPageToImage(hasil, i, zoom);
                try { grafis.drawImage(halaman, 0, i * tinggiHalaman, null); }
                finally { halaman.flush(); }
            }
            java.util.Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IOException("Encoder JPG tidak tersedia.");
            ImageWriter writer = writers.next();
            try (ImageOutputStream output = ImageIO.createImageOutputStream(tujuan)) {
                if (output == null) throw new IOException("File JPG hasil PA tidak dapat dibuat.");
                writer.setOutput(output);
                ImageWriteParam kualitas = writer.getDefaultWriteParam();
                if (kualitas.canWriteCompressed()) {
                    kualitas.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    kualitas.setCompressionQuality(0.95f);
                }
                writer.write(null, new IIOImage(gabungan, null, null), kualitas);
            } finally { writer.dispose(); }
        } finally {
            grafis.dispose();
            gabungan.flush();
        }
    }

    public static final class Foto {
        public final BufferedImage gambar;
        public final String pemeriksaan;
        public Foto(BufferedImage gambar, String pemeriksaan) {
            this.gambar = gambar;
            this.pemeriksaan = pemeriksaan == null ? "PA" : pemeriksaan;
        }
    }

    public static void tambahkan(JasperPrint hasil, List<Foto> foto,
            Map<String, Object> identitas, int kiri, int lebar, int atas, int bawah) throws JRException {
        if (foto == null || foto.isEmpty()) return;
        if (kiri < 0 || lebar <= 0 || kiri + lebar > hasil.getPageWidth()) {
            throw new JRException("Posisi lampiran foto PA berada di luar halaman.");
        }
        final int awal = atas + 36;
        final int akhir = hasil.getPageHeight() - bawah;
        final int label = 18;
        final int jarak = 10;
        final int kapasitas = akhir - awal - label - jarak;
        if (kapasitas <= 0) throw new JRException("Ruang lampiran foto PA tidak cukup.");
        JRBasePrintPage halaman = null;
        int y = awal;
        for (int i = 0; i < foto.size(); i++) {
            Foto item = foto.get(i);
            BufferedImage gambar = item.gambar;
            if (gambar == null || gambar.getWidth() <= 0 || gambar.getHeight() <= 0) {
                throw new JRException("Foto PA ke-" + (i + 1) + " tidak dapat dibaca.");
            }
            int tinggi = (int) Math.ceil((double) lebar * gambar.getHeight() / gambar.getWidth());
            if (tinggi <= kapasitas) {
                if (halaman == null || y + label + tinggi + jarak > akhir) {
                    halaman = halamanBaru(hasil, identitas, kiri, lebar, atas);
                    y = awal;
                }
                teks(hasil, halaman, kiri, y, lebar, label,
                        "Foto " + (i + 1) + " — " + item.pemeriksaan, 9f);
                gambar(hasil, halaman, gambar, kiri, y + label, lebar, tinggi);
                y += label + tinggi + jarak;
            } else {
                // Foto sangat tinggi dibagi menjadi bagian berurutan, bukan dipotong/hilang.
                // Lebar dan skala setiap bagian tetap sama dengan kotak identitas pasien.
                int barisMaksimum = Math.max(1, (int) Math.floor(
                        (double) kapasitas * gambar.getWidth() / lebar));
                int bagian = 0;
                for (int baris = 0; baris < gambar.getHeight(); baris += barisMaksimum) {
                    int barisBagian = Math.min(barisMaksimum, gambar.getHeight() - baris);
                    int tinggiBagian = (int) Math.ceil((double) lebar * barisBagian / gambar.getWidth());
                    halaman = halamanBaru(hasil, identitas, kiri, lebar, atas);
                    y = awal;
                    teks(hasil, halaman, kiri, y, lebar, label,
                            "Foto " + (i + 1) + " — " + item.pemeriksaan + " (bagian " + (++bagian) + ")", 9f);
                    gambar(hasil, halaman, gambar.getSubimage(0, baris, gambar.getWidth(), barisBagian),
                            kiri, y + label, lebar, tinggiBagian);
                    y += label + tinggiBagian + jarak;
                }
            }
        }
    }

    private static JRBasePrintPage halamanBaru(JasperPrint hasil, Map<String, Object> identitas,
            int kiri, int lebar, int atas) {
        JRBasePrintPage halaman = new JRBasePrintPage();
        hasil.addPage(halaman);
        teks(hasil, halaman, kiri, atas, lebar, 18, "LAMPIRAN FOTO HASIL PEMERIKSAAN PA", 11f);
        teks(hasil, halaman, kiri, atas + 19, lebar, 16,
                "No. RM: " + nilai(identitas, "norm") + "   |   " + nilai(identitas, "namapasien")
                + "   |   " + nilai(identitas, "tanggal") + " " + nilai(identitas, "jam"), 8f);
        return halaman;
    }

    private static String nilai(Map<String, Object> identitas, String kunci) {
        Object nilai = identitas.get(kunci);
        return nilai == null ? "" : nilai.toString();
    }

    private static void teks(JasperPrint hasil, JRBasePrintPage halaman, int x, int y,
            int lebar, int tinggi, String isi, float ukuran) {
        JRBasePrintText teks = new JRBasePrintText(hasil.getDefaultStyleProvider());
        teks.setX(x); teks.setY(y); teks.setWidth(lebar); teks.setHeight(tinggi);
        teks.setFontName("SansSerif"); teks.setFontSize(ukuran); teks.setText(isi);
        teks.setTextHeight(tinggi); teks.setLineSpacingFactor(1f);
        halaman.addElement(teks);
    }

    private static void gambar(JasperPrint hasil, JRBasePrintPage halaman, BufferedImage data,
            int x, int y, int lebar, int tinggi) throws JRException {
        JRBasePrintImage gambar = new JRBasePrintImage(hasil.getDefaultStyleProvider());
        gambar.setX(x); gambar.setY(y); gambar.setWidth(lebar); gambar.setHeight(tinggi);
        gambar.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (!ImageIO.write(data, "png", bytes)) throw new IOException("Encoder PNG tidak tersedia.");
            gambar.setRenderer(SimpleDataRenderer.getInstance(bytes.toByteArray()));
        } catch (IOException ex) { throw new JRException("Foto PA gagal disiapkan untuk report.", ex); }
        halaman.addElement(gambar);
    }
}
