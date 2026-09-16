package bridging;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** Dialog Khanza. main() membuka pratinjau data fiktif tanpa database/API. */
public final class SatuSehatRujukanIGDRanap extends JDialog {
    private static final long serialVersionUID = 1L;
    private final SatuSehatRujukanIGDRanapPanel content;
    private Component formAnchor;
    private Window anchorWindow;
    private final ComponentAdapter followAnchor=new ComponentAdapter(){
        @Override public void componentMoved(ComponentEvent e){fitAnchor();}
        @Override public void componentResized(ComponentEvent e){fitAnchor();}
        @Override public void componentShown(ComponentEvent e){fitAnchor();}
    };
    public SatuSehatRujukanIGDRanap(Frame parent,boolean modal){this((Window)parent,modal,false);}
    public SatuSehatRujukanIGDRanap(Window parent,boolean modal){this(parent,modal,false);}
    private SatuSehatRujukanIGDRanap(Window parent,boolean modal,boolean preview){
        super(parent,modal?Dialog.ModalityType.APPLICATION_MODAL:Dialog.ModalityType.MODELESS);
        // Set before initComponents() packs the dialog. No title bar or resize/drag handle.
        setUndecorated(true);setResizable(false);getRootPane().setWindowDecorationStyle(JRootPane.NONE);initComponents();
        content=new SatuSehatRujukanIGDRanapPanel(preview?null:SatuSehatRujukanIGDRanapRepository.forKhanza(),preview);
        hostPanel.add(content,BorderLayout.CENTER);content.setOnClose(() -> dispose());
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){content.requestClose(() -> dispose());}});
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"closeDraft");
        getRootPane().getActionMap().put("closeDraft",new AbstractAction(){@Override public void actionPerformed(ActionEvent e){content.requestClose(() -> dispose());}});
        if(preview)content.showPreview(SatuSehatRujukanIGDRanapDraft.Jenis.IGD);else content.setOperator(currentOperator());
        Rectangle screen=getGraphicsConfiguration().getBounds();Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int w=Math.max(640,screen.width-insets.left-insets.right-24),h=Math.max(480,screen.height-insets.top-insets.bottom-24);
        setSize(Math.min(1320,w),Math.min(880,h));setLocationRelativeTo(parent);
        if(parent!=null)ikutiForm(parent);
    }
    /** Fill the caller's content area; do not depend on a fixed desktop resolution. */
    public void ikutiForm(final Component parent){
        if(!SwingUtilities.isEventDispatchThread()){SwingUtilities.invokeLater(() -> ikutiForm(parent));return;}
        detachAnchor();if(parent==null)return;
        formAnchor=parent instanceof RootPaneContainer?((RootPaneContainer)parent).getContentPane():parent;
        anchorWindow=parent instanceof Window?(Window)parent:SwingUtilities.getWindowAncestor(parent);
        formAnchor.addComponentListener(followAnchor);
        if(anchorWindow!=null&&anchorWindow!=formAnchor)anchorWindow.addComponentListener(followAnchor);
        fitAnchor();
    }
    private void fitAnchor(){
        if(formAnchor==null||!formAnchor.isShowing())return;
        Point point=formAnchor.getLocationOnScreen();
        if(formAnchor.getWidth()>0&&formAnchor.getHeight()>0)setBounds(point.x,point.y,formAnchor.getWidth(),formAnchor.getHeight());
    }
    private void detachAnchor(){
        if(formAnchor!=null)formAnchor.removeComponentListener(followAnchor);
        if(anchorWindow!=null&&anchorWindow!=formAnchor)anchorWindow.removeComponentListener(followAnchor);
        formAnchor=null;anchorWindow=null;
    }
    @Override public void setVisible(boolean visible){if(visible)fitAnchor();super.setVisible(visible);}
    @Override public void dispose(){detachAnchor();super.dispose();}
    private String currentOperator(){
        try{Object code=Class.forName("fungsi.akses").getMethod("getkode").invoke(null);return code==null?"":code.toString();}
        catch(ReflectiveOperationException ex){return "";}
    }
    /** Gunakan identitas sesi login Khanza, bukan input bebas operator. */
    public void setOperator(final String code){onEdt(() -> content.setOperator(code));}
    /** Panggil sebelum setNoRawat, atau gunakan bukaKunjungan untuk mengirim keduanya sekaligus. */
    public void setJenisRujukan(String jenis){final SatuSehatRujukanIGDRanapDraft.Jenis kind=SatuSehatRujukanIGDRanapDraft.Jenis.from(jenis);onEdt(() -> content.setJenis(kind));}
    /** Sudah memuat data secara asinkron; tidak perlu memanggil loadData lagi. */
    public void setNoRawat(final String rawat){onEdt(() -> content.loadVisit(rawat,content.getJenis()));}
    public void bukaKunjungan(final String rawat,String jenis){final SatuSehatRujukanIGDRanapDraft.Jenis kind=SatuSehatRujukanIGDRanapDraft.Jenis.from(jenis);onEdt(() -> content.loadVisit(rawat,kind));}
    private static void onEdt(Runnable task){if(SwingUtilities.isEventDispatchThread())task.run();else SwingUtilities.invokeLater(task);}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        hostPanel = new javax.swing.JPanel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Rujukan IGD dan Rawat Inap - RS Akademis Jaury");
        hostPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(hostPanel, java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel hostPanel;
    // End of variables declaration//GEN-END:variables
    public static void main(String[] args){
        if(GraphicsEnvironment.isHeadless()){System.err.println("Jalankan pratinjau pada komputer dengan desktop grafis.");return;}
        SwingUtilities.invokeLater(() -> new SatuSehatRujukanIGDRanap((Frame)null,false,true).setVisible(true));
    }
}
