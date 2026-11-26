package laporan;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

public final class DlgSensusHarianRanap extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(DlgSensusHarianRanap.class.getName());
    private static final ZoneId WITA = ZoneId.of("Asia/Makassar");
    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter FORMAT_BULAN = DateTimeFormatter.ofPattern("MMMM yyyy", INDONESIA);
    private static final DateTimeFormatter FORMAT_JAM = DateTimeFormatter.ofPattern("HH.mm");
    private static final String JUDUL = "Sensus Harian Ranap";
    private static final String NAMA_TEMPLATE = "rptSensusHarianRanapRSAJ.jasper";

    private final JComboBox<String> pilihanBulan = SensusHarianRanapUi.pilihan(145);
    private final JSpinner pilihanTahun;
    private final JComboBox<SensusHarianRanap.Ruang> pilihanRuang = SensusHarianRanapUi.pilihan(320);
    private final JButton tampilkan = SensusHarianRanapUi.tombol("Tampilkan", true);
    private final JPanel pratinjau = new JPanel(new BorderLayout());
    private final JLabel status = SensusHarianRanapUi.label(" ", 12, false);

    public DlgSensusHarianRanap(Window pemilik, String namaRuangAwal) {
        super(pemilik, JUDUL, ModalityType.DOCUMENT_MODAL);
        YearMonth bulanKini = YearMonth.now(WITA);
        for (java.time.Month bulan : java.time.Month.values()) {
            pilihanBulan.addItem(bulan.getDisplayName(TextStyle.FULL, INDONESIA));
        }
        pilihanBulan.setSelectedIndex(bulanKini.getMonthValue() - 1);
        pilihanTahun = SensusHarianRanapUi.tahun(new SpinnerNumberModel(bulanKini.getYear(), 1900, 9999, 1));
        tampilkan.addActionListener(evt -> tampilkanLaporan());
        JButton tutup = SensusHarianRanapUi.tombol("Tutup", false);
        tutup.addActionListener(evt -> dispose());

        JPanel halaman = new JPanel(new BorderLayout(0, 12));
        halaman.setBackground(SensusHarianRanapUi.LATAR);
        halaman.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        halaman.add(buatFilter(tutup), BorderLayout.NORTH);
        pratinjau.setBackground(Color.WHITE);
        pratinjau.setBorder(BorderFactory.createLineBorder(SensusHarianRanapUi.GARIS));
        halaman.add(pratinjau, BorderLayout.CENTER);
        halaman.add(status, BorderLayout.SOUTH);
        setContentPane(halaman);
        getRootPane().setDefaultButton(tampilkan);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        sesuaikanLayar();
        muatRuang(namaRuangAwal);
    }

    private JPanel buatFilter(JButton tutup) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(SensusHarianRanapUi.label("Sensus Harian Rawat Inap", 21, true), BorderLayout.NORTH);

        JPanel baris = new JPanel(new GridBagLayout());
        baris.setOpaque(false);
        Component[] kontrol = {
            SensusHarianRanapUi.kolom("Bulan", pilihanBulan),
            SensusHarianRanapUi.kolom("Tahun", pilihanTahun),
            SensusHarianRanapUi.kolom("Ruang Rawat", pilihanRuang),
            tampilkan, tutup
        };
        GridBagConstraints posisi = new GridBagConstraints();
        posisi.gridy = 0;
        posisi.anchor = GridBagConstraints.SOUTHWEST;
        posisi.insets = new Insets(0, 0, 0, 10);
        for (Component item : kontrol) {
            baris.add(item, posisi);
            posisi.gridx = baris.getComponentCount();
        }
        posisi.weightx = 1;
        baris.add(javax.swing.Box.createHorizontalGlue(), posisi);
        panel.add(baris, BorderLayout.CENTER);
        return panel;
    }

    private void sesuaikanLayar() {
        GraphicsConfiguration konfigurasi = getGraphicsConfiguration();
        Rectangle area = konfigurasi.getBounds();
        Insets tepi = Toolkit.getDefaultToolkit().getScreenInsets(konfigurasi);
        area.x += tepi.left;
        area.y += tepi.top;
        area.width -= tepi.left + tepi.right;
        area.height -= tepi.top + tepi.bottom;
        setMinimumSize(new Dimension(Math.min(780, area.width), Math.min(480, area.height)));
        setBounds(area);
    }

    private void muatRuang(String namaRuangAwal) {
        ubahSibuk(true, "Memuat daftar ruang rawat...");
        new SwingWorker<List<SensusHarianRanap.Ruang>, Void>() {
            @Override
            protected List<SensusHarianRanap.Ruang> doInBackground() throws Exception {
                try (Connection koneksi = koneksiDB.condbTransaksi()) {
                    return SensusHarianRanap.daftarRuang(koneksi);
                }
            }

            @Override
            protected void done() {
                if (!isDisplayable()) {
                    return;
                }
                try {
                    for (SensusHarianRanap.Ruang ruang : get()) {
                        pilihanRuang.addItem(ruang);
                        if (ruang.nama.equalsIgnoreCase(namaRuangAwal)) {
                            pilihanRuang.setSelectedItem(ruang);
                        }
                    }
                    tampilkanLaporan();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    tampilkanKesalahan("Daftar ruang rawat gagal dimuat.", ex);
                } catch (ExecutionException ex) {
                    tampilkanKesalahan("Daftar ruang rawat gagal dimuat.", ex.getCause());
                }
            }
        }.execute();
    }

    private void tampilkanLaporan() {
        try {
            pilihanTahun.commitEdit();
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Masukkan tahun yang valid.", JUDUL, JOptionPane.WARNING_MESSAGE);
            return;
        }
        YearMonth bulan = YearMonth.of((Integer) pilihanTahun.getValue(), pilihanBulan.getSelectedIndex() + 1);
        if (bulan.isAfter(YearMonth.now(WITA))) {
            JOptionPane.showMessageDialog(this, "Bulan sensus belum dimulai.", JUDUL, JOptionPane.WARNING_MESSAGE);
            return;
        }
        SensusHarianRanap.Ruang ruang = (SensusHarianRanap.Ruang) pilihanRuang.getSelectedItem();
        if (ruang == null) {
            return;
        }

        String instansi = akses.getnamars();
        String namaRS = instansi == null || instansi.trim().isEmpty()
                ? "RS Akademis Jaury Jusuf Putera" : instansi.trim();
        pratinjau.removeAll();
        pratinjau.revalidate();
        pratinjau.repaint();
        ubahSibuk(true, "Menghitung sensus harian...");
        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                LocalDateTime sekarang = LocalDateTime.now(WITA);
                SensusHarianRanap.Rekap rekap;
                try (Connection koneksi = koneksiDB.condbTransaksi()) {
                    rekap = SensusHarianRanap.muat(koneksi, bulan, ruang, sekarang);
                }
                return buatLaporan(namaRS, bulan, ruang, rekap, sekarang);
            }

            @Override
            protected void done() {
                if (!isDisplayable()) {
                    return;
                }
                try {
                    PratinjauSensus viewer = new PratinjauSensus(get());
                    pratinjau.add(viewer, BorderLayout.CENTER);
                    pratinjau.revalidate();
                    pratinjau.repaint();
                    SwingUtilities.invokeLater(viewer::sesuaikanLebar);
                    ubahSibuk(false, "");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    tampilkanKesalahan("Laporan sensus harian gagal dimuat.", ex);
                } catch (ExecutionException ex) {
                    Throwable penyebab = ex.getCause();
                    String pesan = penyebab instanceof FileNotFoundException ? penyebab.getMessage()
                            : "Laporan sensus harian gagal dimuat.";
                    tampilkanKesalahan(pesan, penyebab);
                }
            }
        }.execute();
    }

    static JasperPrint buatLaporan(String namaRS, YearMonth bulan, SensusHarianRanap.Ruang ruang,
                                   SensusHarianRanap.Rekap rekap, LocalDateTime sekarang) throws Exception {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("RUMAH_SAKIT", namaRS);
        parameter.put("DATA_BULAN", bulan.format(FORMAT_BULAN));
        parameter.put("RUANG_RAWAT", ruang.nama);
        parameter.put("TT_TERSEDIA", rekap.tempatTidur);
        String catatan = "Waktu sensus WITA. TT tersedia berdasarkan tempat tidur aktif saat laporan dibuat.";
        if (bulan.equals(YearMonth.from(sekarang))) {
            catatan += " Tanggal " + sekarang.getDayOfMonth() + " sementara sampai " + sekarang.format(FORMAT_JAM) + " WITA.";
        }
        parameter.put("CATATAN", catatan);
        parameter.put("VERIFIKASI", rekap.riwayatPerluVerifikasi == 0 ? ""
                : "Perlu verifikasi: " + rekap.riwayatPerluVerifikasi + " catatan kamar memiliki waktu atau status yang belum konsisten.");

        List<Map<String, ?>> barisLaporan = new ArrayList<>();
        for (Integer[] hari : rekap.baris) {
            Map<String, Integer> baris = new HashMap<>();
            for (int i = 0; i < hari.length; i++) {
                baris.put("kolom" + (i + 1), hari[i]);
            }
            barisLaporan.add(baris);
        }
        try (InputStream template = bukaTemplate()) {
            return JasperFillManager.fillReport(template, parameter, new JRMapCollectionDataSource(barisLaporan));
        }
    }

    private static InputStream bukaTemplate() throws IOException {
        InputStream bawaan = DlgSensusHarianRanap.class.getResourceAsStream(NAMA_TEMPLATE);
        if (bawaan != null) {
            return bawaan;
        }

        Path eksternal = Path.of("report", NAMA_TEMPLATE).toAbsolutePath().normalize();
        try {
            return Files.newInputStream(eksternal);
        } catch (NoSuchFileException ex) {
            FileNotFoundException tidakAda = new FileNotFoundException(
                    "Template Sensus Harian Ranap belum terpasang.\n"
                    + "Salin " + NAMA_TEMPLATE + " ke src/laporan lalu Clean and Build.\n"
                    + "Folder report yang diperiksa: " + eksternal.getParent());
            tidakAda.initCause(ex);
            throw tidakAda;
        }
    }

    private void ubahSibuk(boolean sibuk, String pesan) {
        pilihanBulan.setEnabled(!sibuk);
        pilihanTahun.setEnabled(!sibuk);
        pilihanRuang.setEnabled(!sibuk);
        tampilkan.setEnabled(!sibuk && pilihanRuang.getItemCount() > 0);
        status.setText(pesan.isEmpty() ? " " : pesan);
    }

    private void tampilkanKesalahan(String pesan, Throwable penyebab) {
        LOG.log(Level.WARNING, pesan, penyebab);
        ubahSibuk(false, pesan);
        JOptionPane.showMessageDialog(this, pesan, JUDUL, JOptionPane.ERROR_MESSAGE);
    }

    private static final class PratinjauSensus extends JRViewer {
        private static final long serialVersionUID = 1L;

        PratinjauSensus(JasperPrint hasil) {
            super(hasil);
            SensusHarianRanapUi.fontPratinjau(this);
        }

        void sesuaikanLebar() {
            viewerContext.fitWidth();
        }
    }
}
