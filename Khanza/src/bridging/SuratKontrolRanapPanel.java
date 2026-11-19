package bridging;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.LayerUI;
import javax.swing.text.View;

final class SuratKontrolRanapPanel {
    private static final Color LATAR_LUAR = new Color(0xE8EDF3);
    private static final Color KANVAS = new Color(0xF4F7FB);
    private static final Color TEKS = new Color(0x1B2B40);
    private static final Color REDUP = new Color(0x506278);
    private static final Color TEKS_LABEL = new Color(0x465D76);
    private static final Color GARIS = new Color(0xD6E0EB);
    private static final Color GARIS_HALUS = new Color(0xE6ECF3);
    private static final Color BIRU = new Color(0x2356AD);
    private static final Color BIRU_MUDA = new Color(0xEDF3FD);
    private static final Color FOKUS = new Color(0x4E78CF);
    private static final Color MERAH = new Color(0x963F44);
    private static final Color GARIS_MERAH = new Color(0xE7CCD0);
    private static final Color HIJAU = new Color(0x1D6147);
    private static final Color HIJAU_MUDA = new Color(0xEDF7F1);
    private static final Color GARIS_HIJAU = new Color(0xCDE5D6);
    private static final int TINGGI_INPUT = 34;
    static final int TINGGI_FILTER = 30;
    private static final int RADIUS_INPUT = 12;
    private static final int JARAK_TEPI = 24;
    private static final int JARAK_KOLOM = 14;
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SEMIBOLD = fontSemibold();

    final JTextArea obat = new TeksMenyesuaikan();
    final JTextArea lab = new TeksMenyesuaikan();
    final JTextArea pemeriksaan = new TeksMenyesuaikan();
    final JButton ambilObat = tombolIkon("Ambil resep pulang", Ikon.OBAT);
    final JButton lihatJadwal = tombol("Jadwal dokter", false, Ikon.KALENDER);
    private final JLabel statusKuota = label("Pilih dokter dan poli untuk cek kuota.", 12, false);
    final JButton ambilLab = tombolIkon("Ambil hasil lab", Ikon.LAB);
    final JButton ambilRadiologi = tombolIkon("Ambil hasil radiologi", Ikon.RADIOLOGI);
    final JButton muatUlang = tombolIkon("Muat ulang formulir lepas rawat", Ikon.MUAT_ULANG);
    final JTabbedPane tab = new JTabbedPane();
    private final JLabel status = label(" ", 12, false);
    private final Permukaan barKuota = new Permukaan(new BorderLayout(7, 0), RADIUS_INPUT);

    SuratKontrolRanapPanel() {
        status.setForeground(REDUP);
        barKuota.add(statusKuota, BorderLayout.CENTER);
        barKuota.add(lihatJadwal, BorderLayout.EAST);
        lihatJadwal.setPreferredSize(new Dimension(lihatJadwal.getPreferredSize().width, 27));
        kuota(statusKuota.getText(), false);
        setSiap(false);
    }

    private static Font fontSemibold() {
        Font segoe = new Font("Segoe UI Semibold", Font.PLAIN, 13);
        return Font.DIALOG.equals(segoe.getFamily()) ? FONT.deriveFont(Font.BOLD) : segoe;
    }

    JComponent barKuota() {
        return barKuota;
    }

    JComponent buat(JComponent pasien, JComponent kontrol, JComponent riwayat,
            JComponent aksi, AbstractButton keluar, JLabel statusBpjs) {
        Permukaan root = new Permukaan(new BorderLayout(), 24);
        root.setBackground(KANVAS);
        root.setBorder(new BPJSPulangUI.Bingkai(GARIS, 1f, 24));

        JLabel judul = label("SKDP & Formulir Lepas Rawat", 21, true);
        judul.setOpaque(true);
        judul.setBackground(Color.WHITE);
        judul.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS_HALUS),
                BorderFactory.createEmptyBorder(0, JARAK_TEPI, 0, JARAK_TEPI)));
        judul.setPreferredSize(new Dimension(0, 47));
        root.add(judul, BorderLayout.NORTH);

        Kartu formulir = kartu("Formulir Lepas Rawat");
        formulir.tambah(area("Obat-obat lepas rawat", obat, ambilObat));
        formulir.tambah(area("Hasil laboratorium penting", lab, ambilLab));
        formulir.tambah(area("Hasil pemeriksaan lain-lain", pemeriksaan, ambilRadiologi));
        formulir.tambah(denganAksi(status, muatUlang));

        JPanel pasangan = new KolomDokumen(kontrol, formulir);
        JPanel dokumen = new KanvasDokumen();
        dokumen.setLayout(new BorderLayout(0, 11));
        dokumen.setBorder(BorderFactory.createEmptyBorder(10, JARAK_TEPI, 12, JARAK_TEPI));
        dokumen.add(pasien, BorderLayout.NORTH);
        dokumen.add(rataAtas(pasangan), BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(dokumen, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getViewport().setBackground(KANVAS);

        tab.setUI(new BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                tabInsets = new Insets(0, 3, 0, 3);
                tabAreaInsets = new Insets(0, JARAK_TEPI, 1, 0);
                contentBorderInsets = new Insets(0, 0, 0, 0);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
            }

            @Override protected int calculateTabHeight(int letak, int indeks, int tinggiFont) {
                return 36;
            }

            @Override protected void paintTabArea(Graphics g, int letak, int indeksAktif) {
                super.paintTabArea(g, letak, indeksAktif);
                int y = calculateTabAreaHeight(letak, runCount, maxTabHeight) - 1;
                g.setColor(GARIS);
                g.drawLine(0, y, tab.getWidth(), y);
            }

            @Override protected void paintTabBackground(Graphics g, int letak, int indeks,
                    int x, int y, int lebar, int tinggi, boolean aktif) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, lebar, tinggi);
                if (aktif) {
                    g.setColor(BIRU);
                    g.fillRect(x, y + tinggi - 2, lebar - 22, 2);
                }
            }

            @Override protected int calculateTabWidth(int letak, int indeks, java.awt.FontMetrics fm) {
                return super.calculateTabWidth(letak, indeks, fm) + 22;
            }

            @Override protected void layoutLabel(int letak, java.awt.FontMetrics fm, int indeks,
                    String teks, Icon ikon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect,
                    boolean aktif) {
                Rectangle area = new Rectangle(tabRect);
                area.width -= 22;
                super.layoutLabel(letak, fm, indeks, teks, ikon, area, iconRect, textRect, aktif);
            }

            @Override protected void paintTabBorder(Graphics g, int letak, int indeks,
                    int x, int y, int lebar, int tinggi, boolean aktif) {
                // Garis bawah menandai tab aktif.
            }

            @Override protected void paintText(Graphics g, int letak, Font font, java.awt.FontMetrics fm,
                    int indeks, String teks, Rectangle area, boolean aktif) {
                gambarTeks(tab, g, font, aktif ? BIRU : REDUP, teks, tab.getDisplayedMnemonicIndexAt(indeks),
                        area.x, area.y + fm.getAscent());
            }

            @Override protected void paintContentBorder(Graphics g, int letak, int indeks) { }
        });
        tab.setFont(FONT_SEMIBOLD);
        tab.setOpaque(true);
        tab.setBackground(Color.WHITE);
        tab.setForeground(TEKS);
        tab.addTab("Input dokumen", scroll);
        JPanel isiRiwayat = new JPanel(new BorderLayout());
        isiRiwayat.setBackground(KANVAS);
        isiRiwayat.setBorder(BorderFactory.createEmptyBorder(10, JARAK_TEPI, 12, JARAK_TEPI));
        isiRiwayat.add(riwayat);
        tab.addTab("Riwayat SKDP", isiRiwayat);
        root.add(tab, BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(12, 0));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, GARIS),
                BorderFactory.createEmptyBorder(7, JARAK_TEPI - 7, 7, JARAK_TEPI)));
        statusBpjs.setFont(FONT.deriveFont(12f));
        statusBpjs.setForeground(REDUP);
        statusBpjs.setHorizontalAlignment(SwingConstants.RIGHT);
        bawah.add(aksi, BorderLayout.WEST);
        bawah.add(statusBpjs, BorderLayout.CENTER);
        bawah.add(keluar, BorderLayout.EAST);
        root.add(bawah, BorderLayout.SOUTH);
        JPanel bingkaiLuar = new JPanel(null) {
            @Override public Dimension getPreferredSize() {
                Dimension ukuran = root.getPreferredSize();
                return new Dimension(Math.min(1450, ukuran.width) + 36, ukuran.height + 18);
            }

            @Override public void doLayout() {
                int margin = getWidth() < 900 ? 8 : 18;
                int lebar = Math.max(0, Math.min(1450, getWidth() - 2 * margin));
                root.setBounds((getWidth() - lebar) / 2, 9, lebar, Math.max(0, getHeight() - 18));
            }
        };
        bingkaiLuar.setBackground(LATAR_LUAR);
        bingkaiLuar.add(root);
        return bingkaiLuar;
    }

    private static JComponent rataAtas(JComponent isi) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(isi, BorderLayout.NORTH);
        return panel;
    }

    void isi(LepasRawatRanap.Isi isi) {
        obat.setText(isi.obat);
        lab.setText(isi.lab);
        pemeriksaan.setText(isi.pemeriksaan);
        obat.setCaretPosition(0);
        lab.setCaretPosition(0);
        pemeriksaan.setCaretPosition(0);
    }

    LepasRawatRanap.Isi baca() {
        return new LepasRawatRanap.Isi(obat.getText(), lab.getText(), pemeriksaan.getText());
    }

    void setSiap(boolean siap) {
        obat.setEditable(siap);
        lab.setEditable(siap);
        pemeriksaan.setEditable(siap);
        ambilObat.setEnabled(siap);
        ambilLab.setEnabled(siap);
        ambilRadiologi.setEnabled(siap);
    }

    void status(String teks, boolean error) {
        status.setText(teks);
        status.setForeground(error ? new Color(165, 39, 49) : REDUP);
        status.setToolTipText(teks);
    }

    void kuota(String teks, boolean peringatan) {
        statusKuota.setText(teks);
        statusKuota.setToolTipText(teks);
        boolean tersedia = !peringatan && teks.startsWith("Kuota ");
        statusKuota.setForeground(peringatan ? MERAH : tersedia ? HIJAU : REDUP);
        barKuota.setBackground(peringatan ? new Color(0xFFF3F4) : tersedia ? HIJAU_MUDA : KANVAS);
        Color garis = peringatan ? GARIS_MERAH : tersedia ? GARIS_HIJAU : GARIS;
        barKuota.setBorder(BorderFactory.createCompoundBorder(new BPJSPulangUI.Bingkai(garis, 1f, RADIUS_INPUT),
                BorderFactory.createEmptyBorder(2, 7, 2, 3)));
    }

    void tampilJadwal(Component induk, JadwalKontrolRanap.Hasil hasil, String tanggal) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Hari", "Jam praktik", "Kuota", "Sumber"}, 0) {
            @Override public boolean isCellEditable(int baris, int kolom) { return false; }
        };
        if (hasil.dariHfis) {
            for (JadwalKontrolRanap.Sesi sesi : hasil.jadwalTanggal) {
                model.addRow(new Object[]{sesi.hari, sesi.jam, sesi.libur ? "Libur" : sesi.kuota, "HFIS · " + tanggal});
            }
        }
        for (JadwalKontrolRanap.Sesi sesi : hasil.jadwalMingguan) {
            model.addRow(new Object[]{sesi.hari, sesi.jam, sesi.kuota, "Jadwal praktik SIMRS"});
        }

        Kartu konten = new Kartu(new BorderLayout(0, 8), new Insets(9, 10, 10, 10));
        String ringkas = tanggal + "   ·   " + (hasil.adaPraktik()
                ? "Kuota " + hasil.kuota + "   ·   Terisi " + hasil.terisi + "   ·   Sisa " + hasil.sisa()
                : "Tidak ada praktik");
        JLabel info = label(ringkas, 13, true);
        info.setForeground(hasil.sisa() == 0 ? MERAH : BIRU);
        konten.add(info, BorderLayout.NORTH);
        if (model.getRowCount() == 0) {
            konten.add(label("Jadwal dokter untuk poli tujuan belum tersedia.", 12, false), BorderLayout.CENTER);
        } else {
            JTable tabel = new JTable(model);
            rapikanTabel(tabel, 27);
            tabel.getColumnModel().getColumn(2).setPreferredWidth(62);
            tabel.getColumnModel().getColumn(3).setPreferredWidth(170);
            konten.add(gulirTabel(tabel), BorderLayout.CENTER);
        }
        JDialog dialog = dialog(induk, "Jadwal dokter", Ikon.KALENDER, konten, 650, 360);
        JButton tutup = tombol("Tutup", true, Ikon.KELUAR);
        tutup.addActionListener(e -> dialog.dispose());
        footerDialog(dialog, tutup);
        dialog.setVisible(true);
    }

    void pilihHasil(Component induk, String judul, List<String> hasil, JTextArea tujuan) {
        if (hasil.isEmpty()) {
            JOptionPane.showMessageDialog(induk, "Belum ada hasil untuk kunjungan ini.", judul, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Pilih", "Hasil pemeriksaan"}, 0) {
            @Override public Class<?> getColumnClass(int kolom) { return kolom == 0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int baris, int kolom) { return kolom == 0; }
        };
        for (String nilai : hasil) {
            model.addRow(new Object[]{Boolean.FALSE, nilai.replaceAll("\\s+", " ")});
        }
        JTable tabel = new JTable(model) {
            @Override public String getToolTipText(java.awt.event.MouseEvent e) {
                int baris = rowAtPoint(e.getPoint());
                return baris < 0 ? null : getValueAt(baris, 1).toString();
            }
        };
        rapikanTabel(tabel, 27);
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        tabel.getColumnModel().getColumn(0).setPreferredWidth(55);
        tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int baris = tabel.rowAtPoint(e.getPoint());
                if (baris >= 0 && tabel.columnAtPoint(e.getPoint()) == 1 && SwingUtilities.isLeftMouseButton(e)) {
                    model.setValueAt(!Boolean.TRUE.equals(model.getValueAt(baris, 0)), baris, 0);
                }
            }
        });

        JTextArea pratinjau = new JTextArea(4, 38);
        pratinjau.setFont(FONT);
        pratinjau.setForeground(TEKS);
        pratinjau.setBackground(KANVAS);
        pratinjau.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
        pratinjau.setEditable(false);
        pratinjau.setLineWrap(true);
        pratinjau.setWrapStyleWord(true);
        JScrollPane gulirPratinjau = new JScrollPane(pratinjau);
        gulirPratinjau.setBorder(BorderFactory.createLineBorder(GARIS));

        Kartu konten = new Kartu(new BorderLayout(0, 8), new Insets(9, 10, 10, 10));
        JLabel pilihan = label("Centang hasil yang ingin dimasukkan.", 12, false);
        pilihan.setForeground(REDUP);
        konten.add(pilihan, BorderLayout.NORTH);
        konten.add(gulirTabel(tabel), BorderLayout.CENTER);
        konten.add(ruas("Pratinjau hasil terpilih", gulirPratinjau), BorderLayout.SOUTH);

        JDialog dialog = dialog(induk, judul, Ikon.MEDIS, konten, 760, 475);
        JButton batal = tombol("Batal", false, Ikon.KELUAR);
        JButton tambah = tombol("Masukkan hasil", true, Ikon.SIMPAN);
        tambah.setEnabled(false);
        model.addTableModelListener(e -> {
            List<String> terpilih = hasilTerpilih(model, hasil);
            pratinjau.setText(String.join("\n\n", terpilih));
            pratinjau.setCaretPosition(0);
            tambah.setEnabled(!terpilih.isEmpty());
            pilihan.setText(terpilih.isEmpty() ? "Centang hasil yang ingin dimasukkan." : terpilih.size() + " hasil dipilih");
        });
        batal.addActionListener(e -> dialog.dispose());
        tambah.addActionListener(e -> {
            if (tabel.isEditing() && !tabel.getCellEditor().stopCellEditing()) {
                return;
            }
            List<String> terpilih = hasilTerpilih(model, hasil);
            if (!terpilih.isEmpty()) {
                tujuan.append((tujuan.getText().trim().isEmpty() ? "" : "\n\n") + String.join("\n\n", terpilih));
                dialog.dispose();
            }
        });
        footerDialog(dialog, batal, tambah);
        dialog.setVisible(true);
        tujuan.requestFocusInWindow();
    }

    private static List<String> hasilTerpilih(DefaultTableModel model, List<String> hasil) {
        List<String> terpilih = new ArrayList<>();
        for (int i = 0; i < hasil.size(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                terpilih.add(hasil.get(i));
            }
        }
        return terpilih;
    }

    private static JDialog dialog(Component induk, String judul, Ikon ikon, JComponent isi, int lebar, int tinggi) {
        Window pemilik = induk instanceof Window ? (Window) induk : SwingUtilities.getWindowAncestor(induk);
        JDialog dialog = new JDialog(pemilik, judul, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel dasar = new JPanel(new BorderLayout(0, 8));
        dasar.setBackground(KANVAS);
        dasar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GARIS),
                BorderFactory.createEmptyBorder(0, 0, 6, 0)));
        JLabel header = label(judul, 16, true);
        header.setIcon(ikon);
        header.setIconTextGap(9);
        header.setForeground(TEKS);
        header.setOpaque(true);
        header.setBackground(BIRU_MUDA);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        dasar.add(header, BorderLayout.NORTH);
        JPanel konten = new JPanel(new BorderLayout());
        konten.setOpaque(false);
        konten.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        konten.add(isi);
        dasar.add(konten, BorderLayout.CENTER);
        dialog.setContentPane(dasar);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        Rectangle area = induk.getGraphicsConfiguration() == null
                ? new Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize())
                : induk.getGraphicsConfiguration().getBounds();
        dialog.setSize(Math.min(lebar, area.width - 24), Math.min(tinggi, area.height - 48));
        dialog.setLocationRelativeTo(induk);
        return dialog;
    }

    private static void footerDialog(JDialog dialog, JButton... tombol) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 8));
        for (JButton aksi : tombol) {
            footer.add(aksi);
        }
        dialog.getContentPane().add(footer, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(tombol[tombol.length - 1]);
    }

    static JComponent riwayat(JScrollPane scroll, JTable tabel, JComponent tanggal, JComponent pencarian,
            JComponent aksi) {
        Kartu panel = new Kartu(new BorderLayout(0, 11), new Insets(10, 12, 10, 12));
        JPanel filter = new JPanel(new BorderLayout(0, 8));
        filter.setOpaque(false);
        rapatkanTombolRiwayat(pencarian);
        rapatkanTombolRiwayat(aksi);
        filter.add(tanggal, BorderLayout.CENTER);
        filter.add(pencarian, BorderLayout.SOUTH);
        panel.add(filter, BorderLayout.NORTH);
        rapikanTabel(tabel, 26);
        rapikanGulirTabel(scroll, tabel);
        int[] lebar = {155, 180, 135, 85, 185, 105, 44, 225, 105, 180, 105, 120, 215, 92, 155, 200};
        for (int i = 0; i < Math.min(lebar.length, tabel.getColumnCount()); i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        Permukaan bingkaiTabel = new Permukaan(new BorderLayout(), RADIUS_INPUT);
        bingkaiTabel.setBackground(Color.WHITE);
        bingkaiTabel.setBorder(new BPJSPulangUI.Bingkai(GARIS, 1f, RADIUS_INPUT));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        bingkaiTabel.add(scroll);
        panel.add(bingkaiTabel, BorderLayout.CENTER);
        panel.add(aksi, BorderLayout.SOUTH);
        return panel;
    }

    private static void rapatkanTombolRiwayat(JComponent baris) {
        for (Component item : baris.getComponents()) {
            if (item instanceof AbstractButton) {
                item.setPreferredSize(new Dimension(item.getPreferredSize().width, TINGGI_FILTER));
            }
        }
    }

    static JComponent filterTanggal(AbstractButton pilih, String nama, JComboBox<?> awal, JComboBox<?> akhir) {
        pilih.setText(nama);
        pilih.setForeground(TEKS);
        pilih.setFont(FONT_SEMIBOLD.deriveFont(12f));
        pilih.setOpaque(false);
        pilih.setContentAreaFilled(false);
        pilih.setBorderPainted(false);
        pilih.setPreferredSize(null);
        pilih.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 4));
        JPanel filter = barisAksi(pilih, tanggal(awal, TINGGI_FILTER), label("s.d.", 11, false),
                tanggal(akhir, TINGGI_FILTER));
        filter.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        return filter;
    }

    private static JScrollPane gulirTabel(JTable tabel) {
        JScrollPane scroll = new JScrollPane(tabel);
        rapikanGulirTabel(scroll, tabel);
        return scroll;
    }

    private static void rapikanGulirTabel(JScrollPane scroll, JTable tabel) {
        scroll.setColumnHeaderView(tabel.getTableHeader());
        scroll.setBorder(BorderFactory.createLineBorder(GARIS));
        scroll.getViewport().setBackground(Color.WHITE);
    }

    private static void rapikanTabel(JTable tabel, int tinggi) {
        tabel.setBackground(Color.WHITE);
        tabel.setForeground(TEKS);
        tabel.setRowHeight(tinggi);
        tabel.setFont(FONT.deriveFont(12f));
        tabel.setSelectionBackground(new Color(204, 223, 255));
        tabel.setSelectionForeground(BIRU);
        tabel.setShowVerticalLines(false);
        tabel.setShowHorizontalLines(true);
        tabel.setGridColor(new Color(222, 229, 240));
        tabel.setFillsViewportHeight(true);
        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                    boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                cell.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 5));
                cell.setToolTipText(value == null ? null : value.toString());
                if (!selected) {
                    cell.setForeground("Libur".equals(value) ? MERAH : TEKS);
                    cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(241, 246, 253));
                }
                return cell;
            }
        });
        JTableHeader header = tabel.getTableHeader();
        TableCellRenderer bawaan = header.getDefaultRenderer();
        header.setFont(FONT_SEMIBOLD.deriveFont(12f));
        header.setDefaultRenderer((t, value, sel, focus, row, col) -> {
            Component c = bawaan.getTableCellRendererComponent(t, value, sel, focus, row, col);
            c.setBackground(BIRU_MUDA);
            c.setForeground(TEKS);
            c.setFont(header.getFont());
            if (c instanceof JLabel) {
                JLabel judul = (JLabel) c;
                judul.setOpaque(true);
                judul.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 5));
                judul.setHorizontalAlignment(SwingConstants.LEFT);
            }
            return c;
        });
    }

    static Kartu kartu(String judul) {
        return new Kartu(judul);
    }

    static JComponent ruas(String nama, JComponent komponen, Icon... ikon) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = labelRuas(nama);
        label.setLabelFor(komponen instanceof JLayer<?> ? ((JLayer<?>) komponen).getView() : komponen);
        if (ikon.length > 0) {
            label.setIcon(ikon[0]);
        }
        label.setPreferredSize(new Dimension(label.getPreferredSize().width, 17));
        panel.add(label, BorderLayout.NORTH);
        panel.add(komponen, BorderLayout.CENTER);
        return panel;
    }

    static JComponent sejajar(JComponent... isian) {
        JPanel panel = new JPanel(new java.awt.GridLayout(1, isian.length, 10, 0));
        panel.setOpaque(false);
        for (JComponent komponen : isian) {
            panel.add(komponen);
        }
        return panel;
    }

    static JComponent barisNomor(JComponent sep, JComponent surat) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.add(sep, BorderLayout.WEST);
        panel.add(surat, BorderLayout.CENTER);
        return panel;
    }

    static JPanel denganAksi(JComponent tengah, JComponent aksi) {
        JPanel panel = new JPanel(new BorderLayout(7, 0));
        panel.setOpaque(false);
        panel.add(tengah, BorderLayout.CENTER);
        panel.add(aksi, BorderLayout.EAST);
        return panel;
    }

    static JComponent pilihan(JComponent kode, JComponent nama, AbstractButton tombol, String teks, Ikon ikon) {
        JPanel panel = new JPanel(new BorderLayout(7, 0));
        panel.setOpaque(false);
        panel.add(input(kode, 106, false), BorderLayout.WEST);
        panel.add(input(nama, 120, false), BorderLayout.CENTER);
        pasangIkon(aksi(tombol, teks), ikon, BIRU);
        tombol.setPreferredSize(new Dimension(132, TINGGI_INPUT));
        panel.add(tombol, BorderLayout.EAST);
        return panel;
    }

    static JPanel barisAksi(Component... isi) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        panel.setOpaque(false);
        for (Component komponen : isi) {
            panel.add(komponen);
        }
        return panel;
    }

    static JComponent input(JComponent komponen, int lebar, boolean editable) {
        return input(komponen, lebar, editable, TINGGI_INPUT);
    }

    static JComponent input(JComponent komponen, int lebar, boolean editable, int tinggi) {
        Kotak kotak = new Kotak(editable ? Color.WHITE : KANVAS, new Insets(2, 4, 2, 4));
        kotak.setPreferredSize(new Dimension(lebar, tinggi));
        kotak.setMinimumSize(new Dimension(45, tinggi));
        komponen.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        komponen.setOpaque(false);
        komponen.setBackground(kotak.isi);
        komponen.setFont(FONT.deriveFont(14f));
        komponen.setForeground(TEKS);
        if (komponen instanceof JTextField) {
            JTextField field = (JTextField) komponen;
            // Pakai tampilan teks Swing langsung agar clip dari widget lama tidak memotong karakter.
            kotak.add(new JLayer<>(field, new LayerUI<JTextField>() {
                @Override public void paint(Graphics g, JComponent c) {
                    gambarField(field, g);
                }
            }));
        } else {
            kotak.add(komponen);
        }
        kotak.pantau(komponen);
        return kotak;
    }

    static JComponent tanggal(JComboBox<?> tanggal) {
        return tanggal(tanggal, TINGGI_INPUT);
    }

    private static JComponent tanggal(JComboBox<?> tanggal, int tinggi) {
        tanggal.setFont(FONT.deriveFont(tinggi == TINGGI_FILTER ? 13f : 14f));
        tanggal.setForeground(TEKS);
        tanggal.setBackground(Color.WHITE);
        tanggal.setOpaque(false);
        tanggal.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 6));
        tanggal.setPreferredSize(new Dimension(135, tinggi));
        Component editor = tanggal.isEditable() ? tanggal.getEditor().getEditorComponent() : null;
        if (editor instanceof JTextField) {
            JTextField field = (JTextField) editor;
            field.setFont(tanggal.getFont());
            field.setForeground(TEKS);
            field.setOpaque(false);
            field.setBorder(BorderFactory.createEmptyBorder());
        }
        Image ikonPanah = bacaIkon("panah-tanggal");
        Icon normal = ikonTampilan(ikonPanah, Ikon.CHEVRON, TEKS, true);
        // Kontrol asli tetap menangani mouse, keyboard dan popup; layer hanya menggambar tampilan tertutup.
        JLayer<JComboBox<?>> layer = new JLayer<>(tanggal, new LayerUI<JComboBox<?>>() {
            @Override public void paint(Graphics g, JComponent c) {
                int lebar = c.getWidth();
                int tinggiKotak = c.getHeight();
                boolean aktif = tanggal.isEnabled();
                latarBulat(g, aktif ? Color.WHITE : KANVAS, lebar, tinggiKotak, RADIUS_INPUT);
                Component editorAktif = aktif && tanggal.isEditable() ? tanggal.getEditor().getEditorComponent() : null;
                if (editorAktif != null && editorAktif.getParent() != null) {
                    Rectangle posisi = SwingUtilities.convertRectangle(editorAktif.getParent(), editorAktif.getBounds(), tanggal);
                    int ruangTeks = Math.max(0, Math.min(posisi.width, lebar - 29 - posisi.x));
                    Graphics area = g.create(posisi.x, posisi.y, ruangTeks, posisi.height);
                    if (editorAktif instanceof JTextField) {
                        gambarField((JTextField) editorAktif, area);
                    } else {
                        editorAktif.paint(area);
                    }
                    area.dispose();
                } else {
                    Font font = tanggal.getFont();
                    java.awt.FontMetrics metrik = c.getFontMetrics(font);
                    gambarTeks(tanggal, g, font, aktif ? TEKS : REDUP,
                            Objects.toString(tanggal.getSelectedItem(), ""), -1, 10,
                            (tinggiKotak - metrik.getHeight()) / 2 + metrik.getAscent());
                }
                if (aktif) {
                    normal.paintIcon(c, g, lebar - 23, (tinggiKotak - normal.getIconHeight()) / 2);
                }
                bingkaiFokus(c, g, 0, 0, lebar, tinggiKotak);
            }
        });
        pantauFokus(tanggal, layer);
        tanggal.addItemListener(e -> layer.repaint());
        return layer;
    }

    static JComponent barisIdentitas(JComponent... data) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3) {
            @Override public Dimension preferredLayoutSize(java.awt.Container wadah) {
                Dimension natural = super.preferredLayoutSize(wadah);
                int batas = wadah.getWidth();
                if (batas <= 0 || natural.width <= batas) {
                    return natural;
                }
                int tinggi = getVgap();
                int lebarBaris = 0;
                int tinggiBaris = 0;
                for (Component komponen : wadah.getComponents()) {
                    Dimension ukuran = komponen.getPreferredSize();
                    if (lebarBaris > 0 && lebarBaris + ukuran.width > batas) {
                        tinggi += tinggiBaris + getVgap();
                        lebarBaris = 0;
                        tinggiBaris = 0;
                    }
                    lebarBaris += ukuran.width;
                    tinggiBaris = Math.max(tinggiBaris, ukuran.height);
                }
                return new Dimension(batas, tinggi + tinggiBaris + getVgap());
            }
        });
        panel.setOpaque(false);
        for (JComponent item : data) {
            panel.add(item);
        }
        panel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { panel.revalidate(); }
        });
        return panel;
    }

    static JComponent barisIdentitasPasien(JTextField diagnosa, JComponent... data) {
        JPanel baris = new JPanel(new BorderLayout());
        baris.setOpaque(false);
        baris.add(barisIdentitas(data), BorderLayout.CENTER);

        JComponent rincian = identitas("Diagnosis awal", diagnosa, null);
        rincian.setBorder(BorderFactory.createEmptyBorder());
        JPanel slotDiagnosa = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                int lebarIdentitas = 0;
                for (JComponent item : data) {
                    lebarIdentitas += item.getPreferredSize().width;
                }
                int tersedia = baris.getWidth();
                int lebar = tersedia <= 0 ? 140 : Math.min(tersedia, Math.max(140, tersedia - lebarIdentitas));
                return new Dimension(lebar, super.getPreferredSize().height);
            }
        };
        slotDiagnosa.setOpaque(false);
        slotDiagnosa.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        slotDiagnosa.add(rincian, BorderLayout.NORTH);
        baris.add(slotDiagnosa, BorderLayout.EAST);
        return baris;
    }

    static JComponent identitas(String keterangan, JTextField field, Icon ikon, JComponent... aksi) {
        JLabel kecil = label(keterangan, 12, false);
        kecil.setForeground(REDUP);
        JLabel nilai = label("", "Nama pasien".equals(keterangan) ? 16 : 14, true);
        nilai.putClientProperty("html.disable", Boolean.TRUE);
        Runnable isi = () -> {
            String teks = field.getText().trim();
            nilai.setText(teks.isEmpty() ? "—" : teks);
            nilai.setToolTipText(teks.isEmpty() ? null : teks);
        };
        isi.run();
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { isi.run(); }
            @Override public void removeUpdate(DocumentEvent e) { isi.run(); }
            @Override public void changedUpdate(DocumentEvent e) { isi.run(); }
        });

        JPanel isiNilai = new JPanel(new BorderLayout(4, 0));
        isiNilai.setOpaque(false);
        isiNilai.add(nilai, BorderLayout.CENTER);
        if (aksi.length != 0) {
            isiNilai.add(aksi[0], BorderLayout.EAST);
        }
        JPanel susun = new JPanel(new BorderLayout(0, 2));
        susun.setOpaque(false);
        susun.add(kecil, BorderLayout.NORTH);
        susun.add(isiNilai, BorderLayout.CENTER);
        JPanel item = new JPanel(new BorderLayout(9, 0));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 29));
        if (ikon != null) {
            item.add(new JLabel(ikon), BorderLayout.WEST);
        }
        item.add(susun, BorderLayout.CENTER);
        return item;
    }

    static Icon ikonKasir(String sumber, int ukuran, Ikon cadangan) {
        URL url = SuratKontrolRanapPanel.class.getResource("/picture/" + sumber);
        if (url == null) {
            return cadangan;
        }
        Image gambar = new ImageIcon(url).getImage().getScaledInstance(ukuran, ukuran, Image.SCALE_SMOOTH);
        return new ImageIcon(gambar);
    }

    static AbstractButton aksi(AbstractButton tombol, String teks) {
        return gayaTombol(tombol, teks, Color.WHITE, BIRU, GARIS);
    }

    static AbstractButton aksi(AbstractButton tombol, String teks, Ikon ikon) {
        return pasangIkon(aksi(tombol, teks), ikon, BIRU);
    }

    static AbstractButton aksiUtama(AbstractButton tombol, String teks) {
        return gayaTombol(tombol, teks, BIRU, Color.WHITE, BIRU);
    }

    static AbstractButton aksiUtama(AbstractButton tombol, String teks, Ikon ikon) {
        return pasangIkon(aksiUtama(tombol, teks), ikon, Color.WHITE);
    }

    static AbstractButton aksiBahaya(AbstractButton tombol, String teks) {
        return gayaTombol(tombol, teks, Color.WHITE, MERAH, GARIS_MERAH);
    }

    static AbstractButton aksiBahaya(AbstractButton tombol, String teks, Ikon ikon) {
        return pasangIkon(aksiBahaya(tombol, teks), ikon, MERAH);
    }

    static JButton tombol(String teks, boolean utama) {
        JButton tombol = new JButton();
        if (utama) {
            aksiUtama(tombol, teks);
        } else {
            aksi(tombol, teks);
        }
        return tombol;
    }

    static JButton tombol(String teks, boolean utama, Ikon ikon) {
        JButton tombol = tombol(teks, utama);
        pasangIkon(tombol, ikon, utama ? Color.WHITE : BIRU);
        return tombol;
    }

    static JButton tombolIkon(String nama, Ikon ikon) {
        JButton tombol = new JButton();
        gayaTombol(tombol, "", BIRU_MUDA, BIRU, BIRU_MUDA);
        tombol.setToolTipText(nama);
        pasangIkon(tombol, ikon, BIRU);
        tombol.getAccessibleContext().setAccessibleName(nama);
        tombol.setPreferredSize(new Dimension(32, 27));
        tombol.setBorder(BorderFactory.createEmptyBorder());
        return tombol;
    }

    private static AbstractButton pasangIkon(AbstractButton tombol, Ikon ikon, Color warna) {
        String teks = tombol.getText().isEmpty() ? tombol.getToolTipText() : tombol.getText();
        Image gambar = bacaIkon(teks.toLowerCase(Locale.ROOT).replace(' ', '-'));
        tombol.setIcon(ikonTampilan(gambar, ikon, warna, true));
        tombol.setDisabledIcon(ikonTampilan(gambar, ikon, REDUP, false));
        tombol.setPressedIcon(null);
        tombol.setRolloverIcon(null);
        tombol.setIconTextGap(6);
        return tombol;
    }

    private static Image bacaIkon(String nama) {
        String sumberIkon = "picture/skdp-ranap/" + nama + ".png";
        Path relatif = Path.of(sumberIkon);
        try {
            CodeSource sumber = SuratKontrolRanapPanel.class.getProtectionDomain().getCodeSource();
            URL url = sumber == null ? null : sumber.getLocation();
            if (url != null && "file".equals(url.getProtocol())) {
                Path lokasi = Path.of(url.toURI());
                if (Files.isRegularFile(lokasi)) {
                    Image pengganti = bacaIkonLokal(lokasi.resolveSibling(relatif));
                    if (pengganti != null) {
                        return pengganti;
                    }
                }
            }
        } catch (java.net.URISyntaxException | IllegalArgumentException | SecurityException ex) {
            // Ikon dalam resource aplikasi masih bisa dibaca jika lokasi JAR tidak tersedia.
        }

        Image pengganti = bacaIkonLokal(relatif);
        if (pengganti != null) {
            return pengganti;
        }
        URL bawaan = SuratKontrolRanapPanel.class.getResource("/" + sumberIkon);
        try {
            return bawaan == null ? null : ImageIO.read(bawaan);
        } catch (IOException ex) {
            return null;
        }
    }

    private static Image bacaIkonLokal(Path file) {
        try {
            return Files.isRegularFile(file) ? ImageIO.read(file.toFile()) : null;
        } catch (IOException | SecurityException ex) {
            return null;
        }
    }

    private static Icon ikonTampilan(Image gambar, Ikon cadangan, Color warna, boolean aktif) {
        if (gambar == null) {
            return cadangan.berwarna(warna);
        }
        return new Icon() {
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
            @Override public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (!aktif) {
                    g.setComposite(AlphaComposite.SrcOver.derive(.45f));
                }
                g.drawImage(gambar, x, y, getIconWidth(), getIconHeight(), c);
                g.dispose();
            }
        };
    }

    private static AbstractButton gayaTombol(AbstractButton tombol, String teks, Color latar,
            Color warnaTeks, Color garis) {
        tombol.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                boolean aktif = tombol.isEnabled();
                boolean interaksi = tombol.getModel().isArmed() || tombol.getModel().isRollover();
                Color warna = aktif ? latar : new Color(0xF0F3F7);
                if (aktif && interaksi) {
                    warna = latar.equals(BIRU) ? BIRU.darker() : BIRU_MUDA;
                }
                latarBulat(g, warna, c.getWidth(), c.getHeight(), RADIUS_INPUT);
                new BPJSPulangUI.Bingkai(tombol.hasFocus() ? FOKUS : garis, 1f, RADIUS_INPUT)
                        .paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
                super.paint(g, c);
            }

            @Override protected void paintText(Graphics g, AbstractButton b, Rectangle area, String text) {
                gambarTeks(b, g, b.getFont(), b.isEnabled() ? warnaTeks : REDUP, text, b.getDisplayedMnemonicIndex(),
                        area.x, area.y + b.getFontMetrics(b.getFont()).getAscent());
            }

            @Override public Dimension getPreferredSize(JComponent c) {
                Dimension ukuran = super.getPreferredSize(c);
                return new Dimension(ukuran.width, Math.max(TINGGI_INPUT, ukuran.height));
            }
        });
        tombol.setText(teks);
        tombol.setFont(FONT_SEMIBOLD.deriveFont(13f));
        tombol.setForeground(warnaTeks);
        tombol.setBackground(latar);
        tombol.setPreferredSize(null);
        tombol.setOpaque(false);
        tombol.setContentAreaFilled(false);
        tombol.setFocusPainted(false);
        tombol.setBorderPainted(false);
        tombol.setRolloverEnabled(true);
        int sisi = BIRU.equals(latar) ? 14 : 10;
        tombol.setBorder(BorderFactory.createEmptyBorder(4, sisi, 4, sisi));
        return tombol;
    }

    private static Graphics2D grafikTeks(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        return g;
    }

    private static void gambarTeks(JComponent c, Graphics graphics, Font font, Color warna,
            String teks, int mnemonik, int x, int baseline) {
        Graphics2D g = grafikTeks(graphics);
        g.setFont(font);
        g.setColor(warna);
        BasicGraphicsUtils.drawStringUnderlineCharAt(c, g, teks, mnemonik, x, baseline);
        g.dispose();
    }

    private static void gambarField(JTextField field, Graphics graphics) {
        Graphics2D g = grafikTeks(graphics);
        g.setFont(field.getFont());
        field.getUI().paint(g, field);
        g.dispose();
    }

    enum Ikon implements Icon {
        PASIEN, BERKAS, KARTU, KALENDER, MEDIS, EDIT,
        POLI, DOKTER, OBAT, LAB, RADIOLOGI, SIMPAN, CETAK, PULANG, KELUAR, MUAT_ULANG, CHEVRON,
        CARI, CENTANG, HAPUS, UPLOAD, PESAN;

        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            gambar(g, x, y, BIRU);
        }

        Icon berwarna(Color warna) {
            return new Icon() {
                @Override public int getIconWidth() { return Ikon.this.getIconWidth(); }
                @Override public int getIconHeight() { return Ikon.this.getIconHeight(); }
                @Override public void paintIcon(Component c, Graphics g, int x, int y) { gambar(g, x, y, warna); }
            };
        }

        private void gambar(Graphics grafik, int x, int y, Color warna) {
            Graphics2D g = (Graphics2D) grafik.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(warna);
            g.setStroke(new BasicStroke(1.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (this) {
                case PASIEN:
                    g.drawOval(5, 2, 6, 6);
                    g.drawArc(2, 9, 12, 8, 0, 180);
                    break;
                case BERKAS:
                    g.drawRoundRect(3, 1, 10, 14, 2, 2);
                    g.drawLine(6, 6, 10, 6);
                    g.drawLine(6, 9, 10, 9);
                    break;
                case KARTU:
                    g.drawRoundRect(1, 3, 14, 10, 3, 3);
                    g.drawOval(4, 6, 3, 3);
                    g.drawLine(9, 7, 12, 7);
                    g.drawLine(9, 10, 12, 10);
                    break;
                case KALENDER:
                    g.drawRoundRect(2, 3, 12, 11, 2, 2);
                    g.drawLine(2, 6, 14, 6);
                    g.drawLine(5, 1, 5, 4);
                    g.drawLine(11, 1, 11, 4);
                    break;
                case MEDIS:
                    g.drawOval(1, 1, 14, 14);
                    g.drawLine(5, 8, 11, 8);
                    g.drawLine(8, 5, 8, 11);
                    break;
                case EDIT:
                    g.drawPolygon(new int[]{3, 4, 11, 14, 7}, new int[]{13, 9, 2, 5, 12}, 5);
                    g.drawLine(9, 4, 12, 7);
                    break;
                case POLI:
                    g.drawRoundRect(2, 4, 12, 10, 2, 2);
                    g.drawLine(6, 14, 6, 11);
                    g.drawLine(10, 14, 10, 11);
                    g.drawLine(6, 11, 10, 11);
                    g.drawLine(6, 7, 10, 7);
                    g.drawLine(8, 5, 8, 9);
                    break;
                case DOKTER:
                    g.drawLine(3, 2, 3, 6);
                    g.drawLine(9, 2, 9, 6);
                    g.drawArc(3, 3, 6, 6, 180, 180);
                    g.drawLine(6, 9, 6, 11);
                    g.drawArc(6, 8, 7, 6, 180, 180);
                    g.drawOval(11, 5, 4, 4);
                    break;
                case OBAT:
                    g.rotate(-Math.PI / 4, 8, 8);
                    g.drawRoundRect(1, 5, 14, 6, 6, 6);
                    g.drawLine(8, 5, 8, 11);
                    break;
                case LAB:
                    g.drawLine(5, 2, 11, 2);
                    g.drawPolyline(new int[]{6, 6, 2, 3, 13, 14, 10, 10},
                            new int[]{2, 6, 12, 14, 14, 12, 6, 2}, 8);
                    g.drawLine(4, 10, 12, 10);
                    break;
                case RADIOLOGI:
                    g.drawRoundRect(1, 2, 14, 12, 2, 2);
                    g.drawLine(8, 5, 8, 11);
                    g.drawArc(3, 5, 4, 6, 90, 180);
                    g.drawArc(9, 5, 4, 6, 270, 180);
                    break;
                case SIMPAN:
                    g.drawPolyline(new int[]{2, 2, 11, 14, 14, 2}, new int[]{14, 2, 2, 5, 14, 14}, 6);
                    g.drawPolyline(new int[]{5, 5, 10, 10}, new int[]{2, 6, 6, 2}, 4);
                    g.drawRect(5, 9, 6, 5);
                    break;
                case CETAK:
                    g.drawRect(4, 2, 8, 4);
                    g.drawRoundRect(1, 6, 14, 6, 2, 2);
                    g.drawLine(12, 8, 13, 8);
                    g.drawRect(4, 10, 8, 4);
                    break;
                case PULANG:
                    g.drawPolyline(new int[]{1, 7, 13}, new int[]{7, 2, 7}, 3);
                    g.drawPolyline(new int[]{3, 3, 10}, new int[]{6, 14, 14}, 3);
                    g.drawLine(7, 10, 15, 10);
                    g.drawPolyline(new int[]{12, 15, 12}, new int[]{7, 10, 13}, 3);
                    break;
                case KELUAR:
                    g.drawPolyline(new int[]{8, 2, 2, 8}, new int[]{2, 2, 14, 14}, 4);
                    g.drawLine(6, 8, 15, 8);
                    g.drawPolyline(new int[]{12, 15, 12}, new int[]{5, 8, 11}, 3);
                    break;
                case MUAT_ULANG:
                    g.drawArc(2, 2, 12, 12, 50, 290);
                    g.drawPolyline(new int[]{11, 14, 14}, new int[]{6, 6, 3}, 3);
                    break;
                case CHEVRON:
                    g.drawPolyline(new int[]{4, 8, 12}, new int[]{6, 10, 6}, 3);
                    break;
                case CARI:
                    g.drawOval(2, 2, 8, 8);
                    g.drawLine(9, 9, 14, 14);
                    break;
                case CENTANG:
                    g.drawPolyline(new int[]{2, 6, 14}, new int[]{8, 12, 4}, 3);
                    break;
                case HAPUS:
                    g.drawLine(2, 4, 14, 4);
                    g.drawPolyline(new int[]{4, 5, 11, 12}, new int[]{4, 14, 14, 4}, 4);
                    g.drawLine(6, 2, 10, 2);
                    g.drawLine(8, 6, 8, 11);
                    break;
                case UPLOAD:
                    g.drawPolyline(new int[]{2, 2, 14, 14}, new int[]{9, 14, 14, 9}, 4);
                    g.drawLine(8, 2, 8, 10);
                    g.drawPolyline(new int[]{4, 8, 12}, new int[]{6, 2, 6}, 3);
                    break;
                case PESAN:
                    g.drawRoundRect(2, 3, 12, 10, 2, 2);
                    g.drawPolyline(new int[]{2, 8, 14}, new int[]{4, 8, 4}, 3);
                    break;
            }
            g.dispose();
        }
    }

    static JLabel label(String teks, int ukuran, boolean tebal) {
        JLabel label = new JLabel(teks);
        label.setForeground(TEKS);
        label.setFont((tebal ? FONT_SEMIBOLD : FONT).deriveFont((float) ukuran));
        return label;
    }

    private static JLabel labelRuas(String teks) {
        JLabel label = label(teks, 12, true);
        label.setForeground(TEKS_LABEL);
        return label;
    }

    private static JComponent area(String nama, JTextArea teks, JButton ambil) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = labelRuas(nama);
        label.setLabelFor(teks);
        if (ambil == null) {
            panel.add(label, BorderLayout.NORTH);
        } else {
            panel.add(denganAksi(label, ambil), BorderLayout.NORTH);
        }
        Kotak kotak = new Kotak(Color.WHITE, new Insets(5, 10, 5, 10));
        kotak.add(teks);
        kotak.pantau(teks);
        panel.add(kotak, BorderLayout.CENTER);
        return panel;
    }

    static final class Kartu extends Permukaan {
        private JPanel isiKartu;

        Kartu(String judul) {
            this(new BorderLayout(), new Insets(0, 0, 0, 0));
            isiKartu = new JPanel();
            isiKartu.setOpaque(false);
            isiKartu.setLayout(new BoxLayout(isiKartu, BoxLayout.Y_AXIS));
            add(isiKartu, BorderLayout.CENTER);
            if (judul != null) {
                JLabel heading = label(judul, 16, true);
                heading.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS_HALUS),
                        BorderFactory.createEmptyBorder(0, 14, 0, 14)));
                heading.setPreferredSize(new Dimension(0, 40));
                add(heading, BorderLayout.NORTH);
                isiKartu.setBorder(BorderFactory.createEmptyBorder(9, 14, 10, 14));
            } else {
                isiKartu.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            }
        }

        Kartu(java.awt.LayoutManager layout, Insets isi) {
            super(layout, 20);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(new BPJSPulangUI.Bingkai(GARIS, 1f, 20),
                    BorderFactory.createEmptyBorder(isi.top, isi.left, isi.bottom, isi.right)));
        }

        void tambah(JComponent isi) {
            JPanel baris = new JPanel(new BorderLayout()) {
                @Override public Dimension getMaximumSize() {
                    return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                }
            };
            baris.setOpaque(false);
            baris.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (isiKartu.getComponentCount() > 0) {
                baris.setBorder(BorderFactory.createEmptyBorder(9, 0, 0, 0));
            }
            baris.add(isi);
            isiKartu.add(baris);
        }
    }

    private static class Permukaan extends JPanel {
        private final int radius;

        Permukaan(java.awt.LayoutManager layout, int radius) {
            super(layout);
            this.radius = radius;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            latarBulat(g, getBackground(), getWidth(), getHeight(), radius);
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D area = grafikTeks(g);
            area.clip(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, radius - 2, radius - 2));
            super.paintChildren(area);
            area.dispose();
        }
    }

    private static final class Kotak extends JPanel {
        private final Color isi;

        Kotak(Color isi, Insets margin) {
            super(new BorderLayout());
            this.isi = isi;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(margin.top, margin.left, margin.bottom, margin.right));
        }

        void pantau(Component komponen) {
            pantauFokus(komponen, this);
        }

        @Override protected void paintComponent(Graphics g) {
            latarBulat(g, isi, getWidth(), getHeight(), RADIUS_INPUT);
        }

        @Override protected void paintBorder(Graphics g) {
            bingkaiFokus(this, g, 0, 0, getWidth(), getHeight());
        }
    }

    private static void pantauFokus(Component komponen, JComponent bingkai) {
        komponen.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { bingkai.repaint(); }
            @Override public void focusLost(FocusEvent e) { bingkai.repaint(); }
        });
        if (komponen instanceof Container) {
            for (Component anak : ((Container) komponen).getComponents()) {
                pantauFokus(anak, bingkai);
            }
        }
    }

    private static void bingkaiFokus(Component komponen, Graphics g, int x, int y, int lebar, int tinggi) {
        Component fokus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean aktif = fokus != null && SwingUtilities.isDescendingFrom(fokus, komponen);
        new BPJSPulangUI.Bingkai(aktif ? FOKUS : GARIS, 1f, RADIUS_INPUT)
                .paintBorder(komponen, g, x, y, lebar, tinggi);
    }

    private static void latarBulat(Graphics graphics, Color warna, int lebar, int tinggi, int radius) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(warna);
        g.fillRoundRect(0, 0, lebar, tinggi, radius, radius);
        g.dispose();
    }

    private static final class KolomDokumen extends JPanel {
        private final JComponent kiri;
        private final JComponent kanan;

        KolomDokumen(JComponent kiri, JComponent kanan) {
            super(null);
            this.kiri = kiri;
            this.kanan = kanan;
            setOpaque(false);
            add(kiri);
            add(kanan);
        }

        private boolean bertumpuk() {
            return getWidth() > 0 && getWidth() < 1060;
        }

        @Override public Dimension getPreferredSize() {
            Dimension kontrol = kiri.getPreferredSize();
            Dimension lepas = kanan.getPreferredSize();
            if (bertumpuk()) {
                return new Dimension(Math.max(kontrol.width, lepas.width), kontrol.height + JARAK_KOLOM + lepas.height);
            }
            return new Dimension(kontrol.width + JARAK_KOLOM + lepas.width, Math.max(kontrol.height, lepas.height));
        }

        @Override public void doLayout() {
            int tinggiKiri = kiri.getPreferredSize().height;
            int tinggiKanan = kanan.getPreferredSize().height;
            if (bertumpuk()) {
                kiri.setBounds(0, 0, getWidth(), tinggiKiri);
                kanan.setBounds(0, tinggiKiri + JARAK_KOLOM, getWidth(), tinggiKanan);
            } else {
                int tersedia = Math.max(0, getWidth() - JARAK_KOLOM);
                int lebarKiri = (int) Math.round(tersedia * .475);
                kiri.setBounds(0, 0, lebarKiri, tinggiKiri);
                kanan.setBounds(lebarKiri + JARAK_KOLOM, 0, tersedia - lebarKiri, tinggiKanan);
            }
        }
    }

    private static final class KanvasDokumen extends JPanel implements Scrollable {
        KanvasDokumen() { setBackground(KANVAS); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientasi, int arah) { return 22; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientasi, int arah) {
            return Math.max(22, r.height - 22);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class TeksMenyesuaikan extends JTextArea {
        private int lebarLama;

        TeksMenyesuaikan() {
            setFont(FONT.deriveFont(14f));
            setForeground(TEKS);
            setCaretColor(BIRU);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setLineWrap(true);
            setWrapStyleWord(true);
            setFocusTraversalKeysEnabled(true);
            getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { revalidate(); }
                @Override public void removeUpdate(DocumentEvent e) { revalidate(); }
                @Override public void changedUpdate(DocumentEvent e) { revalidate(); }
            });
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) {
                    if (getWidth() != lebarLama) {
                        lebarLama = getWidth();
                        revalidate();
                    }
                }
            });
        }

        @Override public Dimension getPreferredSize() {
            Insets margin = getInsets();
            int lebar = getWidth() > 0 ? getWidth() : 500;
            View view = getUI().getRootView(this);
            view.setSize(Math.max(1, lebar - margin.left - margin.right), Integer.MAX_VALUE);
            int tinggi = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)) + margin.top + margin.bottom;
            return new Dimension(300, Math.max(28, tinggi));
        }

        @Override public Dimension getMinimumSize() {
            return new Dimension(80, 28);
        }
    }
}
