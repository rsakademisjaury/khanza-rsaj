/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media).
 */

package simrskhanza;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Halaman Tentang SIMRS Khanza dengan pencarian riwayat pembaruan.
 *
 * @author perpustakaan
 */
public class DlgAbout extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Color PAGE_BACKGROUND = new Color(238, 244, 247);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(7, 137, 178);
    private static final Color PRIMARY_DARK = new Color(5, 105, 151);
    private static final Color PRIMARY_SOFT = new Color(229, 246, 251);
    private static final Color TEXT_DARK = new Color(24, 52, 69);
    private static final Color TEXT_MUTED = new Color(104, 125, 138);
    private static final Color BORDER = new Color(218, 230, 236);
    private static final Color INPUT_BACKGROUND = new Color(248, 251, 252);
    private static final Color SUCCESS = new Color(43, 174, 102);
    private static final Color WARNING = new Color(232, 161, 35);
    private static final Color DANGER = new Color(231, 111, 81);
    private static final Color FEATURE = new Color(108, 123, 217);
    private static final String FONT_REGULAR = "Segoe UI";
    private static final String FONT_SEMIBOLD = "Segoe UI Semibold";
    private static final int INITIAL_VISIBLE_UPDATES = 7;
    private static final int LOAD_MORE_AMOUNT = 10;

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "(?i)^\\s*Versi\\s*:\\s*(.+?)\\s*$");
    private static final Pattern UPDATE_HEADER_PATTERN = Pattern.compile(
            "(?i)^\\s*Update\\s*:?\\s*\\*?\\s*$");

    private final List<UpdateEntry> allUpdates = new ArrayList<UpdateEntry>();
    private final List<UpdateEntry> filteredUpdates = new ArrayList<UpdateEntry>();

    private PromptTextField searchField;
    private ModernButton searchButton;
    private ModernButton loadMoreButton;
    private JLabel resultCountLabel;
    private JLabel showingLabel;
    private JPanel historyListPanel;
    private JScrollPane historyScrollPane;
    private JComboBox<String> sortCombo;
    private PillToggleButton allFilterButton;
    private PillToggleButton fixFilterButton;
    private PillToggleButton featureFilterButton;
    private PillToggleButton performanceFilterButton;
    private FilterMode activeFilter = FilterMode.ALL;
    private int visibleUpdateLimit = INITIAL_VISIBLE_UPDATES;
    private String applicationVersion = "-";
    private Point dragOffset;

    public DlgAbout(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        loadUpdateHistory();
        applicationVersion = resolveApplicationVersion();
        buildModernInterface();
        configureDialog(parent);
    }

    private void configureDialog(java.awt.Frame parent) {
        setMinimumSize(new Dimension(940, 610));

        try {
            Rectangle available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();
            int width = Math.min(1500, Math.max(940, available.width - 36));
            int height = Math.min(840, Math.max(610, available.height - 36));
            width = Math.min(width, available.width);
            height = Math.min(height, available.height);
            setSize(width, height);
        } catch (Exception ex) {
            setSize(1280, 720);
        }

        setLocationRelativeTo(parent);
        installCloseShortcuts();
        getRootPane().setDefaultButton(searchButton);
    }

    private void installCloseShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-about");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK), "close-about");
        getRootPane().getActionMap().put("close-about", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void buildModernInterface() {
        modernHost.removeAll();
        modernHost.setBackground(PAGE_BACKGROUND);
        modernHost.setLayout(new BorderLayout());

        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(true);
        page.setBackground(PAGE_BACKGROUND);
        page.setBorder(new EmptyBorder(12, 18, 12, 18));

        page.add(createHeroPanel(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 0));
        content.setOpaque(false);
        content.add(createSidebar(), BorderLayout.WEST);
        content.add(createHistoryPanel(), BorderLayout.CENTER);
        page.add(content, BorderLayout.CENTER);

        modernHost.add(page, BorderLayout.CENTER);
        modernHost.revalidate();
        modernHost.repaint();
        applyFilters(true);
    }

    private JPanel createHeroPanel() {
        HeroPanel hero = new HeroPanel();
        hero.setPreferredSize(new Dimension(0, 68));
        hero.setLayout(new BorderLayout(14, 0));
        hero.setBorder(new EmptyBorder(8, 15, 8, 10));

        JPanel identity = transparentPanel(new BorderLayout(11, 0));
        InfoMark infoMark = new InfoMark();
        infoMark.setPreferredSize(new Dimension(42, 42));
        identity.add(infoMark, BorderLayout.WEST);

        JPanel titles = transparentPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Box.createVerticalGlue());
        JLabel title = createLabel("Tentang SIMRS Khanza", Font.BOLD, 21, Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        JLabel subtitle = createLabel(
                "Informasi aplikasi, pengembang, dan seluruh riwayat pembaruan dalam satu halaman.",
                Font.PLAIN, 11, new Color(220, 244, 250));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(subtitle);
        titles.add(Box.createVerticalGlue());
        identity.add(titles, BorderLayout.CENTER);
        hero.add(identity, BorderLayout.CENTER);

        JPanel right = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        RoundedPanel versionBox = new RoundedPanel(11,
                new Color(255, 255, 255, 38), new Color(255, 255, 255, 22));
        versionBox.setPreferredSize(new Dimension(205, 48));
        versionBox.setLayout(new BoxLayout(versionBox, BoxLayout.Y_AXIS));
        versionBox.setBorder(new EmptyBorder(6, 14, 6, 14));
        JLabel versionCaption = createLabel("VERSI SAAT INI", Font.PLAIN, 9,
                new Color(220, 244, 250));
        versionCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel versionValue = createLabel(applicationVersion, Font.BOLD, 14, Color.WHITE);
        versionValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionBox.add(versionCaption);
        versionBox.add(Box.createVerticalStrut(1));
        versionBox.add(versionValue);
        right.add(versionBox);

        ModernButton closeButton = new ModernButton("", new CloseIcon(),
                new Color(255, 255, 255, 38), new Color(255, 255, 255, 70), Color.WHITE);
        closeButton.setToolTipText("Tutup (Esc)");
        closeButton.setPreferredSize(new Dimension(46, 48));
        closeButton.addActionListener((ActionEvent e) -> dispose());
        right.add(closeButton);
        hero.add(right, BorderLayout.EAST);

        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), DlgAbout.this);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null) {
                    return;
                }
                Point screen = e.getLocationOnScreen();
                setLocation(screen.x - dragOffset.x, screen.y - dragOffset.y);
            }
        };
        hero.addMouseListener(dragListener);
        hero.addMouseMotionListener(dragListener);
        identity.addMouseListener(dragListener);
        identity.addMouseMotionListener(dragListener);
        titles.addMouseListener(dragListener);
        titles.addMouseMotionListener(dragListener);

        return hero;
    }

    private JPanel createSidebar() {
        JPanel sidebar = transparentPanel(new BorderLayout(0, 12));
        sidebar.setPreferredSize(new Dimension(380, 0));
        sidebar.add(createApplicationCard(), BorderLayout.NORTH);
        sidebar.add(createDeveloperCard(), BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel createApplicationCard() {
        RoundedPanel card = createCardPanel();
        card.setPreferredSize(new Dimension(380, 264));
        card.setBorder(new EmptyBorder(14, 20, 14, 20));
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = createSectionHeader("A", "Tentang Aplikasi",
                "Konfigurasi aktif pada komputer ini", PRIMARY, PRIMARY_SOFT);
        card.add(header, BorderLayout.NORTH);

        JPanel rows = transparentPanel(new GridBagLayout());
        addInfoRow(rows, 0, "Versi aplikasi", applicationVersion, false);
        addInfoRow(rows, 1, "Update aplikasi", "19 Juni 2024", false);
        addInfoRow(rows, 2, "Database server", "●  Aktif", true);
        addInfoRow(rows, 3, "Database", "sik", false);
        addInfoRow(rows, 4, "File konfigurasi", "database.xml", false);
        addInfoRow(rows, 5, "Port bawaan", "3306", false);
        card.add(rows, BorderLayout.CENTER);

        RoundedPanel notice = new RoundedPanel(9, new Color(255, 247, 230), null);
        notice.setLayout(new BorderLayout(8, 0));
        notice.setBorder(new EmptyBorder(7, 10, 7, 10));
        JLabel alert = createLabel("!", Font.BOLD, 11, Color.WHITE);
        alert.setHorizontalAlignment(SwingConstants.CENTER);
        alert.setOpaque(true);
        alert.setBackground(WARNING);
        alert.setPreferredSize(new Dimension(19, 19));
        notice.add(alert, BorderLayout.WEST);
        notice.add(createLabel("Koneksi tetap mengikuti database.xml", Font.PLAIN, 11,
                new Color(137, 96, 20)), BorderLayout.CENTER);
        card.add(notice, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDeveloperCard() {
        RoundedPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.add(createSectionHeader("D", "Tentang Developer",
                "Tim pengembang dan kanal resmi", FEATURE, new Color(239, 241, 255)),
                BorderLayout.NORTH);

        JPanel body = transparentPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel identity = transparentPanel(new BorderLayout(12, 0));
        Avatar avatar = new Avatar("KS", PRIMARY);
        avatar.setPreferredSize(new Dimension(54, 54));
        identity.add(avatar, BorderLayout.WEST);
        JPanel identityText = transparentPanel();
        identityText.setLayout(new BoxLayout(identityText, BoxLayout.Y_AXIS));
        identityText.add(Box.createVerticalGlue());
        JLabel developerName = createLabel("Khanza Soft Media", Font.BOLD, 15, TEXT_DARK);
        developerName.setAlignmentX(Component.LEFT_ALIGNMENT);
        identityText.add(developerName);
        identityText.add(Box.createVerticalStrut(4));
        JLabel developerRole = createLabel("IT Maintenance, Software & Web Developer",
                Font.PLAIN, 12, TEXT_MUTED);
        developerRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        identityText.add(developerRole);
        identityText.add(Box.createVerticalGlue());
        identity.add(identityText, BorderLayout.CENTER);
        identity.setAlignmentX(Component.LEFT_ALIGNMENT);
        identity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        body.add(identity);
        body.add(Box.createVerticalStrut(11));

        RoundedPanel contactBox = new RoundedPanel(9, INPUT_BACKGROUND, BORDER);
        contactBox.setLayout(new BorderLayout());
        contactBox.setBorder(new EmptyBorder(8, 10, 8, 10));
        JTextArea contact = createReadOnlyText(
                "Telepon: 08562675039\n"
                + "Pengembang: Windiarto Nugroho, Dewi Ekawati,\n"
                + "YASKI, dan tim\n"
                + "Blog: elkhanza.wordpress.com\n"
                + "simrskhanza.weebly.com • yaski.or.id\n"
                + "Email: khanza_media@yahoo.com\n"
                + "khanzasoftmedia@gmail.com\n"
                + "GitHub: github.com/mas-elkhanza • IG: simrskhanza",
                12, TEXT_DARK);
        contactBox.add(contact, BorderLayout.CENTER);
        contactBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        contactBox.setPreferredSize(new Dimension(0, 164));
        contactBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 164));
        body.add(contactBox);
        body.add(Box.createVerticalStrut(10));

        JLabel contributorTitle = createLabel("KONTRIBUTOR & DONATUR", Font.BOLD, 11, TEXT_MUTED);
        contributorTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(contributorTitle);
        body.add(Box.createVerticalStrut(5));
        JTextArea contributors = createReadOnlyText(
                "RS Jeumpa Pontianak, RS Sadewa Yogyakarta, RS Puri Husada Sleman, "
                + "RS Pelita Insani Martapura, RS Assyifa Manna, RS Arvita Bunda Sleman, "
                + "RS Permata Medika Kebumen, RS Handayani Lampung Utara, "
                + "RS Haji Kamino Way Kanan, RS PKU Jatinom Klaten, dan lainnya.",
                12, TEXT_MUTED);
        contributors.setRows(5);
        contributors.setPreferredSize(new Dimension(0, 96));
        contributors.setAlignmentX(Component.LEFT_ALIGNMENT);
        contributors.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        body.add(contributors);
        body.add(Box.createVerticalGlue());

        RoundedPanel donation = new RoundedPanel(9, new Color(255, 247, 230), null);
        donation.setLayout(new BorderLayout());
        donation.setBorder(new EmptyBorder(7, 10, 7, 10));
        donation.add(createLabel("Donasi: BSI 1015369872 • kode 451 • a.n. Windiarto",
                Font.PLAIN, 11, new Color(137, 96, 20)), BorderLayout.CENTER);
        donation.setAlignmentX(Component.LEFT_ALIGNMENT);
        donation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        body.add(donation);

        body.setBorder(new EmptyBorder(0, 0, 0, 3));
        JScrollPane developerScroll = new JScrollPane(body);
        developerScroll.setBorder(null);
        developerScroll.setOpaque(false);
        developerScroll.getViewport().setOpaque(false);
        developerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        developerScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        developerScroll.getVerticalScrollBar().setUnitIncrement(12);
        card.add(developerScroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHistoryPanel() {
        RoundedPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel top = transparentPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel titleRow = transparentPanel(new BorderLayout(12, 0));
        JPanel titleArea = transparentPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        JLabel title = createLabel("Riwayat Pembaruan", Font.BOLD, 20, TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(3));
        JLabel subtitle = createLabel("Cari berdasarkan nomor versi atau nama fitur/perbaikan.",
                Font.PLAIN, 11, TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleArea.add(subtitle);
        titleRow.add(titleArea, BorderLayout.CENTER);

        sortCombo = new JComboBox<String>(new String[]{"Terbaru", "Terlama"});
        sortCombo.setFont(uiFont(Font.PLAIN, 11));
        sortCombo.setForeground(TEXT_DARK);
        sortCombo.setBackground(Color.WHITE);
        sortCombo.setFocusable(false);
        sortCombo.setPreferredSize(new Dimension(112, 28));
        sortCombo.setMinimumSize(new Dimension(112, 28));
        sortCombo.setMaximumSize(new Dimension(112, 28));
        sortCombo.addActionListener((ActionEvent e) -> {
            visibleUpdateLimit = INITIAL_VISIBLE_UPDATES;
            applyFilters(true);
        });
        JPanel sortWrapper = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        sortWrapper.add(sortCombo);
        titleRow.add(sortWrapper, BorderLayout.EAST);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        top.add(titleRow);
        top.add(Box.createVerticalStrut(9));

        JPanel searchRow = transparentPanel(new BorderLayout(10, 0));
        RoundedPanel searchBox = new RoundedPanel(11, INPUT_BACKGROUND, new Color(199, 216, 225));
        searchBox.setLayout(new BorderLayout(9, 0));
        searchBox.setBorder(new EmptyBorder(2, 12, 2, 9));
        JLabel searchIcon = new JLabel(new SearchIcon(new Color(112, 136, 150), 15));
        searchBox.add(searchIcon, BorderLayout.WEST);
        searchField = new PromptTextField(
                "Ketik versi atau fitur, misalnya 26.0718 atau hasil laboratorium");
        searchField.setFont(uiFont(Font.PLAIN, 12));
        searchField.setForeground(TEXT_DARK);
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.addActionListener((ActionEvent e) -> performSearch());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch();
            }
        });
        searchBox.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchBox, BorderLayout.CENTER);

        searchButton = new ModernButton("Cari", new SearchIcon(Color.WHITE, 14),
                PRIMARY, PRIMARY_DARK, Color.WHITE);
        searchButton.setPreferredSize(new Dimension(104, 34));
        searchButton.addActionListener((ActionEvent e) -> performSearch());
        searchRow.add(searchButton, BorderLayout.EAST);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        top.add(searchRow);
        top.add(Box.createVerticalStrut(9));

        JPanel filterRow = transparentPanel(new BorderLayout(10, 0));
        JPanel filterButtons = transparentPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        ButtonGroup filterGroup = new ButtonGroup();

        allFilterButton = createFilterButton(FilterMode.ALL, filterGroup);
        fixFilterButton = createFilterButton(FilterMode.FIX, filterGroup);
        featureFilterButton = createFilterButton(FilterMode.FEATURE, filterGroup);
        performanceFilterButton = createFilterButton(FilterMode.PERFORMANCE, filterGroup);
        filterButtons.add(allFilterButton);
        filterButtons.add(fixFilterButton);
        filterButtons.add(featureFilterButton);
        filterButtons.add(performanceFilterButton);
        allFilterButton.setSelected(true);
        filterRow.add(filterButtons, BorderLayout.CENTER);

        resultCountLabel = createLabel("", Font.PLAIN, 11, TEXT_MUTED);
        resultCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        filterRow.add(resultCountLabel, BorderLayout.EAST);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        top.add(filterRow);
        refreshFilterButtonLabels();
        card.add(top, BorderLayout.NORTH);

        historyListPanel = new JPanel();
        historyListPanel.setOpaque(false);
        historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));
        historyScrollPane = new JScrollPane(historyListPanel);
        historyScrollPane.setBorder(null);
        historyScrollPane.setOpaque(false);
        historyScrollPane.getViewport().setOpaque(false);
        historyScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        card.add(historyScrollPane, BorderLayout.CENTER);

        JPanel footer = transparentPanel(new BorderLayout(12, 0));
        footer.setBorder(new EmptyBorder(9, 0, 0, 0));
        showingLabel = createLabel("", Font.PLAIN, 10, TEXT_MUTED);
        footer.add(showingLabel, BorderLayout.CENTER);

        loadMoreButton = new ModernButton("Tampilkan lebih banyak", null,
                new Color(244, 249, 251), new Color(229, 242, 247), PRIMARY_DARK);
        loadMoreButton.setPreferredSize(new Dimension(184, 32));
        loadMoreButton.addActionListener((ActionEvent e) -> {
            visibleUpdateLimit += LOAD_MORE_AMOUNT;
            applyFilters(false);
        });
        footer.add(loadMoreButton, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private PillToggleButton createFilterButton(final FilterMode mode, ButtonGroup group) {
        PillToggleButton button = new PillToggleButton(mode.label, mode.color);
        button.addActionListener((ActionEvent e) -> {
            activeFilter = mode;
            visibleUpdateLimit = INITIAL_VISIBLE_UPDATES;
            applyFilters(true);
        });
        group.add(button);
        return button;
    }

    private void performSearch() {
        visibleUpdateLimit = INITIAL_VISIBLE_UPDATES;
        applyFilters(true);
    }

    private void applyFilters(boolean resetScroll) {
        if (historyListPanel == null || searchField == null) {
            return;
        }

        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String[] terms = query.length() == 0 ? new String[0] : query.split("\\s+");

        filteredUpdates.clear();
        for (UpdateEntry entry : allUpdates) {
            if (activeFilter != FilterMode.ALL && entry.category.filterMode != activeFilter) {
                continue;
            }
            if (entry.matches(terms)) {
                filteredUpdates.add(entry);
            }
        }

        Collections.sort(filteredUpdates, new Comparator<UpdateEntry>() {
            @Override
            public int compare(UpdateEntry left, UpdateEntry right) {
                int compared = compareVersions(left.version, right.version);
                if (compared == 0) {
                    compared = Integer.compare(left.originalOrder, right.originalOrder);
                }
                return sortCombo != null && sortCombo.getSelectedIndex() == 1
                        ? compared : -compared;
            }
        });

        historyListPanel.removeAll();
        int shown = Math.min(visibleUpdateLimit, filteredUpdates.size());
        if (shown == 0) {
            historyListPanel.add(createEmptyState(query));
        } else {
            for (int i = 0; i < shown; i++) {
                UpdateEntry entry = filteredUpdates.get(i);
                boolean latest = entry.originalOrder == 0;
                JPanel updateCard = createUpdateCard(entry, latest);
                updateCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                historyListPanel.add(updateCard);
                if (i < shown - 1) {
                    historyListPanel.add(Box.createVerticalStrut(9));
                }
            }
        }

        resultCountLabel.setText(filteredUpdates.size() + " pembaruan ditemukan");
        showingLabel.setText("Menampilkan " + shown + " dari "
                + filteredUpdates.size() + " riwayat pembaruan");
        loadMoreButton.setVisible(shown < filteredUpdates.size());
        refreshFilterButtonLabels();
        historyListPanel.revalidate();
        historyListPanel.repaint();

        if (resetScroll) {
            SwingUtilities.invokeLater(() -> historyScrollPane.getVerticalScrollBar().setValue(0));
        }
    }

    private JPanel createUpdateCard(UpdateEntry entry, boolean latest) {
        Color accent = entry.category.color;
        Color background = latest ? new Color(247, 252, 253) : Color.WHITE;
        RoundedPanel card = new RoundedPanel(11, background, latest
                ? new Color(208, 232, 240) : BORDER);
        card.setLayout(new BorderLayout(11, 0));
        card.setBorder(new EmptyBorder(12, 13, 12, 13));

        JPanel marker = new JPanel();
        marker.setOpaque(true);
        marker.setBackground(accent);
        marker.setPreferredSize(new Dimension(5, 1));
        card.add(marker, BorderLayout.WEST);

        JPanel content = transparentPanel(new BorderLayout(0, 6));
        JPanel header = transparentPanel(new BorderLayout(10, 0));

        JPanel versionArea = transparentPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        if (latest) {
            BadgeLabel newest = new BadgeLabel("VERSI TERBARU", PRIMARY, Color.WHITE);
            versionArea.add(newest);
        }
        versionArea.add(createLabel(entry.version, Font.BOLD, 13, TEXT_DARK));
        header.add(versionArea, BorderLayout.CENTER);
        header.add(new BadgeLabel(entry.category.label, entry.category.softColor,
                entry.category.darkColor), BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        String detailsText = entry.detailsText();
        JPanel detailRow = transparentPanel(new BorderLayout(7, 0));
        JLabel check = new JLabel(new CheckIcon(SUCCESS, 13));
        check.setVerticalAlignment(SwingConstants.TOP);
        detailRow.add(check, BorderLayout.WEST);
        JTextArea details = createReadOnlyText(detailsText, 11, TEXT_DARK);
        details.setForeground(TEXT_DARK);
        detailRow.add(details, BorderLayout.CENTER);
        content.add(detailRow, BorderLayout.CENTER);
        card.add(content, BorderLayout.CENTER);

        int estimatedRows = Math.max(1, Math.min(4, (detailsText.length() / 105) + 1));
        int height = 59 + (estimatedRows * 16);
        card.setPreferredSize(new Dimension(0, height));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return card;
    }

    private JPanel createEmptyState(String query) {
        JPanel empty = transparentPanel();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBorder(new EmptyBorder(55, 20, 20, 20));

        JLabel icon = new JLabel(new SearchIcon(new Color(164, 181, 191), 30));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        empty.add(icon);
        empty.add(Box.createVerticalStrut(12));
        JLabel title = createLabel("Riwayat pembaruan tidak ditemukan", Font.BOLD, 14, TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        empty.add(title);
        empty.add(Box.createVerticalStrut(5));
        String hint = query.length() == 0
                ? "Coba pilih kategori pembaruan yang lain."
                : "Coba gunakan nomor versi atau kata kunci yang lebih singkat.";
        JLabel description = createLabel(hint, Font.PLAIN, 11, TEXT_MUTED);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        empty.add(description);
        empty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        return empty;
    }

    private void refreshFilterButtonLabels() {
        if (allFilterButton == null) {
            return;
        }
        allFilterButton.setText("Semua  " + allUpdates.size());
        fixFilterButton.setText("Perbaikan  " + countCategory(UpdateCategory.FIX));
        featureFilterButton.setText("Fitur  " + countCategory(UpdateCategory.FEATURE));
        performanceFilterButton.setText("Performa  " + countCategory(UpdateCategory.PERFORMANCE));
    }

    private int countCategory(UpdateCategory category) {
        int count = 0;
        for (UpdateEntry entry : allUpdates) {
            if (entry.category == category) {
                count++;
            }
        }
        return count;
    }

    private void loadUpdateHistory() {
        allUpdates.clear();
        File updateFile = new File(System.getProperty("user.dir"), "settingupdate/update.txt");
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(updateFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            parseUpdateHistory(content.toString());
        } catch (Exception ex) {
            System.out.println("Notif Setting : " + ex);
        }

        if (allUpdates.isEmpty()) {
            List<String> detail = new ArrayList<String>();
            detail.add("File settingupdate/update.txt belum ditemukan atau belum berisi riwayat update.");
            allUpdates.add(new UpdateEntry("Riwayat belum tersedia", detail,
                    UpdateCategory.GENERAL, 0));
        }
    }

    private void parseUpdateHistory(String rawContent) {
        String version = null;
        List<String> details = new ArrayList<String>();
        int order = 0;

        String[] lines = rawContent.replace("\r", "").split("\n");
        for (String originalLine : lines) {
            String line = originalLine == null ? "" : originalLine.trim();
            Matcher versionMatcher = VERSION_PATTERN.matcher(line);

            if (versionMatcher.matches()) {
                if (version != null) {
                    addUpdateEntry(version, details, order++);
                }
                version = versionMatcher.group(1).trim();
                details = new ArrayList<String>();
                continue;
            }

            if (line.matches("^-{3,}$")) {
                if (version != null) {
                    addUpdateEntry(version, details, order++);
                    version = null;
                    details = new ArrayList<String>();
                }
                continue;
            }

            if (version == null || line.length() == 0 || UPDATE_HEADER_PATTERN.matcher(line).matches()) {
                continue;
            }

            String cleaned = line.replaceFirst("^[\\-\\*•]+\\s*", "").trim();
            if (cleaned.length() > 0) {
                details.add(cleaned);
            }
        }

        if (version != null) {
            addUpdateEntry(version, details, order);
        }
    }

    private void addUpdateEntry(String version, List<String> details, int order) {
        List<String> safeDetails = new ArrayList<String>(details);
        if (safeDetails.isEmpty()) {
            safeDetails.add("Informasi pembaruan tidak dirinci.");
        }
        UpdateCategory category = detectCategory(safeDetails);
        allUpdates.add(new UpdateEntry(version, safeDetails, category, order));
    }

    private UpdateCategory detectCategory(List<String> details) {
        StringBuilder text = new StringBuilder();
        for (String detail : details) {
            text.append(detail).append(' ');
        }
        String value = text.toString().toLowerCase(Locale.ROOT);

        if (containsAny(value, "mempercepat", "optimasi", "optimalisasi", "performa",
                "loading", "kecepatan", "splash")) {
            return UpdateCategory.PERFORMANCE;
        }
        if (containsAny(value, "penambahan", "menambahkan", "tambah fitur", "fitur baru",
                "menu baru", "integrasi")) {
            return UpdateCategory.FEATURE;
        }
        if (containsAny(value, "perbaikan", "memperbaiki", "koreksi", "bug", "error", "fix")) {
            return UpdateCategory.FIX;
        }
        return UpdateCategory.GENERAL;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String resolveApplicationVersion() {
        try {
            Class<?> mainClass = Class.forName("simrskhanza.frmUtama", false,
                    getClass().getClassLoader());
            Field versionField;
            try {
                versionField = mainClass.getField("versionlocal");
            } catch (NoSuchFieldException ex) {
                versionField = mainClass.getDeclaredField("versionlocal");
                versionField.setAccessible(true);
            }
            Object value = versionField.get(null);
            if (value != null && value.toString().trim().length() > 0) {
                return value.toString().trim();
            }
        } catch (Exception ex) {
            // Gunakan versi terbaru dari update.txt sebagai fallback.
        }

        if (!allUpdates.isEmpty()) {
            return allUpdates.get(0).version;
        }
        return "-";
    }

    private int compareVersions(String left, String right) {
        String[] leftParts = left.replaceAll("[^0-9.]", "").split("\\.");
        String[] rightParts = right.replaceAll("[^0-9.]", "").split("\\.");
        int length = Math.max(leftParts.length, rightParts.length);

        for (int i = 0; i < length; i++) {
            long leftValue = parseVersionPart(leftParts, i);
            long rightValue = parseVersionPart(rightParts, i);
            if (leftValue != rightValue) {
                return leftValue < rightValue ? -1 : 1;
            }
        }
        return 0;
    }

    private long parseVersionPart(String[] parts, int index) {
        if (index >= parts.length || parts[index].length() == 0) {
            return 0L;
        }
        try {
            return Long.parseLong(parts[index]);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private RoundedPanel createCardPanel() {
        RoundedPanel card = new RoundedPanel(15, CARD_BACKGROUND, new Color(226, 235, 240));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        return card;
    }

    private JPanel createSectionHeader(String initial, String title, String subtitle,
            Color accent, Color softBackground) {
        JPanel header = transparentPanel(new BorderLayout(12, 0));
        Avatar icon = new Avatar(initial, accent);
        icon.setBackgroundColor(softBackground);
        icon.setPreferredSize(new Dimension(38, 38));
        header.add(icon, BorderLayout.WEST);

        JPanel text = transparentPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLabel = createLabel(title, Font.BOLD, 16, TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitleLabel = createLabel(subtitle, Font.PLAIN, 11, TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));
        text.add(subtitleLabel);
        header.add(text, BorderLayout.CENTER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(232, 239, 243)));
        header.setPreferredSize(new Dimension(0, 56));
        return header;
    }

    private void addInfoRow(JPanel panel, int row, String labelText, String valueText,
            boolean successValue) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.weightx = 1.0;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(3, 2, 3, 8);
        panel.add(createLabel(labelText, Font.PLAIN, 12, TEXT_MUTED), labelConstraints);

        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.gridx = 1;
        valueConstraints.gridy = row;
        valueConstraints.weightx = 0.0;
        valueConstraints.anchor = GridBagConstraints.EAST;
        valueConstraints.insets = new Insets(3, 8, 3, 2);
        panel.add(createLabel(valueText, successValue ? Font.BOLD : Font.PLAIN, 12,
                successValue ? SUCCESS : TEXT_DARK), valueConstraints);
    }

    private JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private JLabel createLabel(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont(style, size));
        label.setForeground(color);
        return label;
    }

    private JTextArea createReadOnlyText(String text, int size, Color color) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFocusable(true);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(null);
        area.setFont(uiFont(Font.PLAIN, size));
        area.setForeground(color);
        area.setRows(Math.max(1, text.split("\\n", -1).length));
        return area;
    }

    private Font uiFont(int style, int size) {
        return appFont(style, size);
    }

    private static Font appFont(int style, int size) {
        boolean semibold = (style & Font.BOLD) == Font.BOLD;
        int resolvedStyle = (style & Font.ITALIC) == Font.ITALIC ? Font.ITALIC : Font.PLAIN;
        return new Font(semibold ? FONT_SEMIBOLD : FONT_REGULAR, resolvedStyle, size);
    }

    /**
     * Komponen dasar yang disimpan oleh NetBeans Form Editor. Seluruh isi modern
     * dipasang secara programatis agar responsif saat dialog diperbesar/diperkecil.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modernHost = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("::[ About Program ]::");
        setUndecorated(true);
        setResizable(false);

        modernHost.setBackground(new java.awt.Color(238, 244, 247));
        modernHost.setName("modernHost"); // NOI18N
        modernHost.setLayout(new java.awt.BorderLayout());
        getContentPane().add(modernHost, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        EventQueue.invokeLater(() -> {
            DlgAbout dialog = new DlgAbout(new JFrame(), false);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel modernHost;
    // End of variables declaration//GEN-END:variables

    private enum FilterMode {
        ALL("Semua", PRIMARY),
        FIX("Perbaikan", DANGER),
        FEATURE("Fitur", DlgAbout.FEATURE),
        PERFORMANCE("Performa", WARNING);

        private final String label;
        private final Color color;

        FilterMode(String label, Color color) {
            this.label = label;
            this.color = color;
        }
    }

    private enum UpdateCategory {
        FIX("Perbaikan", DANGER, new Color(252, 234, 230), new Color(166, 76, 56), FilterMode.FIX),
        FEATURE("Fitur", DlgAbout.FEATURE, new Color(239, 241, 255), new Color(82, 96, 183), FilterMode.FEATURE),
        PERFORMANCE("Performa", WARNING, new Color(255, 242, 216), new Color(150, 107, 23), FilterMode.PERFORMANCE),
        GENERAL("Pembaruan", PRIMARY, PRIMARY_SOFT, PRIMARY_DARK, FilterMode.ALL);

        private final String label;
        private final Color color;
        private final Color softColor;
        private final Color darkColor;
        private final FilterMode filterMode;

        UpdateCategory(String label, Color color, Color softColor, Color darkColor,
                FilterMode filterMode) {
            this.label = label;
            this.color = color;
            this.softColor = softColor;
            this.darkColor = darkColor;
            this.filterMode = filterMode;
        }
    }

    private static final class UpdateEntry {

        private final String version;
        private final List<String> details;
        private final UpdateCategory category;
        private final int originalOrder;
        private final String searchableText;

        private UpdateEntry(String version, List<String> details,
                UpdateCategory category, int originalOrder) {
            this.version = version;
            this.details = details;
            this.category = category;
            this.originalOrder = originalOrder;

            StringBuilder search = new StringBuilder(version).append(' ');
            for (String detail : details) {
                search.append(detail).append(' ');
            }
            this.searchableText = search.toString().toLowerCase(Locale.ROOT);
        }

        private boolean matches(String[] terms) {
            for (String term : terms) {
                if (!searchableText.contains(term)) {
                    return false;
                }
            }
            return true;
        }

        private String detailsText() {
            StringBuilder text = new StringBuilder();
            for (String detail : details) {
                if (text.length() > 0) {
                    text.append("  •  ");
                }
                text.append(detail);
            }
            return text.toString();
        }
    }

    private static class RoundedPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private final int radius;
        private final Color fillColor;
        private final Color borderColor;

        RoundedPanel(int radius, Color fillColor, Color borderColor) {
            this.radius = radius;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1,
                    getHeight() - 1, radius, radius));
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 2,
                        getHeight() - 2, radius, radius));
            }
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class HeroPanel extends RoundedPanel {

        private static final long serialVersionUID = 1L;

        HeroPanel() {
            super(17, PRIMARY, null);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            LinearGradientPaint gradient = new LinearGradientPaint(0, 0, getWidth(), getHeight(),
                    new float[]{0f, 1f}, new Color[]{new Color(8, 146, 185), PRIMARY_DARK});
            g2.setPaint(gradient);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1,
                    getHeight() - 1, 17, 17));
            g2.dispose();
        }
    }

    private static final class ModernButton extends JButton {

        private static final long serialVersionUID = 1L;

        private final Color normalColor;
        private final Color hoverColor;
        private boolean hovered;

        ModernButton(String text, Icon icon, Color normalColor, Color hoverColor,
                Color foreground) {
            super(text, icon);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setFont(appFont(Font.BOLD, 11));
            setForeground(foreground);
            setIconTextGap(8);
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = getModel().isPressed() ? hoverColor.darker()
                    : (hovered ? hoverColor : normalColor);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class PillToggleButton extends JToggleButton {

        private static final long serialVersionUID = 1L;

        private final Color accent;
        private boolean hovered;

        PillToggleButton(String text, Color accent) {
            super(text);
            this.accent = accent;
            setFont(appFont(Font.PLAIN, 11));
            setForeground(new Color(83, 103, 116));
            setBorder(new EmptyBorder(5, 11, 5, 11));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected()) {
                g2.setColor(accent);
                setForeground(Color.WHITE);
            } else {
                g2.setColor(hovered ? new Color(239, 246, 249) : INPUT_BACKGROUND);
                setForeground(new Color(83, 103, 116));
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            if (!isSelected()) {
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        getHeight(), getHeight());
            }
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class BadgeLabel extends JLabel {

        private static final long serialVersionUID = 1L;

        private final Color backgroundColor;

        BadgeLabel(String text, Color backgroundColor, Color foregroundColor) {
            super(text);
            this.backgroundColor = backgroundColor;
            setFont(appFont(Font.BOLD, 9));
            setForeground(foregroundColor);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(4, 10, 4, 10));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class PromptTextField extends JTextField {

        private static final long serialVersionUID = 1L;

        private final String prompt;

        PromptTextField(String prompt) {
            this.prompt = prompt;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().length() > 0 || isFocusOwner()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(new Color(143, 158, 168));
            Insets insets = getInsets();
            FontMetrics metrics = g2.getFontMetrics();
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(prompt, insets.left, y);
            g2.dispose();
        }
    }

    private static final class SearchIcon implements Icon {

        private final Color color;
        private final int size;

        SearchIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int circle = Math.max(6, size - 7);
            g2.drawOval(x + 1, y + 1, circle, circle);
            g2.drawLine(x + circle, y + circle, x + size - 1, y + size - 1);
            g2.dispose();
        }
    }

    private static final class CheckIcon implements Icon {

        private final Color color;
        private final int size;

        CheckIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 1, y + (size / 2), x + (size / 3), y + size - 2);
            g2.drawLine(x + (size / 3), y + size - 2, x + size - 1, y + 1);
            g2.dispose();
        }
    }

    private static final class CloseIcon implements Icon {

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 2, y + 2, x + 16, y + 16);
            g2.drawLine(x + 16, y + 2, x + 2, y + 16);
            g2.dispose();
        }
    }

    private static final class InfoMark extends JComponent {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 38));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(10, 10, getWidth() - 21, getHeight() - 21);
            g2.setFont(appFont(Font.BOLD, 18));
            FontMetrics metrics = g2.getFontMetrics();
            String text = "i";
            int x = (getWidth() - metrics.stringWidth(text)) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    private static final class Avatar extends JComponent {

        private static final long serialVersionUID = 1L;

        private final String text;
        private final Color foregroundColor;
        private Color backgroundColor;

        Avatar(String text, Color color) {
            this.text = text;
            this.foregroundColor = color;
            this.backgroundColor = color;
        }

        void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int diameter = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g2.setColor(backgroundColor);
            g2.fillOval(x, y, diameter, diameter);
            g2.setColor(backgroundColor.equals(foregroundColor) ? Color.WHITE : foregroundColor);
            int fontSize = Math.max(10, diameter / 3);
            g2.setFont(appFont(Font.BOLD, fontSize));
            FontMetrics metrics = g2.getFontMetrics();
            int textX = (getWidth() - metrics.stringWidth(text)) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(text, textX, textY);
            g2.dispose();
        }
    }
}
