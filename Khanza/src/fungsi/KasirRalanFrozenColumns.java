package fungsi;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Tampilan lokal DlgKasirRalan, tanpa query database di renderer.
 * Kolom model/view tabel utama 0..28 tetap utuh (Status hanya disembunyikan).
 * Data tambahan di akhir model: 29=Status SKDP, 30=penanda geriatri L/P.
 */
public final class KasirRalanFrozenColumns extends JPanel {
    public static final int KOLOM_STATUS = 10;
    public static final int KOLOM_BAYAR = 15;
    public static final int KOLOM_RESUME = 23;
    public static final int KOLOM_SKDP = 29;
    public static final int KOLOM_GERIATRI = 30;
    private static final Color NORMAL = Color.WHITE;
    private static final Color SELANG = new Color(249, 250, 252);
    private static final Color TEKS = new Color(45, 52, 60);
    private static final Color PILIH = new Color(226, 240, 254);
    private static final Color GARIS = new Color(32, 112, 190);
    private static final Color GRID = new Color(228, 233, 239);

    private final JTable utama;
    private final JTable frozen;
    private final JScrollPane scrollFrozen;
    private final JLayer<JScrollPane> layerUtama;

    public KasirRalanFrozenColumns(final JTable utama, final JScrollPane scrollUtama,
            final Runnable saatPasienDipilih) {
        super(new BorderLayout());
        this.utama = utama;
        setOpaque(true);
        setBackground(NORMAL);
        utama.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        utama.setRowHeight(Math.max(36, utama.getRowHeight()));
        utama.setSelectionBackground(PILIH);
        utama.setSelectionForeground(TEKS);
        utama.setGridColor(GRID);
        utama.setShowVerticalLines(false);
        utama.setDefaultRenderer(Object.class, new NeutralRenderer());
        // Handler lama menggunakan nomor kolom view tetap.
        utama.getTableHeader().setReorderingAllowed(false);
        utama.getTableHeader().setFont(utama.getFont());
        sembunyikanKolom(utama, KOLOM_STATUS);
        sembunyikanKolom(utama, KOLOM_RESUME);
        sembunyikanKolom(utama, KOLOM_SKDP);
        sembunyikanKolom(utama, KOLOM_GERIATRI);

        DefaultTableColumnModel kolom = new DefaultTableColumnModel();
        TableColumn status = new TableColumn(KOLOM_STATUS);
        status.setHeaderValue("Status");
        status.setCellRenderer(new BadgeRenderer());
        kolom.addColumn(status);
        frozen = new FrozenTable(utama, kolom);
        frozen.setName("tbKasirRalanFrozenRSAJ");
        frozen.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        frozen.setFont(utama.getFont());
        frozen.setRowHeight(utama.getRowHeight());
        frozen.setRowMargin(utama.getRowMargin());
        frozen.setSelectionBackground(PILIH);
        frozen.setSelectionForeground(TEKS);
        frozen.setBackground(NORMAL);
        frozen.setGridColor(GRID);
        frozen.setShowVerticalLines(false);
        frozen.setFocusable(false);
        frozen.setComponentPopupMenu(utama.getComponentPopupMenu());
        frozen.getTableHeader().setReorderingAllowed(false);
        frozen.getTableHeader().setResizingAllowed(false);
        frozen.getTableHeader().setFont(utama.getTableHeader().getFont());
        frozen.getTableHeader().setBackground(utama.getTableHeader().getBackground());
        frozen.getTableHeader().setForeground(utama.getTableHeader().getForeground());

        FontMetrics fm = frozen.getFontMetrics(frozen.getFont());
        int lebarStatus = Math.max(126,
                Math.max(fm.stringWidth("Berkas Diterima"), fm.stringWidth("Batal Pelayanan")) + 40)
                + Math.max(148, fm.stringWidth("SKDP") + fm.stringWidth("Resume") + 76);
        aturLebar(status, lebarStatus);
        frozen.getTableHeader().setPreferredSize(new Dimension(lebarStatus,
                utama.getTableHeader().getPreferredSize().height));

        scrollUtama.setBorder(BorderFactory.createEmptyBorder());
        scrollUtama.setColumnHeaderView(utama.getTableHeader());
        scrollUtama.getViewport().setBackground(NORMAL);
        scrollUtama.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollUtama.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollFrozen = new JScrollPane(frozen, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFrozen.setBorder(BorderFactory.createEmptyBorder());
        scrollFrozen.setColumnHeaderView(frozen.getTableHeader());
        scrollFrozen.getViewport().setBackground(NORMAL);
        scrollFrozen.getVerticalScrollBar().setModel(scrollUtama.getVerticalScrollBar().getModel());
        scrollFrozen.getVerticalScrollBar().setUnitIncrement(utama.getRowHeight());
        // Scrollbar vertikal utama tersembunyi. Roda mouse di area data tetap
        // menggulir vertikal melalui scrollbar kanan; Shift+roda tetap horizontal.
        scrollUtama.setWheelScrollingEnabled(false);
        scrollUtama.addMouseWheelListener(e -> {
            if (e.isShiftDown()) {
                javax.swing.JScrollBar bar = scrollUtama.getHorizontalScrollBar();
                int direction = e.getPreciseWheelRotation() >= 0 ? 1 : -1;
                double delta = e.getPreciseWheelRotation() * (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL
                        ? e.getScrollAmount() * bar.getUnitIncrement(direction) : bar.getBlockIncrement(direction));
                bar.setValue(bar.getValue() + (int) Math.round(delta));
            } else {
                Point p = SwingUtilities.convertPoint(scrollUtama, e.getPoint(), scrollFrozen);
                scrollFrozen.dispatchEvent(new MouseWheelEvent(scrollFrozen, e.getID(), e.getWhen(),
                        e.getModifiersEx(), p.x, p.y, e.getXOnScreen(), e.getYOnScreen(),
                        e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(),
                        e.getWheelRotation(), e.getPreciseWheelRotation()));
            }
            e.consume();
        });

        JPanel sisiKanan = new JPanel(new BorderLayout());
        sisiKanan.setBackground(NORMAL);
        sisiKanan.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, GRID));
        sisiKanan.setPreferredSize(new Dimension(lebarStatus + 1
                + scrollFrozen.getVerticalScrollBar().getPreferredSize().width, 0));
        JPanel ruangScrollbar = new JPanel();
        ruangScrollbar.setBackground(new Color(245, 247, 250));
        ruangScrollbar.setPreferredSize(new Dimension(0,
                scrollUtama.getHorizontalScrollBar().getPreferredSize().height));
        sisiKanan.add(scrollFrozen, BorderLayout.CENTER);
        sisiKanan.add(ruangScrollbar, BorderLayout.SOUTH);

        layerUtama = new JLayer<JScrollPane>(scrollUtama, new SelectionLineUI(utama));
        add(layerUtama, BorderLayout.CENTER);
        add(sisiKanan, BorderLayout.EAST);
        utama.getSelectionModel().addListSelectionListener(e -> {
            layerUtama.repaint();
            frozen.repaint();
        });
        scrollUtama.getViewport().addChangeListener(e -> layerUtama.repaint());
        // Tabel frozen mengikuti urutan/filter tabel utama, tanpa menggandakan
        // rowsInserted/rowsDeleted pada RowSorter yang sama.
        ikutiSorter();
        utama.addPropertyChangeListener("rowSorter", e -> { ikutiSorter(); frozen.revalidate(); frozen.repaint(); });
        utama.addPropertyChangeListener("selectionModel", e -> frozen.setSelectionModel(utama.getSelectionModel()));
        utama.addPropertyChangeListener("model", e -> frozen.setModel(utama.getModel()));
        utama.addPropertyChangeListener("rowHeight", e -> frozen.setRowHeight(utama.getRowHeight()));
        frozen.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int row = frozen.rowAtPoint(e.getPoint());
                if (row < 0) return;
                // Pilih baris sebelum menu klik kanan dibuka; jangan scroll horizontal.
                utama.setRowSelectionInterval(row, row);
                utama.setColumnSelectionInterval(KOLOM_STATUS, KOLOM_STATUS);
                utama.requestFocusInWindow();
                if (saatPasienDipilih != null) saatPasienDipilih.run();
            }
        });
    }

    private javax.swing.RowSorter<?> sorterDiikuti;
    private final javax.swing.event.RowSorterListener listenerSorter = e -> refreshFrozen();

    private void refreshFrozen() {
        if (frozen != null) { frozen.revalidate(); frozen.repaint(); }
    }

    private void ikutiSorter() {
        if (sorterDiikuti != null) sorterDiikuti.removeRowSorterListener(listenerSorter);
        sorterDiikuti = utama.getRowSorter();
        if (sorterDiikuti != null) sorterDiikuti.addRowSorterListener(listenerSorter);
    }

    public JTable getFrozenTable() { return frozen; }
    public JScrollPane getFrozenScrollPane() { return scrollFrozen; }

    private static void sembunyikanKolom(JTable tabel, int modelIndex) {
        int view = tabel.convertColumnIndexToView(modelIndex);
        if (view < 0) return;
        TableColumn c = tabel.getColumnModel().getColumn(view);
        c.setMinWidth(0); c.setMaxWidth(0); c.setPreferredWidth(0); c.setWidth(0);
    }

    private static void aturLebar(TableColumn c, int width) {
        c.setMinWidth(width); c.setMaxWidth(width); c.setPreferredWidth(width); c.setWidth(width);
    }

    private static String nilai(JTable tabel, int row, int modelColumn) {
        if (row < 0 || row >= tabel.getRowCount() || modelColumn >= tabel.getModel().getColumnCount()) return "";
        Object value = tabel.getModel().getValueAt(tabel.convertRowIndexToModel(row), modelColumn);
        return value == null ? "" : value.toString().trim();
    }

    private static Color latar(int row, boolean selected) { return selected ? PILIH : row % 2 == 0 ? NORMAL : SELANG; }

    private static final class FrozenTable extends JTable {
        private JTable sumber;
        FrozenTable(JTable sumber, DefaultTableColumnModel kolom) {
            super(sumber.getModel(), kolom);
            this.sumber = sumber;
            setAutoCreateColumnsFromModel(false);
            setSelectionModel(sumber.getSelectionModel());
        }
        @Override public int getRowCount() { return sumber == null ? super.getRowCount() : sumber.getRowCount(); }
        @Override public int convertRowIndexToModel(int row) { return sumber == null ? super.convertRowIndexToModel(row) : sumber.convertRowIndexToModel(row); }
        @Override public int convertRowIndexToView(int row) { return sumber == null ? super.convertRowIndexToView(row) : sumber.convertRowIndexToView(row); }
        @Override public void tableChanged(TableModelEvent event) {
            if (sumber == null) { super.tableChanged(event); return; }
            // Tabel utama menangani pemilihan/sorting. Hindari clearSelection dari
            // tabel kedua yang bisa menghapus pilihan saat status diperbarui.
            revalidate(); repaint();
        }
    }

    private static final class NeutralRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBackground(latar(row, selected)); setForeground(TEKS);
            setFont(table.getFont()); setIcon(null); setToolTipText(null);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            if (table.convertColumnIndexToModel(column) == 2) {
                String jk = nilai(table, row, KOLOM_GERIATRI);
                if (jk.equalsIgnoreCase("L") || jk.equalsIgnoreCase("P")) {
                    setFont(new Font("Segoe UI Emoji", Font.PLAIN, table.getFont().getSize()));
                    setText((jk.equalsIgnoreCase("L") ? "\uD83D\uDC74 " : "\uD83D\uDC75 ") + (value == null ? "" : value));
                    setToolTipText("Pasien geriatri — " + (jk.equalsIgnoreCase("L") ? "laki-laki" : "perempuan"));
                }
            }
            return this;
        }
    }

    private static final class SelectionLineUI extends LayerUI<JScrollPane> {
        private final JTable tabel;
        SelectionLineUI(JTable tabel) { this.tabel = tabel; }
        @Override public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            @SuppressWarnings("unchecked") JLayer<JScrollPane> layer = (JLayer<JScrollPane>) c;
            JScrollPane scroll = layer.getView();
            Point p = SwingUtilities.convertPoint(scroll.getViewport(), 0, 0, layer);
            Rectangle area = new Rectangle(p.x, p.y, scroll.getViewport().getWidth(), scroll.getViewport().getHeight());
            Graphics copy = g.create(); copy.clipRect(area.x, area.y, area.width, area.height); copy.setColor(GARIS);
            for (int row : tabel.getSelectedRows()) {
                if (row >= tabel.getRowCount()) continue;
                Rectangle r = tabel.getCellRect(row, 0, true);
                int y = p.y + r.y - scroll.getViewport().getViewPosition().y;
                // Tetap di tepi viewport, sekalipun kolom pertama sudah di-scroll keluar.
                copy.fillRect(p.x, y, 4, r.height);
            }
            copy.dispose();
        }
    }

    private static final class Badge {
        final String text, icon;
        final Color background, foreground;
        final boolean bold;
        Badge(String text, String icon, Color background, Color foreground) {
            this(text, icon, background, foreground, false);
        }
        Badge(String text, String icon, Color background, Color foreground, boolean bold) {
            this.text = text; this.icon = icon; this.background = background; this.foreground = foreground;
            this.bold = bold;
        }
    }

    private static final class BadgeRenderer extends JPanel implements TableCellRenderer {
        private Badge[] badges = new Badge[0];
        BadgeRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            setFont(table.getFont()); setBackground(latar(row, selected));
            Badge[] dokumen = badgesPelayanan(table, row);
            badges = new Badge[dokumen.length + 1];
            badges[0] = badgeStatus(table, row);
            System.arraycopy(dokumen, 0, badges, 1, dokumen.length);
            StringBuilder tooltip = new StringBuilder();
            for (Badge badge : badges) {
                if (tooltip.length() > 0) tooltip.append("; ");
                if (badge.text.equals("SKDP") || badge.text.equals("Resume")) {
                    boolean tersedia = badge.icon.equals("check");
                    if (badge.text.equals("SKDP")) tooltip.append(tersedia
                            ? "SKDP BPJS sudah tersedia untuk SEP kunjungan ini"
                            : "SKDP BPJS belum dibuat/terbit untuk kunjungan ini");
                    else tooltip.append(tersedia ? "Resume medis sudah tersedia untuk No.Rawat ini" : "Resume medis belum ada");
                }
                else tooltip.append(badge.text.equals("Closing") ? "Sudah dilayani; billing sudah closing (Sudah Bayar)" : "Status: " + badge.text);
            }
            if (nilai(table, row, KOLOM_SKDP).equalsIgnoreCase("Menunggu"))
                tooltip.append("; Pengecekan rencana kontrol/SKDP sedang berlangsung");
            else if (nilai(table, row, KOLOM_SKDP).equalsIgnoreCase("Gagal"))
                tooltip.append("; Pengecekan rencana kontrol/SKDP belum berhasil; muat ulang daftar");
            setToolTipText(tooltip.toString());
            getAccessibleContext().setAccessibleName(tooltip.toString());
            return this;
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int height = Math.min(getHeight() - 4, Math.max(20, fm.getHeight() + 4));
            int y = (getHeight() - height) / 2, x = 6;
            for (Badge badge : badges) {
                g.setFont(badge.bold ? getFont().deriveFont(Font.BOLD) : getFont());
                fm = g.getFontMetrics();
                int width = fm.stringWidth(badge.text) + 32;
                g.setColor(badge.background); g.fillRoundRect(x, y, width, height, height, height);
                g.setColor(badge.foreground);
                drawIcon(g, badge.icon, x + 8, y + (height - 12) / 2);
                g.drawString(badge.text, x + 25, y + (height - fm.getHeight()) / 2 + fm.getAscent());
                x += width + 4;
            }
            g.dispose();
        }
    }

    private static Badge badgeStatus(JTable table, int row) {
        String status = nilai(table, row, KOLOM_STATUS);
        String key = status.toLowerCase(Locale.ROOT);
        if (key.equals("sudah") && nilai(table, row, KOLOM_BAYAR).equalsIgnoreCase("Sudah Bayar"))
            return new Badge("Closing", "lock", new Color(38, 43, 49), Color.WHITE, true);
        if (key.equals("sudah")) return new Badge("Sudah", "check", new Color(252, 228, 230), new Color(200, 43, 52));
        if (key.equals("belum")) return new Badge("Belum", "clock", new Color(234, 238, 243), new Color(75, 86, 99));
        if (key.equals("berkas diterima")) return new Badge("Berkas Diterima", "file", new Color(218, 243, 222), new Color(37, 109, 61));
        if (key.equals("batal")) return new Badge("Batal Pelayanan", "cross", new Color(255, 239, 173), new Color(126, 88, 7));
        // Status lain tetap ditampilkan; tidak mengubah nilai stts di database.
        return new Badge(status.isEmpty() ? "—" : status, "file", new Color(234, 238, 243), new Color(75, 86, 99));
    }

    private static Badge[] badgesPelayanan(JTable table, int row) {
        if (nilai(table, row, KOLOM_STATUS).equalsIgnoreCase("Batal")) return new Badge[0];
        String statusSkdp = nilai(table, row, KOLOM_SKDP);
        boolean skdpTersedia = statusSkdp.equalsIgnoreCase("Ada");
        boolean resumeTersedia = nilai(table, row, KOLOM_RESUME).equalsIgnoreCase("Ada");
        Badge skdp = skdpTersedia
                ? new Badge("SKDP", "check", new Color(218, 243, 222), new Color(37, 109, 61))
                : new Badge("SKDP", "cross", new Color(234, 238, 243), new Color(75, 86, 99));
        Badge resume = resumeTersedia
                ? new Badge("Resume", "check", new Color(218, 243, 222), new Color(37, 109, 61))
                : new Badge("Resume", "cross", new Color(234, 238, 243), new Color(75, 86, 99));
        return nilai(table, row, 17).equalsIgnoreCase("BPJ")
                && (skdpTersedia || statusSkdp.equalsIgnoreCase("Tidak Ada"))
                ? new Badge[]{skdp, resume} : new Badge[]{resume};
    }

    /** Ikon vektor kecil, tajam pada HiDPI dan tidak memerlukan font simbol/PNG. */
    private static void drawIcon(Graphics2D g, String icon, int x, int y) {
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (icon.equals("check")) {
            g.drawLine(x, y + 6, x + 4, y + 10); g.drawLine(x + 4, y + 10, x + 11, y + 2);
        } else if (icon.equals("clock")) {
            g.drawOval(x, y, 11, 11); g.drawLine(x + 6, y + 2, x + 6, y + 6); g.drawLine(x + 6, y + 6, x + 9, y + 7);
        } else if (icon.equals("cross")) {
            g.drawLine(x + 2, y + 2, x + 10, y + 10); g.drawLine(x + 10, y + 2, x + 2, y + 10);
        } else if (icon.equals("warning")) {
            g.drawPolygon(new int[]{x + 6, x, x + 12}, new int[]{y, y + 11, y + 11}, 3);
            g.drawLine(x + 6, y + 4, x + 6, y + 7); g.fillOval(x + 5, y + 9, 2, 2);
        } else if (icon.equals("lock")) {
            g.drawRoundRect(x + 1, y + 5, 10, 7, 2, 2); g.drawArc(x + 3, y, 6, 10, 0, 180); g.drawLine(x + 6, y + 8, x + 6, y + 10);
        } else {
            g.drawRoundRect(x + 1, y, 9, 12, 2, 2); g.drawLine(x + 3, y + 4, x + 8, y + 4); g.drawLine(x + 3, y + 7, x + 8, y + 7);
        }
    }
}
