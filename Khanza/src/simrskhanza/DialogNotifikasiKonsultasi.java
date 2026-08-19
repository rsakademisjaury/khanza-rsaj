package simrskhanza;

import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * NetBeans Form: DialogNotifikasiKonsultasi
 * - Wrap teks (HTML)
 * - Default button = btnJawab (ENTER)
 * - ESC = Abaikan
 * - Auto-close (countdown)
 * - Posisi pojok kanan bawah
 */
public class DialogNotifikasiKonsultasi extends javax.swing.JDialog {
    // ====== Data ======
    private String idKonsultasi;
    private String noRawat;
    private String namaPasien;
    private String dokterPengirim;
    private String dokterPenerima;

    // ====== Auto close ======
    private Timer autoCloseTimer;
    private int secondsLeft;

    // ====== Callback ======
    public interface Listener {
        void onJawab(String idKonsultasi, String noRawat);
        void onAbaikan(String idKonsultasi, String noRawat);
    }
    private Listener listener;

    public DialogNotifikasiKonsultasi(java.awt.Frame parent) {
        super(parent, false);
        initComponents();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);

        // Default button = Jawab
        getRootPane().setDefaultButton(btnJawab);

        // Icon null-safe
        java.net.URL iconUrl = getClass().getResource("/picture/notifikasi_32.png");
        if (iconUrl != null) {
            lblTitle.setIcon(new ImageIcon(iconUrl));
        }

        installShortcuts();
        pack();
        positionBottomRight(this);
    }

    // ====== API dipanggil dari luar ======
    public void setListener(Listener l) { this.listener = l; }

    public void setData(String idKonsultasi, String noRawat, String namaPasien,
                        String dokterPengirim, String dokterPenerima) {
        this.idKonsultasi = idKonsultasi;
        this.noRawat = noRawat;
        this.namaPasien = namaPasien;
        this.dokterPengirim = dokterPengirim;
        this.dokterPenerima = dokterPenerima;

        String plain = "Ada konsultasi medik " + safe(namaPasien) + " dari " + safe(dokterPengirim);
        lblTitle.setText("Konsultasi Medik Masuk");
        lblPesan.setText(htmlWrap(plain, 360));
        lblPesan.setToolTipText(plain);
        lblMeta.setText(htmlWrap("No. Rawat: " + safe(noRawat) + "   |   Penerima: " + safe(dokterPenerima), 360));
        setTitle("Konsultasi untuk " + safe(namaPasien));

        pack();
        positionBottomRight(this);
    }

    public void showWithAutoClose(int seconds) {
        if (seconds > 0) startAutoClose(seconds); else stopAutoClose();
        positionBottomRight(this);
        setVisible(true);
        requestFocusInWindow();
        toFront();
    }

    public void disableAutoClose() { stopAutoClose(); }

    private void startAutoClose(int seconds) {
        stopAutoClose();
        this.secondsLeft = seconds;
        updateAbaikanText();
        autoCloseTimer = new Timer(1000, e -> {
            secondsLeft--;
            updateAbaikanText();
            if (secondsLeft <= 0) doAbaikan();
        });
        autoCloseTimer.start();
    }

    private void stopAutoClose() {
        if (autoCloseTimer != null) {
            autoCloseTimer.stop();
            autoCloseTimer = null;
            btnAbaikan.setText("Abaikan");
        }
    }

    private void updateAbaikanText() {
        btnAbaikan.setText("Abaikan (" + secondsLeft + ")");
    }

    private void doJawab() {
        stopAutoClose();
        if (listener != null) listener.onJawab(idKonsultasi, noRawat);
        dispose();
    }

    private void doAbaikan() {
        stopAutoClose();
        if (listener != null) listener.onAbaikan(idKonsultasi, noRawat);
        dispose();
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String htmlWrap(String text, int widthPx) {
        if (text == null) text = "";
        String esc = text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        return "<html><div style='width:"+widthPx+"px;'>" + esc + "</div></html>";
    }

    // ====== Shortcut (ENTER/ESC) ======
    private void installShortcuts() {
        bind(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, "ENTER_JAWAB",
            javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { doJawab(); }
            });
        bind(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, "ESC_ABAIKAN",
            javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { doAbaikan(); }
            });
    }
    private static void bind(JComponent c, int cond, String key, KeyStroke ks, AbstractAction action) {
        InputMap im = c.getInputMap(cond);
        im.put(ks, key);
        c.getActionMap().put(key, action);
    }

    // ====== Posisi pojok kanan bawah ======
    public static void positionBottomRight(Window w) {
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (active == null) active = SwingUtilities.getWindowAncestor(w);
        if (active == null) active = w.getOwner();
        Rectangle bounds = (active != null ? active.getGraphicsConfiguration().getBounds() : w.getGraphicsConfiguration().getBounds());
        int x = bounds.x + bounds.width - w.getWidth() - 16;
        int y = bounds.y + bounds.height - w.getHeight() - 48;
        w.setLocation(x, y);
    }

    // ====== Generated code by NetBeans GUI Builder ======
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rootPanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        centerPanel = new javax.swing.JPanel();
        lblPesan = new javax.swing.JLabel();
        lblMeta = new javax.swing.JLabel();
        southPanel = new javax.swing.JPanel();
        btnAbaikan = new javax.swing.JButton();
        btnJawab = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Notifikasi Konsultasi Medik");
        setAlwaysOnTop(true);
        setResizable(false);

        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTitle.setText("Konsultasi Medik Masuk");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(lblTitle))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(lblTitle))
        );

        rootPanel.add(headerPanel);

        centerPanel.setLayout(new java.awt.BorderLayout());

        lblPesan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        centerPanel.add(lblPesan, java.awt.BorderLayout.NORTH);

        lblMeta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        centerPanel.add(lblMeta, java.awt.BorderLayout.CENTER);

        rootPanel.add(centerPanel);

        southPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnAbaikan.setText("Abaikan");
        southPanel.add(btnAbaikan);

        btnJawab.setText("Jawab / Lihat Konsul");
        btnJawab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJawabActionPerformed(evt);
            }
        });
        southPanel.add(btnJawab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 159, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(southPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(rootPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(southPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAbaikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbaikanActionPerformed
        doAbaikan();
    }//GEN-LAST:event_btnAbaikanActionPerformed

    private void btnJawabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJawabActionPerformed
        doJawab();
    }//GEN-LAST:event_btnJawabActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbaikan;
    private javax.swing.JButton btnJawab;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel lblMeta;
    private javax.swing.JLabel lblPesan;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel rootPanel;
    private javax.swing.JPanel southPanel;
    // End of variables declaration//GEN-END:variables
}
