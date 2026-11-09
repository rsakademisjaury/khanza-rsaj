package bridging;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/** Komponen bersama khusus SSRME; tidak mengubah Look and Feel global SIMRS. */
public final class SsrmeUi {
    private static final Color BLUE=new Color(35,99,190), INK=new Color(36,53,75), SOFT=new Color(235,244,255);
    public static final String SHARING_MESSAGE="SATUSEHAT menyatakan resume medis belum dibagikan oleh pasien. Minta pasien memeriksa akses dan pengaturan berbagi Resume Medis di SATUSEHAT Mobile.";
    public static final String PATIENT_GUIDE="Yang perlu disampaikan kepada pasien:\n\n"
            +"1. Buka Resume Medis di SATUSEHAT Mobile. Jika belum terpasang, unduh aplikasinya terlebih dahulu.\n\n"
            +"2. Pastikan profil sudah terverifikasi (KYC) dan fitur Resume Medis dapat diakses.\n\n"
            +"3. Periksa pengaturan berbagi/persetujuan RME pada aplikasi pasien.\n\n"
            +"4. Untuk akses non-gawat darurat, gunakan kode akses enam digit dari SATUSEHAT Mobile pada halaman Persetujuan SSRME. Kode ini berbeda dari PIN pribadi pasien.";
    private SsrmeUi() { }
    public static Rectangle usable(Rectangle bounds, Insets taskbar) {
        return new Rectangle(bounds.x+taskbar.left,bounds.y+taskbar.top,
                Math.max(1,bounds.width-taskbar.left-taskbar.right),Math.max(1,bounds.height-taskbar.top-taskbar.bottom));
    }
    public static void fit(Window window, Window owner) {
        GraphicsConfiguration gc=owner==null?window.getGraphicsConfiguration():owner.getGraphicsConfiguration();
        if(gc==null)gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle area=usable(gc.getBounds(),Toolkit.getDefaultToolkit().getScreenInsets(gc));
        window.setMinimumSize(new Dimension(Math.min(680,area.width),Math.min(440,area.height)));window.setBounds(area);
    }

    /** JFrame dengan min/max native dan penantian EDT tanpa membuka form lain saat alur SSRME aktif. */
    public static class WindowFrame extends JFrame {
        private static final java.util.List<WindowFrame> BLOCKING=new ArrayList<WindowFrame>();
        final Window modalOwner;
        private final Map<Window,Boolean> disabled=new IdentityHashMap<Window,Boolean>();
        private SecondaryLoop waiting;
        private boolean released;
        private AWTEventListener opened;
        protected WindowFrame(Window owner,String title) {
            super(title,owner==null?null:owner.getGraphicsConfiguration());modalOwner=owner;
            setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            if(owner!=null)setIconImages(owner.getIconImages());
            setUndecorated(false);getRootPane().setWindowDecorationStyle(JRootPane.NONE);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);setResizable(true);
            fit(this,owner);Rectangle area=getBounds();setMaximizedBounds(area);
            if(Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)){
                int w=Math.max(getMinimumSize().width,area.width*9/10),h=Math.max(getMinimumSize().height,area.height*9/10);
                setBounds(area.x+(area.width-w)/2,area.y+(area.height-h)/2,w,h);setExtendedState(Frame.MAXIMIZED_BOTH);
            }
        }
        private boolean owns(Window w) {
            for(Window p=w;p!=null;p=p instanceof WindowFrame?((WindowFrame)p).modalOwner:p.getOwner())if(p==this)return true;
            return false;
        }
        private void block(Window w) {
            if(w!=this&&w.isShowing()&&w.isEnabled()&&!owns(w)){disabled.put(w,Boolean.TRUE);w.setEnabled(false);}
        }
        public final void showBlocking() {
            if(!SwingUtilities.isEventDispatchThread())throw new IllegalStateException("EDT required");
            if(released)return;
            waiting=Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();BLOCKING.add(this);
            opened=e->{if(e instanceof WindowEvent&&e.getID()==WindowEvent.WINDOW_OPENED&&!BLOCKING.isEmpty()&&BLOCKING.get(BLOCKING.size()-1)==this)block(((WindowEvent)e).getWindow());};
            Toolkit.getDefaultToolkit().addAWTEventListener(opened,AWTEvent.WINDOW_EVENT_MASK);
            try{
                for(Window w:Window.getWindows())block(w);
                setVisible(true);if(!released)waiting.enter();
            }finally{dispose();}
        }
        @Override public void dispose() {
            if(!released){
                released=true;
                // JFrame tidak memiliki owner AWT. Tutup juga viewer/JSON yang memakai owner logis ini.
                for(Window w:Window.getWindows())if(w!=this&&w instanceof WindowFrame&&owns(w))w.dispose();
                if(opened!=null)Toolkit.getDefaultToolkit().removeAWTEventListener(opened);
                BLOCKING.remove(this);
                for(Window w:disabled.keySet())if(w.isDisplayable())w.setEnabled(true);
                disabled.clear();
                if(!BLOCKING.isEmpty())for(Window w:Window.getWindows())BLOCKING.get(BLOCKING.size()-1).block(w);
            }
            super.dispose();if(waiting!=null)waiting.exit();
        }
    }

    public static final class Header extends JPanel {
        private final JLabel patient=identity("Memuat pasien…",0),rm=identity("No. RM …",1),doctor=identity("Dokter: memeriksa akun…",2),page=label("Akses SSRME",false);
        public Header() {
            setName("ssrme.viewer.header");setBackground(Color.WHITE);setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
            setPreferredSize(new Dimension(0,50));setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
            JLabel title=label("SATUSEHAT RME",true);title.setFont(new Font("Segoe UI Semibold",Font.PLAIN,17));title.setForeground(BLUE);
            add(title);add(Box.createHorizontalStrut(18));add(patient);add(Box.createHorizontalStrut(30));
            add(rm);add(Box.createHorizontalStrut(30));add(doctor);add(Box.createHorizontalGlue());add(Box.createHorizontalStrut(12));
            page.setForeground(BLUE);page.setBackground(SOFT);page.setOpaque(true);page.setBorder(BorderFactory.createEmptyBorder(5,9,5,9));add(page);
            for(Component c:getComponents())if(c instanceof JLabel){c.setMaximumSize(c.getPreferredSize());((JLabel)c).setToolTipText(((JLabel)c).getText());}
            patient.setMinimumSize(new Dimension(50,22));rm.setMinimumSize(new Dimension(65,22));doctor.setMinimumSize(new Dimension(70,22));page.setMinimumSize(new Dimension(30,22));
        }
        public void update(SsrmeClient.Setup setup,String action) {
            if(setup!=null){
                value(patient,setup.patientName);value(rm,"No. RM "+setup.patientRm);
                String account=SsrmeClient.text(setup.actorDoctor);if(account.isEmpty())account=SsrmeClient.text(setup.actor);
                String name=account;for(SsrmeClient.Doctor d:setup.doctors)if(d.code.equals(account)){name=d.name;break;}
                value(doctor,"Dokter: "+name);
            }
            value(page,"VIEW".equals(action)?"Rekam medis nasional":"CONSENT".equals(action)?"Persetujuan SSRME":"EMERGENCY".equals(action)?"Akses emergency":"Akses SSRME");
            revalidate();repaint();
        }
        public void noDoctor(){value(doctor,"Dokter: akun bukan dokter aktif");revalidate();repaint();}
        private static void value(JLabel l,String text){l.setText(text);l.setToolTipText(text);l.setMaximumSize(l.getPreferredSize());}
    }
    public static JLabel label(String text,boolean bold) {
        JLabel l=new JLabel(text);l.putClientProperty("html.disable",Boolean.TRUE);l.setFont(new Font(bold?"Segoe UI Semibold":"Segoe UI",Font.PLAIN,12));l.setForeground(INK);return l;
    }
    private static JLabel identity(String text,int kind){JLabel l=label(text,true);l.setIcon(new IdentityIcon(kind));l.setIconTextGap(7);return l;}

    /** Bantuan baca saja; tidak melakukan request, mengubah izin, atau membaca isi halaman resmi. */
    public static JButton guideButton(Window owner,boolean emergency){
        JButton open=button("Panduan pasien");final WindowFrame[] shown={null};
        open.addActionListener(e->{
            if(shown[0]!=null&&shown[0].isDisplayable()){shown[0].toFront();return;}
            WindowFrame guide=new WindowFrame(owner,"SATUSEHAT RME · Panduan pasien");shown[0]=guide;
            JPanel body=new JPanel(new BorderLayout());body.setBackground(Color.WHITE);guide.setContentPane(body);
            JPanel top=new JPanel(new BorderLayout());top.setBackground(Color.WHITE);top.setPreferredSize(new Dimension(0,50));top.setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
            JLabel title=label("SATUSEHAT RME · Panduan pasien",true);title.setFont(new Font("Segoe UI Semibold",Font.PLAIN,17));top.add(title);body.add(top,BorderLayout.NORTH);
            StatusPanel help=new StatusPanel();help.showStatus("Siapkan persetujuan pasien",emergency
                    ?"Panduan ini untuk akses non-gawat darurat. Akses emergency tetap mengikuti alur darurat yang berlaku pada halaman resmi."
                    :"Sampaikan panduan berikut kepada pasien sebelum proses persetujuan SSRME.",StatusPanel.LOCKED);
            help.setGuidance(PATIENT_GUIDE);body.add(help,BorderLayout.CENTER);
            JButton close=button("Tutup");close.addActionListener(v->guide.dispose());body.add(footer(null,close),BorderLayout.SOUTH);guide.setVisible(true);
        });return open;
    }

    /** Pesan lokal SSRME: ilustrasi, judul dan penjelasan plain text di tengah area. */
    public static final class StatusPanel extends JPanel {
        public static final int LOCKED=0, ERROR=1, INFO=2;
        private final JTextPane title=plain(22,true),detail=plain(14,false),guidance=plain(13,false);
        private final JLabel picture=new JLabel(new StatusPicture(LOCKED));
        private final JButton back=button("Kembali ke halaman");
        private final Center center=new Center();
        private final Block block=new Block();
        private Runnable returnAction;
        private int kind;
        public StatusPanel(){
            super(new BorderLayout());setName("ssrme.status");setBackground(Color.WHITE);
            title.setName("ssrme.status.title");detail.setName("ssrme.status.detail");
            guidance.setName("ssrme.status.guide");guidance.setVisible(false);guidance.setOpaque(true);guidance.setBackground(new Color(245,249,255));
            guidance.setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
            javax.swing.text.SimpleAttributeSet left=new javax.swing.text.SimpleAttributeSet();javax.swing.text.StyleConstants.setAlignment(left,javax.swing.text.StyleConstants.ALIGN_LEFT);guidance.setParagraphAttributes(left,false);
            back.setVisible(false);back.addActionListener(e->{if(returnAction!=null)returnAction.run();});
            center.setLayout(new GridBagLayout());center.setBackground(Color.WHITE);center.add(block);
            JScrollPane scroll=new JScrollPane(center);scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getViewport().setBackground(Color.WHITE);scroll.getVerticalScrollBar().setUnitIncrement(20);add(scroll);
            addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent e){center.revalidate();}});
        }
        public void showStatus(String heading,String explanation,int type){
            kind=type;title.setText(heading==null?"":heading);detail.setText(explanation==null?"":explanation);
            guidance.setText("");guidance.setVisible(false);
            picture.setIcon(new StatusPicture(type));picture.getAccessibleContext().setAccessibleName(type==LOCKED?"Ilustrasi rekam medis terkunci":type==ERROR?"Ilustrasi layanan memerlukan perhatian":"Ilustrasi rekam medis");
            title.setCaretPosition(0);detail.setCaretPosition(0);center.revalidate();repaint();
        }
        public String getText(){return detail.getText();}
        public String getGuidance(){return guidance.getText();}
        public void setGuidance(String value){guidance.setText(value==null?"":value);guidance.setCaretPosition(0);guidance.setVisible(value!=null&&!value.isEmpty());center.revalidate();repaint();}
        public String getTitleText(){return title.getText();}
        public int getKind(){return kind;}
        public void setReturnAction(Runnable action){returnAction=action;back.setVisible(action!=null);center.revalidate();}
        private static JTextPane plain(int size,boolean bold){
            JTextPane t=new JTextPane();t.setEditable(false);t.setFocusable(false);t.setOpaque(false);t.setBorder(BorderFactory.createEmptyBorder());
            t.setFont(new Font(bold?"Segoe UI Semibold":"Segoe UI",bold?Font.BOLD:Font.PLAIN,size));t.setForeground(bold?new Color(24,39,57):new Color(84,102,122));
            javax.swing.text.SimpleAttributeSet a=new javax.swing.text.SimpleAttributeSet();javax.swing.text.StyleConstants.setAlignment(a,javax.swing.text.StyleConstants.ALIGN_CENTER);
            javax.swing.text.StyleConstants.setLineSpacing(a,.18f);t.setParagraphAttributes(a,false);return t;
        }
        private int width(){int w=center.getParent()==null?getWidth():center.getParent().getWidth();return Math.min(560,Math.max(120,w-48));}
        private int height(JTextPane text,int w){text.setSize(w,Integer.MAX_VALUE/16);return Math.max(text.getFontMetrics(text.getFont()).getHeight(),text.getPreferredSize().height);}
        private final class Block extends JPanel {
            Block(){super(null);setOpaque(false);add(picture);add(title);add(detail);add(guidance);add(back);}
            @Override public Dimension getPreferredSize(){int w=width();return new Dimension(w,150+24+height(title,w)+18+height(detail,w)+(guidance.isVisible()?24+height(guidance,w):0)+(back.isVisible()?58:0));}
            @Override public void doLayout(){int w=getWidth(),y=0;picture.setBounds((w-180)/2,0,180,150);y=174;
                int th=height(title,w);title.setBounds(0,y,w,th);y+=th+18;int dh=height(detail,w);detail.setBounds(0,y,w,dh);y+=dh;
                if(guidance.isVisible()){y+=24;int gh=height(guidance,w);guidance.setBounds(0,y,w,gh);y+=gh;}
                if(back.isVisible()){y+=24;Dimension d=back.getPreferredSize();back.setBounds((w-d.width)/2,y,d.width,34);}}
        }
        private final class Center extends JPanel implements Scrollable {
            @Override public Dimension getPreferredSize(){Dimension d=block.getPreferredSize();return new Dimension(d.width+48,d.height+48);}
            public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
            public int getScrollableUnitIncrement(Rectangle r,int orientation,int direction){return 20;}
            public int getScrollableBlockIncrement(Rectangle r,int orientation,int direction){return Math.max(20,r.height-20);}
            public boolean getScrollableTracksViewportWidth(){return true;}
            public boolean getScrollableTracksViewportHeight(){return getParent()!=null&&getPreferredSize().height<=getParent().getHeight();}
        }
    }
    private static final class StatusPicture implements Icon {
        private final int kind;StatusPicture(int kind){this.kind=kind;}public int getIconWidth(){return 180;}public int getIconHeight(){return 150;}
        public void paintIcon(Component c,Graphics original,int x,int y){
            Graphics2D g=(Graphics2D)original.create();g.translate(x,y);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.geom.Path2D blob=new java.awt.geom.Path2D.Double();blob.moveTo(24,83);blob.curveTo(8,41,52,5,88,19);blob.curveTo(130,4,176,44,166,88);blob.curveTo(180,130,138,146,94,135);blob.curveTo(46,143,18,117,24,83);
            g.setColor(new Color(226,243,255));g.fill(blob);g.setColor(new Color(255,168,70));g.fillOval(148,29,8,8);g.setStroke(new BasicStroke(1.6f));g.drawOval(44,13,8,8);
            Graphics2D doc=(Graphics2D)g.create();doc.rotate(.16,104,80);doc.setColor(new Color(164,208,251));doc.fillRoundRect(68,30,72,103,5,5);
            doc.setColor(new Color(201,228,255));doc.fillRoundRect(64,26,72,103,5,5);doc.setColor(Color.WHITE);doc.fillRoundRect(60,22,72,103,4,4);
            doc.setColor(new Color(117,178,241));doc.setStroke(new BasicStroke(1));doc.drawRoundRect(60,22,72,103,4,4);
            doc.setColor(new Color(210,229,249));for(int row=0;row<7;row++)doc.drawLine(72,39+row*7,119-(row%3)*7,39+row*7);
            doc.setColor(new Color(162,203,246));doc.drawOval(103,98,17,17);doc.drawOval(106,101,11,11);doc.drawLine(72,107,94,107);doc.dispose();
            if(kind==StatusPanel.LOCKED){
                g.setColor(new Color(35,99,190));g.setStroke(new BasicStroke(3));g.drawRoundRect(30,50,29,40,15,15);g.setColor(Color.WHITE);g.setStroke(new BasicStroke(1.5f));g.drawRoundRect(34,54,21,34,11,11);
                g.setColor(new Color(38,196,208));g.fillRoundRect(24,75,42,33,6,6);g.setColor(new Color(25,114,200));g.drawRoundRect(24,75,42,33,6,6);g.fillOval(42,85,6,6);g.fillRect(43,90,4,8);
            }else if(kind==StatusPanel.ERROR){
                g.setColor(new Color(255,240,210));g.fillRoundRect(20,72,48,40,12,12);g.setColor(new Color(238,159,43));g.setStroke(new BasicStroke(2));g.drawPolygon(new int[]{44,63,25},new int[]{77,106,106},3);g.drawLine(44,86,44,94);g.fillOval(42,99,4,4);
            }else{
                g.setColor(new Color(44,190,202));g.fillOval(23,74,41,41);g.setColor(Color.WHITE);g.setStroke(new BasicStroke(2.5f));g.drawLine(44,83,44,105);g.drawLine(33,94,55,94);
            }
            g.dispose();
        }
    }
    public static JPanel footer(JButton json,JButton... buttons) {
        JPanel footer=new JPanel(new GridBagLayout());footer.setName("ssrme.viewer.footer");footer.setBackground(Color.WHITE);
        footer.setPreferredSize(new Dimension(0,50));footer.setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
        GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.weightx=1;c.anchor=GridBagConstraints.WEST;
        if(json!=null)footer.add(json,c);else footer.add(Box.createHorizontalGlue(),c);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);
        for(JButton b:buttons)actions.add(b);
        c.gridx=1;c.weightx=0;c.anchor=GridBagConstraints.EAST;footer.add(actions,c);return footer;
    }
    public static JButton button(String value){return new ModernButton(value);}
    private static final class ModernButton extends JButton {
        private final boolean primary,danger;
        ModernButton(String text){
            super(text);String t=text.toLowerCase(java.util.Locale.ROOT);primary=t.contains("persetujuan")||t.startsWith("lihat ");danger=t.contains("emergency");
            setFont(new Font("Segoe UI Semibold",Font.PLAIN,12));setForeground(primary?Color.WHITE:danger?new Color(162,83,11):BLUE);
            setIcon(new ActionIcon(t));setIconTextGap(7);setBorder(BorderFactory.createEmptyBorder(7,12,7,12));
            setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setOpaque(false);setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));setToolTipText(text);
        }
        @Override public void updateUI(){setUI(new BasicButtonUI());}
        @Override public Dimension getPreferredSize(){Dimension d=super.getPreferredSize();return new Dimension(d.width,34);}
        @Override public Dimension getMaximumSize(){return getPreferredSize();}
        @Override protected void paintComponent(Graphics original){
            Graphics2D g=(Graphics2D)original.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            boolean enabled=isEnabled(),hover=getModel().isRollover(),pressed=getModel().isPressed();int w=getWidth(),h=getHeight();
            Color top=primary?new Color(53,117,224):danger?new Color(255,248,232):Color.WHITE;
            Color bottom=primary?BLUE:danger?new Color(255,240,214):new Color(247,250,255);
            if(!enabled){top=new Color(246,247,249);bottom=new Color(239,242,246);}
            else if(pressed){top=primary?new Color(25,76,159):new Color(218,233,254);bottom=top;}
            else if(hover){top=primary?new Color(66,133,238):new Color(235,244,255);bottom=primary?new Color(33,102,205):new Color(226,238,255);}
            g.setColor(new Color(29,65,115,enabled?22:8));g.fillRoundRect(2,3,w-4,h-4,12,12);
            g.setPaint(new GradientPaint(0,1,top,0,h-3,bottom));g.fillRoundRect(2,1,w-4,h-4,12,12);
            g.setColor(!enabled?new Color(226,230,237):primary?new Color(29,86,175):danger?new Color(243,211,162):new Color(206,222,244));
            g.drawRoundRect(2,1,w-5,h-5,12,12);
            if(isFocusOwner()){g.setColor(new Color(67,139,245));g.setStroke(new BasicStroke(2));g.drawRoundRect(1,0,w-3,h-2,13,13);}
            g.dispose();super.paintComponent(original);
        }
    }
    private static final class ActionIcon implements Icon {
        private final String type;ActionIcon(String type){this.type=type;}public int getIconWidth(){return 16;}public int getIconHeight(){return 16;}
        public void paintIcon(Component c,Graphics original,int x,int y){Graphics2D g=(Graphics2D)original.create();g.translate(x,y);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.isEnabled()?c.getForeground():new Color(158,168,182));g.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if(type.contains("json")){g.drawLine(5,3,1,8);g.drawLine(1,8,5,13);g.drawLine(11,3,15,8);g.drawLine(15,8,11,13);g.drawLine(9,3,7,13);}
            else if(type.contains("browser")){g.drawLine(9,2,14,2);g.drawLine(14,2,14,7);g.drawLine(14,2,7,9);g.drawLine(6,4,2,4);g.drawLine(2,4,2,14);g.drawLine(2,14,12,14);g.drawLine(12,14,12,10);}
            else if(type.contains("tutup")){g.drawLine(4,4,12,12);g.drawLine(12,4,4,12);}
            else if(type.contains("kembali")){g.drawLine(13,8,2,8);g.drawLine(2,8,7,3);g.drawLine(2,8,7,13);}
            else if(type.contains("ulang")||type.contains("cek")){g.drawArc(2,2,11,11,40,290);g.drawLine(12,1,13,5);g.drawLine(9,5,13,5);}
            else if(type.contains("copy")){g.drawRoundRect(5,5,9,9,2,2);g.drawLine(2,10,2,2);g.drawLine(2,2,10,2);}
            else if(type.contains("emergency")){g.drawPolygon(new int[]{8,15,1},new int[]{1,14,14},3);g.drawLine(8,6,8,9);g.fillOval(7,11,2,2);}
            else if(type.contains("persetujuan")){g.drawPolygon(new int[]{2,8,14,13,8,3},new int[]{3,1,3,10,15,10},6);g.drawLine(5,7,7,9);g.drawLine(7,9,11,5);}
            else {g.drawRoundRect(3,1,10,14,2,2);g.drawLine(6,5,10,5);g.drawLine(6,8,10,8);g.drawLine(6,11,9,11);}
            g.dispose();}
    }
    private static final class IdentityIcon implements Icon {
        private final int kind;IdentityIcon(int kind){this.kind=kind;}public int getIconWidth(){return 22;}public int getIconHeight(){return 22;}
        public void paintIcon(Component c,Graphics original,int x,int y){Graphics2D g=(Graphics2D)original.create();g.translate(x,y);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(SOFT);g.fillRoundRect(0,0,22,22,8,8);g.setColor(BLUE);g.setStroke(new BasicStroke(1.4f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if(kind==1){g.drawRoundRect(4,5,14,12,2,2);g.drawLine(7,9,9,9);g.drawLine(12,9,15,9);g.drawLine(7,13,15,13);}
            else {g.drawOval(8,4,6,6);g.drawArc(5,12,12,11,0,180);if(kind==2){g.drawLine(16,5,16,9);g.drawLine(14,7,18,7);}}
            g.dispose();}
    }
}
