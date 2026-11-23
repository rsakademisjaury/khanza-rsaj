package rekammedis;

import bridging.BPJSPulangUI;
import com.toedter.calendar.JDateChooser;
import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import rekammedis.SoapCpptBerkasDigital.Dokter;
import rekammedis.SoapCpptBerkasDigital.Hasil;
import rekammedis.SoapCpptBerkasDigital.Kunjungan;

public final class DlgFilterSoapCppt extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Color BIRU = new Color(31, 94, 179);
    private static final Color GARIS = new Color(192, 213, 239);
    private static final Color KANVAS = new Color(245, 249, 255);
    private static final Color BIRU_MUDA = new Color(231, 241, 254);
    private static final Color TEKS = new Color(26, 45, 71);
    private static final Color REDUP = new Color(67, 89, 116);
    private static final Font SEGOE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SEGOE_SEMIBOLD = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    private static final Font SEMIBOLD = Font.DIALOG.equals(SEGOE_SEMIBOLD.getFamily())
            ? SEGOE.deriveFont(Font.BOLD) : SEGOE_SEMIBOLD;

    private final String noRawat;
    private final JComboBox<Dokter> dokter = new JComboBox<>();
    private final JDateChooser awal = new JDateChooser();
    private final JDateChooser akhir = new JDateChooser();
    private final JButton cari = tombol("Tampilkan", true);
    private final JButton unggah = tombol("Unggah ke Berkas Digital", true);
    private final JButton lihatBerkas = tombol("Lihat Berkas Digital", false);
    private final JButton tutup = tombol("Tutup", false);
    private final JButton ubahUkuran = tombol("Maksimalkan", false);
    private final JLabel pasien = label(" ", 16, true);
    private final JLabel identitas = label(" ", 13, false);
    private final JLabel status = label(" ", 13, false);
    private final TabelCatatan pratinjau = new TabelCatatan();
    private Kunjungan kunjungan;
    private Hasil hasil;
    private boolean sibuk;
    private boolean sudahUnggah;
    private Rectangle ukuranSebelumMaksimal;

    public static JPanel toolbar(Window pemilik, Supplier<String> noRawat, Runnable bukaBerkas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panel.setOpaque(false);
        JButton tombol = tombol("Filter & Unggah SOAP/CPPT", false);
        tombol.addActionListener(e -> buka(pemilik, noRawat.get(), bukaBerkas));
        panel.add(tombol);
        return panel;
    }

    public static void buka(Window pemilik, String noRawat, Runnable bukaBerkas) {
        if (!akses.getberkas_digital_perawatan()) {
            JOptionPane.showMessageDialog(pemilik, "Anda belum memiliki akses Berkas Digital Perawatan.");
        } else if (noRawat == null || noRawat.isBlank()) {
            JOptionPane.showMessageDialog(pemilik, "Pilih kunjungan pasien terlebih dahulu.");
        } else {
            DlgFilterSoapCppt dialog = new DlgFilterSoapCppt(pemilik, noRawat.trim(), bukaBerkas);
            dialog.muatKunjungan();
            dialog.setVisible(true);
        }
    }

    private DlgFilterSoapCppt(Window pemilik, String noRawat, Runnable bukaBerkas) {
        super(pemilik, "Filter & Unggah SOAP/CPPT", ModalityType.DOCUMENT_MODAL);
        this.noRawat = noRawat;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel isi = new JPanel(new BorderLayout(0, 13));
        isi.setBackground(KANVAS);
        isi.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        setContentPane(isi);
        isi.add(bagianAtas(), BorderLayout.PAGE_START);
        pratinjau.getAccessibleContext().setAccessibleName("Tabel hasil filter SOAP/CPPT");
        JScrollPane gulir = new JScrollPane(pratinjau);
        gulir.setBorder(new BPJSPulangUI.Bingkai(GARIS, 1f, 12));
        gulir.setBackground(Color.WHITE);
        gulir.getViewport().setBackground(Color.WHITE);
        isi.add(gulir, BorderLayout.CENTER);

        cari.addActionListener(e -> cariCatatan());
        unggah.addActionListener(e -> unggahCatatan());
        tutup.addActionListener(e -> dispose());
        lihatBerkas.addActionListener(e -> {
            if (akses.getberkas_digital_perawatan()) {
                dispose();
                bukaBerkas.run();
            }
        });
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        tombol.setOpaque(false);
        tombol.add(lihatBerkas);
        tombol.add(unggah);
        tombol.add(tutup);
        status.setForeground(REDUP);
        JPanel bagianBawah = new JPanel(new BorderLayout(10, 0));
        bagianBawah.setOpaque(false);
        bagianBawah.add(status, BorderLayout.CENTER);
        bagianBawah.add(tombol, BorderLayout.LINE_END);
        isi.add(bagianBawah, BorderLayout.PAGE_END);

        dokter.addActionListener(e -> filterBerubah());
        awal.addPropertyChangeListener("date", e -> filterBerubah());
        akhir.addPropertyChangeListener("date", e -> filterBerubah());
        getRootPane().setDefaultButton(cari);
        GraphicsConfiguration monitor = pemilik != null ? pemilik.getGraphicsConfiguration() : getGraphicsConfiguration();
        Rectangle area = areaKerja(monitor);
        int tinggiMinimum = Math.min(500, area.height);
        int tinggiForm = Math.max(tinggiMinimum, Math.min(800, area.height * 9 / 10));
        setMinimumSize(new Dimension(Math.min(760, area.width), tinggiMinimum));
        setBounds(area.x, area.y + (area.height - tinggiForm) / 2, area.width, tinggiForm);
    }

    private static Rectangle areaKerja(GraphicsConfiguration monitor) {
        Rectangle layar = monitor.getBounds();
        Insets sela = Toolkit.getDefaultToolkit().getScreenInsets(monitor);
        return new Rectangle(layar.x + sela.left, layar.y + sela.top,
                layar.width - sela.left - sela.right, layar.height - sela.top - sela.bottom);
    }

    private void gantiUkuran() {
        if (ukuranSebelumMaksimal == null) {
            ukuranSebelumMaksimal = getBounds();
            setBounds(areaKerja(getGraphicsConfiguration()));
            ubahUkuran.setText("Pulihkan ukuran");
        } else {
            setBounds(ukuranSebelumMaksimal);
            ukuranSebelumMaksimal = null;
            ubahUkuran.setText("Maksimalkan");
        }
    }

    private JPanel bagianAtas() {
        JPanel bagian = new JPanel(new BorderLayout(0, 13));
        bagian.setOpaque(false);
        JLabel judul = label("Filter & Unggah SOAP/CPPT", 23, true);
        JPanel kepala = new JPanel(new BorderLayout(10, 0));
        kepala.setOpaque(false);
        kepala.add(judul, BorderLayout.CENTER);
        ubahUkuran.addActionListener(e -> gantiUkuran());
        kepala.add(ubahUkuran, BorderLayout.LINE_END);
        JPanel identitasPasien = new JPanel(new BorderLayout(0, 2));
        identitasPasien.setOpaque(false);
        identitas.setText("No. Rawat " + noRawat);
        identitas.setForeground(REDUP);
        identitasPasien.add(pasien, BorderLayout.PAGE_START);
        identitasPasien.add(identitas, BorderLayout.PAGE_END);

        dokter.setUI(new BPJSPulangUI.ComboStatus() {
            @Override
            public void paintCurrentValue(Graphics grafis, Rectangle area, boolean fokus) {
                Component nilai = comboBox.getRenderer().getListCellRendererComponent(
                        listBox, comboBox.getSelectedItem(), -1, false, false);
                if (!comboBox.isEnabled()) {
                    nilai.setForeground(REDUP);
                }
                currentValuePane.paintComponent(grafis, nilai, comboBox, area.x, area.y, area.width, area.height, true);
            }
        });
        dokter.setBorder(BorderFactory.createEmptyBorder());
        dokter.setOpaque(false);
        dokter.setBackground(Color.WHITE);
        dokter.setForeground(TEKS);
        dokter.setFont(SEGOE);
        dokter.setToolTipText("Dokter DPJP pada kunjungan ini");
        dokter.setPreferredSize(new Dimension(290, 32));
        dokter.setRenderer((daftar, nilai, index, dipilih, fokus) -> {
            JLabel item = label(nilai == null ? "Pilih dokter DPJP" : nilai.toString(), 13, false);
            item.setForeground(dipilih && index >= 0 ? BIRU : nilai == null ? REDUP : TEKS);
            item.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
            item.setOpaque(index >= 0);
            item.setBackground(dipilih ? BIRU_MUDA : Color.WHITE);
            return item;
        });
        BPJSPulangUI.KotakInput kotakDokter = new BPJSPulangUI.KotakInput();
        kotakDokter.add(dokter, BorderLayout.CENTER);
        gayaTanggal(awal);
        gayaTanggal(akhir);
        JPanel filter = new JPanel(new GridBagLayout());
        filter.setOpaque(false);
        GridBagConstraints kolom = new GridBagConstraints();
        kolom.anchor = GridBagConstraints.SOUTHWEST;
        kolom.fill = GridBagConstraints.HORIZONTAL;
        kolom.insets = new Insets(0, 0, 0, 10);
        kolom.weightx = 1;
        filter.add(berlabel("Dokter DPJP", kotakDokter, dokter), kolom);
        kolom.weightx = 0;
        filter.add(berlabel("Tanggal mulai", awal, awal), kolom);
        filter.add(berlabel("Tanggal akhir", akhir, akhir), kolom);
        kolom.insets = new Insets(0, 0, 0, 0);
        filter.add(cari, kolom);

        JPanel kartu = new JPanel(new BorderLayout(0, 14));
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createCompoundBorder(new BPJSPulangUI.Bingkai(GARIS, 1f, 13),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        kartu.add(identitasPasien, BorderLayout.PAGE_START);
        kartu.add(filter, BorderLayout.CENTER);
        bagian.add(kepala, BorderLayout.PAGE_START);
        bagian.add(kartu, BorderLayout.CENTER);
        return bagian;
    }

    private static JPanel berlabel(String teks, javax.swing.JComponent kontrol, Component target) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = label(teks, 12, true);
        label.setLabelFor(target);
        panel.add(label, BorderLayout.PAGE_START);
        panel.add(kontrol, BorderLayout.CENTER);
        return panel;
    }

    private static void gayaTanggal(JDateChooser tanggal) {
        tanggal.setDateFormatString("dd-MM-yyyy");
        tanggal.setPreferredSize(new Dimension(130, 34));
        tanggal.setOpaque(false);
        tanggal.getDateEditor().getUiComponent().setFont(SEGOE);
        tanggal.getDateEditor().getUiComponent().setForeground(TEKS);
        tanggal.getDateEditor().getUiComponent().setOpaque(false);
        tanggal.setBorder(BorderFactory.createCompoundBorder(new BPJSPulangUI.Bingkai(GARIS, 1f, 12),
                BorderFactory.createEmptyBorder(1, 8, 1, 4)));
        tanggal.getDateEditor().getUiComponent().setBorder(BorderFactory.createEmptyBorder());
        tanggal.getCalendarButton().setIcon(BPJSPulangUI.ikon("calendar.png"));
        tanggal.getCalendarButton().setContentAreaFilled(false);
        tanggal.getCalendarButton().setOpaque(false);
        tanggal.getCalendarButton().setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    }

    private static JButton tombol(String teks, boolean utama) {
        BPJSPulangUI.Tombol tombol = new BPJSPulangUI.Tombol();
        tombol.setText(teks);
        tombol.setUI(new BasicButtonUI());
        tombol.setFont(SEMIBOLD);
        tombol.setForeground(utama ? Color.WHITE : BIRU);
        tombol.setBackground(utama ? BIRU : Color.WHITE);
        tombol.setBorderPainted(!utama);
        tombol.setBorder(BorderFactory.createCompoundBorder(new BPJSPulangUI.Bingkai(GARIS, 1f, 10),
                BorderFactory.createEmptyBorder(6, 11, 6, 11)));
        return tombol;
    }

    private static JLabel label(String teks, int ukuran, boolean tebal) {
        JLabel label = new JLabel(teks);
        label.setForeground(TEKS);
        label.setFont((tebal ? SEMIBOLD : SEGOE).deriveFont((float) ukuran));
        return label;
    }

    private void muatKunjungan() {
        jalankan("Memuat daftar dokter DPJP…", () -> {
            try (Connection koneksi = koneksiDB.condbTransaksi()) {
                return SoapCpptBerkasDigital.bacaKunjungan(koneksi, noRawat);
            }
        }, data -> {
            pasien.setText(data.pasien);
            identitas.setText("No. RM " + data.noRm + "  ·  No. Rawat " + data.noRawat);
            ZoneId zona = ZoneId.systemDefault();
            LocalDate hariIni = LocalDate.now(zona);
            LocalDate batasAkhir = hariIni.isBefore(data.tanggalMasuk) ? data.tanggalMasuk : hariIni;
            awal.setDate(Date.from(data.tanggalMasuk.atStartOfDay(zona).toInstant()));
            akhir.setDate(Date.from(batasAkhir.atStartOfDay(zona).toInstant()));
            data.dokter.forEach(dokter::addItem);
            dokter.setSelectedIndex(data.dokter.size() == 1 ? 0 : -1);
            kunjungan = data;
            filterBerubah();
        }, "Daftar dokter DPJP gagal dimuat.");
    }

    private void filterBerubah() {
        hasil = null;
        pratinjau.isi(List.of());
        if (kunjungan != null) {
            status.setText(kunjungan.dokter.isEmpty() ? "Belum ada dokter DPJP pada kunjungan ini."
                    : "Pilih dokter dan rentang tanggal, lalu klik Tampilkan.");
        }
        aturKontrol();
    }

    private void aturKontrol() {
        boolean siap = !sibuk && kunjungan != null && !kunjungan.dokter.isEmpty();
        dokter.setEnabled(siap);
        awal.setEnabled(siap);
        akhir.setEnabled(siap);
        cari.setEnabled(siap && dokter.getSelectedItem() != null);
        unggah.setEnabled(siap && hasil != null && !hasil.catatan.isEmpty());
        lihatBerkas.setVisible(sudahUnggah);
        lihatBerkas.setEnabled(!sibuk);
        tutup.setEnabled(!sibuk);
        setDefaultCloseOperation(sibuk ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
    }

    private void cariCatatan() {
        Dokter penulis = (Dokter) dokter.getSelectedItem();
        LocalDate dari = tanggal(awal);
        LocalDate sampai = tanggal(akhir);
        if (penulis == null || dari == null || sampai == null || sampai.isBefore(dari)) {
            filterBerubah();
            status.setText("Pilih dokter dan rentang tanggal yang valid.");
            return;
        }
        filterBerubah();
        jalankan("Mencari catatan SOAP/CPPT…", () -> {
            try (Connection koneksi = koneksiDB.condbTransaksi()) {
                return SoapCpptBerkasDigital.cari(koneksi, kunjungan, penulis, dari, sampai);
            }
        }, data -> {
            hasil = data;
            if (data.catatan.isEmpty()) {
                status.setText("Tidak ada catatan dari dokter tersebut pada rentang tanggal ini.");
            } else {
                pratinjau.isi(data.catatan);
                status.setText(data.catatan.size() + " catatan siap diunggah.");
            }
        }, "Pencarian catatan SOAP/CPPT gagal.");
    }

    private void unggahCatatan() {
        Hasil terpilih = hasil;
        if (terpilih == null || terpilih.catatan.isEmpty()) {
            return;
        }
        if (!akses.getberkas_digital_perawatan()) {
            status.setText("Anda belum memiliki akses Berkas Digital Perawatan.");
            return;
        }
        if (terpilih.penulis != dokter.getSelectedItem() || !terpilih.awal.equals(tanggal(awal))
                || !terpilih.akhir.equals(tanggal(akhir))) {
            filterBerubah();
            return;
        }
        String rumahSakit = akses.getnamars();
        jalankan("Mengunggah halaman SOAP/CPPT…", () -> SoapCpptBerkasDigital.unggah(terpilih, rumahSakit), jumlah -> {
            hasil = null;
            sudahUnggah = true;
            status.setText(jumlah + " halaman tersimpan di Berkas Lain-lain (138).");
        }, "Unggah SOAP/CPPT gagal. Silakan coba lagi.");
    }

    private static LocalDate tanggal(JDateChooser pemilih) {
        Date nilai = pemilih.getDate();
        return nilai == null ? null : nilai.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static final class TabelCatatan extends JTable {
        private static final long serialVersionUID = 1L;
        private static final int[] LEBAR = {145, 150, 195, 195, 158, 158, 150, 135};
        private static final int MIN_TINGGI = 70;
        private final ModelCatatan model = new ModelCatatan();
        private final SelCatatan sel = new SelCatatan();
        private final javax.swing.Timer ukurUlang = new javax.swing.Timer(80, e -> ukurBaris());

        TabelCatatan() {
            setModel(model);
            setFont(SEGOE);
            setForeground(TEKS);
            setBackground(Color.WHITE);
            setRowHeight(MIN_TINGGI);
            setShowGrid(true);
            setGridColor(new Color(222, 231, 244));
            setSelectionBackground(BIRU_MUDA);
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            setFillsViewportHeight(true);
            setDefaultRenderer(Object.class, sel);
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setDefaultRenderer((table, nilai, terpilih, fokus, baris, kolom) -> {
                JLabel kepala = label(nilai.toString(), 12, true);
                kepala.setOpaque(true);
                kepala.setBackground(BIRU_MUDA);
                kepala.setForeground(BIRU.darker());
                kepala.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, GARIS),
                        BorderFactory.createEmptyBorder(10, 10, 10, 8)));
                return kepala;
            });
            for (int i = 0; i < LEBAR.length; i++) {
                getColumnModel().getColumn(i).setPreferredWidth(LEBAR[i]);
            }
            ukurUlang.setRepeats(false);
            getColumnModel().addColumnModelListener(new javax.swing.event.TableColumnModelListener() {
                @Override public void columnMarginChanged(javax.swing.event.ChangeEvent e) { ukurUlang.restart(); }
                @Override public void columnAdded(javax.swing.event.TableColumnModelEvent e) { }
                @Override public void columnRemoved(javax.swing.event.TableColumnModelEvent e) { }
                @Override public void columnMoved(javax.swing.event.TableColumnModelEvent e) { }
                @Override public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) { }
            });
        }

        void isi(List<List<String>> catatan) {
            model.baris = catatan;
            model.fireTableDataChanged();
            ukurBaris();
            scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        }

        private void ukurBaris() {
            for (int baris = 0; baris < getRowCount(); baris++) {
                int tinggi = MIN_TINGGI;
                for (int kolom = 0; kolom < getColumnCount(); kolom++) {
                    sel.getTableCellRendererComponent(this, getValueAt(baris, kolom), false, false, baris, kolom);
                    sel.setSize(Math.max(20, getColumnModel().getColumn(kolom).getWidth() - 1), Short.MAX_VALUE);
                    tinggi = Math.max(tinggi, sel.getPreferredSize().height + 2);
                }
                if (getRowHeight(baris) != tinggi) {
                    setRowHeight(baris, tinggi);
                }
            }
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle tampak, int orientasi, int arah) {
            return orientasi == SwingConstants.VERTICAL ? 24 : super.getScrollableUnitIncrement(tampak, orientasi, arah);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            if (!(getParent() instanceof JViewport)) {
                return false;
            }
            int lebarKolom = 0;
            for (int i = 0; i < getColumnCount(); i++) {
                lebarKolom += getColumnModel().getColumn(i).getPreferredWidth();
            }
            return lebarKolom <= getParent().getWidth();
        }
    }

    private static final class ModelCatatan extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private List<List<String>> baris = List.of();

        @Override public int getRowCount() { return baris.size(); }
        @Override public int getColumnCount() { return SoapCpptBerkasDigital.KOLOM.size(); }
        @Override public String getColumnName(int kolom) { return SoapCpptBerkasDigital.KOLOM.get(kolom); }
        @Override public Object getValueAt(int barisTabel, int kolom) { return baris.get(barisTabel).get(kolom); }
    }

    private static final class SelCatatan extends JTextArea implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        SelCatatan() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean dipilih,
                boolean fokus, int baris, int kolom) {
            setText(nilai.toString());
            setFont(kolom == 0 ? SEMIBOLD : SEGOE);
            setForeground(kolom == 0 ? BIRU.darker() : TEKS);
            setBackground(dipilih ? tabel.getSelectionBackground() : baris % 2 == 0 ? Color.WHITE : KANVAS);
            return this;
        }
    }

    private <T> void jalankan(String proses, Callable<T> pekerjaan, Consumer<T> selesai, String pesanGagal) {
        sibuk = true;
        status.setText(proses);
        aturKontrol();
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return pekerjaan.call();
            }

            @Override
            protected void done() {
                try {
                    selesai.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status.setText(pesanGagal);
                } catch (ExecutionException e) {
                    Throwable sebab = e.getCause();
                    status.setText(pesanGagal);
                    if (sebab instanceof SQLException) {
                        System.err.println("SOAP/CPPT: SQLState=" + ((SQLException) sebab).getSQLState());
                    }
                    String pesan = sebab instanceof IOException && sebab.getClass() == IOException.class
                            ? sebab.getMessage() : pesanGagal;
                    JOptionPane.showMessageDialog(DlgFilterSoapCppt.this, pesan, "SOAP/CPPT", JOptionPane.ERROR_MESSAGE);
                } finally {
                    sibuk = false;
                    aturKontrol();
                }
            }
        }.execute();
    }
}
