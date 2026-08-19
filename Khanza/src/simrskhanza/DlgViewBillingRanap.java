package simrskhanza;
/*
 * DlgViewBilling* — Viewer billing via Web (proxy) + Simpan ke Berkas Digital (Gambar)
 * - Fix JavaFX threading (JFXPanel + Platform.runLater)
 * - Support proxy: /webapps/billing/proxy_view.php?target=...
 * - Optional pdf.js wrapper (setForcePdfJs(true))
 * - Plan-B: ambil PDF langsung -> render JPG (PDFBox) -> upload ke berkas_digital_perawatan (kode=144)
 */

import fungsi.koneksiDB;
import fungsi.sekuel;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;


public class DlgViewBillingRanap extends javax.swing.JDialog {
    static {
        try { new JFXPanel(); } catch (Throwable ignore) {}
        try { Platform.setImplicitExit(false); } catch (Throwable ignore) {}
    }
    
    private sekuel Sequel=new sekuel();    
    private Connection koneksi=koneksiDB.condb(); 
    
    private JPanel toolbar;
    private JLabel lblNoRawat;
    private JTextField tfNoRawat;
    private JButton btnTampil, btnReload, btnOpenBrowser, btnSaveBerkas;
    private JPanel panelWeb;

    private JFXPanel fxPanel;
    private WebEngine engine;
    private volatile boolean fxReady = false;
    private String pendingUrl = null;
    private String lastReportUrl = null;

    private String baseWebapps = "http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB();
    private String userWeb = koneksiDB.USERHYBRIDWEB();
    private String passWeb = koneksiDB.PASHYBRIDWEB();
    private boolean forcePdfJs = false;
    
    

    public DlgViewBillingRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initFx();
        setTitle("Laporan Billing Ranap (Web)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(parent);
    }
    public DlgViewBillingRanap(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initFx();
        setTitle("Laporan Billing Ranap (Web)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(parent);
    }

    public void setBaseWebapps(String s) { if (s!=null && !s.isEmpty()) baseWebapps = s; }
    public void setWebCredentials(String u, String p) { if (u!=null) userWeb=u; if (p!=null) passWeb=p; }
    public void setForcePdfJs(boolean b) { forcePdfJs=b; }
    public void setNoRawat(String nr) { tfNoRawat.setText(nr); tampilByNoRawat(nr); }

    private void initComponents() {
        toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        lblNoRawat = new JLabel("no_rawat:");
        tfNoRawat = new JTextField(22);
        btnTampil = new JButton("Tampilkan (no_rawat)");
        btnReload = new JButton("Reload");
        btnOpenBrowser = new JButton("Buka di Browser");
        btnSaveBerkas = new JButton("Simpan ke Berkas Digital (Gambar)");

        toolbar.add(lblNoRawat); toolbar.add(tfNoRawat); toolbar.add(btnTampil);
        toolbar.add(btnReload); toolbar.add(btnOpenBrowser); toolbar.add(btnSaveBerkas);

        panelWeb = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(panelWeb, BorderLayout.CENTER);

        btnTampil.addActionListener(e -> {
            String nr = tfNoRawat.getText()!=null?tfNoRawat.getText().trim():"";
            if(nr.isEmpty()){ JOptionPane.showMessageDialog(this,"Isi no_rawat dulu."); return; }
            tampilByNoRawat(nr);
        });
        btnReload.addActionListener(e -> fxReload());
        btnOpenBrowser.addActionListener(e -> {
            if(lastReportUrl!=null){ try{ Desktop.getDesktop().browse(new URI(lastReportUrl)); }catch(Exception ex){ JOptionPane.showMessageDialog(this,"Gagal buka browser: "+ex.getMessage()); } }
        });
        btnSaveBerkas.addActionListener(e -> simpanGambarKeBerkas());
    }

    private void initFx() {
        fxPanel = new JFXPanel();
        panelWeb.add(fxPanel, BorderLayout.CENTER);
        Platform.runLater(() -> {
            WebView view = new WebView();
            engine = view.getEngine();
            fxPanel.setScene(new Scene(view));
            fxReady = true;
            if(pendingUrl!=null){ engine.load(pendingUrl); pendingUrl=null; }
        });
    }
    private void fxLoad(String url) {
        if(!fxReady || engine==null){ pendingUrl=url; return; }
        Platform.runLater(() -> { try{ engine.load(url); }catch(Exception e){ e.printStackTrace(); } });
    }
    private void fxReload() { if(fxReady && engine!=null) Platform.runLater(() -> engine.reload()); }

    private String urlenc(String s){ try{ return URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8.name()); }catch(Exception e){ return s; } }
    private String buildFinalUrl(String raw){ return forcePdfJs ? baseWebapps + "/pdfjs/web/viewer.html?file="+urlenc(raw)+"#zoom=page-width" : raw; }

    private void tampilByNoRawat(String nr) {
        if(nr==null || nr.trim().isEmpty()) return;
        String raw = baseWebapps + "/billing/proxy_view.php"
                + "?target=" + urlenc("LaporanBillingRanap.php")
                + "&usere=" + urlenc(userWeb)
                + "&passwordte=" + urlenc(passWeb)
                + "&no_rawat=" + urlenc(nr)
                + "&norawat=" + urlenc(nr);
        String url = buildFinalUrl(raw);
        lastReportUrl = url;
        fxLoad(url);
    }

    private void simpanGambarKeBerkas() {
        String nr = tfNoRawat.getText()!=null?tfNoRawat.getText().trim():"";
        if(nr.isEmpty()){ JOptionPane.showMessageDialog(this,"Isi no_rawat dulu."); return; }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            try{
                int sukses = fetchPdfAndUploadAsJpg(nr, true);
                if(sukses>0){ int ok=sukses; SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,"Upload Billing selesai ("+ok+" halaman).")); }
                else if(sukses==0){ SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,"Sudah pernah diupload (hal 1 terdeteksi).")); }
                else { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,"Gagal ambil billing (lihat browser).","Gagal",JOptionPane.ERROR_MESSAGE)); }
            } finally { SwingUtilities.invokeLater(() -> setCursor(Cursor.getDefaultCursor())); }
        }).start();
    }

    private int fetchPdfAndUploadAsJpg(String noRawat, boolean ranap) {
        try{
            String raw = baseWebapps + "/billing/proxy_view.php"
                    + "?target=" + urlenc(ranap ? "LaporanBillingRanap.php" : "LaporanBillingRalan.php")
                    + "&usere=" + urlenc(userWeb)
                    + "&passwordte=" + urlenc(passWeb)
                    + "&no_rawat=" + urlenc(noRawat)
                    + "&norawat=" + urlenc(noRawat);
            byte[] pdfBytes = httpGetBytes(raw);
            if(pdfBytes==null || pdfBytes.length<4 || pdfBytes[0] != '%' || pdfBytes[1] != 'P'){ 
                try{ Desktop.getDesktop().browse(new URI(raw)); }catch(Exception ig){}
                throw new RuntimeException("Billing bukan PDF / ditolak server.");
            }
            String baseName = (ranap ? "BILLING_RANAP_" : "BILLING_RALAN_") + sanitize(noRawat)
                    + "_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            File tmpDir = new File("tempPDF"); if(!tmpDir.exists()) tmpDir.mkdirs();
            File tmpPdf = new File(tmpDir, baseName + ".pdf");
            Files.write(tmpPdf.toPath(), pdfBytes);

            java.util.List<File> jpgs = convertPdfToJpgMulti(tmpPdf, baseName);

            String lokasiPage1 = "pages/upload/" + baseName + "_hal_1.jpg";
            int sudahAda = 0;
            try{ sudahAda = Sequel.cariInteger("SELECT COUNT(*) FROM berkas_digital_perawatan WHERE no_rawat=? AND kode=? AND lokasi_file=?", noRawat, "144", lokasiPage1); }catch(Exception ig){}
            if(sudahAda>0){ cleanupTmpJpg(baseName); try{ tmpPdf.delete(); }catch(Exception ig){} return 0; }

            int sukses=0; final String docpath="berkasrawat/pages/upload/";
            for(File jpg : jpgs){
                if(uploadOneJpg(jpg, docpath)){
                    String lokasi = "pages/upload/" + jpg.getName();
                    boolean ok = Sequel.menyimpantf("berkas_digital_perawatan","?,?,?","No.Rawat",3,new String[]{noRawat,"144",lokasi});
                    if(ok) sukses++;
                }
            }
            cleanupTmpJpg(baseName); try{ tmpPdf.delete(); }catch(Exception ig){}
            return sukses;
        }catch(Exception e){
            System.out.println("fetchPdfAndUploadAsJpg ERR: "+e.getMessage());
            return -1;
        }
    }

    private byte[] httpGetBytes(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(60000);
        c.setRequestMethod("GET");
        c.setRequestProperty("Accept","application/pdf, */*;q=0.8");
        try(InputStream in = c.getInputStream()){ return in.readAllBytes(); }
    }

private String sanitize(String s) {
    if (s == null) return "";
    // hanya izinkan huruf, angka, underscore, dan minus
    return s.replaceAll("[^0-9A-Za-z_-]", "_");
}


    private java.util.List<File> convertPdfToJpgMulti(File pdfFile, String baseName) throws Exception {
        java.util.List<File> out = new ArrayList<>();
        try(PDDocument doc = PDDocument.load(pdfFile)){
            PDFRenderer renderer = new PDFRenderer(doc);
            File dir = new File("tmpJPG"); if(!dir.exists()) dir.mkdir();
            int pages = doc.getNumberOfPages();
            for(int i=0;i<pages;i++){ 
                BufferedImage img = renderer.renderImageWithDPI(i,300,ImageType.RGB);
                String name = baseName + "_hal_" + (i+1) + ".jpg";
                File fout = new File(dir,name);
                ImageIO.write(img, "jpg", fout);
                out.add(fout);
            }
        }
        return out;
    }
    private void cleanupTmpJpg(String baseName){
        File dir = new File("tmpJPG"); if(!dir.exists()) return;
        File[] list = dir.listFiles((d,n)-> n!=null && n.startsWith(baseName+"_hal_") && n.endsWith(".jpg"));
        if(list==null) return;
        for(File f: list){ try{ f.delete(); }catch(Exception e){} }
    }

    private boolean uploadOneJpg(File jpg, String docpath){
        String url = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/upload.php?doc=" + docpath;
        String boundary = "----RSBoundary" + System.nanoTime();
        try{
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);
            try(OutputStream out = conn.getOutputStream(); FileInputStream fis = new FileInputStream(jpg)){
                String hdr = "--"+boundary+"\r\n"+"Content-Disposition: form-data; name=\"file\"; filename=\""+jpg.getName()+"\"\r\n"+"Content-Type: image/jpeg\r\n\r\n";
                out.write(hdr.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                fis.transferTo(out);
                out.write("\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                String end = "--"+boundary+"--\r\n";
                out.write(end.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if(code>=200 && code<300){ try(InputStream is=conn.getInputStream()){ if(is!=null) is.readAllBytes(); } return true; }
            else { try(InputStream es=conn.getErrorStream()){ if(es!=null) es.readAllBytes(); } return false; }
        }catch(Exception e){ System.out.println("uploadOneJpg err: "+e.getMessage()); return false; }
    }
}
