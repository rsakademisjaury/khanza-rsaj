package fungsi;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/** Auto panel khusus PA; fokus input/aksi dan dialog anak tetap dapat dipakai. */
public final class LabPAPanelFocus {
    private LabPAPanelFocus(){}
    public static void pasang(final Window owner,final JTable tabel,final JComponent panel,final Runnable pilih,final Runnable tutup){
        final KeyboardFocusManager manager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
        final boolean[] pending={false};
        final Runnable pilihNanti=()->{
            if(pending[0])return;pending[0]=true;
            SwingUtilities.invokeLater(()->{pending[0]=false;if(owner.isShowing()&&tabel.getSelectedRow()>=0&&tabel.isFocusOwner())pilih.run();});
        };
        final PropertyChangeListener focus=e->SwingUtilities.invokeLater(()->{
            if(!owner.isShowing())return;
            Component target=manager.getPermanentFocusOwner();if(target==null)return;
            if(diArea(target,tabel,panel))return;
            Window window=SwingUtilities.getWindowAncestor(target);
            if(window!=owner&&anak(window,owner))return;
            tutup.run();
        });
        final ListSelectionListener selection=e->{if(!e.getValueIsAdjusting()){
            if(tabel.getSelectedRow()<0)tutup.run();else pilihNanti.run();
        }};
        manager.addPropertyChangeListener("permanentFocusOwner",focus);
        tabel.getSelectionModel().addListSelectionListener(selection);
        tabel.addFocusListener(new FocusAdapter(){@Override public void focusGained(FocusEvent e){pilihNanti.run();}});
        owner.addWindowFocusListener(new WindowAdapter(){@Override public void windowLostFocus(WindowEvent e){if(!anak(e.getOppositeWindow(),owner))tutup.run();}});
        owner.addComponentListener(new ComponentAdapter(){@Override public void componentHidden(ComponentEvent e){tutup.run();}});
        owner.addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){
            manager.removePropertyChangeListener("permanentFocusOwner",focus);tabel.getSelectionModel().removeListSelectionListener(selection);
        }});
    }
    public static boolean diArea(Component target,Component tabel,Component panel){
        if(target==null)return false;
        if(target==tabel||target==panel||SwingUtilities.isDescendingFrom(target,tabel)||SwingUtilities.isDescendingFrom(target,panel))return true;
        for(Component p=target;p!=null;p=p.getParent())if(p instanceof JPopupMenu){
            Component invoker=((JPopupMenu)p).getInvoker();
            if(invoker!=null&&(invoker==tabel||invoker==panel||SwingUtilities.isDescendingFrom(invoker,tabel)||SwingUtilities.isDescendingFrom(invoker,panel)))return true;
        }
        return false;
    }
    private static boolean anak(Window window,Window owner){
        for(Window w=window;w!=null;w=w.getOwner())if(w==owner)return true;
        return false;
    }
}
