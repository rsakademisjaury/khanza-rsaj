/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgAbout.java
 *
 * Created on 23 Jun 10, 19:03:08
 */

package laporan;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import static javafx.concurrent.Worker.State.FAILED;
import javafx.embed.swing.JFXPanel;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author perpustakaan
 */
public class DlgBerkasRawatDev extends javax.swing.JDialog {
    private final JFXPanel jfxPanel = new JFXPanel();
    private WebEngine engine;
 
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();

    private final JTextField txtURL = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();
    private final validasi Valid=new validasi();
    private final sekuel Sequel=new sekuel();
    private String halaman="",norawat="";
    private boolean bdrDibukaDiBrowser=false;
    private PreparedStatement ps;
    private ResultSet rs;
    private final Connection koneksi=koneksiDB.condb();
    public DlgBerkasRawatDev(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initComponents2();
    }
    
    private void initComponents2() {           
        txtURL.addActionListener((ActionEvent e) -> {
            loadURL(txtURL.getText());
        });
  
        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);
        
        panel.add(jfxPanel, BorderLayout.CENTER);
        
        internalFrame1.setLayout(new BorderLayout());
        internalFrame1.add(panel);        
    }
    
     private void createScene() {        
        Platform.runLater(new Runnable() {

            public void run() {
                WebView view = new WebView();
                
                engine = view.getEngine();
                engine.setJavaScriptEnabled(true);
                
                engine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
                    @Override
                    public WebEngine call(PopupFeatures p) {
                        Stage stage = new Stage(StageStyle.TRANSPARENT);
                        return view.getEngine();
                    }
                });
                
                engine.titleProperty().addListener((ObservableValue<? extends String> observable, String oldValue, final String newValue) -> {
                    SwingUtilities.invokeLater(() -> {
                        DlgBerkasRawatDev.this.setTitle(newValue);
                    });
                });
                
                
                engine.setOnStatusChanged((final WebEvent<String> event) -> {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText(event.getData());
                    });
                });
                
                
                engine.getLoadWorker().workDoneProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) -> {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(newValue.intValue());
                    });                                                   
                });
                
                engine.getLoadWorker().exceptionProperty().addListener((ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) -> {
                    if (engine.getLoadWorker().getState() == FAILED) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    panel,
                                    (value != null) ?
                                            engine.getLocation() + "\n" + value.getMessage() :
                                            engine.getLocation() + "\nUnexpected Catatan.",
                                    "Loading Catatan...",
                                    JOptionPane.ERROR_MESSAGE);
                        });
                    }
                });
                
                
                engine.locationProperty().addListener((ObservableValue<? extends String> ov, String oldValue, final String newValue) -> {
                    SwingUtilities.invokeLater(() -> {
                        txtURL.setText(newValue);
                    });
                });
                
                engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                    @Override
                    public void changed(ObservableValue ov, State oldState, State newState) {
                        if (newState == State.SUCCEEDED) {
                            try {
                                if(engine.getLocation().replaceAll("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/","").contains("berkasrawat/pages")){
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    Valid.panggilUrl(engine.getLocation().replaceAll("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/berkasrawat/pages/upload/","berkasrawat/").replaceAll("http://"+koneksiDB.HOSTHYBRIDWEB()+"/"+koneksiDB.HYBRIDWEB()+"/berkasrawat/pages/upload/","berkasrawat/"));
                                    engine.executeScript("history.back()");
                                    setCursor(Cursor.getDefaultCursor());
                                }else if(engine.getLocation().replaceAll("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/","").contains("action=Keluar")){
                                    dispose();
                                }else if(engine.getLocation().replaceAll("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/","").contains("action=GABUNG")){
                                    norawat=Sequel.cariIsi("select temppanggilnorawat.no_rawat from temppanggilnorawat");
                                    ps=koneksi.prepareStatement("SELECT berkas_digital_perawatan.lokasi_file "+
                                                  "from berkas_digital_perawatan inner join master_berkas_digital "+
                                                  "on berkas_digital_perawatan.kode=master_berkas_digital.kode "+
                                                  "where berkas_digital_perawatan.no_rawat=? ORDER BY master_berkas_digital.nama ASC ");
                                    try {
                                        PDFMergerUtility ut = new PDFMergerUtility();
                                        URL url;
                                        ps.setString(1,norawat);
                                        rs=ps.executeQuery();
                                        while(rs.next()){
                                            url = new URL("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/berkasrawat/"+rs.getString("lokasi_file"));
                                            InputStream is = url.openStream();
                                            ut.addSource(is);
                                        }
                                        ut.setDestinationFileName("merge.pdf");
                                        ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                                        JOptionPane.showMessageDialog(null,"Proses gabung file selesai..!");
                                        Properties systemProp = System.getProperties();
                                        String currentDir = systemProp.getProperty("user.dir");
                                        File dir = new File(currentDir);
                                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                        Valid.panggilUrl2(dir+"/merge.pdf");
                                        setCursor(Cursor.getDefaultCursor());
                                    } catch (SQLException e) {
                                        System.out.println("Notif : "+e);
                                    } catch (IOException e) {
                                        System.out.println("Notif : "+e);
                                        JOptionPane.showMessageDialog(null,"Gagal menggabungkan file, cek kembali file apakah sudah dalam bentuk PDF.\nAtau cek kembali hak akses file di server dokumen..!!");
                                    } finally{
                                        if(rs!=null){
                                            rs.close();
                                        }
                                        if(ps!=null){
                                            ps.close();
                                        }
                                    }                                    
                                }
                            } catch (Exception ex) {
                                System.out.println("Notifikasi : "+ex);
                            }
                        } 
                    }
                });
                
                jfxPanel.setScene(new Scene(view));
            }
        });
    }
 
    private String bdrDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value == null ? "" : value, "UTF-8");
        } catch (Exception e) {
            return value == null ? "" : value;
        }
    }

    private String bdrEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value == null ? "" : value, "UTF-8");
        } catch (Exception e) {
            return value == null ? "" : value;
        }
    }

    private String bdrQueryValue(String url, String key) {
        try {
            if (url == null || !url.contains("?")) {
                return "";
            }

            String query = url.substring(url.indexOf("?") + 1);
            String[] params = query.split("&");
            for (String param : params) {
                int eq = param.indexOf("=");
                if (eq > -1) {
                    String paramKey = bdrDecode(param.substring(0, eq));
                    if (key.equals(paramKey)) {
                        return bdrDecode(param.substring(eq + 1));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Query Detail4 : " + e);
        }
        return "";
    }

    private String bdrBaseBerkasRawat(String url) {
        try {
            String marker = "/berkasrawat/";
            int pos = url.indexOf(marker);
            if (pos > -1) {
                return url.substring(0, pos + marker.length());
            }
        } catch (Exception e) {
            System.out.println("Notif Base Detail4 : " + e);
        }

        return "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/berkasrawat/";
    }

private String bdrSafe(String value) {
    return value == null ? "" : value.trim();
}

private String bdrReportSafeId(String value) {
    return bdrSafe(value).replaceAll("[^A-Za-z0-9]", "");
}

private String bdrReportFileStem(String type, String id) {
    String clean = bdrReportSafeId(id);

    if ("resume_ralan".equals(type)) {
        return "BDP4_RESUME_RALAN_" + clean;
    } else if ("resume_ranap".equals(type)) {
        return "BDP4_RESUME_RANAP_" + clean;
    } else if ("sep".equals(type)) {
        return "BDP4_SEP_" + clean;
    }

    return "";
}

private File bdrReportPdfFile(String type, String id) {
    String stem = bdrReportFileStem(type, id);
    if (stem.equals("")) {
        return null;
    }
    return new File("tmpPDF/" + stem + ".pdf");
}

private Map<String, Object> bdrCommonReportParam() {
    Map<String, Object> param = new HashMap<>();

    param.put("namars", akses.getnamars());
    param.put("alamatrs", akses.getalamatrs());
    param.put("kotars", akses.getkabupatenrs());
    param.put("propinsirs", akses.getpropinsirs());
    param.put("kontakrs", akses.getkontakrs());
    param.put("emailrs", akses.getemailrs());
    param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

    return param;
}

private String bdrFingerDokter(String kdDokter) {
    if (bdrSafe(kdDokter).equals("")) {
        return "";
    }

    return bdrSafe(Sequel.cariIsi(
        "select sha1(sidikjari.sidikjari) " +
        "from sidikjari inner join pegawai on pegawai.id=sidikjari.id " +
        "where pegawai.nik=?",
        kdDokter
    ));
}

private String bdrTextFinger(
        String kdDokter,
        String nmDokter,
        String tanggal
) {
    String finger = bdrFingerDokter(kdDokter);
    String idDokter = finger.equals("") ? kdDokter : finger;

    return "Dikeluarkan di " + akses.getnamars() +
           ", Kabupaten/Kota " + akses.getkabupatenrs() +
           "\nDitandatangani secara elektronik oleh " + nmDokter +
           "\nID " + idDokter +
           "\n" + tanggal;
}

/*
|--------------------------------------------------------------------------
| GENERATE RESUME RAWAT JALAN
|--------------------------------------------------------------------------
*/
private File bdrGenerateResumeRalanPdf(String noRawat) {
    try {
        String kdDokter = bdrSafe(Sequel.cariIsi(
            "select kd_dokter from resume_pasien where no_rawat=?",
            noRawat
        ));

        if (kdDokter.equals("")) {
            return null;
        }

        String nmDokter = bdrSafe(Sequel.cariIsi(
            "select dokter.nm_dokter " +
            "from resume_pasien inner join dokter " +
            "on resume_pasien.kd_dokter=dokter.kd_dokter " +
            "where resume_pasien.no_rawat=?",
            noRawat
        ));

        String statusLanjut = bdrSafe(Sequel.cariIsi(
            "select status_lanjut from reg_periksa where no_rawat=?",
            noRawat
        ));

        String ruang = "";
        String tanggal = "";

        if ("Ralan".equalsIgnoreCase(statusLanjut)) {
            ruang = bdrSafe(Sequel.cariIsi(
                "select poliklinik.nm_poli " +
                "from poliklinik inner join reg_periksa " +
                "on reg_periksa.kd_poli=poliklinik.kd_poli " +
                "where reg_periksa.no_rawat=?",
                noRawat
            ));

            tanggal = bdrSafe(Sequel.cariIsi(
                "select DATE_FORMAT(tgl_registrasi,'%d-%m-%Y') " +
                "from reg_periksa where no_rawat=?",
                noRawat
            ));
        } else {
            ruang = bdrSafe(Sequel.cariIsi(
                "select bangsal.nm_bangsal " +
                "from bangsal inner join kamar inner join kamar_inap " +
                "on bangsal.kd_bangsal=kamar.kd_bangsal " +
                "and kamar_inap.kd_kamar=kamar.kd_kamar " +
                "where kamar_inap.no_rawat=? " +
                "order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1",
                noRawat
            ));

            tanggal = bdrSafe(Sequel.cariIsi(
                "select DATE_FORMAT(tgl_keluar,'%d-%m-%Y') " +
                "from kamar_inap where no_rawat=? " +
                "and tgl_keluar<>'0000-00-00' " +
                "order by tgl_keluar desc,jam_keluar desc limit 1",
                noRawat
            ));
        }

        Map<String, Object> param = bdrCommonReportParam();

        param.put("norawat", noRawat);
        param.put("ruang", ruang);
        param.put("tanggalkeluar", tanggal);
        param.put(
            "finger",
            bdrTextFinger(kdDokter, nmDokter, tanggal)
        );

        String stem = bdrReportFileStem("resume_ralan", noRawat);

        Valid.MyReportPDF3(
            "rptLaporanResume.jasper",
            "report",
            "::[ Laporan Resume Pasien ]::",
            stem,
            param
        );

        File pdf = bdrReportPdfFile("resume_ralan", noRawat);

        if (pdf != null && pdf.exists() && pdf.length() > 0) {
            return pdf;
        }

    } catch (Exception e) {
        System.out.println("Notif Generate Resume Ralan Detail4 : " + e);
    }

    return null;
}

/*
|--------------------------------------------------------------------------
| GENERATE RESUME RAWAT INAP
|--------------------------------------------------------------------------
*/
private File bdrGenerateResumeRanapPdf(String noRawat) {
    PreparedStatement psDpjp = null;
    ResultSet rsDpjp = null;

    try {
        String kdDokterResume = bdrSafe(Sequel.cariIsi(
            "select kd_dokter from resume_pasien_ranap where no_rawat=?",
            noRawat
        ));

        if (kdDokterResume.equals("")) {
            return null;
        }

        String nmDokterResume = bdrSafe(Sequel.cariIsi(
            "select dokter.nm_dokter " +
            "from resume_pasien_ranap inner join dokter " +
            "on resume_pasien_ranap.kd_dokter=dokter.kd_dokter " +
            "where resume_pasien_ranap.no_rawat=?",
            noRawat
        ));

        String kdDokterPengirim = bdrSafe(Sequel.cariIsi(
            "select kd_dokter from reg_periksa where no_rawat=?",
            noRawat
        ));

        String kdKamar = bdrSafe(Sequel.cariIsi(
            "select kamar_inap.kd_kamar " +
            "from kamar_inap where no_rawat=? " +
            "order by tgl_keluar desc,jam_keluar desc,tgl_masuk desc,jam_masuk desc limit 1",
            noRawat
        ));

        String nmBangsal = bdrSafe(Sequel.cariIsi(
            "select bangsal.nm_bangsal " +
            "from kamar_inap inner join kamar " +
            "on kamar_inap.kd_kamar=kamar.kd_kamar " +
            "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
            "where kamar_inap.no_rawat=? " +
            "order by kamar_inap.tgl_keluar desc,kamar_inap.jam_keluar desc," +
            "kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1",
            noRawat
        ));

        String tanggalKeluar = bdrSafe(Sequel.cariIsi(
            "select DATE_FORMAT(tgl_keluar,'%d-%m-%Y') " +
            "from kamar_inap where no_rawat=? " +
            "and tgl_keluar<>'0000-00-00' " +
            "order by tgl_keluar desc,jam_keluar desc limit 1",
            noRawat
        ));

        String jamKeluar = bdrSafe(Sequel.cariIsi(
            "select jam_keluar " +
            "from kamar_inap where no_rawat=? " +
            "and tgl_keluar<>'0000-00-00' " +
            "order by tgl_keluar desc,jam_keluar desc limit 1",
            noRawat
        ));

        String ruang = (kdKamar + " " + nmBangsal).trim();

        Map<String, Object> param = bdrCommonReportParam();

        param.put("norawat", noRawat);

        param.put(
            "finger",
            bdrTextFinger(
                kdDokterResume,
                nmDokterResume,
                tanggalKeluar
            )
        );

        /*
         * Default kosong agar Jasper tetap aman
         * apabila pasien hanya memiliki satu DPJP.
         */
        param.put("finger2", "");
        param.put("finger3", "");
        param.put("namadokter2", "");
        param.put("namadokter3", "");

        psDpjp = koneksi.prepareStatement(
            "select dpjp_ranap.kd_dokter,dokter.nm_dokter " +
            "from dpjp_ranap inner join dokter " +
            "on dpjp_ranap.kd_dokter=dokter.kd_dokter " +
            "where dpjp_ranap.no_rawat=? " +
            "and dpjp_ranap.kd_dokter<>?"
        );

        psDpjp.setString(1, noRawat);
        psDpjp.setString(2, kdDokterPengirim);

        rsDpjp = psDpjp.executeQuery();

        int urutDokter = 2;

        while (rsDpjp.next() && urutDokter <= 3) {
            String kdDokter = bdrSafe(rsDpjp.getString("kd_dokter"));
            String nmDokter = bdrSafe(rsDpjp.getString("nm_dokter"));

            if (urutDokter == 2) {
                param.put(
                    "finger2",
                    bdrTextFinger(
                        kdDokter,
                        nmDokter,
                        tanggalKeluar
                    )
                );
                param.put("namadokter2", nmDokter);
            } else if (urutDokter == 3) {
                param.put(
                    "finger3",
                    bdrTextFinger(
                        kdDokter,
                        nmDokter,
                        tanggalKeluar
                    )
                );
                param.put("namadokter3", nmDokter);
            }

            urutDokter++;
        }

        param.put("ruang", ruang);
        param.put("tanggalkeluar", tanggalKeluar);
        param.put("jamkeluar", jamKeluar);

        String stem = bdrReportFileStem(
            "resume_ranap",
            noRawat
        );

        Valid.MyReportPDF3(
            "rptLaporanResumeRanap.jasper",
            "report",
            "::[ Laporan Resume Pasien ]::",
            stem,
            param
        );

        File pdf = bdrReportPdfFile(
            "resume_ranap",
            noRawat
        );

        if (pdf != null && pdf.exists() && pdf.length() > 0) {
            return pdf;
        }

    } catch (Exception e) {
        System.out.println(
            "Notif Generate Resume Ranap Detail4 : " + e
        );
    } finally {
        try {
            if (rsDpjp != null) {
                rsDpjp.close();
            }
        } catch (Exception e) {
        }

        try {
            if (psDpjp != null) {
                psDpjp.close();
            }
        } catch (Exception e) {
        }
    }

    return null;
}

/*
|--------------------------------------------------------------------------
| GENERATE SEP
|--------------------------------------------------------------------------
*/
private File bdrGenerateSepPdf(String noSep) {
    try {
        if (bdrSafe(noSep).equals("")) {
            return null;
        }

        Map<String, Object> param = bdrCommonReportParam();

        /*
         * Logo SEP menggunakan gambar BPJS,
         * sesuai pemanggilan report SEP Khanza.
         */
        param.put(
            "logo",
            Sequel.cariGambar("select gambar.bpjs from gambar")
        );

        param.put(
            "prb",
            Sequel.cariIsi(
                "select bpjs_prb.prb " +
                "from bpjs_prb where bpjs_prb.no_sep=?",
                noSep
            )
        );

        param.put("parameter", noSep);

        String stem = bdrReportFileStem(
            "sep",
            noSep
        );

        Valid.MyReportPDF3(
            "rptBridgingSEP.jasper",
            "report",
            "::[ Cetak SEP ]::",
            stem,
            param
        );

        File pdf = bdrReportPdfFile(
            "sep",
            noSep
        );

        if (pdf != null && pdf.exists() && pdf.length() > 0) {
            return pdf;
        }

    } catch (Exception e) {
        System.out.println(
            "Notif Generate SEP Detail4 : " + e
        );
    }

    return null;
}

/*
|--------------------------------------------------------------------------
| UPLOAD PDF CACHE KE SERVER BERKASRAWAT
|--------------------------------------------------------------------------
*/
private void bdrWriteFormField(
        java.io.DataOutputStream out,
        String boundary,
        String name,
        String value
) throws IOException {

    out.writeBytes("--" + boundary + "\r\n");

    out.writeBytes(
        "Content-Disposition: form-data; name=\"" +
        name +
        "\"\r\n\r\n"
    );

    out.write(
        (value == null ? "" : value).getBytes("UTF-8")
    );

    out.writeBytes("\r\n");
}

private boolean bdrUploadReportCache(
        String base,
        File pdf,
        String remoteName,
        String user,
        String pass
) {
    java.net.HttpURLConnection connection = null;
    java.io.DataOutputStream output = null;
    java.io.FileInputStream input = null;

    try {
        if (pdf == null || !pdf.exists() || pdf.length() <= 0) {
            return false;
        }

        String boundary =
            "----BDP4Boundary" +
            System.currentTimeMillis();

        URL endpoint = new URL(
            base + "pages/upload_report_cache.php"
        );

        connection =
            (java.net.HttpURLConnection)
            endpoint.openConnection();

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(120000);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");

        connection.setRequestProperty(
            "Content-Type",
            "multipart/form-data; boundary=" + boundary
        );

        output = new java.io.DataOutputStream(
            connection.getOutputStream()
        );

        bdrWriteFormField(
            output,
            boundary,
            "usere",
            user
        );

        bdrWriteFormField(
            output,
            boundary,
            "passwordte",
            pass
        );

        bdrWriteFormField(
            output,
            boundary,
            "filename",
            remoteName
        );

        output.writeBytes(
            "--" + boundary + "\r\n"
        );

        output.writeBytes(
            "Content-Disposition: form-data; " +
            "name=\"file\"; filename=\"" +
            remoteName +
            "\"\r\n"
        );

        output.writeBytes(
            "Content-Type: application/pdf\r\n\r\n"
        );

        input = new java.io.FileInputStream(pdf);

        byte[] buffer = new byte[8192];
        int read;

        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }

        output.writeBytes("\r\n");
        output.writeBytes(
            "--" + boundary + "--\r\n"
        );

        output.flush();

        int responseCode =
            connection.getResponseCode();

        InputStream responseStream;

        if (responseCode >= 200 &&
            responseCode < 400) {

            responseStream =
                connection.getInputStream();

        } else {
            responseStream =
                connection.getErrorStream();
        }

        StringBuilder response =
            new StringBuilder();

        if (responseStream != null) {
            java.io.BufferedReader reader =
                new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                        responseStream,
                        "UTF-8"
                    )
                );

            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
        }

        boolean berhasil =
            responseCode == 200 &&
            response.toString().contains("\"ok\":true");

        if (!berhasil) {
            System.out.println(
                "Upload Cache Detail4 gagal [" +
                responseCode +
                "] : " +
                response
            );
        }

        return berhasil;

    } catch (Exception e) {
        System.out.println(
            "Notif Upload Report Cache Detail4 : " + e
        );

        return false;

    } finally {
        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
        }

        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {
        }

        if (connection != null) {
            connection.disconnect();
        }
    }
}

private void bdrUploadDanBersihkan(
        String base,
        File pdf,
        String remoteName,
        String user,
        String pass
) {
    if (pdf == null) {
        return;
    }

    boolean berhasil =
        bdrUploadReportCache(
            base,
            pdf,
            remoteName,
            user,
            pass
        );

    /*
     * Hapus file lokal hanya setelah PDF
     * berhasil sampai ke cache web.
     */
    if (berhasil && pdf.exists()) {
        try {
            pdf.delete();
        } catch (Exception e) {
        }
    }
}

/*
|--------------------------------------------------------------------------
| SIAPKAN SEMUA CACHE REPORT UNTUK PASIEN
|--------------------------------------------------------------------------
*/
private void bdrSiapkanReportCache(
        String originalUrl
) {
    PreparedStatement psSep = null;
    ResultSet rsSep = null;

    try {
        String noRawat =
            bdrQueryValue(
                originalUrl,
                "no_rawat"
            );

        String user =
            bdrQueryValue(
                originalUrl,
                "usere"
            );

        String pass =
            bdrQueryValue(
                originalUrl,
                "passwordte"
            );

        if (bdrSafe(noRawat).equals("")) {
            return;
        }

        String base =
            bdrBaseBerkasRawat(
                originalUrl
            );

        /*
        |--------------------------------------------------------------------------
        | RESUME RAWAT JALAN
        |--------------------------------------------------------------------------
        */
        String adaResumeRalan =
            bdrSafe(
                Sequel.cariIsi(
                    "select no_rawat " +
                    "from resume_pasien " +
                    "where no_rawat=?",
                    noRawat
                )
            );

        if (!adaResumeRalan.equals("")) {

            File pdf =
                bdrGenerateResumeRalanPdf(
                    noRawat
                );

            String nama =
                bdrReportFileStem(
                    "resume_ralan",
                    noRawat
                ) +
                ".pdf";

            bdrUploadDanBersihkan(
                base,
                pdf,
                nama,
                user,
                pass
            );
        }

        /*
        |--------------------------------------------------------------------------
        | RESUME RAWAT INAP
        |--------------------------------------------------------------------------
        */
        String adaResumeRanap =
            bdrSafe(
                Sequel.cariIsi(
                    "select no_rawat " +
                    "from resume_pasien_ranap " +
                    "where no_rawat=?",
                    noRawat
                )
            );

        if (!adaResumeRanap.equals("")) {

            File pdf =
                bdrGenerateResumeRanapPdf(
                    noRawat
                );

            String nama =
                bdrReportFileStem(
                    "resume_ranap",
                    noRawat
                ) +
                ".pdf";

            bdrUploadDanBersihkan(
                base,
                pdf,
                nama,
                user,
                pass
            );
        }

        /*
        |--------------------------------------------------------------------------
        | SEMUA SEP DALAM SATU NO. RAWAT
        |--------------------------------------------------------------------------
        */
        psSep = koneksi.prepareStatement(
            "select no_sep " +
            "from bridging_sep " +
            "where no_rawat=? " +
            "order by tglsep desc,no_sep desc"
        );

        psSep.setString(
            1,
            noRawat
        );

        rsSep =
            psSep.executeQuery();

        while (rsSep.next()) {

            String noSep =
                bdrSafe(
                    rsSep.getString(
                        "no_sep"
                    )
                );

            if (noSep.equals("")) {
                continue;
            }

            File pdf =
                bdrGenerateSepPdf(
                    noSep
                );

            String nama =
                bdrReportFileStem(
                    "sep",
                    noSep
                ) +
                ".pdf";

            bdrUploadDanBersihkan(
                base,
                pdf,
                nama,
                user,
                pass
            );
        }

    } catch (Exception e) {
        /*
         * Gagal membuat report tidak boleh
         * menggagalkan pembukaan Detail4.
         */
        System.out.println(
            "Notif Siapkan Report Cache Detail4 : " + e
        );

    } finally {
        try {
            if (rsSep != null) {
                rsSep.close();
            }
        } catch (Exception e) {
        }

        try {
            if (psSep != null) {
                psSep.close();
            }
        } catch (Exception e) {
        }
    }
}
    
    private String bdrUbahKeOpenDetail4(String url) {
        String lower = (url == null ? "" : url).toLowerCase();

        if (lower.contains("/berkasrawat/login2.php") || lower.contains("/berkasrawat/login2nonhapus.php")) {
            String base = bdrBaseBerkasRawat(url);
            String noRawat = bdrQueryValue(url, "no_rawat");
            String user = bdrQueryValue(url, "usere");
            String pass = bdrQueryValue(url, "passwordte");

            /*
             * USERHYBRIDWEB/usere adalah akun service untuk akses web, bukan
             * user SIMRS yang sedang login. Kirim identitas user SIMRS secara
             * terpisah agar uploaded_by mencatat akun yang benar.
             */
            String loginUser = "";
            try {
                loginUser = akses.getkode();
            } catch (Exception e) {
                loginUser = "";
            }

            return base + "open_detail4.php?act=login"
                    + "&usere=" + bdrEncode(user)
                    + "&passwordte=" + bdrEncode(pass)
                    + "&login_user=" + bdrEncode(loginUser)
                    + "&no_rawat=" + bdrEncode(noRawat);
        }

        return url;
    }

    private boolean bdrBukaBerkasDigitalDiBrowser(String url) {
        try {
            String lower = (url == null ? "" : url).toLowerCase();

            if (lower.contains("/berkasrawat/login2.php")
                    || lower.contains("/berkasrawat/login2nonhapus.php")
                    || lower.contains("/berkasrawat/open_detail4.php")
                    || (lower.contains("/berkasrawat/index.php") && lower.contains("act=detail4"))) {

//                String targetUrl = bdrUbahKeOpenDetail4(url);
//                bdrDibukaDiBrowser = true;

                    /*
                     * Hanya generate report saat datang dari pemanggil asli Khanza.
                     * Reload open_detail4.php tidak akan membuat PDF berulang.
                     */
                    if (lower.contains("/berkasrawat/login2.php")
                            || lower.contains("/berkasrawat/login2nonhapus.php")) {

                        setCursor(
                            Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR
                            )
                        );

                        bdrSiapkanReportCache(url);

                        setCursor(
                            Cursor.getDefaultCursor()
                        );
                    }

                    String targetUrl =
                        bdrUbahKeOpenDetail4(url);

                    bdrDibukaDiBrowser = true;

                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(targetUrl));
                    dispose();
                    return true;
                }

                JOptionPane.showMessageDialog(null, "Browser bawaan tidak tersedia di komputer ini.");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Notif Buka Browser Detail4 : " + e);
            JOptionPane.showMessageDialog(null, "Gagal membuka Berkas Digital Perawatan di browser.\n" + e);
            return true;
        }

        return false;
    }

    public void loadURL(String url) {  
        if (bdrBukaBerkasDigitalDiBrowser(url)) {
            return;
        }

        try {
            createScene();
        } catch (Exception e) {
        }
        
        Platform.runLater(() -> {
            try {
                engine.load(url);
            }catch (Exception exception) {
                engine.load(url);
            }
        });        
    }    
    
    @Override
    public void setVisible(boolean b) {
        if (b && bdrDibukaDiBrowser) {
            return;
        }
        super.setVisible(b);
    }

    public void CloseScane(){
        Platform.setImplicitExit(false);
    }
    
    public void print(final Node node) {
        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
        double scaleX = pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth();
        double scaleY = pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight();
        node.getTransforms().add(new Scale(scaleX, scaleY));

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.printPage(node);
            if (success) {
                job.endJob();
            }
        }
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("::[ About Program ]::");
        setUndecorated(true);
        setResizable(false);
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout());
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        Platform.setImplicitExit(false);
    }//GEN-LAST:event_formWindowClosed

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        if(this.isActive()==false){
            Platform.setImplicitExit(false);
        }
    }//GEN-LAST:event_formWindowStateChanged

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgBerkasRawatDev dialog = new DlgBerkasRawatDev(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.InternalFrame internalFrame1;
    // End of variables declaration//GEN-END:variables

    public void setJudul(String Judul,String Pages){
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), Judul, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50))); 
        this.halaman=Pages;
    }
}
