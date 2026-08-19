package digital_klaim;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Notif {

    // Simpan dialog dan listener yang sedang aktif
    private static JDialog currentDialog;
    private static AWTEventListener currentClickListener;

    public static void showRespAutoClose(Component parent, String title, String message) {
        showRespAutoClose(parent, title, message, 5000);  // default 3 detik
    }

    public static void showRespAutoClose(Component parent, String title, String message, int millis) {
        // Kalau pesan kosong, jangan tampilkan dialog
        if (message == null || message.trim().isEmpty()) {
            System.out.println("[Notif] Pesan kosong, dialog tidak akan ditampilkan.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // Tutup dialog lama & lepas listener lama (kalau masih ada)
            if (currentDialog != null) {
                currentDialog.dispose();
                currentDialog = null;
            }
            if (currentClickListener != null) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(currentClickListener);
                currentClickListener = null;
            }

            final JOptionPane pane = new JOptionPane(
                    message,
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION,
                    null,
                    new Object[]{}, // tanpa tombol OK
                    null
            );

            final JDialog dialog = pane.createDialog(parent, title);
            dialog.setModal(false);
            dialog.setAlwaysOnTop(true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            currentDialog = dialog;

            // Timer auto-close
            final Timer timer = new Timer(millis, e -> {
                if (dialog.isShowing()) {
                    dialog.dispose();
                }
            });
            timer.setRepeats(false);
            timer.start();

            // Listener global: kalau klik di luar dialog -> tutup
            AWTEventListener clickListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (!(event instanceof MouseEvent)) return;

                    MouseEvent me = (MouseEvent) event;
                    if (me.getID() != MouseEvent.MOUSE_PRESSED) return;

                    Object src = me.getSource();
                    if (src instanceof Component) {
                        Component c = (Component) src;
                        // Kalau klik TIDAK di dalam dialog -> tutup
                        if (!SwingUtilities.isDescendingFrom(c, dialog)) {
                            dialog.dispose();
                        }
                    } else {
                        dialog.dispose();
                    }
                }
            };

            Toolkit.getDefaultToolkit().addAWTEventListener(
                    clickListener,
                    AWTEvent.MOUSE_EVENT_MASK
            );
            currentClickListener = clickListener;

            // Kalau dialog tertutup (karena timer / klik), bersihkan listener & referensi
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    timer.stop();
                    if (currentClickListener != null) {
                        Toolkit.getDefaultToolkit().removeAWTEventListener(currentClickListener);
                        currentClickListener = null;
                    }
                    if (currentDialog == dialog) {
                        currentDialog = null;
                    }
                }
            });

            // Posisi dialog relatif ke parent (kalau ada), kalau tidak di tengah layar
            if (parent instanceof Window) {
                dialog.setLocationRelativeTo(parent);
            } else {
                dialog.setLocationRelativeTo(null);
            }

            dialog.setVisible(true);
        });
    }
}
