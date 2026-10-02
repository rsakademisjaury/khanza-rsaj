package permintaan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.net.URL;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/** Tampilan pengalihan jadwal. Validasi dan transaksi tetap di DlgBookingOperasi. */
final class PopupAlihkanJadwalOperasi {
    private static final Color BIRU = new Color(25, 102, 183);
    private static final Color GELAP = new Color(35, 50, 68);
    private static final Color SEKUNDER = new Color(99, 115, 134);
    private static final Color GARIS = new Color(215, 226, 239);
    private static final Color MUDA = new Color(239, 246, 254);
    private static final Color PUTIH = Color.WHITE;
    private static final Font NORMAL = new Font("Segoe UI",Font.PLAIN,13);
    private static final Font TEBAL = new Font("Segoe UI",Font.BOLD,13);

    private PopupAlihkanJadwalOperasi() { }

    static DlgBookingOperasi.CalonJadwal pilih(Window owner, List<DlgBookingOperasi.CalonJadwal> calon,
            String nama, String noRm, String noRawatTujuan) {
        JDialog dialog = dialog(owner,"Alihkan Jadwal Operasi",940,475);
        JPanel root = new JPanel(new BorderLayout(0,0));
        root.setBackground(PUTIH);

        JPanel body = new JPanel(new BorderLayout(0,10));
        body.setBackground(PUTIH);
        body.setBorder(BorderFactory.createEmptyBorder(11,16,10,16));
        body.add(identitas(nama,noRm,noRawatTujuan),BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12,0));
        content.setBackground(PUTIH);
        JPanel left = new JPanel(new BorderLayout(0,8));
        left.setBackground(PUTIH);
        left.setPreferredSize(new Dimension(220,100));
        left.setBorder(BorderFactory.createMatteBorder(0,0,0,1,GARIS));
        left.add(heading("Jadwal ditemukan ("+calon.size()+")"),BorderLayout.NORTH);
        DefaultListModel<DlgBookingOperasi.CalonJadwal> model = new DefaultListModel<>();
        calon.forEach(model::addElement);
        JList<DlgBookingOperasi.CalonJadwal> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new JadwalRenderer());
        list.setFixedCellHeight(82);
        list.setBackground(PUTIH);
        JScrollPane choices = new JScrollPane(list);
        choices.setBorder(BorderFactory.createEmptyBorder(0,0,0,12));
        choices.getViewport().setBackground(PUTIH);
        left.add(choices,BorderLayout.CENTER);
        content.add(left,BorderLayout.WEST);

        JPanel detail = new JPanel(new BorderLayout(0,6));
        detail.setBackground(PUTIH);
        detail.add(heading("Rincian jadwal terpilih"),BorderLayout.NORTH);
        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields,BoxLayout.Y_AXIS));
        fields.setBackground(PUTIH);
        fields.add(heading("Kunjungan Asal"));
        JLabel asal = value("");
        JLabel status = value("");
        fields.add(row(input("No. Rawat Jalan",asal),input("Status Jadwal",status)));
        fields.add(Box.createVerticalStrut(7));
        JPanel columns = new JPanel(new GridLayout(1,2,14,0));
        columns.setBackground(PUTIH);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel rencana = column("Rencana Operasi");
        JLabel paket=value(""), tindakan=value(""), tanggal=value(""), ruang=value(""), mulai=value(""), selesai=value("");
        rencana.add(row(input("Kode Paket",paket),input("Tindakan Operasi",tindakan)));
        rencana.add(row(input("Tanggal Operasi",tanggal),input("Ruang Operasi",ruang)));
        rencana.add(row(input("Jam Mulai",mulai),input("Jam Selesai",selesai)));
        JPanel tim = column("Tim dan Pencatatan");
        JLabel operator=value(""), anestesi=value(""), perawat=value(""), waktu=value("");
        tim.add(input("Dokter Operator",operator));
        tim.add(input("Dokter Anestesi",anestesi));
        tim.add(row(input("Perawat pada Jadwal Asal",perawat),input("Waktu Input Awal",waktu)));
        columns.add(rencana);
        columns.add(tim);
        fields.add(columns);
        detail.add(fields,BorderLayout.CENTER);
        content.add(detail,BorderLayout.CENTER);
        body.add(content,BorderLayout.CENTER);

        JPanel tujuan = new JPanel(new BorderLayout(10,0));
        tujuan.setBackground(MUDA);
        tujuan.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        tujuan.add(label("→  Rawat Inap  ·  "+noRawatTujuan,TEBAL,BIRU),BorderLayout.WEST);
        JLabel audit = label("Data asal tetap tersimpan di riwayat pengalihan",NORMAL,SEKUNDER);
        tujuan.add(audit,BorderLayout.EAST);
        body.add(tujuan,BorderLayout.SOUTH);
        root.add(body,BorderLayout.CENTER);

        final DlgBookingOperasi.CalonJadwal[] dipilih = {null};
        JButton alihkan = button("Alihkan Jadwal",true);
        JButton batal = button("Batal",false);
        batal.addActionListener(e -> dialog.dispose());
        alihkan.addActionListener(e -> {
            DlgBookingOperasi.CalonJadwal terpilih = list.getSelectedValue();
            if(terpilih!=null && konfirmasi(dialog,terpilih,noRawatTujuan)) {
                dipilih[0]=terpilih;
                dialog.dispose();
            }
        });
        root.add(footer("Pastikan jadwal dan pasien sesuai sebelum melanjutkan.",alihkan,batal),BorderLayout.SOUTH);
        list.addListSelectionListener(e -> {
            if(e.getValueIsAdjusting()) return;
            DlgBookingOperasi.CalonJadwal j = list.getSelectedValue();
            alihkan.setEnabled(j!=null);
            if(j==null) return;
            asal.setText(kosong(j.noRawat)); status.setText(kosong(j.status));
            paket.setText(kosong(j.kodePaket)); tindakan.setText(kosong(j.tindakan));
            tanggal.setText(kosong(j.tanggal)); ruang.setText(kosong(j.ruang));
            mulai.setText(kosong(j.jam)); selesai.setText(kosong(j.jamSelesai));
            operator.setText(kosong(j.kodeDokter)+" · "+kosong(j.dokter));
            anestesi.setText(kosong(j.dokterAnastesi)); perawat.setText(kosong(j.perawat));
            waktu.setText(kosong(j.waktu));
        });
        dialog.setContentPane(root);
        if(!model.isEmpty()) list.setSelectedIndex(0);
        present(dialog,owner);
        return dipilih[0];
    }

    static void informasi(Window owner, List<DlgBookingOperasi.CalonJadwal> calon,
            String nama, String noRm, Runnable lihatJadwal) {
        JDialog dialog = dialog(owner,"Jadwal Operasi Ditemukan",760,235);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PUTIH);
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(PUTIH);
        body.setBorder(BorderFactory.createEmptyBorder(12,16,8,16));
        JPanel content=new JPanel(new GridBagLayout());
        content.setBackground(PUTIH);
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.anchor=GridBagConstraints.WEST;
        JPanel badgeRow=new JPanel(new BorderLayout());
        badgeRow.setBackground(PUTIH);
        badgeRow.setBorder(BorderFactory.createEmptyBorder(0,36,0,0));
        badgeRow.add(badge(calon.size()+" jadwal aktif dari rawat jalan"),BorderLayout.WEST);
        gbc.gridy=0;
        gbc.insets=new Insets(0,0,7,0);
        content.add(badgeRow,gbc);
        gbc.gridy=1;
        gbc.insets=new Insets(0,0,8,0);
        content.add(identitasSingkat(nama,noRm),gbc);
        DlgBookingOperasi.CalonJadwal pertama=calon.get(0);
        JPanel summary=new JPanel(new GridLayout(1,3,12,0));
        summary.setBackground(PUTIH);
        summary.setBorder(BorderFactory.createEmptyBorder(0,36,0,0));
        summary.add(input("No. Rawat Jalan",value(pertama.noRawat)));
        summary.add(input("Tanggal dan Jam Operasi",value(kosong(pertama.tanggal)+"  "+kosong(pertama.jam))));
        summary.add(input("Tindakan Operasi",value(pertama.tindakan)));
        gbc.gridy=2;
        gbc.insets=new Insets(0,0,0,0);
        content.add(summary,gbc);
        if(calon.size()>1) {
            gbc.gridy=3;
            gbc.insets=new Insets(6,36,0,0);
            content.add(label("Masih ada "+(calon.size()-1)+" jadwal lain. Buka rincian untuk memilih.",NORMAL,SEKUNDER),gbc);
        }
        body.add(content,BorderLayout.NORTH);
        root.add(body,BorderLayout.CENTER);
        JButton lihat = button("Lihat Jadwal",true);
        JButton tutup = button("Tutup",false);
        lihat.addActionListener(e -> { dialog.dispose(); lihatJadwal.run(); });
        tutup.addActionListener(e -> dialog.dispose());
        root.add(footer("",lihat,tutup),BorderLayout.SOUTH);
        dialog.setContentPane(root);
        present(dialog,owner);
    }

    static boolean konfirmasiJadwalBaru(Window owner,int jumlah) {
        JDialog dialog=dialog(owner,"Periksa Jadwal Sebelumnya",500,175);
        JPanel root=new JPanel(new BorderLayout());
        root.setBackground(PUTIH);
        JPanel body=new JPanel();
        body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        body.setBackground(PUTIH);
        body.setBorder(BorderFactory.createEmptyBorder(16,20,13,20));
        body.add(heading(jumlah+" jadwal aktif ditemukan"));
        body.add(label("Lanjut hanya jika ini operasi yang berbeda.",NORMAL,GELAP));
        root.add(body,BorderLayout.CENTER);
        final boolean[] lanjut={false};
        JButton buat=button("Buat Jadwal Baru",true),batal=button("Batal",false);
        buat.setPreferredSize(new Dimension(150,31));
        buat.addActionListener(e -> { lanjut[0]=true; dialog.dispose(); });
        batal.addActionListener(e -> dialog.dispose());
        root.add(footer("",buat,batal),BorderLayout.SOUTH);
        dialog.setContentPane(root);
        present(dialog,owner);
        return lanjut[0];
    }

    private static boolean konfirmasi(Window owner, DlgBookingOperasi.CalonJadwal j, String tujuan) {
        JDialog dialog = dialog(owner,"Konfirmasi Pengalihan",510,190);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PUTIH);
        JPanel body = new JPanel(new GridLayout(1,2,12,0));
        body.setBackground(PUTIH);
        body.setBorder(BorderFactory.createEmptyBorder(20,16,12,16));
        body.add(input("Dari Rawat Jalan",value(j.noRawat)));
        body.add(input("Ke Rawat Inap",value(tujuan)));
        root.add(body,BorderLayout.CENTER);
        final boolean[] ya={false};
        JButton konfirmasi=button("Ya, Alihkan",true), batal=button("Batal",false);
        konfirmasi.addActionListener(e -> { ya[0]=true; dialog.dispose(); });
        batal.addActionListener(e -> dialog.dispose());
        root.add(footer("Data awal akan disimpan di riwayat.",konfirmasi,batal),BorderLayout.SOUTH);
        dialog.setContentPane(root);
        present(dialog,owner);
        return ya[0];
    }

    private static JDialog dialog(Window owner,String title,int width,int height) {
        JDialog dialog=new JDialog(owner,title,JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(width,height);
        dialog.setMinimumSize(new Dimension(width,height));
        return dialog;
    }

    private static void present(JDialog dialog,Window owner) {
        Dimension minimum=dialog.getMinimumSize();
        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(),minimum.width),Math.max(dialog.getHeight(),minimum.height));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static JPanel identitas(String nama,String noRm,String tujuan) {
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        panel.setBackground(PUTIH);
        panel.add(identity("/picture/no_rawat.png","No. Rawat Inap",tujuan));
        panel.add(Box.createHorizontalStrut(16));
        panel.add(identity("/picture/no_rm.png","No. RM",noRm));
        panel.add(Box.createHorizontalStrut(16));
        panel.add(identity("/picture/nama_pasien.png","Nama Pasien",nama));
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private static JPanel identitasSingkat(String nama,String noRm) {
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        panel.setBackground(PUTIH);
        panel.add(identity("/picture/nama_pasien.png","Nama Pasien",nama));
        panel.add(Box.createHorizontalStrut(16));
        panel.add(identity("/picture/no_rm.png","No. RM",noRm));
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private static JPanel identity(String iconPath,String title,String text) {
        JPanel panel=new JPanel(new BorderLayout(5,0));
        panel.setBackground(PUTIH);
        panel.setBorder(BorderFactory.createEmptyBorder(4,5,9,8));
        JLabel icon=label("",NORMAL,BIRU);
        icon.setIcon(icon(iconPath,23));
        icon.setPreferredSize(new Dimension(26,30));
        panel.add(icon,BorderLayout.WEST);
        JPanel words=new JPanel();
        words.setLayout(new BoxLayout(words,BoxLayout.Y_AXIS));
        words.setBackground(PUTIH);
        words.add(label(title,new Font("Segoe UI",Font.PLAIN,11),SEKUNDER));
        words.add(label(kosong(text),NORMAL,GELAP));
        panel.add(words,BorderLayout.CENTER);
        panel.setMaximumSize(panel.getPreferredSize());
        return panel;
    }

    private static JLabel badge(String text) {
        JLabel label=new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D graphics=(Graphics2D)g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(BIRU);
                graphics.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                graphics.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(TEBAL);
        label.setForeground(PUTIH);
        label.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
        return label;
    }

    private static ImageIcon icon(String path,int size) {
        URL url=PopupAlihkanJadwalOperasi.class.getResource(path);
        if(url==null) return null;
        Image scaled=new ImageIcon(url).getImage().getScaledInstance(size,size,Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static JPanel column(String name) {
        JPanel p=new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(PUTIH);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(heading(name));
        return p;
    }

    private static JLabel heading(String value) {
        JLabel l=label(value,TEBAL,BIRU);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
        return l;
    }

    private static JPanel row(JPanel first,JPanel second) {
        JPanel p=new JPanel(new GridLayout(1,2,9,0));
        p.setBackground(PUTIH);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));
        p.add(first); p.add(second);
        return p;
    }

    private static JPanel input(String title,JLabel value) {
        JPanel p=new JPanel(new BorderLayout(0,1));
        p.setBackground(PUTIH);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
        p.add(label(title,new Font("Segoe UI",Font.PLAIN,11),SEKUNDER),BorderLayout.NORTH);
        p.add(value,BorderLayout.CENTER);
        return p;
    }

    private static JLabel value(String text) {
        return label(kosong(text),NORMAL,GELAP);
    }

    private static JPanel footer(String note,JButton action,JButton cancel) {
        JPanel panel=new JPanel(new BorderLayout(10,0));
        panel.setBackground(PUTIH);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,GARIS),
                BorderFactory.createEmptyBorder(10,20,10,20)));
        panel.add(label(note,NORMAL,SEKUNDER),BorderLayout.WEST);
        JPanel actions=new JPanel();
        actions.setBackground(PUTIH);
        actions.add(action);
        actions.add(cancel); // Tombol keluar/batal paling kanan sesuai pola Khanza.
        panel.add(actions,BorderLayout.EAST);
        return panel;
    }

    private static JButton button(String text,boolean primary) {
        JButton b=new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D graphics=(Graphics2D)g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(primary) {
                    graphics.setColor(isEnabled()?(getModel().isRollover()?BIRU.darker():BIRU):new Color(152,184,217));
                    graphics.fillRoundRect(1,1,getWidth()-2,getHeight()-2,8,8);
                }else{
                    graphics.setColor(PUTIH);
                    graphics.fillRoundRect(1,1,getWidth()-2,getHeight()-2,8,8);
                    graphics.setColor(GARIS);
                    graphics.drawRoundRect(1,1,getWidth()-3,getHeight()-3,8,8);
                }
                if(hasFocus()) {
                    graphics.setColor(BIRU);
                    graphics.drawRoundRect(1,1,getWidth()-3,getHeight()-3,8,8);
                }
                graphics.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(TEBAL);
        b.setPreferredSize(new Dimension(primary?132:84,31));
        b.setFocusPainted(false);
        b.setForeground(primary?PUTIH:GELAP);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(2,7,2,7));
        b.setRolloverEnabled(true);
        return b;
    }

    private static JLabel label(String text,Font font,Color color) {
        JLabel l=new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private static String kosong(String value) {
        return value==null || value.trim().isEmpty() || value.equals("00:00:00")?"-":value;
    }

    private static final class JadwalRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,
                boolean selected,boolean focus) {
            DlgBookingOperasi.CalonJadwal j=(DlgBookingOperasi.CalonJadwal)value;
            JPanel card=new JPanel(new BorderLayout(0,4)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D graphics=(Graphics2D)g.create();
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setColor(selected?MUDA:PUTIH);
                    graphics.fillRoundRect(2,2,getWidth()-4,getHeight()-4,12,12);
                    graphics.setColor(selected?BIRU:GARIS);
                    graphics.drawRoundRect(2,2,getWidth()-5,getHeight()-5,12,12);
                    graphics.dispose();
                    super.paintComponent(g);
                }
            };
            card.setOpaque(false);
            card.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
            JLabel top=label(kosong(j.tanggal)+"  ·  "+kosong(j.jam),TEBAL,GELAP);
            JLabel middle=label(kosong(j.tindakan),NORMAL,GELAP);
            JLabel bottom=label("Ralan "+kosong(j.noRawat),NORMAL,SEKUNDER);
            top.setHorizontalAlignment(SwingConstants.LEFT);
            card.add(top,BorderLayout.NORTH);
            card.add(middle,BorderLayout.CENTER);
            card.add(bottom,BorderLayout.SOUTH);
            return card;
        }
    }
}
