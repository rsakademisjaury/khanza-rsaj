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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
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
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import widget.Tanggal;

public final class DlgRujukBalik extends JDialog {
    private static final String RAWAT_INAP = "1. Rawat Inap";
    private static final String RAWAT_JALAN = "2. Rawat Jalan";
    private static final String JUDUL = "Surat Rujuk Balik BPJS";
    private static final String FORMAT_TANGGAL = "dd-MM-yyyy";
    private static final DateTimeFormatter PEMBACA_TANGGAL =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Color BIRU = new Color(29, 96, 202);
    private static final Color TEKS = new Color(28, 47, 73);
    private static final Color GARIS = new Color(187, 202, 220);
    private static final Color LATAR = new Color(243, 247, 252);

    private final VClaimRujukBalik.Sep sep;
    private final Tanggal tanggalRujukan = tanggal("Tanggal Rujukan");
    private final Tanggal rencanaKunjungan = tanggal("Rencana Kunjungan");
    private final JComboBox<Tipe> tipeRujukan = new JComboBox<>(Tipe.values());
    private final JComboBox<String> jenisPelayanan = new JComboBox<>(new String[]{RAWAT_JALAN});
    private final JTextField catatan = new JTextField();
    private final Referensi ppk = new Referensi();
    private final JButton cekFktp = tombol("Cek FKTP", false);
    private final Referensi diagnosa = new Referensi();
    private final Referensi poli = new Referensi();
    private final JButton simpan = tombol("Simpan", true);
    private final JButton cetak = tombol("Cetak", false);
    private final JButton tutup = tombol("Tutup", false);
    private final JLabel status = label("", 12, Font.PLAIN);
    private final Map<Tipe, List<String>> riwayat;
    private final JPanel bidangKunjungan = bidang("Rencana Kunjungan", rencanaKunjungan);
    private final JPanel bidangPoli = bidang("Poli Tujuan", poli.panel);
    private boolean perluCekVClaim;
    private Faskes fktp;
    private boolean sedangCekFktp;
    private String pesanFktp = "";
    private String kodePpkManual = "", namaPpkManual = "";
    private String pelayananManual = RAWAT_JALAN;

    public static void buka(Component induk, String noRawat) {
        if (!akses.getbpjs_rujukan_keluar()) {
            return;
        }
        if (noRawat == null || noRawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(induk, "Silakan pilih kunjungan pasien terlebih dahulu.");
            return;
        }

        try {
            List<VClaimRujukBalik.Sep> daftar = VClaimRujukBalik.cariSep(noRawat.trim(), null);
            if (daftar.isEmpty()) {
                JOptionPane.showMessageDialog(induk, "Belum ada SEP BPJS untuk kunjungan ini.");
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
            dialog.setLocationRelativeTo(induk);
            dialog.setVisible(true);
            dialog.cekFktpJikaDiperlukan();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(induk, "Data SEP atau riwayat rujukan tidak dapat dibaca. Periksa koneksi database.");
        }
    }

    private DlgRujukBalik(Window pemilik, VClaimRujukBalik.Sep sep, Map<Tipe, List<String>> riwayat) {
        super(pemilik, JUDUL, ModalityType.MODELESS);
        this.sep = sep;
        this.riwayat = riwayat;

        diagnosa.isi(sep.kodeDiagnosa, sep.namaDiagnosa);
        tipeRujukan.setSelectedItem(Tipe.RUJUK_BALIK);
        catatan.setName("Catatan");
        catatan.setDocument(new batasInput((byte) 50).getKata(catatan));
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

        for (JComboBox<?> pilihan : List.of(tipeRujukan, jenisPelayanan)) {
            pilihan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pilihan.setForeground(TEKS);
            pilihan.setPreferredSize(new Dimension(180, 32));
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        baris(form, duaKolom(bidang("No. Rawat", identitas(sep.noRawat)), bidang("No. SEP", identitas(sep.nomor))));
        baris(form, duaKolom(bidang("Pasien / No. RM", identitas(sep.namaPasien + " / " + sep.noRm)),
                bidang("Tipe Rujukan", tipeRujukan)));
        JPanel tempatKunjungan = new JPanel(new BorderLayout());
        tempatKunjungan.setOpaque(false);
        tempatKunjungan.add(bidangKunjungan);
        baris(form, duaKolom(bidang("Tanggal Rujukan", tanggalRujukan), tempatKunjungan));
        JPanel tindakanPpk = new JPanel(new BorderLayout());
        tindakanPpk.setOpaque(false);
        tindakanPpk.add(ppk.pilih, BorderLayout.WEST);
        tindakanPpk.add(cekFktp, BorderLayout.EAST);
        ppk.panel.add(tindakanPpk, BorderLayout.EAST);
        baris(form, bidang("PPK Rujukan", ppk.panel));
        baris(form, bidang("Diagnosa Rujuk", diagnosa.panel));
        baris(form, duaKolom(bidang("Jenis Pelayanan", jenisPelayanan), bidang("Catatan", input(catatan, true))));
        baris(form, bidangPoli);
        tipeRujukan.addActionListener(e -> {
            if (tipeTerpilih() == Tipe.RUJUK_BALIK) {
                if (jenisPelayanan.getItemCount() > 1) {
                    kodePpkManual = ppk.kode.getText();
                    namaPpkManual = ppk.nama.getText();
                    pelayananManual = (String) jenisPelayanan.getSelectedItem();
                    jenisPelayanan.setModel(new DefaultComboBoxModel<>(new String[]{RAWAT_JALAN}));
                    isiPpkDariFktp();
                }
            } else if (jenisPelayanan.getItemCount() == 1) {
                jenisPelayanan.setModel(new DefaultComboBoxModel<>(new String[]{RAWAT_INAP, RAWAT_JALAN}));
                jenisPelayanan.setSelectedItem(pelayananManual);
                ppk.isi(kodePpkManual, namaPpkManual);
            }
            tampilkanIsianPelayanan();
            tampilkanStatus();
            cekFktpJikaDiperlukan();
        });
        jenisPelayanan.addActionListener(e -> tampilkanIsianPelayanan());
        tampilkanIsianPelayanan();
        isi.add(form, BorderLayout.CENTER);

        simpan.addActionListener(e -> simpan());
        cetak.addActionListener(e -> cetak());
        tutup.addActionListener(e -> dispose());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(cetak);
        tombol.add(tutup);
        JPanel tindakan = new JPanel(new BorderLayout(8, 0));
        tindakan.setOpaque(false);
        tindakan.add(status, BorderLayout.CENTER);
        tindakan.add(tombol, BorderLayout.EAST);
        isi.add(tindakan, BorderLayout.SOUTH);

        tampilkanStatus();
        setContentPane(isi);
        pack();
        setMinimumSize(new Dimension(640, 0));
        setSize(Math.max(760, getWidth()), getHeight());
    }

    private static void baris(JPanel form, JPanel baris) {
        baris.setBorder(BorderFactory.createEmptyBorder(0, 0, 9, 0));
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, baris.getPreferredSize().height));
        form.add(baris);
    }

    private Tipe tipeTerpilih() {
        return (Tipe) tipeRujukan.getSelectedItem();
    }

    private String kodePelayanan() {
        return RAWAT_INAP.equals(jenisPelayanan.getSelectedItem()) ? "1" : "2";
    }

    private void tampilkanIsianPelayanan() {
        Tipe tipe = tipeTerpilih();
        String pelayanan = kodePelayanan();
        bidangKunjungan.setVisible(tipe.tampilKunjungan(pelayanan));
        bidangPoli.setVisible(tipe.tampilPoli(pelayanan));
        if (isDisplayable()) {
            int lebar = getWidth();
            pack();
            setSize(Math.max(lebar, getWidth()), getHeight());
        }
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
        JDialog dialog = new JDialog(this, judul, ModalityType.DOCUMENT_MODAL);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        // Gunakan dokumen pencarian dan model tabel dari dialog referensi BPJS.
        JTextField kataKunci = new JTextField(inputSumber.getDocument(), "", 25);
        kataKunci.getAccessibleContext().setAccessibleName("Kode atau nama");
        JTable tabel = tabelReferensi(tabelSumber);
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
                String kode = Objects.toString(tabel.getValueAt(baris, 1), "").trim();
                String nama = Objects.toString(tabel.getValueAt(baris, 2), "").trim();
                if (!kode.isEmpty() && !nama.isEmpty()) {
                    referensi.isi(kode, nama);
                    dialog.dispose();
                }
            }
        };
        pilih.addActionListener(e -> pilihBaris.run());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            int baris = tabel.getSelectedRow();
            pilih.setEnabled(baris >= 0 && !Objects.toString(tabel.getValueAt(baris, 2), "").trim().isEmpty());
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

        JButton cetakDaftar = tombol("Cetak", false);
        cetakDaftar.setMnemonic('T');
        cetakDaftar.addActionListener(e -> cetakSumber.doClick(0));
        JButton keluar = tombol("Keluar", false);
        keluar.setMnemonic('K');
        keluar.addActionListener(e -> dialog.dispose());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(pilih);
        tombol.add(keluar);
        JPanel tindakan = new JPanel(new BorderLayout(8, 0));
        tindakan.setOpaque(false);
        tindakan.add(cetakDaftar, BorderLayout.WEST);
        tindakan.add(tombol, BorderLayout.EAST);

        JPanel isi = panelDialog(judul);
        isi.add(daftar, BorderLayout.CENTER);
        isi.add(tindakan, BorderLayout.SOUTH);
        dialog.setContentPane(isi);
        pasangTombol(dialog.getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW,
                KeyEvent.VK_ESCAPE, "keluar", dialog::dispose);
        ComponentAdapter ikutiInduk = new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e) {
                dialog.setBounds(getBounds());
            }

            @Override public void componentResized(ComponentEvent e) {
                dialog.setBounds(getBounds());
            }
        };
        addComponentListener(ikutiInduk);
        dialog.setBounds(getBounds());
        SwingUtilities.invokeLater(kataKunci::requestFocusInWindow);
        try {
            dialog.setVisible(true);
        } finally {
            removeComponentListener(ikutiInduk);
            dialog.dispose();
            sumber.dispose();
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
        tabel.getColumnModel().getColumn(0).setMaxWidth(54);
        tabel.getColumnModel().getColumn(1).setMinWidth(138);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(145);
        tabel.getColumnModel().getColumn(1).setMaxWidth(170);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(460);

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
                if (isDisplayable() && tipeTerpilih() == Tipe.RUJUK_BALIK) {
                    isiPpkDariFktp();
                    tampilkanStatus();
                }
            }
        }.execute();
    }

    private void isiPpkDariFktp() {
        ppk.isi(fktp == null ? "" : fktp.kode, fktp == null ? "" : fktp.nama);
    }

    private void simpan() {
        if (!akses.getbpjs_rujukan_keluar() || !simpan.isEnabled()) {
            return;
        }
        if (sep.namaPasien.trim().isEmpty() || sep.kartu.trim().isEmpty() || sep.kodePpkPelayanan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih SEP dengan data pasien, kartu, dan PPK pelayanan yang lengkap.");
            return;
        }
        String isiCatatan = catatan.getText().trim();
        if (ppk.kosong() || diagnosa.kosong() || isiCatatan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi PPK rujukan, diagnosa rujuk, dan catatan.");
            return;
        }

        if (isiCatatan.length() > 50) {
            JOptionPane.showMessageDialog(this, "Catatan maksimal 50 karakter.");
            return;
        }

        Tipe tipe = tipeTerpilih();
        String pelayanan = kodePelayanan();
        boolean adaRencana = tipe.tampilKunjungan(pelayanan);
        Date tanggal;
        Date kunjungan;
        try {
            tanggal = bacaTanggal(tanggalRujukan);
            kunjungan = adaRencana ? bacaTanggal(rencanaKunjungan) : tanggal;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, adaRencana
                    ? "Isi tanggal rujukan dan rencana kunjungan dengan benar."
                    : "Isi tanggal rujukan dengan benar.");
            return;
        }

        String link = Objects.toString(koneksiDB.URLAPIBPJS(), "").trim();
        if (link.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alamat layanan BPJS VClaim belum tersedia.");
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
        setFormAktif(false);
        tipeRujukan.setEnabled(false);
        simpan.setEnabled(false);
        tutup.setEnabled(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        status.setText("Mengirim rujukan ke BPJS…");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return VClaimRujukBalik.kirim(sep, data, link, new ApiBPJS());
            }

            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                tutup.setEnabled(true);
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

    private void tanganiGagalKirim(Tipe tipe, Throwable sebab) {
        if (sebab instanceof VClaimRujukBalik.SudahTerbit) {
            riwayat.put(tipe, ((VClaimRujukBalik.SudahTerbit) sebab).nomor);
            tampilkanStatus();
            JOptionPane.showMessageDialog(this, "Rujukan dengan tipe ini sudah terbit. Gunakan Cetak untuk mencetak ulang.");
        } else if (sebab instanceof VClaimRujukBalik.PeriksaVClaim) {
            statusTidakPasti(((VClaimRujukBalik.PeriksaVClaim) sebab).nomor);
        } else {
            tampilkanStatus();
            JOptionPane.showMessageDialog(this, sebab instanceof VClaimRujukBalik.Ditolak
                    ? "BPJS menolak rujukan. " + sebab.getMessage()
                    : "Rujukan gagal diproses. Periksa koneksi dan konfigurasi.");
        }
    }

    private void statusTidakPasti(String nomor) {
        perluCekVClaim = true;
        tampilkanStatus();
        String pesan = nomor.isEmpty() ? "Status pengiriman belum dapat dipastikan."
                : "BPJS memberi nomor " + nomor + ". Periksa penyimpanan lokal.";
        JOptionPane.showMessageDialog(this, pesan + "\nPeriksa VClaim sebelum mengirim ulang.");
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
        boolean aktif = !terbit && !perluCekVClaim;
        boolean balik = tipeTerpilih() == Tipe.RUJUK_BALIK;
        tipeRujukan.setEnabled(true);
        ppk.pilih.setVisible(!balik);
        cekFktp.setVisible(balik && !terbit && fktp == null);
        setFormAktif(aktif);
        simpan.setEnabled(aktif && (!balik || fktp != null));
        cetak.setEnabled(terbit);
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
        tanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tanggal.setForeground(TEKS);
        tanggal.setPreferredSize(new Dimension(130, 32));
        tanggal.setBorder(new AbstractBorder() {
            @Override public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(1, 1, 1, 1);
                return insets;
            }

            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Component fokus = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                boolean aktif = fokus == c || fokus != null && SwingUtilities.isDescendingFrom(fokus, c);
                Graphics2D garis = (Graphics2D) g.create();
                garis.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                garis.setColor(aktif ? BIRU : GARIS);
                garis.drawRoundRect(x, y, w - 1, h - 1, 8, 8);
                garis.dispose();
            }
        });
        FocusAdapter segarkanBingkai = new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tanggal.repaint(); }
            @Override public void focusLost(FocusEvent e) { tanggal.repaint(); }
        };
        tanggal.addFocusListener(segarkanBingkai);
        if (tanggal.isEditable()) {
            tanggal.getEditor().getEditorComponent().addFocusListener(segarkanBingkai);
        }
        return tanggal;
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
        judul.setLabelFor(input);
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
