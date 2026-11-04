package fungsi;

import fungsi.LabPASoapDialog.Catatan;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.*;

/** Viewer baca-saja dengan gaya lokal; tidak mengubah tema aplikasi atau catatan sumber. */
final class LabPASoapTimeline extends JPanel {
    private static final Color INK=new Color(12,27,65),MUTED=new Color(91,111,140),BLUE=new Color(0,87,230),
            BACK=new Color(250,252,255),LINE=new Color(189,207,231),PALE=new Color(226,240,255);
    private static final Font NORMAL=font(false,13),SMALL=font(false,11),BOLD=font(true,13);
    private static final String PETUNJUK="Pilih catatan untuk melihat detail. Blok teks lalu Ctrl+C untuk menyalin.";
    private final Function<String,Boolean> clipboard;
    private final JTextField cari=new Cari();
    private final JLabel jumlah=label("Memuat catatan...",BOLD,INK),ringkasan=label("",SMALL,MUTED),info=label(PETUNJUK,SMALL,MUTED);
    private final JComboBox<String> urutan=new PilihanUrutan();
    private final JPanel daftar=new PanelDaftar();
    private final JScrollPane scroll=new JScrollPane(daftar){@Override public void updateUI(){setUI(new BasicScrollPaneUI());}};
    private final JButton salin,tambahan;
    private final Timer debounce,feedback;
    private final List<Catatan> semua=new ArrayList<>(),terlihat=new ArrayList<>();
    private final List<Kartu> kartu=new ArrayList<>();
    private Catatan aktif,terbuka;
    private String layanan="Semua";
    private boolean siap;
    @Override public void updateUI(){setUI(new BasicPanelUI());}
    @Override public void paint(Graphics g){Graphics2D gg=halus(g);try{super.paint(gg);}finally{gg.dispose();}}

    LabPASoapTimeline(String noRawat,String pasien,Runnable tutup,Function<String,Boolean> clipboard){
        super(new BorderLayout(0,12));this.clipboard=clipboard;setBackground(BACK);setBorder(new EmptyBorder(16,22,12,22));
        JPanel atas=kolom(),judul=baris();judul.setBorder(new EmptyBorder(0,4,12,0));
        Round alas=new Round(PALE,new Color(243,249,255),null,12);alas.setLayout(new BorderLayout());alas.setBorder(new EmptyBorder(6,6,6,6));alas.add(ikon("clipboard",32));judul.add(alas,BorderLayout.WEST);
        JLabel teksJudul=label("SOAP / CPPT Pasien",font(true,24),INK);teksJudul.setBorder(new EmptyBorder(0,14,0,0));judul.add(teksJudul,BorderLayout.CENTER);atas.add(judul);atas.add(identitas(noRawat,pasien));atas.add(Box.createVerticalStrut(12));
        Surface toolbar=new Surface(null){
            @Override public Dimension getPreferredSize(){return new Dimension(100,getWidth()>0&&getWidth()<1010?86:40);}
            @Override public void doLayout(){
                if(getComponentCount()!=2)return;
                if(getHeight()!=getPreferredSize().height&&getParent()!=null){getParent().invalidate();getParent().revalidate();}
                Component search=getComponent(0),controls=getComponent(1);
                if(getWidth()<1010){search.setBounds(0,0,getWidth(),40);controls.setBounds(0,48,getWidth(),38);}
                else{int cw=controls.getPreferredSize().width;search.setBounds(0,0,getWidth()-cw-18,40);controls.setBounds(getWidth()-cw,0,cw,40);}
            }
        };
        Round kotakCari=new Round(Color.WHITE,Color.WHITE,LINE,12);kotakCari.setLayout(new BorderLayout(10,0));kotakCari.setBorder(new EmptyBorder(4,12,4,7));kotakCari.add(ikon("search",20),BorderLayout.WEST);
        cari.setFont(NORMAL);cari.setForeground(INK);cari.setCaretColor(BLUE);cari.setBackground(Color.WHITE);cari.setOpaque(false);cari.setBorder(new EmptyBorder(2,0,2,0));cari.setToolTipText("Cari isi SOAP/CPPT, petugas, atau tanggal (Ctrl+F)");cari.getAccessibleContext().setAccessibleName("Cari dalam SOAP/CPPT");kotakCari.add(cari,BorderLayout.CENTER);
        JButton bersih=aksiIkon("clear","Bersihkan pencarian");bersih.setPreferredSize(new Dimension(25,25));bersih.addActionListener(e->{cari.setText("");cari.requestFocusInWindow();});kotakCari.add(bersih,BorderLayout.EAST);
        cari.addFocusListener(new FocusAdapter(){@Override public void focusGained(FocusEvent e){kotakCari.garis=BLUE;kotakCari.repaint();}@Override public void focusLost(FocusEvent e){kotakCari.garis=LINE;kotakCari.repaint();}});toolbar.add(kotakCari);
        Surface kontrol=new Surface(new FlowLayout(FlowLayout.LEFT,8,2));ButtonGroup grup=new ButtonGroup();
        for(String jenis:new String[]{"Semua","Rawat Inap","Rawat Jalan"}){JToggleButton pilih=new Pilih(jenis);pilih.setSelected(jenis.equals(layanan));grup.add(pilih);kontrol.add(pilih);pilih.addActionListener(e->{layanan=jenis;saring();});}
        urutan.setFont(NORMAL);urutan.setPreferredSize(new Dimension(188,36));urutan.setToolTipText("Urutan catatan");urutan.getAccessibleContext().setAccessibleName("Urutan catatan");urutan.addActionListener(e->saring());kontrol.add(urutan);toolbar.add(kontrol);atas.add(toolbar);add(atas,BorderLayout.NORTH);
        daftar.setLayout(new Column());daftar.setBackground(BACK);scroll.setBorder(new EmptyBorder(0,0,0,0));scroll.setOpaque(false);scroll.getViewport().setOpaque(false);scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);scroll.setVerticalScrollBar(new Gulir());scroll.getVerticalScrollBar().setUnitIncrement(24);add(scroll,BorderLayout.CENTER);
        JPanel bawah=baris();bawah.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,LINE),new EmptyBorder(12,2,0,0)));
        JPanel ket=baris();jumlah.setIcon(LabPASoapIcons.get("notes",21));jumlah.setIconTextGap(9);jumlah.setBorder(new EmptyBorder(0,0,0,16));ket.add(jumlah,BorderLayout.WEST);info.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,1,0,0,LINE),new EmptyBorder(0,16,0,8)));info.setToolTipText(PETUNJUK);ket.add(info,BorderLayout.CENTER);bawah.add(ket,BorderLayout.CENTER);
        Surface aksi=new Surface(new FlowLayout(FlowLayout.RIGHT,7,0));salin=tombol("Salin Catatan",true);salin.setIcon(LabPASoapIcons.get("copy-white",19));salin.setEnabled(false);salin.addActionListener(e->salinAktif());aksi.add(salin);
        tambahan=tombol("",true);tambahan.setIcon(LabPASoapIcons.get("chevron-white",14));tambahan.setPreferredSize(new Dimension(32,38));tambahan.setEnabled(false);tambahan.setToolTipText("Pilihan salin catatan");tambahan.getAccessibleContext().setAccessibleName("Pilihan salin catatan");tambahan.addActionListener(e->{JPopupMenu menu=new JPopupMenu();JMenuItem item=new JMenuItem("Salin semua yang tampil ("+terlihat.size()+")");item.addActionListener(a->salinSemua());menu.add(item);menu.show(tambahan,0,-menu.getPreferredSize().height);});aksi.add(tambahan);
        JButton selesai=tombol("Tutup",false);selesai.setPreferredSize(new Dimension(90,38));selesai.addActionListener(e->tutup.run());aksi.add(selesai);bawah.add(aksi,BorderLayout.EAST);add(bawah,BorderLayout.SOUTH);
        debounce=new Timer(180,e->saring());debounce.setRepeats(false);feedback=new Timer(3500,e->{info.setForeground(MUTED);info.setText(PETUNJUK);});feedback.setRepeats(false);
        cari.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){debounce.restart();}public void removeUpdate(DocumentEvent e){debounce.restart();}public void changedUpdate(DocumentEvent e){debounce.restart();}});pesan("Memuat SOAP/CPPT...","Catatan kunjungan pasien sedang dibaca.");
    }

    /** Pisahkan hanya format identitas tabel yang dikenal; sumber utuh tersedia di tooltip. */
    private JPanel identitas(String noRawat,String sumber){
        String nama=LabPAData.text(sumber),rm="-",unit="-";
        Matcher kamar=Pattern.compile("^(.*?)\\s*\\(Kamar\\s*:\\s*(.+)\\)\\s*$",Pattern.CASE_INSENSITIVE).matcher(nama);if(kamar.matches()){nama=kamar.group(1).trim();unit=kamar.group(2).trim();}
        Matcher nomor=Pattern.compile("^(\\d+)\\s+(.+)$").matcher(nama);if(nomor.matches()){rm=nomor.group(1);nama=nomor.group(2);}
        Round strip=new Round(new Color(221,239,255),new Color(232,244,255),null,14){
            private int columns;
            @Override public Dimension getPreferredSize(){return new Dimension(100,getWidth()>0&&getWidth()<950?106:58);}
            @Override public void doLayout(){
                if(getHeight()!=getPreferredSize().height&&getParent()!=null){getParent().invalidate();getParent().revalidate();}
                Insets in=getInsets();int w=getWidth()-in.left-in.right,h=getHeight()-in.top-in.bottom;
                double[] weights={.25,.19,.29,.27};boolean narrow=getWidth()<950;int x=in.left;
                int next=narrow?2:4;if(columns!=next){columns=next;for(int i=0;i<getComponentCount();i++){JComponent cell=(JComponent)getComponent(i);boolean first=i%columns==0;cell.setBorder(BorderFactory.createCompoundBorder(first?new EmptyBorder(0,0,0,0):BorderFactory.createMatteBorder(0,1,0,0,new Color(149,183,221)),new EmptyBorder(3,first?1:14,3,10)));}}
                for(int i=0;i<getComponentCount();i++){
                    int cw=narrow?w/2:(int)(w*weights[i]);if(i==3)cw=getWidth()-in.right-x;
                    if(narrow){cw=w/2;getComponent(i).setBounds(in.left+(i%2)*cw,in.top+(i/2)*(h/2),cw,h/2);}
                    else{getComponent(i).setBounds(x,in.top,cw,h);x+=cw;}
                }
            }
        };strip.setLayout(null);strip.setBorder(new EmptyBorder(9,12,9,12));strip.setToolTipText(LabPAData.text(sumber));
        String[] gambar={"person","id-card","bed","hospital"},teks={nama,"No. RM "+rm,"No. Rawat "+LabPAData.text(noRawat),unit};
        for(int i=0;i<4;i++){JPanel cell=baris();cell.setBorder(BorderFactory.createCompoundBorder(i==0?new EmptyBorder(0,0,0,0):BorderFactory.createMatteBorder(0,1,0,0,new Color(149,183,221)),new EmptyBorder(3,i==0?1:14,3,10)));JLabel icon=ikon(gambar[i],i==0?31:23);icon.setBorder(new EmptyBorder(0,0,0,10));cell.add(icon,BorderLayout.WEST);JLabel value=label(teks[i],i==0?font(true,16):font(i==3,13),INK);value.setToolTipText(i==0?LabPAData.text(sumber):teks[i]);cell.add(value,BorderLayout.CENTER);strip.add(cell);cell.setMinimumSize(new Dimension(0,40));}
        return strip;
    }
    void fokusCari(){cari.requestFocusInWindow();cari.selectAll();}
    void hentikan(){debounce.stop();feedback.stop();}
    void setCatatan(List<Catatan> data){semua.clear();semua.addAll(data);siap=true;aktif=null;terbuka=null;saring();}
    void gagal(){siap=false;semua.clear();terlihat.clear();kartu.clear();aktif=null;terbuka=null;salin.setEnabled(false);tambahan.setEnabled(false);jumlah.setText("Gagal memuat");ringkasan.setText("Catatan belum dapat dibaca");pesan("SOAP/CPPT belum dapat dibaca","Silakan tutup dan coba kembali.");}
    private void pesan(String judul,String isi){daftar.removeAll();JPanel kosong=kolom();kosong.setBorder(new EmptyBorder(38,20,38,20));kosong.add(label(judul,BOLD,INK));kosong.add(Box.createVerticalStrut(8));kosong.add(new Teks(isi,NORMAL));daftar.add(kosong);daftar.revalidate();daftar.repaint();}

    void saring(){
        if(!siap)return;
        Catatan sebelumnya=aktif;boolean pernah=sebelumnya!=null,adaTerbuka=terbuka!=null;
        terlihat.clear();String query=cari.getText().trim().toLowerCase(Locale.ROOT);
        for(Catatan c:semua){
            if(!layanan.equals("Semua")&&!layanan.equals(c.layanan))continue;
            String sumber=(c.tanggal+" "+c.jam+" "+c.petugas+" "+c.jabatan+" "+c.layanan+" "+c.subjektif+" "+c.objektif+" "+c.asesmen+" "+c.rencana+" "+c.instruksi+" "+c.evaluasi+" "+c.ttv+" "+c.alergi).toLowerCase(Locale.ROOT);
            boolean cocok=true;for(String kata:query.split("\\s+")){if(!sumber.contains(kata)){cocok=false;break;}}if(cocok)terlihat.add(c);
        }
        Comparator<Catatan> waktu=Comparator.comparing((Catatan c)->c.tanggal).thenComparing(c->c.jam);
        Collections.sort(terlihat,urutan.getSelectedIndex()==0?waktu.reversed():waktu);
        aktif=terlihat.contains(sebelumnya)?sebelumnya:(terlihat.isEmpty()?null:terlihat.get(0));
        terbuka=aktif==null?null:(!pernah||aktif!=sebelumnya||adaTerbuka?aktif:null);
        kartu.clear();daftar.removeAll();
        for(Catatan c:terlihat){Kartu k=new Kartu(c);kartu.add(k);daftar.add(k);}
        jumlah.setText((terlihat.size()==semua.size()?"":terlihat.size()+" / ")+semua.size()+" catatan");ringkasan.setText(terlihat.size()+" dari "+semua.size()+" catatan · "+urutan.getSelectedItem());jumlah.setToolTipText(ringkasan.getText());
        salin.setEnabled(aktif!=null);tambahan.setEnabled(!terlihat.isEmpty());
        if(terlihat.isEmpty())pesan(semua.isEmpty()?"Belum ada SOAP/CPPT":"Tidak ada catatan yang sesuai",semua.isEmpty()?LabPASoapDialog.teks(semua):"Coba kata lain atau pilih filter Semua.");
        daftar.revalidate();daftar.repaint();SwingUtilities.invokeLater(()->scroll.getVerticalScrollBar().setValue(0));
    }
    private void pilih(Catatan c){aktif=c;terbuka=terbuka==c?null:c;for(Kartu k:kartu)k.perbarui();daftar.revalidate();daftar.repaint();
        if(terbuka!=null)SwingUtilities.invokeLater(()->{if(terbuka!=c)return;for(Kartu k:kartu)if(k.data==c){daftar.scrollRectToVisible(new Rectangle(0,k.getY(),Math.max(1,daftar.getWidth()),Math.min(k.getHeight(),120)));break;}});
    }
    private void salin(String teks){boolean ok=Boolean.TRUE.equals(clipboard.apply(teks));info.setForeground(ok?new Color(26,128,91):new Color(169,72,35));info.setText(ok?"Teks berhasil disalin.":"Clipboard sibuk. Blok teks lalu tekan Ctrl+C.");feedback.restart();}
    void salinAktif(){if(aktif!=null)salin(LabPASoapDialog.teks(Collections.singletonList(aktif)));}
    void salinSemua(){if(!terlihat.isEmpty())salin(LabPASoapDialog.teks(terlihat));}

    private final class Kartu extends Surface {
        final Catatan data;final Round kotak;final JButton lipat;JPanel isi;
        Kartu(Catatan c){
            super(new BorderLayout(12,0));data=c;setBorder(new EmptyBorder(0,0,14,0));
            Surface tanggal=new Surface(null){@Override protected void paintComponent(Graphics g){Graphics2D gg=halus(g);gg.setColor(BLUE);gg.fillOval(111,18,12,12);gg.setColor(Color.WHITE);gg.setStroke(new BasicStroke(1.5f));gg.drawOval(110,17,14,14);gg.dispose();}};
            tanggal.setPreferredSize(new Dimension(130,62));JLabel hari=label(tanggal(c.tanggal),font(true,13),INK);hari.setBounds(1,8,107,21);hari.setToolTipText(c.tanggal+" "+c.jam);tanggal.add(hari);JLabel jam=label(c.jam,SMALL,MUTED);jam.setHorizontalAlignment(SwingConstants.CENTER);jam.setBounds(0,30,104,18);tanggal.add(jam);add(tanggal,BorderLayout.WEST);
            kotak=new Round(Color.WHITE,new Color(246,250,255),LINE,14);kotak.setLayout(new BorderLayout());kotak.setBorder(new EmptyBorder(1,1,1,1));
            Surface header=new Surface(new BorderLayout(12,0));header.setBorder(new EmptyBorder(9,14,9,12));
            String peran=c.jabatan.toLowerCase(Locale.ROOT),jenis=peran.contains("perawat")?"nurse":peran.contains("dokter")?"doctor":"staff";
            Round avatar=new Round(new Color(207,229,255),new Color(232,243,255),null,44);avatar.setLayout(new BorderLayout());avatar.setBorder(new EmptyBorder(6,6,6,6));avatar.add(ikon(jenis,30));header.add(avatar,BorderLayout.WEST);
            JPanel nama=kolom();JLabel petugas=label(c.petugas,font(true,14),INK);petugas.setToolTipText(c.petugas+" | "+c.jabatan);nama.add(petugas);nama.add(Box.createVerticalStrut(3));JLabel detil=label(tanggal(c.tanggal)+" · "+c.jam+" · "+c.jabatan,SMALL,MUTED);detil.setToolTipText(c.jabatan);nama.add(detil);
            Round badge=new Round(new Color(208,231,255),new Color(228,242,255),null,28);badge.setLayout(new BorderLayout(7,0));badge.setBorder(new EmptyBorder(5,10,5,12));badge.add(ikon(c.layanan.equals("Rawat Inap")?"bed":"hospital",21),BorderLayout.WEST);badge.add(label(c.layanan,BOLD,INK),BorderLayout.CENTER);
            Surface infoPetugas=new Surface(null){
                @Override public Dimension getPreferredSize(){return new Dimension(100,42);}
                @Override public void doLayout(){int bw=badge.getPreferredSize().width,nw=Math.min(Math.max(70,getWidth()-bw-30),Math.max(200,Math.min(330,nama.getPreferredSize().width)));nama.setBounds(0,1,nw,getHeight()-2);badge.setBounds(nw+20,6,bw,30);}
                @Override protected void paintComponent(Graphics g){g.setColor(LINE);int x=Math.min(Math.max(70,getWidth()-badge.getPreferredSize().width-30),Math.max(200,Math.min(330,nama.getPreferredSize().width)))+10;g.drawLine(x,9,x,getHeight()-9);}
            };infoPetugas.add(nama);infoPetugas.add(badge);header.add(infoPetugas,BorderLayout.CENTER);
            Surface aksi=new Surface(new FlowLayout(FlowLayout.RIGHT,6,0));JButton copy=aksiIkon("copy","Salin catatan ini");copy.addActionListener(e->salin(LabPASoapDialog.teks(Collections.singletonList(c))));aksi.add(copy);lipat=aksiIkon("chevron-down","Buka catatan");lipat.addActionListener(e->pilih(c));aksi.add(lipat);header.add(aksi,BorderLayout.EAST);
            MouseAdapter buka=new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){pilih(c);}};klik(header,buka);klik(infoPetugas,buka);klik(nama,buka);klik(badge,buka);klik(avatar,buka);for(Component l:nama.getComponents())klik(l,buka);
            kotak.add(header,BorderLayout.NORTH);add(kotak,BorderLayout.CENTER);perbarui();
        }
        void perbarui(){boolean buka=data==terbuka;kotak.garis=buka?new Color(50,139,255):data==aktif?new Color(144,177,222):LINE;kotak.awal=buka?new Color(229,242,255):Color.WHITE;lipat.setIcon(LabPASoapIcons.get(buka?"chevron-up":"chevron-down",17));lipat.setToolTipText(buka?"Tutup catatan":"Buka catatan");lipat.getAccessibleContext().setAccessibleName(lipat.getToolTipText()+" "+data.petugas);
            if(buka&&isi==null){isi=kolom();isi.setBorder(new EmptyBorder(0,12,10,12));
                isi.add(bagian("S","Subjektif",data.subjektif,data.subjektif,new Color(204,231,255),new Color(11,79,127),null));isi.add(Box.createVerticalStrut(5));
                isi.add(bagian("O","Objektif",data.objektif,data.objektif+"\n"+data.ttv+"\nAlergi: "+data.alergi,new Color(206,244,216),new Color(0,91,63),vital(data)));isi.add(Box.createVerticalStrut(5));
                isi.add(bagian("A","Asesmen",data.asesmen,data.asesmen,new Color(255,243,182),new Color(128,83,8),null));isi.add(Box.createVerticalStrut(5));
                isi.add(bagian("P","Rencana",data.rencana,data.rencana,new Color(222,212,255),new Color(77,33,132),null));isi.add(Box.createVerticalStrut(5));
                isi.add(tambahan("Instruksi",data.instruksi,"instruction"));isi.add(Box.createVerticalStrut(5));isi.add(tambahan("Evaluasi",data.evaluasi,"evaluation"));kotak.add(isi,BorderLayout.CENTER);
            }if(isi!=null)isi.setVisible(buka);kotak.revalidate();kotak.repaint();
        }
    }
    private static void klik(Component c,MouseAdapter listener){c.addMouseListener(listener);c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
    private JPanel bagian(String huruf,String judul,String isi,String isiSalin,Color warna,Color warnaHuruf,JComponent tambahanIsi){
        Round panel=new Round(Color.WHITE,new Color(246,250,255),LINE,10);panel.setLayout(new BorderLayout(12,0));panel.setBorder(new EmptyBorder(4,4,4,8));
        Surface kiri=new Surface(new BorderLayout()){@Override protected void paintComponent(Graphics g){g.setColor(new Color(152,175,205));g.drawLine(getWidth()-1,3,getWidth()-1,getHeight()-3);}};kiri.setBorder(new EmptyBorder(0,0,0,12));Round badge=new Round(warna,warna,null,8);badge.setLayout(new BorderLayout());badge.setPreferredSize(new Dimension(40,38));JLabel teksBadge=label(huruf,font(true,21),warnaHuruf);teksBadge.setHorizontalAlignment(SwingConstants.CENTER);badge.add(teksBadge);kiri.add(badge,BorderLayout.NORTH);panel.add(kiri,BorderLayout.WEST);
        JPanel konten=kolom();konten.setBorder(new EmptyBorder(1,0,1,0));Teks teks=new Teks(isi,NORMAL);JLabel title=label((huruf.equals("P")?"Plan":judul)+" ("+huruf+")",BOLD,INK);title.setToolTipText(judul);konten.add(title);konten.add(Box.createVerticalStrut(2));konten.add(teks);if(tambahanIsi!=null){konten.add(Box.createVerticalStrut(6));konten.add(tambahanIsi);}panel.add(konten,BorderLayout.CENTER);
        JPanel kanan=baris();JButton copy=aksiIkon("copy","Salin "+judul+" atau teks yang diblok");copy.addActionListener(e->{String pilihan=teks.getSelectedText();salin(pilihan==null||pilihan.isEmpty()?isiSalin:pilihan);});kanan.add(copy,BorderLayout.NORTH);panel.add(kanan,BorderLayout.EAST);return panel;
    }
    private JPanel vital(Catatan c){
        JPanel p=kolom();Surface chips=new Surface(new Wrap());String[] nama={"thermometer","pressure","heart","lungs","droplet"},nilai={"Suhu "+satuan(c.suhu," °C"),"TD "+c.td,"Nadi "+c.nadi,"RR "+c.rr,"SpO2 "+satuan(c.spo2,"%")};
        for(int i=0;i<nilai.length;i++){Round chip=new Round(new Color(220,239,255),new Color(232,244,255),null,24);chip.setLayout(new BorderLayout(6,0));chip.setBorder(new EmptyBorder(4,9,4,10));chip.add(ikon(nama[i],18),BorderLayout.WEST);chip.add(label(nilai[i],font(true,12),INK));chips.add(chip);}
        p.add(chips);p.add(Box.createVerticalStrut(4));Teks lain=new Teks("GCS: "+c.gcs+"  |  Kesadaran: "+c.kesadaran+"  |  BB: "+c.bb+"  |  TB: "+c.tb+"  |  Alergi: "+c.alergi,SMALL);lain.setForeground(MUTED);p.add(lain);return p;
    }
    private static String satuan(String nilai,String unit){return nilai.matches("[0-9]+([.,][0-9]+)?")?nilai+unit:nilai;}
    private JPanel tambahan(String judul,String nilai,String gambar){
        Round panel=new Round(new Color(244,250,255),new Color(236,246,255),LINE,10);panel.setLayout(new BorderLayout());panel.setBorder(new EmptyBorder(3,9,3,8));
        JPanel kepala=baris();JButton buka=tombol(judul,false);buka.setIcon(LabPASoapIcons.get(gambar,20));buka.setHorizontalAlignment(SwingConstants.LEFT);buka.setBorder(new EmptyBorder(1,3,1,12));buka.setPreferredSize(new Dimension(130,28));((Tombol)buka).bingkai=false;kepala.add(buka,BorderLayout.WEST);
        JLabel preview=label(nilai.replace('\n',' '),NORMAL,INK);preview.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,1,0,0,new Color(152,175,205)),new EmptyBorder(0,12,0,8)));preview.setToolTipText(nilai);kepala.add(preview,BorderLayout.CENTER);
        Surface aksi=new Surface(new FlowLayout(FlowLayout.RIGHT,5,0));JButton copy=aksiIkon("copy","Salin "+judul);aksi.add(copy);JButton panah=aksiIkon("chevron-down","Buka "+judul);((Tombol)panah).bingkai=false;aksi.add(panah);kepala.add(aksi,BorderLayout.EAST);panel.add(kepala,BorderLayout.NORTH);
        Teks teks=new Teks(nilai,NORMAL);teks.setBorder(new EmptyBorder(8,4,4,4));teks.setVisible(false);panel.add(teks,BorderLayout.CENTER);
        ActionListener toggle=e->{boolean tampil=!teks.isVisible();teks.setVisible(tampil);preview.setVisible(!tampil);panah.setIcon(LabPASoapIcons.get(tampil?"chevron-up":"chevron-down",17));panah.setToolTipText((tampil?"Tutup ":"Buka ")+judul);panel.revalidate();};buka.addActionListener(toggle);panah.addActionListener(toggle);klik(preview,new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){buka.doClick();}});
        copy.addActionListener(e->{String pilihan=teks.getSelectedText();salin(pilihan==null||pilihan.isEmpty()?nilai:pilihan);});return panel;
    }
    private static String tanggal(String sumber){try{String[] p=sumber.split("-");if(p.length==3){String[] bulan={"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des"};return p[2]+" "+bulan[Integer.parseInt(p[1])-1]+" "+p[0];}}catch(RuntimeException ex){/* Tampilkan sumber bila format berbeda. */}return sumber;}
    private static Font font(boolean bold,int size){String nama=bold?"Segoe UI Semibold":"Segoe UI";Font f=new Font(nama,bold?Font.BOLD:Font.PLAIN,size);if(f.getFamily().equals("Dialog"))f=new Font(Font.SANS_SERIF,bold?Font.BOLD:Font.PLAIN,size);return f;}
    private static JLabel label(String teks,Font font,Color warna){JLabel l=new Label(teks);l.setFont(font);l.setForeground(warna);l.setHorizontalAlignment(SwingConstants.LEFT);l.setVerticalAlignment(SwingConstants.CENTER);l.setAlignmentX(LEFT_ALIGNMENT);l.putClientProperty("html.disable",Boolean.TRUE);return l;}
    private static JLabel ikon(String nama,int ukuran){JLabel l=label("",NORMAL,INK);l.setIcon(LabPASoapIcons.get(nama,ukuran));l.setHorizontalAlignment(SwingConstants.CENTER);return l;}
    private static JPanel kolom(){Surface p=new Surface();p.setLayout(new Column());return p;}
    private static JPanel baris(){return new Surface(new BorderLayout());}
    private static JButton tombol(String teks,boolean utama){return new Tombol(teks,utama);}
    private static JButton aksiIkon(String ikon,String tooltip){Tombol b=new Tombol("",false);b.setIcon(LabPASoapIcons.get(ikon,17));b.setBorder(new EmptyBorder(5,5,5,5));b.setPreferredSize(new Dimension(29,29));b.setToolTipText(tooltip);b.getAccessibleContext().setAccessibleName(tooltip);return b;}
    private static Graphics2D halus(Graphics g){Graphics2D gg=(Graphics2D)g.create();gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);return gg;}
    private static class Surface extends JPanel {
        Surface(){this(null);}Surface(LayoutManager layout){super(layout);setOpaque(false);setAlignmentX(LEFT_ALIGNMENT);}
        @Override public void updateUI(){setUI(new BasicPanelUI());}
        @Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}
    }
    private static final class Label extends JLabel {Label(String text){super(text);}@Override public void updateUI(){setUI(new BasicLabelUI());}}
    private static class Round extends Surface {
        Color awal,akhir,garis;final int radius;
        Round(Color awal,Color akhir,Color garis,int radius){this.awal=awal;this.akhir=akhir;this.garis=garis;this.radius=radius;}
        @Override protected void paintComponent(Graphics g){Graphics2D gg=halus(g);gg.setPaint(new GradientPaint(0,0,awal,Math.max(1,getWidth()),getHeight(),akhir));gg.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);if(garis!=null){gg.setColor(garis);gg.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);}gg.dispose();}
    }
    private static final class Tombol extends JButton {
        final boolean utama;boolean bingkai=true;
        Tombol(String text,boolean utama){super(text);this.utama=utama;setFont(BOLD);setForeground(utama?Color.WHITE:BLUE);setContentAreaFilled(false);setOpaque(false);setBorder(new EmptyBorder(9,15,9,15));setIconTextGap(9);setHorizontalAlignment(SwingConstants.CENTER);setFocusPainted(false);setRolloverEnabled(true);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
        @Override public void updateUI(){setUI(new BasicButtonUI());}
        @Override protected void paintComponent(Graphics g){Graphics2D gg=halus(g);if(bingkai){gg.setColor(utama?(isEnabled()?BLUE:new Color(153,182,227)):getModel().isRollover()?PALE:Color.WHITE);gg.fillRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);gg.setColor(utama?BLUE:LINE);gg.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);}if(hasFocus()){gg.setColor(new Color(91,157,255));gg.drawRoundRect(2,2,getWidth()-5,getHeight()-5,8,8);}gg.dispose();super.paintComponent(g);}
    }
    private static final class Pilih extends JToggleButton {
        Pilih(String text){super(text);setFont(NORMAL);setForeground(INK);setBorder(new EmptyBorder(7,17,7,17));setContentAreaFilled(false);setOpaque(false);setFocusPainted(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
        @Override public void updateUI(){setUI(new BasicButtonUI());}
        @Override protected void paintComponent(Graphics g){Graphics2D gg=halus(g);gg.setColor(isSelected()?BLUE:new Color(242,246,250));gg.fillRoundRect(0,0,getWidth()-1,getHeight()-1,32,32);gg.setColor(isSelected()?BLUE:LINE);gg.drawRoundRect(0,0,getWidth()-1,getHeight()-1,32,32);if(hasFocus()){gg.setColor(new Color(91,157,255));gg.drawRoundRect(2,2,getWidth()-5,getHeight()-5,28,28);}gg.dispose();setForeground(isSelected()?Color.WHITE:INK);super.paintComponent(g);}
    }
    private static final class Cari extends JTextField {
        @Override public void updateUI(){setUI(new BasicTextFieldUI());}
        @Override protected void paintComponent(Graphics g){super.paintComponent(g);if(getText().isEmpty()){g.setFont(getFont());g.setColor(MUTED);g.drawString("Cari dalam SOAP/CPPT...",getInsets().left,(getHeight()-g.getFontMetrics().getHeight())/2+g.getFontMetrics().getAscent());}}
    }
    private static final class Teks extends JTextArea {
        Teks(String isi,Font font){super(isi);setEditable(false);setLineWrap(true);setWrapStyleWord(true);setFont(font);setForeground(INK);setOpaque(false);setBorder(new EmptyBorder(0,0,0,0));setCaretPosition(0);setAlignmentX(LEFT_ALIGNMENT);addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent e){revalidate();}});}
        @Override public void updateUI(){setUI(new BasicTextAreaUI());}
        @Override public Dimension getPreferredSize(){int width=getWidth();if(width<=0)width=620;Insets in=getInsets();getUI().getRootView(this).setSize(Math.max(1,width-in.left-in.right),Integer.MAX_VALUE);int tinggi=(int)Math.ceil(getUI().getRootView(this).getPreferredSpan(javax.swing.text.View.Y_AXIS))+in.top+in.bottom;return new Dimension(100,Math.max(getFontMetrics(getFont()).getHeight(),tinggi));}
        @Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}
    }
    /** Hitung tinggi setiap layout agar wrap tidak memakai ukuran cache dari lebar sebelumnya. */
    private static final class Column implements LayoutManager {
        public void addLayoutComponent(String name,Component c){}public void removeLayoutComponent(Component c){}
        public Dimension minimumLayoutSize(Container p){return new Dimension(0,0);}
        public Dimension preferredLayoutSize(Container p){Insets in=p.getInsets();int w=0,h=in.top+in.bottom;for(Component c:p.getComponents())if(c.isVisible()){Dimension d=c.getPreferredSize();w=Math.max(w,d.width);h+=d.height;}return new Dimension(w+in.left+in.right,h);}
        public void layoutContainer(Container p){Insets in=p.getInsets();int w=Math.max(1,p.getWidth()-in.left-in.right),y=in.top;for(Component c:p.getComponents())if(c.isVisible()){c.setSize(w,c.getHeight());int h=c.getPreferredSize().height;c.setBounds(in.left,y,w,h);y+=h;}}
    }
    private static final class Wrap extends FlowLayout {
        Wrap(){super(FlowLayout.LEFT,6,2);}
        @Override public void layoutContainer(Container p){super.layoutContainer(p);if(p.getHeight()!=preferredLayoutSize(p).height&&p.getParent()!=null){p.getParent().invalidate();p.getParent().revalidate();}}
        @Override public Dimension preferredLayoutSize(Container p){int width=p.getWidth()>0?p.getWidth():600;Insets in=p.getInsets();int available=Math.max(1,width-in.left-in.right-getHgap()*2),x=0,row=0,height=in.top+in.bottom+getVgap()*2;
            for(Component c:p.getComponents()){if(!c.isVisible())continue;Dimension d=c.getPreferredSize();if(x>0&&x+d.width+getHgap()>available){height+=row+getVgap();x=0;row=0;}x+=d.width+getHgap();row=Math.max(row,d.height);}return new Dimension(100,height+row);}
    }
    private final class PanelDaftar extends Surface implements Scrollable {
        public Dimension getPreferredScrollableViewportSize(){return new Dimension(900,480);}public int getScrollableUnitIncrement(Rectangle r,int o,int d){return 24;}public int getScrollableBlockIncrement(Rectangle r,int o,int d){return Math.max(24,r.height-48);}public boolean getScrollableTracksViewportWidth(){return true;}public boolean getScrollableTracksViewportHeight(){return false;}
        @Override protected void paintComponent(Graphics g){if(!kartu.isEmpty()){Graphics2D gg=halus(g);gg.setColor(new Color(166,202,250));gg.setStroke(new BasicStroke(2));int end=24;for(Kartu k:kartu)end=Math.max(end,k.getY()+24);gg.drawLine(117,24,117,end);gg.dispose();}}
    }
    private static final class Gulir extends JScrollBar {
        Gulir(){super(VERTICAL);setPreferredSize(new Dimension(10,10));setOpaque(false);setBorder(new EmptyBorder(0,0,0,0));}
        @Override public void updateUI(){setUI(new BasicScrollBarUI(){
            @Override protected JButton createDecreaseButton(int o){return kosong();}@Override protected JButton createIncreaseButton(int o){return kosong();}
            private JButton kosong(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));b.setMinimumSize(new Dimension(0,0));b.setMaximumSize(new Dimension(0,0));return b;}
            @Override protected void paintTrack(Graphics g,JComponent c,Rectangle r){}
            @Override protected void paintThumb(Graphics g,JComponent c,Rectangle r){if(!c.isEnabled())return;Graphics2D gg=halus(g);gg.setColor(new Color(188,203,222));gg.fillRoundRect(r.x+2,r.y+2,Math.max(3,r.width-4),Math.max(3,r.height-4),6,6);gg.dispose();}
        });}
    }
    private static final class PilihanUrutan extends JComboBox<String> {
        PilihanUrutan(){super(new String[]{"Terbaru dahulu","Terlama dahulu"});setBackground(Color.WHITE);setForeground(INK);setOpaque(false);setBorder(new EmptyBorder(1,1,1,1));setRenderer((list,value,index,selected,focus)->{JLabel l=label(value==null?"":value.toString(),NORMAL,INK);l.setOpaque(true);l.setBackground(selected?PALE:Color.WHITE);l.setBorder(new EmptyBorder(6,8,6,8));if(index<0){l.setIcon(LabPASoapIcons.get("sort",17));l.setIconTextGap(8);}return l;});}
        @Override public void updateUI(){setUI(new BasicComboBoxUI(){
            @Override protected JButton createArrowButton(){Tombol b=new Tombol("",false);b.bingkai=false;b.setIcon(LabPASoapIcons.get("chevron-down",14));b.setBorder(new EmptyBorder(0,4,0,6));return b;}
            @Override public void paintCurrentValueBackground(Graphics g,Rectangle r,boolean focus){g.setColor(Color.WHITE);g.fillRect(r.x,r.y,r.width,r.height);}
        });}
        @Override protected void paintComponent(Graphics g){Graphics2D gg=halus(g);gg.setColor(Color.WHITE);gg.fillRoundRect(0,0,getWidth()-1,getHeight()-1,11,11);gg.setColor(LINE);gg.drawRoundRect(0,0,getWidth()-1,getHeight()-1,11,11);gg.dispose();super.paintComponent(g);}
    }
}
