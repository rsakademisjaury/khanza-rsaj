package bridging;

import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Form mandiri pengelolaan Lepas Rawat, ditulis untuk Daengmuma / RSAJ. */
public final class SuratLepasRawatForm extends JDialog {
    private final SuratLepasRawatService service=new SuratLepasRawatService();
    private final JTextField rm=new JTextField(12);
    private final JLabel pasien=new JLabel("Masukkan No. RM pasien");
    private final JLabel identitas=new JLabel("Pilih episode rawat inap / surat pada daftar");
    private final JLabel awal=new JLabel("—");
    private final JLabel aktif=new JLabel("—");
    private final JLabel status=new JLabel(" ");
    private final JLabel sumber=new JLabel(" ");
    private final JTextArea alasan=new JTextArea(2,45);
    private final JTextArea klinis=new JTextArea();
    private final JSpinner tanggal=new JSpinner(new SpinnerDateModel());
    private final DefaultTableModel model=readonly("No. Rawat Ranap","SEP Asal","No. SKDP","Tgl. Surat", "Tgl. Kontrol", "Dokter", "Poli");
    private final DefaultTableModel audit=readonly("Waktu","Aksi","Tanggal Lama","Tanggal Baru","Petugas","Alasan","Sumber");
    private final JTable tabel=new JTable(model);
    private final JTable riwayat=new JTable(audit);
    private final JButton cari=button("Cari / Muat Ulang");
    private final JButton buat=button("Buat Surat Awal");
    private final JButton simpan=button("Simpan Tanggal Kontrol");
    private final JButton cek=button("Cek Status BPJS");
    private final JButton cetak=button("Cetak");
    private final JButton upload=button("Upload Lepas Rawat");
    private final JButton berkas=button("Perbarui Berkas");
    private final JButton tautkan=button("Pilih Berkas Lama");
    private final JButton tutup=button("Tutup");
    private List<SuratLepasRawatService.Data> data=new ArrayList<>();
    private SuratLepasRawatService.Data dipilih;
    private String pilihSurat="";
    private boolean sibuk;
    private long versiRiwayat;
    private static final Color BIRU=new Color(24,103,177);

    /** Perubahan tanggal surat ranap dari tombol Ganti diarahkan ke form beralasan/audit. */
    static boolean arahkanEditTanggal(Component pemilik,java.sql.Connection c,String nomor,String tanggalBaru) {
        if(nomor==null||nomor.trim().isEmpty()) return false;
        try(java.sql.PreparedStatement ps=c.prepareStatement("SELECT s.nomr,k.tgl_rencana FROM bridging_surat_kontrol_bpjs k "
                + "INNER JOIN bridging_sep s ON s.no_sep=k.no_sep WHERE k.no_surat=? AND s.jnspelayanan='1'")) {
            ps.setString(1,nomor);
            try(java.sql.ResultSet rs=ps.executeQuery()) {
                if(!rs.next()) return false;
                String norm=rs.getString(1),lama=rs.getString(2);
                if(!java.util.Objects.equals(lama,tanggalBaru)) {
                    buka(pemilik,norm,nomor); return true;
                }
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(pemilik,"Gagal memeriksa Surat Lepas Rawat: "+e.getMessage()); return true;
        }
        return false;
    }

    public static void buka(Component pemilik, String noRM, String nomorSurat) {
        if (!akses.getbpjs_surat_kontrol()) {
            JOptionPane.showMessageDialog(pemilik,"Anda tidak memiliki hak akses Surat Kontrol BPJS."); return;
        }
        Window window=pemilik instanceof Window ? (Window)pemilik : SwingUtilities.getWindowAncestor(pemilik);
        SuratLepasRawatForm form=new SuratLepasRawatForm(window);
        form.rm.setText(noRM==null ? "" : noRM.trim());
        form.pilihSurat=nomorSurat==null ? "" : nomorSurat;
        form.setVisible(true);
        if (!form.rm.getText().isEmpty()) form.muat();
    }
    private SuratLepasRawatForm(Window owner) {
        super(owner,"Surat Lepas Rawat",ModalityType.MODELESS);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { if (!sibuk) dispose(); }
        });
        JPanel root=new JPanel(new BorderLayout(0,12)); root.setBackground(new Color(244,247,251));
        root.setBorder(BorderFactory.createEmptyBorder(16,18,12,18)); setContentPane(root);
        JPanel atas=new JPanel(new BorderLayout(0,10)); atas.setOpaque(false);
        JLabel judul=new JLabel("SURAT LEPAS RAWAT"); judul.setForeground(BIRU);
        judul.setFont(new Font("Segoe UI Semibold",Font.BOLD,19)); atas.add(judul,BorderLayout.NORTH);
        JPanel cariPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); cariPanel.setOpaque(false);
        cariPanel.add(new JLabel("No. RM")); cariPanel.add(rm); cariPanel.add(cari); cariPanel.add(pasien);
        atas.add(cariPanel,BorderLayout.CENTER);
        atas.add(new JLabel("Pilih surat dari episode rawat inap asal, termasuk saat pasien sudah berkunjung ke rawat jalan."),BorderLayout.SOUTH);
        root.add(atas,BorderLayout.NORTH);

        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabel.setRowHeight(28);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebar={140,160,175,95,100,220,180};
        for (int i=0;i<lebar.length;i++) tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        riwayat.setRowHeight(26); riwayat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] auditLebar={150,75,100,100,120,390,160};
        for(int i=0;i<auditLebar.length;i++) riwayat.getColumnModel().getColumn(i).setPreferredWidth(auditLebar[i]);
        riwayat.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int r=riwayat.rowAtPoint(e.getPoint()), c=riwayat.columnAtPoint(e.getPoint());
                Object nilai=r<0||c<0?null:riwayat.getValueAt(r,c);
                riwayat.setToolTipText(nilai==null?null:nilai.toString());
            }
        });
        JScrollPane daftar=new JScrollPane(tabel); daftar.setPreferredSize(new Dimension(900,165));
        JPanel tengah=new JPanel(new BorderLayout(0,10)); tengah.setOpaque(false); tengah.add(daftar,BorderLayout.NORTH);
        JPanel detail=new JPanel(new BorderLayout(0,9)); detail.setOpaque(false);
        identitas.setForeground(BIRU); detail.add(identitas,BorderLayout.NORTH);
        JPanel cards=new JPanel(new GridLayout(1,3,12,0)); cards.setOpaque(false);
        cards.add(card("Tanggal awal / mulai audit (terkunci)",awal));
        cards.add(card("Tanggal kontrol berlaku",aktif));
        JSpinner.DateEditor editor=new JSpinner.DateEditor(tanggal,"dd-MM-yyyy");
        editor.getFormat().setLenient(false); tanggal.setEditor(editor);
        cards.add(card("Tanggal kontrol baru",tanggal));
        JPanel isi=new JPanel(new BorderLayout(0,6)); isi.setOpaque(false); isi.add(cards,BorderLayout.NORTH);
        JPanel catatan=new JPanel(new BorderLayout(0,5)); catatan.setOpaque(false);
        JPanel labelAlasan=new JPanel(new BorderLayout(0,8)); labelAlasan.setOpaque(false);
        labelAlasan.add(sumber,BorderLayout.NORTH);
        labelAlasan.add(new JLabel("Alasan perubahan tanggal kontrol (wajib)"),BorderLayout.SOUTH);
        catatan.add(labelAlasan,BorderLayout.NORTH);
        alasan.setLineWrap(true); alasan.setWrapStyleWord(true); catatan.add(new JScrollPane(alasan),BorderLayout.CENTER);
        isi.add(catatan,BorderLayout.CENTER); detail.add(isi,BorderLayout.CENTER);
        JTabbedPane tabs=new JTabbedPane(); tabs.addTab("Riwayat Perubahan",new JScrollPane(riwayat));
        klinis.setEditable(false); klinis.setLineWrap(true); klinis.setWrapStyleWord(true);
        tabs.addTab("Data Resume Ranap",new JScrollPane(klinis));
        tabs.setPreferredSize(new Dimension(900,145)); detail.add(tabs,BorderLayout.SOUTH);
        tengah.add(detail,BorderLayout.CENTER); root.add(tengah,BorderLayout.CENTER);
        JPanel bawah=new JPanel(new BorderLayout(0,8)); bawah.setOpaque(false); bawah.add(status,BorderLayout.NORTH);
        JPanel aksi=new JPanel(new FlowLayout(FlowLayout.LEFT,7,4)) {
            @Override public Dimension getPreferredSize() {
                int lebar=Math.max(400,SuratLepasRawatForm.this.getWidth()-40), terpakai=7,baris=1,tinggi=34;
                for(Component c:getComponents()) {
                    int ukuran=c.getPreferredSize().width+7;
                    if(terpakai+ukuran>lebar) { baris++; terpakai=7; }
                    terpakai+=ukuran; tinggi=Math.max(tinggi,c.getPreferredSize().height);
                }
                return new Dimension(lebar,baris*(tinggi+8));
            }
        }; aksi.setOpaque(false);
        for(JButton b:new JButton[]{buat,simpan,cek,cetak,upload,berkas,tautkan,tutup}) aksi.add(b);
        simpan.setBackground(BIRU); simpan.setForeground(Color.WHITE); bawah.add(aksi,BorderLayout.SOUTH); root.add(bawah,BorderLayout.SOUTH);
        font(root); judul.setFont(new Font("Segoe UI Semibold",Font.BOLD,19));
        for(JButton b:new JButton[]{cari,buat,simpan,cek,cetak,upload,berkas,tautkan,tutup}) {
            b.setPreferredSize(new Dimension(b.getFontMetrics(b.getFont()).stringWidth(b.getText())+40,34));
        }
        for(JLabel label:new JLabel[]{awal,aktif}) label.setFont(new Font("Segoe UI Semibold",Font.BOLD,17));
        tabel.getTableHeader().setReorderingAllowed(false); riwayat.getTableHeader().setReorderingAllowed(false);
        tabel.getSelectionModel().addListSelectionListener(e->{ if(!e.getValueIsAdjusting()) pilih(); });
        cari.addActionListener(e->muat()); rm.addActionListener(e->muat());
        simpan.addActionListener(e->simpan()); cek.addActionListener(e->periksa());
        buat.addActionListener(e->buat()); cetak.addActionListener(e->laporan(false)); upload.addActionListener(e->laporan(true));
        berkas.addActionListener(e->periksa());tautkan.addActionListener(e->tautkan());
        tutup.addActionListener(e->{if(!sibuk) dispose();});
        Dimension screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1140,screen.width-50),Math.min(790,screen.height-65));
        setMinimumSize(new Dimension(850,650)); setLocationRelativeTo(owner); tombol();
    }
    private static DefaultTableModel readonly(String... kolom) {
        return new DefaultTableModel(kolom,0) { @Override public boolean isCellEditable(int r,int c){return false;} };
    }
    private static JButton button(String text) {
        JButton b=new JButton(text); b.setFocusPainted(false); b.setPreferredSize(new Dimension(
                Math.max(76,text.length()*7+24),32)); return b;
    }
    private static JPanel card(String judul,JComponent nilai) {
        JPanel p=new JPanel(new BorderLayout(0,8)); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(217,228,240)),
                BorderFactory.createEmptyBorder(10,12,10,12)));
        p.add(new JLabel(judul),BorderLayout.NORTH); p.add(nilai,BorderLayout.CENTER); return p;
    }
    private static void font(Component component) {
        component.setFont(new Font("Segoe UI",Font.PLAIN,12));
        if(component instanceof java.awt.Container) for(Component child:((java.awt.Container)component).getComponents()) font(child);
    }
    private String text(String s) { return s==null||s.isEmpty()?"—":s; }
    private void tombol() {
        boolean aksesOK=akses.getbpjs_surat_kontrol(), ada=dipilih!=null, surat=ada&&dipilih.noSurat!=null;
        cari.setEnabled(!sibuk&&aksesOK); rm.setEnabled(!sibuk); tabel.setEnabled(!sibuk);
        buat.setEnabled(!sibuk&&aksesOK&&ada&&!surat);
        simpan.setEnabled(!sibuk&&aksesOK&&surat&&dipilih.pending==null);
        tanggal.setEnabled(simpan.isEnabled()); alasan.setEnabled(simpan.isEnabled());
        cek.setEnabled(!sibuk&&aksesOK&&surat); cetak.setEnabled(!sibuk&&aksesOK&&surat&&dipilih.pending==null);
        upload.setEnabled(cetak.isEnabled()); tutup.setEnabled(!sibuk);
        berkas.setEnabled(!sibuk&&aksesOK&&surat&&dipilih.pending==null&&dipilih.berkasStatus!=null);
        tautkan.setEnabled(berkas.isEnabled());
    }
    private <T> void kerja(String pesan,Callable<T> pekerjaan,Consumer<T> selesai) {
        if(sibuk) return; final boolean reloadOnError=dipilih!=null&&!pesan.startsWith("Memuat"); sibuk=true; status.setText(pesan); tombol();
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() throws Exception { return pekerjaan.call(); }
            @Override protected void done() {
                sibuk=false; tombol(); if(!isDisplayable()) return;
                try { T hasil=get(); status.setText(" "); selesai.accept(hasil); }
                catch(Exception e) {
                    Throwable sebab=e.getCause()==null?e:e.getCause(); status.setText("Proses belum berhasil.");
                    JOptionPane.showMessageDialog(SuratLepasRawatForm.this,sebab.getMessage(),"Surat Lepas Rawat",JOptionPane.WARNING_MESSAGE);
                    // Ambil status pengiriman terbaru, termasuk kegagalan respons BPJS.
                    if(reloadOnError) { pilihSurat=dipilih.noSurat; muat(); }
                }
            }
        }.execute();
    }
    private void muat() {
        if(sibuk) return;
        if(!akses.getbpjs_surat_kontrol()) { JOptionPane.showMessageDialog(this,"Hak akses tidak tersedia."); return; }
        String nomor=rm.getText().trim();
        if(nomor.isEmpty()) { rm.requestFocus(); return; }
        if(nomor.length()>15) { JOptionPane.showMessageDialog(this,"No. RM maksimal 15 karakter."); return; }
        if(dipilih!=null&&pilihSurat==null) pilihSurat=dipilih.noSurat;
        final String target=pilihSurat;
        kerja("Memuat episode rawat inap dan surat...",()->service.daftar(nomor),hasil->{
            data=hasil; dipilih=null; model.setRowCount(0); audit.setRowCount(0);
            for(SuratLepasRawatService.Data d:data) model.addRow(new Object[]{d.noRawat,d.noSEP,d.noSurat,
                d.tanggalSurat,d.tanggal,d.namaDokter,d.namaPoli});
            pasien.setText(data.isEmpty()?"Tidak ada SEP rawat inap untuk No. RM ini":data.get(0).nama);
            pilihSurat=null;
            int index=0; for(int i=0;i<data.size();i++) if(target!=null&&target.equals(data.get(i).noSurat)) index=i;
            if(!data.isEmpty()) tabel.setRowSelectionInterval(index,index); else bersihkan();
            tombol();
        });
    }
    private void bersihkan() {
        dipilih=null; identitas.setText("Pilih episode rawat inap / surat pada daftar");
        awal.setText("—"); aktif.setText("—"); sumber.setText(" "); klinis.setText(""); alasan.setText("");
        audit.setRowCount(0); versiRiwayat++; tombol();
    }
    private void pilih() {
        int row=tabel.getSelectedRow(); if(row<0||row>=data.size()) { bersihkan(); return; }
        dipilih=data.get(row); alasan.setText("");
        identitas.setText(text(dipilih.nama)+"  |  RM "+text(dipilih.noRM)+"  |  Ranap "+text(dipilih.noRawat));
        awal.setText(dipilih.awal==null?"Belum ada surat":dipilih.awal.toString());
        aktif.setText(dipilih.tanggal==null?"Belum ada surat":dipilih.tanggal.toString());
        LocalDate d=dipilih.tanggal==null?LocalDate.now().plusDays(1):dipilih.tanggal;
        tanggal.setValue(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sumber.setText(dipilih.noSurat==null?"Buat SKDP awal dari episode rawat inap yang dipilih.":
                ("ASLI".equals(dipilih.asal)?"Tanggal awal saat pembuatan SKDP":"Baseline: tanggal tersimpan saat mulai diaudit")
                +" | Petugas: "+text(dipilih.petugasAwal)+" | Dicatat: "+text(dipilih.dicatat));
        klinis.setText("Diagnosis utama\n"+text(dipilih.diagnosa)+"\n\nObat pulang\n"+text(dipilih.obat)
                +"\n\nTanggal & jam kontrol pada resume ranap\n"+text(dipilih.kontrolResume)); klinis.setCaretPosition(0);
        status.setText(dipilih.pending!=null?"Pengiriman belum selesai dipastikan. Gunakan Cek Status BPJS.":
                dipilih.berkasStatus!=null?"Berkas Lepas Rawat belum diperbarui ("+dipilih.berkasStatus+"). Gunakan Perbarui Berkas.":" ");
        tombol(); audit.setRowCount(0); long versi=++versiRiwayat;
        final String nomor=dipilih.noSurat;
        if(nomor==null) return;
        new SwingWorker<List<Object[]>,Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return service.riwayat(nomor); }
            @Override protected void done() {
                if(versi!=versiRiwayat||!isDisplayable()) return;
                try { for(Object[] r:get()) audit.addRow(r); }
                catch(Exception e) { status.setText("Riwayat belum berhasil dimuat. Klik Cari / Muat Ulang."); }
            }
        }.execute();
    }
    private void simpan() {
        final SuratLepasRawatService.Data d=dipilih;
        try {
            tanggal.commitEdit();
            final LocalDate baru=((Date)tanggal.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            final String catatan=alasan.getText().trim(), petugas=akses.getkode();
            final boolean boleh=akses.getbpjs_surat_kontrol();
            if(catatan.isEmpty()) { JOptionPane.showMessageDialog(this,"Isi alasan perubahan tanggal kontrol."); alasan.requestFocus(); return; }
            kerja("Menyimpan perubahan tanggal kontrol ke BPJS...",()->{
                return service.ubah(d,baru,catatan,petugas,boleh);
            },pesan->{ JOptionPane.showMessageDialog(this,pesan); pilihSurat=d.noSurat; muat(); });
        } catch(Exception e) { JOptionPane.showMessageDialog(this,"Tanggal tidak valid. Gunakan format dd-MM-yyyy."); }
    }
    private void periksa() {
        final SuratLepasRawatService.Data d=dipilih; final boolean boleh=akses.getbpjs_surat_kontrol();
        kerja("Memeriksa tanggal kontrol BPJS dan berkas Lepas Rawat...",()->service.periksa(d,boleh),pesan->{
            JOptionPane.showMessageDialog(this,pesan); pilihSurat=d.noSurat; muat();
        });
    }
    private void tautkan() {
        final SuratLepasRawatService.Data d=dipilih;
        if(d==null||d.noSurat==null) return;
        final boolean boleh=akses.getbpjs_surat_kontrol();final String petugas=akses.getkode();
        kerja("Mencari berkas Lepas Rawat pasien...",()->service.kandidatBerkas(d.noSurat,boleh),refs->{
            if(refs.isEmpty()) { JOptionPane.showMessageDialog(this,"Belum ada berkas Lepas Rawat pasien ini.");return; }
            DefaultTableModel daftar=readonly("No. Rawat Pemilik","Kode","Lokasi Berkas Lama");
            for(SuratLepasRawatBerkas.Ref r:refs) daftar.addRow(new Object[]{r.rawat,r.kode,r.path});
            JTable pilihan=new JTable(daftar);pilihan.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            pilihan.setRowHeight(27);pilihan.getColumnModel().getColumn(0).setPreferredWidth(160);
            pilihan.getColumnModel().getColumn(1).setPreferredWidth(60);pilihan.getColumnModel().getColumn(2).setPreferredWidth(640);
            JScrollPane scroll=new JScrollPane(pilihan);scroll.setPreferredSize(new Dimension(880,220));
            JPanel isi=new JPanel(new BorderLayout(0,8));
            isi.add(new JLabel("Pilih hanya halaman berkas lama milik SKDP "+d.noSurat+". Gunakan Ctrl untuk beberapa halaman."),BorderLayout.NORTH);
            isi.add(scroll,BorderLayout.CENTER);
            if(JOptionPane.showConfirmDialog(this,isi,"Tautkan Berkas Lepas Rawat Lama",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
            List<SuratLepasRawatBerkas.Ref> selected=new ArrayList<>();for(int row:pilihan.getSelectedRows()) selected.add(refs.get(row));
            if(selected.isEmpty()) return;
            kerja("Menautkan dan memperbarui berkas...",()->{
                service.tautkanBerkas(d.noSurat,selected,petugas,boleh);return service.periksa(d,boleh);
            },pesan->{ JOptionPane.showMessageDialog(this,pesan);pilihSurat=d.noSurat;muat(); });
        });
    }
    private void buat() {
        if(dipilih==null||!akses.getbpjs_surat_kontrol()) return;
        BPJSSuratKontrol form=new BPJSSuratKontrol(null,false);
        form.setNoRm(dipilih.noRawat,dipilih.noSEP,dipilih.noKartu,dipilih.noRM,dipilih.nama,
                dipilih.lahir,dipilih.jk,dipilih.diagnosaAwal);
        form.setSize(getSize()); form.setLocationRelativeTo(this); form.setVisible(true);
        form.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override public void windowClosed(java.awt.event.WindowEvent e){muat();}
        });
    }
    private void laporan(boolean uploadBerkas) {
        if(dipilih==null||dipilih.noSurat==null||!akses.getbpjs_surat_kontrol()) return;
        BPJSSuratKontrol form=new BPJSSuratKontrol(null,false);
        try { form.prosesLepasRawat(dipilih.noSurat,uploadBerkas); }
        catch(Exception e) { JOptionPane.showMessageDialog(this,e.getMessage()); }
        finally { form.dispose(); }
    }
}
