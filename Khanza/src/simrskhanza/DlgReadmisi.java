package simrskhanza;

import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Form mandiri; hanya alasan dan keterangan yang diinput pengguna. */
public final class DlgReadmisi extends JDialog {
    private static final long serialVersionUID=1L;
    private static final Color BIRU=new Color(24,103,177);
    private static final DateTimeFormatter WAKTU=DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final String[] BULAN={"Januari","Februari","Maret","April","Mei","Juni",
            "Juli","Agustus","September","Oktober","November","Desember"};
    private final ReadmisiService service=new ReadmisiService();
    private final JTabbedPane tabs=new JTabbedPane();
    private final JTextField asal=otomatis(), masuk=otomatis(), rm=otomatis(), nama=otomatis();
    private final JTextField pulang=otomatis(), datang=otomatis(), ruangan=otomatis(), selisih=otomatis();
    private final JTextArea diagnosa=area(2), alasan=area(3), keterangan=area(3);
    private final JLabel status=new JLabel(" "), tipe=new JLabel(" ");
    private final JButton simpan=tombol("Simpan"), muat=tombol("Muat Ulang"), cari=tombol("Tampilkan");
    private final JButton cetak=tombol("Cetak Rekap"), buka=tombol("Buka Catatan"), tutup=tombol("Tutup");
    private final JComboBox<String> bulan=new JComboBox<>(BULAN), ruang=new JComboBox<>();
    private final JSpinner tahun=new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(),2000,2200,1));
    private final JTextField pencarian=new JTextField(14);
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"No.","No. RM","Tgl dan Jam Pulang",
            "Tgl dan Jam Masuk","Alasan Readmisi","Diagnosa","Keterangan"},0) {
        private static final long serialVersionUID=1L;
        @Override public boolean isCellEditable(int r,int c) { return false; }
    };
    private final JTable tabel=new JTable(model);
    private List<ReadmisiService.Data> daftar=new ArrayList<>();
    private ReadmisiService.Data data;
    private String noRawat="", judulRekap="";
    private boolean sibuk;

    public static void buka(Component parent,String noRawat) {
        if (!akses.getkamar_inap()) {
            JOptionPane.showMessageDialog(parent,"Anda tidak memiliki hak akses Kamar Inap."); return;
        }
        Window owner=parent instanceof Window ? (Window)parent : SwingUtilities.getWindowAncestor(parent);
        DlgReadmisi form=new DlgReadmisi(owner);
        form.noRawat=noRawat==null ? "" : noRawat.trim();
        if (form.noRawat.isEmpty()) { form.tabs.setSelectedIndex(1); form.rekap(); }
        else form.muat(false);
        form.setVisible(true);
    }
    private DlgReadmisi(Window owner) {
        super(owner,"Readmisi Pasien <= 5 Hari",ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { selesai(); }
        });
        JPanel root=new JPanel(new BorderLayout(0,12)); root.setBackground(new Color(245,248,252));
        root.setBorder(BorderFactory.createEmptyBorder(16,18,12,18)); setContentPane(root);
        JPanel header=new JPanel(new BorderLayout(0,5)); header.setOpaque(false);
        JLabel judul=new JLabel("READMISI PASIEN <= 5 HARI"); judul.setForeground(BIRU);
        judul.setFont(new Font("Segoe UI Semibold",Font.BOLD,20)); header.add(judul,BorderLayout.NORTH);
        header.add(new JLabel("Indikator Mutu Unit Rawat Inap | RS Akademis Jaury Jusuf Putera"),BorderLayout.CENTER);
        root.add(header,BorderLayout.NORTH);
        tabs.addTab("Pencatatan Pasien",input()); tabs.addTab("Rekap Bulanan",daftar()); root.add(tabs,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout()); footer.setOpaque(false); footer.add(status,BorderLayout.CENTER);
        footer.add(tutup,BorderLayout.EAST); root.add(footer,BorderLayout.SOUTH);
        simpan.addActionListener(e -> simpan()); muat.addActionListener(e -> muat(true));
        cari.addActionListener(e -> rekap()); pencarian.addActionListener(e -> rekap());
        cetak.addActionListener(e -> cetak()); buka.addActionListener(e -> bukaCatatan());
        tutup.addActionListener(e -> selesai());
        tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if(e.getClickCount()==2 && !sibuk) bukaCatatan(); }
        });
        getRootPane().registerKeyboardAction(e -> selesai(),javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setMinimumSize(new Dimension(780,620)); setSize(1050,730); setLocationRelativeTo(owner); tombolAktif();
    }
    private JPanel input() {
        JPanel panel=new JPanel(new BorderLayout(0,10)); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14,14,12,14));
        JPanel fields=new JPanel(new GridBagLayout()); fields.setOpaque(false);
        pasangan(fields,0,"No. Rawat Asal",asal,"No. Rawat Masuk",masuk);
        pasangan(fields,1,"No. RM",rm,"Nama Pasien",nama);
        pasangan(fields,2,"Tgl dan Jam Pulang",pulang,"Tgl dan Jam Masuk",datang);
        pasangan(fields,3,"Ruangan Asal",ruangan,"Selisih Waktu",selisih);
        panel.add(fields,BorderLayout.NORTH);
        JPanel isi=new JPanel(new GridBagLayout()); isi.setOpaque(false);
        diagnosa.setEditable(false); diagnosa.setBackground(new Color(241,245,249));
        blok(isi,0,"Diagnosa Akhir (otomatis dari kepulangan asal)",diagnosa,0.6);
        blok(isi,2,"Alasan Readmisi *",alasan,1);
        blok(isi,4,"Keterangan",keterangan,1);
        panel.add(isi,BorderLayout.CENTER);
        JPanel bawah=new JPanel(new BorderLayout(0,8)); bawah.setOpaque(false);
        tipe.setForeground(BIRU); bawah.add(tipe,BorderLayout.NORTH);
        JPanel aksi=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); aksi.setOpaque(false);
        aksi.add(simpan); aksi.add(muat); aksi.add(new JLabel("* Wajib diisi. Batas readmisi: 120 jam sejak pulang."));
        bawah.add(aksi,BorderLayout.SOUTH); panel.add(bawah,BorderLayout.SOUTH); return panel;
    }
    private JPanel daftar() {
        JPanel panel=new JPanel(new BorderLayout(0,10)); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14,14,12,14));
        JPanel filter=new JPanel(new GridLayout(2,1,0,6)); filter.setOpaque(false);
        JPanel baris=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); baris.setOpaque(false);
        bulan.setSelectedIndex(LocalDate.now().getMonthValue()-1); ruang.addItem("Semua Ruangan");
        ruang.setPreferredSize(new Dimension(215,28));
        baris.add(new JLabel("Bulan Masuk")); baris.add(bulan); baris.add(tahun);
        baris.add(new JLabel("Ruangan Asal")); baris.add(ruang); filter.add(baris);
        baris=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); baris.setOpaque(false);
        baris.add(new JLabel("RM / Nama")); baris.add(pencarian); baris.add(cari); baris.add(buka); baris.add(cetak); filter.add(baris);
        panel.add(filter,BorderLayout.NORTH);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabel.setRowHeight(30);
        tabel.setFont(new Font("Segoe UI",Font.PLAIN,12)); tabel.getTableHeader().setReorderingAllowed(false);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths={45,85,155,155,240,220,220};
        for(int i=0;i<widths.length;i++) tabel.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        tabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int r=tabel.rowAtPoint(e.getPoint()),c=tabel.columnAtPoint(e.getPoint());
                if(r<0 || c<0) { tabel.setToolTipText(null); return; }
                Object nilai=tabel.getValueAt(r,c);
                tabel.setToolTipText("<html>"+html(daftar.get(r).nama)+"<br>"+html(String.valueOf(nilai))+"</html>");
            }
        });
        panel.add(new JScrollPane(tabel),BorderLayout.CENTER);
        panel.add(new JLabel("Periode mengikuti tanggal masuk kembali; ruangan mengikuti kamar terakhir saat pulang asal."),BorderLayout.SOUTH);
        return panel;
    }
    private void muat(boolean terbaru) {
        if(sibuk || noRawat.isEmpty()) return;
        if(kotor() && JOptionPane.showConfirmDialog(this,"Muat ulang dan buang perubahan catatan yang belum disimpan?",
                "Muat Ulang",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        String rawat=noRawat;
        kerja("Mengambil riwayat kunjungan...",() -> terbaru ? service.sumberTerbaru(rawat) : service.muat(rawat),this::tampilkan);
    }
    private void tampilkan(ReadmisiService.Data d) {
        data=d; noRawat=d.asal;
        asal.setText(d.asal); masuk.setText(d.masuk); rm.setText(d.rm); nama.setText(d.nama);
        pulang.setText(WAKTU.format(d.pulang)); datang.setText(WAKTU.format(d.datang)); ruangan.setText(d.ruangan);
        long detik=Duration.between(d.pulang,d.datang).getSeconds();
        selisih.setText((detik/86400)+" hari "+(detik%86400/3600)+" jam "+(detik%3600/60)+" menit "+(detik%60)+" detik");
        diagnosa.setText(d.diagnosa); alasan.setText(d.alasan); keterangan.setText(d.keterangan);
        diagnosa.setCaretPosition(0); alasan.setCaretPosition(0); keterangan.setCaretPosition(0);
        tipe.setText(d.perluSimpan ? "Data sumber berubah — periksa lalu Simpan untuk memperbarui snapshot."
                : d.versi==0 ? "Catatan baru — data otomatis belum disimpan." : "Catatan tersimpan — versi "+d.versi);
        tombolAktif();
    }
    private void simpan() {
        if(sibuk || data==null) return;
        ReadmisiService.Data dipilih=data;
        String a=alasan.getText(),k=keterangan.getText(),petugas=akses.getkode(); boolean boleh=akses.getkamar_inap();
        kerja("Menyimpan pencatatan readmisi...",() -> service.simpan(dipilih,a,k,petugas,boleh),d -> {
            tampilkan(d); status.setText("Readmisi berhasil disimpan. Rekap bersumber dari catatan tersimpan.");
        });
    }
    private void rekap() {
        if(sibuk) return;
        try { tahun.commitEdit(); } catch(java.text.ParseException e) { pesan("Tahun tidak valid."); return; }
        LocalDate awal=LocalDate.of(((Number)tahun.getValue()).intValue(),bulan.getSelectedIndex()+1,1);
        String unit=ruang.getSelectedIndex()<=0 ? "" : String.valueOf(ruang.getSelectedItem());
        String teksCari=pencarian.getText().trim();
        kerja("Memuat rekap tersimpan...",() -> new Rekap(service.rekap(awal,awal.plusMonths(1),unit,teksCari),service.ruangan()),hasil -> {
            daftar=hasil.data; model.setRowCount(0); int i=1;
            for(ReadmisiService.Data d:daftar) model.addRow(new Object[]{i++,d.rm,WAKTU.format(d.pulang),
                    WAKTU.format(d.datang),d.alasan,d.diagnosa,d.keterangan});
            ruang.removeAllItems(); ruang.addItem("Semua Ruangan"); for(String s:hasil.ruang) ruang.addItem(s);
            if(!unit.isEmpty()) ruang.setSelectedItem(unit);
            judulRekap="RUANGAN: "+(unit.isEmpty()?"SEMUA RUANGAN":unit)+" / BULAN: "+BULAN[awal.getMonthValue()-1]+" "+awal.getYear();
            if(!teksCari.isEmpty()) judulRekap+=" / PENCARIAN: "+teksCari;
            status.setText("Jumlah catatan readmisi: "+daftar.size()); tombolAktif();
        });
    }
    private static final class Rekap {
        final List<ReadmisiService.Data> data; final List<String> ruang;
        Rekap(List<ReadmisiService.Data> data,List<String> ruang) { this.data=data; this.ruang=ruang; }
    }
    private void bukaCatatan() {
        int row=tabel.getSelectedRow();
        if(sibuk || row<0) { if(!sibuk) pesan("Pilih catatan pada tabel rekap."); return; }
        if(kotor() && JOptionPane.showConfirmDialog(this,"Buka catatan lain dan buang perubahan yang belum disimpan?",
                "Buka Catatan",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        // Muat versi terkini agar tidak menimpa perubahan sejak rekap ditampilkan.
        String rawat=daftar.get(row).asal;
        kerja("Membuka catatan tersimpan...",() -> service.muat(rawat),d -> { tampilkan(d); tabs.setSelectedIndex(0); });
    }
    private boolean kotor() {
        return data!=null && (data.perluSimpan || !alasan.getText().equals(data.alasan) || !keterangan.getText().equals(data.keterangan));
    }
    private void selesai() {
        if(sibuk) return;
        if(!kotor() || JOptionPane.showConfirmDialog(this,"Tutup tanpa menyimpan perubahan catatan?","Tutup",
                JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) dispose();
    }
    private <T> void kerja(String pesan,Callable<T> tugas,Consumer<T> sukses) {
        if(sibuk) return;
        sibuk=true; status.setText(pesan); tombolAktif();
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() throws Exception { return tugas.call(); }
            @Override protected void done() {
                sibuk=false;
                try { T hasil=get(); status.setText("Data siap."); sukses.accept(hasil); }
                catch(Exception e) {
                    Throwable sebab=e instanceof java.util.concurrent.ExecutionException ? e.getCause() : e;
                    if(sebab instanceof ReadmisiService.SumberBerubah) tampilkan(((ReadmisiService.SumberBerubah)sebab).terbaru);
                    status.setText("Proses belum berhasil. "+sebab.getMessage()); pesan(sebab.getMessage());
                } finally { tombolAktif(); }
            }
        }.execute();
    }
    private void tombolAktif() {
        simpan.setEnabled(!sibuk && data!=null && akses.getkamar_inap());
        muat.setEnabled(!sibuk && !noRawat.isEmpty()); cari.setEnabled(!sibuk); buka.setEnabled(!sibuk && !daftar.isEmpty());
        cetak.setEnabled(!sibuk && !daftar.isEmpty()); tutup.setEnabled(!sibuk);
        alasan.setEditable(!sibuk && data!=null); keterangan.setEditable(!sibuk && data!=null);
        bulan.setEnabled(!sibuk); tahun.setEnabled(!sibuk); ruang.setEnabled(!sibuk); pencarian.setEnabled(!sibuk);
    }
    private void cetak() {
        if(sibuk || daftar.isEmpty()) return;
        // Isi dan judul dicetak dari hasil Tampilkan terakhir, bukan filter yang belum diterapkan.
        StringBuilder h=new StringBuilder("<html><head><style>body{font-family:Arial;font-size:9pt;}th,td{padding:4px;}"
                + "th{text-align:center;}td{vertical-align:top;}</style></head><body><div style='text-align:center'>"
                + "<b>INDIKATOR MUTU UNIT<br>FORM ANGKA PASIEN READMISI &le;5 HARI<br>RS AKADEMIS JAURY JUSUF PUTERA</b><br>"
                + html(judulRekap)+"</div><br><table border='1' cellspacing='0' width='100%'><tr>"
                + "<th>NO.</th><th>RM</th><th>TGL DAN JAM<br>PULANG</th><th>TGL DAN JAM<br>MASUK</th>"
                + "<th>ALASAN READMISI</th><th>DIAGNOSA</th><th>KET.</th></tr>");
        int n=1;
        for(ReadmisiService.Data d:daftar) h.append("<tr><td>").append(n++).append("</td><td>")
                .append(html(d.rm)).append("</td><td>").append(html(WAKTU.format(d.pulang)))
                .append("</td><td>").append(html(WAKTU.format(d.datang))).append("</td><td>")
                .append(html(d.alasan)).append("</td><td>").append(html(d.diagnosa)).append("</td><td>")
                .append(html(d.keterangan)).append("</td></tr>");
        h.append("</table><br>Jumlah catatan readmisi: ").append(daftar.size()).append("</body></html>");
        JEditorPane lembar=new JEditorPane("text/html",h.toString()); lembar.setEditable(false);
        PrinterJob job=PrinterJob.getPrinterJob(); job.setJobName("Rekap Readmisi RSAJ");
        Paper paper=new Paper(); paper.setSize(612,936); paper.setImageableArea(24,24,564,888);
        PageFormat page=new PageFormat(); page.setPaper(paper); page.setOrientation(PageFormat.LANDSCAPE);
        job.setPrintable(lembar.getPrintable(null,new java.text.MessageFormat("Halaman {0}")),page);
        if(job.printDialog()) kerja("Mencetak rekap...",() -> { job.print(); return true; },v -> status.setText("Rekap selesai dikirim ke printer."));
    }
    static String html(String s) {
        return s==null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;").replace("\n","<br>");
    }
    private void pesan(String pesan) { JOptionPane.showMessageDialog(this,pesan,"Readmisi",JOptionPane.INFORMATION_MESSAGE); }
    private static JTextField otomatis() {
        JTextField f=new JTextField(); f.setEditable(false); f.setBackground(new Color(241,245,249));
        f.setFont(new Font("Segoe UI",Font.PLAIN,12)); return f;
    }
    private static JTextArea area(int rows) {
        JTextArea a=new JTextArea(rows,30); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setMargin(new Insets(7,9,7,9)); a.setFont(new Font("Segoe UI",Font.PLAIN,13)); return a;
    }
    private static JButton tombol(String text) {
        JButton b=new JButton(text); b.setFont(new Font("Segoe UI",Font.PLAIN,12));
        b.setMargin(new Insets(7,14,7,14)); return b;
    }
    private static void pasangan(JPanel p,int row,String l1,JComponent c1,String l2,JComponent c2) {
        GridBagConstraints g=new GridBagConstraints(); g.gridy=row; g.insets=new Insets(4,3,4,8); g.fill=GridBagConstraints.HORIZONTAL;
        g.gridx=0; g.weightx=0; p.add(new JLabel(l1),g); g.gridx=1; g.weightx=0.5; p.add(c1,g);
        g.gridx=2; g.weightx=0; p.add(new JLabel(l2),g); g.gridx=3; g.weightx=0.5; p.add(c2,g);
        c1.setPreferredSize(new Dimension(170,29)); c2.setPreferredSize(new Dimension(190,29));
    }
    private static void blok(JPanel p,int row,String label,JTextArea area,double weight) {
        GridBagConstraints g=new GridBagConstraints(); g.gridx=0; g.gridy=row; g.weightx=1;
        g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(4,3,4,3); p.add(new JLabel(label),g);
        g.gridy++; g.weighty=weight; g.fill=GridBagConstraints.BOTH; p.add(new JScrollPane(area),g);
    }
}
