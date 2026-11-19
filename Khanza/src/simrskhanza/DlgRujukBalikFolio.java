package simrskhanza;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import widget.Tanggal;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

final class DlgRujukBalikFolio extends JPanel {
    private static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.forLanguageTag("id-ID"));
    private static final Color BIRU = new Color(29, 96, 202);
    private static final Color TEKS = new Color(28, 47, 73);
    private static final Color GARIS = new Color(187, 202, 220);
    private static final Color LATAR = new Color(243, 247, 252);
    private static final int SUDUT = 10;
    private static final String JUDUL_OBAT = "Obat yang diberikan:\n";

    private final String noRawat;
    private final boolean bpjs;
    private final JTextField pasien = new JTextField();
    private final JTextField tujuan = new JTextField();
    private final JTextField dpjp = new JTextField();
    private final Tanggal tanggal = new Tanggal();
    private final JTextArea diagnosa = new JTextArea(3, 30);
    private final JTextArea terapi = new JTextArea(4, 24);
    private final JTextArea obat = new JTextArea(4, 24);
    private final JComboBox<TindakLanjut> tindakLanjut = new JComboBox<>(TindakLanjut.values());
    private final Tanggal tanggalKontrol = new Tanggal();
    private final JTextField keteranganLain = new JTextField();
    private final JButton simpan = tombol("Simpan", true);
    private final JButton cetak = tombol("Cetak Folio", false);
    private final JButton unggah = tombol("Upload", false);
    private final JLabel statusUnggah = label("", 12, Font.PLAIN);
    private final JButton tutup = tombol("Tutup", false);
    private String noRm, namaPasien, alamatPasien, jenisKelamin;
    private Date tanggalLahir;
    private boolean rawatInap;
    private String kodeDokter = "";
    private Surat tersimpan;
    private final Supplier<Date> tanggalBersama;
    private final Supplier<String> tujuanBersama;
    private final boolean gabungan;

    static void buka(Component induk, String noRawat, boolean bpjs) {
        if (!bolehBuka(bpjs)) {
            JOptionPane.showMessageDialog(induk, "Akun ini belum memiliki akses Surat Rujuk Balik untuk penjamin kunjungan ini.");
            return;
        }
        Window pemilik = induk instanceof Window ? (Window) induk
                : induk == null ? null : SwingUtilities.getWindowAncestor(induk);
        try {
            DlgRujukBalikFolio folio = new DlgRujukBalikFolio(noRawat, bpjs, null, null);
            JDialog dialog = new JDialog(pemilik, "Surat Rujuk Balik Folio", java.awt.Dialog.ModalityType.MODELESS);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setContentPane(folio);
            dialog.pack();
            DlgRujukBalik.aturUkuranLayar(dialog);
            dialog.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(induk, pesanDatabase(e, "membuka"));
        }
    }

    private static boolean bolehBuka(boolean bpjs) {
        return akses.getrujukan_keluar() || (bpjs && akses.getbpjs_rujukan_keluar());
    }

    DlgRujukBalikFolio(String noRawat, boolean bpjs, Supplier<Date> tanggalBersama,
            Supplier<String> tujuanBersama) throws SQLException {
        this.noRawat = noRawat;
        this.bpjs = bpjs;
        this.tanggalBersama = tanggalBersama;
        this.tujuanBersama = tujuanBersama;
        this.gabungan = tanggalBersama != null;
        muatKunjungan();
        buatTampilan();
    }

    Date tanggalAwalSurat() {
        return tanggalSql(tanggal.getDate());
    }

    String tujuanTersimpan() {
        return tersimpan == null ? "" : tersimpan.tujuan;
    }

    private void buatTampilan() {
        setLayout(new BorderLayout());
        JPanel isi = new JPanel(new BorderLayout(0, 14));
        isi.setBackground(LATAR);
        if (!gabungan) {
            isi.setBorder(BorderFactory.createEmptyBorder(16, 20, 15, 20));
        }
        JButton tarik = tombol("Tarik Data Klinis", false);
        tarik.setToolTipText("Ambil diagnosa, terapi CPPT, dan obat yang tercatat diberikan pada kunjungan ini");
        tarik.addActionListener(e -> tarikDataKlinis());
        JPanel judul = new JPanel(new BorderLayout(12, 0));
        judul.setOpaque(false);
        JPanel identitasSurat = new JPanel(new GridLayout(0, 1, 0, 1));
        identitasSurat.setOpaque(false);
        identitasSurat.add(label(gabungan ? "Surat Folio" : "Surat Rujuk Balik Folio", gabungan ? 15 : 20, Font.BOLD));
        if (!gabungan) {
            identitasSurat.add(label(bpjs ? "Jenis bayar BPJS" : "Jenis bayar Non-BPJS", 12, Font.PLAIN));
        }
        judul.add(identitasSurat, BorderLayout.CENTER);
        judul.add(tarik, BorderLayout.EAST);
        isi.add(judul, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
        int baris = 0;
        if (!gabungan) {
            baris(form, baris++, duaKolom(
                    bidang("No. Rawat", inputTeks(new JTextField(noRawat), false)),
                    bidang("Pasien / No. RM", inputTeks(pasien, false))), 0);
            baris(form, baris++, duaKolom(
                    bidang("Tanggal Surat", DlgRujukBalik.gayaDropdown(tanggal)),
                    bidang("Faskes / Dokter Tujuan", inputTeks(tujuan, true))), 0);
        }
        JButton pilihDokter = tombol("Pilih DPJP", false);
        pilihDokter.addActionListener(e -> DlgRujukBalik.pilihDokter(SwingUtilities.getWindowAncestor(this), this::setDpjp));
        JPanel dokter = new JPanel(new BorderLayout(8, 0));
        dokter.setOpaque(false);
        dokter.add(inputTeks(dpjp, false), BorderLayout.CENTER);
        dokter.add(pilihDokter, BorderLayout.EAST);

        baris(form, baris++, bidang("Dokter Penanggung Jawab", dokter), 0);
        baris(form, baris++, bidang("Diagnosa", bidangKlinis(diagnosa)), 1);
        terapi.setName("Terapi");
        obat.setName("Obat");
        baris(form, baris++, duaKolom(
                bidang("Terapi / Instruksi CPPT", bidangKlinis(terapi)),
                bidang("Obat yang Diberikan", bidangKlinis(obat))), 2);
        baris(form, baris, bidangTindakLanjut(), 0);
        isi.add(form, BorderLayout.CENTER);

        simpan.setMnemonic('S');
        simpan.addActionListener(e -> simpan(true));
        cetak.setMnemonic('T');
        cetak.setEnabled(tersimpan != null);
        aturTombolFolio(false);
        unggah.setToolTipText("Upload gambar PNG ke Berkas Digital, 138 Berkas Lain-lain");
        unggah.addActionListener(e -> prosesFolio(true));
        cetak.addActionListener(e -> cetak());
        tutup.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        JPanel kaki = new JPanel(new BorderLayout(0, 7));
        kaki.setOpaque(false);
        if (bpjs) {
            kaki.add(statusUnggah, BorderLayout.NORTH);
        }
        if (!gabungan) {
            JPanel tindakan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            tindakan.setOpaque(false);
            tindakan.add(simpan);
            tambahTombolFolio(tindakan);
            tindakan.add(tutup);
            kaki.add(tindakan, BorderLayout.EAST);
        }
        isi.add(kaki, BorderLayout.SOUTH);

        add(isi, BorderLayout.CENTER);
        setPreferredSize(gabungan ? new Dimension(552, 550) : new Dimension(780, 665));
    }

    void tambahTombolFolio(JPanel tindakan) {
        tindakan.add(cetak);
        if (bpjs) {
            tindakan.add(unggah);
        }
    }

    private static JLabel label(String teks, int ukuran, int gaya) {
        JLabel label = new JLabel(teks);
        label.setFont(new Font("Segoe UI", gaya, ukuran));
        label.setForeground(TEKS);
        return label;
    }

    private static JPanel bidang(String teks, JComponent input) {
        JPanel bidang = new JPanel(new BorderLayout(0, 4));
        bidang.setOpaque(false);
        JLabel judul = label(teks, 12, Font.BOLD);
        judul.setLabelFor(input instanceof JLayer<?> ? ((JLayer<?>) input).getView() : input);
        bidang.add(judul, BorderLayout.NORTH);
        bidang.add(input, BorderLayout.CENTER);
        return bidang;
    }

    private static JPanel duaKolom(JPanel kiri, JPanel kanan) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.add(kiri);
        panel.add(kanan);
        return panel;
    }

    private static void baris(JPanel form, int nomor, JComponent isi, int bobot) {
        GridBagConstraints posisi = new GridBagConstraints();
        posisi.gridx = 0;
        posisi.gridy = nomor;
        posisi.insets = new Insets(4, 0, 4, 0);
        posisi.weightx = 1;
        posisi.weighty = bobot;
        posisi.fill = GridBagConstraints.BOTH;
        form.add(isi, posisi);
    }

    private static JPanel inputTeks(JTextField input, boolean bisaDiedit) {
        input.setUI(new BasicTextFieldUI());
        input.setEditable(bisaDiedit);
        input.setBorder(BorderFactory.createEmptyBorder());
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        input.setForeground(TEKS);
        input.setOpaque(false);
        return bingkaiInput(input, bisaDiedit ? Color.WHITE : LATAR, 7, 10);
    }

    private JPanel bidangTindakLanjut() {
        tindakLanjut.setSelectedItem(tersimpan == null
                ? TindakLanjut.KONSULTASI_SELESAI : tersimpan.tindakLanjut);
        tanggalKontrol.setName("Tanggal Kontrol");
        JPanel kontrol = bidang("Tanggal Kontrol", DlgRujukBalik.gayaDropdown(tanggalKontrol));
        JPanel lain = bidang("Keterangan Lain-lain", inputTeks(keteranganLain, true));
        JPanel tambahan = new JPanel(new BorderLayout());
        tambahan.setOpaque(false);
        tindakLanjut.addActionListener(e -> tampilkanRincianTindakLanjut(tambahan, kontrol, lain));
        tampilkanRincianTindakLanjut(tambahan, kontrol, lain);
        return duaKolom(bidang("Tindak Lanjut yang Dianjurkan", DlgRujukBalik.gayaDropdown(tindakLanjut)), tambahan);
    }

    private void tampilkanRincianTindakLanjut(JPanel tempat, JPanel kontrol, JPanel lain) {
        tempat.removeAll();
        TindakLanjut pilihan = (TindakLanjut) tindakLanjut.getSelectedItem();
        if (pilihan == TindakLanjut.KONTROL_RS) {
            tempat.add(kontrol);
        } else if (pilihan == TindakLanjut.LAIN_LAIN) {
            tempat.add(lain);
        }
        tempat.revalidate();
        tempat.repaint();
    }

    private static JPanel bidangKlinis(JTextArea input) {
        input.setUI(new BasicTextAreaUI());
        input.setBorder(BorderFactory.createEmptyBorder());
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        input.setForeground(TEKS);
        input.setOpaque(false);
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(input,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setMinimumSize(new Dimension(80, 36));
        scroll.getViewport().setOpaque(false);
        return bingkaiInput(scroll, Color.WHITE, 8, 10);
    }

    private static JPanel bingkaiInput(JComponent input, Color latar, int vertikal, int horizontal) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Component pemilikFokus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                boolean fokus = pemilikFokus != null && SwingUtilities.isDescendingFrom(pemilikFokus, this);
                gambarBingkai(g, getWidth(), getHeight(), latar, fokus ? BIRU : GARIS);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(vertikal, horizontal, vertikal, horizontal));
        panel.add(input);
        FocusAdapter penyegar = new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { panel.repaint(); }
            @Override public void focusLost(FocusEvent e) { panel.repaint(); }
        };
        amatiFokus(input, penyegar);
        return panel;
    }

    private static void amatiFokus(Component komponen, FocusAdapter penyegar) {
        komponen.addFocusListener(penyegar);
        if (komponen instanceof Container) {
            for (Component anak : ((Container) komponen).getComponents()) {
                amatiFokus(anak, penyegar);
            }
        }
    }

    private static void gambarBingkai(Graphics graphics, int lebar, int tinggi, Color isi,
            Color garis) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(isi);
        g.fillRoundRect(1, 1, lebar - 2, tinggi - 2, SUDUT, SUDUT);
        g.setColor(garis);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(1, 1, lebar - 3, tinggi - 3, SUDUT, SUDUT);
        g.dispose();
    }

    private static JButton tombol(String teks, boolean utama) {
        JButton tombol = new JButton(teks) {
            @Override protected void paintComponent(Graphics g) {
                Color latar = utama ? BIRU : Color.WHITE;
                if (!isEnabled()) {
                    latar = LATAR;
                } else if (getModel().isRollover() || getModel().isPressed()) {
                    latar = utama ? BIRU.darker() : LATAR;
                }
                gambarBingkai(g, getWidth(), getHeight(), latar,
                        utama || isFocusOwner() ? BIRU : GARIS);
                super.paintComponent(g);
            }
        };
        tombol.setUI(new BasicButtonUI());
        tombol.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tombol.setForeground(utama ? Color.WHITE : BIRU);
        tombol.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        tombol.setContentAreaFilled(false);
        tombol.setOpaque(false);
        tombol.setFocusPainted(utama);
        tombol.setRolloverEnabled(true);
        return tombol;
    }

    private void muatKunjungan() throws SQLException {
        String sql = "select p.no_rkm_medis, p.nm_pasien, p.alamat, p.jk, p.tgl_lahir, r.status_lanjut, "
                + "r.kd_dokter as kode_reg, dr.nm_dokter as dokter_reg, rm.perujuk, "
                + "s.kd_dokter, ds.nm_dokter as dokter_surat, s.tanggal_surat, s.tujuan, s.diagnosa, s.terapi, "
                + "s.tindak_lanjut, s.tanggal_kontrol, s.keterangan_lain, s.revisi "
                + "from reg_periksa r join pasien p on p.no_rkm_medis=r.no_rkm_medis "
                + "left join dokter dr on dr.kd_dokter=r.kd_dokter "
                + "left join rujuk_masuk rm on rm.no_rawat=r.no_rawat "
                + "left join surat_rujuk_balik s on s.no_rawat=r.no_rawat "
                + "left join dokter ds on ds.kd_dokter=s.kd_dokter where r.no_rawat=?";
        try (Connection koneksi = koneksiDB.condbTransaksi();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Kunjungan pasien tidak ditemukan.", "02000");
                }
                noRm = rs.getString("no_rkm_medis");
                namaPasien = rs.getString("nm_pasien");
                alamatPasien = rs.getString("alamat");
                jenisKelamin = rs.getString("jk");
                tanggalLahir = rs.getDate("tgl_lahir");
                pasien.setText(namaPasien + " / " + noRm);
                rawatInap = "Ranap".equalsIgnoreCase(rs.getString("status_lanjut"));
                int revisi = rs.getInt("revisi");
                if (revisi > 0) {
                    tersimpan = new Surat(rs.getString("kd_dokter"), rs.getDate("tanggal_surat"),
                            rs.getString("tujuan"), rs.getString("diagnosa"), rs.getString("terapi"),
                            TindakLanjut.valueOf(rs.getString("tindak_lanjut")), rs.getDate("tanggal_kontrol"),
                            rs.getString("keterangan_lain"), revisi);
                    setDpjp(tersimpan.kodeDokter, rs.getString("dokter_surat"));
                    tanggal.setDate(tersimpan.tanggal);
                    tujuan.setText(tersimpan.tujuan);
                    isiBidangKlinis(diagnosa, tersimpan.diagnosa);
                    isiTerapiTersimpan(tersimpan.terapi);
                    if (tersimpan.tanggalKontrol != null) {
                        tanggalKontrol.setDate(tersimpan.tanggalKontrol);
                    }
                    keteranganLain.setText(tersimpan.keteranganLain);
                    return;
                }
                setDpjp(rs.getString("kode_reg"), rs.getString("dokter_reg"));
                tanggal.setDate(Date.valueOf(LocalDate.now()));
                tujuan.setText(rs.getString("perujuk"));
            }
            if (rawatInap) {
                muatDpjpRanap(koneksi);
            }
            DataKlinis data = bacaDataKlinis(koneksi);
            isiBidangKlinis(diagnosa, data.diagnosa);
            isiBidangKlinis(terapi, data.terapi);
            isiBidangKlinis(obat, data.obat);
        }
    }

    private void muatDpjpRanap(Connection koneksi) throws SQLException {
        String sql = "select d.kd_dokter,d.nm_dokter from dpjp_ranap p "
                + "join dokter d on d.kd_dokter=p.kd_dokter where p.no_rawat=?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    setDpjp(rs.getString(1), rs.getString(2));
                    if (rs.next()) {
                        setDpjp("", "");
                    }
                }
            }
        }
    }

    private DataKlinis bacaDataKlinis(Connection koneksi) throws SQLException {
        String hasilDiagnosa = bacaDiagnosa(koneksi, rawatInap ? "Ranap" : "Ralan");
        if (hasilDiagnosa.isEmpty()) {
            hasilDiagnosa = bacaDiagnosa(koneksi, rawatInap ? "Ralan" : "Ranap");
        }
        String hasilTerapi = bacaTerapiCppt(koneksi, rawatInap);
        if (hasilTerapi.isEmpty()) {
            hasilTerapi = bacaTerapiCppt(koneksi, !rawatInap);
        }
        return new DataKlinis(hasilDiagnosa, hasilTerapi, bacaObatDiberikan(koneksi));
    }

    private String bacaDiagnosa(Connection koneksi, String status) throws SQLException {
        String sql = "select concat(d.kd_penyakit,' - ',p.nm_penyakit) "
                + "from diagnosa_pasien d join penyakit p on p.kd_penyakit=d.kd_penyakit "
                + "where d.no_rawat=? and d.status=? order by d.prioritas,d.kd_penyakit";
        StringJoiner hasil = new StringJoiner("\n");
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(rs.getString(1));
                }
            }
        }
        return hasil.toString();
    }

    private String bacaTerapiCppt(Connection koneksi, boolean ranap) throws SQLException {
        String tabel = ranap ? "pemeriksaan_ranap" : "pemeriksaan_ralan";
        String sql = "select rtl,instruksi from " + tabel + " where no_rawat=? "
                + "order by tgl_perawatan desc,jam_rawat desc,nip";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String plan = teksCppt(rs.getString("rtl"));
                    String instruksi = teksCppt(rs.getString("instruksi"));
                    if (!plan.isEmpty()) {
                        return instruksi.isEmpty() || plan.equals(instruksi)
                                ? plan : plan + "\n\nInstruksi: " + instruksi;
                    }
                    if (!instruksi.isEmpty()) {
                        return instruksi;
                    }
                }
            }
        }
        return "";
    }

    private String bacaObatDiberikan(Connection koneksi) throws SQLException {
        String sql = "select b.nama_brng,s.satuan,sum(o.jml) as jumlah "
                + "from detail_pemberian_obat o join databarang b on b.kode_brng=o.kode_brng "
                + "left join kodesatuan s on s.kode_sat=b.kode_sat "
                + "where o.no_rawat=? group by b.kode_brng,b.nama_brng,s.satuan "
                + "having sum(o.jml)>0 order by b.nama_brng,b.kode_brng";
        StringJoiner obat = new StringJoiner("\n");
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal jumlah = rs.getBigDecimal("jumlah").stripTrailingZeros();
                    String satuan = Objects.toString(rs.getString("satuan"), "").trim();
                    obat.add((rs.getString("nama_brng") + " — " + jumlah.toPlainString() + " " + satuan).trim());
                }
            }
        }
        return obat.toString();
    }

    private static String teksCppt(String teks) {
        String isi = Objects.toString(teks, "").strip();
        return "-".equals(isi) || "–".equals(isi) || "—".equals(isi) ? "" : isi;
    }

    private void tarikDataKlinis() {
        if (!bolehBuka(bpjs)) {
            return;
        }
        DataKlinis data;
        try (Connection koneksi = koneksiDB.condbTransaksi()) {
            data = bacaDataKlinis(koneksi);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, pesanDatabase(e, "membaca"));
            return;
        }
        if (data.diagnosa.isEmpty() && data.terapi.isEmpty() && data.obat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Diagnosa, terapi atau obat belum tercatat pada kunjungan ini.");
            return;
        }
        boolean gantiDiagnosa = !data.diagnosa.isEmpty() && !data.diagnosa.equals(diagnosa.getText().trim());
        boolean gantiTerapi = !data.terapi.isEmpty() && !data.terapi.equals(terapi.getText().trim());
        boolean gantiObat = !data.obat.isEmpty() && !data.obat.equals(obat.getText().trim());
        boolean adaIsian = (gantiDiagnosa && !diagnosa.getText().isBlank())
                || (gantiTerapi && !terapi.getText().isBlank())
                || (gantiObat && !obat.getText().isBlank());
        if (adaIsian && JOptionPane.showConfirmDialog(this,
                "Ganti isian yang berbeda dengan data diagnosa, terapi atau obat terbaru?",
                "Tarik Data Klinis", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        if (gantiDiagnosa) {
            isiBidangKlinis(diagnosa, data.diagnosa);
        }
        if (gantiTerapi) {
            isiBidangKlinis(terapi, data.terapi);
        }
        if (gantiObat) {
            isiBidangKlinis(obat, data.obat);
        }
    }

    private void isiTerapiTersimpan(String isi) {
        int bagianObat = isi.startsWith(JUDUL_OBAT) ? 0 : isi.indexOf("\n\n" + JUDUL_OBAT);
        if (bagianObat < 0) {
            isiBidangKlinis(terapi, isi);
            return;
        }
        int awalObat = bagianObat == 0 ? JUDUL_OBAT.length() : bagianObat + 2 + JUDUL_OBAT.length();
        isiBidangKlinis(terapi, isi.substring(0, bagianObat));
        isiBidangKlinis(obat, isi.substring(awalObat));
    }

    private String gabungTerapiDanObat() {
        String isiTerapi = terapi.getText().trim();
        String isiObat = obat.getText().trim();
        if (isiObat.isEmpty()) {
            return isiTerapi;
        }
        return (isiTerapi.isEmpty() ? "" : isiTerapi + "\n\n") + JUDUL_OBAT + isiObat;
    }

    private static void isiBidangKlinis(JTextArea bidang, String isi) {
        bidang.setText(isi);
        bidang.setCaretPosition(0);
    }

    private static final class DataKlinis {
        final String diagnosa;
        final String terapi;
        final String obat;

        DataKlinis(String diagnosa, String terapi, String obat) {
            this.diagnosa = diagnosa;
            this.terapi = terapi;
            this.obat = obat;
        }
    }

    private void setDpjp(String kode, String nama) {
        kodeDokter = Objects.toString(kode, "");
        dpjp.setText(nama);
    }

    private static Date tanggalSql(java.util.Date pilihan) {
        return pilihan == null ? null : Date.valueOf(
                LocalDate.ofInstant(Instant.ofEpochMilli(pilihan.getTime()), ZoneId.systemDefault()));
    }

    private Date bacaTanggalKontrol() {
        String teks = Objects.toString(tanggalKontrol.getEditor().getItem(), "").strip();
        SimpleDateFormat format = new SimpleDateFormat(tanggalKontrol.getDisplayFormat());
        format.setLenient(false);
        ParsePosition posisi = new ParsePosition(0);
        java.util.Date pilihan = format.parse(teks, posisi);
        return posisi.getIndex() == teks.length() ? tanggalSql(pilihan) : null;
    }

    Date tanggalKontrolTerpilih() {
        return tindakLanjut.getSelectedItem() == TindakLanjut.KONTROL_RS ? bacaTanggalKontrol() : null;
    }

    void saatKontrolBerubah(Runnable aksi) {
        tindakLanjut.addActionListener(e -> aksi.run());
        tanggalKontrol.addActionListener(e -> aksi.run());
        Component editor = tanggalKontrol.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            ((JTextField) editor).getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(aksi); }
                @Override public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(aksi); }
                @Override public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(aksi); }
            });
        }
        aksi.run();
    }

    private Surat isiForm() {
        TindakLanjut pilihan = (TindakLanjut) tindakLanjut.getSelectedItem();
        Date kontrol = tanggalKontrolTerpilih();
        String lain = pilihan == TindakLanjut.LAIN_LAIN ? keteranganLain.getText().strip() : null;
        Date tanggalSurat = gabungan ? tanggalBersama.get() : tanggalAwalSurat();
        String tujuanSurat = gabungan ? tujuanBersama.get() : tujuan.getText();
        return new Surat(kodeDokter, tanggalSurat, tujuanSurat.trim(), diagnosa.getText().trim(),
                gabungTerapiDanObat(), pilihan, kontrol, lain, tersimpan == null ? 0 : tersimpan.revisi);
    }

    boolean simpan(boolean tampilPesan) {
        if (!bolehBuka(bpjs)) {
            return false;
        }
        Surat isi = isiForm();
        if (isi.kodeDokter.isEmpty() || dpjp.getText().trim().isEmpty() || isi.tanggal == null
                || isi.tujuan.isEmpty() || isi.diagnosa.isEmpty() || isi.terapi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi tanggal, tujuan, DPJP, diagnosa, serta terapi atau obat yang diberikan.");
            return false;
        }
        if (isi.tujuan.length() > 150 || isi.diagnosa.length() > 5000 || isi.terapi.length() > 5000) {
            JOptionPane.showMessageDialog(this, "Tujuan maksimal 150 karakter. Diagnosa dan gabungan terapi serta obat masing-masing maksimal 5.000 karakter.");
            return false;
        }
        String pesan = isi.periksaTindakLanjut();
        if (pesan != null) {
            JOptionPane.showMessageDialog(this, pesan);
            return false;
        }
        if (tersimpan != null && tersimpan.isiSama(isi)) {
            try (Connection koneksi = koneksiDB.condbTransaksi()) {
                BerkasRujukBalikFolio.periksaRevisi(koneksi, noRawat, isi.revisi);
                if (tampilPesan) {
                    JOptionPane.showMessageDialog(this, "Surat folio sudah tersimpan.");
                }
                return true;
            } catch (BerkasRujukBalikFolio.SuratBerubah e) {
                tampilkanKonflik();
                return false;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, pesanDatabase(e, "memeriksa"));
                return false;
            }
        }
        String sql = isi.revisi == 0
                ? "insert into surat_rujuk_balik (kd_dokter,tanggal_surat,tujuan,diagnosa,terapi,"
                  + "tindak_lanjut,tanggal_kontrol,keterangan_lain,no_rawat) values (?,?,?,?,?,?,?,?,?)"
                : "update surat_rujuk_balik set kd_dokter=?,tanggal_surat=?,tujuan=?,diagnosa=?,terapi=?,"
                  + "tindak_lanjut=?,tanggal_kontrol=?,keterangan_lain=?,revisi=revisi+1 "
                  + "where no_rawat=? and revisi=?";
        try (Connection koneksi = koneksiDB.condbTransaksi();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, isi.kodeDokter);
            ps.setDate(2, isi.tanggal);
            ps.setString(3, isi.tujuan);
            ps.setString(4, isi.diagnosa);
            ps.setString(5, isi.terapi);
            ps.setString(6, isi.tindakLanjut.name());
            ps.setDate(7, isi.tanggalKontrol);
            ps.setString(8, isi.keteranganLain);
            ps.setString(9, noRawat);
            if (isi.revisi > 0) {
                ps.setInt(10, isi.revisi);
            }
            if (ps.executeUpdate() == 0) {
                tampilkanKonflik();
                return false;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                tampilkanKonflik();
            } else {
                JOptionPane.showMessageDialog(this, pesanDatabase(e, "menyimpan"));
            }
            return false;
        }
        isi.revisi++;
        tersimpan = isi;
        aturTombolFolio(false);
        if (tampilPesan) {
            JOptionPane.showMessageDialog(this, "Surat folio berhasil disimpan.");
        }
        return true;
    }

    private void cetak() {
        prosesFolio(false);
    }

    private boolean bolehUnggah() {
        return bpjs && bolehBuka(true) && akses.getberkas_digital_perawatan();
    }

    void aturTombolFolio(boolean sibuk) {
        simpan.setEnabled(!sibuk);
        tutup.setEnabled(!sibuk);
        cetak.setEnabled(!sibuk && tersimpan != null);
        unggah.setEnabled(!sibuk && tersimpan != null && bolehUnggah());
        if (bpjs && !akses.getberkas_digital_perawatan()) {
            statusUnggah.setText("Unggah PNG memerlukan izin Berkas Digital Perawatan.");
        } else if (bpjs) {
            statusUnggah.setText(tersimpan == null ? "Simpan surat untuk mengaktifkan Upload."
                    : "Upload PNG ke Berkas Lain-lain (138).");
        }
    }

    private void prosesFolio(boolean upload) {
        if (!bolehBuka(bpjs) || tersimpan == null || (upload && !bolehUnggah()) || !tutup.isEnabled()) {
            return;
        }
        if (!tersimpan.isiSama(isiForm())) {
            JOptionPane.showMessageDialog(this, "Simpan perubahan surat terlebih dahulu.");
            return;
        }
        String pesan = tersimpan.periksaTindakLanjut();
        if (pesan != null) {
            JOptionPane.showMessageDialog(this, pesan);
            return;
        }
        Surat surat = tersimpan;
        String namaDokter = dpjp.getText();
        JDialog induk = (JDialog) SwingUtilities.getWindowAncestor(this);
        aturProsesFolio(induk, true);
        new SwingWorker<JasperPrint, Void>() {
            @Override protected JasperPrint doInBackground() throws Exception {
                try (Connection koneksi = koneksiDB.condbTransaksi()) {
                    BerkasRujukBalikFolio.periksaRevisi(koneksi, noRawat, surat.revisi);
                }
                JasperPrint hasil = JasperFillManager.fillReport("./report/rptSuratRujukBalik.jasper",
                        parameterCetak(surat, namaDokter), new JREmptyDataSource(1));
                if (upload) {
                    BerkasRujukBalikFolio.unggah(noRawat, surat.revisi, BerkasRujukBalikFolio.renderPng(hasil));
                }
                return hasil;
            }

            @Override protected void done() {
                try {
                    JasperPrint hasil = get();
                    if (upload) {
                        JOptionPane.showMessageDialog(DlgRujukBalikFolio.this,
                                "Seluruh halaman Surat Rujuk Balik tersimpan sebagai PNG di Berkas Lain-lain (138).");
                    } else {
                        JasperViewer viewer = new JasperViewer(hasil, false);
                        viewer.setTitle("Surat Rujuk Balik Folio");
                        viewer.setLocationRelativeTo(DlgRujukBalikFolio.this);
                        viewer.setVisible(true);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable sebab = e.getCause();
                    if (sebab instanceof BerkasRujukBalikFolio.SuratBerubah) {
                        tampilkanKonflik();
                    } else {
                        String detail = sebab instanceof BerkasRujukBalikFolio.GagalUnggah
                                ? "\n" + sebab.getMessage() : "";
                        JOptionPane.showMessageDialog(DlgRujukBalikFolio.this,
                                (upload ? "Gagal mengunggah PNG folio. Periksa report dan server Berkas Digital."
                                        : "Gagal membuka cetakan folio. Periksa file report.") + detail);
                    }
                } finally {
                    aturProsesFolio(induk, false);
                }
            }
        }.execute();
    }

    private void aturProsesFolio(JDialog induk, boolean sibuk) {
        if (induk instanceof DlgRujukBalik) {
            ((DlgRujukBalik) induk).aturSibuk(sibuk);
        } else {
            aturTombolFolio(sibuk);
            induk.setDefaultCloseOperation(sibuk ? JDialog.DO_NOTHING_ON_CLOSE : JDialog.DISPOSE_ON_CLOSE);
            induk.setCursor(Cursor.getPredefinedCursor(sibuk ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        }
    }

    private Map<String, Object> parameterCetak(Surat surat, String namaDokter) {
        Map<String, Object> data = new HashMap<>();
        File kop = new File("setting", "logo2.png");
        data.put("kop", kop.isFile() ? kop.getAbsolutePath() : null);
        data.put("logo", kop.isFile() ? null : new sekuel().cariGambar("select setting.logo from setting"));
        data.put("namaRs", akses.getnamars());
        data.put("alamatRs", akses.getalamatrs());
        data.put("kontakRs", akses.getkontakrs());
        data.put("nomorSurat", "SRB/" + noRawat);
        data.put("tujuan", surat.tujuan);
        data.put("namaPasien", namaPasien);
        data.put("noRm", noRm);
        data.put("noRawat", noRawat);
        data.put("alamat", alamatPasien);
        data.put("tglLahir", tanggalLahir == null ? "-" : tanggalLahir.toLocalDate().format(FORMAT_TANGGAL));
        data.put("jenisKelamin", "L".equals(jenisKelamin) ? "Laki-laki" : "P".equals(jenisKelamin) ? "Perempuan" : "-");
        data.put("diagnosa", surat.diagnosa);
        data.put("terapi", surat.terapi);
        data.put("tindakLanjut", surat.teksTindakLanjut());
        data.put("tempatTanggal", akses.getkabupatenrs() + ", " + surat.tanggal.toLocalDate().format(FORMAT_TANGGAL));
        data.put("dpjp", namaDokter);
        String host = koneksiDB.HOSTHYBRIDWEB();
        if (host != null && !host.isBlank() && !surat.kodeDokter.isBlank()) {
            String port = koneksiDB.PORTWEB();
            String web = koneksiDB.HYBRIDWEB();
            String kode = URLEncoder.encode(surat.kodeDokter, StandardCharsets.UTF_8).replace("+", "%20");
            data.put("ttd", "http://" + host + (port == null || port.isBlank() ? "" : ":" + port)
                    + "/" + (web == null || web.isBlank() ? "" : web.replaceAll("^/+|/+$", "") + "/")
                    + "tandatangandokter/pages/upload/" + kode + ".png");
        }
        return data;
    }

    private void tampilkanKonflik() {
        JOptionPane.showMessageDialog(this, "Surat sudah disimpan atau diubah di jendela lain. Buka kembali surat untuk memuat data terbaru.");
    }

    static String pesanDatabase(SQLException e, String tindakan) {
        if ("02000".equals(e.getSQLState())) {
            return "Kunjungan pasien tidak ditemukan.";
        }
        if ("42S02".equals(e.getSQLState()) || "42S22".equals(e.getSQLState())) {
            return "Struktur tabel Surat Rujuk Balik perlu diperbarui. Jalankan rsaj_surat_rujuk_balik.sql.";
        }
        System.out.println("Gagal " + tindakan + " Surat Rujuk Balik (" + e.getSQLState() + "/" + e.getErrorCode() + ").");
        return "Gagal " + tindakan + " Surat Rujuk Balik. Periksa koneksi dan coba lagi.";
    }

    private enum TindakLanjut {
        KONTROL_RS("Kontrol Kembali ke RS"),
        RAWAT_INAP("Perlu Rawat Inap"),
        KONSULTASI_SELESAI("Konsultasi Selesai"),
        LAIN_LAIN("Lain-lain");

        private final String label;

        TindakLanjut(String label) {
            this.label = label;
        }

        @Override public String toString() {
            return label;
        }
    }

    private static final class Surat {
        final String kodeDokter;
        final Date tanggal;
        final String tujuan, diagnosa, terapi;
        final TindakLanjut tindakLanjut;
        final Date tanggalKontrol;
        final String keteranganLain;
        int revisi;

        Surat(String kodeDokter, Date tanggal, String tujuan, String diagnosa, String terapi,
                TindakLanjut tindakLanjut, Date tanggalKontrol, String keteranganLain, int revisi) {
            this.kodeDokter = kodeDokter;
            this.tanggal = tanggal;
            this.tujuan = tujuan;
            this.diagnosa = diagnosa;
            this.terapi = terapi;
            this.tindakLanjut = tindakLanjut;
            this.tanggalKontrol = tanggalKontrol;
            this.keteranganLain = keteranganLain;
            this.revisi = revisi;
        }

        boolean isiSama(Surat lain) {
            return kodeDokter.equals(lain.kodeDokter) && Objects.equals(tanggal, lain.tanggal)
                    && tujuan.equals(lain.tujuan) && diagnosa.equals(lain.diagnosa) && terapi.equals(lain.terapi)
                    && tindakLanjut == lain.tindakLanjut && Objects.equals(tanggalKontrol, lain.tanggalKontrol)
                    && Objects.equals(keteranganLain, lain.keteranganLain);
        }

        String periksaTindakLanjut() {
            if (tindakLanjut == TindakLanjut.KONTROL_RS && tanggalKontrol == null) {
                return "Isi tanggal kontrol kembali ke RS.";
            }
            if (tindakLanjut == TindakLanjut.LAIN_LAIN && (keteranganLain == null || keteranganLain.isBlank())) {
                return "Isi keterangan tindak lanjut Lain-lain.";
            }
            if (keteranganLain != null && keteranganLain.length() > 500) {
                return "Keterangan Lain-lain maksimal 500 karakter.";
            }
            return null;
        }

        String teksTindakLanjut() {
            switch (tindakLanjut) {
                case KONTROL_RS:
                    return "Kontrol kembali ke RS pada tanggal " + tanggalKontrol.toLocalDate().format(FORMAT_TANGGAL);
                case RAWAT_INAP:
                    return "Perlu rawat inap";
                case LAIN_LAIN:
                    return keteranganLain;
                default:
                    return "Konsultasi selesai";
            }
        }
    }
}
