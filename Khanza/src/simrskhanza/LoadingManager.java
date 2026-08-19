package simrskhanza;

import java.awt.Window;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Manager tunggal untuk DlgLoading.
 *
 * Tujuan:
 * - Semua form memakai satu mekanisme loading yang sama.
 * - Tidak perlu membuat DlgLoading berulang-ulang di setiap form.
 * - Mendukung progress manual 0-100%.
 * - Mendukung pekerjaan background melalui SwingWorker.
 *
 * Contoh paling sederhana:
 *
 *   LoadingManager.show(this, "Memuat data pasien...");
 *   LoadingManager.update(45, "Menyiapkan data pasien...");
 *   LoadingManager.update(80, "Menampilkan form...");
 *   LoadingManager.hide();
 *
 * Untuk query / I/O yang aman dikerjakan di background, gunakan run(...).
 */
public final class LoadingManager {

    private static final Object LOCK = new Object();
    private static DlgLoading dialog;
    private static Window currentOwner;
    private static int currentProgress = 0;
    private static final AtomicBoolean closing = new AtomicBoolean(false);
    private static final ScheduledExecutorService CLOSE_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "RSAJ-GlobalLoading-Closer");
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private LoadingManager() {
        // Utility class.
    }

    public static void show(Window owner, String message) {
        show(owner, message, 0);
    }

    public static void show(Window owner, String message, int progress) {
        final int safeProgress = clamp(progress);
        runOnEdt(new Runnable() {
            @Override
            public void run() {
                synchronized (LOCK) {
                    closing.set(false);
                    currentOwner = owner;
                    currentProgress = safeProgress;

                    if (dialog == null || !dialog.isDisplayable()) {
                        dialog = new DlgLoading(owner);
                    }

                    dialog.showLoading(owner, normalizeMessage(message), safeProgress);
                }
            }
        });
    }

    public static void update(int progress) {
        update(progress, null);
    }

    public static void update(int progress, String message) {
        final int safeProgress = clamp(progress);

        // Jangan kirim update progress ke EDT. DlgLoading V3 memakai model
        // volatile + renderer khusus, sehingga update ini tetap berjalan walau
        // EDT sedang sibuk membuat/query form lama.
        synchronized (LOCK) {
            currentProgress = safeProgress;
            if (dialog != null) {
                if (message != null && !message.trim().isEmpty()) {
                    dialog.setMessage(message);
                }
                dialog.setProgress(safeProgress);
            }
        }
    }

    /**
     * Menutup loading langsung.
     */
    public static void hide() {
        hide(false);
    }

    /**
     * @param showHundredFirst true = tampilkan 100% sebentar sebelum ditutup.
     */
    public static void hide(final boolean showHundredFirst) {
        synchronized (LOCK) {
            if (dialog == null || closing.get()) {
                return;
            }
            closing.set(true);

            if (showHundredFirst) {
                currentProgress = 100;
                dialog.setProgress(100);

                // Beri waktu renderer khusus menggambar 100% terlebih dahulu.
                // Penjadwal ini bukan Swing Timer, jadi tidak ikut macet di EDT.
                CLOSE_EXECUTOR.schedule(new Runnable() {
                    @Override
                    public void run() {
                        runOnEdtLater(new Runnable() {
                            @Override
                            public void run() {
                                closeDialogNow();
                            }
                        });
                    }
                }, 220L, TimeUnit.MILLISECONDS);
            } else {
                runOnEdtLater(new Runnable() {
                    @Override
                    public void run() {
                        closeDialogNow();
                    }
                });
            }
        }
    }

    public static boolean isShowing() {
        synchronized (LOCK) {
            return dialog != null && dialog.isVisible();
        }
    }

    public static int getProgress() {
        synchronized (LOCK) {
            return currentProgress;
        }
    }

    /**
     * Menjalankan proses non-UI di background dengan progress yang dapat di-update.
     *
     * PENTING:
     * Jangan memanipulasi komponen Swing dari doInBackground.
     * Gunakan onSuccess / onError untuk membuka atau mengubah form karena callback
     * tersebut dijalankan kembali di EDT.
     */
    public static <T> void run(
            final Window owner,
            final String initialMessage,
            final LoadingTask<T> task,
            final SuccessHandler<T> onSuccess,
            final ErrorHandler onError) {

        if (task == null) {
            throw new IllegalArgumentException("LoadingTask tidak boleh null");
        }

        show(owner, initialMessage, 0);

        SwingWorker<T, ProgressUpdate> worker = new SwingWorker<T, ProgressUpdate>() {
            @Override
            protected T doInBackground() throws Exception {
                ProgressReporter reporter = new ProgressReporter() {
                    @Override
                    public void update(int progress, String message) {
                        publish(new ProgressUpdate(clamp(progress), message));
                    }
                };
                return task.run(reporter);
            }

            @Override
            protected void process(List<ProgressUpdate> chunks) {
                if (chunks == null || chunks.isEmpty()) {
                    return;
                }
                ProgressUpdate latest = chunks.get(chunks.size() - 1);
                LoadingManager.update(latest.progress, latest.message);
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    LoadingManager.update(100, "Selesai...");
                    LoadingManager.hide(true);
                    if (onSuccess != null) {
                        onSuccess.onSuccess(result);
                    }
                } catch (Throwable ex) {
                    LoadingManager.hide();
                    Throwable actual = ex.getCause() != null ? ex.getCause() : ex;
                    if (onError != null) {
                        onError.onError(actual);
                    } else {
                        actual.printStackTrace();
                    }
                }
            }
        };
        worker.execute();
    }

    /**
     * Versi sederhana untuk Runnable background tanpa nilai balik.
     */
    public static void run(
            final Window owner,
            final String initialMessage,
            final BackgroundRunnable backgroundTask,
            final Runnable onFinished,
            final ErrorHandler onError) {

        run(owner, initialMessage,
                new LoadingTask<Void>() {
                    @Override
                    public Void run(ProgressReporter reporter) throws Exception {
                        backgroundTask.run(reporter);
                        return null;
                    }
                },
                new SuccessHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (onFinished != null) {
                            onFinished.run();
                        }
                    }
                },
                onError);
    }

    /**
     * Helper untuk pekerjaan UI yang memang HARUS tetap berjalan di EDT.
     * Loading ditampilkan lebih dulu agar user melihat indikator proses.
     *
     * Catatan: jika uiTask sangat berat, animasi spinner dapat berhenti sementara
     * karena Swing memakai satu EDT. Form tetap aman karena pekerjaan UI tidak
     * dipindahkan ke thread background.
     */
    public static void runUiTask(
            final Window owner,
            final String initialMessage,
            final Runnable uiTask,
            final Runnable onFinished,
            final ErrorHandler onError) {

        if (uiTask == null) {
            throw new IllegalArgumentException("uiTask tidak boleh null");
        }

        show(owner, initialMessage, 0);

        // Delay kecil memberi kesempatan dialog tampil dahulu sebelum task UI berat dimulai.
        runOnEdtLater(new Runnable() {
            @Override
            public void run() {
                Timer startTimer = new Timer(90, e -> {
                    try {
                        LoadingManager.update(15, initialMessage);
                        uiTask.run();
                        LoadingManager.update(100, "Selesai...");
                        LoadingManager.hide(true);
                        if (onFinished != null) {
                            onFinished.run();
                        }
                    } catch (Throwable ex) {
                        LoadingManager.hide();
                        if (onError != null) {
                            onError.onError(ex);
                        } else {
                            ex.printStackTrace();
                        }
                    }
                });
                startTimer.setRepeats(false);
                startTimer.start();
            }
        });
    }

    private static void closeDialogNow() {
        synchronized (LOCK) {
            if (dialog != null) {
                dialog.closeLoading();
                dialog = null;
            }
            currentOwner = null;
            currentProgress = 0;
            closing.set(false);
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static String normalizeMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Memuat data...";
        }
        return message.trim();
    }

    private static void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (Exception ex) {
            throw new IllegalStateException("Gagal menjalankan LoadingManager di EDT", ex);
        }
    }

    private static void runOnEdtLater(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public interface ProgressReporter {
        void update(int progress, String message);
    }

    public interface LoadingTask<T> {
        T run(ProgressReporter reporter) throws Exception;
    }

    public interface BackgroundRunnable {
        void run(ProgressReporter reporter) throws Exception;
    }

    public interface SuccessHandler<T> {
        void onSuccess(T result);
    }

    public interface ErrorHandler {
        void onError(Throwable error);
    }

    private static final class ProgressUpdate {
        private final int progress;
        private final String message;

        private ProgressUpdate(int progress, String message) {
            this.progress = progress;
            this.message = message;
        }
    }
}
