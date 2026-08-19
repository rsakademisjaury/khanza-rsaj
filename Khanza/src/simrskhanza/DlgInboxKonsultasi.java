package simrskhanza;

import fungsi.akses;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.Vector;
import permintaan.DlgPermintaanKonsultasiMedik;

public class DlgInboxKonsultasi extends JDialog {
    private final Connection conn;
    private String kdDokter;
    private DefaultTableModel tabMode;

    // NetBeans form vars
    private JPanel rootPanel;
    private JPanel topPanel;
    private JLabel lblBadge;
    private JTextField txtCari;
    private JButton btnCari;
    private JButton btnRefresh;
    private JScrollPane scroll;
    private JTable tbl;
    private JPanel bottomPanel;
    private JButton btnJawab;
    private JButton btnTutup;

    public DlgInboxKonsultasi(Frame owner, boolean modal, Connection conn) {
        super(owner, modal);
        this.conn = conn;
        initComponents();
        initTable();
        setPreferredSize(new Dimension(900, 480));
        setLocationRelativeTo(owner);
        // default ambil dari akses.getkode() bila ada
        try {
            this.kdDokter = akses.getkode();
        } catch (Throwable t) { this.kdDokter = null; }
        hookEvents();
    }

    public void setKdDokter(String kd) { this.kdDokter = kd; }

    private void initComponents() {
        rootPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        lblBadge = new JLabel("Pending: 0");
        txtCari = new JTextField(24);
        txtCari.setToolTipText("Cari no. permintaan / no. rawat / pasien / pengirim");
        btnCari = new JButton("Cari");
        btnRefresh = new JButton("Refresh");
        topPanel.add(lblBadge);
        topPanel.add(txtCari);
        topPanel.add(btnCari);
        topPanel.add(btnRefresh);

        tbl = new JTable();
        tbl.setAutoCreateRowSorter(true);
        scroll = new JScrollPane(tbl);

        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnJawab = new JButton("Jawab / Lihat Konsul");
        btnTutup = new JButton("Tutup");
        bottomPanel.add(btnJawab);
        bottomPanel.add(btnTutup);

        setTitle("Inbox Konsultasi Medik");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        rootPanel.add(topPanel, BorderLayout.NORTH);
        rootPanel.add(scroll, BorderLayout.CENTER);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
        pack();
    }

    private void initTable() {
        tabMode = new DefaultTableModel(new Object[]{
                "No", "No.Permintaan", "No.Rawat", "Tanggal", "Pasien", "Pengirim", "Diagnosa", "Uraian"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl.setModel(tabMode);
        tbl.getColumnModel().getColumn(0).setMaxWidth(40);
        tbl.setRowHeight(22);
    }

    private void hookEvents() {
        btnRefresh.addActionListener(e -> tampil());
        btnCari.addActionListener(e -> tampil());
        txtCari.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tampil();
            }
        });
        btnTutup.addActionListener(e -> dispose());
        btnJawab.addActionListener(e -> jawabSelected());
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) jawabSelected();
            }
        });
    }

    public void tampil() {
        tabMode.setRowCount(0);
        if (kdDokter == null || kdDokter.trim().isEmpty()) {
            lblBadge.setText("Pending: 0");
            return;
        }
        String key = txtCari.getText().trim();
        String sql =
                "SELECT k.no_permintaan, k.no_rawat, k.tanggal, p.nm_pasien, " +
                "       d_from.nm_dokter AS pengirim, k.diagnosa_kerja, k.uraian_konsultasi " +
                "FROM konsultasi_medik k " +
                "JOIN reg_periksa rp ON rp.no_rawat = k.no_rawat " +
                "JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis " +
                "JOIN dokter d_from ON d_from.kd_dokter = k.kd_dokter " +
                "WHERE k.kd_dokter_dikonsuli = ? " +
                "AND NOT EXISTS (SELECT 1 FROM jawaban_konsultasi_medik j WHERE j.no_permintaan = k.no_permintaan) ";
        if (!key.isEmpty()) {
            sql += "AND (k.no_permintaan LIKE ? OR k.no_rawat LIKE ? OR p.nm_pasien LIKE ? OR d_from.nm_dokter LIKE ?) ";
        }
        sql += "ORDER BY k.tanggal DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kdDokter);
            int idx = 2;
            if (!key.isEmpty()) {
                String like = "%" + key + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(no++);
                    row.add(rs.getString("no_permintaan"));
                    row.add(rs.getString("no_rawat"));
                    row.add(rs.getString("tanggal"));
                    row.add(rs.getString("nm_pasien"));
                    row.add(rs.getString("pengirim"));
                    row.add(rs.getString("diagnosa_kerja"));
                    row.add(rs.getString("uraian_konsultasi"));
                    tabMode.addRow(row);
                }
                lblBadge.setText("Pending: " + (no - 1));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage());
        }
    }

    private void jawabSelected() {
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu konsultasi terlebih dahulu.");
            return;
        }
        int modelRow = tbl.convertRowIndexToModel(r);
        String noPermintaan = (String) tabMode.getValueAt(modelRow, 1);
        String noRawat = (String) tabMode.getValueAt(modelRow, 2);
        String namaPasien = (String) tabMode.getValueAt(modelRow, 4);

        // buka form jawaban/permintaan yang sudah ada
        DlgPermintaanKonsultasiMedik dlg = new DlgPermintaanKonsultasiMedik((Frame) getOwner(), false);
        try {
            // sesuai method yang kamu punya: setNoRm(noRawat, noPermintaan, namaPasien)
            dlg.setNoRm(noRawat, noPermintaan, namaPasien);
        } catch (Throwable t) {
            // fallback kalau method beda
            try { dlg.getClass().getMethod("setNoRm", String.class, String.class, String.class)
                    .invoke(dlg, noRawat, noPermintaan, namaPasien);
            } catch (Exception ex) { /* ignore */ }
        }
        dlg.tampil(); // refresh isi
        dlg.setVisible(true);
    }
}
