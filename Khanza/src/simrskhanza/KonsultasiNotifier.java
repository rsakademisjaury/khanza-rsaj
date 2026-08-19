package simrskhanza;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import permintaan.DlgPermintaanKonsultasiMedik;

/**
 * Manager notifikasi konsultasi: stackable popup + sound + anti-duplikat (per sesi aplikasi).
 *
 * Cara pakai (contoh di after-login dokter):
 * KonsultasiNotifier notifier = new KonsultasiNotifier(koneksi, frameUtama);
 * notifier.checkAndNotify(kdDokterLogin, 5, 15); // limit=5, autoClose=15 detik
 *
 * Catatan anti-duplikat:
 * - Pop-up untuk no_permintaan yang sama hanya akan muncul SEKALI selama aplikasi berjalan.
 * - Jika perlu reset (mis. saat logout), panggil KonsultasiNotifier.clearShown().
 */
public class KonsultasiNotifier {
    private final Connection conn;
    private final Frame owner;

    // daftar popup aktif (untuk stacking & reflow)
    private final List<JDialog> activePopups = new ArrayList<>();

    // anti-duplikat (per sesi aplikasi)
    private static final Set<String> lastShown = ConcurrentHashMap.newKeySet();

    public static void clearShown() { lastShown.clear(); }

    public KonsultasiNotifier(Connection conn, Frame owner) {
        this.conn = Objects.requireNonNull(conn);
        this.owner = owner;
    }

    /** Data sederhana untuk 1 notifikasi */
    public static class KonsulItem {
        public final String noPermintaan;
        public final String noRawat;
        public final String namaPasien;
        public final String dokterFrom;
        public final String dokterTo;
        public KonsulItem(String noPermintaan, String noRawat, String namaPasien, String dokterFrom, String dokterTo) {
            this.noPermintaan = noPermintaan;
            this.noRawat = noRawat;
            this.namaPasien = namaPasien;
            this.dokterFrom = dokterFrom;
            this.dokterTo = dokterTo;
        }
    }

    /**
     * Cek konsultasi belum dijawab untuk dokter penerima dan tampilkan popup.
     * @param kdDokterPenerima kode dokter login
     * @param limit maksimum item
     * @param autoCloseSeconds 0 untuk nonaktif, >0 untuk auto-close
     */
    public void checkAndNotify(String kdDokterPenerima, int limit, int autoCloseSeconds) {
        List<KonsulItem> items = fetchPending(kdDokterPenerima, limit);
        if (items.isEmpty()) return;
        // filter anti-duplikat
        List<KonsulItem> fresh = new ArrayList<>();
        for (KonsulItem it : items) {
            if (lastShown.add(it.noPermintaan)) {
                fresh.add(it);
            }
        }
        if (fresh.isEmpty()) return;

        playSound();
        for (KonsulItem it : fresh) {
            showPopup(it, autoCloseSeconds);
        }
    }

    /** Query pending (NOT EXISTS di jawaban_konsultasi_medik) */
    private List<KonsulItem> fetchPending(String kdDokterPenerima, int limit) {
        List<KonsulItem> list = new ArrayList<>();
        String sql = "SELECT k.no_permintaan, k.no_rawat, p.nm_pasien, d_from.nm_dokter AS dokter_pengirim, d_to.nm_dokter AS dokter_penerima " +
                "FROM konsultasi_medik k " +
                "JOIN reg_periksa rp ON rp.no_rawat = k.no_rawat " +
                "JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis " +
                "JOIN dokter d_from ON d_from.kd_dokter = k.kd_dokter " +
                "JOIN dokter d_to   ON d_to.kd_dokter = k.kd_dokter_dikonsuli " +
                "WHERE k.kd_dokter_dikonsuli = ? " +
                "AND NOT EXISTS (SELECT 1 FROM jawaban_konsultasi_medik j WHERE j.no_permintaan = k.no_permintaan) " +
                "ORDER BY k.tanggal DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kdDokterPenerima);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new KonsulItem(
                            rs.getString("no_permintaan"),
                            rs.getString("no_rawat"),
                            rs.getString("nm_pasien"),
                            rs.getString("dokter_pengirim"),
                            rs.getString("dokter_penerima")
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("KonsultasiNotifier.fetchPending: " + e.getMessage());
        }
        return list;
    }

    /** Tampilkan satu popup dan stack di atas popup lain */
    private void showPopup(KonsulItem item, int autoCloseSeconds) {
        DialogNotifikasiKonsultasi d = new DialogNotifikasiKonsultasi(owner);
        d.setListener(new DialogNotifikasiKonsultasi.Listener() {
            @Override public void onJawab(String idKonsultasi, String noRawat) {
                // Buka langsung form DlgPermintaanKonsultasiMedik
                DlgPermintaanKonsultasiMedik dlg = new DlgPermintaanKonsultasiMedik(owner, false);
                dlg.setNoRm(item.noRawat, item.noPermintaan, item.namaPasien); // Sesuai definisi setNoRm
                dlg.tampil();
                dlg.setVisible(true);
                removeAndReflow(d);
            }
            @Override public void onAbaikan(String idKonsultasi, String noRawat) {
                removeAndReflow(d);
            }
        });
        d.setData(item.noPermintaan, item.noRawat, item.namaPasien, item.dokterFrom, item.dokterTo);
        d.pack();

        int index = activePopups.size();
        positionStack(d, index);
        activePopups.add(d);

        d.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { removeAndReflow(d); }
            @Override public void windowClosing(WindowEvent e) { removeAndReflow(d); }
        });
        d.showWithAutoClose(autoCloseSeconds);
    }

    private void removeAndReflow(JDialog dialog) {
        SwingUtilities.invokeLater(() -> {
            activePopups.remove(dialog);
            for (int i = 0; i < activePopups.size(); i++) {
                positionStack(activePopups.get(i), i);
            }
        });
    }

    private void positionStack(Window w, int index) {
        Rectangle screen;
        Window base = owner != null ? owner : KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (base != null && base.getGraphicsConfiguration() != null) {
            screen = base.getGraphicsConfiguration().getBounds();
        } else {
            screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        }
        int gap = 10;
        int marginX = 16;
        int marginY = 48;
        int x = screen.x + screen.width - w.getWidth() - marginX;
        int y = screen.y + screen.height - w.getHeight() - marginY - index * (w.getHeight() + gap);
        w.setLocation(x, y);
    }

    private void playSound() {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = getClass().getResource("/suara/notifikasi.wav");
                if (url != null) {
                    try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(url)) {
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioIn);
                        clip.start();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }
}
