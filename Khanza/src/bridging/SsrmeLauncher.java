package bridging;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.beans.Beans;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Satu alur SSRME untuk seluruh form. Tidak melakukan query saat form/tabel dimuat. */
public final class SsrmeLauncher {
    private static final Color BLUE = new Color(35, 99, 190), SOFT = new Color(235, 244, 255), INK = new Color(36, 53, 75);
    private static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 12), BOLD = new Font("Segoe UI Semibold", Font.PLAIN, 12);
    private static final Map<Component, JDialog> OPEN = new WeakHashMap<Component, JDialog>();
    private SsrmeLauncher() { }

    /** Strip kecil sesudah identitas pasien; tidak menambah isi toolbar/filter lama. */
    public static void pasang(Component owner, Container root, Supplier<SsrmeClient.Context> source, boolean admission, boolean emergency) {
        if (Beans.isDesignTime() || !(root.getLayout() instanceof BorderLayout)) return;
        for (Component child : root.getComponents()) if ("rsaj.ssrme.header".equals(child.getName())) return;
        BorderLayout layout = (BorderLayout) root.getLayout();
        Component old = layout.getLayoutComponent(root, BorderLayout.NORTH);
        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setName("rsaj.ssrme.header"); wrapper.setOpaque(false);
        if (old != null) { root.remove(old); wrapper.add(old, BorderLayout.CENTER); }
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); strip.setBackground(Color.WHITE);
        strip.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(228, 235, 244)));
        JButton button = button(admission ? "Persetujuan SSRME" : "SSRME · RME Nasional");
        button.setToolTipText(admission ? "Persetujuan akses riwayat nasional untuk pasien terpilih" : "Buka riwayat rekam medis nasional pasien terpilih");
        button.addActionListener(e -> buka(owner, source, admission, emergency));
        strip.add(button);
        JLabel note = new JLabel(admission ? "Persetujuan pasien melalui SATUSEHAT Mobile" : "Riwayat pasien lintas fasilitas kesehatan");
        note.setFont(NORMAL); note.setForeground(INK); strip.add(note);
        wrapper.add(strip, BorderLayout.SOUTH); root.add(wrapper, BorderLayout.PAGE_START); root.revalidate();
    }

    /** Kolom MODEL: aman terhadap sorter, pemindahan kolom dan frozen columns kasir. */
    public static SsrmeClient.Context dariTabel(JTable table, int visitColumn, int rmColumn) {
        int row = table == null ? -1 : table.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException("Pilih pasien pada tabel terlebih dahulu.");
        int modelRow = table.convertRowIndexToModel(row);
        Object visit = table.getModel().getValueAt(modelRow, visitColumn);
        Object rm = table.getModel().getValueAt(modelRow, rmColumn);
        return new SsrmeClient.Context(visit == null ? "" : visit.toString(), rm == null ? "" : rm.toString());
    }

    private static void buka(Component owner, Supplier<SsrmeClient.Context> source, boolean admission, boolean allowEmergency) {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> buka(owner, source, admission, allowEmergency)); return; }
        JDialog opened = OPEN.get(owner);
        if (opened != null && opened.isDisplayable()) { opened.toFront(); return; }
        final String actor = SsrmeClient.text(akses.getkode());
        final long epoch = koneksiDB.versiSesiDatabase();
        SsrmeClient.Context context;
        try { context = source.get(); }
        catch (Exception e) { javax.swing.JOptionPane.showMessageDialog(owner, "Pilih pasien pada tabel terlebih dahulu.", "SSRME", javax.swing.JOptionPane.INFORMATION_MESSAGE); return; }
        if (actor.isEmpty() || koneksiDB.sesiDatabaseTerkunci()) {
            javax.swing.JOptionPane.showMessageDialog(owner, "Silakan buka kembali sesi login SIMRS.", "SSRME", javax.swing.JOptionPane.INFORMATION_MESSAGE); return;
        }
        Window parent = owner instanceof Window ? (Window) owner : SwingUtilities.getWindowAncestor(owner);
        final SsrmeClient.Context capturedContext = context;
        java.util.function.BooleanSupplier selectedPatient = () -> {
            try {
                SsrmeClient.Context current = source.get();
                return capturedContext.noRm.equals(current.noRm) && capturedContext.noRawat.equals(current.noRawat);
            } catch (Exception e) { return false; }
        };
        Screen screen = new Screen(parent, actor, epoch, admission, allowEmergency, selectedPatient);
        OPEN.put(owner, screen);
        screen.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { if (OPEN.get(owner)==screen) OPEN.remove(owner); }
        });
        screen.load(context); screen.setVisible(true);
    }

    enum AccessState { CHECKING, REQUIRED, GRANTED, ERROR, NO_DOCTOR }
    static AccessState failureState(Throwable cause) {
        if (cause instanceof ExecutionException && cause.getCause()!=null) cause=cause.getCause();
        return cause instanceof SsrmeClient.Failure && "CONSENT_REQUIRED".equals(((SsrmeClient.Failure)cause).code)
                ? AccessState.REQUIRED : AccessState.ERROR;
    }
    private static final class Screen extends JDialog {
        private final String actor;
        private final long epoch;
        private final boolean allowEmergency;
        private final java.util.function.BooleanSupplier selectedPatient;
        private final javax.swing.Timer sessionGuard;
        private final SsrmeTrace trace=new SsrmeTrace();
        private final SsrmeClient client=new SsrmeClient(trace::add);
        private final JLabel patient=label("Memuat identitas pasien...",true),visit=label(" ",false),doctor=label("Dokter: memeriksa akun login...",true);
        private final JTextArea status=area("Memeriksa konfigurasi dan hak akses SSRME...");
        private final JButton view=button("Lihat RME Nasional"),consent=button("Persetujuan SSRME"),emergency=button("Akses Emergency"),retry=button("Cek Akses Lagi");
        private SsrmeClient.Setup setup;
        private AccessState state=AccessState.CHECKING;
        private boolean busy=true;
        Screen(Window parent,String actor,long epoch,boolean admission,boolean allowEmergency,java.util.function.BooleanSupplier selectedPatient) {
            super(parent,"SATUSEHAT · Rekam Medis Nasional",ModalityType.APPLICATION_MODAL);
            this.actor=actor;this.epoch=epoch;this.allowEmergency=allowEmergency;this.selectedPatient=selectedPatient;
            sessionGuard=new javax.swing.Timer(400,e->{if(!sameSession()||!selectedPatient.getAsBoolean())dispose();});sessionGuard.start();
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel body=new JPanel(new BorderLayout(0,14));body.setBackground(Color.WHITE);body.setBorder(BorderFactory.createEmptyBorder(18,22,16,22));setContentPane(body);
            JPanel head=new JPanel(new java.awt.GridLayout(0,1,0,8));head.setOpaque(false);
            JLabel title=label("SATUSEHAT RME",true);title.setFont(new Font("Segoe UI Semibold",Font.PLAIN,21));title.setForeground(BLUE);
            head.add(title);head.add(patient);head.add(visit);head.add(doctor);body.add(head,BorderLayout.NORTH);
            JPanel middle=new JPanel(new BorderLayout(0,10));middle.setOpaque(false);
            JTextArea note=area("Persetujuan dan akses RME mengikuti pasien serta dokter yang sedang login. Status diperiksa langsung ke SATUSEHAT. Masukkan kode pasien pada halaman persetujuan resmi bila diminta.");
            note.setBackground(SOFT);note.setOpaque(true);note.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            middle.add(note,BorderLayout.NORTH);middle.add(status,BorderLayout.CENTER);body.add(middle,BorderLayout.CENTER);
            JPanel footer=new JPanel(new BorderLayout());footer.setOpaque(false);footer.add(trace.button(this),BorderLayout.WEST);
            JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);
            JButton close=button("Tutup");close.addActionListener(e->dispose());
            actions.add(retry);actions.add(consent);actions.add(emergency);actions.add(view);actions.add(close);footer.add(actions,BorderLayout.EAST);body.add(footer,BorderLayout.SOUTH);
            consent.addActionListener(e->flow("CONSENT"));view.addActionListener(e->flow("VIEW"));emergency.addActionListener(e->flow("EMERGENCY"));retry.addActionListener(e->flow("VIEW"));
            enableActions();SsrmeUi.fit(this,parent);
        }
        private boolean sameSession(){return actor.equals(SsrmeClient.text(akses.getkode()))&&epoch==koneksiDB.versiSesiDatabase()&&!koneksiDB.sesiDatabaseTerkunci();}
        @Override public void dispose(){sessionGuard.stop();trace.clear();super.dispose();}
        void load(SsrmeClient.Context context){
            new SwingWorker<SsrmeClient.Setup,Void>(){
                @Override protected SsrmeClient.Setup doInBackground()throws Exception{try(Connection c=koneksiDB.condbTransaksi()){return client.load(c,actor,context);}}
                @Override protected void done(){
                    if(!isDisplayable())return;busy=false;if(!sameSession()){dispose();return;}
                    try{
                        setup=get();patient.setText(setup.patientName+" · No. RM "+setup.patientRm);
                        visit.setText("No. rawat: "+(setup.visit.isEmpty()?"riwayat pasien":setup.visit)+" | Operator: "+actor);
                        SsrmeClient.Doctor own=null;for(SsrmeClient.Doctor d:setup.doctors)if(d.code.equals(setup.actorDoctor))own=d;
                        if(!setup.view||own==null){state=AccessState.NO_DOCTOR;doctor.setText("Dokter: akun ini bukan akun dokter aktif");message("Gunakan akun dokter aktif untuk persetujuan dan RME Nasional. Identitas dokter lain tidak dipilih secara otomatis.",true);enableActions();return;}
                        doctor.setText("Dokter: "+own.name+" ("+own.code+")");flow("VIEW");
                    }catch(Exception e){state=AccessState.ERROR;message(error(e),true);enableActions();}
                }
            }.execute();
        }
        private void enableActions(){
            boolean ready=!busy&&setup!=null&&setup.view&&sameSession();
            view.setVisible(state==AccessState.GRANTED);view.setEnabled(ready);
            consent.setVisible(state==AccessState.REQUIRED);consent.setEnabled(ready&&setup.consent);
            emergency.setVisible(state==AccessState.REQUIRED&&allowEmergency&&setup!=null&&setup.emergency);emergency.setEnabled(ready);
            retry.setVisible(state==AccessState.ERROR);retry.setEnabled(!busy&&setup!=null&&sameSession());
            revalidate();repaint();
        }
        private void flow(final String action){
            if(busy||setup==null||!setup.view||!sameSession())return;
            final SsrmeClient.Setup captured=setup;busy=true;state=AccessState.CHECKING;enableActions();
            message("VIEW".equals(action)?"Memeriksa izin akses RME Nasional...":"Meminta halaman persetujuan SSRME...",false);
            new SwingWorker<SsrmeClient.Link,Void>(){
                @Override protected SsrmeClient.Link doInBackground()throws Exception{return requestLink(captured,action);}
                @Override protected void done(){
                    if(!isDisplayable())return;busy=false;if(!sameSession()){dispose();return;}
                    try{
                        SsrmeClient.Link result=get();state="VIEW".equals(action)?AccessState.GRANTED:AccessState.CHECKING;enableActions();
                        SsrmeViewer.open(Screen.this,captured,result,action,
                            ()->isDisplayable()&&sameSession()&&selectedPatient.getAsBoolean(),()->requestLink(captured,action),trace);
                        if(!isDisplayable()||!sameSession())return;
                        if(!"VIEW".equals(action)){flow("VIEW");return;}
                        message("Viewer ditutup. Akses diperiksa kembali setiap kali RME dibuka.",false);
                    }catch(Exception e){state=failureState(e);message(error(e),true);}
                    enableActions();
                }
            }.execute();
        }
        private SsrmeClient.Link requestLink(SsrmeClient.Setup captured,String action)throws Exception{
            if(!sameSession())throw new SsrmeClient.Failure("SESSION","Sesi login berubah. Buka kembali dialog SSRME.");
            String ticket;try(Connection c=koneksiDB.condbTransaksi()){ticket=client.ticket(c,captured,captured.actorDoctor,action);}
            if(!sameSession())throw new SsrmeClient.Failure("SESSION","Sesi login berubah. Buka kembali dialog SSRME.");
            return client.request(captured.backend,ticket,!"VIEW".equals(action));
        }
        private void message(String value,boolean failed){status.setForeground(failed?new Color(166,52,52):INK);status.setText(value);status.setCaretPosition(0);revalidate();}
    }
    private static String error(Exception e) {
        Throwable cause=e instanceof ExecutionException && e.getCause()!=null ? e.getCause() : e;
        if (cause instanceof SsrmeClient.Failure) return cause.getMessage();
        if (cause instanceof SQLException) return "Konfigurasi SSRME belum dapat dibaca. Periksa koneksi database dan pemasangan rsaj_ssrme.sql.";
        return "SSRME belum dapat dibuka. Periksa koneksi server, browser, dan pemasangan modul. Coba kembali setelah pemeriksaan selesai.";
    }
    private static JLabel label(String value, boolean bold) { JLabel l=new JLabel(value); l.putClientProperty("html.disable",Boolean.TRUE); l.setFont(bold ? BOLD : NORMAL); l.setForeground(INK); return l; }
    private static JTextArea area(String value) {
        JTextArea a=new JTextArea(value); a.setEditable(false); a.setFocusable(false); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setFont(NORMAL); a.setForeground(INK); a.setOpaque(false); a.setColumns(52); return a;
    }
    public static JButton button(String value) {
        JButton b = new JButton(value) {
            @Override protected void paintComponent(Graphics original) {
                Graphics2D g=(Graphics2D)original.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isEnabled() ? (getModel().isRollover() ? new Color(218, 234, 255) : SOFT) : new Color(242,244,247));
                g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,18,18);
                if (isFocusOwner()) { g.setColor(BLUE); g.setStroke(new java.awt.BasicStroke(2)); g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,16,16); }
                g.dispose(); super.paintComponent(original);
            }
        };
        java.net.URL icon=SsrmeLauncher.class.getResource("/picture/item.png"); if (icon!=null) b.setIcon(new javax.swing.ImageIcon(icon));
        b.setFont(BOLD); b.setForeground(BLUE); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(7,13,7,13)); b.setHorizontalAlignment(SwingConstants.CENTER); return b;
    }
}
