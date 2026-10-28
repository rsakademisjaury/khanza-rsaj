package fungsi;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/** Input bahan yang sama pada permintaan dan hasil; kategori menentukan field aktif. */
public final class LabPABahanPanel extends JPanel {
    private final JComboBox<String> jenis = new JComboBox<>(new String[]{"Pilih jenis pemeriksaan", "Sitologi", "Histopatologi", "Imunohistokimia"});
    private final Map<String,JTextComponent> controls = new LinkedHashMap<>();
    private final Map<String,String> labels = new LinkedHashMap<>();
    private final JPanel rows = new JPanel(new GridBagLayout());
    private final boolean hasil;
    private boolean loading;
    private JComponent tanggalRiwayatPA;
    private String noorder="";
    public LabPABahanPanel(boolean hasil, JTextComponent... lama) {
        super(new BorderLayout(0,5)); this.hasil=hasil;setBackground(Color.WHITE);rows.setBackground(Color.WHITE);
        String[] keys={"jenisJaringan","lokasi","cara","fiksasi","rsSebelum","noSebelum","diagnosaPA","permintaanIHC","pemeriksaanIHC"};
        String[] names={"Jenis Jaringan","Lokasi/Bahan Jaringan","Cara Mendapatkan Jaringan","Cairan Fiksasi","RS sebelumnya","No. PA sebelumnya","Diagnosa PA","Permintaan IHC","Pemeriksaan IHC"};
        if(!hasil){names[4]="Pernah dilakukan PA di";names[5]="Dengan No. PA";}
        for(int n=0;n<keys.length;n++){
            JTextComponent c;
            // Komponen lama dipakai kembali, termasuk batasInput dan key handler-nya.
            if(lama.length==6 && n>=1 && n<=6)c=lama[n-1];else c=new JTextArea(1,15);
            c.setFont(new Font("Segoe UI",Font.PLAIN,12));c.setBackground(Color.WHITE);
            if(c instanceof JTextArea){((JTextArea)c).setLineWrap(true);((JTextArea)c).setWrapStyleWord(true);}
            controls.put(keys[n],c);labels.put(keys[n],names[n]);
        }
        JPanel header=new JPanel(new BorderLayout(8,0));header.setBackground(Color.WHITE);
        JLabel label=new JLabel("Jenis Pemeriksaan");label.setFont(new Font("Segoe UI",Font.PLAIN,12));
        header.add(label,BorderLayout.WEST);jenis.setFont(new Font("Segoe UI",Font.PLAIN,12));header.add(jenis,BorderLayout.CENTER);
        add(header,BorderLayout.NORTH);add(rows,BorderLayout.CENTER);
        jenis.addActionListener(e->{if(!loading)rebuild();});rebuild();
    }
    private boolean visible(String key){
        boolean ihc="IHC".equals(getJenis());
        if(key.equals("jenisJaringan")||key.equals("lokasi"))return true;
        if(key.equals("cara")||key.equals("fiksasi"))return !ihc;
        if(key.equals("pemeriksaanIHC"))return ihc&&hasil;
        return ihc;
    }
    private final Map<String,JPanel> wrappers=new HashMap<>();
    private void rebuild(){
        rows.removeAll();int row=0;
        for(String key:controls.keySet())if(visible(key)){
            // Hanya panel hasil: dua pasangan bahan ditampilkan dalam dua kolom.
            // Form permintaan tetap memakai susunan satu kolom sebelumnya.
            boolean kiri=hasil&&(key.equals("jenisJaringan")||key.equals("cara"));
            boolean kanan=hasil&&(key.equals("lokasi")||key.equals("fiksasi"));
            JLabel label=new JLabel(labels.get(key)+" :");label.setFont(new Font("Segoe UI",Font.PLAIN,12));label.setForeground(new Color(53,74,96));
            GridBagConstraints c=new GridBagConstraints();c.gridx=kanan?1:0;c.gridy=row;c.weightx=1;
            c.gridwidth=kiri||kanan?1:2;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;
            c.insets=new Insets(5,kanan?6:0,2,kiri?6:0);rows.add(label,c);c.gridy=row+1;
            c.insets=new Insets(0,kanan?6:0,2,kiri?6:0);
            JPanel wrapper=wrappers.get(key);
            if(wrapper==null){wrapper=new Input(controls.get(key));wrappers.put(key,wrapper);}
            rows.add(wrapper,c);
            if(!kiri)row+=2;
            if(!hasil&&key.equals("rsSebelum")&&tanggalRiwayatPA!=null){
                JLabel tanggal=new JLabel("Riwayat PA Tgl. :");tanggal.setFont(new Font("Segoe UI",Font.PLAIN,12));tanggal.setForeground(new Color(53,74,96));
                c.gridx=0;c.gridwidth=2;c.gridy=row++;c.insets=new Insets(5,0,2,0);rows.add(tanggal,c);
                c.gridy=row++;c.insets=new Insets(0,0,2,0);rows.add(tanggalRiwayatPA,c);
            }
        }
        revalidate();repaint();
    }
    /** Pada hasil PA, nomor dan combo jenis yang sama dipasang dalam satu baris. */
    public void setNomorPAField(JTextField nomor){
        if(!hasil)return;
        Component lama=((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if(lama!=null)remove(lama);
        JPanel header=new JPanel(new GridBagLayout());header.setBackground(Color.WHITE);
        String[] judul={"No. PA :","Jenis Pemeriksaan :"};
        JComponent[] isian={nomor,jenis};
        for(int n=0;n<isian.length;n++){
            JPanel kolom=new JPanel(new BorderLayout(8,0));kolom.setBackground(Color.WHITE);
            JLabel label=new JLabel(judul[n]);label.setFont(new Font("Segoe UI",Font.PLAIN,12));
            isian[n].setPreferredSize(new Dimension(120,30));isian[n].setMinimumSize(new Dimension(0,30));
            kolom.add(label,BorderLayout.WEST);kolom.add(isian[n],BorderLayout.CENTER);
            kolom.setPreferredSize(new Dimension(200,30));kolom.setMinimumSize(new Dimension(0,30));
            GridBagConstraints c=new GridBagConstraints();c.gridx=n;c.gridy=0;c.weightx=1;
            c.fill=GridBagConstraints.HORIZONTAL;c.insets=new Insets(0,n==0?0:6,0,n==0?6:0);
            header.add(kolom,c);
        }
        add(header,BorderLayout.NORTH);revalidate();repaint();
    }
    public String getJenis(){return LabPAData.JENIS[Math.max(0,jenis.getSelectedIndex())];}
    public void setJenisVisible(boolean visible){
        if(hasil)return;
        Component header=((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if(header!=null)header.setVisible(visible);revalidate();repaint();
    }
    public void setTanggalRiwayatPA(JComponent tanggal){
        if(hasil)return;tanggalRiwayatPA=tanggal;
        tanggal.setPreferredSize(new Dimension(160,30));tanggal.setMinimumSize(new Dimension(0,30));rebuild();
    }
    public void setTypeLocked(boolean locked){jenis.setEnabled(!locked);}
    public void addTypeListener(ActionListener listener){jenis.addActionListener(listener);}
    public LabPAData.Bahan getValue(){
        LabPAData.Bahan b=new LabPAData.Bahan();b.noorder=noorder;b.jenis=getJenis();
        b.jenisJaringan=t("jenisJaringan");b.lokasi=t("lokasi");b.cara=t("cara");b.fiksasi=t("fiksasi");
        b.rsSebelum=t("rsSebelum");b.noSebelum=t("noSebelum");b.diagnosaPA=t("diagnosaPA");
        b.permintaanIHC=t("permintaanIHC");b.pemeriksaanIHC=t("pemeriksaanIHC");return b;
    }
    private String t(String key){return controls.get(key).getText().trim();}
    public void setValue(LabPAData.Bahan b){
        loading=true;
        try{
            noorder=b.noorder;int idx=0;for(int n=1;n<LabPAData.JENIS.length;n++)if(LabPAData.JENIS[n].equals(b.jenis))idx=n;
            jenis.setSelectedIndex(idx);
            String[] values={b.jenisJaringan,b.lokasi,b.cara,b.fiksasi,b.rsSebelum,b.noSebelum,b.diagnosaPA,b.permintaanIHC,b.pemeriksaanIHC};
            int n=0;for(JTextComponent c:controls.values())c.setText(values[n++]);
        }finally{loading=false;rebuild();}
    }
    public boolean validasi(Component parent){
        String kosong=getValue().wajibKosong();if(kosong.isEmpty())return true;
        JOptionPane.showMessageDialog(parent,"Maaf, tidak boleh kosong","Validasi "+kosong,JOptionPane.WARNING_MESSAGE);
        if(kosong.equals("Jenis pemeriksaan"))jenis.requestFocusInWindow();
        else if(kosong.equals("RS sebelumnya"))controls.get("rsSebelum").requestFocusInWindow();
        else if(kosong.equals("No. PA sebelumnya"))controls.get("noSebelum").requestFocusInWindow();
        else for(String key:labels.keySet())if(labels.get(key).equals(kosong))controls.get(key).requestFocusInWindow();
        return false;
    }
    private static final class Input extends JPanel {
        private final JTextComponent text;
        private final JScrollPane scroll;
        private final boolean garisBawaan;
        private boolean pending;
        Input(JTextComponent text){
            super(new BorderLayout());this.text=text;garisBawaan=text instanceof widget.TextBox;
            setBackground(Color.WHITE);setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            text.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            if(text instanceof JTextArea){scroll=new JScrollPane(text);scroll.setBorder(null);scroll.setViewportBorder(null);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                add(scroll,BorderLayout.CENTER);
            }else{scroll=null;add(text,BorderLayout.CENTER);}
            setPreferredSize(new Dimension(160,30));setMinimumSize(new Dimension(50,30));
            text.addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){repaint();}public void focusLost(FocusEvent e){repaint();}});
            text.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){schedule();}public void removeUpdate(DocumentEvent e){schedule();}public void changedUpdate(DocumentEvent e){schedule();}});
            addComponentListener(new ComponentAdapter(){public void componentResized(ComponentEvent e){schedule();}});schedule();
        }
        private void schedule(){if(pending)return;pending=true;SwingUtilities.invokeLater(()->{pending=false;resizeInput();});}
        private void resizeInput(){
            if(scroll==null)return;int width=scroll.getViewport().getWidth();Insets pad=text.getInsets();
            if(width<=pad.left+pad.right)return;View v=text.getUI().getRootView(text);v.setSize(width-pad.left-pad.right,Integer.MAX_VALUE);
            int h=Math.max(30,(int)Math.ceil(v.getPreferredSpan(View.Y_AXIS))+pad.top+pad.bottom+4);
            if(getPreferredSize().height!=h){setPreferredSize(new Dimension(160,h));setMinimumSize(new Dimension(50,h));revalidate();}
        }
        public void paint(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Shape round=new java.awt.geom.RoundRectangle2D.Double(.75,.75,getWidth()-1.5,getHeight()-1.5,12,12);
            g.clip(round);super.paint(g);g.setClip(graphics.getClip());
            // widget.TextBox sudah menggambar border dan perubahan warna fokusnya.
            // Textarea/plain Swing tetap memakai border pembungkus PA.
            if(!garisBawaan){g.setColor(text.isFocusOwner()?new Color(39,119,205):new Color(182,190,200));g.setStroke(new BasicStroke(1.2f));g.draw(round);}
        }finally{g.dispose();}}
    }
}
