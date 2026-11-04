package simrskhanza;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import javax.swing.text.View;

/** Tampilan lokal Readmisi: tidak mengganti UIManager atau look and feel SIMRS. */
final class ReadmisiUi {
    static final Color BLUE=new Color(28,104,211), INK=new Color(29,45,68), MUTED=new Color(109,125,147);
    static final Color LINE=new Color(222,230,241), BG=new Color(245,248,252), SOFT=new Color(237,245,255);
    private static final boolean HAS_SEMIBOLD=java.util.Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains("Segoe UI Semibold");
    static Font font(float size,boolean semibold) {
        return new Font(semibold?"Segoe UI Semibold":"Segoe UI",semibold && !HAS_SEMIBOLD?Font.BOLD:Font.PLAIN,Math.round(size)).deriveFont(size);
    }
    static Icon icon(String name,int limit) {
        URL url=ReadmisiUi.class.getResource("/picture/"+name);
        if(url==null) return null;
        ImageIcon original=new ImageIcon(url);
        int w=original.getIconWidth(),h=original.getIconHeight();
        if(w<=limit && h<=limit) return original; // PNG kecil tidak diperbesar.
        double scale=Math.min((double)limit/w,(double)limit/h);
        return new ImageIcon(original.getImage().getScaledInstance(Math.max(1,(int)(w*scale)),Math.max(1,(int)(h*scale)),Image.SCALE_SMOOTH));
    }
    static JLabel label(String text,float size,boolean bold,Color color) {
        JLabel l=new JLabel(text);l.setFont(font(size,bold));l.setForeground(color);return l;
    }
    static JPanel clear(LayoutManager layout) { JPanel p=new JPanel(layout);p.setOpaque(false);return p; }
    static class Card extends JPanel {
        private static final long serialVersionUID=1L;
        private final Color fill; private final boolean shadow;
        Card(LayoutManager layout,Color fill,boolean shadow) {
            super(layout);this.fill=fill;this.shadow=shadow;setOpaque(false);
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g=(Graphics2D)graphics.create();antialias(g);
            if(shadow) {g.setColor(new Color(31,60,105,9));g.fillRoundRect(0,3,getWidth()-1,getHeight()-4,19,19);}
            g.setColor(fill);g.fillRoundRect(0,0,getWidth()-1,getHeight()-(shadow?4:1),18,18);
            g.setColor(LINE);g.drawRoundRect(0,0,getWidth()-1,getHeight()-(shadow?4:1),18,18);g.dispose();
        }
    }
    static Card card(LayoutManager layout) {
        Card p=new Card(layout,Color.WHITE,true);p.setBorder(BorderFactory.createEmptyBorder(14,16,16,16));return p;
    }
    static JPanel heading(String title,String png,String detail) {
        JPanel row=clear(new BorderLayout(8,0));JLabel h=label(title,14,true,INK);
        h.setIcon(icon(png,18));h.setIconTextGap(8);row.add(h,BorderLayout.WEST);
        if(detail!=null) row.add(label(detail,11,false,MUTED),BorderLayout.EAST);
        row.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));return row;
    }
    static JComponent pill(String text) {
        Card c=new Card(new FlowLayout(FlowLayout.CENTER,8,5),SOFT,false);
        JLabel l=label(text,11,true,BLUE);c.add(l);return c;
    }
    static JComponent field(String title,JComponent control) {
        JPanel p=clear(new BorderLayout(0,5));p.add(label(title,11.5f,true,MUTED),BorderLayout.NORTH);
        p.add(control,BorderLayout.CENTER);return p;
    }
    static JPanel pair(JComponent a,JComponent b,double left) {
        JPanel p=clear(new GridBagLayout());GridBagConstraints g=new GridBagConstraints();
        g.fill=GridBagConstraints.BOTH;g.weightx=left;g.gridx=0;g.insets=new Insets(0,0,0,10);p.add(a,g);
        g.gridx=1;g.weightx=1-left;g.insets=new Insets(0,0,0,0);p.add(b,g);return p;
    }
    static JTextField searchField() {
        JTextField f=new JTextField(14) {
            private static final long serialVersionUID=1L;
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if(getText().isEmpty() && !hasFocus()) {
                    Graphics2D g=(Graphics2D)graphics.create();antialias(g);g.setFont(getFont());g.setColor(MUTED);
                    g.drawString("Ketik RM atau nama...",getInsets().left+1,(getHeight()-g.getFontMetrics().getHeight())/2+g.getFontMetrics().getAscent());g.dispose();
                }
            }
        };return f;
    }
    static JTextField readonly() {
        JTextField f=new JTextField();f.setEditable(false);styleField(f,true);return f;
    }
    static void styleField(JTextField f,boolean readonly) {
        f.setFont(font(12.5f,false));f.setForeground(INK);f.setCaretColor(BLUE);
        f.setOpaque(false);f.setUI(new BasicTextFieldUI() {
            @Override protected void paintSafely(Graphics g) { fillField(g,getComponent(),!getComponent().isEditable());super.paintSafely(g); }
        });
        f.setBorder(new FocusBorder(f,readonly));f.setPreferredSize(new Dimension(150,36));
        f.setMinimumSize(new Dimension(50,36));focusRepaint(f,f);
        f.addMouseMotionListener(new MouseMotionAdapter(){@Override public void mouseMoved(MouseEvent e){f.setToolTipText(f.getText());}});
    }
    static void fillField(Graphics graphics,JComponent c,boolean readonly) {
        Graphics2D g=(Graphics2D)graphics.create();antialias(g);
        g.setColor(readonly?new Color(247,249,253):Color.WHITE);
        g.fillRoundRect(1,1,c.getWidth()-2,c.getHeight()-2,11,11);g.dispose();
    }
    private static final class FocusBorder extends AbstractBorder {
        private static final long serialVersionUID=1L;
        private final JComponent target;private final boolean readonly;
        FocusBorder(JComponent target,boolean readonly){this.target=target;this.readonly=readonly;}
        @Override public Insets getBorderInsets(Component c){return new Insets(7,10,7,10);}
        @Override public Insets getBorderInsets(Component c,Insets i){i.set(7,10,7,10);return i;}
        @Override public void paintBorder(Component c,Graphics graphics,int x,int y,int w,int h){
            Graphics2D g=(Graphics2D)graphics.create();antialias(g);
            Component focus=KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            boolean active=focus==target || (focus!=null && SwingUtilities.isDescendingFrom(focus,target));
            if(active){g.setColor(new Color(28,104,211,28));g.setStroke(new BasicStroke(4));g.drawRoundRect(x+2,y+2,w-5,h-5,12,12);}
            boolean locked=target instanceof javax.swing.text.JTextComponent ? !((javax.swing.text.JTextComponent)target).isEditable() : readonly;
            g.setColor(active?BLUE:locked?new Color(226,233,243):new Color(207,218,233));
            g.setStroke(new BasicStroke(active?1.7f:1));g.drawRoundRect(x+2,y+2,w-5,h-5,10,10);g.dispose();
        }
    }
    static void focusRepaint(JComponent focus,JComponent paint){
        focus.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e){
                paint.repaint();
                if(focus instanceof javax.swing.text.JTextComponent)
                    SwingUtilities.invokeLater(()->{
                        if(focus.getParent() instanceof JComponent){
                            JComponent parent=(JComponent)focus.getParent();
                            Rectangle r=SwingUtilities.convertRectangle(focus,new Rectangle(0,0,focus.getWidth(),Math.min(80,focus.getHeight())),parent);
                            parent.scrollRectToVisible(r);
                        }
                    });
            }
            @Override public void focusLost(FocusEvent e){paint.repaint();}
        });
    }
    static JButton button(String text,String png,boolean primary){
        JButton b=new JButton(text){
            private static final long serialVersionUID=1L;
            @Override protected void paintComponent(Graphics graphics){
                Graphics2D g=(Graphics2D)graphics.create();antialias(g);
                Color fill=!isEnabled()?new Color(242,245,249):primary?(getModel().isPressed()?new Color(17,80,175):getModel().isRollover()?new Color(43,119,227):BLUE)
                        :getModel().isRollover()?SOFT:Color.WHITE;
                g.setColor(fill);g.fillRoundRect(1,1,getWidth()-3,getHeight()-3,32,32);
                g.setColor(primary&&isEnabled()?fill:LINE);g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,32,32);
                if(hasFocus()){g.setColor(new Color(122,174,245));g.setStroke(new BasicStroke(2));g.drawRoundRect(3,3,getWidth()-7,getHeight()-7,28,28);}
                g.dispose();super.paintComponent(graphics);
            }
        };
        b.setUI(new BasicButtonUI());b.setFont(font(12,true));b.setForeground(primary?Color.WHITE:INK);
        b.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));b.setContentAreaFilled(false);b.setOpaque(false);
        b.setFocusPainted(false);b.setRolloverEnabled(true);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setIcon(icon(png,16));b.setIconTextGap(7);focusRepaint(b,b);return b;
    }
    static void combo(JComboBox<String> combo) {
        combo.setFont(font(12,false));combo.setForeground(INK);combo.setBackground(Color.WHITE);
        combo.setUI(new BasicComboBoxUI(){
            @Override protected JButton createArrowButton(){return arrow(false);}
            @Override public void paintCurrentValueBackground(Graphics g,Rectangle b,boolean focus){ /* latar membulat digambar oleh paint */ }
            @Override public void paint(Graphics g,JComponent c){fillField(g,c,false);super.paint(g,c);}
        });
        combo.setBorder(new FocusBorder(combo,false));combo.setOpaque(false);combo.setPreferredSize(new Dimension(150,36));
        combo.setMinimumSize(new Dimension(70,36));
        combo.setRenderer(new DefaultListCellRenderer(){
            private static final long serialVersionUID=1L;
            @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){
                JLabel c=(JLabel)super.getListCellRendererComponent(l,v,i,s,f);c.setFont(font(12,false));
                c.setBorder(BorderFactory.createEmptyBorder(i<0?0:7,4,i<0?0:7,4));
                c.setBackground(s?SOFT:Color.WHITE);c.setForeground(s?BLUE:INK);c.setOpaque(i>=0);return c;
            }
        });focusRepaint(combo,combo);
    }
    private static JButton arrow(boolean up){
        JButton a=new JButton(){
            private static final long serialVersionUID=1L;
            @Override protected void paintComponent(Graphics graphics){
                Graphics2D g=(Graphics2D)graphics.create();antialias(g);g.setColor(isEnabled()?MUTED:LINE);g.setStroke(new BasicStroke(1.6f));
                int x=getWidth()/2,y=getHeight()/2;g.drawLine(x-3,y+(up?2:-2),x,y+(up?-1:1));g.drawLine(x,y+(up?-1:1),x+3,y+(up?2:-2));g.dispose();
            }
        };
        a.setUI(new BasicButtonUI());a.setOpaque(false);a.setContentAreaFilled(false);a.setBorder(null);a.setFocusable(false);a.setPreferredSize(new Dimension(20,13));return a;
    }
    static void spinner(JSpinner spinner){
        spinner.setUI(new BasicSpinnerUI(){
            @Override protected Component createNextButton(){JButton b=arrow(true);installNextButtonListeners(b);return b;}
            @Override protected Component createPreviousButton(){JButton b=arrow(false);installPreviousButtonListeners(b);return b;}
        });
        spinner.setEditor(new JSpinner.NumberEditor(spinner,"0"));
        spinner.setOpaque(false);spinner.setBorder(new FocusBorder(spinner,false));spinner.setPreferredSize(new Dimension(84,36));
        spinner.setMinimumSize(new Dimension(76,36));
        JFormattedTextField f=((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        f.setUI(new BasicFormattedTextFieldUI());f.setFont(font(12,false));f.setForeground(INK);f.setBorder(null);f.setOpaque(false);
        f.setHorizontalAlignment(JTextField.LEFT);focusRepaint(f,spinner);spinner.getEditor().setOpaque(false);
    }
    static void tabs(JTabbedPane tabs){
        tabs.setFont(font(12,true));tabs.setForeground(MUTED);tabs.setOpaque(false);
        tabs.setUI(new BasicTabbedPaneUI(){
            @Override protected void installDefaults(){super.installDefaults();tabInsets=new Insets(10,16,10,16);contentBorderInsets=new Insets(9,0,0,0);tabAreaInsets=new Insets(0,0,0,0);selectedTabPadInsets=new Insets(0,0,0,0);}
            @Override protected void paintTabBorder(Graphics g,int place,int i,int x,int y,int w,int h,boolean selected){}
            @Override protected void paintFocusIndicator(Graphics g,int place,Rectangle[] rs,int i,Rectangle ir,Rectangle tr,boolean sel){}
            @Override protected void paintContentBorder(Graphics g,int place,int selected){}
            @Override protected void paintTabBackground(Graphics graphics,int place,int i,int x,int y,int w,int h,boolean selected){
                Graphics2D g=(Graphics2D)graphics.create();antialias(g);g.setColor(selected?Color.WHITE:new Color(0,0,0,0));
                if(selected){g.fillRoundRect(x,y,w-5,h-2,14,14);g.setColor(LINE);g.drawRoundRect(x,y,w-5,h-2,14,14);g.setColor(BLUE);g.fillRoundRect(x+17,y+h-5,w-39,3,3,3);}
                g.dispose();
            }
            @Override protected void paintText(Graphics g,int place,Font font,FontMetrics metrics,int i,String title,Rectangle text,boolean selected){
                g.setFont(ReadmisiUi.font(12,true));g.setColor(selected?BLUE:MUTED);g.drawString(title,text.x,text.y+metrics.getAscent());
            }
        });
    }
    static JTextArea area(){JTextArea a=new JTextArea();a.setUI(new BasicTextAreaUI());a.setBorder(null);a.setFont(font(13,false));a.setLineWrap(true);a.setWrapStyleWord(true);a.setForeground(INK);a.setCaretColor(BLUE);a.setMargin(new Insets(0,0,0,0));return a;}
    /** Satu scroll pada halaman; tinggi textbox mengikuti baris aktual. */
    static final class GrowingArea extends JPanel {
        private static final long serialVersionUID=1L;
        final JTextArea area;private boolean pending;
        GrowingArea(JTextArea area,boolean readonly){
            super(new BorderLayout());this.area=area;setOpaque(false);setBorder(new FocusBorder(area,readonly));
            area.setOpaque(false);area.setEditable(!readonly);add(area,BorderLayout.CENTER);focusRepaint(area,this);
            area.getDocument().addDocumentListener(new DocumentListener(){
                @Override public void insertUpdate(DocumentEvent e){schedule();}
                @Override public void removeUpdate(DocumentEvent e){schedule();}
                @Override public void changedUpdate(DocumentEvent e){schedule();}
            });
            addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent e){schedule();}});
            setAlignmentX(Component.LEFT_ALIGNMENT);setMinimumSize(new Dimension(20,40));
        }
        void schedule(){if(pending)return;pending=true;SwingUtilities.invokeLater(()->{pending=false;revalidate();repaint();});}
        @Override public Dimension getPreferredSize(){
            Insets i=getInsets();int width=Math.max(70,getWidth()-i.left-i.right);
            View v=area.getUI().getRootView(area);v.setSize(width,Integer.MAX_VALUE);
            int height=(int)Math.ceil(v.getPreferredSpan(View.Y_AXIS))+i.top+i.bottom;
            return new Dimension(150,Math.max(40,height));
        }
        @Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}
        @Override protected void paintComponent(Graphics g){fillField(g,this,!area.isEditable());}
        @Override protected void paintChildren(Graphics graphics) {
            super.paintChildren(graphics);
            Object hint=area.getClientProperty("readmisi.placeholder");
            if(hint!=null && area.getText().isEmpty() && area.isEditable() && !area.hasFocus()) {
                Graphics2D g=(Graphics2D)graphics.create();antialias(g);g.setFont(area.getFont());g.setColor(MUTED);
                g.drawString(hint.toString(),getInsets().left,getInsets().top+g.getFontMetrics().getAscent());g.dispose();
            }
        }
    }
    static JScrollPane scroll(Component view){
        JScrollPane s=new JScrollPane(view);s.setBorder(null);s.setOpaque(false);s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(24);styleScrollbar(s.getVerticalScrollBar());styleScrollbar(s.getHorizontalScrollBar());return s;
    }
    private static void styleScrollbar(JScrollBar s){
        s.setPreferredSize(new Dimension(9,9));s.setOpaque(false);
        s.setUI(new BasicScrollBarUI(){
            @Override protected JButton createDecreaseButton(int dir){return zero();}
            @Override protected JButton createIncreaseButton(int dir){return zero();}
            private JButton zero(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
            @Override protected void paintTrack(Graphics g,JComponent c,Rectangle r){}
            @Override protected void paintThumb(Graphics graphics,JComponent c,Rectangle r){
                Graphics2D g=(Graphics2D)graphics.create();antialias(g);g.setColor(new Color(198,210,226));g.fillRoundRect(r.x+1,r.y+1,Math.max(3,r.width-2),Math.max(3,r.height-2),7,7);g.dispose();
            }
        });
    }
    static final class ModernTable extends JTable {
        private static final long serialVersionUID=1L;
        ModernTable(TableModel model){super(model);}
        @Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);if(getRowCount()!=0)return;
            Graphics2D g=(Graphics2D)graphics.create();antialias(g);int y=Math.max(80,getHeight()/2-30);
            Icon i=icon("report24.png",24);if(i!=null)i.paintIcon(this,g,(getWidth()-i.getIconWidth())/2,y-35);
            String t="Belum ada catatan readmisi";g.setFont(font(15,true));g.setColor(INK);g.drawString(t,(getWidth()-g.getFontMetrics().stringWidth(t))/2,y+14);
            t="Pilih periode dan ruangan, lalu klik Tampilkan.";g.setFont(font(12,false));g.setColor(MUTED);g.drawString(t,(getWidth()-g.getFontMetrics().stringWidth(t))/2,y+39);g.dispose();
        }
    }
    static void table(JTable table){
        table.setUI(new BasicTableUI());table.getTableHeader().setUI(new BasicTableHeaderUI());
        table.setFont(font(12,false));table.setForeground(INK);table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);table.setShowGrid(false);table.setIntercellSpacing(new Dimension(0,0));
        table.setRowHeight(48);table.setSelectionBackground(SOFT);table.setSelectionForeground(BLUE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(100,42));
        table.getTableHeader().setDefaultRenderer((t,value,selected,focus,row,col)->{
            JTextArea h=area();h.setFont(font(11.5f,true));h.setForeground(MUTED);h.setBackground(new Color(247,249,253));
            h.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,LINE),BorderFactory.createEmptyBorder(8,col==0?4:10,6,col==0?4:10)));
            h.setText(String.valueOf(value).replace("Tgl dan Jam ","Tgl & jam\n"));return h;
        });
        table.setDefaultRenderer(Object.class,new JTextAreaRenderer());
        final boolean[] queued={false};Runnable fit=()->{
            if(queued[0])return;queued[0]=true;SwingUtilities.invokeLater(()->{
                queued[0]=false;
                for(int row=0;row<table.getRowCount();row++){
                    int height=48;
                    for(int col=0;col<table.getColumnCount();col++){
                        Component c=table.prepareRenderer(table.getCellRenderer(row,col),row,col);
                        c.setSize(Math.max(30,table.getColumnModel().getColumn(col).getWidth()),Short.MAX_VALUE);
                        height=Math.max(height,c.getPreferredSize().height);
                    }
                    if(table.getRowHeight(row)!=height)table.setRowHeight(row,height);
                }
                table.repaint();
            });
        };
        Timer timer=new Timer(100,e->fit.run());timer.setRepeats(false);
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener(){
            @Override public void columnMarginChanged(ChangeEvent e){timer.restart();}
            @Override public void columnAdded(TableColumnModelEvent e){}
            @Override public void columnRemoved(TableColumnModelEvent e){}
            @Override public void columnMoved(TableColumnModelEvent e){}
            @Override public void columnSelectionChanged(ListSelectionEvent e){}
        });table.getModel().addTableModelListener(e->fit.run());
    }
    private static final class JTextAreaRenderer extends JTextArea implements TableCellRenderer{
        private static final long serialVersionUID=1L;
        JTextAreaRenderer(){setUI(new BasicTextAreaUI());setLineWrap(true);setWrapStyleWord(true);setFont(font(12,false));setOpaque(true);}
        @Override public Component getTableCellRendererComponent(JTable t,Object value,boolean selected,boolean focus,int row,int col){
            setText(value==null?"":String.valueOf(value));setForeground(selected?BLUE:col==1?BLUE:INK);
            setBackground(selected?SOFT:row%2==0?Color.WHITE:new Color(250,252,255));
            setFont(font(12,col==1));
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(238,242,248)),BorderFactory.createEmptyBorder(10,10,10,10)));
            return this;
        }
    }
    static void antialias(Graphics2D g){g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);}
    private ReadmisiUi(){}
}
