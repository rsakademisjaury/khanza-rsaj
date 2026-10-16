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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Tampilan lokal DlgKasirRalan, tanpa query database di renderer.
 * Indeks model tetap; urutan kolom tampilan dapat berubah tanpa mengubah data.
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
    private static final Color GRID = new Color(235, 237, 240);
    private static final Color BATAS_HEADER = new Color(203, 213, 225);

    private final JTable utama;
    private final JTable frozen;
    private final JScrollPane scrollFrozen;
    private final StatusGutter garisStatus;
    private final TableColumn kolomStatus;
    private final JPanel sisiKanan;
    private boolean lebarStatusTerjadwal;

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
        utama.setShowVerticalLines(true);
        utama.setDefaultRenderer(Object.class, new NeutralRenderer());
        // Handler membaca indeks model melalui nilaiModel, bukan posisi tampilan.
        utama.getTableHeader().setReorderingAllowed(false);
        utama.getTableHeader().setFont(utama.getFont());
        sembunyikanKolom(utama, KOLOM_STATUS);
        sembunyikanKolom(utama, KOLOM_RESUME);
        sembunyikanKolom(utama, KOLOM_SKDP);
        sembunyikanKolom(utama, KOLOM_GERIATRI);
        susunKolom(utama);
        // Tambahan ruang 5 px hanya untuk No.SEP sampai Status Bayar.
        for (int model : new int[]{20, 9, 11, 12, 13, 14, 15}) {
            int view = utama.convertColumnIndexToView(model);
            if (view < 0) continue;
            TableColumn c = utama.getColumnModel().getColumn(view);
            c.setPreferredWidth(c.getPreferredWidth() + 5);
            c.setWidth(c.getWidth() + 5);
        }

        DefaultTableColumnModel kolom = new DefaultTableColumnModel();
        TableColumn status = new TableColumn(KOLOM_STATUS);
        kolomStatus = status;
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
        frozen.setShowVerticalLines(true);
        frozen.setFocusable(false);
        frozen.setComponentPopupMenu(utama.getComponentPopupMenu());
        frozen.getTableHeader().setReorderingAllowed(false);
        frozen.getTableHeader().setResizingAllowed(false);
        frozen.getTableHeader().setFont(utama.getTableHeader().getFont());
        frozen.getTableHeader().setBackground(utama.getTableHeader().getBackground());
        frozen.getTableHeader().setForeground(utama.getTableHeader().getForeground());

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

        sisiKanan = new JPanel(new BorderLayout());
        sisiKanan.setBackground(NORMAL);
        sisiKanan.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, GRID));
        JPanel ruangScrollbar = new JPanel();
        ruangScrollbar.setBackground(new Color(245, 247, 250));
        ruangScrollbar.setPreferredSize(new Dimension(0,
                scrollUtama.getHorizontalScrollBar().getPreferredSize().height));
        sisiKanan.add(scrollFrozen, BorderLayout.CENTER);
        sisiKanan.add(ruangScrollbar, BorderLayout.SOUTH);

        // Row header adalah gutter tetap: tidak menutup tulisan pada kolom data
        // dan mengikuti scroll vertikal secara native, tanpa ikut scroll horizontal.
        garisStatus = new StatusGutter(utama);
        scrollUtama.setRowHeaderView(garisStatus);
        JPanel sudut = new JPanel();
        sudut.setBackground(utama.getTableHeader().getBackground());
        sudut.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BATAS_HEADER));
        scrollUtama.setCorner(JScrollPane.UPPER_LEFT_CORNER, sudut);
        garisStatus.setComponentPopupMenu(utama.getComponentPopupMenu());
        garisStatus.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int row = utama.rowAtPoint(new Point(0, e.getY()));
                if (row < 0) return;
                utama.setRowSelectionInterval(row, row);
                utama.requestFocusInWindow();
                if (saatPasienDipilih != null) saatPasienDipilih.run();
            }
        });
        add(scrollUtama, BorderLayout.CENTER);
        add(sisiKanan, BorderLayout.EAST);
        utama.getSelectionModel().addListSelectionListener(e -> {
            garisStatus.repaint();
            frozen.repaint();
        });
        scrollUtama.getViewport().addChangeListener(e -> garisStatus.repaint());
        new AlignedColumns(utama, scrollUtama);
        utama.getModel().addTableModelListener(listenerModel);
        // Tabel frozen mengikuti urutan/filter tabel utama, tanpa menggandakan
        // rowsInserted/rowsDeleted pada RowSorter yang sama.
        ikutiSorter();
        utama.addPropertyChangeListener("rowSorter", e -> { ikutiSorter(); refreshFrozen(); });
        utama.addPropertyChangeListener("selectionModel", e -> frozen.setSelectionModel(utama.getSelectionModel()));
        utama.addPropertyChangeListener("model", e -> {
            if (e.getOldValue() instanceof javax.swing.table.TableModel)
                ((javax.swing.table.TableModel) e.getOldValue()).removeTableModelListener(listenerModel);
            utama.getModel().addTableModelListener(listenerModel);
            frozen.setModel(utama.getModel()); refreshFrozen();
        });
        utama.addPropertyChangeListener("rowHeight", e -> {
            frozen.setRowHeight(utama.getRowHeight()); refreshFrozen();
        });
        utama.addPropertyChangeListener("font", e -> {
            frozen.setFont(utama.getFont()); refreshFrozen();
            scrollUtama.getRowHeader().revalidate(); scrollUtama.revalidate();
        });
        frozen.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int row = frozen.rowAtPoint(e.getPoint());
                if (row < 0) return;
                // Pilih baris sebelum menu klik kanan dibuka; jangan scroll horizontal.
                utama.setRowSelectionInterval(row, row);
                int view = utama.convertColumnIndexToView(KOLOM_STATUS);
                if (view >= 0) utama.setColumnSelectionInterval(view, view);
                utama.requestFocusInWindow();
                if (saatPasienDipilih != null) saatPasienDipilih.run();
            }
        });
        garisBatasHeader(utama);
        garisBatasHeader(frozen);
        sesuaikanLebarStatus();
    }

    private static void garisBatasHeader(JTable table) {
        table.getTableHeader().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BATAS_HEADER),
                table.getTableHeader().getBorder()));
    }

    private javax.swing.RowSorter<?> sorterDiikuti;
    private final javax.swing.event.RowSorterListener listenerSorter = e -> refreshFrozen();
    private final javax.swing.event.TableModelListener listenerModel = e -> refreshFrozen();

    private void refreshFrozen() {
        if (frozen != null) { frozen.revalidate(); frozen.repaint(); }
        if (garisStatus != null) { garisStatus.revalidate(); garisStatus.repaint(); }
        // Banyak baris dipublish sekaligus: ukur satu kali setelah batch EDT,
        // bukan per baris atau setiap paint. Tidak mengakses database.
        if (!lebarStatusTerjadwal) {
            lebarStatusTerjadwal = true;
            SwingUtilities.invokeLater(() -> {
                lebarStatusTerjadwal = false;
                sesuaikanLebarStatus();
            });
        }
    }

    private void sesuaikanLebarStatus() {
        Font font = frozen.getFont();
        FontMetrics biasa = frozen.getFontMetrics(font);
        FontMetrics bold = frozen.getFontMetrics(font.deriveFont(Font.BOLD));
        int width = frozen.getTableHeader().getFontMetrics(frozen.getTableHeader().getFont())
                .stringWidth("Status") + 12;
        // Satu kolom memiliki lebar seragam: gunakan baris badge terlebar pada
        // seluruh hasil/filter, agar tidak berubah ketika digulir vertikal.
        for (int row = 0; row < frozen.getRowCount(); row++) {
            Badge[] badges = badgesBaris(frozen, row);
            int needed = 12 + Math.max(0, badges.length - 1) * 4;
            for (Badge badge : badges)
                needed += (badge.bold ? bold : biasa).stringWidth(badge.text) + 32;
            width = Math.max(width, needed);
        }
        int lebarPanel = width + 1 + scrollFrozen.getVerticalScrollBar().getPreferredSize().width;
        if (kolomStatus.getWidth() == width && sisiKanan.getPreferredSize().width == lebarPanel) return;
        aturLebar(kolomStatus, width);
        frozen.getTableHeader().setPreferredSize(new Dimension(width,
                utama.getTableHeader().getPreferredSize().height));
        sisiKanan.setPreferredSize(new Dimension(lebarPanel, 0));
        frozen.revalidate(); frozen.getTableHeader().revalidate();
        revalidate(); repaint();
    }

    private void ikutiSorter() {
        if (sorterDiikuti != null) sorterDiikuti.removeRowSorterListener(listenerSorter);
        sorterDiikuti = utama.getRowSorter();
        if (sorterDiikuti != null) sorterDiikuti.addRowSorterListener(listenerSorter);
    }

    public JTable getFrozenTable() { return frozen; }
    public JScrollPane getFrozenScrollPane() { return scrollFrozen; }

    /** Nomor kolom lama adalah indeks model; baris tetap mengikuti urutan layar. */
    public static Object nilaiModel(JTable tabel, int baris, int kolom) {
        return tabel.getModel().getValueAt(tabel.convertRowIndexToModel(baris), kolom);
    }

    private static void susunKolom(JTable tabel) {
        int[] depan = {0, 1, 2, 3, 4, 20, 9, 11, 12, 13, 14, 15};
        int tujuan = 0;
        for (int model : depan) {
            int view = tabel.convertColumnIndexToView(model);
            if (view >= 0) tabel.moveColumn(view, tujuan++);
        }
    }

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

    private static final class StatusGutter extends JComponent {
        private final JTable tabel;
        StatusGutter(JTable tabel) {
            this.tabel = tabel;
            setOpaque(true);
            setToolTipText("Bulatan warna: status pelayanan; latar biru muda: pasien terpilih");
        }
        private int diameter() {
            // Ukuran penanda status: 80% tinggi huruf, proporsional pada HiDPI.
            return Math.max(6, Math.round(tabel.getFontMetrics(tabel.getFont()).getAscent() * 0.80f));
        }
        @Override public Dimension getPreferredSize() {
            return new Dimension(diameter() + 4, tabel.getPreferredSize().height);
        }
        @Override protected void paintComponent(Graphics g) {
            Rectangle clip = g.getClipBounds();
            g.setColor(NORMAL); g.fillRect(clip.x, clip.y, clip.width, clip.height);
            int count = tabel.getRowCount();
            if (count == 0) return;
            int awal = tabel.rowAtPoint(new Point(0, Math.max(0, clip.y)));
            if (awal < 0) return;
            int akhir = tabel.rowAtPoint(new Point(0, clip.y + clip.height - 1));
            if (akhir < 0) akhir = count - 1;
            for (int row = awal; row <= akhir; row++) {
                Rectangle r = tabel.getCellRect(row, 0, true);
                boolean selected = tabel.isRowSelected(row);
                g.setColor(latar(row, selected));
                g.fillRect(0, r.y, getWidth(), r.height);
                Graphics2D copy = (Graphics2D) g.create();
                copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                copy.setColor(warnaStatus(tabel, row));
                int d = Math.min(diameter(), Math.max(1, r.height - 4));
                copy.fillOval((getWidth() - d) / 2, r.y + (r.height - d) / 2, d, d);
                copy.dispose();
                // Pemisah putih antarbaris tetap dipertahankan.
                g.setColor(NORMAL);
                g.fillRect(0, r.y + r.height - 1, getWidth(), 1);
            }
        }
    }

    /** Meratakan ujung kolom terhadap viewport tanpa mengubah indeks/model. */
    private static final class AlignedColumns implements javax.swing.event.TableColumnModelListener {
        private final JTable table;
        private final JScrollPane scroll;
        private final java.util.Map<TableColumn, UkuranKolom> dasar = new java.util.IdentityHashMap<>();
        private boolean applying, scheduled, dirty = true;
        private int lastX = -1, lastWidth = -1;

        private final class UkuranKolom {
            int width, min;
            final java.beans.PropertyChangeListener listener;
            UkuranKolom(TableColumn column) {
                width = column.getPreferredWidth(); min = column.getMinWidth();
                listener = e -> {
                    if (applying) return;
                    if (e.getPropertyName().equals("preferredWidth")) width = column.getPreferredWidth();
                    else if (e.getPropertyName().equals("minWidth")) min = column.getMinWidth();
                    else return;
                    request(true);
                };
                column.addPropertyChangeListener(listener);
            }
        }

        AlignedColumns(JTable table, JScrollPane scroll) {
            this.table = table; this.scroll = scroll;
            capture(); table.getColumnModel().addColumnModelListener(this);
            scroll.getViewport().addChangeListener(e -> request(false));
            table.getTableHeader().addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) { request(true); }
            });
            request(true);
        }

        private void capture() {
            java.util.Set<TableColumn> current = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            java.util.Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
            while (columns.hasMoreElements()) {
                TableColumn column = columns.nextElement(); current.add(column);
                if (!dasar.containsKey(column)) dasar.put(column, new UkuranKolom(column));
            }
            java.util.Iterator<java.util.Map.Entry<TableColumn, UkuranKolom>> it = dasar.entrySet().iterator();
            while (it.hasNext()) {
                java.util.Map.Entry<TableColumn, UkuranKolom> entry = it.next();
                if (!current.contains(entry.getKey())) {
                    entry.getKey().removePropertyChangeListener(entry.getValue().listener); it.remove();
                }
            }
        }

        private void request(boolean changed) {
            if (applying) return;
            dirty |= changed;
            int x = scroll.getViewport().getViewPosition().x, width = scroll.getViewport().getWidth();
            if (scheduled || (!dirty && lastX == x && lastWidth == width)) return;
            scheduled = true;
            SwingUtilities.invokeLater(() -> { scheduled = false; fit(); });
        }

        private void setWidth(TableColumn column, int width) {
            // Penyempitan sementara hanya pada kolom di luar viewport. Lebar/min
            // aslinya disimpan dan dipulihkan sebelum perhitungan berikutnya.
            column.setMinWidth(Math.min(dasar.get(column).min, width));
            column.setPreferredWidth(width); column.setWidth(width);
        }

        private void fit() {
            int x = scroll.getViewport().getViewPosition().x, width = scroll.getViewport().getWidth();
            if (width <= 0 || table.getTableHeader().getResizingColumn() != null) return;
            if (!dirty && lastX == x && lastWidth == width) return;
            applying = true;
            try {
                capture();
                int count = table.getColumnCount(), right = x + width, end = 0, last = -1, lastEnd = 0, crossing = -1;
                for (int i = 0; i < count; i++) {
                    TableColumn column = table.getColumnModel().getColumn(i);
                    UkuranKolom original = dasar.get(column);
                    column.setMinWidth(original.min);
                    column.setPreferredWidth(original.width); column.setWidth(original.width);
                }
                for (int i = 0; i < count; i++) {
                    TableColumn column = table.getColumnModel().getColumn(i);
                    int size = column.getWidth(); if (size == 0) continue;
                    end += size;
                    if (end <= right) { if (end > x) { last = i; lastEnd = end; } }
                    else { crossing = i; break; }
                }
                if (last >= 0 && lastEnd < right) {
                    TableColumn column = table.getColumnModel().getColumn(last);
                    int extra = Math.min(right - lastEnd, column.getMaxWidth() - column.getWidth());
                    setWidth(column, column.getWidth() + extra);
                    // Pindahkan ruang dari kolom sesudahnya yang berada di luar
                    // viewport. Total lebar/range scrollbar tetap, tanpa loop
                    // resize. Kolom tersebut pulih ketika digulir ke arahnya.
                    for (int i = last + 1; i < count && extra > 0; i++) {
                        TableColumn next = table.getColumnModel().getColumn(i);
                        int taken = Math.min(extra, next.getWidth());
                        if (taken > 0) { setWidth(next, next.getWidth() - taken); extra -= taken; }
                    }
                } else if (last < 0 && crossing >= 0) {
                    // Viewport sangat kecil: hanya satu kolom yang muat. Tepatkan
                    // ujungnya dan kembalikan ruang ke kolom di luar layar.
                    TableColumn column = table.getColumnModel().getColumn(crossing);
                    int removed = end - right;
                    setWidth(column, column.getWidth() - removed);
                    for (int i = crossing + 1; i < count && removed > 0; i++) {
                        TableColumn next = table.getColumnModel().getColumn(i);
                        if (next.getMaxWidth() == 0) continue;
                        int added = Math.min(removed, next.getMaxWidth() - next.getWidth());
                        setWidth(next, next.getWidth() + added); removed -= added;
                    }
                }
                dirty = false; lastX = x; lastWidth = width;
                table.revalidate(); table.getTableHeader().revalidate();
                table.repaint(); table.getTableHeader().repaint();
            } finally { applying = false; }
        }

        @Override public void columnAdded(javax.swing.event.TableColumnModelEvent e) { request(true); }
        @Override public void columnRemoved(javax.swing.event.TableColumnModelEvent e) { request(true); }
        @Override public void columnMoved(javax.swing.event.TableColumnModelEvent e) { request(true); }
        @Override public void columnMarginChanged(javax.swing.event.ChangeEvent e) { request(true); }
        @Override public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {
            if (applying || e.getValueIsAdjusting()) return;
            // Navigasi keyboard juga harus dapat membuka kolom berikutnya,
            // termasuk kolom yang sementara tidak mendapat ruang di viewport.
            SwingUtilities.invokeLater(() -> {
                int selected = table.getSelectedColumn();
                if (selected < 0 || selected >= table.getColumnCount()) return;
                TableColumn column = table.getColumnModel().getColumn(selected);
                UkuranKolom original = dasar.get(column);
                if (original == null || original.width == 0) return;
                int x = scroll.getViewport().getViewPosition().x, width = scroll.getViewport().getWidth();
                Rectangle cell = table.getCellRect(Math.max(0, table.getSelectedRow()), selected, true);
                boolean sementaraDipersempit = column.getWidth() < original.width;
                if (width <= 0 || (!sementaraDipersempit && cell.x + cell.width <= x + width)) return;
                int end = 0;
                for (int i = 0; i <= selected; i++) end += dasar.get(table.getColumnModel().getColumn(i)).width;
                scroll.getHorizontalScrollBar().setValue(Math.max(0, end - width));
            });
        }
    }

    private static Color warnaStatus(JTable tabel, int row) {
        String status = nilai(tabel, row, KOLOM_STATUS);
        if (status.equalsIgnoreCase("Sudah")) return nilai(tabel, row, KOLOM_BAYAR)
                .equalsIgnoreCase("Sudah Bayar") ? new Color(38, 43, 49) : new Color(200, 43, 52);
        if (status.equalsIgnoreCase("Berkas Diterima")) return new Color(37, 109, 61);
        if (status.equalsIgnoreCase("Batal")) return new Color(232, 179, 35);
        return new Color(134, 144, 156);
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
            badges = badgesBaris(table, row);
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

    private static Badge[] badgesBaris(JTable table, int row) {
        Badge[] dokumen = badgesPelayanan(table, row);
        Badge[] result = new Badge[dokumen.length + 1];
        result[0] = badgeStatus(table, row);
        System.arraycopy(dokumen, 0, result, 1, dokumen.length);
        return result;
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
