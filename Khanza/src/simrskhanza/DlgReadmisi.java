package simrskhanza;

import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

/** Pencatatan kejadian readmisi, termasuk nomor rawat hasil penggabungan. */
public final class DlgReadmisi extends JDialog {
    private static final long serialVersionUID=1L;
    private static final Color BIRU=ReadmisiUi.BLUE;
    private static final DateTimeFormatter WAKTU=DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final String[] BULAN={"Januari","Februari","Maret","April","Mei","Juni",
            "Juli","Agustus","September","Oktober","November","Desember"};
    private final ReadmisiService service=new ReadmisiService();
    private final JTabbedPane tabs=new JTabbedPane();
    private final JTextField asal=otomatis(), masuk=otomatis(), rm=otomatis(), nama=otomatis();
    private final JTextField pulang=otomatis(), datang=otomatis(), ruangan=otomatis(), selisih=otomatis();
    private final JTextArea diagnosa=ReadmisiUi.area(), alasan=ReadmisiUi.area(), keterangan=ReadmisiUi.area(), koreksi=ReadmisiUi.area();
    private final javax.swing.JCheckBox manual=new javax.swing.JCheckBox("Koreksi / input manual");
    private final JButton pilih=ReadmisiUi.button("Pilih riwayat","calendar (1).png",false),
            baru=ReadmisiUi.button("Catatan baru","add-document (1).png",false),
            audit=ReadmisiUi.button("Riwayat perubahan","report24.png",false);
    private boolean pilihanBelumDisimpan;
    private JScrollPane inputScroll;
    private JPanel koreksiBlock;
    private static final DateTimeFormatter INPUT_WAKTU=DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm:ss")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);
    private final JLabel status=new JLabel(" "), tipe=new JLabel(" ");
    private final JButton simpan=ReadmisiUi.button("Simpan catatan","save-16x16.png",true),
            muat=ReadmisiUi.button("Muat ulang","refresh1.png",false), cari=ReadmisiUi.button("Tampilkan","Search-16x16.png",true);
    private final JButton cetak=ReadmisiUi.button("Cetak rekap","printer_blue.png",false),
            buka=ReadmisiUi.button("Buka catatan","add-document (1).png",false), tutup=ReadmisiUi.button("Tutup","password-r5/close.png",false);
    private final JComboBox<String> bulan=new JComboBox<>(BULAN), ruang=new JComboBox<>();
    private final JSpinner tahun=new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(),2000,2200,1));
    private final JTextField pencarian=ReadmisiUi.searchField();
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"No.","No. RM","Tgl dan Jam Pulang",
            "Tgl dan Jam Masuk","Alasan Readmisi","Diagnosa","Keterangan"},0) {
        private static final long serialVersionUID=1L;
        @Override public boolean isCellEditable(int r,int c) { return false; }
    };
    private final JTable tabel=new ReadmisiUi.ModernTable(model);
    private final JLabel jumlah=ReadmisiUi.label("0 catatan",11,true,BIRU);
    private final JLabel periode=ReadmisiUi.label("Pilih periode untuk melihat catatan tersimpan",11,false,ReadmisiUi.MUTED);
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
        super(owner,"Readmisi Pasien \u2264 5 Hari",ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { selesai(); }
        });
        JPanel root=new JPanel(new BorderLayout(0,14));root.setBackground(ReadmisiUi.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18,22,14,22));setContentPane(root);
        JPanel header=ReadmisiUi.clear(new BorderLayout(14,0));
        ReadmisiUi.Card tile=new ReadmisiUi.Card(new java.awt.GridBagLayout(),Color.WHITE,false);
        tile.setPreferredSize(new Dimension(52,52));
        JLabel ico=new JLabel(ReadmisiUi.icon("userblue.png",24));tile.add(ico);header.add(tile,BorderLayout.WEST);
        JPanel text=ReadmisiUi.clear(new BorderLayout(0,4));
        text.add(ReadmisiUi.label("Readmisi pasien",23,true,ReadmisiUi.INK),BorderLayout.NORTH);
        text.add(ReadmisiUi.label("Indikator mutu rawat inap  \u00b7  RS Akademis Jaury Jusuf Putera",12,false,ReadmisiUi.MUTED),BorderLayout.CENTER);
        header.add(text,BorderLayout.CENTER);
        JPanel limit=ReadmisiUi.clear(new java.awt.GridBagLayout());limit.add(ReadmisiUi.pill("\u2264 5 hari  \u00b7  120 jam"));header.add(limit,BorderLayout.EAST);
        root.add(header,BorderLayout.NORTH);
        ReadmisiUi.tabs(tabs);
        tabs.addTab("Pencatatan pasien",ReadmisiUi.icon("userblue.png",16),input());
        tabs.addTab("Rekap bulanan",ReadmisiUi.icon("calendar (1).png",16),daftar());root.add(tabs,BorderLayout.CENTER);
        JPanel footer=ReadmisiUi.clear(new BorderLayout(12,0));
        status.setFont(ReadmisiUi.font(11,false));status.setForeground(ReadmisiUi.MUTED);
        status.setText("Siap untuk pencatatan readmisi.");
        status.setIcon(ReadmisiUi.icon("password-r5/info.png",16));status.setIconTextGap(7);
        status.addPropertyChangeListener("text",e->status.setToolTipText(String.valueOf(e.getNewValue())));
        footer.add(status,BorderLayout.CENTER);footer.add(tutup,BorderLayout.EAST);root.add(footer,BorderLayout.SOUTH);
        simpan.addActionListener(e -> simpan());muat.addActionListener(e -> muat(true));
        cari.addActionListener(e -> rekap());pencarian.addActionListener(e -> rekap());
        cetak.addActionListener(e -> cetak());buka.addActionListener(e -> bukaCatatan());tutup.addActionListener(e -> selesai());
        pilih.addActionListener(e -> pilihRiwayat());baru.addActionListener(e -> baru());audit.addActionListener(e -> audit());
        manual.setFont(ReadmisiUi.font(12,true));manual.setForeground(BIRU);manual.setOpaque(false);
        manual.addActionListener(e -> ubahMode());
        javax.swing.event.DocumentListener hitung=new javax.swing.event.DocumentListener(){
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e){hitungSelisih();}
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e){hitungSelisih();}
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e){hitungSelisih();}
        };
        pulang.getDocument().addDocumentListener(hitung);datang.getDocument().addDocumentListener(hitung);
        pulang.setToolTipText("Tanggal pulang sebelumnya: dd-MM-yyyy HH:mm:ss");
        datang.setToolTipText("Tanggal masuk kembali: dd-MM-yyyy HH:mm:ss");
        tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if(e.getClickCount()==2 && !sibuk) bukaCatatan(); }
        });
        getRootPane().registerKeyboardAction(e -> selesai(),javax.swing.KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        Dimension screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(860,620));setSize(Math.min(1120,screen.width-44),Math.min(800,screen.height-52));
        setLocationRelativeTo(owner);tombolAktif();
    }
    private JPanel input() {
        JPanel panel=ReadmisiUi.clear(new BorderLayout(0,10));
        JPanel body=ReadmisiUi.clear(new java.awt.GridBagLayout());
        JPanel identity=ReadmisiUi.card(new BorderLayout(0,0));
        identity.add(ReadmisiUi.heading("Identitas pasien","userblue.png","Data otomatis"),BorderLayout.NORTH);
        JPanel patientFields=ReadmisiUi.clear(new GridLayout(2,1,0,10));
        patientFields.add(ReadmisiUi.pair(ReadmisiUi.field("No. RM",rm),ReadmisiUi.field("Nama pasien",nama),0.26));
        patientFields.add(ReadmisiUi.pair(ReadmisiUi.field("No. rawat referensi",asal),ReadmisiUi.field("No. rawat masuk (jika ada)",masuk),0.5));
        identity.add(patientFields,BorderLayout.CENTER);
        JPanel journey=ReadmisiUi.card(new BorderLayout(0,0));
        journey.add(ReadmisiUi.heading("Riwayat perawatan","calendar (1).png",null),BorderLayout.NORTH);
        JPanel dates=ReadmisiUi.clear(new GridLayout(2,1,0,10));
        dates.add(ReadmisiUi.pair(ReadmisiUi.field("Pulang sebelumnya",pulang),ReadmisiUi.field("Masuk kembali",datang),0.5));
        dates.add(ReadmisiUi.pair(ReadmisiUi.field("Ruangan asal",ruangan),ReadmisiUi.field("Selisih waktu",selisih),0.44));
        journey.add(dates,BorderLayout.CENTER);
        JPanel top=ReadmisiUi.pair(identity,journey,0.5);
        java.awt.GridBagConstraints g=new java.awt.GridBagConstraints();g.gridx=0;g.gridy=0;g.weightx=1;g.fill=java.awt.GridBagConstraints.HORIZONTAL;
        g.insets=new Insets(0,0,12,0);body.add(top,g);
        JPanel mode=ReadmisiUi.card(new BorderLayout(0,6));
        JPanel choose=ReadmisiUi.clear(new FlowLayout(FlowLayout.LEFT,8,0));choose.add(pilih);choose.add(manual);
        choose.add(ReadmisiUi.label("Format: dd-MM-yyyy HH:mm:ss",11,false,ReadmisiUi.MUTED));mode.add(choose,BorderLayout.NORTH);
        mode.add(ReadmisiUi.label("Nomor rawat boleh sama. Bila riwayat tidak lengkap setelah digabung, gunakan input manual dengan alasan.",11,false,ReadmisiUi.MUTED),BorderLayout.SOUTH);
        g.gridy++;body.add(mode,g);
        JPanel notes=ReadmisiUi.card(new BorderLayout(0,0));
        notes.add(ReadmisiUi.heading("Catatan readmisi","add-document (1).png","* Wajib diisi"),BorderLayout.NORTH);
        JPanel fields=ReadmisiUi.clear(new java.awt.GridBagLayout());
        alasan.putClientProperty("readmisi.placeholder","Tulis alasan pasien kembali dirawat...");
        keterangan.putClientProperty("readmisi.placeholder","Tambahkan keterangan bila diperlukan...");
        catatan(fields,0,"Diagnosa akhir perawatan sebelumnya",diagnosa,true);
        catatan(fields,1,"Alasan readmisi *",alasan,false);
        catatan(fields,2,"Keterangan",keterangan,false);
        koreksi.putClientProperty("readmisi.placeholder","Wajib untuk input manual atau perubahan tanggal / diagnosa / ruangan...");
        catatan(fields,3,"Alasan koreksi / input manual",koreksi,false);
        notes.add(fields,BorderLayout.CENTER);g.gridy++;body.add(notes,g);
        // Seluruh ruang kosong ada di bawah; textbox tetap mengikuti jumlah baris, bukan tinggi jendela.
        g.gridy++;g.weighty=1;g.insets=new Insets(0,0,0,0);g.fill=java.awt.GridBagConstraints.BOTH;
        body.add(ReadmisiUi.clear(new BorderLayout()),g);
        javax.swing.JScrollPane scroll=ReadmisiUi.scroll(body);scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        inputScroll=scroll;panel.add(scroll,BorderLayout.CENTER);
        JPanel actions=ReadmisiUi.clear(new BorderLayout(8,7));
        tipe.setFont(ReadmisiUi.font(11,false));tipe.setForeground(BIRU);tipe.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
        actions.add(tipe,BorderLayout.NORTH);
        JPanel buttons=ReadmisiUi.clear(new FlowLayout(FlowLayout.LEFT,8,0));buttons.add(simpan);buttons.add(baru);buttons.add(muat);buttons.add(audit);
actions.add(buttons,BorderLayout.CENTER);panel.add(actions,BorderLayout.SOUTH);
        return panel;
    }
    private void catatan(JPanel parent,int row,String title,JTextArea a,boolean readonly) {
        JPanel block=ReadmisiUi.clear(new BorderLayout(0,5));
        if(row==3)koreksiBlock=block;
        JLabel caption=ReadmisiUi.label(title,11.5f,true,ReadmisiUi.MUTED);block.add(caption,BorderLayout.NORTH);
        ReadmisiUi.GrowingArea growing=new ReadmisiUi.GrowingArea(a,readonly);block.add(growing,BorderLayout.CENTER);
        java.awt.GridBagConstraints g=new java.awt.GridBagConstraints();g.gridx=0;g.gridy=row;g.weightx=1;g.fill=java.awt.GridBagConstraints.HORIZONTAL;
        g.insets=new Insets(0,0,row==3?0:10,0);parent.add(block,g);
    }
    private JPanel daftar() {
        JPanel panel=ReadmisiUi.clear(new BorderLayout(0,12));
        JPanel filter=ReadmisiUi.card(new BorderLayout());
        filter.add(ReadmisiUi.heading("Filter rekap","Search-16x16.png","Berdasarkan tanggal masuk kembali"),BorderLayout.NORTH);
        bulan.setSelectedIndex(LocalDate.now().getMonthValue()-1);ruang.addItem("Semua ruangan");
        ReadmisiUi.combo(bulan);ReadmisiUi.combo(ruang);ReadmisiUi.spinner(tahun);ReadmisiUi.styleField(pencarian,false);
        pencarian.setToolTipText("Cari nomor rekam medis atau nama pasien");
        JPanel fields=ReadmisiUi.clear(new java.awt.GridBagLayout());
        JComponent[] controls={ReadmisiUi.field("Bulan masuk",bulan),ReadmisiUi.field("Tahun",tahun),
                ReadmisiUi.field("Ruangan asal",ruang),ReadmisiUi.field("No. RM / nama pasien",pencarian),cari};
        double[] weights={0.18,0,0.30,0.30,0};
        for(int i=0;i<controls.length;i++){
            java.awt.GridBagConstraints g=new java.awt.GridBagConstraints();g.gridx=i;g.weightx=weights[i];g.fill=java.awt.GridBagConstraints.HORIZONTAL;
            g.anchor=java.awt.GridBagConstraints.SOUTH;g.insets=new Insets(0,0,0,i==controls.length-1?0:10);fields.add(controls[i],g);
        }
        filter.add(fields,BorderLayout.CENTER);panel.add(filter,BorderLayout.NORTH);
        JPanel results=ReadmisiUi.card(new BorderLayout(0,12));
        JPanel toolbar=ReadmisiUi.clear(new BorderLayout(10,0));
        JPanel summary=ReadmisiUi.clear(new BorderLayout(0,3));
        JPanel title=ReadmisiUi.clear(new FlowLayout(FlowLayout.LEFT,8,0));
        title.add(ReadmisiUi.label("Daftar readmisi",15,true,ReadmisiUi.INK));
        jumlah.setBorder(BorderFactory.createEmptyBorder(3,7,3,7));title.add(jumlah);
        summary.add(title,BorderLayout.NORTH);summary.add(periode,BorderLayout.SOUTH);toolbar.add(summary,BorderLayout.CENTER);
        JPanel tools=ReadmisiUi.clear(new FlowLayout(FlowLayout.RIGHT,8,0));tools.add(buka);tools.add(cetak);toolbar.add(tools,BorderLayout.EAST);
        results.add(toolbar,BorderLayout.NORTH);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);ReadmisiUi.table(tabel);
        int[] widths={42,82,145,145,210,190,190};int[] minimum={34,65,100,100,110,100,100};
        for(int i=0;i<widths.length;i++){tabel.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);tabel.getColumnModel().getColumn(i).setMinWidth(minimum[i]);}
        tabel.getColumnModel().getColumn(0).setMaxWidth(48);tabel.getColumnModel().getColumn(1).setMaxWidth(110);
        tabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int r=tabel.rowAtPoint(e.getPoint()),c=tabel.columnAtPoint(e.getPoint());
                if(r<0 || c<0) { tabel.setToolTipText(null); return; }
                Object value=tabel.getValueAt(r,c);tabel.setToolTipText("<html>"+html(daftar.get(r).nama)+"<br>"+html(String.valueOf(value))+"</html>");
            }
        });
        model.addTableModelListener(e->{jumlah.setText(model.getRowCount()+" catatan");tabel.repaint();});
        JScrollPane tableScroll=ReadmisiUi.scroll(tabel);tableScroll.getViewport().setBackground(Color.WHITE);tableScroll.getViewport().setOpaque(true);
        tableScroll.setBorder(BorderFactory.createLineBorder(ReadmisiUi.LINE));results.add(tableScroll,BorderLayout.CENTER);
        JLabel hint=ReadmisiUi.label("Ruangan mengikuti kamar terakhir pada kepulangan asal.",11,false,ReadmisiUi.MUTED);
        hint.setIcon(ReadmisiUi.icon("hospital-bed.png",16));hint.setIconTextGap(7);results.add(hint,BorderLayout.SOUTH);
        panel.add(results,BorderLayout.CENTER);return panel;
    }
    private void muat(boolean terbaru) {
        if(sibuk || (noRawat.isEmpty() && data==null))return;
        if(!bolehBuang("Muat ulang dan buang perubahan yang belum disimpan?"))return;
        long id=data==null?0:data.id;String ref=noRawat;
        kerja("Memuat data readmisi...",()->{
            if(id>0)return service.baca(id);
            ReadmisiService.Data ctx=service.konteks(ref);List<ReadmisiService.Data> list=service.riwayat(ref,ctx.rm);
            ReadmisiService.Data satu=null;int count=0;
            for(ReadmisiService.Data h:list)if(h.asal.equals(ref)||h.masuk.equals(ref)){satu=h;count++;}
            return count==1?satu:ctx;
        },d->{tampilkan(d);if(d.pulang==null)status.setText("Pilih pasangan riwayat atau aktifkan koreksi / input manual.");});
    }
    private void tampilkan(ReadmisiService.Data d) {
        data=d.copy();noRawat=d.referensi;pilihanBelumDisimpan=false;
        asal.setText(d.referensi);masuk.setText(d.masuk);rm.setText(d.rm);nama.setText(d.nama);
        pulang.setText(format(d.pulang));datang.setText(format(d.datang));ruangan.setText(d.ruangan);
        diagnosa.setText(d.diagnosa);alasan.setText(d.alasan);keterangan.setText(d.keterangan);koreksi.setText(d.alasanKoreksi);
        manual.setSelected("MANUAL".equals(d.sumber));
        diagnosa.setCaretPosition(0);alasan.setCaretPosition(0);keterangan.setCaretPosition(0);koreksi.setCaretPosition(0);
        tipe.setText(d.id==0?"Kejadian baru \u00b7 "+(d.sumber.isEmpty()?"pilih riwayat / manual":d.sumber)
                :"Catatan #"+d.id+" \u00b7 versi "+d.versi+" \u00b7 "+d.sumber);
        hitungSelisih();tombolAktif();
        SwingUtilities.invokeLater(()->{if(inputScroll!=null)inputScroll.getVerticalScrollBar().setValue(0);});
    }
    private static String format(java.time.LocalDateTime t){return t==null?"":WAKTU.format(t);}
    static java.time.LocalDateTime parseWaktu(String text)throws Exception{
        try{return java.time.LocalDateTime.parse(text.trim(),INPUT_WAKTU);}
        catch(Exception e){throw new Exception("Tanggal/jam harus valid dengan format dd-MM-yyyy HH:mm:ss.");}
    }
    private void hitungSelisih(){
        try{
            long seconds=Duration.between(parseWaktu(pulang.getText()),parseWaktu(datang.getText())).getSeconds();
            if(seconds<0){selisih.setText("Masuk sebelum pulang");return;}
            selisih.setText((seconds/86400)+" hari \u00b7 "+(seconds%86400/3600)+" jam "+(seconds%3600/60)+" mnt"
                    +(seconds%60==0?"":" "+(seconds%60)+" dtk")+(seconds>432000?" (lebih 5 hari)":""));
        }catch(Exception e){selisih.setText(pulang.getText().isEmpty() && datang.getText().isEmpty()?"Belum dipilih":"Periksa format tanggal");}
    }
    private void ubahMode(){
        if(data==null)return;
        if(!manual.isSelected()){
            if(!"MANUAL".equals(data.sumber)){
                if(JOptionPane.showConfirmDialog(this,"Kembali ke nilai riwayat yang terakhir dipilih? Perubahan manual pada tanggal/diagnosa/ruangan akan dibuang.",
                        "Kembali ke riwayat",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){manual.setSelected(true);return;}
                pulang.setText(format(data.pulang));datang.setText(format(data.datang));diagnosa.setText(data.diagnosa);ruangan.setText(data.ruangan);
            }else{
                manual.setSelected(true);pesan("Catatan ini berasal dari input manual. Gunakan Pilih riwayat bila ingin mengganti sumbernya.");
            }
        }
        tombolAktif();
    }
    private void pilihRiwayat(){
        if(sibuk || data==null)return;
        ReadmisiService.Data ctx=data.copy();
        kerja("Mencari pasangan pulang dan masuk kembali...",()->service.riwayat(ctx.referensi,ctx.rm),list->{
            if(list.isEmpty()){pesan("Pasangan riwayat yang valid belum ditemukan. Gunakan koreksi / input manual dan isi alasan sesuai data yang telah diverifikasi.");return;}
            DefaultTableModel m=new DefaultTableModel(new String[]{"Pulang sebelumnya","Masuk kembali","No. rawat pulang","No. rawat masuk","Ruangan asal","Diagnosa"},0){
                private static final long serialVersionUID=1L;@Override public boolean isCellEditable(int r,int c){return false;}
            };
            for(ReadmisiService.Data h:list)m.addRow(new Object[]{format(h.pulang),format(h.datang),h.asal,h.masuk,h.ruangan,h.diagnosa});
            JTable t=new JTable(m);ReadmisiUi.table(t);t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setRowSelectionInterval(0,0);
            JDialog dialog=new JDialog(this,"Pilih riwayat readmisi",true);JPanel p=ReadmisiUi.clear(new BorderLayout(0,12));
            p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));p.setBackground(ReadmisiUi.BG);p.setOpaque(true);
            p.add(ReadmisiUi.label("Pilih kejadian yang sesuai. Nomor rawat pulang dan masuk boleh sama.",12,true,ReadmisiUi.INK),BorderLayout.NORTH);
            p.add(ReadmisiUi.scroll(t),BorderLayout.CENTER);JPanel buttons=ReadmisiUi.clear(new FlowLayout(FlowLayout.RIGHT));
            JButton use=ReadmisiUi.button("Gunakan riwayat","save-16x16.png",true),cancel=ReadmisiUi.button("Batal","password-r5/close.png",false);
            buttons.add(cancel);buttons.add(use);p.add(buttons,BorderLayout.SOUTH);dialog.setContentPane(p);
            use.addActionListener(e->{
                int row=t.getSelectedRow();if(row<0)return;
                ReadmisiService.Data h=list.get(row).copy();h.id=data.id;h.versi=data.versi;
                h.alasan=alasan.getText();h.keterangan=keterangan.getText();h.alasanKoreksi=koreksi.getText();
                boolean changed=data.id>0 && (!data.klinisSama(h) || !data.sumber.equals(h.sumber));tampilkan(h);pilihanBelumDisimpan=changed;
                dialog.dispose();
            });cancel.addActionListener(e->dialog.dispose());dialog.setSize(Math.min(1020,getWidth()),440);dialog.setLocationRelativeTo(this);dialog.setVisible(true);
        });
    }
    private void baru(){
        if(sibuk || data==null || !bolehBuang("Buat catatan baru dan buang perubahan yang belum disimpan?"))return;
        ReadmisiService.Data n=new ReadmisiService.Data();n.referensi=data.referensi;n.rm=data.rm;n.nama=data.nama;tampilkan(n);
        status.setText("Kejadian baru: pilih riwayat atau gunakan input manual.");
    }
    private ReadmisiService.Data inputData()throws Exception{
        ReadmisiService.Data d=data.copy();d.alasan=alasan.getText();d.keterangan=keterangan.getText();d.alasanKoreksi=koreksi.getText();
        if(manual.isSelected()){
            d.sumber="MANUAL";d.pulang=parseWaktu(pulang.getText());d.datang=parseWaktu(datang.getText());
            d.diagnosa=diagnosa.getText();d.ruangan=ruangan.getText();
            if(!d.ruangan.equals(data.ruangan)){d.kamar="";d.bangsal="";}
            if(!java.util.Objects.equals(d.pulang,data.pulang)||!java.util.Objects.equals(d.datang,data.datang)){d.asal="";d.masuk="";}
        }
        return d;
    }
    private void simpan(){
        if(sibuk || data==null)return;
        try{
            ReadmisiService.Data d=inputData();String petugas=akses.getkode();boolean boleh=akses.getkamar_inap();
            ReadmisiService.validasi(d,petugas,boleh);
            kerja("Menyimpan kejadian readmisi...",()->service.simpan(d,petugas,boleh),saved->{tampilkan(saved);status.setText("Catatan #"+saved.id+" berhasil disimpan beserta riwayat perubahannya.");});
        }catch(Exception e){pesan(e.getMessage());}
    }
    private boolean bolehBuang(String message){return !kotor() || JOptionPane.showConfirmDialog(this,message,"Readmisi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;}
    private void audit(){
        if(sibuk || data==null || data.id==0)return;long id=data.id;
        kerja("Memuat riwayat perubahan...",()->service.audit(id),rows->{
            DefaultTableModel m=new DefaultTableModel(new String[]{"Versi","Aksi","Waktu","Petugas","Pulang","Masuk kembali","Ruangan","Diagnosa","Alasan readmisi","Keterangan","Sumber","Alasan koreksi"},0){
                private static final long serialVersionUID=1L;@Override public boolean isCellEditable(int r,int c){return false;}
            };for(String[] r:rows)m.addRow(r);JTable t=new JTable(m);ReadmisiUi.table(t);t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for(int i=0;i<t.getColumnCount();i++)t.getColumnModel().getColumn(i).setPreferredWidth(i<2?60:i==2?150:180);
            JScrollPane scroll=ReadmisiUi.scroll(t);scroll.setPreferredSize(new Dimension(Math.min(1000,getWidth()-70),360));
            JOptionPane.showMessageDialog(this,scroll,"Riwayat perubahan catatan #"+id,JOptionPane.PLAIN_MESSAGE);
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
            ruang.removeAllItems(); ruang.addItem("Semua ruangan"); for(String s:hasil.ruang) ruang.addItem(s);
            if(!unit.isEmpty()) ruang.setSelectedItem(unit);
            judulRekap="RUANGAN: "+(unit.isEmpty()?"SEMUA RUANGAN":unit)+" / BULAN: "+BULAN[awal.getMonthValue()-1]+" "+awal.getYear();
            if(!teksCari.isEmpty()) judulRekap+=" / PENCARIAN: "+teksCari;
            periode.setText(BULAN[awal.getMonthValue()-1]+" "+awal.getYear()+"  \u00b7  "+(unit.isEmpty()?"Semua ruangan":unit));
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
        long id=daftar.get(row).id;
        kerja("Membuka catatan tersimpan...",() -> service.baca(id),d -> { tampilkan(d); tabs.setSelectedIndex(0); });
    }
    private boolean kotor(){
        return data!=null && (pilihanBelumDisimpan || !alasan.getText().equals(data.alasan) || !keterangan.getText().equals(data.keterangan)
            || !koreksi.getText().equals(data.alasanKoreksi) || !pulang.getText().equals(format(data.pulang)) || !datang.getText().equals(format(data.datang))
            || !diagnosa.getText().equals(data.diagnosa) || !ruangan.getText().equals(data.ruangan) || manual.isSelected()!=data.sumber.equals("MANUAL"));
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
                    status.setText("Proses belum berhasil. "+sebab.getMessage()); pesan(sebab.getMessage());
                } finally { tombolAktif(); }
            }
        }.execute();
    }
    private void tombolAktif(){
        boolean ready=!sibuk && data!=null;
        simpan.setEnabled(ready && akses.getkamar_inap());muat.setEnabled(!sibuk && (!noRawat.isEmpty() || data!=null));
        cari.setEnabled(!sibuk);buka.setEnabled(!sibuk && !daftar.isEmpty());cetak.setEnabled(!sibuk && !daftar.isEmpty());tutup.setEnabled(!sibuk);
        pilih.setEnabled(ready);baru.setEnabled(ready);manual.setEnabled(ready);audit.setEnabled(ready && data.id>0);
        alasan.setEditable(ready);keterangan.setEditable(ready);koreksi.setEditable(ready);
        if(koreksiBlock!=null){
            boolean show=ready && (manual.isSelected() || data.id>0 || !koreksi.getText().isEmpty());
            if(koreksiBlock.isVisible()!=show){koreksiBlock.setVisible(show);koreksiBlock.getParent().revalidate();}
        }
        boolean edit=ready && manual.isSelected();pulang.setEditable(edit);datang.setEditable(edit);diagnosa.setEditable(edit);ruangan.setEditable(edit);
        pulang.repaint();datang.repaint();ruangan.repaint();diagnosa.getParent().repaint();
        bulan.setEnabled(!sibuk);tahun.setEnabled(!sibuk);ruang.setEnabled(!sibuk);pencarian.setEnabled(!sibuk);
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
    private static JTextField otomatis() { return ReadmisiUi.readonly(); }
}
