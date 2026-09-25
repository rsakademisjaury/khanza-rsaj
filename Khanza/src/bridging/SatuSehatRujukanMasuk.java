package bridging;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** Form rujukan masuk mengikuti ukuran form pemanggil; detail tidak membuka dialog lain. */
public final class SatuSehatRujukanMasuk extends JDialog {
    private final SatuSehatRujukanMasukPanel content;
    private Component anchor;private Window anchorWindow;
    private final ComponentAdapter follow=new ComponentAdapter(){
        @Override public void componentMoved(ComponentEvent e){fit();}
        @Override public void componentResized(ComponentEvent e){fit();}
        @Override public void componentShown(ComponentEvent e){fit();}
    };
    public SatuSehatRujukanMasuk(Window owner,Component parent,SatuSehatRujukanMasukService service){
        super(owner,Dialog.ModalityType.MODELESS);setUndecorated(true);setResizable(false);setTitle("Rujukan Masuk IGD / Rawat Inap");setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        content=new SatuSehatRujukanMasukPanel(service);setContentPane(content);content.setOnClose(()->dispose());
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){content.requestClose();}});
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"closeInbox");
        getRootPane().getActionMap().put("closeInbox",new AbstractAction(){@Override public void actionPerformed(ActionEvent e){content.requestClose();}});
        anchor=parent instanceof RootPaneContainer?((RootPaneContainer)parent).getContentPane():parent;anchorWindow=parent instanceof Window?(Window)parent:SwingUtilities.getWindowAncestor(parent);
        if(anchor!=null)anchor.addComponentListener(follow);if(anchorWindow!=null&&anchorWindow!=anchor)anchorWindow.addComponentListener(follow);
        setSize(1200,800);setLocationRelativeTo(parent);fit();
    }
    private void fit(){if(anchor!=null&&anchor.isShowing()&&anchor.getWidth()>0&&anchor.getHeight()>0){Point p=anchor.getLocationOnScreen();setBounds(p.x,p.y,anchor.getWidth(),anchor.getHeight());}}
    @Override public void setVisible(boolean visible){if(visible)fit();super.setVisible(visible);}
    @Override public void dispose(){content.shutdown();if(anchor!=null)anchor.removeComponentListener(follow);if(anchorWindow!=null&&anchorWindow!=anchor)anchorWindow.removeComponentListener(follow);anchor=null;anchorWindow=null;super.dispose();}
}
