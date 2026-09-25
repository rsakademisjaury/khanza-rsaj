package bridging;

import java.awt.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import javax.swing.*;

/** Tombol inbox tanpa judul, badge dan toast. Tidak mengubah notifier konsul/ranap yang lain. */
public final class SatuSehatRujukanMasukNotifier {
    private final Window owner;private final Component anchor;private final BooleanSupplier allowed;
    private final BiConsumer<String,Runnable> toast;private final InboxButton button=new InboxButton();
    private final SatuSehatRujukanMasukService service=new SatuSehatRujukanMasukService();
    private SatuSehatRujukanMasuk dialog;private boolean active;
    public SatuSehatRujukanMasukNotifier(Window owner,Component anchor,BooleanSupplier allowed,BiConsumer<String,Runnable> toast){
        this.owner=owner;this.anchor=anchor;this.allowed=allowed;this.toast=toast;
        button.setName("BtnRujukanMasukSatuSehat");button.setText("");button.setToolTipText("Rujukan masuk IGD / Rawat Inap");button.getAccessibleContext().setAccessibleName("Rujukan masuk IGD dan rawat inap");
        button.setEnabled(false);button.addActionListener(e->open());service.addListener((snapshot,error,fresh,first)->changed(snapshot,error,fresh,first));
    }
    public JButton button(){return button;}
    public void start(String operator){stop();if(!allowed.getAsBoolean())return;active=true;button.setEnabled(true);service.start(operator);}
    public void stop(){active=false;service.stop();button.count=0;button.repaint();button.setEnabled(false);button.setToolTipText("Rujukan masuk IGD / Rawat Inap");if(dialog!=null){dialog.dispose();dialog=null;}}
    private void changed(SatuSehatRujukanMasukService.Snapshot snapshot,String error,List<SatuSehatRujukanMasukService.Row> fresh,boolean first){
        if(!active||!allowed.getAsBoolean())return;
        if(snapshot!=null){button.count=snapshot.pending;button.repaint();button.getAccessibleContext().setAccessibleDescription(snapshot.pending+" rujukan menunggu keputusan");}
        if(error!=null&&!error.isEmpty()){button.setToolTipText(error);return;}
        button.setToolTipText(button.count+" rujukan menunggu keputusan • pemeriksaan setiap "+SatuSehatRujukanMasukService.pollSeconds()+" detik");
        if(fresh.isEmpty())return;
        Map<String,Integer> groups=new LinkedHashMap<String,Integer>();
        for(SatuSehatRujukanMasukService.Row r:fresh){String key=("Belum diketahui".equals(r.kind)?"Rujukan masuk":"Rujukan "+("RANAP".equals(r.kind)?"Ranap":r.kind))+" dari "+r.referrer;groups.put(key,groups.containsKey(key)?groups.get(key)+1:1);}
        StringBuilder text=new StringBuilder();int i=0;for(Map.Entry<String,Integer> g:groups.entrySet()){if(i++==4){text.append("\nDan rujukan baru lainnya. Buka daftar untuk melihat semuanya.");break;}if(text.length()>0)text.append('\n');text.append("Ada ").append(g.getValue()).append(' ').append(g.getKey());}
        toast.accept(text.toString(),()->open());
    }
    public void open(){
        if(!active||!allowed.getAsBoolean())return;
        if(dialog!=null&&dialog.isDisplayable()){dialog.toFront();dialog.requestFocus();return;}
        SatuSehatRujukanMasuk opened=new SatuSehatRujukanMasuk(owner,anchor,service);dialog=opened;
        opened.addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){if(dialog==opened)dialog=null;if(active)service.refresh();}});
        opened.setVisible(true);
    }
    private static final class InboxButton extends JButton {
        private int count;private final Icon inbox;
        InboxButton(){
            java.net.URL url=SatuSehatRujukanMasukNotifier.class.getResource("/picture/inbox.png");
            inbox=url==null?null:new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(22,22,Image.SCALE_SMOOTH));
            Dimension size=new Dimension(38,27);setPreferredSize(size);setMinimumSize(size);setMaximumSize(size);setMargin(new Insets(0,0,0,0));setFocusable(false);setFocusPainted(false);setBorderPainted(false);setContentAreaFilled(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(!isEnabled())g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.4f));
            if(inbox!=null)inbox.paintIcon(this,g,2,(getHeight()-22)/2);else{g.setColor(new Color(10,112,182));g.setStroke(new BasicStroke(1.6f));g.drawRoundRect(3,5,19,17,4,4);g.drawLine(3,15,8,15);g.drawLine(8,15,10,18);g.drawLine(10,18,15,18);g.drawLine(15,18,17,15);g.drawLine(17,15,22,15);}
            if(count>0){String text=count>99?"99+":String.valueOf(count);g.setFont(new Font("Segoe UI Semibold",Font.BOLD,10));FontMetrics fm=g.getFontMetrics();int w=Math.max(15,fm.stringWidth(text)+6),x=getWidth()-w;g.setColor(new Color(209,48,52));g.fillRoundRect(x,0,w,15,15,15);g.setColor(Color.WHITE);g.drawString(text,x+(w-fm.stringWidth(text))/2,11);}
            g.dispose();
        }
    }
}
