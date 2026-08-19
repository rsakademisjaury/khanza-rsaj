/*
 * PATCH RSAJ - Popup mandiri cek rujukan BPJS PCare/FKTP berdasarkan No.Kartu hasil pencarian NIK.
 * Endpoint dan struktur output diadopsi dari form BPJSCekRujukanKartuPCare.
 */
package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public final class BPJSCekRujukanKartuPCarePopup extends JDialog {
    private final DefaultTableModel tabMode;
    private final JTable tbRujukan = new JTable();
    private final JTextField NoKartu = new JTextField();
    private final JLabel LblStatus = new JLabel("Siap memproses permintaan rujukan");
    private final JLabel LblNamaPasien = new JLabel("-");
    private final JLabel LblPoliRujukan = new JLabel("-");
    private final ApiBPJS api = new ApiBPJS();
    private final ObjectMapper mapper = new ObjectMapper();
    private String link = "";

    private static final Color WARNA_BG = new Color(245, 247, 250);
    private static final Color WARNA_PANEL = Color.WHITE;
    private static final Color WARNA_AKSESEN = new Color(25, 118, 210);
    private static final Color WARNA_AKSESEN_GELAP = new Color(13, 71, 161);
    private static final Color WARNA_AKSESEN_MUDA = new Color(232, 240, 254);
    private static final Color WARNA_HIJAU = new Color(22, 163, 74);
    private static final Color WARNA_TEXT = new Color(33, 43, 54);
    private static final Color WARNA_TEXT_SEKUNDER = new Color(94, 109, 130);
    private static final Color WARNA_BORDER = new Color(226, 232, 240);
    private static final Color WARNA_GANJIL = new Color(250, 252, 255);
    private static final Color WARNA_HEADER_SEKSI = new Color(237, 242, 247);
    private static final Font FONT_JUDUL = new Font("SansSerif", Font.BOLD, 18);
    private static final Font FONT_SUBJUDUL = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 12);

    public BPJSCekRujukanKartuPCarePopup(Frame parent, boolean modal) {
        super(parent, modal);
//        setTitle("Cek Rujukan BPJS PCare Berdasarkan NIK");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setSize(860, 620);
        setMinimumSize(new Dimension(720, 500));
        setLocationRelativeTo(parent);

        try {
            link = koneksiDB.URLAPIBPJS();
        } catch (Exception e) {
            link = "";
            System.out.println("Notif URL BPJS Popup Rujukan : " + e);
        }

        tabMode = new DefaultTableModel(null, new Object[]{"Field", "Isi"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        initComponentsMandiri();
    }

    private void initComponentsMandiri() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(WARNA_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildHeaderPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
        root.add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        JPanel titleCard = new JPanel(new BorderLayout(12, 8));
        titleCard.setBackground(WARNA_PANEL);
        titleCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNA_BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Cek Rujukan BPJS PCare Berdasarkan NIK");
        lblTitle.setFont(FONT_JUDUL);
        lblTitle.setForeground(WARNA_AKSESEN_GELAP);

        kiri.add(lblTitle);

//        JLabel chip = new JLabel("BPJS PCARE", SwingConstants.CENTER);
//        chip.setOpaque(true);
//        chip.setBackground(WARNA_AKSESEN_MUDA);
//        chip.setForeground(WARNA_AKSESEN_GELAP);
//        chip.setFont(new Font("SansSerif", Font.BOLD, 11));
//        chip.setBorder(new EmptyBorder(8, 12, 8, 12));

        titleCard.add(kiri, BorderLayout.CENTER);
//        titleCard.add(chip, BorderLayout.EAST);

        JPanel searchCard = new JPanel(new BorderLayout(10, 10));
        searchCard.setBackground(WARNA_PANEL);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNA_BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formPanel.setOpaque(false);

        JLabel lblNoKartu = new JLabel("No. Kartu");
        lblNoKartu.setFont(FONT_LABEL);
        lblNoKartu.setForeground(WARNA_TEXT);
        formPanel.add(lblNoKartu);

        NoKartu.setEditable(false);
        NoKartu.setColumns(18);
        NoKartu.setFont(new Font("Monospaced", Font.BOLD, 14));
        NoKartu.setMargin(new Insets(6, 8, 6, 8));
        NoKartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(4, 6, 4, 6)
        ));
        formPanel.add(NoKartu);

        JButton BtnCari = buatTombolHijau("Cari Ulang");
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tampil(NoKartu.getText().trim());
            }
        });
        formPanel.add(BtnCari);

        JPanel ringkasanPanel = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        ringkasanPanel.setOpaque(false);
        ringkasanPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        ringkasanPanel.add(buatKotakRingkasan("Nama Pasien :", LblNamaPasien));
        ringkasanPanel.add(buatKotakRingkasan("Poli Rujukan :", LblPoliRujukan));

        searchCard.add(formPanel, BorderLayout.NORTH);
        searchCard.add(ringkasanPanel, BorderLayout.SOUTH);

        wrapper.add(titleCard);
        wrapper.add(Box.createVerticalStrut(10));
        wrapper.add(searchCard);

        return wrapper;
    }

    private JPanel buatKotakRingkasan(String judul, JLabel labelNilai) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel labelJudul = new JLabel(judul);
        labelJudul.setFont(new Font("SansSerif", Font.BOLD, 12));
        labelJudul.setForeground(WARNA_TEXT_SEKUNDER);

        labelNilai.setFont(new Font("SansSerif", Font.BOLD, 14));
        labelNilai.setForeground(WARNA_AKSESEN_GELAP);
        labelNilai.setBorder(new EmptyBorder(4, 0, 0, 0));

        panel.add(labelJudul);
        panel.add(labelNilai);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(WARNA_PANEL);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNA_BORDER),
                new EmptyBorder(8, 8, 8, 8)
        ));

        tbRujukan.setModel(tabMode);
        tbRujukan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbRujukan.setRowHeight(28);
        tbRujukan.setShowGrid(true);
        tbRujukan.setGridColor(new Color(235, 240, 245));
        tbRujukan.setFillsViewportHeight(true);
        tbRujukan.setSelectionBackground(new Color(219, 234, 254));
        tbRujukan.setSelectionForeground(WARNA_TEXT);
        tbRujukan.setFont(FONT_NORMAL);
        tbRujukan.setDefaultRenderer(Object.class, new RendererRujukan());

        JTableHeader header = tbRujukan.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(WARNA_TEXT);
        header.setBorder(BorderFactory.createLineBorder(WARNA_BORDER));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));

        TableColumn c0 = tbRujukan.getColumnModel().getColumn(0);
        TableColumn c1 = tbRujukan.getColumnModel().getColumn(1);
        c0.setPreferredWidth(220);
        c1.setPreferredWidth(585);

        JScrollPane scroll = new JScrollPane(tbRujukan);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(WARNA_PANEL);

        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WARNA_PANEL);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNA_BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel ikon = new JLabel("● ");
        ikon.setForeground(new Color(34, 197, 94));
        ikon.setFont(new Font("SansSerif", Font.BOLD, 14));

        LblStatus.setFont(FONT_SUBJUDUL);
        LblStatus.setForeground(WARNA_TEXT_SEKUNDER);

        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        kiri.setOpaque(false);
        kiri.add(ikon);
        kiri.add(LblStatus);

        JLabel bantuan = new JLabel("Tips: jika rujukan tidak ditemukan, cek kembali masa berlaku atau sumber rujukan");
        bantuan.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bantuan.setForeground(new Color(100, 116, 139));

        footer.add(kiri, BorderLayout.WEST);
        footer.add(bantuan, BorderLayout.EAST);
        return footer;
    }

    private JButton buatTombolHijau(String teks) {
        JButton btn = new JButton(teks) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color warna = WARNA_HIJAU;
                if (!isEnabled()) {
                    warna = new Color(134, 239, 172);
                } else if (getModel().isPressed()) {
                    warna = new Color(21, 128, 61);
                } else if (getModel().isRollover()) {
                    warna = new Color(34, 197, 94);
                }

                g2.setColor(warna);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buatTombol(String teks, Color background, Color foreground) {
        JButton btn = new JButton(teks);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void SetNoKartu(String noKartu) {
        setNoKartu(noKartu);
    }

    public void setNoKartu(String noKartu) {
        NoKartu.setText(noKartu == null ? "" : noKartu.trim());
        tampil(NoKartu.getText().trim());
    }

    public void tampil(String nomorKartu) {
        if (nomorKartu == null || nomorKartu.trim().equals("")) {
            JOptionPane.showMessageDialog(this, "No.Kartu masih kosong..!!");
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            kosongkanTabel();
            LblStatus.setText("Menghubungi server BPJS...");
            LblNamaPasien.setText("Memproses...");
            LblPoliRujukan.setText("-");

            // Endpoint PCare/FKTP mengikuti BPJSCekRujukanKartuPCare: /Rujukan/Peserta/{noKartu}
            String URL = link + "/Rujukan/Peserta/" + nomorKartu.trim();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Cons-ID", koneksiDB.CONSIDAPIBPJS());
            String utc = String.valueOf(api.GetUTCdatetimeAsString());
            headers.add("X-Timestamp", utc);
            headers.add("X-Signature", api.getHmac(utc));
            headers.add("user_key", koneksiDB.USERKEYAPIBPJS());
            HttpEntity requestEntity = new HttpEntity(headers);

            JsonNode root = mapper.readTree(api.getRest().exchange(URL, HttpMethod.GET, requestEntity, String.class).getBody());
            JsonNode metaData = root.path("metaData");
            if ("200".equals(metaData.path("code").asText())) {
                JsonNode response = mapper.readTree(api.Decrypt(root.path("response").asText(), utc)).path("rujukan");
                isiTabel(response);
                String nama = text(response.path("peserta").path("nama"));
                String poli = text(response.path("poliRujukan").path("nama"));
                if (nama.equals("")) {
                    nama = "Peserta";
                }
                if (poli.equals("")) {
                    poli = "Poli tidak tersedia";
                }
                LblNamaPasien.setText(nama);
                LblPoliRujukan.setText(poli);
                LblStatus.setText("Berhasil mengambil data rujukan BPJS");
            } else {
                tambah("Status", metaData.path("code").asText() + " - " + metaData.path("message").asText());
                LblNamaPasien.setText("Rujukan tidak ditemukan");
                LblPoliRujukan.setText("-");
                LblStatus.setText(metaData.path("message").asText());
                JOptionPane.showMessageDialog(this, metaData.path("message").asText());
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi Popup Rujukan Peserta PCare : " + ex);
            tambah("Error", ex.toString());
            LblNamaPasien.setText("Gagal mengambil data");
            LblPoliRujukan.setText("-");
            LblStatus.setText("Gagal mengambil data rujukan");
            if (ex.toString().contains("UnknownHostException")) {
                JOptionPane.showMessageDialog(this, "Koneksi ke server BPJS terputus...!");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengambil data rujukan BPJS.\n" + ex.getMessage());
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void isiTabel(JsonNode response) {
        kosongkanTabel();
        tambahSeksi("INFORMASI RUJUKAN");
        tambah("Diagnosa", gabung(text(response.path("diagnosa").path("kode")), text(response.path("diagnosa").path("nama"))));
        tambah("Keluhan", text(response.path("keluhan")));
        tambah("No. Kunjungan", text(response.path("noKunjungan")));
        tambah("Pelayanan", gabung(text(response.path("pelayanan").path("kode")), text(response.path("pelayanan").path("nama"))));
        tambah("Poli Rujukan", gabung(text(response.path("poliRujukan").path("kode")), text(response.path("poliRujukan").path("nama"))));
        tambah("Provider Perujuk", gabung(text(response.path("provPerujuk").path("kode")), text(response.path("provPerujuk").path("nama"))));
        tambah("Tanggal Kunjungan", text(response.path("tglKunjungan")));

        tambahSeksi("DATA PESERTA");
        tambah("Nama Pasien", text(response.path("peserta").path("nama")));
        tambah("NIK", text(response.path("peserta").path("nik")));
        tambah("No. Kartu", text(response.path("peserta").path("noKartu")));
        tambah("Pisa", text(response.path("peserta").path("pisa")));
        tambah("Jenis Kelamin", text(response.path("peserta").path("sex")).replaceAll("L", "Laki-Laki").replaceAll("P", "Perempuan"));
        tambah("Jenis Peserta", gabung(text(response.path("peserta").path("jenisPeserta").path("kode")), text(response.path("peserta").path("jenisPeserta").path("keterangan"))));
        tambah("Status Peserta", gabung(text(response.path("peserta").path("statusPeserta").path("kode")), text(response.path("peserta").path("statusPeserta").path("keterangan"))));
        tambah("Hak Kelas", gabung(text(response.path("peserta").path("hakKelas").path("kode")), text(response.path("peserta").path("hakKelas").path("keterangan"))));
        tambah("Provider", gabung(text(response.path("peserta").path("provUmum").path("kdProvider")), text(response.path("peserta").path("provUmum").path("nmProvider"))));
        tambah("Tanggal Cetak Kartu", text(response.path("peserta").path("tglCetakKartu")));
        tambah("Tanggal Lahir", text(response.path("peserta").path("tglLahir")));
        tambah("Tanggal TAT", text(response.path("peserta").path("tglTAT")));
        tambah("Tanggal TMT", text(response.path("peserta").path("tglTMT")));
        tambah("Umur Saat Pelayanan", text(response.path("peserta").path("umur").path("umurSaatPelayanan")));
        tambah("Umur Sekarang", text(response.path("peserta").path("umur").path("umurSekarang")));

        tambahSeksi("COB / ASURANSI");
        tambah("Nama Asuransi", text(response.path("peserta").path("cob").path("nmAsuransi")));
        tambah("No. Asuransi", text(response.path("peserta").path("cob").path("noAsuransi")));
        tambah("Tanggal TAT COB", text(response.path("peserta").path("cob").path("tglTAT")));
        tambah("Tanggal TMT COB", text(response.path("peserta").path("cob").path("tglTMT")));

        tambahSeksi("INFORMASI TAMBAHAN");
        tambah("Dinsos", text(response.path("peserta").path("informasi").path("dinsos")));
        tambah("No. SKTM", text(response.path("peserta").path("informasi").path("noSKTM")));
        tambah("Prolanis PRB", text(response.path("peserta").path("informasi").path("prolanisPRB")));
        tambah("Nomor RM", text(response.path("peserta").path("mr").path("noMR")));
        tambah("Nomor Telepon", text(response.path("peserta").path("mr").path("noTelepon")));
    }

    private void tambahSeksi(String judul) {
        tabMode.addRow(new Object[]{judul, ""});
    }

    private void tambah(String field, String isi) {
        tabMode.addRow(new Object[]{field, isi == null ? "" : isi});
    }

    private String gabung(String kiri, String kanan) {
        kiri = kiri == null ? "" : kiri.trim();
        kanan = kanan == null ? "" : kanan.trim();
        if (kiri.equals("")) {
            return kanan;
        }
        if (kanan.equals("")) {
            return kiri;
        }
        return kiri + " - " + kanan;
    }

    private String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return "";
        }

        String nilai = node.asText();
        if (nilai == null || "null".equalsIgnoreCase(nilai.trim())) {
            return "";
        }
        return nilai;
    }

    private void kosongkanTabel() {
        while (tabMode.getRowCount() > 0) {
            tabMode.removeRow(0);
        }
    }

    private final class RendererRujukan extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String field = table.getValueAt(row, 0) == null ? "" : table.getValueAt(row, 0).toString();
            String isi = table.getValueAt(row, 1) == null ? "" : table.getValueAt(row, 1).toString();
            boolean seksi = !field.trim().equals("") && isi.trim().equals("");

            setBorder(new EmptyBorder(0, 8, 0, 8));
            setForeground(WARNA_TEXT);
            setFont(FONT_NORMAL);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else if (seksi) {
                setBackground(WARNA_HEADER_SEKSI);
                setForeground(WARNA_AKSESEN_GELAP);
                setFont(new Font("SansSerif", Font.BOLD, 12));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : WARNA_GANJIL);
                if (column == 0) {
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                } else {
                    setFont(FONT_NORMAL);
                }
            }

            if (seksi) {
                if (column == 0) {
                    setText("  " + field);
                } else {
                    setText("");
                }
            } else {
                setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }
}
