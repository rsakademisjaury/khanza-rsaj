package rekammedis;

import AESsecurity.EnkripsiAES;
import fungsi.akses;
import java.io.FileInputStream;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

/** Formulir sesi HD RSAJ. Java 8+, tanpa dependensi Jasper/library tambahan. */
public class DlgFormulirHemodialisa extends JDialog {
    private static final String[] KEYS = {"alamat","ruangan","anamnesis","pemeriksaan_fisik",
        "laboratorium","diagnosa","terapi","nadi","tekanan_darah","suhu","pernapasan",
        "bb_sebelum","bb_sesudah","hb","ureum_sebelum","ureum_sesudah",
        "kreatinin_sebelum","kreatinin_sesudah","elektrolit","heparin_beban",
        "heparin_selama","heparin_per_jam","lama_hd","priming_volume",
        "pelaksana_nip","pelaksana_nama","dokter_kode","dokter_nama"};
    private static final String[] OBS_KEYS = {"jam","waktu","tekanan_darah","laju_dial",
        "konduktivitas","suhu","tmp","laju_heparin","tekanan_arteri","tekanan_vena","laju_darah","kelainan"};
    private static final String[] OBS_LABELS = {"Jam","Waktu HD (jam)","T.D.","Laju dial",
        "Konduktivitas","Suhu","TMP","Laju heparin","Tek. arteri","Tek. vena","Laju darah","Kelainan / komplikasi"};
    private final Map<String,javax.swing.text.JTextComponent> fields = new LinkedHashMap<>();
    private final List<Component> inputs = new ArrayList<>();
    private final Store store;
    private final String user;
    private Session current;
    private boolean loading, dirty, busy, ownsConnection;
    private JTextField rawat = new RoundedTextField(20);
    private JLabel nilaiRawat = new JLabel("Belum ada data"), nilaiRM = new JLabel("Belum ada data"), nilaiNama = new JLabel("Belum ada data");
    private JButton btnSave, btnNew, btnUpdate, btnPrint, btnExit;
    private static final Color BLUE = new Color(26, 105, 163), BG = new Color(245,248,251);
    private static final Font NORMAL = new Font("Segoe UI",Font.PLAIN,12), SEMIBOLD = new Font("Segoe UI Semibold",Font.PLAIN,13);
    private widget.Tanggal tanggal = new widget.Tanggal();
    private JTextField hdKe = new RoundedTextField(12);
    private Component areaInduk;
    private JLabel totalHeparin = new JLabel("—");
    private DefaultTableModel obsModel = new DefaultTableModel(OBS_LABELS,0);
    private JTable observasi = new JTable(obsModel);
    private DefaultTableModel listModel = new DefaultTableModel(new String[]{"ID","Tanggal","HD ke","Pelaksana","Versi"},0) {
        @Override public boolean isCellEditable(int r,int c) { return false; }
    };
    private JTable daftar = new JTable(listModel);

    public DlgFormulirHemodialisa(Frame parent, boolean modal) {
        this(parent,modal,openConnection(),akses.getkode());
        ownsConnection=true;
    }

    private static Connection openConnection() {
        // Koneksi khusus: transaksi form ini tidak mengubah autoCommit koneksi global SIMRS.
        try(FileInputStream in=new FileInputStream("setting/database.xml")) {
            Properties config=new Properties();config.loadFromXML(in);
            Class.forName("com.mysql.jdbc.Driver");
            String url="jdbc:mysql://"+EnkripsiAES.decrypt(config.getProperty("HOST"))+":"+
                EnkripsiAES.decrypt(config.getProperty("PORT"))+"/"+EnkripsiAES.decrypt(config.getProperty("DATABASE"))+
                "?zeroDateTimeBehavior=convertToNull&connectTimeout=10000&socketTimeout=60000&characterEncoding=UTF-8";
            return DriverManager.getConnection(url,EnkripsiAES.decrypt(config.getProperty("USER")),EnkripsiAES.decrypt(config.getProperty("PAS")));
        } catch(Exception e) {throw new IllegalStateException("Tidak dapat membuka koneksi HD. Periksa setting/database.xml dan koneksi server.",e);}
    }
    public DlgFormulirHemodialisa(Frame parent, boolean modal, Connection connection, String userCode) {
        super(parent,modal);
        store = new Store(connection);
        user = userCode == null ? "" : userCode;
        initComponents();
        buildUI();
        resetFields();
        sesuaikanMonitor(parent);
        setMinimumSize(new Dimension(0,0));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addComponentListener(new ComponentAdapter(){
            @Override public void componentShown(ComponentEvent e){sesuaikanMonitor(areaInduk);}
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { closeForm(); }
        });
    }

    /** Borderless window inside the launching work area, excluding the system taskbar. */
    public void sesuaikanMonitor(Component induk) {
        areaInduk=induk;setBounds(monitorBounds(induk));
    }
    private static Rectangle monitorBounds(Component induk) {
        GraphicsConfiguration config=induk==null?null:induk.getGraphicsConfiguration();
        if(config==null)config=GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle usable=formBounds(config.getBounds(),Toolkit.getDefaultToolkit().getScreenInsets(config),null);
        Component work=findMainWorkArea(induk);
        if(work!=null&&work.isShowing()) {
            int top=Math.max(usable.y,work.getLocationOnScreen().y);
            if(top<usable.y+usable.height)return workBounds(usable,top);
        }
        if(induk!=null&&induk.isShowing()) {
            int top=Math.max(usable.y,induk.getLocationOnScreen().y);
            if(top<usable.y+usable.height)return workBounds(usable,top);
        }
        return usable;
    }
    private static Rectangle workBounds(Rectangle usable,int top) {
        int y=Math.max(usable.y,Math.min(top,usable.y+usable.height-1));
        return new Rectangle(usable.x,y,usable.width,usable.y+usable.height-y);
    }
    private static Component findMainWorkArea(Component anchor) {
        Window window=anchor instanceof Window?(Window)anchor:anchor==null?null:SwingUtilities.getWindowAncestor(anchor);
        for(Window w=window;w!=null;w=w.getOwner()) {
            if(w instanceof JFrame){Component area=findNamed((Container)w,"PanelUtama");if(area!=null&&area.isShowing())return area;}
        }
        for(Frame frame:Frame.getFrames()) {
            if(frame.isShowing()&&frame instanceof JFrame) {
                Component area=findNamed(frame,"PanelUtama");
                if(area!=null&&area.isShowing()&&(anchor==null||anchor.getGraphicsConfiguration()==null||
                    area.getGraphicsConfiguration().getBounds().equals(anchor.getGraphicsConfiguration().getBounds())))return area;
            }
        }
        return null;
    }
    private static Component findNamed(Container parent,String name) {
        if(name.equals(parent.getName()))return parent;
        for(Component child:parent.getComponents()) {
            if(name.equals(child.getName()))return child;
            if(child instanceof Container){Component found=findNamed((Container)child,name);if(found!=null)return found;}
        }
        return null;
    }

    private static Rectangle formBounds(Rectangle monitor,Insets insets,Rectangle area) {
        Rectangle usable=new Rectangle(monitor.x+insets.left,monitor.y+insets.top,
                Math.max(1,monitor.width-insets.left-insets.right),
                Math.max(1,monitor.height-insets.top-insets.bottom));
        if(area!=null) {
            Rectangle intersection=usable.intersection(area);
            if(intersection.width>0&&intersection.height>0)return intersection;
        }
        return usable;
    }

    // NetBeans GUI Builder hanya mengelola wadah; komponen dinamis ada di buildUI().
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {//GEN-BEGIN:initComponents
        contentPanel = new javax.swing.JPanel();
        setTitle("Formulir Hemodialisa — RS Akademis Jaury Jusuf Putera");
        setUndecorated(true);
        getContentPane().setLayout(new java.awt.BorderLayout());
        contentPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);
        pack();
    }//GEN-END:initComponents
    // </editor-fold>

    private void buildUI() {
        contentPanel.setLayout(new BorderLayout(0,12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12,14,10,14));
        contentPanel.setBackground(BG);
        JPanel heading = new RoundPanel();
        heading.setLayout(new BoxLayout(heading,BoxLayout.X_AXIS));
        heading.setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
        JLabel title = new JLabel("Formulir Hemodialisa");
        title.setFont(new Font("Segoe UI Semibold",Font.PLAIN,16));title.setForeground(BLUE);
        heading.add(title);heading.add(Box.createHorizontalStrut(24));
        heading.add(identityBlock("No. Rawat",nilaiRawat,"/picture/no_rawat.png"));
        heading.add(Box.createHorizontalStrut(18));
        heading.add(identityBlock("No. RM",nilaiRM,"/picture/no_rm.png"));
        heading.add(Box.createHorizontalStrut(18));
        heading.add(identityBlock("Nama Pasien",nilaiNama,"/picture/nama_pasien.png"));
        heading.add(Box.createHorizontalGlue());
        contentPanel.add(heading,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(SEMIBOLD);tabs.setBackground(BG);
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI(){
            @Override protected void installDefaults(){
                super.installDefaults();tabInsets=new Insets(10,22,10,22);
                tabAreaInsets=new Insets(6,0,10,0);selectedTabPadInsets=new Insets(0,0,0,0);
                contentBorderInsets=new Insets(4,0,0,0);
            }
            @Override protected int calculateTabHeight(int placement,int index,int fontHeight){return 44;}
            @Override protected void paintTabBackground(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){
                Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(selected?Color.WHITE:BG);p.fillRoundRect(x+4,y,w-8,h,10,10);p.dispose();
            }
            @Override protected void paintTabBorder(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){if(selected){g.setColor(BLUE);g.fillRect(x+18,y+h-3,w-36,3);}}
            @Override protected void paintContentBorder(Graphics g,int placement,int selectedIndex){}
            @Override protected void paintFocusIndicator(Graphics g,int placement,Rectangle[] rects,int index,Rectangle iconRect,Rectangle textRect,boolean selected){}
        });
        JPanel body = new VerticalPanel();
        JPanel session = section("Identitas sesi",3);
        tanggal.setDisplayFormat("dd-MM-yyyy");tanggal.setFont(NORMAL);
        tanggal.setPreferredSize(new Dimension(170,34));
        styleInput(hdKe);hdKe.setEditable(false);
        hdKe.setToolTipText("Jumlah kunjungan pasien ke poli 53 sampai registrasi ini, tanpa kunjungan batal");
        addControl(session,"Tanggal",tanggal); inputs.add(tanggal);
        addControl(session,"Hemodialisa ke",hdKe); inputs.add(hdKe);
        addField(session,"ruangan","Ruang / bangsal",false);fields.get("ruangan").setEditable(false);
        // Retain stored address values for existing sessions; no address input is displayed.
        fields.put("alamat",new JTextField()); 
        JPanel clinical = section("Data klinik",1);
        addField(clinical,"anamnesis","Anamnesis",true);
        addField(clinical,"pemeriksaan_fisik","Pemeriksaan fisik",true);
        addField(clinical,"laboratorium","Laboratorium / catatan",true);
        addField(clinical,"diagnosa","Diagnosa",true);
        addField(clinical,"terapi","Terapi",true); 
        JPanel vital = section("Status presens",3);
        addField(vital,"nadi","Nadi (x/menit)",false);
        addField(vital,"tekanan_darah","Tekanan darah (mmHg)",false);
        addField(vital,"suhu","Suhu (°C)",false);
        addField(vital,"pernapasan","Pernapasan (x/menit)",false);
        addField(vital,"bb_sebelum","BB sebelum HD (kg)",false);
        addField(vital,"bb_sesudah","BB sesudah HD (kg)",false); 
        JPanel lab = section("Hasil laboratorium",3);
        addField(lab,"hb","Hb",false);
        addField(lab,"elektrolit","Elektrolit / satuan",true);
        addField(lab,"ureum_sebelum","Ureum sebelum HD",false);
        addField(lab,"ureum_sesudah","Ureum sesudah HD",false);
        addField(lab,"kreatinin_sebelum","Kreatinin sebelum HD",false);
        addField(lab,"kreatinin_sesudah","Kreatinin sesudah HD",false); 
        JPanel treatment = section("Pelaksanaan HD dan heparin",3);
        addField(treatment,"heparin_beban","Heparin dosis beban (U)",false);
        addField(treatment,"heparin_selama","Total dosis selama HD (U)",false);
        addField(treatment,"heparin_per_jam","Dosis heparin tiap jam (U/jam)",false);
        totalHeparin.setToolTipText("Jumlah dosis beban + total dosis selama HD");
        addControl(treatment,"Jumlah heparin (U)",totalHeparin);
        addField(treatment,"lama_hd","Lamanya HD (jam)",false);
        addField(treatment,"priming_volume","Priming volume (mL)",false); 
        JPanel staff = section("Pelaksana dan dokter",2);
        addField(staff,"pelaksana_nip","NIP / kode pelaksana",false);
        addField(staff,"pelaksana_nama","Nama pelaksana",false);
        addField(staff,"dokter_kode","Kode dokter (opsional)",false);
        addField(staff,"dokter_nama","Nama dokter (opsional)",false); 

        JPanel left = new JPanel(), right = new JPanel();
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));left.setOpaque(false);right.setOpaque(false);
        left.add(clinical);left.add(Box.createVerticalStrut(12));left.add(lab);left.add(Box.createVerticalGlue());
        right.add(vital);right.add(Box.createVerticalStrut(12));right.add(treatment);right.add(Box.createVerticalStrut(12));right.add(staff);right.add(Box.createVerticalGlue());
        JPanel columns=new JPanel(new GridLayout(1,2,14,0));columns.setOpaque(false);columns.add(left);columns.add(right);
        body.add(session,BorderLayout.NORTH);body.add(columns,BorderLayout.CENTER);
        JScrollPane formScroll=new JScrollPane(body);formScroll.setBorder(BorderFactory.createEmptyBorder());formScroll.getVerticalScrollBar().setUnitIncrement(24);
        tabs.addTab("Data Formulir",formScroll);
        JPanel obsPanel=new JPanel(new BorderLayout(8,10));obsPanel.setBackground(BG);obsPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        observasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);styleTable(observasi);
        RoundedTextField cellInput=new RoundedTextField(0);styleInput(cellInput);observasi.setDefaultEditor(Object.class,new DefaultCellEditor(cellInput));observasi.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
        for(int i=0;i<OBS_LABELS.length;i++)observasi.getColumnModel().getColumn(i).setPreferredWidth(i==11?270:110);
        obsPanel.add(new JScrollPane(observasi),BorderLayout.CENTER);inputs.add(observasi);
        JPanel obsActions=new JPanel(new FlowLayout(FlowLayout.LEFT,7,4));obsActions.setOpaque(false);
        obsActions.add(button("Tambah baris",()->obsModel.addRow(new Object[12])));
        obsActions.add(button("Hapus baris",()->{if(observasi.isEditing())observasi.getCellEditor().stopCellEditing();int r=observasi.getSelectedRow();if(r>=0)obsModel.removeRow(r);}));
        JLabel hint=new JLabel("Jam: HH:mm  •  Waktu HD: jam sejak mulai");hint.setFont(NORMAL);hint.setForeground(new Color(107,120,134));obsActions.add(hint);
        obsPanel.add(obsActions,BorderLayout.SOUTH);tabs.addTab("Observasi Selama HD",obsPanel);
        JPanel history=new JPanel(new BorderLayout(8,10));history.setBackground(BG);history.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        styleTable(daftar);daftar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        daftar.removeColumn(daftar.getColumnModel().getColumn(0));daftar.removeColumn(daftar.getColumnModel().getColumn(3));
        history.add(new JScrollPane(daftar),BorderLayout.CENTER);
        JPanel historyActions=new JPanel(new FlowLayout(FlowLayout.LEFT,7,4));historyActions.setOpaque(false);
        historyActions.add(button("Muat sesi terpilih",this::loadSelected));historyActions.add(button("Refresh daftar",this::refreshList));
        historyActions.add(button("Hapus Sesi",this::deleteForm));history.add(historyActions,BorderLayout.SOUTH);tabs.addTab("Daftar Sesi HD",history);
        contentPanel.add(tabs,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,6,8));actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(224,231,238)));
        btnSave=button("Simpan",this::saveForm);btnNew=button("Baru",this::newSession);btnUpdate=button("Ganti",this::updateForm);btnPrint=button("Cetak",this::printForm);btnExit=button("Keluar",this::closeForm);
        actions.add(btnSave);actions.add(btnNew);actions.add(btnUpdate);actions.add(btnPrint);actions.add(btnExit);contentPanel.add(actions,BorderLayout.SOUTH);
        tanggal.addActionListener(e->markDirty());
        DocumentListener listener=new DocumentListener(){public void insertUpdate(DocumentEvent e){changed();}public void removeUpdate(DocumentEvent e){changed();}public void changedUpdate(DocumentEvent e){changed();}private void changed(){markDirty();updateHeparin();}};
        for(javax.swing.text.JTextComponent f:fields.values())f.getDocument().addDocumentListener(listener);
        obsModel.addTableModelListener(e->markDirty());
        refreshActionState();
    }
    private JPanel section(String title,int cols) {
        JPanel p=new RoundPanel();p.setLayout(new GridBagLayout());p.setBorder(BorderFactory.createEmptyBorder(13,14,13,14));p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.putClientProperty("hdCols",cols);p.putClientProperty("hdSlots",0);
        JLabel h=new JLabel(title);h.setFont(SEMIBOLD);h.setForeground(BLUE);
        GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.gridy=0;c.gridwidth=cols;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.insets=new Insets(0,0,4,0);p.add(h,c);return p;
    }
    private void addControl(JPanel panel,String label,JComponent control) {
        JLabel l=new JLabel(label);l.setFont(new Font("Segoe UI",Font.PLAIN,11));l.setForeground(new Color(100,112,125));
        JPanel field=new JPanel(new BorderLayout(0,5));field.setOpaque(false);field.add(l,BorderLayout.NORTH);field.add(control,BorderLayout.CENTER);
        int cols=(Integer)panel.getClientProperty("hdCols"),slot=(Integer)panel.getClientProperty("hdSlots");
        GridBagConstraints c=new GridBagConstraints();c.gridx=slot%cols;c.gridy=slot/cols+1;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;
        c.insets=new Insets(9,0,0,c.gridx<cols-1?12:0);panel.add(field,c);panel.putClientProperty("hdSlots",slot+1);control.setFont(NORMAL);
        if(control instanceof JSpinner){
            control.setBackground(new Color(253,254,255));control.setBorder(BorderFactory.createEmptyBorder());
            field.add(new RoundedControlPanel(control),BorderLayout.CENTER);
            JComponent editor=((JSpinner)control).getEditor();editor.setOpaque(false);
            if(editor instanceof JSpinner.DefaultEditor){JFormattedTextField text=((JSpinner.DefaultEditor)editor).getTextField();text.setFont(NORMAL);text.setOpaque(false);text.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));text.addFocusListener(new java.awt.event.FocusAdapter(){public void focusGained(java.awt.event.FocusEvent e){control.getParent().repaint();}public void focusLost(java.awt.event.FocusEvent e){control.getParent().repaint();}});}
        }
    }
    private void addField(JPanel panel,String key,String label,boolean multiline) {
        javax.swing.text.JTextComponent f=multiline?new JTextArea(2,12):new RoundedTextField(12);styleInput(f);
        JComponent control=f;
        if(f instanceof JTextArea){((JTextArea)f).setLineWrap(true);((JTextArea)f).setWrapStyleWord(true);f.setBorder(BorderFactory.createEmptyBorder(7,9,7,9));f.setOpaque(true);control=new RoundedScrollPane(f);}
        addControl(panel,label,control);fields.put(key,f);inputs.add(f);
    }
    private static void styleInput(javax.swing.text.JTextComponent f) {
        f.setFont(NORMAL);f.setForeground(new Color(45,56,67));f.setBackground(new Color(253,254,255));
        f.setOpaque(false);f.setBorder(new RoundedInputBorder(6,9));
        f.addFocusListener(new java.awt.event.FocusAdapter(){
            @Override public void focusGained(java.awt.event.FocusEvent e){refresh();}
            @Override public void focusLost(java.awt.event.FocusEvent e){refresh();}
            private void refresh(){f.repaint();Container parent=SwingUtilities.getAncestorOfClass(RoundedScrollPane.class,f);if(parent!=null)parent.repaint();}
        });
    }
    // Inset child components keep native editors and scrollbars inside the smooth outer border.
    private static final int INPUT_ARC=14;
    private static java.awt.Shape inputShape(int width,int height) {
        return new java.awt.geom.RoundRectangle2D.Float(0,0,Math.max(0,width-1),Math.max(0,height-1),INPUT_ARC,INPUT_ARC);
    }
    private static void inputBackground(JComponent component,Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(component.getBackground());g.fill(inputShape(component.getWidth(),component.getHeight()));
    }
    private static class RoundedInputBorder extends javax.swing.border.AbstractBorder {
        private final int vertical,horizontal;
        RoundedInputBorder(int vertical,int horizontal){this.vertical=vertical;this.horizontal=horizontal;}
        @Override public Insets getBorderInsets(Component c){return new Insets(vertical,horizontal,vertical,horizontal);}
        @Override public Insets getBorderInsets(Component c,Insets insets){insets.set(vertical,horizontal,vertical,horizontal);return insets;}
        @Override public boolean isBorderOpaque(){return false;}
        @Override public void paintBorder(Component c,Graphics graphics,int x,int y,int w,int h){
            Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Component owner=KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            boolean focused=owner==c||(owner!=null&&c instanceof Container&&SwingUtilities.isDescendingFrom(owner,(Container)c));
            g.setColor(focused?BLUE:new Color(217,225,233));g.draw(new java.awt.geom.RoundRectangle2D.Float(x+0.5f,y+0.5f,Math.max(0,w-1.5f),Math.max(0,h-1.5f),INPUT_ARC,INPUT_ARC));g.dispose();
        }
    }
    private static class RoundedTextField extends JTextField {
        RoundedTextField(int columns){super(columns);setOpaque(false);}
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();inputBackground(this,g);g.clip(inputShape(getWidth(),getHeight()));super.paintComponent(g);g.dispose();
        }
    }
    private static class RoundedScrollPane extends JScrollPane {
        RoundedScrollPane(Component view){super(view);setOpaque(false);getViewport().setOpaque(false);setBackground(new Color(253,254,255));setBorder(new RoundedInputBorder(8,8));}
        @Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();inputBackground(this,g);g.dispose();}
    }
    private static class RoundedControlPanel extends JPanel {
        RoundedControlPanel(JComponent control){
            super(new BorderLayout());setOpaque(false);setBackground(new Color(253,254,255));
            setBorder(new RoundedInputBorder(8,8));add(control,BorderLayout.CENTER);
        }
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();inputBackground(this,g);g.dispose();
        }
    }
    private static class RoundedSpinner extends JSpinner {
        RoundedSpinner(SpinnerModel model){super(model);setOpaque(false);}
        @Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();inputBackground(this,g);g.dispose();}
    }
    private JPanel identityBlock(String caption,JLabel value,String resource) {
        JPanel p=new JPanel(new BorderLayout(10,0)) {
            @Override public Dimension getMaximumSize(){return getPreferredSize();}
        };p.setOpaque(false);
        JLabel icon=new JLabel(resourceIcon(resource,24,24));icon.setPreferredSize(new Dimension(28,38));p.add(icon,BorderLayout.WEST);
        JPanel text=new JPanel(new GridLayout(2,1,0,2));text.setOpaque(false);
        JLabel title=new JLabel(caption);title.setFont(new Font("Segoe UI",Font.PLAIN,10));title.setForeground(new Color(100,100,100));
        value.setFont(new Font("Segoe UI",Font.PLAIN,12));value.setForeground(new Color(45,45,45));text.add(title);text.add(value);p.add(text,BorderLayout.CENTER);return p;
    }
    private static ImageIcon resourceIcon(String resource,int w,int h) {
        java.net.URL url=DlgFormulirHemodialisa.class.getResource(resource);if(url==null)return null;
        ImageIcon source=new ImageIcon(url);return new ImageIcon(source.getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH));
    }
    private JButton button(String text,Runnable action) {
        widget.Button b=new widget.Button();b.setText(text);b.setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));b.setPreferredSize(new Dimension(Math.max(100,b.getPreferredSize().width),30));
        String path="/picture/";
        switch(text){case "Simpan":path+="save-16x16.png";break;case "Baru":path+="Cancel-2-16x16.png";break;case "Ganti":path+="inventaris.png";break;case "Cetak":path+="b_print.png";break;case "Keluar":path+="exit.png";break;case "Hapus Sesi":case "Hapus baris":path+="delete-16x16.png";break;default:path+="file-edit-16x16.png";}
        b.setIcon(resourceIcon(path,16,16));b.addActionListener(e->{if(!busy)action.run();});inputs.add(b);return b;
    }
    private void refreshIdentity(Session s) {
        nilaiRawat.setText(s==null?"Belum ada data":s.rawat);nilaiRM.setText(s==null?"Belum ada data":s.rm);nilaiNama.setText(s==null?"Belum ada data":s.name);
        nilaiRawat.setToolTipText(nilaiRawat.getText());nilaiRM.setToolTipText(nilaiRM.getText());nilaiNama.setToolTipText(nilaiNama.getText());
        contentPanel.revalidate();contentPanel.repaint();
    }
    private void refreshActionState() {
        boolean has=current!=null,saved=has&&current.id!=null;
        if(btnSave!=null)btnSave.setEnabled(!busy&&has&&!saved);
        if(btnUpdate!=null)btnUpdate.setEnabled(!busy&&saved);
        if(btnNew!=null)btnNew.setEnabled(!busy&&has);
        if(btnPrint!=null)btnPrint.setEnabled(!busy&&saved);
    }
    private static void styleTable(JTable table) {
        table.setFont(NORMAL);table.setRowHeight(30);table.setGridColor(new Color(230,236,242));table.setSelectionBackground(new Color(217,235,250));table.setSelectionForeground(new Color(35,67,93));
        table.getTableHeader().setFont(SEMIBOLD);table.getTableHeader().setBackground(new Color(235,244,251));table.getTableHeader().setForeground(BLUE);table.getTableHeader().setPreferredSize(new Dimension(0,34));
        table.setDefaultRenderer(Object.class,new javax.swing.table.DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean selected,boolean focused,int row,int col){
                Component c=super.getTableCellRendererComponent(t,v,selected,focused,row,col);if(!selected)c.setBackground(row%2==0?Color.WHITE:new Color(248,251,254));setBorder(BorderFactory.createEmptyBorder(3,7,3,7));return c;
            }
        });
    }
    private static class RoundPanel extends JPanel {
        RoundPanel(){setOpaque(false);setBackground(Color.WHITE);}
        @Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(getBackground());p.fillRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);p.setColor(new Color(224,232,239));p.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);p.dispose();super.paintComponent(g);}
        @Override public Dimension getMaximumSize(){Dimension d=getPreferredSize();return new Dimension(Integer.MAX_VALUE,d.height);}
    }
    private static class VerticalPanel extends JPanel implements Scrollable {
        VerticalPanel(){setLayout(new BorderLayout(0,12));setBackground(BG);setBorder(BorderFactory.createEmptyBorder(12,0,12,0));}
        public Dimension getPreferredScrollableViewportSize(){return new Dimension(1080,600);}
        public int getScrollableUnitIncrement(Rectangle r,int o,int d){return 24;}
        public int getScrollableBlockIncrement(Rectangle r,int o,int d){return Math.max(24,r.height-24);}
        public boolean getScrollableTracksViewportWidth(){return true;}
        public boolean getScrollableTracksViewportHeight(){return false;}
    }
    private void markDirty(){if(!loading && current!=null)dirty=true;}
    private void updateHeparin() {
        if(!fields.containsKey("heparin_beban"))return;
        try {
            String a=text("heparin_beban"),b=text("heparin_selama");
            totalHeparin.setText(a.isEmpty()&&b.isEmpty()?"—":decimal(a.isEmpty()?"0":a).add(decimal(b.isEmpty()?"0":b)).stripTrailingZeros().toPlainString()+" U");
        } catch(IllegalArgumentException e){totalHeparin.setText("Periksa nilai dosis");}
    }
    private String text(String key){return fields.get(key).getText().trim();}
    private void resetFields() {
        loading=true;
        for(javax.swing.text.JTextComponent f:fields.values())f.setText("");
        tanggal.setDate(new java.util.Date()); hdKe.setText("0"); obsModel.setRowCount(0);
        for(int i=0;i<=10;i++){Object[] row=new Object[12];Arrays.fill(row,"");row[1]=BigDecimal.valueOf(i).divide(BigDecimal.valueOf(2)).stripTrailingZeros().toPlainString();obsModel.addRow(row);}
        loading=false;dirty=false;updateHeparin();refreshActionState();
    }
    private boolean mayDiscard() {
        if(observasi.isEditing())observasi.getCellEditor().stopCellEditing();
        return !dirty || JOptionPane.showConfirmDialog(this,"Ada perubahan belum disimpan. Abaikan perubahan?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }
    /** Dipanggil dari popup Kasir Ralan/Kamar Inap. */
    public void setNoRm(String noRawat) { rawat.setText(noRawat==null?"":noRawat.trim());fetchPatient(); }
    public void setNoRawat(String noRawat) { setNoRm(noRawat); }
    private void fetchPatient() {
        String n=rawat.getText().trim(); if(n.isEmpty()){info("Isi No. Rawat terlebih dahulu.");return;}
        if(!mayDiscard())return;
        job(()->store.patient(n,user),s->{
            current=s;resetFields();loading=true;fill(s);loading=false;dirty=false;
            rawat.setEditable(false);refreshList();
        });
    }
    private void fill(Session s) {
        rawat.setText(s.rawat);
        refreshIdentity(s);
        tanggal.setDate(s.date);hdKe.setText(String.valueOf(s.hdKe));
        for(String key:KEYS)fields.get(key).setText(s.data.getOrDefault(key,""));
        if(s.id!=null){obsModel.setRowCount(0);for(String[] row:s.rows)obsModel.addRow(row);}
        updateHeparin();
    }
    private void newSession() {
        if(!mayDiscard())return;
        if(current==null){rawat.setEditable(true);rawat.requestFocus();return;}
        // Tetap pada pasien ini; tidak menyalin hasil klinik sesi sebelumnya.
        String n=current.rawat;
        job(()->store.patient(n,user),s->{current=s;resetFields();loading=true;fill(s);loading=false;dirty=false;});
    }
    /** Memungkinkan ganti pasien tanpa menutup dialog. */
    public void gantiPasien(String noRawat) { setNoRm(noRawat); }
    private void refreshList() {
        if(current==null)return;
        String n=current.rawat;
        job(()->store.list(n),rows->{listModel.setRowCount(0);for(Object[] r:rows)listModel.addRow(r);});
    }
    private void loadSelected() {
        int r=daftar.getSelectedRow(); if(r<0){info("Pilih sesi pada daftar.");return;}
        if(!mayDiscard())return;
        String id=listModel.getValueAt(daftar.convertRowIndexToModel(r),0).toString();
        job(()->store.load(id),s->{loading=true;current=s;fill(s);loading=false;dirty=false;});
    }
    private Session snapshot() {
        if(current==null)throw new IllegalArgumentException("Ambil data pasien terlebih dahulu.");
        java.util.Date picked=tanggal.getDate();
        if(picked==null)throw new IllegalArgumentException("Pilih tanggal formulir.");
        if(observasi.isEditing()&&!observasi.getCellEditor().stopCellEditing())throw new IllegalArgumentException("Selesaikan isian observasi.");
        Session s=current.copy();
        Calendar selected=Calendar.getInstance(),stamp=Calendar.getInstance();selected.setTime(picked);stamp.setTime(current.date);
        stamp.set(selected.get(Calendar.YEAR),selected.get(Calendar.MONTH),selected.get(Calendar.DAY_OF_MONTH));
        s.date=stamp.getTime();s.hdKe=current.hdKe;
        for(String key:KEYS){String value=text(key);int max=key.endsWith("nama")?150:30;
            if(Arrays.asList("ruangan").contains(key))max=150;
            boolean longText=Arrays.asList("alamat","anamnesis","pemeriksaan_fisik","laboratorium","diagnosa","terapi","elektrolit").contains(key);
            if(value.length()>(longText?8000:max))throw new IllegalArgumentException("Isian "+key+" terlalu panjang.");s.data.put(key,value);}
        if(s.data.get("pelaksana_nip").isEmpty()||s.data.get("pelaksana_nama").isEmpty())throw new IllegalArgumentException("Isi kode dan nama pelaksana.");
        if(user.trim().isEmpty())throw new IllegalArgumentException("Pengguna SIMRS belum teridentifikasi. Buka melalui akun yang sudah login.");
        for(String key:new String[]{"nadi","suhu","pernapasan","bb_sebelum","bb_sesudah","hb","ureum_sebelum","ureum_sesudah","kreatinin_sebelum","kreatinin_sesudah","heparin_beban","heparin_selama","heparin_per_jam","lama_hd","priming_volume"})validateNumber(s.data.get(key),key,false);
        validateTensi(s.data.get("tekanan_darah"));s.rows.clear();
        for(int r=0;r<obsModel.getRowCount();r++) {
            String[] row=new String[12];boolean hasResult=false;
            for(int c=0;c<12;c++){Object v=obsModel.getValueAt(r,c);row[c]=v==null?"":v.toString().trim();if(c>=2&&!row[c].isEmpty())hasResult=true;}
            if(!hasResult)continue;
            if(!row[0].matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))throw new IllegalArgumentException("Jam observasi baris "+(r+1)+" harus HH:mm.");
            if(row[1].isEmpty())throw new IllegalArgumentException("Isi waktu HD pada baris "+(r+1)+".");
            validateNumber(row[1],"Waktu HD baris "+(r+1),false);
            BigDecimal elapsed=decimal(row[1]);
            if(elapsed.compareTo(new BigDecimal("999.99"))>0 || elapsed.stripTrailingZeros().scale()>2)throw new IllegalArgumentException("Waktu HD maksimal 999,99 jam / dua desimal.");
            row[1]=elapsed.toPlainString();validateTensi(row[2]);
            for(int c=3;c<=10;c++)validateNumber(row[c],OBS_LABELS[c]+" baris "+(r+1),c==6||c==8||c==9);
            for(int c=2;c<12;c++)if(row[c].length()>(c==11?8000:30))throw new IllegalArgumentException("Isian observasi terlalu panjang, baris "+(r+1)+".");
            s.rows.add(row);
        }
        return s;
    }
    private static BigDecimal decimal(String value) {
        try {return new BigDecimal(value.replace(',','.'));}catch(NumberFormatException e){throw new IllegalArgumentException("Nilai angka tidak valid: "+value);}
    }
    private static void validateNumber(String value,String label,boolean signed) {
        if(value.isEmpty())return;
        if(!value.matches(signed?"-?[0-9]+(?:[.,][0-9]+)?":"[0-9]+(?:[.,][0-9]+)?"))throw new IllegalArgumentException(label+" harus berupa angka"+(signed?".":" nol atau positif."));
    }
    private static void validateTensi(String value) {
        if(!value.isEmpty()&&!value.matches("[0-9]+\\s*/\\s*[0-9]+"))throw new IllegalArgumentException("Tekanan darah gunakan format sistolik/diastolik, contoh 120/80.");
    }
    private void saveForm() {
        if(current!=null&&current.id!=null){info("Sesi ini sudah tersimpan. Gunakan Ganti untuk memperbarui.");return;}
        persistForm();
    }
    private void updateForm() {
        if(current==null||current.id==null){info("Muat sesi tersimpan dari Daftar Sesi HD sebelum memilih Ganti.");return;}
        persistForm();
    }
    private void persistForm() {
        try{Session s=snapshot();job(()->store.save(s,user),saved->{current=saved;dirty=false;refreshActionState();info("Formulir tersimpan.");refreshList();});}
        catch(IllegalArgumentException e){info(e.getMessage());}
    }
    private void deleteForm() {
        if(current==null||current.id==null){info("Muat sesi tersimpan terlebih dahulu.");return;}
        if(JOptionPane.showConfirmDialog(this,"Hapus sesi HD ini beserta seluruh observasinya?","Hapus Sesi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        Session s=current.copy();job(()->{store.delete(s);return store.patient(s.rawat,user);},patient->{current=patient;resetFields();loading=true;fill(patient);loading=false;dirty=false;refreshList();});
    }
    private void closeForm(){if(!busy&&mayDiscard())dispose();}
    @Override public void dispose(){
        if(busy)return;
        if(ownsConnection){try{store.c.close();}catch(SQLException ignored){} ownsConnection=false;}
        super.dispose();
    }
    private void info(String message){JOptionPane.showMessageDialog(this,message,"Formulir Hemodialisa",JOptionPane.INFORMATION_MESSAGE);}
    private interface DbTask<T>{T run() throws Exception;}
    private <T> void job(DbTask<T> task,java.util.function.Consumer<T> success) {
        busy=true;for(Component c:inputs)c.setEnabled(false);
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() throws Exception{return task.run();}
            @Override protected void done(){
                busy=false;for(Component c:inputs)c.setEnabled(true);
                try{success.accept(get());}catch(Exception e){Throwable cause=e.getCause()==null?e:e.getCause();info("Proses gagal: "+cause.getMessage());}
                finally{if(!busy)refreshActionState();}
            }
        }.execute();
    }
    private void printForm() {
        if(observasi.isEditing())observasi.getCellEditor().stopCellEditing();
        if(current==null||current.id==null||dirty){info("Simpan perubahan formulir sebelum mencetak.");return;}
        JEditorPane page=new JEditorPane("text/html",html(current));page.setEditable(false);
        try{page.print(new MessageFormat("Formulir Hemodialisa — "+current.rm),new MessageFormat("Halaman {0}"),true,null,null,true);}
        catch(java.awt.print.PrinterException e){info("Cetak gagal: "+e.getMessage());}
    }
    static String esc(String value) {
        if(value==null)return "";return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("\n","<br>");
    }
    private static String cell(String label,String value){return "<td><b>"+esc(label)+"</b><br>"+esc(value)+"</td>";}
    static String html(Session s) {
        StringBuilder b=new StringBuilder("<html><head><style>body{font-family:Arial;font-size:10pt;}td,th{padding:4px;}h2{text-align:center;font-size:15pt;}h3{font-size:11pt;}table{width:100%;}</style></head><body>");
        b.append("<b>RS AKADEMIS JAURY JUSUF PUTERA</b><h2>FORMULIR HEMODIALISA</h2><table border='1' cellspacing='0'>");
        b.append("<tr>").append(cell("Hari / tanggal",new SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm",new Locale("id","ID")).format(s.date))).append(cell("No. Register / RM",s.rm)).append("</tr>");
        b.append("<tr>").append(cell("Nama penderita",s.name)).append(cell("No. Rawat",s.rawat)).append("</tr>");
        b.append("<tr>").append(cell("Jenis kelamin / umur",s.sex+" / "+s.age)).append(cell("Ruang / bangsal",s.data.get("ruangan"))).append("</tr>");
        b.append("<tr>").append(cell("Alamat rumah",s.data.get("alamat"))).append(cell("Hemodialisa ke",String.valueOf(s.hdKe))).append("</tr></table><h3>Data klinik</h3><table border='1' cellspacing='0'>");
        String[] clinical={"anamnesis","pemeriksaan_fisik","laboratorium","diagnosa","terapi"};
        String[] clinicalNames={"Anamnesis","Pemeriksaan fisik","Laboratorium","Diagnosa","Terapi"};
        for(int i=0;i<clinical.length;i++)b.append("<tr>").append(cell(clinicalNames[i],s.data.get(clinical[i]))).append("</tr>");
        b.append("</table><h3>Status presens / laboratorium / pelaksanaan</h3><table border='1' cellspacing='0'>");
        String[] labKeys={"nadi","tekanan_darah","suhu","pernapasan","bb_sebelum","bb_sesudah","hb","elektrolit","ureum_sebelum","ureum_sesudah","kreatinin_sebelum","kreatinin_sesudah","heparin_beban","heparin_selama","heparin_per_jam","lama_hd","priming_volume"};
        String[] labNames={"Nadi (x/menit)","Tekanan darah (mmHg)","Suhu (°C)","Pernapasan (x/menit)","BB sebelum HD (kg)","BB sesudah HD (kg)","Hb","Elektrolit","Ureum sebelum HD","Ureum sesudah HD","Kreatinin sebelum HD","Kreatinin sesudah HD","Dosis heparin beban (U)","Total dosis selama HD (U)","Dosis tiap jam (U/jam)","Lama HD (jam)","Priming volume (mL)"};
        for(int i=0;i<labKeys.length;i+=2){b.append("<tr>").append(cell(labNames[i],s.data.get(labKeys[i])));if(i+1<labKeys.length)b.append(cell(labNames[i+1],s.data.get(labKeys[i+1])));else b.append("<td></td>");b.append("</tr>");}
        String a=s.data.get("heparin_beban"),d=s.data.get("heparin_selama");
        String total=a.isEmpty()&&d.isEmpty()?"":decimal(a.isEmpty()?"0":a).add(decimal(d.isEmpty()?"0":d)).stripTrailingZeros().toPlainString()+" U";
        b.append("<tr>").append(cell("Jumlah heparin (beban + selama HD)",total)).append("<td></td></tr></table><h3>Perubahan selama hemodialisa</h3><table border='1' cellspacing='0'><tr>");
        for(String label:OBS_LABELS)b.append("<th>").append(esc(label)).append("</th>");b.append("</tr>");
        for(String[] row:s.rows){b.append("<tr>");for(String v:row)b.append("<td>").append(esc(v)).append("</td>");b.append("</tr>");}
        if(s.rows.isEmpty())b.append("<tr><td colspan='12'>Belum ada hasil observasi yang dicatat.</td></tr>");
        b.append("</table><br><table><tr>").append(cell("Pelaksana",s.data.get("pelaksana_nama")+" / "+s.data.get("pelaksana_nip"))).append(cell("Dokter",s.data.get("dokter_nama"))).append("</tr><tr><td><br><br>Tanda tangan: __________________</td><td><br><br>Tanda tangan: __________________</td></tr></table></body></html>");return b.toString();
    }

    static final class Session {
        String id,rawat,rm,name,sex,age;int version,hdKe=1;
        java.util.Date date=new java.util.Date();
        Map<String,String> data=new LinkedHashMap<>();List<String[]> rows=new ArrayList<>();
        Session(){for(String key:KEYS)data.put(key,"");}
        Session copy(){Session s=new Session();s.id=id;s.rawat=rawat;s.rm=rm;s.name=name;s.sex=sex;s.age=age;s.version=version;s.hdKe=hdKe;s.date=new java.util.Date(date.getTime());s.data.putAll(data);for(String[] row:rows)s.rows.add(row.clone());return s;}
    }
    /** Seluruh nilai pengguna memakai PreparedStatement; induk/detail satu transaksi. */
    static final class Store {
        private final Connection c;
        Store(Connection connection){c=Objects.requireNonNull(connection,"Koneksi database belum tersedia");}
        Session patient(String n,String actor) throws SQLException {
            synchronized(c){
                Session s=new Session();String status;java.sql.Date registrationDate;java.sql.Time registrationTime;
                try(PreparedStatement p=c.prepareStatement("SELECT r.no_rawat,p.no_rkm_medis,p.nm_pasien,p.jk,r.umurdaftar,r.sttsumur,p.alamat,r.status_lanjut,r.tgl_registrasi,r.jam_reg FROM reg_periksa r JOIN pasien p ON p.no_rkm_medis=r.no_rkm_medis WHERE r.no_rawat=?")){
                    p.setString(1,n);try(ResultSet r=p.executeQuery()){
                        if(!r.next())throw new SQLException("No. Rawat tidak ditemukan.");
                        s.rawat=r.getString(1);s.rm=r.getString(2);s.name=r.getString(3);s.sex="L".equals(r.getString(4))?"Pria":"Wanita";
                        s.age=r.getString(5)+" "+r.getString(6);s.data.put("alamat",safe(r.getString(7)));
                        status=safe(r.getString(8));registrationDate=r.getDate(9);registrationTime=r.getTime(10);
                    }
                }
                boolean inpatient="Ranap".equalsIgnoreCase(status);
                String roomSql=inpatient?
                    "SELECT CONCAT(k.kd_kamar,' - ',b.nm_bangsal) FROM kamar_inap ki JOIN kamar k ON k.kd_kamar=ki.kd_kamar JOIN bangsal b ON b.kd_bangsal=k.kd_bangsal WHERE ki.no_rawat=? ORDER BY CASE WHEN ki.tgl_keluar='0000-00-00' THEN 0 ELSE 1 END,ki.tgl_masuk DESC,ki.jam_masuk DESC LIMIT 1":
                    "SELECT p.nm_poli FROM reg_periksa r JOIN poliklinik p ON p.kd_poli=r.kd_poli WHERE r.no_rawat=?";
                try(PreparedStatement p=c.prepareStatement(roomSql)){p.setString(1,n);try(ResultSet r=p.executeQuery()){if(r.next())s.data.put("ruangan",safe(r.getString(1)));}}
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM reg_periksa WHERE no_rkm_medis=? AND kd_poli='53' AND stts<>'Batal' AND (tgl_registrasi<? OR (tgl_registrasi=? AND jam_reg<=?))")){
                    p.setString(1,s.rm);p.setDate(2,registrationDate);p.setDate(3,registrationDate);p.setTime(4,registrationTime);
                    try(ResultSet r=p.executeQuery()){if(r.next())s.hdKe=r.getInt(1);}
                }
                String table=inpatient?"pemeriksaan_ranap":"pemeriksaan_ralan";
                Map<String,String> soap=latestSoap(table,n,actor,false);
                if(!soap.isEmpty()) {
                    s.data.put("anamnesis",soap.get("keluhan"));s.data.put("pemeriksaan_fisik",soap.get("pemeriksaan"));
                    s.data.put("diagnosa",soap.get("penilaian"));
                    String plan=soap.get("rtl"),instructions=soap.get("instruksi");
                    s.data.put("terapi",plan.isEmpty()?instructions:instructions.isEmpty()||instructions.equals(plan)?plan:plan+"\nInstruksi: "+instructions);
                }
                Map<String,String> vital=hasVitals(soap)?soap:latestSoap(table,n,actor,true);
                if(!vital.isEmpty()) {
                    s.data.put("nadi",vital.get("nadi"));s.data.put("tekanan_darah",vital.get("tensi"));
                    s.data.put("suhu",vital.get("suhu_tubuh"));s.data.put("pernapasan",vital.get("respirasi"));s.data.put("bb_sebelum",vital.get("berat"));
                }
                return s;
            }
        }
        private Map<String,String> latestSoap(String table,String n,String actor,boolean vitalOnly) throws SQLException {
            // table comes only from the two fixed Ralan/Ranap names above.
            String sql="SELECT pr.*,pg.nama FROM "+table+" pr LEFT JOIN pegawai pg ON pg.nik=pr.nip WHERE pr.no_rawat=? "+
                "AND (pr.nip=? OR LOWER(COALESCE(pg.jbtn,'')) LIKE '%perawat%' OR LOWER(COALESCE(pg.jbtn,'')) LIKE '%keperawatan%') "+
                (vitalOnly?"AND (TRIM(COALESCE(pr.tensi,''))<>'' OR TRIM(COALESCE(pr.nadi,''))<>'' OR TRIM(COALESCE(pr.suhu_tubuh,''))<>'' OR TRIM(COALESCE(pr.respirasi,''))<>'' OR TRIM(COALESCE(pr.berat,''))<>'') ":"")+
                "ORDER BY CASE WHEN pr.nip=? THEN 0 ELSE 1 END,pr.tgl_perawatan DESC,pr.jam_rawat DESC,pr.nip LIMIT 1";
            Map<String,String> values=new LinkedHashMap<>();
            try(PreparedStatement p=c.prepareStatement(sql)) {
                p.setString(1,n);p.setString(2,actor==null?"":actor);p.setString(3,actor==null?"":actor);
                try(ResultSet r=p.executeQuery()){if(r.next())for(String key:new String[]{"keluhan","pemeriksaan","penilaian","rtl","instruksi","nadi","tensi","suhu_tubuh","respirasi","berat"})values.put(key,safe(r.getString(key)).trim());}
            }
            return values;
        }
        private static boolean hasVitals(Map<String,String> values) {
            for(String key:new String[]{"nadi","tensi","suhu_tubuh","respirasi","berat"})if(!values.getOrDefault(key,"").isEmpty())return true;
            return false;
        }
        List<Object[]> list(String n) throws SQLException {
            synchronized(c){List<Object[]> out=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT id_hd,tanggal,hd_ke,pelaksana_nama,versi FROM rsaj_formulir_hemodialisa WHERE no_rawat=? ORDER BY tanggal DESC,id_hd")){
                p.setString(1,n);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Object[]{r.getString(1),r.getTimestamp(2),r.getInt(3),r.getString(4),r.getInt(5)});}return out;
            }}
        }
        Session load(String id) throws SQLException {
            synchronized(c){Session s=new Session();transaction(()->{try(PreparedStatement p=c.prepareStatement("SELECT * FROM rsaj_formulir_hemodialisa WHERE id_hd=? FOR UPDATE")){
                p.setString(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Sesi sudah dihapus. Refresh daftar.");s.id=id;s.rawat=r.getString("no_rawat");s.rm=r.getString("no_rkm_medis");s.name=r.getString("nama_pasien");s.sex=r.getString("jenis_kelamin");s.age=r.getString("umur");s.date=r.getTimestamp("tanggal");s.hdKe=r.getInt("hd_ke");s.version=r.getInt("versi");for(String key:KEYS)s.data.put(key,safe(r.getString(key)));}
            }
            try(PreparedStatement p=c.prepareStatement("SELECT * FROM rsaj_formulir_hemodialisa_observasi WHERE id_hd=? ORDER BY urutan")){
                p.setString(1,id);try(ResultSet r=p.executeQuery()){while(r.next()){String[] row=new String[12];for(int i=0;i<12;i++)row[i]=safe(r.getString(OBS_KEYS[i]));s.rows.add(row);}}
            }});return s;}
        }
        Session save(Session source,String actor) throws SQLException {
            synchronized(c){Session s=source.copy();boolean fresh=s.id==null;if(fresh)s.id=UUID.randomUUID().toString();
                transaction(()->{
                    try(PreparedStatement p=c.prepareStatement("SELECT no_rawat FROM reg_periksa WHERE no_rawat=?")){p.setString(1,s.rawat);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Registrasi pasien sudah tidak tersedia.");}}
                    String[] base={"no_rawat","tanggal","hd_ke","no_rkm_medis","nama_pasien","jenis_kelamin","umur"};
                    List<String> cols=new ArrayList<>(Arrays.asList(base));cols.addAll(Arrays.asList(KEYS));cols.add("diubah_oleh");
                    String sql;
                    if(fresh){cols.add("dibuat_oleh");cols.add("id_hd");sql="INSERT INTO rsaj_formulir_hemodialisa ("+String.join(",",cols)+") VALUES ("+String.join(",",Collections.nCopies(cols.size(),"?"))+")";}
                    else {List<String> sets=new ArrayList<>();for(String key:cols)sets.add(key+"=?");sql="UPDATE rsaj_formulir_hemodialisa SET "+String.join(",",sets)+",versi=versi+1 WHERE id_hd=? AND versi=?";}
                    try(PreparedStatement p=c.prepareStatement(sql)){
                        int i=1;p.setString(i++,s.rawat);p.setTimestamp(i++,new Timestamp(s.date.getTime()));p.setInt(i++,s.hdKe);p.setString(i++,s.rm);p.setString(i++,s.name);p.setString(i++,s.sex);p.setString(i++,s.age);
                        for(String key:KEYS)p.setString(i++,s.data.get(key));p.setString(i++,actor);
                        if(fresh){p.setString(i++,actor);p.setString(i,s.id);}else{p.setString(i++,s.id);p.setInt(i,s.version);}
                        if(p.executeUpdate()!=1)throw new SQLException("Sesi diubah/dihapus pengguna lain. Muat ulang sebelum menyimpan.");
                    }
                    try(PreparedStatement p=c.prepareStatement("DELETE FROM rsaj_formulir_hemodialisa_observasi WHERE id_hd=?")){p.setString(1,s.id);p.executeUpdate();}
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO rsaj_formulir_hemodialisa_observasi (id_hd,urutan,"+String.join(",",OBS_KEYS)+") VALUES ("+String.join(",",Collections.nCopies(14,"?"))+")")){
                        int seq=0;for(String[] row:s.rows){p.setString(1,s.id);p.setInt(2,++seq);for(int i=0;i<12;i++){if(i==1)p.setBigDecimal(i+3,decimal(row[i]));else p.setString(i+3,row[i]);}p.addBatch();}p.executeBatch();
                    }
                });s.version=fresh?1:s.version+1;return s;
            }
        }
        void delete(Session s) throws SQLException {
            synchronized(c){transaction(()->{try(PreparedStatement p=c.prepareStatement("DELETE FROM rsaj_formulir_hemodialisa WHERE id_hd=? AND versi=?")){
                p.setString(1,s.id);p.setInt(2,s.version);if(p.executeUpdate()!=1)throw new SQLException("Sesi telah berubah. Muat ulang sebelum menghapus.");
            }});}
        }
        private interface SqlAction {void run() throws SQLException;}
        private void transaction(SqlAction action) throws SQLException {
            if(!c.getAutoCommit())throw new SQLException("Koneksi sedang digunakan transaksi lain. Coba lagi setelah proses selesai.");
            c.setAutoCommit(false);
            try{action.run();c.commit();}
            catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
            finally{c.setAutoCommit(true);}
        }
        private static String safe(String value){return value==null?"":value;}
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    // End of variables declaration//GEN-END:variables
}
