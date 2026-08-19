/*
 * PATCH RSAJ - Popup daftar surat kontrol pasien dari form Registrasi.
 * UI modern mandiri tanpa mengubah query/alur data.
 */
package bridging;

import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public final class CekSuratKontrolPopup extends JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final DefaultTableModel tabMode;
    private final JTable tbKontrol = new JTable();
    private final JTextField TNoRM = new JTextField();
    private final JLabel LblNamaPasien = new JLabel("-");
    private final JLabel LblPenjamin = new JLabel("-");
    private final JLabel LblSumber = new JLabel("-");
    private final JLabel LblStatus = new JLabel("Siap menampilkan surat kontrol pasien");

    private String noRM = "";
    private String namaPasien = "";
    private String kodePenjamin = "";
    private String namaPenjamin = "";

    // Palet modern yang sengaja digambar mandiri agar tidak ditimpa Look & Feel Khanza.
    private static final Color WARNA_BG = new Color(244, 247, 251);
    private static final Color WARNA_PANEL = Color.WHITE;
    private static final Color WARNA_PRIMARY = new Color(17, 71, 143);
    private static final Color WARNA_PRIMARY_HOVER = new Color(10, 88, 176);
    private static final Color WARNA_PRIMARY_PRESSED = new Color(8, 60, 124);
    private static final Color WARNA_DARK = new Color(51, 65, 85);
    private static final Color WARNA_DARK_HOVER = new Color(30, 41, 59);
    private static final Color WARNA_DARK_PRESSED = new Color(15, 23, 42);
    private static final Color WARNA_TEXT = new Color(30, 41, 59);
    private static final Color WARNA_TEXT_SEKUNDER = new Color(100, 116, 139);
    private static final Color WARNA_BORDER = new Color(220, 227, 236);
    private static final Color WARNA_HEADER_TABEL = new Color(239, 244, 250);
    private static final Color WARNA_GANJIL = new Color(248, 250, 252);
    private static final Color WARNA_SELECTED = new Color(219, 234, 254);

    private static final Font FONT_JUDUL = new Font("Segoe UI Semibold", Font.PLAIN, 20);
    private static final Font FONT_SUBJUDUL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI Semibold", Font.PLAIN, 12);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_VALUE = new Font("Segoe UI Semibold", Font.PLAIN, 14);

    public CekSuratKontrolPopup(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Cek Surat Kontrol Pasien");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1080, 650);
        setMinimumSize(new Dimension(900, 540));

        tabMode = new DefaultTableModel(null, new Object[]{
            "Sumber", "No. Surat", "Tanggal Surat", "Tanggal Kontrol",
            "Dokter Tujuan", "Poli Tujuan", "Diagnosa / Keterangan", "Status"
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        initComponentsMandiri();
        setLocationRelativeTo(parent);
    }

    private void initComponentsMandiri() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(WARNA_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        root.add(buildHeaderPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
        root.add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        RoundedPanel titleCard = new RoundedPanel(16, WARNA_PANEL);
        titleCard.setLayout(new BorderLayout(12, 6));
        titleCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel titleText = new JPanel();
        titleText.setOpaque(false);
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Cek Surat Kontrol Pasien");
        lblTitle.setFont(FONT_JUDUL);
        lblTitle.setForeground(WARNA_PRIMARY);

        JLabel lblSubTitle = new JLabel("Riwayat surat kontrol berdasarkan No. RM pasien");
        lblSubTitle.setFont(FONT_SUBJUDUL);
        lblSubTitle.setForeground(WARNA_TEXT_SEKUNDER);
        lblSubTitle.setBorder(new EmptyBorder(3, 0, 0, 0));

        titleText.add(lblTitle);
        titleText.add(lblSubTitle);
        titleCard.add(titleText, BorderLayout.CENTER);

        RoundedPanel infoCard = new RoundedPanel(16, WARNA_PANEL);
        infoCard.setLayout(new BorderLayout(0, 14));
        infoCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        formPanel.setOpaque(false);

        JLabel lblNoRM = new JLabel("No. RM");
        lblNoRM.setFont(FONT_LABEL);
        lblNoRM.setForeground(WARNA_TEXT);
        formPanel.add(lblNoRM);

        TNoRM.setEditable(false);
        TNoRM.setColumns(12);
        TNoRM.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        TNoRM.setForeground(WARNA_TEXT);
        TNoRM.setBackground(new Color(248, 250, 252));
        TNoRM.setMargin(new Insets(6, 9, 6, 9));
        TNoRM.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(3, 7, 3, 7)
        ));
        formPanel.add(TNoRM);

        ModernButton BtnCari = new ModernButton(
                "Cek Ulang", WARNA_PRIMARY, WARNA_PRIMARY_HOVER, WARNA_PRIMARY_PRESSED);
        BtnCari.setPreferredSize(new Dimension(112, 36));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tampil();
            }
        });
        formPanel.add(BtnCari);

        JPanel ringkasanPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        ringkasanPanel.setOpaque(false);
        ringkasanPanel.add(buatKotakRingkasan("Nama Pasien", LblNamaPasien));
        ringkasanPanel.add(buatKotakRingkasan("Penjamin", LblPenjamin));
        ringkasanPanel.add(buatKotakRingkasan("Sumber Data", LblSumber));

        infoCard.add(formPanel, BorderLayout.NORTH);
        infoCard.add(ringkasanPanel, BorderLayout.CENTER);

        wrapper.add(titleCard);
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(infoCard);
        return wrapper;
    }

    private JPanel buatKotakRingkasan(String judul, JLabel nilai) {
        RoundedPanel panel = new RoundedPanel(12, new Color(248, 250, 252));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(11, 13, 11, 13));

        JLabel labelJudul = new JLabel(judul);
        labelJudul.setFont(FONT_LABEL);
        labelJudul.setForeground(WARNA_TEXT_SEKUNDER);

        nilai.setFont(FONT_VALUE);
        nilai.setForeground(WARNA_PRIMARY);
        nilai.setBorder(new EmptyBorder(4, 0, 0, 0));

        panel.add(labelJudul);
        panel.add(nilai);
        return panel;
    }

    private JPanel buildCenterPanel() {
        RoundedPanel content = new RoundedPanel(16, WARNA_PANEL);
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        tbKontrol.setModel(tabMode);
        tbKontrol.setAutoCreateRowSorter(true);
        tbKontrol.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbKontrol.setRowHeight(32);
        tbKontrol.setShowHorizontalLines(true);
        tbKontrol.setShowVerticalLines(false);
        tbKontrol.setIntercellSpacing(new Dimension(0, 1));
        tbKontrol.setGridColor(new Color(235, 240, 245));
        tbKontrol.setFillsViewportHeight(true);
        tbKontrol.setSelectionBackground(WARNA_SELECTED);
        tbKontrol.setSelectionForeground(WARNA_TEXT);
        tbKontrol.setFont(FONT_NORMAL);
        tbKontrol.setDefaultRenderer(Object.class, new RendererKontrol());

        JTableHeader header = tbKontrol.getTableHeader();
        header.setFont(FONT_LABEL);
        header.setBackground(WARNA_HEADER_TABEL);
        header.setForeground(WARNA_TEXT);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, WARNA_BORDER));

        int[] lebar = {95, 180, 110, 115, 210, 170, 330, 100};
        for (int i = 0; i < lebar.length; i++) {
            TableColumn column = tbKontrol.getColumnModel().getColumn(i);
            column.setPreferredWidth(lebar[i]);
        }

        JScrollPane scroll = new JScrollPane(tbKontrol);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(WARNA_PANEL);
        scroll.setBackground(WARNA_PANEL);
        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildFooterPanel() {
        RoundedPanel footer = new RoundedPanel(16, WARNA_PANEL);
        footer.setLayout(new BorderLayout(12, 0));
        footer.setBorder(new EmptyBorder(10, 14, 10, 12));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        statusPanel.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dot.setForeground(new Color(34, 197, 94));
        dot.setBorder(new EmptyBorder(0, 0, 0, 8));
        statusPanel.add(dot);

        LblStatus.setFont(FONT_NORMAL);
        LblStatus.setForeground(WARNA_TEXT_SEKUNDER);
        statusPanel.add(LblStatus);
        footer.add(statusPanel, BorderLayout.CENTER);

        ModernButton BtnTutup = new ModernButton(
                "Tutup", WARNA_DARK, WARNA_DARK_HOVER, WARNA_DARK_PRESSED);
        BtnTutup.setPreferredSize(new Dimension(96, 36));
        BtnTutup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        footer.add(BtnTutup, BorderLayout.EAST);
        return footer;
    }

    /** Panel rounded tanpa ketergantungan Look & Feel. */
    private static final class RoundedPanel extends JPanel {
        private final int radius;
        private final Color warna;

        private RoundedPanel(int radius, Color warna) {
            this.radius = radius;
            this.warna = warna;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(warna);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.setColor(WARNA_BORDER);
                g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), radius, radius);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /**
     * Tombol digambar sendiri agar warna tidak pernah ditimpa Nimbus/Look & Feel Khanza.
     */
    private static final class ModernButton extends JButton {
        private final Color normal;
        private final Color hover;
        private final Color pressed;
        private boolean mouseOver = false;

        private ModernButton(String text, Color normal, Color hover, Color pressed) {
            super(text);
            this.normal = normal;
            this.hover = hover;
            this.pressed = pressed;

            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            setForeground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFocusPainted(false);
            setFocusable(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMargin(new Insets(0, 14, 0, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    mouseOver = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mouseOver = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel model = getModel();
                Color bg = normal;
                if (model.isPressed()) {
                    bg = pressed;
                } else if (mouseOver || model.isRollover()) {
                    bg = hover;
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    public void setPasien(String noRM, String namaPasien, String kodePenjamin, String namaPenjamin) {
        this.noRM = aman(noRM);
        this.namaPasien = aman(namaPasien);
        this.kodePenjamin = aman(kodePenjamin);
        this.namaPenjamin = aman(namaPenjamin);

        TNoRM.setText(this.noRM);
        LblNamaPasien.setText(this.namaPasien.equals("") ? "-" : this.namaPasien);
        LblPenjamin.setText(this.namaPenjamin.equals("") ? this.kodePenjamin : this.namaPenjamin);
        tampil();
    }

    public void tampil() {
        kosongkanTabel();
        if (noRM.equals("")) {
            LblStatus.setText("No.RM pasien masih kosong.");
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (isPasienBPJS()) {
                LblSumber.setText("Surat Kontrol BPJS");
                tampilBPJS();
            } else {
                LblSumber.setText("Surat Kontrol Umum / Instansi");
                tampilUmumInstansi();
            }

            if (tabMode.getRowCount() == 0) {
                LblStatus.setText("Tidak ditemukan surat kontrol untuk No.RM " + noRM + ".");
            } else {
                LblStatus.setText("Ditemukan " + tabMode.getRowCount() + " surat kontrol untuk No.RM " + noRM + ".");
            }
        } catch (Exception e) {
            System.out.println("Notif Cek Surat Kontrol : " + e);
            LblStatus.setText("Gagal mengambil data surat kontrol: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isPasienBPJS() {
        String kode = kodePenjamin.toUpperCase();
        String nama = namaPenjamin.toUpperCase();
        return kode.equals("BPJ") || kode.contains("BPJS") || nama.contains("BPJS");
    }

    private void tampilBPJS() throws Exception {
        String sql =
                "select bsk.no_surat,bsk.tgl_surat,bsk.tgl_rencana," +
                "bsk.nm_dokter_bpjs,bsk.nm_poli_bpjs,bs.nmdiagnosaawal," +
                "ifnull(kk.keterangan_kontrol,'') as keterangan_kontrol " +
                "from bridging_surat_kontrol_bpjs bsk " +
                "inner join bridging_sep bs on bs.no_sep=bsk.no_sep " +
                "left join keterangan_kontrol_bpjs kk on kk.no_rawat=bs.no_rawat " +
                "where bs.nomr=? " +
                "order by bsk.tgl_rencana desc,bsk.tgl_surat desc,bsk.no_surat desc";

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String diagnosa = aman(rs.getString("nmdiagnosaawal"));
                    String keterangan = aman(rs.getString("keterangan_kontrol"));
                    if (!keterangan.equals("")) {
                        diagnosa = diagnosa.equals("") ? keterangan : diagnosa + " | " + keterangan;
                    }
                    tabMode.addRow(new Object[]{
                        "BPJS",
                        aman(rs.getString("no_surat")),
                        aman(rs.getString("tgl_surat")),
                        aman(rs.getString("tgl_rencana")),
                        aman(rs.getString("nm_dokter_bpjs")),
                        aman(rs.getString("nm_poli_bpjs")),
                        diagnosa,
                        "Terbit"
                    });
                }
            }
        }
    }

    private void tampilUmumInstansi() throws Exception {
        String sql =
                "select s.no_antrian,s.tanggal_rujukan,s.tanggal_datang,d.nm_dokter," +
                "s.diagnosa,s.status," +
                "ifnull((select p.nm_poli from booking_registrasi br " +
                "inner join poliklinik p on p.kd_poli=br.kd_poli " +
                "where br.kd_dokter=s.kd_dokter " +
                "and br.tanggal_periksa=date(s.tanggal_datang) " +
                "and br.no_rkm_medis=s.no_rkm_medis limit 1),'') as nm_poli " +
                "from skdp_bpjs s " +
                "inner join dokter d on d.kd_dokter=s.kd_dokter " +
                "where s.no_rkm_medis=? " +
                "order by s.tanggal_datang desc,s.tanggal_rujukan desc,s.no_antrian desc";

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        "Umum/Instansi",
                        aman(rs.getString("no_antrian")),
                        aman(rs.getString("tanggal_rujukan")),
                        aman(rs.getString("tanggal_datang")),
                        aman(rs.getString("nm_dokter")),
                        aman(rs.getString("nm_poli")),
                        aman(rs.getString("diagnosa")),
                        aman(rs.getString("status"))
                    });
                }
            }
        }
    }

    private void kosongkanTabel() {
        while (tabMode.getRowCount() > 0) {
            tabMode.removeRow(0);
        }
    }

    private String aman(String nilai) {
        return nilai == null ? "" : nilai.trim();
    }

    private final class RendererKontrol extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            c.setFont(FONT_NORMAL);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : WARNA_GANJIL);
                c.setForeground(WARNA_TEXT);
            }
            setBorder(new EmptyBorder(0, 8, 0, 8));
            return c;
        }
    }
}
