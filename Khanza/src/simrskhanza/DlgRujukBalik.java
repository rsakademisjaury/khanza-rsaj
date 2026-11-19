package simrskhanza;

import bridging.ApiBPJS;
import bridging.BPJSCekReferensiFaskes;
import bridging.BPJSCekReferensiPenyakit;
import bridging.BPJSCekReferensiPoli;
import bridging.VClaimRujukBalik;
import bridging.VClaimRujukBalik.Faskes;
import bridging.VClaimRujukBalik.Tipe;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import kepegawaian.DlgCariDokter;
import widget.Tanggal;

public final class DlgRujukBalik extends JDialog {
    private static final String RAWAT_INAP = "1. Rawat Inap";
    private static final String RAWAT_JALAN = "2. Rawat Jalan";
    private static final String JUDUL = "Surat Rujuk Balik BPJS";
    private static final String FORMAT_TANGGAL = "dd-MM-yyyy";
    private static final DateTimeFormatter FORMAT_CATATAN_KONTROL = DateTimeFormatter.ofPattern("dd/MM/uu");
    private static final DateTimeFormatter PEMBACA_TANGGAL =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Color BIRU = new Color(29, 96, 202);
    private static final Color TEKS = new Color(28, 47, 73);
    private static final Color GARIS = new Color(187, 202, 220);
    private static final Color LATAR = new Color(243, 247, 252);
    private static final int SUDUT_DROPDOWN = 12;

    private final VClaimRujukBalik.Sep sep;
    private final Tanggal tanggalRujukan = tanggal("Tanggal Rujukan");
    private final Tanggal rencanaKunjungan = tanggal("Rencana Kunjungan");
    private final JComboBox<Tipe> tipeRujukan = new JComboBox<>(Tipe.values());
    private final JComboBox<String> jenisPelayanan = new JComboBox<>(new String[]{RAWAT_JALAN});
    private final JTextField catatan = new JTextField();
    private String catatanKontrolTerakhir;
    private String catatanSebelumKontrol = "";
    private final Referensi ppk = new Referensi();
    private final JButton cekFktp = tombol("Cek FKTP", false);
    private final Referensi diagnosa = new Referensi();
    private final Referensi poli = new Referensi();
    private final JButton simpan = tombol("Simpan", true);
    private final JButton cetak = tombol("Cetak VClaim", false);
    private final JButton tutup = tombol("Tutup", false);
    private final DlgRujukBalikFolio folio;
    private boolean sibuk;
    private final JLabel status = label("", 12, Font.PLAIN);
    private final Map<Tipe, List<String>> riwayat;
    private final JPanel bidangKunjungan = bidang("Rencana Kunjungan", gayaDropdown(rencanaKunjungan));
    private final JPanel bidangPoli = bidang("Poli Tujuan", poli.panel);
    private boolean perluCekVClaim;
    private Faskes fktp;
    private boolean sedangCekFktp;
    private String pesanFktp = "";
    private String kodePpkManual = "", namaPpkManual = "";
    private String pelayananManual = RAWAT_JALAN;
    private Tipe tipeDitampilkan = Tipe.RUJUK_BALIK;
    private Date tanggalDraf;

    public static void buka(Component induk, String noRawat) {
        if (!akses.getbpjs_rujukan_keluar() && !akses.getrujukan_keluar()) {
            return;
        }
        if (noRawat == null || noRawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(induk, "Silakan pilih kunjungan pasien terlebih dahulu.");
            return;
        }

        String kunjungan = noRawat.trim();
        try {
            boolean bpjs = kunjunganBpjs(kunjungan);
            if (!bpjs || !akses.getbpjs_rujukan_keluar()) {
                DlgRujukBalikFolio.buka(induk, kunjungan, bpjs);
                return;
            }
            List<VClaimRujukBalik.Sep> daftar = VClaimRujukBalik.cariSep(kunjungan, null);
            if (daftar.isEmpty()) {
                JOptionPane.showMessageDialog(induk, "SEP BPJS belum tersedia. Surat folio tetap bisa diisi dan dicetak.");
                DlgRujukBalikFolio.buka(induk, kunjungan, true);
                return;
            }
            VClaimRujukBalik.Sep sep = daftar.size() == 1 ? daftar.get(0)
                    : (VClaimRujukBalik.Sep) JOptionPane.showInputDialog(induk, "Pilih SEP untuk rujuk balik.",
                            JUDUL, JOptionPane.QUESTION_MESSAGE, null, daftar.toArray(), daftar.get(0));
            if (sep == null) {
                return;
            }

            Window pemilik = induk instanceof Window ? (Window) induk
                    : induk == null ? null : SwingUtilities.getWindowAncestor(induk);
            DlgRujukBalik dialog = new DlgRujukBalik(pemilik, sep, VClaimRujukBalik.riwayat(sep.nomor));
            dialog.setVisible(true);
            dialog.cekFktpJikaDiperlukan();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(induk, DlgRujukBalikFolio.pesanDatabase(e, "membuka"));
        }
    }

    private static boolean kunjunganBpjs(String noRawat) throws SQLException {
        try (Connection koneksi = koneksiDB.condbTransaksi();
                PreparedStatement ps = koneksi.prepareStatement("select kd_pj from reg_periksa where no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Kunjungan pasien tidak ditemukan.");
                }
                return penjaminBpjs(rs.getString("kd_pj"));
            }
        }
    }

    static boolean penjaminBpjs(String kode) {
        return "BPJ".equalsIgnoreCase(Objects.toString(kode, "").trim());
    }

    private DlgRujukBalik(Window pemilik, VClaimRujukBalik.Sep sep, Map<Tipe, List<String>> riwayat) throws SQLException {
        super(pemilik, JUDUL, ModalityType.MODELESS);
        this.sep = sep;
        this.riwayat = riwayat;
        folio = new DlgRujukBalikFolio(sep.noRawat, true, this::bacaTanggalBersama, ppk.nama::getText);
        tanggalDraf = folio.tanggalAwalSurat();
        if (tanggalDraf == null) {
            tanggalDraf = Date.valueOf(LocalDate.now());
        }
        tanggalRujukan.setDate(tanggalDraf);

        diagnosa.isi(sep.kodeDiagnosa, sep.namaDiagnosa);
        tipeRujukan.setSelectedItem(Tipe.RUJUK_BALIK);
        muatTanggalDanPpkTerbit();
        catatan.setName("Catatan");
        catatan.setDocument(new batasInput((byte) 50).getKata(catatan));
        folio.saatKontrolBerubah(this::isiCatatanKontrol);
        cekFktp.addActionListener(e -> cekFktpJikaDiperlukan());
        ppk.pilih.addActionListener(e -> {
            BPJSCekReferensiFaskes dialog = new BPJSCekReferensiFaskes(null, true);
            pilihReferensi(dialog, dialog.getTable(), "diagnosa", "Pilih PPK Rujukan", ppk);
        });
        diagnosa.pilih.addActionListener(e -> {
            BPJSCekReferensiPenyakit dialog = new BPJSCekReferensiPenyakit(null, true);
            pilihReferensi(dialog, dialog.getTable(), "diagnosa", "Pilih Diagnosa Rujuk", diagnosa);
        });
        poli.pilih.addActionListener(e -> {
            BPJSCekReferensiPoli dialog = new BPJSCekReferensiPoli(null, true);
            pilihReferensi(dialog, dialog.getTable(), "Poli", "Pilih Poli Tujuan", poli);
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel isi = panelDialog(JUDUL);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        baris(form, duaKolom(bidang("No. Rawat", identitas(sep.noRawat)), bidang("No. SEP", identitas(sep.nomor))));
        baris(form, duaKolom(bidang("Pasien / No. RM", identitas(sep.namaPasien + " / " + sep.noRm)),
                bidang("Tipe Rujukan", gayaDropdown(tipeRujukan))));
        JPanel tempatKunjungan = new JPanel(new BorderLayout());
        tempatKunjungan.setOpaque(false);
        tempatKunjungan.add(bidangKunjungan);
        baris(form, duaKolom(bidang("Tanggal Rujukan / Surat", gayaDropdown(tanggalRujukan)), tempatKunjungan));
        JPanel tindakanPpk = new JPanel(new BorderLayout());
        tindakanPpk.setOpaque(false);
        tindakanPpk.add(ppk.pilih, BorderLayout.WEST);
        tindakanPpk.add(cekFktp, BorderLayout.EAST);
        ppk.panel.add(tindakanPpk, BorderLayout.EAST);
        baris(form, bidang("PPK Rujukan / Faskes Tujuan", ppk.panel));
        baris(form, bidang("Diagnosa Rujuk", diagnosa.panel));
        JPanel ruangJenisPelayanan = new JPanel();
        ruangJenisPelayanan.setOpaque(false);
        baris(form, duaKolom(bidang("Jenis Pelayanan", gayaDropdown(jenisPelayanan)), ruangJenisPelayanan));
        baris(form, bidang("Catatan", input(catatan, true)));
        baris(form, bidangPoli);
        tipeRujukan.addActionListener(e -> gantiTipeRujukan());
        jenisPelayanan.addActionListener(e -> tampilkanIsianPelayanan());
        tampilkanIsianPelayanan();
        JPanel rujukan = new JPanel(new BorderLayout(0, 14));
        rujukan.setOpaque(false);
        JLabel judulRujukan = label("Rujukan VClaim", 15, Font.BOLD);
        judulRujukan.setPreferredSize(new Dimension(0, 34));
        rujukan.add(judulRujukan, BorderLayout.NORTH);
        rujukan.add(form, BorderLayout.CENTER);
        rujukan.add(status, BorderLayout.SOUTH);

        aturUkuranLayar(this);
        boolean duaPanel = getWidth() >= 1100;
        JPanel bagian = new JPanel(new GridBagLayout());
        bagian.setBackground(LATAR);
        GridBagConstraints posisi = new GridBagConstraints();
        posisi.fill = GridBagConstraints.BOTH;
        posisi.anchor = GridBagConstraints.NORTH;
        posisi.weightx = 1;
        posisi.weighty = duaPanel ? 1 : 0;
        posisi.insets = duaPanel ? new Insets(0, 0, 0, 14) : new Insets(0, 0, 14, 0);
        bagian.add(rujukan, posisi);
        posisi.gridx = duaPanel ? 1 : 0;
        posisi.gridy = duaPanel ? 0 : 1;
        posisi.weighty = 1;
        posisi.insets = new Insets(0, 0, 0, 0);
        bagian.add(folio, posisi);
        JScrollPane gulir = new JScrollPane(bagian, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gulir.setBorder(BorderFactory.createEmptyBorder());
        gulir.getVerticalScrollBar().setUnitIncrement(20);
        isi.add(gulir, BorderLayout.CENTER);

        simpan.setMnemonic('S');
        simpan.addActionListener(e -> simpan());
        cetak.addActionListener(e -> cetak());
        tutup.addActionListener(e -> dispose());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(cetak);
        folio.tambahTombolFolio(tombol);
        tombol.add(tutup);
        isi.add(tombol, BorderLayout.SOUTH);

        tampilkanStatus();
        setContentPane(isi);
    }

    static void aturUkuranLayar(JDialog dialog) {
        Rectangle layar = new Rectangle(dialog.getGraphicsConfiguration().getBounds());
        Insets tepi = Toolkit.getDefaultToolkit().getScreenInsets(dialog.getGraphicsConfiguration());
        layar.x += tepi.left;
        layar.y += tepi.top;
        layar.width -= tepi.left + tepi.right;
        layar.height -= tepi.top + tepi.bottom;
        dialog.setMinimumSize(new Dimension(Math.min(layar.width, 640), Math.min(layar.height, 470)));
        dialog.setBounds(layar);
    }

    void aturSibuk(boolean sibuk) {
        this.sibuk = sibuk;
        setCursor(Cursor.getPredefinedCursor(sibuk ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        setDefaultCloseOperation(sibuk ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
        tampilkanStatus();
    }

    private static void baris(JPanel form, JPanel baris) {
        baris.setBorder(BorderFactory.createEmptyBorder(0, 0, 9, 0));
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, baris.getPreferredSize().height));
        form.add(baris);
    }

    private Tipe tipeTerpilih() {
        return (Tipe) tipeRujukan.getSelectedItem();
    }

    private void gantiTipeRujukan() {
        Tipe tipe = tipeTerpilih();
        if (tipe == null || tipe == tipeDitampilkan) {
            return;
        }
        if (riwayat.getOrDefault(tipeDitampilkan, List.of()).isEmpty()) {
            Date tanggal = bacaTanggalBersama();
            if (tanggal != null) {
                tanggalDraf = tanggal;
            }
        }
        boolean balik = tipe == Tipe.RUJUK_BALIK;
        if (balik) {
            pelayananManual = (String) jenisPelayanan.getSelectedItem();
            jenisPelayanan.setModel(new DefaultComboBoxModel<>(new String[]{RAWAT_JALAN}));
        } else if (jenisPelayanan.getItemCount() == 1) {
            jenisPelayanan.setModel(new DefaultComboBoxModel<>(new String[]{RAWAT_INAP, RAWAT_JALAN}));
            jenisPelayanan.setSelectedItem(pelayananManual);
        }
        tipeDitampilkan = tipe;
        tanggalRujukan.setDate(tanggalDraf);
        if (balik) {
            isiPpkDariFktp();
        } else {
            ppk.isi(kodePpkManual, namaPpkManual);
        }
        refreshTanggalDanPpkTerbit();
        tampilkanIsianPelayanan();
        tampilkanStatus();
        isiCatatanKontrol();
        cekFktpJikaDiperlukan();
    }

    private void refreshTanggalDanPpkTerbit() {
        try {
            muatTanggalDanPpkTerbit();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Tanggal dan PPK dari rujukan yang sudah terbit gagal dimuat. Buka kembali form untuk mencoba lagi.");
        }
    }

    private void muatTanggalDanPpkTerbit() throws SQLException {
        List<String> nomor = nomorRujukanTerpilih();
        if (nomor.isEmpty()) {
            return;
        }
        // Jangan pakai isian draf untuk rujukan yang sudah terbit.
        ppk.isi("", "");
        try (Connection koneksi = koneksiDB.condbTransaksi();
                PreparedStatement ps = koneksi.prepareStatement(
                        "select tglRujukan,ppkDirujuk,nm_ppkDirujuk from bridging_rujukan_bpjs "
                        + "where no_sep=? and no_rujukan=?")) {
            ps.setString(1, sep.nomor);
            ps.setString(2, nomor.get(0));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Rujukan VClaim tidak ditemukan.");
                }
                Date tanggal = rs.getDate("tglRujukan");
                if (tanggal != null) {
                    tanggalRujukan.setDate(tanggal);
                } else {
                    tanggalRujukan.setDate(folio.tanggalAwalSurat());
                }
                String kode = Objects.toString(rs.getString("ppkDirujuk"), "").trim();
                String nama = Objects.toString(rs.getString("nm_ppkDirujuk"), "").trim();
                if (nama.isEmpty()) {
                    nama = kode.isEmpty() ? folio.tujuanTersimpan() : kode;
                }
                ppk.isi(kode, nama);
            }
        }
    }

    private Date bacaTanggalBersama() {
        try {
            return bacaTanggal(tanggalRujukan);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String kodePelayanan() {
        return RAWAT_INAP.equals(jenisPelayanan.getSelectedItem()) ? "1" : "2";
    }

    private void tampilkanIsianPelayanan() {
        Tipe tipe = tipeTerpilih();
        String pelayanan = kodePelayanan();
        bidangKunjungan.setVisible(tipe.tampilKunjungan(pelayanan));
        bidangPoli.setVisible(tipe.tampilPoli(pelayanan));
        revalidate();
        repaint();
    }

    private void pilihReferensi(JDialog sumber, JTable tabelSumber, String namaInput, String judul, Referensi referensi) {
        Container kontrol = sumber.getContentPane();
        JTextField inputSumber = cariKomponen(kontrol, namaInput, JTextField.class);
        JButton cariSumber = cariKomponen(kontrol, "BtnCari", JButton.class);
        JButton cetakSumber = cariKomponen(kontrol, "BtnPrint", JButton.class);
        if (inputSumber == null || cariSumber == null || cetakSumber == null) {
            sumber.dispose();
            JOptionPane.showMessageDialog(this, "Kontrol pencarian referensi BPJS tidak ditemukan.");
            return;
        }
        JTable tabel = tabelReferensi(tabelSumber);
        tabel.getColumnModel().getColumn(0).setMaxWidth(54);
        tabel.getColumnModel().getColumn(1).setMinWidth(138);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(145);
        tabel.getColumnModel().getColumn(1).setMaxWidth(170);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(460);
        JButton cetakDaftar = tombol("Cetak", false);
        cetakDaftar.setMnemonic('T');
        cetakDaftar.addActionListener(e -> cetakSumber.doClick(0));
        try {
            tampilkanPemilih(this, judul, inputSumber, cariSumber, tabel, 1, cetakDaftar, (kode, nama) -> {
                referensi.isi(kode, nama);
                if (referensi == ppk) {
                    kodePpkManual = kode;
                    namaPpkManual = nama;
                }
            });
        } finally {
            sumber.dispose();
        }
    }

    static void pilihDokter(Window induk, BiConsumer<String, String> terpilih) {
        DlgCariDokter sumber = new DlgCariDokter(null, true);
        try {
            sumber.isCek();
            Container kontrol = sumber.getContentPane();
            JButton cariSumber = cariKomponen(kontrol, "BtnCari", JButton.class);
            JButton semuaSumber = cariKomponen(kontrol, "BtnAll", JButton.class);
            JButton tambahSumber = cariKomponen(kontrol, "BtnTambah", JButton.class);
            if (cariSumber == null || semuaSumber == null) {
                JOptionPane.showMessageDialog(induk, "Kontrol pencarian dokter tidak ditemukan.");
                return;
            }

            JTable tabel = tabelReferensi(sumber.getTable());
            // Tampilkan kode, nama, dan spesialis dari model dokter yang sama.
            for (int i = tabel.getColumnCount() - 1; i >= 2; i--) {
                if (i != 10) {
                    tabel.removeColumn(tabel.getColumnModel().getColumn(i));
                }
            }
            tabel.getColumnModel().getColumn(0).setMinWidth(128);
            tabel.getColumnModel().getColumn(0).setMaxWidth(160);
            tabel.getColumnModel().getColumn(1).setPreferredWidth(340);
            if (tabel.getColumnCount() > 2) {
                tabel.getColumnModel().getColumn(2).setPreferredWidth(190);
            }

            JPanel tindakanDokter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            tindakanDokter.setOpaque(false);
            JButton semua = tombol("Semua Dokter", false);
            semua.addActionListener(e -> {
                tabel.clearSelection();
                semuaSumber.doClick(0);
            });
            tindakanDokter.add(semua);
            if (tambahSumber != null && tambahSumber.isEnabled()) {
                JButton tambah = tombol("Tambah Dokter", false);
                tambah.addActionListener(e -> {
                    sumber.pack();
                    sumber.setLocationRelativeTo(induk);
                    tambahSumber.doClick(0);
                    semua.doClick(0);
                });
                tindakanDokter.add(tambah);
            }

            cariSumber.doClick(0);
            tampilkanPemilih(induk, "Pilih DPJP", sumber.TCari, cariSumber, tabel, 0, tindakanDokter, terpilih);
        } finally {
            sumber.dispose();
        }
    }

    private static void tampilkanPemilih(Window induk, String judul, JTextField inputSumber, JButton cariSumber,
            JTable tabel, int kolomKode, JComponent tindakanTambahan, BiConsumer<String, String> terpilih) {
        JDialog dialog = new JDialog(induk, judul, ModalityType.DOCUMENT_MODAL);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JTextField kataKunci = new JTextField(inputSumber.getDocument(), null, 25);
        kataKunci.getAccessibleContext().setAccessibleName("Kode atau nama");
        JScrollPane gulir = new JScrollPane(tabel);
        gulir.setBorder(BorderFactory.createLineBorder(GARIS));
        gulir.getViewport().setBackground(Color.WHITE);

        JButton cari = tombol("Cari", true);
        cari.setMnemonic('6');
        cari.addActionListener(e -> {
            tabel.clearSelection();
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                cariSumber.doClick(0);
            } finally {
                dialog.setCursor(Cursor.getDefaultCursor());
            }
        });
        kataKunci.addActionListener(e -> cari.doClick(0));
        JPanel pencarian = new JPanel(new BorderLayout(8, 0));
        pencarian.setOpaque(false);
        pencarian.add(bidang("Kode atau nama", input(kataKunci, true)), BorderLayout.CENTER);
        JPanel tombolCari = new JPanel(new BorderLayout());
        tombolCari.setOpaque(false);
        tombolCari.add(cari, BorderLayout.SOUTH);
        pencarian.add(tombolCari, BorderLayout.EAST);

        JPanel daftar = new JPanel(new BorderLayout(0, 12));
        daftar.setBackground(Color.WHITE);
        daftar.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        daftar.add(pencarian, BorderLayout.NORTH);
        daftar.add(gulir, BorderLayout.CENTER);

        JButton pilih = tombol("Pilih", true);
        pilih.setEnabled(false);
        Runnable pilihBaris = () -> {
            int baris = tabel.getSelectedRow();
            if (baris >= 0) {
                String kode = Objects.toString(tabel.getValueAt(baris, kolomKode), "").trim();
                String nama = Objects.toString(tabel.getValueAt(baris, kolomKode + 1), "").trim();
                if (!kode.isEmpty() && !nama.isEmpty()) {
                    terpilih.accept(kode, nama);
                    dialog.dispose();
                }
            }
        };
        pilih.addActionListener(e -> pilihBaris.run());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            int baris = tabel.getSelectedRow();
            pilih.setEnabled(baris >= 0 && !Objects.toString(tabel.getValueAt(baris, kolomKode + 1), "").trim().isEmpty());
        });
        pasangTombol(tabel, JComponent.WHEN_FOCUSED, KeyEvent.VK_ENTER, "pilihEnter", pilihBaris);
        pasangTombol(tabel, JComponent.WHEN_FOCUSED, KeyEvent.VK_SPACE, "pilihSpasi", pilihBaris);
        tabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2
                        && tabel.rowAtPoint(e.getPoint()) >= 0) {
                    pilihBaris.run();
                }
            }
        });

        JButton keluar = tombol("Keluar", false);
        keluar.setMnemonic('K');
        keluar.addActionListener(e -> dialog.dispose());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(pilih);
        tombol.add(keluar);
        JPanel tindakan = new JPanel(new BorderLayout(8, 0));
        tindakan.setOpaque(false);
        tindakan.add(tindakanTambahan, BorderLayout.WEST);
        tindakan.add(tombol, BorderLayout.EAST);

        JPanel isi = panelDialog(judul);
        isi.add(daftar, BorderLayout.CENTER);
        isi.add(tindakan, BorderLayout.SOUTH);
        dialog.setContentPane(isi);
        pasangTombol(dialog.getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW,
                KeyEvent.VK_ESCAPE, "keluar", dialog::dispose);
        ComponentAdapter ikutiInduk = new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e) {
                dialog.setBounds(induk.getBounds());
            }

            @Override public void componentResized(ComponentEvent e) {
                dialog.setBounds(induk.getBounds());
            }
        };
        induk.addComponentListener(ikutiInduk);
        dialog.setBounds(induk.getBounds());
        SwingUtilities.invokeLater(kataKunci::requestFocusInWindow);
        try {
            dialog.setVisible(true);
        } finally {
            induk.removeComponentListener(ikutiInduk);
            dialog.dispose();
        }
    }

    private static JPanel panelDialog(String judul) {
        JPanel isi = new JPanel(new BorderLayout(0, 15));
        isi.setBackground(LATAR);
        isi.setBorder(BorderFactory.createEmptyBorder(19, 20, 14, 20));
        isi.add(label(judul, 20, Font.BOLD), BorderLayout.NORTH);
        return isi;
    }

    private static JTable tabelReferensi(JTable sumber) {
        JTable tabel = new JTable(sumber.getModel());
        tabel.setAutoCreateRowSorter(true);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setFillsViewportHeight(true);
        tabel.setRowHeight(32);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel.setForeground(TEKS);
        tabel.setBackground(Color.WHITE);
        tabel.setSelectionBackground(new Color(221, 235, 255));
        tabel.setSelectionForeground(TEKS);
        tabel.setShowGrid(false);
        tabel.setIntercellSpacing(new Dimension(0, 0));
        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable daftar, Object nilai,
                    boolean dipilih, boolean fokus, int baris, int kolom) {
                super.getTableCellRendererComponent(daftar, nilai, dipilih, fokus, baris, kolom);
                setBackground(dipilih ? daftar.getSelectionBackground() : baris % 2 == 0 ? Color.WHITE : LATAR);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        JTableHeader header = tabel.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 33));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        TableCellRenderer bawaan = header.getDefaultRenderer();
        header.setDefaultRenderer((daftar, nilai, dipilih, fokus, baris, kolom) -> {
            Component sel = bawaan.getTableCellRendererComponent(daftar, nilai, dipilih, fokus, baris, kolom);
            if (sel instanceof JLabel) {
                JLabel judul = (JLabel) sel;
                judul.setHorizontalAlignment(JLabel.LEADING);
                judul.setOpaque(true);
                judul.setBackground(LATAR);
                judul.setForeground(TEKS);
                judul.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            }
            return sel;
        });
        return tabel;
    }

    private static void pasangTombol(JComponent komponen, int lingkup, int tombol, String nama, Runnable aksi) {
        komponen.getInputMap(lingkup).put(KeyStroke.getKeyStroke(tombol, 0), nama);
        komponen.getActionMap().put(nama, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                aksi.run();
            }
        });
    }

    private static <T extends Component> T cariKomponen(Container wadah, String nama, Class<T> tipe) {
        for (Component komponen : wadah.getComponents()) {
            if (nama.equals(komponen.getName()) && tipe.isInstance(komponen)) {
                return tipe.cast(komponen);
            }
            if (komponen instanceof Container) {
                T hasil = cariKomponen((Container) komponen, nama, tipe);
                if (hasil != null) {
                    return hasil;
                }
            }
        }
        return null;
    }

    private void cekFktpJikaDiperlukan() {
        if (tipeTerpilih() != Tipe.RUJUK_BALIK || sedangCekFktp || fktp != null
                || perluCekVClaim || !nomorRujukanTerpilih().isEmpty()) {
            return;
        }
        sedangCekFktp = true;
        pesanFktp = "Mencari FKTP terdaftar di BPJS…";
        tampilkanStatus();
        new SwingWorker<Faskes, Void>() {
            @Override protected Faskes doInBackground() throws Exception {
                String link = Objects.toString(koneksiDB.URLAPIBPJS(), "").trim();
                return VClaimRujukBalik.cariFaskesPeserta(sep, link, new ApiBPJS());
            }

            @Override protected void done() {
                sedangCekFktp = false;
                try {
                    fktp = get();
                    pesanFktp = "";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    pesanFktp = "Pencarian FKTP terhenti. Klik Cek FKTP untuk mencoba lagi.";
                } catch (ExecutionException e) {
                    Throwable sebab = e.getCause();
                    pesanFktp = sebab instanceof VClaimRujukBalik.Ditolak ? sebab.getMessage()
                            : "Gagal mengambil FKTP dari BPJS. Klik Cek FKTP untuk mencoba lagi.";
                }
                if (isDisplayable() && tipeTerpilih() == Tipe.RUJUK_BALIK && nomorRujukanTerpilih().isEmpty()) {
                    isiPpkDariFktp();
                    tampilkanStatus();
                }
            }
        }.execute();
    }

    private void isiPpkDariFktp() {
        ppk.isi(fktp == null ? "" : fktp.kode, fktp == null ? "" : fktp.nama);
    }

    private void isiCatatanKontrol() {
        if (sibuk || !nomorRujukanTerpilih().isEmpty() || perluCekVClaim) {
            return;
        }
        Date tanggal = folio.tanggalKontrolTerpilih();
        String catatanOtomatis = tanggal == null ? null
                : "kontrol kembali ke rs tgl. " + tanggal.toLocalDate().format(FORMAT_CATATAN_KONTROL);
        if (Objects.equals(catatanOtomatis, catatanKontrolTerakhir)) {
            return;
        }

        String nilaiSekarang = catatan.getText();
        if (catatanOtomatis != null) {
            if (catatanKontrolTerakhir == null) {
                catatanSebelumKontrol = nilaiSekarang;
            }
            catatan.setText(catatanOtomatis);
        } else if (nilaiSekarang.equals(catatanKontrolTerakhir)) {
            catatan.setText(catatanSebelumKontrol);
        }
        catatanKontrolTerakhir = catatanOtomatis;
    }

    private void simpan() {
        if (sibuk || !akses.getbpjs_rujukan_keluar()) {
            return;
        }
        isiCatatanKontrol();
        Date tanggal = bacaTanggalBersama();
        if (tanggal == null) {
            JOptionPane.showMessageDialog(this, "Isi tanggal rujukan / surat dengan benar.");
            return;
        }
        if (ppk.nama.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Isi PPK rujukan / faskes tujuan terlebih dahulu.");
            return;
        }
        if (!folio.simpan(false)) {
            return;
        }
        tanggalDraf = tanggal;
        if (!nomorRujukanTerpilih().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Surat folio tersimpan. Rujukan VClaim sudah terbit.");
            return;
        }
        if (perluCekVClaim) {
            pesanFolioTersimpan("Periksa status penerbitan di VClaim sebelum mengirim ulang.");
            return;
        }
        if (tipeTerpilih() == Tipe.RUJUK_BALIK && fktp == null) {
            pesanFolioTersimpan(sedangCekFktp ? "Tunggu pencarian FKTP selesai, lalu klik Simpan lagi untuk menerbitkan rujukan."
                    : "Gunakan Cek FKTP, lalu klik Simpan lagi untuk menerbitkan rujukan.");
            return;
        }
        if (sep.namaPasien.trim().isEmpty() || sep.kartu.trim().isEmpty() || sep.kodePpkPelayanan.trim().isEmpty()) {
            pesanFolioTersimpan("Pilih SEP dengan data pasien, kartu, dan PPK pelayanan yang lengkap.");
            return;
        }
        String isiCatatan = catatan.getText().trim();
        if (ppk.kosong() || diagnosa.kosong() || isiCatatan.isEmpty()) {
            pesanFolioTersimpan("Untuk menerbitkan VClaim, isi PPK rujukan, diagnosa rujuk, dan catatan.");
            return;
        }

        if (isiCatatan.length() > 50) {
            pesanFolioTersimpan("Catatan VClaim maksimal 50 karakter.");
            return;
        }

        Tipe tipe = tipeTerpilih();
        String pelayanan = kodePelayanan();
        boolean adaRencana = tipe.tampilKunjungan(pelayanan);
        Date kunjungan;
        try {
            kunjungan = adaRencana ? bacaTanggal(rencanaKunjungan) : tanggal;
        } catch (DateTimeParseException e) {
            pesanFolioTersimpan("Isi tanggal rencana kunjungan dengan benar.");
            return;
        }

        String link = Objects.toString(koneksiDB.URLAPIBPJS(), "").trim();
        if (link.isEmpty()) {
            pesanFolioTersimpan("Alamat layanan BPJS VClaim belum tersedia.");
            return;
        }
        String user = akses.getkode();
        String tanpaSpasi = user.replace(" ", "");
        if (tanpaSpasi.length() >= 9) {
            user = tanpaSpasi.substring(0, 9);
        }
        VClaimRujukBalik.Data data = new VClaimRujukBalik.Data(tipe, tanggal, kunjungan, ppk.kode.getText(),
                ppk.nama.getText(), pelayanan, isiCatatan,
                diagnosa.kode.getText(), diagnosa.nama.getText(), poli.kode.getText(), poli.nama.getText(), user);
        aturSibuk(true);
        status.setText("Surat folio tersimpan. Mengirim rujukan ke BPJS…");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return VClaimRujukBalik.kirim(sep, data, link, new ApiBPJS());
            }

            @Override protected void done() {
                aturSibuk(false);
                try {
                    riwayat.put(tipe, List.of(get()));
                    tampilkanStatus();
                    cetak();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusTidakPasti("");
                } catch (ExecutionException e) {
                    tanganiGagalKirim(tipe, e.getCause());
                }
            }
        }.execute();
    }

    private void pesanFolioTersimpan(String pesan) {
        JOptionPane.showMessageDialog(this, "Surat folio tersimpan.\n" + pesan);
    }

    private void tanganiGagalKirim(Tipe tipe, Throwable sebab) {
        if (sebab instanceof VClaimRujukBalik.SudahTerbit) {
            riwayat.put(tipe, ((VClaimRujukBalik.SudahTerbit) sebab).nomor);
            refreshTanggalDanPpkTerbit();
            tampilkanStatus();
            pesanFolioTersimpan("Rujukan dengan tipe ini sudah terbit. Gunakan Cetak VClaim untuk mencetak ulang.");
        } else if (sebab instanceof VClaimRujukBalik.PeriksaVClaim) {
            statusTidakPasti(((VClaimRujukBalik.PeriksaVClaim) sebab).nomor);
        } else {
            tampilkanStatus();
            pesanFolioTersimpan(sebab instanceof VClaimRujukBalik.Ditolak
                    ? "BPJS menolak rujukan. " + sebab.getMessage()
                    : "Rujukan gagal diproses. Periksa koneksi dan konfigurasi.");
        }
    }

    private void statusTidakPasti(String nomor) {
        perluCekVClaim = true;
        tampilkanStatus();
        String pesan = nomor.isEmpty() ? "Status pengiriman belum dapat dipastikan."
                : "BPJS memberi nomor " + nomor + ". Periksa penyimpanan lokal.";
        pesanFolioTersimpan(pesan + "\nPeriksa VClaim sebelum mengirim ulang.");
    }

    private void setFormAktif(boolean aktif) {
        tanggalRujukan.setEnabled(aktif);
        rencanaKunjungan.setEnabled(aktif);
        boolean balik = tipeTerpilih() == Tipe.RUJUK_BALIK;
        jenisPelayanan.setEnabled(aktif && !balik);
        catatan.setEditable(aktif);
        ppk.pilih.setEnabled(aktif && !balik);
        cekFktp.setEnabled(aktif && balik && fktp == null && !sedangCekFktp);
        diagnosa.pilih.setEnabled(aktif);
        poli.pilih.setEnabled(aktif);
    }

    private List<String> nomorRujukanTerpilih() {
        return riwayat.getOrDefault(tipeTerpilih(), List.of());
    }

    private void tampilkanStatus() {
        List<String> nomorRujukan = nomorRujukanTerpilih();
        boolean terbit = !nomorRujukan.isEmpty();
        boolean aktif = !sibuk && !terbit && !perluCekVClaim;
        boolean balik = tipeTerpilih() == Tipe.RUJUK_BALIK;
        tipeRujukan.setEnabled(!sibuk);
        ppk.pilih.setVisible(!balik);
        cekFktp.setVisible(balik && !terbit && fktp == null);
        setFormAktif(aktif);
        simpan.setEnabled(!sibuk);
        cetak.setEnabled(!sibuk && terbit);
        tutup.setEnabled(!sibuk);
        folio.aturTombolFolio(sibuk);
        if (sibuk) {
            return;
        }
        if (terbit) {
            status.setText("No. Rujukan BPJS  " + nomorRujukan.get(0));
        } else if (perluCekVClaim) {
            status.setText("Periksa status rujukan di VClaim.");
        } else {
            status.setText(balik ? pesanFktp : "");
        }
        status.setToolTipText(status.getText().isEmpty() ? null : status.getText());
    }

    private void cetak() {
        List<String> nomorRujukan = nomorRujukanTerpilih();
        if (!akses.getbpjs_rujukan_keluar() || nomorRujukan.isEmpty()) {
            return;
        }
        String nomor = nomorRujukan.size() == 1 ? nomorRujukan.get(0)
                : (String) JOptionPane.showInputDialog(this, "Pilih nomor rujukan untuk mencetak ulang.",
                        JUDUL, JOptionPane.QUESTION_MESSAGE, null, nomorRujukan.toArray(), nomorRujukan.get(0));
        if (nomor == null) {
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("norujuk", nomor);
        param.put("logo", new sekuel().cariGambar("select gambar.bpjs from gambar"));
        new validasi().MyReport("rptBridgingRujukanBPJS.jasper", param, "::[ Surat Rujukan BPJS VClaim ]::");
    }

    private static Tanggal tanggal(String nama) {
        Tanggal tanggal = new Tanggal();
        tanggal.setName(nama);
        tanggal.setDisplayFormat(FORMAT_TANGGAL);
        tanggal.setDate(new java.util.Date());
        return tanggal;
    }

    static JComponent gayaDropdown(JComboBox<?> pilihan) {
        pilihan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pilihan.setForeground(TEKS);
        pilihan.setBackground(Color.WHITE);
        pilihan.setPreferredSize(new Dimension(pilihan.isEditable() ? 135 : 180, 34));
        pilihan.setOpaque(false);
        pilihan.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 6));
        Component editor = pilihan.isEditable() ? pilihan.getEditor().getEditorComponent() : null;
        if (editor instanceof JTextField) {
            JTextField input = (JTextField) editor;
            input.setOpaque(false);
            input.setFont(pilihan.getFont());
            input.setForeground(TEKS);
            input.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        }

        // Seperti pada SKDP: gambar sendiri tampilan tertutup, dengan editor dan popup Swing tetap aktif.
        JLayer<JComboBox<?>> lapisan = new JLayer<>(pilihan, new LayerUI<JComboBox<?>>() {
            @Override public void paint(Graphics g, JComponent c) {
                int lebar = c.getWidth();
                int tinggi = c.getHeight();
                boolean aktif = pilihan.isEnabled();
                Graphics2D gambar = (Graphics2D) g.create();
                gambar.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gambar.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                RoundRectangle2D bentuk = new RoundRectangle2D.Float(
                        .5f, .5f, lebar - 1f, tinggi - 1f, SUDUT_DROPDOWN, SUDUT_DROPDOWN);
                gambar.setColor(aktif ? Color.WHITE : LATAR);
                gambar.fill(bentuk);

                Component input = pilihan.isEditable() ? pilihan.getEditor().getEditorComponent() : null;
                if (aktif && input instanceof JTextField && input.getParent() != null) {
                    Rectangle posisi = SwingUtilities.convertRectangle(input.getParent(), input.getBounds(), pilihan);
                    Graphics area = gambar.create(posisi.x, posisi.y, posisi.width, posisi.height);
                    JTextField teks = (JTextField) input;
                    teks.getUI().paint(area, teks);
                    area.dispose();
                } else {
                    gambar.setFont(pilihan.getFont());
                    gambar.setColor(aktif ? TEKS : TEKS.brighter());
                    FontMetrics metrik = gambar.getFontMetrics();
                    gambar.drawString(Objects.toString(pilihan.getSelectedItem(), ""), 10,
                            (tinggi - metrik.getHeight()) / 2 + metrik.getAscent());
                }

                for (Component anak : pilihan.getComponents()) {
                    if (aktif && anak instanceof JButton && anak.isVisible()) {
                        int x = anak.getX() + anak.getWidth() / 2;
                        int y = anak.getY() + anak.getHeight() / 2;
                        gambar.setColor(TEKS);
                        gambar.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        gambar.drawPolyline(new int[]{x - 4, x, x + 4}, new int[]{y - 2, y + 2, y - 2}, 3);
                        break;
                    }
                }
                Component fokus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                boolean disorot = aktif && (pilihan.isPopupVisible() || fokus == pilihan
                        || fokus != null && SwingUtilities.isDescendingFrom(fokus, pilihan));
                gambar.setColor(disorot ? BIRU : GARIS);
                gambar.setStroke(new BasicStroke(1f));
                gambar.draw(bentuk);
                gambar.dispose();
            }
        });
        FocusAdapter segarkanFokus = new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { lapisan.repaint(); }
            @Override public void focusLost(FocusEvent e) { lapisan.repaint(); }
        };
        pilihan.addFocusListener(segarkanFokus);
        if (editor != null) {
            editor.addFocusListener(segarkanFokus);
        }
        pilihan.addItemListener(e -> lapisan.repaint());
        pilihan.addPropertyChangeListener("enabled", e -> lapisan.repaint());
        pilihan.addPropertyChangeListener("model", e -> lapisan.repaint());
        pilihan.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { lapisan.repaint(); }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { lapisan.repaint(); }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { lapisan.repaint(); }
        });
        return lapisan;
    }

    private static Date bacaTanggal(Tanggal tanggal) {
        Object nilai = tanggal.isEditable() ? tanggal.getEditor().getItem() : tanggal.getSelectedItem();
        return Date.valueOf(LocalDate.parse(Objects.toString(nilai, "").trim(), PEMBACA_TANGGAL));
    }

    private static JLabel label(String teks, int ukuran, int gaya) {
        JLabel label = new JLabel(teks);
        label.setFont(new Font("Segoe UI", gaya, ukuran));
        label.setForeground(TEKS);
        return label;
    }

    private static JPanel bidang(String teks, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel judul = label(teks, 12, Font.BOLD);
        judul.setLabelFor(input instanceof JLayer<?> ? ((JLayer<?>) input).getView() : input);
        panel.add(judul, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel duaKolom(JComponent kiri, JComponent kanan) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.add(kiri);
        panel.add(kanan);
        return panel;
    }

    private static JPanel identitas(String teks) {
        return input(new JTextField(teks), false);
    }

    private static JPanel input(JTextField teks, boolean bisaDiedit) {
        teks.setUI(new BasicTextFieldUI());
        teks.setEditable(bisaDiedit);
        teks.setOpaque(false);
        teks.setBorder(BorderFactory.createEmptyBorder());
        teks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        teks.setForeground(TEKS);
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                bingkai(g, getWidth(), getHeight(), teks.isEditable() ? Color.WHITE : LATAR,
                        teks.isFocusOwner() ? BIRU : GARIS);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));
        panel.add(teks);
        teks.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                panel.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                panel.repaint();
            }
        });
        return panel;
    }

    private static JButton tombol(String teks, boolean utama) {
        JButton tombol = new JButton(teks) {
            @Override protected void paintComponent(Graphics g) {
                boolean aktif = isEnabled();
                Color isi = utama && aktif ? BIRU : aktif ? Color.WHITE : LATAR;
                bingkai(g, getWidth(), getHeight(), isi, aktif && (utama || isFocusOwner()) ? BIRU : GARIS);
                super.paintComponent(g);
            }
        };
        tombol.setUI(new BasicButtonUI());
        tombol.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tombol.setForeground(utama ? Color.WHITE : BIRU);
        tombol.setOpaque(false);
        tombol.setContentAreaFilled(false);
        tombol.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        tombol.setFocusPainted(false);
        return tombol;
    }

    private static void bingkai(Graphics g, int lebar, int tinggi, Color isi, Color garis) {
        Graphics2D gambar = (Graphics2D) g.create();
        gambar.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gambar.setColor(isi);
        gambar.fillRoundRect(0, 0, lebar, tinggi, 9, 9);
        gambar.setColor(garis);
        gambar.drawRoundRect(0, 0, lebar - 1, tinggi - 1, 9, 9);
        gambar.dispose();
    }

    private static final class Referensi {
        final JTextField kode = new JTextField(9);
        final JTextField nama = new JTextField();
        final JButton pilih = tombol("Pilih", false);
        final JPanel panel = new JPanel(new BorderLayout(7, 0));

        Referensi() {
            panel.setOpaque(false);
            panel.add(input(kode, false), BorderLayout.WEST);
            panel.add(input(nama, false), BorderLayout.CENTER);
            panel.add(pilih, BorderLayout.EAST);
        }

        void isi(String kode, String nama) {
            this.kode.setText(Objects.toString(kode, "").trim());
            this.nama.setText(Objects.toString(nama, "").trim());
            this.nama.setCaretPosition(0);
        }

        boolean kosong() {
            return kode.getText().isEmpty() || nama.getText().isEmpty();
        }
    }
}
