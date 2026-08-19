/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package khanzahmsservicesiranap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.ApiSatuSehat;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.swing.Timer;
import org.springframework.http.HttpMethod;

/**
 *
 * @author windiartonugroho
 */
public class frmUtama extends javax.swing.JFrame {
    private final sekuel Sequel = new sekuel();
    private Connection koneksi = null;
    private PreparedStatement ps;
    private ResultSet rs;
    private final ApiSatuSehat api = new ApiSatuSehat();
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean sedangProses = false;
    private String link = "", idrs = "", passrs = "";
    private final AtomicLong lastRunAt = new AtomicLong(0L);
    private static final long INTERVAL_MS = 60L * 60L * 1000L; // 1 jam
    private static final int DELAY_MS = 3000;
    private static final int MAX_BATCH = 203;

    private static class ApiResult {
        boolean sukses;
        String statusApi;
        String message;
        int httpStatus;
    }

    /**
     * Creates new form frmUtama
     */
    public frmUtama() {
        initComponents();
        this.setSize(390, 340);
        this.setLocationRelativeTo(null);
        this.setVisible(false);

        try {
            link = normalisasiURLFasyankes(koneksiDB.URLAPISIRS());
            idrs = koneksiDB.IDSIRS();
            passrs = koneksiDB.PASSSIRS();
        } catch (Exception e) {
            logArea("Gagal membaca konfigurasi SIRS : " + e.getMessage());
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                jam();
            }
        });
    }

    private void logArea(String pesan) {
        String jamLog = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String tag = "INFO";
        String upper = pesan == null ? "" : pesan.toUpperCase();

        if (upper.contains("ERROR") || upper.contains("GAGAL") || upper.contains("FAIL")) {
            tag = "FAIL";
        } else if (upper.contains("RETRY") || upper.contains("WARNING") || upper.contains("LEWATI")) {
            tag = "WARN";
        } else if (upper.contains("HTTP") || upper.contains("PAYLOAD") || upper.contains("URL KIRIM")) {
            tag = "NET";
        } else if (upper.contains("MEMULAI") || upper.contains("KIRIM RUANG") || upper.contains("PERCOBAAN KIRIM") || upper.contains("BERHASIL")) {
            tag = "SYNC";
        }

        System.out.println("[" + jamLog + "] [" + tag + "] " + pesan);
    }

    private boolean kosong(String nilai) {
        return nilai == null || nilai.trim().isEmpty();
    }

    private String normalisasiURLFasyankes(String urlSirs) {
        if (urlSirs == null) {
            return "";
        }
        String url = urlSirs.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.toLowerCase().endsWith("/fasyankes")) {
            url = url + "/Fasyankes";
        }
        return url;
    }


private ApiResult evaluasiResponseApi(String responseJson, int httpStatus) {
    ApiResult result = new ApiResult();
    result.sukses = false;
    result.statusApi = "";
    result.message = "Response kosong";

    try {
        if (responseJson == null || responseJson.trim().isEmpty()) {
            result.message = "Response kosong";
            return result;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootResponse = mapper.readTree(responseJson);

        if (rootResponse.has("fasyankes")
                && rootResponse.path("fasyankes").isArray()
                && rootResponse.path("fasyankes").size() > 0) {

            JsonNode firstObject = rootResponse.path("fasyankes").get(0);

            String statusApi = "";
            String messageApi = "Tanpa pesan";

            if (firstObject.has("status") && !firstObject.path("status").isNull()) {
                statusApi = firstObject.path("status").asText().trim();
            }

            if (firstObject.has("message") && !firstObject.path("message").isNull()) {
                messageApi = firstObject.path("message").asText().trim();
            }

            result.statusApi = statusApi;
            result.message = messageApi;

            String gabung = (statusApi + " " + messageApi).toLowerCase();

            if (httpStatus >= 200 && httpStatus < 300
                    && !gabung.contains("tidak ditemukan")
                    && !gabung.contains("gagal")
                    && !gabung.contains("error")) {
                result.sukses = true;
            }
        } else {
            result.statusApi = "";
            result.message = "Format response tidak valid";
        }
    } catch (Exception e) {
        result.sukses = false;
        result.message = "Gagal parsing response : " + e.getMessage();
    }

    return result;
}

    private void simpanStatusPesan(String idTTKirim, String message, String waktuUpdate) {
        Sequel.meghapus("sirs_update_kamar", "id_t_tt", idTTKirim);
        Sequel.menyimpan("sirs_update_kamar", "?,?,?", "Kode Kamar", 3, new String[]{
            idTTKirim, message, waktuUpdate
        });
    }

    private void tambahStat(Map<String, Integer> target, String kelas) {
        String key = kosong(kelas) ? "TANPA KELAS" : kelas.trim();
        target.put(key, target.getOrDefault(key, 0) + 1);
    }

    private void printSummary(int berhasil, int gagal, Map<String, Integer> suksesPerKelas, Map<String, Integer> gagalPerKelas) {
        System.out.println("============================================================");
        System.out.println("================ UPDATE KAMAR SIRANAP SELESAI =============");
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] Ruang berhasil dikirim : " + berhasil);
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] Ruang gagal dikirim    : " + gagal);
        System.out.println("------------------------------------------------------------");
        System.out.println("RINCIAN BERHASIL PER KELAS");
        if (suksesPerKelas.isEmpty()) {
            System.out.println("- Tidak ada data berhasil");
        } else {
            for (Map.Entry<String, Integer> e : suksesPerKelas.entrySet()) {
                System.out.println("- " + e.getKey() + " : " + e.getValue());
            }
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("RINCIAN GAGAL PER KELAS");
        if (gagalPerKelas.isEmpty()) {
            System.out.println("- Tidak ada data gagal");
        } else {
            for (Map.Entry<String, Integer> e : gagalPerKelas.entrySet()) {
                System.out.println("- " + e.getKey() + " : " + e.getValue());
            }
        }
        System.out.println("============================================================");
    }

    private void prosesUpdateOtomatis() {
        int batchCount = 0;
        int suksesCount = 0;
        int gagalCount = 0;
        Map<String, Integer> suksesPerKelas = new LinkedHashMap<>();
        Map<String, Integer> gagalPerKelas = new LinkedHashMap<>();

        PreparedStatement psData = null;
        ResultSet rsData = null;

        try {
            koneksi = koneksiDB.condb();
            if (koneksi == null || koneksi.isClosed()) {
                logArea("Koneksi database gagal / null. Update dibatalkan.");
                return;
            }

            logArea("Memulai update Siranap Kemenkes");
            logArea("URL kirim : " + link);

            psData = koneksi.prepareStatement(
                "select " +
                "    sirs_mapping_kamar.id_t_tt, " +
                "    sirs_mapping_kamar.tt_kelas, " +
                "    sirs_mapping_kamar.nm_ruang, " +
                "    ifnull(sirs_update_kamar.message,'') as message, " +
                "    ifnull(sirs_update_kamar.updated_at,'') as updated_at, " +
                "    (select count(*) from kamar_tt " +
                "     inner join kamar on kamar.kd_kamar = kamar_tt.kd_kamar " +
                "     inner join sirs_mapping_kamar smk2 on smk2.kd_bangsal = kamar_tt.kd_ruang " +
                "     where kamar.statusdata='1' and smk2.id_t_tt = sirs_mapping_kamar.id_t_tt " +
                "    ) as jumlah, " +
                "    (select count(*) from kamar_tt " +
                "     inner join kamar on kamar.kd_kamar = kamar_tt.kd_kamar " +
                "     inner join sirs_mapping_kamar smk2 on smk2.kd_bangsal = kamar_tt.kd_ruang " +
                "     where kamar.statusdata='1' and kamar.status='ISI' " +
                "     and smk2.id_t_tt = sirs_mapping_kamar.id_t_tt " +
                "    ) as terpakai " +
                "from sirs_mapping_kamar " +
                "left join sirs_update_kamar on sirs_update_kamar.id_t_tt = sirs_mapping_kamar.id_t_tt " +
                "group by sirs_mapping_kamar.id_t_tt " +
                "order by sirs_mapping_kamar.tt_kelas, sirs_mapping_kamar.nm_ruang"
            );

            rsData = psData.executeQuery();

            while (rsData.next()) {
                if (batchCount >= MAX_BATCH) {
                    logArea("Batch maksimal " + MAX_BATCH + " data sudah terkirim.");
                    break;
                }

                String idTTKirim = rsData.getString("id_t_tt");
                String ttKelas = rsData.getString("tt_kelas");
                String namaRuang = rsData.getString("nm_ruang");
                String jumlah = String.valueOf(rsData.getInt("jumlah"));
                String terpakai = String.valueOf(rsData.getInt("terpakai"));

                if (kosong(idTTKirim) || kosong(ttKelas) || kosong(namaRuang)) {
                    logArea("Lewati id_t_tt " + idTTKirim + " karena mapping id_t_tt/tt_kelas/nm_ruang belum lengkap.");
                    continue;
                }

                try {
                    String waktuUpdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    String timestampnya = String.valueOf(System.currentTimeMillis() / 1000L);

                    ObjectNode jsonPayload = mapper.createObjectNode();
                    jsonPayload.put("id_t_tt", idTTKirim);
                    jsonPayload.put("ruang", namaRuang);
                    jsonPayload.put("jumlah_ruang", "0");
                    jsonPayload.put("jumlah", jumlah);
                    jsonPayload.put("terpakai", terpakai);
                    jsonPayload.put("terpakai_suspek", "0");
                    jsonPayload.put("terpakai_konfirmasi", "0");
                    jsonPayload.put("antrian", "0");
                    jsonPayload.put("prepare", "0");
                    jsonPayload.put("prepare_plan", "0");
                    jsonPayload.put("covid", 0);
                    jsonPayload.put("terpakai_dbd", "0");
                    jsonPayload.put("terpakai_dbd_anak", "0");
                    jsonPayload.put("jumlah_dbd", "0");

                    String jsonString = mapper.writeValueAsString(jsonPayload);
                    String responseJson = null;
                    int maxRetry = 3;
                    int retryCount = 0;
                    boolean sukses = false;
                    int[] httpStatusHolder = new int[]{0};
                    ApiResult apiResult = null;

                    logArea("Kirim id_t_tt " + idTTKirim +
                            " | tt_kelas=" + ttKelas +
                            " | ruang=" + namaRuang +
                            " | jumlah=" + jumlah +
                            " | terpakai=" + terpakai);
                    logArea("Payload : " + jsonString);

                    while (!sukses && retryCount < maxRetry) {
                        try {
                            int percobaan = retryCount + 1;
                            logArea("Percobaan kirim " + percobaan + "/" + maxRetry + " untuk id_t_tt " + idTTKirim);

                            responseJson = api.getRest().execute(
                                link,
                                HttpMethod.PUT,
                                request -> {
                                    request.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                                    request.getHeaders().add("X-rs-id", idrs);
                                    request.getHeaders().add("X-Timestamp", timestampnya);
                                    request.getHeaders().add("X-pass", passrs);
                                    OutputStream os = request.getBody();
                                    os.write(jsonString.getBytes(StandardCharsets.UTF_8));
                                    os.flush();
                                },
                                response -> {
                                    httpStatusHolder[0] = response.getStatusCode().value();
                                    logArea("HTTP Status id_t_tt " + idTTKirim + " : " + httpStatusHolder[0]);
                                    if (response.getBody() != null) {
                                        return new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                                            .lines().collect(Collectors.joining(""));
                                    } else {
                                        return "";
                                    }
                                }
                            );

                            logArea("Raw Response : " + responseJson);
                            apiResult = evaluasiResponseApi(responseJson, httpStatusHolder[0]);

                            if (apiResult.sukses) {
                                simpanStatusPesan(idTTKirim, apiResult.message, waktuUpdate);
                                logArea("Berhasil kirim data id_t_tt : " + idTTKirim + " | status=" + apiResult.statusApi + " | message=" + apiResult.message);
                                sukses = true;
                                suksesCount++;
                                tambahStat(suksesPerKelas, ttKelas);
                            } else {
                                retryCount++;
                                String failMsg = "Gagal response API id_t_tt " + idTTKirim +
                                                 " | status_http=" + httpStatusHolder[0] +
                                                 " | status_api=" + apiResult.statusApi +
                                                 " | message=" + apiResult.message;
                                logArea(failMsg);

                                if (retryCount >= maxRetry) {
                                    simpanStatusPesan(idTTKirim, apiResult.message, waktuUpdate);
                                    logArea("Gagal kirim id_t_tt : " + idTTKirim);
                                    gagalCount++;
                                    tambahStat(gagalPerKelas, ttKelas);
                                } else {
                                    Thread.sleep(DELAY_MS);
                                }
                            }
                        } catch (Exception ex) {
                            retryCount++;
                            logArea("Retry " + retryCount + "/" + maxRetry + " gagal kirim id_t_tt " + idTTKirim + " : " + ex.getMessage());

                            if (retryCount >= maxRetry) {
                                simpanStatusPesan(idTTKirim, "Gagal: " + ex.getMessage(), waktuUpdate);
                                logArea("Gagal kirim id_t_tt : " + idTTKirim);
                                gagalCount++;
                                tambahStat(gagalPerKelas, ttKelas);
                            } else {
                                Thread.sleep(DELAY_MS);
                            }
                        }
                    }

                    batchCount++;
                    Thread.sleep(DELAY_MS);
                } catch (Exception exLoop) {
                    exLoop.printStackTrace();
                    logArea("Error loop : " + exLoop.getMessage());
                    gagalCount++;
                    tambahStat(gagalPerKelas, rsData.getString("tt_kelas"));
                }
            }

            printSummary(suksesCount, gagalCount, suksesPerKelas, gagalPerKelas);
        } catch (Exception ex) {
            ex.printStackTrace();
            logArea("Error : " + ex.getMessage());
        } finally {
            try {
                if (rsData != null) rsData.close();
                if (psData != null) psData.close();
            } catch (Exception exClose) {
                exClose.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        TeksArea = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SIMKES Khanza Service Siranap Kemenkes");

        TeksArea.setColumns(20);
        TeksArea.setRows(5);
        jScrollPane1.setViewportView(TeksArea);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jButton1.setText("Keluar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(frmUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmUtama();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea TeksArea;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private void jam() {
        ActionListener taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long sekarang = System.currentTimeMillis();
                long terakhir = lastRunAt.get();

                if (sedangProses) {
                    return;
                }

                if (terakhir == 0L || (sekarang - terakhir) >= INTERVAL_MS) {
                    sedangProses = true;
                    lastRunAt.set(sekarang);

                    new Thread(() -> {
                        try {
                            prosesUpdateOtomatis();
                        } finally {
                            sedangProses = false;
                            lastRunAt.set(System.currentTimeMillis());
                        }
                    }, "siranap-worker").start();
                }
            }
        };

        taskPerformer.actionPerformed(null);
        new Timer(1000, taskPerformer).start();
    }
}
