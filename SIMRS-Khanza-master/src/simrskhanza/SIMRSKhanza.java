package simrskhanza;

import fungsi.koneksiDB;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

public class SIMRSKhanza {

    private static UpdateForm updateForm;
    private static final String PROGRESS_TITLE = "Proses Update Sedang Berlangsung";
    private static boolean mainApplicationStarted = false;

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);

        // Inisialisasi Form Pembaruan di Event Dispatch Thread dan tunggu hingga selesai
        try {
            SwingUtilities.invokeAndWait(() -> {
                updateForm = new UpdateForm();
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Gagal menginisialisasi form pembaruan: " + e.getMessage());
            return; // Menghentikan eksekusi jika form gagal diinisialisasi
        }

        new Thread(() -> {
            try {
                System.out.println("Periksa pembaruan sistem...");
                updateForm.updateStatus("Periksa pembaruan versi terbaru...");
                String serverVersion = getServerVersion();
                String localVersion = readLocalVersion();

                if (serverVersion == null || localVersion == null) {
                    throw new IllegalStateException("Versi server atau lokal tidak dapat diperoleh.");
                }

                System.out.println("Versi terbaru: " + serverVersion);
                System.out.println("Versi saat ini: " + localVersion);

                if (serverVersion.equals(localVersion)) {
                    System.out.println("Tidak ada pembaharuan sistem, membuka aplikasi SIMKES Khanza.");
                    updateForm.updateStatus("Versi terbaru sudah terpasang.");
                    Thread.sleep(1000); // Sedikit delay untuk user melihat
                    runMainApplication();
                } else {
                    System.out.println("Tersedia versi terbaru, memulai pembaruan sistem.");
                    updateForm.updateStatus("Tersedia pembaruan versi "+ serverVersion+". Unduh pembaruan sistem...");
                    performAutoUpdate(serverVersion);
                    System.out.println("Proses pembaruan selesai, menjalankan aplikasi.");
                    updateForm.updateStatus("Pembaruan selesai. Menjalankan aplikasi...");
                    Thread.sleep(1000); // Sedikit delay untuk user melihat
                    runMainApplication();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Proses gagal: " + e.getMessage());
            } finally {
                latch.countDown();
                System.out.println("Sinkronisasi selesai.");
                updateForm.updateStatus("Sinkronisasi berhasil. Menjalankan aplikasi...");
            }
        }).start();

        try {
            System.out.println("Sedang memuat data...");
            updateForm.updateStatus("Sedang memuat data...");
            latch.await();
            System.out.println("Proses selesai...");
            updateForm.updateStatus("Proses selesai...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

//    private static void runMainApplication() {
//        // Tutup Form Pembaruan
//        SwingUtilities.invokeLater(() -> {
//            if (updateForm != null) {
//                updateForm.dispose();
//            }
//            // Jalankan aplikasi utama
////            new MainApplication();
//        });
//        
//        try {
//            System.out.println("Menjalankan aplikasi...");
//            updateForm.updateStatus("Menjalan aplikasi...");
//            main utama = new main();
//            utama.setVisible(true);
//            System.out.println("Aplikasi berjalan.");
//            updateForm.updateStatus("Aplikasi berjalan...");
//        } catch (Exception e) {
//            e.printStackTrace();
//            showError("Gagal menjalankan aplikasi : " + e.getMessage());
//        }
//    }
    
private static String getServerVersion() throws IOException {
    String host = koneksiDB.HOSTHYBRIDWEB();
    String port = koneksiDB.PORTWEB();
    String hybridWeb = koneksiDB.HYBRIDWEB();

    if (hybridWeb.startsWith("/")) {
        hybridWeb = hybridWeb.substring(1);
    }
    if (hybridWeb.endsWith("/")) {
        hybridWeb = hybridWeb.substring(0, hybridWeb.length() - 1);
    }

    String serverURL = "http://" + host + ":" + port + "/" + hybridWeb + "/serverupdate/serverversion.txt";
    
    System.out.println("Mengecek pembaruan sistem...");

    StringBuilder version = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(serverURL).openStream()))) {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            version.append(inputLine.trim());
        }
    } catch (FileNotFoundException e) {
        System.out.println("Pengecekan gagal. File serverversion.txt tidak ditemukan.");
        return "Tidak ditemukan";
    } catch (IOException e) {
        System.out.println("Pengecekan gagal. Silakan periksa koneksi atau konfigurasi.");
        e.printStackTrace();
        throw e;
    }

    String serverVersion = version.toString();
    if (!serverVersion.isEmpty()) {
//        System.out.println("Versi terbaru: " + serverVersion); // log hanya sekali
    }

    return serverVersion.isEmpty() ? "Tidak ditemukan" : serverVersion;
}

    private static String readLocalVersion() throws IOException {
        String localVersionPath = "settingupdate/localversion.txt"; // Lokasi file versi lokal
        String version = "0.0"; // Default versi lokal jika file tidak ada
        try (BufferedReader br = new BufferedReader(new FileReader(localVersionPath))) {
            String line = br.readLine();
            if (line != null) {
                version = line.trim();
            }
        } catch (IOException e) {
            // Jika tidak ada file versi lokal, anggap versi awal adalah 0.0
            System.out.println("File versi saat ini tidak ditemukan, menggunakan versi default: 0.0");
        }
        return version;
    }

    private static void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

//    private static void performAutoUpdate(String serverVersion) {
//        Properties propServ = new Properties();
//        String URLSERVER = "";
//        String VERSION_URL = "";
//        String localVersionPath = "settingupdate/localversion.txt"; // Lokasi file versi lokal
//        String destinationDir = System.getProperty("user.dir"); // Lokasi aplikasi lokal
//        String serverVersionFile = "serverversion.txt"; // File versi yang ada di server
//        String extractedDirPath = destinationDir; // Direktori hasil ekstraksi
//
//        try {
//            // Load URL server dari konfigurasi
//            propServ.loadFromXML(new FileInputStream("settingupdate/config.xml"));
//            URLSERVER = propServ.getProperty("URLSERVERUPDATE");
//            VERSION_URL = URLSERVER + "/" + serverVersionFile;
//
//            if (URLSERVER == null || URLSERVER.isEmpty()) {
//                JOptionPane.showMessageDialog(null, "URL tidak ditemukan, cek Config.", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            // Unduh versi terbaru dari server
//            updateForm.updateStatus("Mengunduh versi terbaru...");
//            String latestVersion = downloadVersionFile(VERSION_URL);
//
//            // Cek versi lokal
//            String localVersion = readLocalVersion();
//
//            // Jika versi berbeda, lakukan update
//            if (!latestVersion.equals(localVersion)) {
//                updateForm.updateStatus("Mengunduh pembaruan sistem...");
//                String zipFilePath = destinationDir + "/dataupdate.zip";
//
//                // Unduh file pembaruan dari server
//                downloadFileWithProgress(URLSERVER + "/dataupdate.zip", zipFilePath);
//
//                updateForm.updateStatus("Mengekstrak data pembaruan...");
//                // Ekstrak file zip dengan progress
//                unzipWithProgress(zipFilePath, extractedDirPath);
//
//                updateForm.updateStatus("Menerapkan pembaruan...");
//                // Hapus file zip setelah ekstraksi
//                File zipFile = new File(zipFilePath);
//                if (zipFile.exists()) {
//                    zipFile.delete();
//                }
//
//                // Terapkan pembaruan dari direktori hasil ekstraksi ke direktori aplikasi utama
//                applyUpdate(extractedDirPath);
//
//                // Update versi lokal dengan versi server terbaru
//                updateLocalVersion(localVersionPath, latestVersion);
//
//                updateForm.updateStatus("Pembaruan berhasil ke versi: " + latestVersion);
//            } else {
//                updateForm.updateStatus("SIMRS sudah versi terbaru.");
//            }
//
//        } catch (IOException ex) {
//            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        } finally {
//            // Menutup frame progress setelah proses update selesai
//            if (updateForm != null) {
//                SwingUtilities.invokeLater(() -> updateForm.dispose());
//            }
//        }
//    }

private static void performAutoUpdate(String serverVersion) {
    Properties propServ = new Properties();
    String URLSERVER = "";
    String VERSION_URL = "";
    String localVersionPath = "settingupdate/localversion.txt";
    String destinationDir = System.getProperty("user.dir");
    String serverVersionFile = "serverversion.txt";
    String extractedDirPath = destinationDir;

    try {
        propServ.loadFromXML(new FileInputStream("settingupdate/config.xml"));
        URLSERVER = propServ.getProperty("URLSERVERUPDATE");
        VERSION_URL = URLSERVER + "/" + serverVersionFile;

        if (URLSERVER == null || URLSERVER.isEmpty()) {
            logError("URL tidak ditemukan, cek Config.");
            updateForm.updateStatus("Gagal mendapatkan URL server, memuat versi lama...");
            runMainApplication();
            return;
        }

        updateForm.updateStatus("Mengunduh versi terbaru...");
        String latestVersion = downloadVersionFile(VERSION_URL);
        String localVersion = readLocalVersion();

        if (latestVersion == null || latestVersion.isEmpty()) {
            logError("Versi terbaru tidak ditemukan.");
            updateForm.updateStatus("Versi terbaru tidak ditemukan, memuat versi lama...");
            runMainApplication();
            return;
        }

        if (localVersion == null || localVersion.isEmpty()) {
            logError("Versi lokal tidak ditemukan.");
            localVersion = "0.0"; // fallback ke versi default
        }

        if (!latestVersion.equals(localVersion)) {
            updateForm.updateStatus("Mengunduh pembaruan...");
            String zipFilePath = destinationDir + "/dataupdate.zip";

            try {
                downloadFileWithProgress(URLSERVER + "/dataupdate.zip", zipFilePath);
                updateForm.updateStatus("Mengekstrak data...");
                unzipWithProgress(zipFilePath, extractedDirPath);
                applyUpdate(extractedDirPath);
                updateLocalVersion(localVersionPath, latestVersion);
                updateForm.updateStatus("Pembaruan berhasil ke versi: " + latestVersion);
                logSuccess("Pembaruan berhasil ke versi: " + latestVersion);
            } catch (IOException e) {
                logError("Gagal memperbarui: " + e.getMessage());
                updateForm.updateStatus("Pembaruan gagal, memuat versi lama...");
                runMainApplication(); // Fallback otomatis
                return;
            }
        } else {
            updateForm.updateStatus("SIMRS sudah versi terbaru.");
            logSuccess("Tidak ada pembaruan, SIMRS sudah versi terbaru.");
        }
    } catch (IOException ex) {
        logError("Error: " + ex.getMessage());
        updateForm.updateStatus("Terjadi kesalahan, memuat versi lama...");
    } finally {
        if (!mainApplicationStarted) {
            runMainApplication(); // Pastikan hanya dijalankan sekali
        }
    }
}

private static void runMainApplication() {
    if (mainApplicationStarted) return; // Mencegah eksekusi ganda
    mainApplicationStarted = true;

    // Tutup Form Pembaruan
    SwingUtilities.invokeLater(() -> {
        if (updateForm != null) {
            updateForm.dispose();
        }
    });

    try {
        System.out.println("Menjalankan aplikasi...");
        updateForm.updateStatus("Menjalankan aplikasi...");
        main utama = new main();
        utama.setVisible(true);
        System.out.println("Aplikasi berjalan.");
        updateForm.updateStatus("Aplikasi berjalan...");
    } catch (Exception e) {
        e.printStackTrace();
        showError("Gagal menjalankan aplikasi: " + e.getMessage());
    }
}

private static void logError(String message) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("update_error.log", true))) {
        bw.write("[" + java.time.LocalDateTime.now() + "] ERROR: " + message);
        bw.newLine();
    } catch (IOException e) {
        System.err.println("Gagal mencatat log: " + e.getMessage());
    }
}

private static void logSuccess(String message) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("update_success.log", true))) {
        bw.write("[" + java.time.LocalDateTime.now() + "] SUCCESS: " + message);
        bw.newLine();
    } catch (IOException e) {
        System.err.println("Gagal mencatat log sukses: " + e.getMessage());
    }
}
   
    private static String downloadVersionFile(String versionURL) throws IOException {
        StringBuilder version = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(versionURL).openStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                version.append(inputLine);
            }
        }
        return version.toString();
    }

    private static void updateLocalVersion(String localVersionPath, String newVersion) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(localVersionPath))) {
            bw.write(newVersion);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error updating local version: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void downloadFileWithProgress(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);
        URLConnection connection = url.openConnection();
        int fileSize = connection.getContentLength();

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(saveDir)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;
            int progress = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                int currentProgress = (int) (totalBytesRead * 100 / fileSize);

                if (currentProgress != progress) {
                    progress = currentProgress;
                    final int prog = progress;
                    SwingUtilities.invokeLater(() -> updateForm.updateProgress(prog));
                }
            }
        }
    }

    private static void unzipWithProgress(String zipFilePath, String destDir) throws IOException {
        File zipFile = new File(zipFilePath);
        long zipFileSize = zipFile.length();  // Mendapatkan ukuran file zip
        long totalBytesRead = 0;  // Total byte yang sudah diekstrak

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                            totalBytesRead += len;
                            int progress = (int) (totalBytesRead * 100 / zipFileSize);
                            final int prog = progress;
                            SwingUtilities.invokeLater(() -> updateForm.updateProgress(progress));
                        }
                    }
                }

                zis.closeEntry();
            }
        }
    }

    private static void applyUpdate(String extractedDirPath) throws IOException {
        File extractedDir = new File(extractedDirPath);
        if (extractedDir.exists() && extractedDir.isDirectory()) {
            Path targetDir = Paths.get(System.getProperty("user.dir")); // Direktori aplikasi utama

            // Salin seluruh isi direktori hasil ekstraksi ke direktori aplikasi utama
            Files.walk(extractedDir.toPath())
                .forEach(sourcePath -> {
                    Path targetPath = targetDir.resolve(extractedDir.toPath().relativize(sourcePath));
                    try {
                        if (Files.isDirectory(sourcePath)) {
                            if (!Files.exists(targetPath)) {
                                Files.createDirectory(targetPath);
                            }
                        } else {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } else {
            throw new FileNotFoundException("Directory tidak ditemukan: " + extractedDirPath);
        }
    }
}
