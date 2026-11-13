package bridging;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/** Satu alur SSRME untuk seluruh form. Tidak melakukan query saat form/tabel dimuat. */
public final class SsrmeLauncher {
    private static final Color BLUE = new Color(35, 99, 190), SOFT = new Color(235, 244, 255), INK = new Color(36, 53, 75);
    private static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 12), BOLD = new Font("Segoe UI Semibold", Font.PLAIN, 12);
    private static final Map<Component, Screen> OPEN = new WeakHashMap<Component, Screen>();
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
        return dariBarisModel(table, table.convertRowIndexToModel(row), visitColumn, rmColumn);
    }

    private static SsrmeClient.Context dariBarisModel(JTable table, int modelRow, int visitColumn, int rmColumn) {
        Object visit = table.getModel().getValueAt(modelRow, visitColumn);
        Object rm = table.getModel().getValueAt(modelRow, rmColumn);
        return new SsrmeClient.Context(visit == null ? "" : visit.toString(), rm == null ? "" : rm.toString());
    }

    public static void pasangMenuTabel(Component owner, JPopupMenu popup, JTable table,
            int visitColumn, int rmColumn, boolean admission, boolean emergency) {
        if (Beans.isDesignTime()) return;
        JMenuItem item = menuItem(admission);
        item.setEnabled(false);
        popup.insert(item, 0);

        class PilihanPasien implements PopupMenuListener {
            private int modelRow = -1;
            private SsrmeClient.Context pasien;

            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                modelRow = barisModelPopup(table);
                pasien = modelRow < 0 ? null : dariBarisModel(table, modelRow, visitColumn, rmColumn);
                item.setEnabled(pasien != null && (!pasien.noRawat.isEmpty() || !pasien.noRm.isEmpty()));
            }

            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent event) { }
            @Override public void popupMenuCanceled(PopupMenuEvent event) { }

            void bukaPasien() {
                SsrmeClient.Context tujuan = pasien;
                if (tujuan == null) return;
                int row = modelRow < table.getModel().getRowCount() ? table.convertRowIndexToView(modelRow) : -1;
                if (row < 0 || !pasienSama(tujuan, dariBarisModel(table, modelRow, visitColumn, rmColumn))) {
                    javax.swing.JOptionPane.showMessageDialog(owner, "Daftar pasien berubah. Klik kanan kembali pada baris pasien.",
                            "SSRME", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                // Pemilihan baris ini hanya terjadi saat aksi SSRME dipilih.
                if (table.getSelectedRow() != row) table.setRowSelectionInterval(row, row);
                buka(owner, () -> {
                    SsrmeClient.Context current = dariTabel(table, visitColumn, rmColumn);
                    if (!pasienSama(tujuan, current)) throw new IllegalArgumentException("Pilihan pasien berubah.");
                    return current;
                }, admission, emergency);
            }
        }

        PilihanPasien pilihan = new PilihanPasien();
        popup.addPopupMenuListener(pilihan);
        item.addActionListener(event -> pilihan.bukaPasien());
    }

    private static int barisModelPopup(JTable table) {
        AWTEvent event = EventQueue.getCurrentEvent();
        if (event instanceof MouseEvent) {
            MouseEvent mouse = (MouseEvent) event;
            Component hit = mouse.getComponent();
            if (hit instanceof JTable && ((JTable) hit).getModel() == table.getModel()) {
                // Tabel utama dan kolom beku memakai model yang sama.
                JTable clicked = (JTable) hit;
                int row = clicked.rowAtPoint(mouse.getPoint());
                return row < 0 ? -1 : clicked.convertRowIndexToModel(row);
            }
            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
            if (scroll != null && scroll.getRowHeader() != null && SwingUtilities.isDescendingFrom(hit, scroll.getRowHeader())) {
                Point point = SwingUtilities.convertPoint(hit, mouse.getPoint(), table);
                int row = table.rowAtPoint(new Point(0, point.y));
                return row < 0 ? -1 : table.convertRowIndexToModel(row);
            }
            return -1;
        }
        // Menu yang dibuka lewat keyboard memakai pilihan tabel saat ini.
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }

    private static boolean pasienSama(SsrmeClient.Context expected, SsrmeClient.Context actual) {
        return expected.noRawat.equals(actual.noRawat) && expected.noRm.equals(actual.noRm);
    }

    public static void pasangMenuIdentitas(Component owner, Supplier<SsrmeClient.Context> source, JComponent... components) {
        if (Beans.isDesignTime()) return;
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item = menuItem(false);
        item.addActionListener(event -> buka(owner, source, false, false));
        popup.add(item);
        for (JComponent component : components) {
            if (component.getComponentPopupMenu() == null) component.setComponentPopupMenu(popup);
        }
    }

    private static JMenuItem menuItem(boolean admission) {
        JMenuItem item = new JMenuItem(admission ? "Persetujuan SSRME" : "SSRME");
        item.setFont(NORMAL);
        item.setToolTipText(admission ? "Persetujuan akses riwayat nasional pasien" : "Buka riwayat rekam medis nasional pasien");
        return item;
    }

    private static void buka(Component owner, Supplier<SsrmeClient.Context> source, boolean admission, boolean allowEmergency) {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> buka(owner, source, admission, allowEmergency)); return; }
        Screen opened = OPEN.get(owner);
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
        screen.load(context); screen.showBlocking();
    }

    enum AccessState { CHECKING, REQUIRED, GRANTED, ERROR, NO_DOCTOR }
    static AccessState failureState(Throwable cause) {
        if (cause instanceof ExecutionException && cause.getCause()!=null) cause=cause.getCause();
        return cause instanceof SsrmeClient.Failure && "CONSENT_REQUIRED".equals(((SsrmeClient.Failure)cause).code)
                ? AccessState.REQUIRED : AccessState.ERROR;
    }
    private static final class Screen extends SsrmeUi.WindowFrame {
        private final String actor;
        private final long epoch;
        private final boolean allowEmergency;
        private final java.util.function.BooleanSupplier selectedPatient;
        private final javax.swing.Timer sessionGuard;
        private final SsrmeTrace trace=new SsrmeTrace();
        private final SsrmeClient client=new SsrmeClient(trace::add);
        private final SsrmeUi.Header header=new SsrmeUi.Header();
        private final SsrmeUi.StatusPanel status=new SsrmeUi.StatusPanel();
        private final JButton view=button("Lihat RME Nasional"),consent=button("Persetujuan SSRME"),emergency=button("Akses Emergency"),retry=button("Cek Akses Lagi");
        private SsrmeClient.Setup setup;
        private AccessState state=AccessState.CHECKING;
        private boolean busy=true;
        Screen(Window parent,String actor,long epoch,boolean admission,boolean allowEmergency,java.util.function.BooleanSupplier selectedPatient) {
            super(parent,"SATUSEHAT RME · SIMRS RSAJ");
            this.actor=actor;this.epoch=epoch;this.allowEmergency=allowEmergency;this.selectedPatient=selectedPatient;
            sessionGuard=new javax.swing.Timer(400,e->{if(!sameSession()||!selectedPatient.getAsBoolean())dispose();});sessionGuard.start();
            JPanel body=new JPanel(new BorderLayout());body.setBackground(Color.WHITE);setContentPane(body);
            header.update(null,admission?"CONSENT":"ACCESS");body.add(header,BorderLayout.NORTH);
            status.showStatus("Menyiapkan akses SATUSEHAT","Memeriksa konfigurasi dan hak akses SSRME...",SsrmeUi.StatusPanel.INFO);
            body.add(status,BorderLayout.CENTER);
            JButton close=button("Tutup");close.addActionListener(e->dispose());
            body.add(SsrmeUi.footer(trace.button(this),SsrmeUi.guideButton(this,false),retry,consent,emergency,view,close),BorderLayout.SOUTH);
            consent.addActionListener(e->flow("CONSENT"));view.addActionListener(e->flow("VIEW"));emergency.addActionListener(e->flow("EMERGENCY"));retry.addActionListener(e->flow("VIEW"));
            enableActions();
        }
        private boolean sameSession(){return actor.equals(SsrmeClient.text(akses.getkode()))&&epoch==koneksiDB.versiSesiDatabase()&&!koneksiDB.sesiDatabaseTerkunci();}
        @Override public void dispose(){sessionGuard.stop();trace.clear();super.dispose();}
        void load(SsrmeClient.Context context){
            new SwingWorker<SsrmeClient.Setup,Void>(){
                @Override protected SsrmeClient.Setup doInBackground()throws Exception{try(Connection c=koneksiDB.condbTransaksi()){return client.load(c,actor,context);}}
                @Override protected void done(){
                    if(!isDisplayable())return;busy=false;if(!sameSession()){dispose();return;}
                    try{
                        setup=get();header.update(setup,"ACCESS");
                        SsrmeClient.Doctor own=null;for(SsrmeClient.Doctor d:setup.doctors)if(d.code.equals(setup.actorDoctor))own=d;
                        if(!setup.view||own==null){state=AccessState.NO_DOCTOR;header.noDoctor();message("Gunakan akun dokter aktif untuk persetujuan dan RME Nasional. Identitas dokter lain tidak dipilih secara otomatis.",true);enableActions();return;}
                        flow("VIEW");
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
            final SsrmeClient.Setup captured=setup;header.update(captured,action);busy=true;state=AccessState.CHECKING;enableActions();
            message("VIEW".equals(action)?"Memeriksa izin akses RME Nasional...":"EMERGENCY".equals(action)?"Meminta halaman akses emergency SSRME...":"Meminta halaman persetujuan SSRME...",false);
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
        private void message(String value,boolean failed){
            String title;int kind;
            if(state==AccessState.REQUIRED){title="Persetujuan pasien diperlukan";kind=SsrmeUi.StatusPanel.LOCKED;}
            else if(state==AccessState.NO_DOCTOR){title="Gunakan akun dokter aktif";kind=SsrmeUi.StatusPanel.LOCKED;}
            else if(state==AccessState.ERROR&&SsrmeUi.SHARING_MESSAGE.equals(value)){title="Periksa berbagi resume medis";kind=SsrmeUi.StatusPanel.LOCKED;}
            else if(state==AccessState.ERROR||failed){title="Layanan SSRME belum dapat diakses";kind=SsrmeUi.StatusPanel.ERROR;}
            else if(state==AccessState.GRANTED){title="Rekam medis siap dibuka";kind=SsrmeUi.StatusPanel.INFO;}
            else{title="Menyiapkan akses SATUSEHAT";kind=SsrmeUi.StatusPanel.INFO;}
            status.showStatus(title,value,kind);
            if(state==AccessState.REQUIRED||SsrmeUi.SHARING_MESSAGE.equals(value))status.setGuidance(SsrmeUi.PATIENT_GUIDE);
            revalidate();
        }
    }
    private static String error(Exception e) {
        Throwable cause=e instanceof ExecutionException && e.getCause()!=null ? e.getCause() : e;
        if (cause instanceof SsrmeClient.Failure) return cause.getMessage();
        if (cause instanceof SQLException) return "Konfigurasi SSRME belum dapat dibaca. Periksa koneksi database dan pemasangan rsaj_ssrme.sql.";
        return "SSRME belum dapat dibuka. Periksa koneksi server, browser, dan pemasangan modul. Coba kembali setelah pemeriksaan selesai.";
    }
    public static JButton button(String value) { return SsrmeUi.button(value); }
}
