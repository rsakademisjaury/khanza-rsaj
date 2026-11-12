package keuangan;

import com.fasterxml.jackson.databind.JsonNode;
import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import keuangan.BillingInacbg.Claim;
import keuangan.BillingInacbg.Context;

final class BillingInacbgPanel extends JPanel {
    private static final Color CANVAS = new Color(0xF1F5FB);
    private static final Color BORDER = new Color(0xDDE5F0);
    private static final Color TEXT = new Color(0x182942);
    private static final Color MUTED = new Color(0x536782);
    private static final Color BLUE = new Color(0x204EB6);
    private static final Color BLUE_SOFT = new Color(0xEAF1FF);
    private static final Color GREEN = new Color(0x166444);
    private static final Color GREEN_SOFT = new Color(0xE6F6EE);
    private static final Color AMBER = new Color(0x8A5314);
    private static final Color AMBER_SOFT = new Color(0xFFF2DA);
    private static final Color RED = new Color(0xA62F3B);
    private static final Color RED_SOFT = new Color(0xFCECEF);
    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter WAKTU = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", INDONESIA);

    private final BillingInacbg data;
    private final JDialog owner;
    private final JTabbedPane tabs;
    private final JTextField noRawat;
    private final DoubleSupplier total;
    private final IntConsumer styleTab;
    private final BooleanSupplier mayPrint;
    private final Consumer<String> openReport;
    private final JButton load = new JButton("Muat ulang");
    private final JButton print = new JButton("Cetak Billing BPJS");
    private final JLabel episode = label("", 13, Font.PLAIN, MUTED);
    private final JLabel status = label("", 12, Font.BOLD, MUTED);
    private final RoundedPanel badge = new RoundedPanel(22, BLUE_SOFT, null);
    private final Dashboard dashboard = new Dashboard();
    private JLabel notice;

    private Context active;
    private Context attemptedForSave;
    private Claim preview;
    private SwingWorker<Claim, Void> worker;
    private Runnable resumeSave;
    private boolean tabSelected;

    BillingInacbgPanel(JDialog owner, JTabbedPane tabs, JTextField noRawat, Connection db,
                       boolean ranap, DoubleSupplier total, IntConsumer styleTab,
                       BooleanSupplier mayPrint, Consumer<String> openReport) {
        super(new BorderLayout(0, 12));
        this.owner = owner;
        this.tabs = tabs;
        this.noRawat = noRawat;
        this.total = total;
        this.styleTab = styleTab;
        this.mayPrint = mayPrint;
        this.openReport = openReport;
        data = new BillingInacbg(db, ranap);
        setBackground(CANVAS);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        add(header(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(dashboard);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CANVAS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(11, 0));
        scroll.getVerticalScrollBar().setUI(new DashboardScrollBar());
        add(scroll, BorderLayout.CENTER);
        load.addActionListener(event -> startLoad());
        print.addActionListener(event -> printBilling());
        tabs.addChangeListener(event -> {
            boolean selected = tabs.getSelectedComponent() == this;
            boolean opened = selected && !tabSelected;
            tabSelected = selected;
            if (opened) refresh(true);
        });
        tabs.addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && tabSelected && tabs.isShowing()) {
                refresh(true);
            }
        });
        noRawat.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { scheduleRefresh(); }
            @Override public void removeUpdate(DocumentEvent e) { scheduleRefresh(); }
            @Override public void changedUpdate(DocumentEvent e) { scheduleRefresh(); }
        });
        refresh();
    }

    private void scheduleRefresh() {
        SwingUtilities.invokeLater(this::refresh);
    }

    private Context currentContext() throws SQLException {
        String rawat = noRawat.getText().trim();
        return rawat.isEmpty() ? null : data.upgradeContext(rawat);
    }

    void refresh() {
        refresh(false);
    }

    private void refresh(boolean opened) {
        try {
            Context context = currentContext();
            boolean changed = active == null ? context != null : !active.matches(context);
            if (changed) reset();
            active = context;
            if (context == null) {
                tabs.remove(this);
                return;
            }
            if (tabs.indexOfComponent(this) < 0) {
                tabs.addTab("INACBG", this);
                styleTab.accept(tabs.indexOfComponent(this));
            }
            if (worker != null) return;
            Claim saved = data.saved(context);
            Claim display = saved != null && (saved.finalGrouping || preview == null) ? saved : preview;
            render(context, display, saved != null && display == saved);
            if (tabSelected && tabs.isShowing() && (opened || changed) && context.sep != null
                    && (display == null || !display.finalGrouping)) {
                startLoad(context, saved);
            }
        } catch (SQLException ex) {
            showReadError("Data INACBG belum dapat dibaca.");
            log("baca", ex);
        }
    }

    private void reset() {
        if (worker != null) worker.cancel(true);
        worker = null;
        resumeSave = null;
        attemptedForSave = null;
        preview = null;
    }

    void beforeSave(Runnable validateAgain, Consumer<Save> save) {
        try {
            Context context = currentContext();
            if (context == null) {
                save.accept(null);
                return;
            }
            if (!context.matches(active)) {
                reset();
                active = context;
            }
            if (context.sep != null && (worker != null || preview == null || !preview.finalGrouping)
                    && !context.matches(attemptedForSave)) {
                resumeSave = validateAgain;
                startLoad();
                return;
            }
            attemptedForSave = null;
            Claim claim = preview == null ? Claim.pending(context, context.status) : preview;
            save.accept(new Save(context, claim));
        } catch (SQLException ex) {
            log("persiapan simpan", ex);
            JOptionPane.showMessageDialog(owner, "Data penjamin atau SEP gagal dibaca. Silakan ulangi Simpan.");
        }
    }

    private void startLoad() {
        try {
            Context requested = currentContext();
            if (requested == null || requested.sep == null) {
                Runnable retry = resumeSave;
                resumeSave = null;
                refresh();
                if (retry != null) retry.run();
                return;
            }
            if (requested.matches(active) && worker != null) return;
            if (!requested.matches(active)) {
                reset();
                active = requested;
            }
            startLoad(requested, data.saved(requested));
        } catch (SQLException ex) {
            resumeSave = null;
            showReadError("Data INACBG belum dapat dibaca.");
            log("muat", ex);
            if (!tabSelected) {
                JOptionPane.showMessageDialog(owner, "Data INACBG gagal dibaca. Silakan ulangi Simpan.");
            }
        }
    }

    private void startLoad(Context requested, Claim saved) {
        if (worker != null) return;
        if (saved != null && saved.finalGrouping) {
            preview = saved;
            render(requested, saved, true);
            resumeValidation(requested);
            return;
        }
        final String user = akses.getkode();
        final long session = koneksiDB.versiSesiDatabase();
        load.setEnabled(false);
        print.setEnabled(false);
        setBadge("Memuat…", BLUE, BLUE_SOFT);
        if (notice != null) notice.setText("Mengambil data klaim dari E-Klaim…");
        worker = new SwingWorker<Claim, Void>() {
            @Override protected Claim doInBackground() throws Exception {
                return BillingInacbg.fetch(requested, data.jenis);
            }

            @Override protected void done() {
                if (worker != this) return;
                worker = null;
                if (!tabs.isShowing() || !user.equals(akses.getkode())
                        || session != koneksiDB.versiSesiDatabase()) {
                    reset();
                    return;
                }
                try {
                    if (!requested.matches(currentContext())) {
                        reset();
                        refresh();
                        return;
                    }
                    try {
                        preview = get();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        preview = Claim.pending(requested, BillingInacbg.GAGAL_AKSES);
                    } catch (java.util.concurrent.ExecutionException ex) {
                        preview = Claim.pending(requested, BillingInacbg.GAGAL_AKSES);
                    }
                    if (preview.finalGrouping && requested.nota != null) {
                        if (saved == null) data.attachToOldNota(requested, preview, user);
                        else data.complete(requested, preview, user);
                    }
                    refresh();
                    resumeValidation(requested);
                } catch (SQLException ex) {
                    resumeSave = null;
                    showReadError("Data INACBG gagal dibaca atau dilengkapi. Klik Muat ulang untuk mencoba lagi.");
                    log("muat", ex);
                }
            }
        };
        worker.execute();
    }

    private void resumeValidation(Context context) {
        Runnable retry = resumeSave;
        resumeSave = null;
        if (retry != null) {
            attemptedForSave = context;
            try { retry.run(); }
            finally { attemptedForSave = null; }
        }
    }

    void lockForSave(Save save) throws SQLException { data.lockForSave(save.context); }

    void persist(Save save, String nota, double totalBilling, String user) throws SQLException {
        data.insert(save.context, save.claim, nota, totalBilling, user);
    }

    boolean removeForCancellation() {
        try {
            data.deleteForCancellation(noRawat.getText().trim());
            return true;
        } catch (SQLException ex) {
            log("hapus nota", ex);
            return false;
        }
    }

    private void printBilling() {
        if (!mayPrint.getAsBoolean()) return;
        try {
            Context context = currentContext();
            Claim saved = data.saved(context);
            if (saved == null || !saved.finalGrouping) {
                refresh();
                JOptionPane.showMessageDialog(owner, "Cetak Billing BPJS tersedia setelah billing dan grouping final tersimpan.");
                return;
            }
            openReport.accept(data.printPath(context, akses.getkode(), koneksiDB.PASHYBRIDWEB(),
                    Instant.now().getEpochSecond() + 300));
        } catch (SQLException ex) {
            log("cetak billing BPJS", ex);
            JOptionPane.showMessageDialog(owner, "Data nota BPJS gagal dibaca. Silakan coba lagi.");
        } catch (GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(owner, "Konfigurasi HybridWeb untuk cetak billing belum tersedia.");
        }
    }

    private JPanel header() {
        RoundedPanel header = new RoundedPanel(18, Color.WHITE, BORDER);
        header.setLayout(new BorderLayout(16, 4));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        header.add(label("Ringkasan Klaim", 20, Font.BOLD, TEXT), BorderLayout.CENTER);
        header.add(episode, BorderLayout.SOUTH);

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setOpaque(false);
        badge.setLayout(new BorderLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(5, 11, 5, 11));
        badge.add(status);
        badge.setAlignmentY(Component.CENTER_ALIGNMENT);
        badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 27));
        actions.add(badge);
        actions.add(Box.createHorizontalStrut(8));
        load.setAlignmentY(Component.CENTER_ALIGNMENT);
        load.setBackground(BLUE_SOFT);
        load.setForeground(BLUE);
        load.setFont(status.getFont());
        load.setMargin(new java.awt.Insets(5, 11, 5, 11));
        load.setToolTipText("Muat ulang ringkasan klaim");
        actions.add(load);
        actions.add(Box.createHorizontalStrut(6));
        print.setAlignmentY(Component.CENTER_ALIGNMENT);
        print.setBackground(BLUE_SOFT);
        print.setForeground(BLUE);
        print.setFont(status.getFont());
        print.setMargin(new java.awt.Insets(5, 10, 5, 10));
        print.setEnabled(false);
        actions.add(print);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void render(Context context, Claim claim, boolean saved) {
        boolean finalGrouping = claim != null && claim.finalGrouping;
        boolean hasDetails = claim != null && claim.waktuAmbil != null;
        String sep = claim == null || claim.sep == null ? context.sep : claim.sep;
        episode.setText(jenisLayanan() + "  ·  SEP " + display(sep)
                + (saved ? "  ·  Nota " + context.nota : ""));
        episode.setToolTipText(html(episode.getText()));
        load.setEnabled(context.sep != null || (saved && finalGrouping));
        print.setEnabled(mayPrint.getAsBoolean() && saved && finalGrouping);
        print.setToolTipText(print.isEnabled() ? "Buka rincian billing dengan ringkasan BPJS"
                : "Tersedia setelah billing dan grouping final tersimpan");
        if (finalGrouping) setBadge(saved ? "Final · Tersimpan" : "Grouping Final", GREEN, GREEN_SOFT);
        else if (context.sep == null) setBadge("SEP Belum Siap", AMBER, AMBER_SOFT);
        else if (hasDetails) setBadge("Belum Final", AMBER, AMBER_SOFT);
        else if (claim != null && BillingInacbg.GAGAL_AKSES.equals(claim.status)) setBadge("Gagal Memuat", RED, RED_SOFT);
        else if (claim != null) setBadge("Data Belum Tersedia", AMBER, AMBER_SOFT);
        else setBadge("Belum Dimuat", BLUE, BLUE_SOFT);

        dashboard.removeAll();
        notice = null;
        JPanel metrics = new JPanel(new GridLayout(1, 3, 10, 0));
        metrics.setOpaque(false);
        double totalRs = total.getAsDouble();
        BigDecimal totalBilling = claim != null && claim.totalBilling != null ? claim.totalBilling
                : Double.isFinite(totalRs) ? BigDecimal.valueOf(totalRs) : null;
        metrics.add(metric("Total Tagihan RS", totalBilling, Color.WHITE, TEXT));
        metrics.add(metric("Tarif INA-CBG", claim == null ? null : claim.tarif, BLUE_SOFT, BLUE));
        metrics.add(metric(isRanap() ? "Tambahan Biaya E-Klaim" : "Biaya Poli Eksekutif",
                claim == null ? null : isRanap() ? claim.tambahan : claim.poliEksekutif, Color.WHITE, TEXT));
        dashboard.block(metrics);

        if (!finalGrouping) {
            String message = claim == null ? context.status : claim.status;
            showNotice(message, hasDetails ? AMBER : MUTED, hasDetails ? AMBER_SOFT : Color.WHITE);
        }
        if (hasDetails) addDetails(context, claim);
        dashboard.revalidate();
        dashboard.repaint();
    }

    private void addDetails(Context context, Claim claim) {
        JsonNode rincian = claim.detail;
        JsonNode inacbg = rincian.path("inacbg");
        JsonNode idrg = rincian.path("idrg");
        JPanel grouping = section("Hasil Grouping");
        row(grouping, "INA-CBG", combine(claim.kodeCbg, BillingInacbg.text(inacbg.path("cbg"), "description")));
        row(grouping, "Status INA-CBG", statusLabel(claim.statusGrouping));
        BigDecimal baseTariff = BillingInacbg.amount(inacbg, "base_tariff");
        if (baseTariff != null) row(grouping, "Tarif Dasar INA-CBG", money(baseTariff));
        if (claim.kodeDrg != null && !claim.kodeDrg.isEmpty()) {
            row(grouping, "iDRG", combine(claim.kodeDrg, BillingInacbg.text(idrg, "drg_description")));
            row(grouping, "Status iDRG", statusLabel(BillingInacbg.text(idrg, "status_cd")));
            if (claim.tarifIdrg != null) row(grouping, "Tarif iDRG", money(claim.tarifIdrg));
        }
        addComponent(grouping, "Sub Acute", inacbg.path("sub_acute"));
        addComponent(grouping, "Chronic", inacbg.path("chronic"));
        for (JsonNode item : inacbg.path("special_cmg")) {
            addComponent(grouping, combine("Special CMG", BillingInacbg.text(item, "type")), item);
        }
        dashboard.block(grouping);

        if (isRanap()) {
            JPanel pelayanan = section("Kelas Perawatan & Pembayar");
            row(pelayanan, "Kelas Hak", kelas(context.kelasHak));
            row(pelayanan, "Kelas Kamar Ditempati", context.kelasKamar);
            row(pelayanan, "Pembayar Tambahan Biaya", claim.pembayar);
            dashboard.block(pelayanan);
        }

        JPanel pengiriman = section("Status Klaim & Pengiriman");
        row(pengiriman, "Status Klaim", statusLabel(claim.statusKlaim));
        row(pengiriman, "Kirim ke Kemenkes", statusLabel(BillingInacbg.text(rincian, "kemenkes_dc_status_cd")));
        row(pengiriman, "Kirim ke BPJS", statusLabel(BillingInacbg.text(rincian, "bpjs_dc_status_cd")));
        row(pengiriman, "Waktu Pengambilan", WAKTU.format(claim.waktuAmbil.toLocalDateTime()));
        dashboard.block(pengiriman);
    }

    private boolean isRanap() {
        return "1".equals(data.jenis);
    }

    private String jenisLayanan() {
        return isRanap() ? "Rawat Inap" : "Rawat Jalan";
    }

    private void setBadge(String message, Color ink, Color fill) {
        status.setText(message);
        status.setForeground(ink);
        badge.setBackground(fill);
        badge.repaint();
    }

    private void showReadError(String message) {
        load.setEnabled(active != null && active.sep != null);
        print.setEnabled(false);
        episode.setText(jenisLayanan());
        episode.setToolTipText(null);
        setBadge("Gagal Memuat", RED, RED_SOFT);
        dashboard.removeAll();
        showNotice(message, RED, RED_SOFT);
        dashboard.revalidate();
        dashboard.repaint();
    }

    private void showNotice(String text, Color ink, Color fill) {
        RoundedPanel box = new RoundedPanel(12, fill, BORDER);
        box.setLayout(new BorderLayout());
        box.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        notice = label(text, 13, Font.PLAIN, ink);
        box.add(notice);
        dashboard.block(box);
    }

    private static JPanel metric(String name, BigDecimal value, Color fill, Color accent) {
        RoundedPanel card = new RoundedPanel(16, fill, BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        card.add(label(name, 13, Font.BOLD, MUTED));
        card.add(Box.createVerticalStrut(5));
        card.add(label(money(value), 22, Font.BOLD, accent));
        return card;
    }

    private static JPanel section(String title) {
        RoundedPanel section = new RoundedPanel(16, Color.WHITE, BORDER);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createEmptyBorder(12, 15, 4, 15));
        section.add(label(title, 15, Font.BOLD, TEXT));
        section.add(Box.createVerticalStrut(7));
        return section;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel();
        label.putClientProperty("html.disable", Boolean.TRUE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setText(text);
        label.setForeground(color);
        Font font = UIManager.getFont("Label.font");
        if (font == null) font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        label.setFont(font.deriveFont(style, (float) size));
        return label;
    }

    private static void row(JPanel section, String name, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(6, 0, 6, 0)));
        JLabel key = label(name, 13, Font.PLAIN, MUTED);
        key.setPreferredSize(new Dimension(184, key.getPreferredSize().height));
        row.add(key, BorderLayout.WEST);
        JLabel content = label(display(value), 13, Font.PLAIN, TEXT);
        if (value != null && !value.isEmpty()) content.setToolTipText(html(value));
        row.add(content, BorderLayout.CENTER);
        section.add(row);
    }

    private static void addComponent(JPanel section, String label, JsonNode node) {
        if (!node.isObject()) return;
        row(section, label, combine(combine(BillingInacbg.text(node, "code"), BillingInacbg.text(node, "description")),
                money(BillingInacbg.amount(node, "tariff"))));
    }

    private static String kelas(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceFirst("^kelas[_ ]", "");
        if (normalized.matches("[1-3]")) return "Kelas " + normalized;
        return "vip".equals(normalized) || "vvip".equals(normalized) ? normalized.toUpperCase(Locale.ROOT) : value;
    }

    private static String statusLabel(String value) {
        if (value == null) return null;
        switch (value.toLowerCase(Locale.ROOT)) {
            case "final": return "Final";
            case "draft": return "Draf";
            case "sent": return "Terkirim";
            case "unsent": return "Belum terkirim";
            default: return value;
        }
    }

    private static String display(String value) {
        return value == null || value.isEmpty() ? "—" : value;
    }

    private static String combine(String first, String second) {
        if (first == null || first.isEmpty()) return second;
        return second == null || second.isEmpty() ? first : first + " · " + second;
    }

    private static final class RoundedPanel extends JPanel {
        private final int radius;
        private final Color outline;

        RoundedPanel(int radius, Color fill, Color outline) {
            this.radius = radius;
            this.outline = outline;
            setOpaque(false);
            setBackground(fill);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                if (outline != null) {
                    g.setColor(outline);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                }
            } finally {
                g.dispose();
            }
        }
    }

    private static final class Dashboard extends JPanel implements Scrollable {
        Dashboard() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        void block(JPanel panel) {
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
            add(panel);
            add(Box.createVerticalStrut(10));
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int direction, int movement) { return 18; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int direction, int movement) {
            return Math.max(18, visible.height - 18);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class DashboardScrollBar extends BasicScrollBarUI {
        @Override protected JButton createDecreaseButton(int orientation) { return spacer(); }
        @Override protected JButton createIncreaseButton(int orientation) { return spacer(); }

        @Override protected void paintTrack(Graphics g, JComponent component, Rectangle bounds) {
            g.setColor(CANVAS);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override protected void paintThumb(Graphics graphics, JComponent component, Rectangle bounds) {
            if (bounds.isEmpty() || !component.isEnabled()) return;
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isThumbRollover() ? MUTED : new Color(0xADBED5));
                g.fillRoundRect(bounds.x + 2, bounds.y, bounds.width - 4, bounds.height, 7, 7);
            } finally {
                g.dispose();
            }
        }

        private static JButton spacer() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setFocusable(false);
            button.setBorderPainted(false);
            return button;
        }
    }

    private static String html(String value) {
        return value == null || value.isEmpty() ? "—" : value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String money(BigDecimal value) {
        if (value == null) return "—";
        NumberFormat format = NumberFormat.getNumberInstance(INDONESIA);
        format.setMaximumFractionDigits(2);
        return "Rp " + format.format(value);
    }

    private static void log(String action, SQLException ex) {
        System.err.println("Billing INACBG " + action + " (SQLState " + ex.getSQLState() + ", kode " + ex.getErrorCode() + ")");
    }

    static final class Save {
        private final Context context;
        private final Claim claim;

        Save(Context context, Claim claim) {
            this.context = context;
            this.claim = claim;
        }
    }
}
