package digital_klaim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import static javafx.concurrent.Worker.State.FAILED;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import rekammedis.RMRiwayatPerawatan;
import rekammedis.RMRiwayatPerawatanResumePDF;
import simrskhanza.DlgPilihanCetakDokumen;

/**
 *
 * @author perpustakaan
 */
public class ViewerKoding extends javax.swing.JDialog {

    private final DefaultTableModel tabModeDiagnosa, tabModeDiagnosaPilih, tabModeProsedur, tabModeProsedurPilih;
    private final JFXPanel jfxPanel = new JFXPanel();
    private WebEngine engine;
    private int i;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();
    private final JTextField txtURL = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();
    private final Properties prop = new Properties();
    private final validasi Valid = new validasi();
    private sekuel Sequel = new sekuel();
    private String status = "", norkmMedis = "", namaPasien, jamHsl, noorder = "", perujuk, halaman = "", norawat = "", auth, authEncrypt, requestJson, pemeriksaan, kdpenjab, kdpetugas, kamar, namakamar, pilihanCetak = "", kddokter="", Tanggal="", Jam="";
    private PreparedStatement ps, ps2;
    private ResultSet rs, rs2;
    private final validasi validasi = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private DlgPilihanCetakDokumen pilihan = new DlgPilihanCetakDokumen(null, false);
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root, subroot, subroot2, subresponse, subresponse2;
    private JsonNode nameNode;
    private JsonNode response, responsename;
    private SimpleDateFormat dateFormatMulai = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateReg = new SimpleDateFormat("yyyy-MM-dd");
    private String limitKunjungan = "", coder_nik = "", pilihpage = "", judulform = "", listOperasi = "";
    private String Verif="";

    public ViewerKoding(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);
        tabModeDiagnosaPilih = new DefaultTableModel(null, new Object[]{
            "Kode", "Nama Penyakit", "Status"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
//                if (colIndex == 0) {
//                    a = true;
//                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbDiagnosaPilih.setModel(tabModeDiagnosaPilih);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbDiagnosaPilih.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbDiagnosaPilih.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 3; i++) {
            TableColumn column = tbDiagnosaPilih.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(40);
            } else if (i == 1) {
                column.setPreferredWidth(400);
            } else if (i == 2) {
                column.setPreferredWidth(150);
            }
        }
        tbDiagnosaPilih.setDefaultRenderer(Object.class, new WarnaTableDiagnosa());

        tabModeDiagnosa = new DefaultTableModel(null, new Object[]{
            "P", "Kode", "Nama Penyakit", "Ciri-ciri Penyakit", "Keterangan", "Ktg.Penyakit", "Ciri-ciri Umum"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbDiagnosa.setModel(tabModeDiagnosa);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbDiagnosa.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbDiagnosa.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 7; i++) {
            TableColumn column = tbDiagnosa.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setPreferredWidth(40);
            } else if (i == 2) {
                column.setPreferredWidth(280);
            } else if (i == 3) {
                column.setPreferredWidth(285);
            } else if (i == 4) {
                column.setPreferredWidth(75);
            } else if (i == 5) {
                column.setPreferredWidth(75);
            } else if (i == 6) {
                column.setPreferredWidth(75);
            }
        }
        tbDiagnosa.setDefaultRenderer(Object.class, new WarnaTable());
        tabModeProsedurPilih = new DefaultTableModel(null, new Object[]{
            "Kode", "Nama Tindakan", "Status"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
//                if (colIndex == 0) {
//                    a = true;
//                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbProsedurPilih.setModel(tabModeProsedurPilih);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbProsedurPilih.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbProsedurPilih.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 3; i++) {
            TableColumn column = tbProsedurPilih.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(40);
            } else if (i == 1) {
                column.setPreferredWidth(400);
            } else if (i == 2) {
                column.setPreferredWidth(150);
            }
        }
//        tbProsedurPilih.setDefaultRenderer(Object.class, new WarnaTableDiagnosa());

        tabModeProsedur = new DefaultTableModel(null, new Object[]{
            "P", "Kode", "Deskripsi Panjang", "Deskripsi Pendek"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbProsedur.setModel(tabModeProsedur);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbProsedur.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbProsedur.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 4; i++) {
            TableColumn column = tbProsedur.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setPreferredWidth(50);
            } else if (i == 2) {
                column.setPreferredWidth(350);
            } else if (i == 3) {
                column.setPreferredWidth(350);
            }
        }
        tbProsedur.setDefaultRenderer(Object.class, new WarnaTable());

        TCariDiagnosaLive.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (TCariDiagnosaLive.getText().length() > 2) {
                    tampilDiagnosa();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (TCariDiagnosaLive.getText().length() > 2) {
                    tampilDiagnosa();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (TCariDiagnosaLive.getText().length() > 2) {
                    tampilDiagnosa();
                }
            }
        });
        TCariProsedur.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (TCariProsedur.getText().length() > 2) {
                    tampilProsedur();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (TCariProsedur.getText().length() > 2) {
                    tampilProsedur();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (TCariProsedur.getText().length() > 2) {
                    tampilProsedur();
                }
            }
        });   
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
                        ViewerKoding.this.setTitle(newValue);
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
                                    (value != null)
                                            ? engine.getLocation() + "\n" + value.getMessage()
                                            : engine.getLocation() + "\nUnexpected Catatan.",
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
                                prop.loadFromXML(new FileInputStream("setting/database.xml"));
                                if (engine.getLocation().replaceAll("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + prop.getProperty("PORTWEB") + "/" + prop.getProperty("HYBRIDWEB") + "/", "").contains("gbrpemeriksaan/pages")) {
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    Valid.panggilUrl(engine.getLocation().replaceAll("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + prop.getProperty("PORTWEB") + "/" + prop.getProperty("HYBRIDWEB") + "/gbrpemeriksaan/pages/upload/", "gbrpemeriksaan/").replaceAll("http://" + koneksiDB.HOSTHYBRIDWEB() + "/" + prop.getProperty("HYBRIDWEB") + "/gbrpemeriksaan/pages/upload/", "gbrpemeriksaan/"));
                                    engine.executeScript("history.back()");
                                    setCursor(Cursor.getDefaultCursor());
                                } else if (engine.getLocation().replaceAll("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + prop.getProperty("PORTWEB") + "/" + prop.getProperty("HYBRIDWEB") + "/", "").contains("Keluar")) {
                                    dispose();
                                } else if (engine.getLocation().replaceAll("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + prop.getProperty("PORTWEB") + "/" + prop.getProperty("HYBRIDWEB") + "/", "").contains("GABUNG")) {
                                    norawat = Sequel.cariIsi("select no_rawat from temppanggilnorawat");
                                    ps = koneksi.prepareStatement("SELECT berkas_digital_perawatan.lokasi_file "
                                            + "from berkas_digital_perawatan inner join master_berkas_digital "
                                            + "on berkas_digital_perawatan.kode=master_berkas_digital.kode "
                                            + "where berkas_digital_perawatan.no_rawat=? ORDER BY master_berkas_digital.nama ASC ");
                                    try {
                                        PDFMergerUtility ut = new PDFMergerUtility();
                                        URL url;
                                        ps.setString(1, norawat);
                                        rs = ps.executeQuery();
                                        while (rs.next()) {
                                            url = new URL("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + prop.getProperty("PORTWEB") + "/" + prop.getProperty("HYBRIDWEB") + "/berkasrawat/" + rs.getString("lokasi_file"));
                                            InputStream is = url.openStream();
                                            ut.addSource(is);
                                        }
                                        ut.setDestinationFileName("merge.pdf");
                                        ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                                        JOptionPane.showMessageDialog(null, "Proses gabung file selesai..!");
                                        Properties systemProp = System.getProperties();
                                        String currentDir = systemProp.getProperty("user.dir");
                                        File dir = new File(currentDir);
                                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                        Valid.panggilUrl2(dir + "/merge.pdf");
                                        setCursor(Cursor.getDefaultCursor());
                                    } catch (SQLException e) {
                                        System.out.println("Notif : " + e);
                                    } catch (IOException e) {
                                        System.out.println("Notif : " + e);
                                        JOptionPane.showMessageDialog(null, "Gagal menggabungkan file, cek kembali file apakah sudah dalam bentuk PDF.\nAtau cek kembali hak akses file di server dokumen..!!");
                                    } finally {
                                        if (rs != null) {
                                            rs.close();
                                        }
                                        if (ps != null) {
                                            ps.close();
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                System.out.println("Notifikasi : " + ex);
                            }
                        }
                    }
                });

                jfxPanel.setScene(new Scene(view));
            }
        });
        }
    
    private void updateTampilDiagnosaOtomatis() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        tampilDiagnosaPilih();
                        Thread.sleep(1000); // Tunggu 1 detik sebelum memperbarui data lagi
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    }
                }
            }
        });
        thread.start();

        }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupDiagnosa = new javax.swing.JPopupMenu();
        mnUbahPrimer = new javax.swing.JMenuItem();
        mnHapusSatu = new javax.swing.JMenuItem();
        mnHapusSemua = new javax.swing.JMenuItem();
        jPopupProsedur = new javax.swing.JPopupMenu();
        mnUbahPrimer1 = new javax.swing.JMenuItem();
        mnHapusSatu1 = new javax.swing.JMenuItem();
        mnHapusSemua1 = new javax.swing.JMenuItem();
        internalFrame3 = new widget.InternalFrame();
        internalFrame4 = new widget.InternalFrame();
        jPanel1 = new javax.swing.JPanel();
        internalFrame1 = new widget.InternalFrame();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        internalFrame19 = new widget.InternalFrame();
        internalFrame20 = new widget.InternalFrame();
        Scroll1 = new widget.ScrollPane();
        tbDiagnosa = new widget.Table();
        Scroll16 = new widget.ScrollPane();
        tbDiagnosaPilih = new widget.Table();
        panelBiasa12 = new widget.PanelBiasa();
        jLabel132 = new widget.Label();
        TCariDiagnosaLive = new widget.TextBox();
        internalFrame21 = new widget.InternalFrame();
        internalFrame22 = new widget.InternalFrame();
        Scroll2 = new widget.ScrollPane();
        tbProsedur = new widget.Table();
        Scroll17 = new widget.ScrollPane();
        tbProsedurPilih = new widget.Table();
        panelBiasa13 = new widget.PanelBiasa();
        jLabel133 = new widget.Label();
        TCariProsedur = new widget.TextBox();
        panelGlass8 = new widget.panelisi();
        Status = new widget.ComboBox();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        BtnRiwayatPerawatan = new widget.Button();
        BtnResume = new widget.Button();
        BtnKeluar = new widget.Button();

        jPopupDiagnosa.setName("jPopupDiagnosa"); // NOI18N

        mnUbahPrimer.setBackground(new java.awt.Color(255, 255, 254));
        mnUbahPrimer.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        mnUbahPrimer.setForeground(new java.awt.Color(50, 50, 50));
        mnUbahPrimer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        mnUbahPrimer.setText("Jadikan Primary");
        mnUbahPrimer.setToolTipText("");
        mnUbahPrimer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mnUbahPrimer.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mnUbahPrimer.setName("mnUbahPrimer"); // NOI18N
        mnUbahPrimer.setPreferredSize(new java.awt.Dimension(200, 26));
        mnUbahPrimer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnUbahPrimerActionPerformed(evt);
            }
        });
        jPopupDiagnosa.add(mnUbahPrimer);

        mnHapusSatu.setBackground(new java.awt.Color(255, 255, 254));
        mnHapusSatu.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        mnHapusSatu.setForeground(new java.awt.Color(50, 50, 50));
        mnHapusSatu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        mnHapusSatu.setText("Hapus Yang Dipilih");
        mnHapusSatu.setToolTipText("");
        mnHapusSatu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mnHapusSatu.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mnHapusSatu.setName("mnHapusSatu"); // NOI18N
        mnHapusSatu.setPreferredSize(new java.awt.Dimension(200, 26));
        mnHapusSatu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnHapusSatuActionPerformed(evt);
            }
        });
        jPopupDiagnosa.add(mnHapusSatu);

        mnHapusSemua.setBackground(new java.awt.Color(255, 255, 254));
        mnHapusSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        mnHapusSemua.setForeground(new java.awt.Color(50, 50, 50));
        mnHapusSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        mnHapusSemua.setText("Hapus Semua");
        mnHapusSemua.setToolTipText("");
        mnHapusSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mnHapusSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mnHapusSemua.setName("mnHapusSemua"); // NOI18N
        mnHapusSemua.setPreferredSize(new java.awt.Dimension(200, 26));
        mnHapusSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnHapusSemuaActionPerformed(evt);
            }
        });
        jPopupDiagnosa.add(mnHapusSemua);

        jPopupProsedur.setName("jPopupProsedur"); // NOI18N

        mnUbahPrimer1.setBackground(new java.awt.Color(255, 255, 254));
        mnUbahPrimer1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        mnUbahPrimer1.setForeground(new java.awt.Color(50, 50, 50));
        mnUbahPrimer1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        mnUbahPrimer1.setText("Jadikan Primary");
        mnUbahPrimer1.setToolTipText("");
        mnUbahPrimer1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mnUbahPrimer1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mnUbahPrimer1.setName("mnUbahPrimer1"); // NOI18N
        mnUbahPrimer1.setPreferredSize(new java.awt.Dimension(200, 26));
        mnUbahPrimer1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnUbahPrimer1ActionPerformed(evt);
            }
        });
        jPopupProsedur.add(mnUbahPrimer1);

        mnHapusSatu1.setBackground(new java.awt.Color(255, 255, 254));
        mnHapusSatu1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        mnHapusSatu1.setForeground(new java.awt.Color(50, 50, 50));
        mnHapusSatu1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        mnHapusSatu1.setText("Hapus Yang Dipilih");
        mnHapusSatu1.setToolTipText("");
        mnHapusSatu1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mnHapusSatu1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mnHapusSatu1.setName("mnHapusSatu1"); // NOI18N
        mnHapusSatu1.setPreferredSize(new java.awt.Dimension(200, 26));
        mnHapusSatu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnHapusSatu1ActionPerformed(evt);
            }
        });
        jPopupProsedur.add(mnHapusSatu1);

        mnHapusSemua1.setBackground(new java.awt.Color(255, 255, 254));
        mnHapusSemua1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        mnHapusSemua1.setForeground(new java.awt.Color(50, 50, 50));
        mnHapusSemua1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        mnHapusSemua1.setText("Hapus Semua");
        mnHapusSemua1.setToolTipText("");
        mnHapusSemua1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mnHapusSemua1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mnHapusSemua1.setName("mnHapusSemua1"); // NOI18N
        mnHapusSemua1.setPreferredSize(new java.awt.Dimension(200, 26));
        mnHapusSemua1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnHapusSemua1ActionPerformed(evt);
            }
        });
        jPopupProsedur.add(mnHapusSemua1);

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

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setPreferredSize(new java.awt.Dimension(500, 600));
        internalFrame3.setLayout(new java.awt.BorderLayout());

        internalFrame4.setBorder(null);
        internalFrame4.setName("internalFrame4"); // NOI18N
        internalFrame4.setPreferredSize(new java.awt.Dimension(500, 500));
        internalFrame4.setLayout(new java.awt.BorderLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(816, 500));
        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        internalFrame19.setBorder(null);
        internalFrame19.setName("internalFrame19"); // NOI18N
        internalFrame19.setLayout(new java.awt.BorderLayout(1, 1));

        internalFrame20.setName("internalFrame20"); // NOI18N
        internalFrame20.setLayout(new java.awt.GridLayout(1, 0));

        Scroll1.setBorder(javax.swing.BorderFactory.createTitledBorder("List Diagnosa"));
        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);

        tbDiagnosa.setName("tbDiagnosa"); // NOI18N
        tbDiagnosa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDiagnosaMouseClicked(evt);
            }
        });
        Scroll1.setViewportView(tbDiagnosa);

        internalFrame20.add(Scroll1);

        Scroll16.setBorder(javax.swing.BorderFactory.createTitledBorder("Diagnosa Dipilih"));
        Scroll16.setName("Scroll16"); // NOI18N
        Scroll16.setOpaque(true);

        tbDiagnosaPilih.setComponentPopupMenu(jPopupDiagnosa);
        tbDiagnosaPilih.setName("tbDiagnosaPilih"); // NOI18N
        Scroll16.setViewportView(tbDiagnosaPilih);

        internalFrame20.add(Scroll16);

        internalFrame19.add(internalFrame20, java.awt.BorderLayout.CENTER);

        panelBiasa12.setName("panelBiasa12"); // NOI18N
        panelBiasa12.setPreferredSize(new java.awt.Dimension(0, 40));
        panelBiasa12.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel132.setText("Cari ICD :");
        jLabel132.setName("jLabel132"); // NOI18N
        panelBiasa12.add(jLabel132);

        TCariDiagnosaLive.setName("TCariDiagnosaLive"); // NOI18N
        TCariDiagnosaLive.setPreferredSize(new java.awt.Dimension(250, 24));
        TCariDiagnosaLive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TCariDiagnosaLiveActionPerformed(evt);
            }
        });
        TCariDiagnosaLive.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariDiagnosaLiveKeyPressed(evt);
            }
        });
        panelBiasa12.add(TCariDiagnosaLive);

        internalFrame19.add(panelBiasa12, java.awt.BorderLayout.PAGE_END);

        jTabbedPane1.addTab("Diagnosa", internalFrame19);

        internalFrame21.setBorder(null);
        internalFrame21.setName("internalFrame21"); // NOI18N
        internalFrame21.setLayout(new java.awt.BorderLayout(1, 1));

        internalFrame22.setName("internalFrame22"); // NOI18N
        internalFrame22.setLayout(new java.awt.GridLayout(1, 0));

        Scroll2.setBorder(javax.swing.BorderFactory.createTitledBorder("List Prosedur"));
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbProsedur.setName("tbProsedur"); // NOI18N
        tbProsedur.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbProsedurMouseClicked(evt);
            }
        });
        Scroll2.setViewportView(tbProsedur);

        internalFrame22.add(Scroll2);

        Scroll17.setBorder(javax.swing.BorderFactory.createTitledBorder("Prosedur Dipilih"));
        Scroll17.setName("Scroll17"); // NOI18N
        Scroll17.setOpaque(true);

        tbProsedurPilih.setComponentPopupMenu(jPopupProsedur);
        tbProsedurPilih.setName("tbProsedurPilih"); // NOI18N
        Scroll17.setViewportView(tbProsedurPilih);

        internalFrame22.add(Scroll17);

        internalFrame21.add(internalFrame22, java.awt.BorderLayout.CENTER);

        panelBiasa13.setName("panelBiasa13"); // NOI18N
        panelBiasa13.setPreferredSize(new java.awt.Dimension(0, 40));
        panelBiasa13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel133.setText("Cari ICD :");
        jLabel133.setName("jLabel133"); // NOI18N
        panelBiasa13.add(jLabel133);

        TCariProsedur.setName("TCariProsedur"); // NOI18N
        TCariProsedur.setPreferredSize(new java.awt.Dimension(250, 24));
        TCariProsedur.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariProsedurKeyPressed(evt);
            }
        });
        panelBiasa13.add(TCariProsedur);

        internalFrame21.add(panelBiasa13, java.awt.BorderLayout.PAGE_END);

        jTabbedPane1.addTab("Prosedur", internalFrame21);

        internalFrame1.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(internalFrame1);

        internalFrame4.add(jPanel1, java.awt.BorderLayout.CENTER);

        internalFrame3.add(internalFrame4, java.awt.BorderLayout.CENTER);

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(50, 50));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ralan", "Ranap" }));
        Status.setName("Status"); // NOI18N
        Status.setPreferredSize(new java.awt.Dimension(85, 23));
        Status.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StatusActionPerformed(evt);
            }
        });
        panelGlass8.add(Status);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        Tgl1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl1KeyPressed(evt);
            }
        });
        panelGlass8.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass8.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl2KeyPressed(evt);
            }
        });
        panelGlass8.add(Tgl2);

        BtnRiwayatPerawatan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Prescription.png"))); // NOI18N
        BtnRiwayatPerawatan.setMnemonic('S');
        BtnRiwayatPerawatan.setText("Riwayat Perawatan");
        BtnRiwayatPerawatan.setToolTipText("Alt+S");
        BtnRiwayatPerawatan.setName("BtnRiwayatPerawatan"); // NOI18N
        BtnRiwayatPerawatan.setPreferredSize(new java.awt.Dimension(180, 30));
        BtnRiwayatPerawatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRiwayatPerawatanActionPerformed(evt);
            }
        });
        BtnRiwayatPerawatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnRiwayatPerawatanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnRiwayatPerawatan);

        BtnResume.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Prescription.png"))); // NOI18N
        BtnResume.setMnemonic('S');
        BtnResume.setText("Riwayat Perawatan Resume");
        BtnResume.setToolTipText("Alt+S");
        BtnResume.setName("BtnResume"); // NOI18N
        BtnResume.setPreferredSize(new java.awt.Dimension(220, 30));
        BtnResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnResumeActionPerformed(evt);
            }
        });
        BtnResume.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnResumeKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnResume);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        internalFrame3.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame3, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        Platform.setImplicitExit(false);
    }//GEN-LAST:event_formWindowClosed

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        if (this.isActive() == false) {
            Platform.setImplicitExit(false);
        }
    }//GEN-LAST:event_formWindowStateChanged

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        noorder = "";
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
        }
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void tbDiagnosaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDiagnosaMouseClicked
        if (tabModeDiagnosa.getRowCount() != 0) {
            try {
                for (i = 0; i < tbDiagnosa.getRowCount(); i++) {
                    if (tbDiagnosa.getValueAt(i, 0).toString().equals("true")) {
                        String kdPenyakit = tbDiagnosa.getValueAt(i, 1).toString();
                        String status = Status.getSelectedItem().toString();
                        int prioritas = Sequel.cariInteger("select ifnull(MAX(diagnosa_pasien.prioritas)+1,1) from diagnosa_pasien where diagnosa_pasien.no_rawat=? and diagnosa_pasien.status=?", norawat, status);

                        if (Sequel.cariInteger("select count(diagnosa_pasien.kd_penyakit) from diagnosa_pasien where diagnosa_pasien.no_rawat=? and diagnosa_pasien.kd_penyakit=?", norawat, kdPenyakit) > 0) {
                            Sequel.menyimpan("diagnosa_pasien", "?,?,?,?,?", "Penyakit", 5, new String[]{ norawat, kdPenyakit, status, String.valueOf(prioritas), "Lama" });
                        } else {
                            Sequel.menyimpan("diagnosa_pasien", "?,?,?,?,?", "Penyakit", 5, new String[]{ norawat, kdPenyakit, status, String.valueOf(prioritas), "Baru" });
                        }
                    }
                }
                tampilDiagnosa();
                tampilDiagnosaPilih();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

    }//GEN-LAST:event_tbDiagnosaMouseClicked

    private void TCariDiagnosaLiveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariDiagnosaLiveKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TCariDiagnosaLiveKeyPressed

    private void tbProsedurMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbProsedurMouseClicked
        if (tabModeProsedur.getRowCount() != 0) {
            try {
                for (i = 0; i < tbProsedur.getRowCount(); i++) {
                    if (tbProsedur.getValueAt(i, 0).toString().equals("true")) {
                        String kdProsedur = tbProsedur.getValueAt(i, 1).toString();
                        String status = Status.getSelectedItem().toString();
                        int prioritas = Sequel.cariInteger("select ifnull(MAX(prosedur_pasien.prioritas)+1,1) from prosedur_pasien where prosedur_pasien.no_rawat=? and prosedur_pasien.status=?", norawat, status);

                        Sequel.menyimpan("prosedur_pasien", "?,?,?,?", "ICD 9", 4, new String[]{ norawat, kdProsedur, status, String.valueOf(prioritas) });
                    }
                }
                tampilProsedur();
                tampilProsedurPilih();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

    }//GEN-LAST:event_tbProsedurMouseClicked

    private void TCariProsedurKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariProsedurKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TCariProsedurKeyPressed

    private void mnUbahPrimerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnUbahPrimerActionPerformed
        int selectedRow = tbDiagnosaPilih.getSelectedRow(); 
        if (selectedRow >= 0) { 
            String kdPenyakit = tbDiagnosaPilih.getValueAt(selectedRow, 0).toString(); 
            String status = Status.getSelectedItem().toString(); 
            if (status != null) { 
                int prioritasOld = Sequel.cariInteger("select prioritas from diagnosa_pasien where status=? and no_rawat=? and kd_penyakit=?", status, norawat, kdPenyakit); 
                if (prioritasOld > 0) { 
                    try { 
                        Sequel.queryu("Update diagnosa_pasien set prioritas='" + prioritasOld + "' where status='" + status + "' and no_rawat='" + norawat + "' and prioritas='1'"); 
                        Sequel.queryu("Update diagnosa_pasien set prioritas='1' where status='" + status + "' and no_rawat='" + norawat + "' and kd_penyakit='" + kdPenyakit + "'"); 
                    } catch (Exception e) { 
                        System.out.println("Error: " + e.getMessage()); 
                    } 
                } 
            } 
        } 
        tampilDiagnosa(); 
        tampilDiagnosaPilih(); 
    }//GEN-LAST:event_mnUbahPrimerActionPerformed

    private void mnHapusSatuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnHapusSatuActionPerformed
        Sequel.queryu("delete from diagnosa_pasien where no_rawat='" + norawat + "' and kd_penyakit='" + tbDiagnosaPilih.getValueAt(tbDiagnosaPilih.getSelectedRow(), 0).toString() + "'  ");
        tampilDiagnosa();
        tampilDiagnosaPilih();
    }//GEN-LAST:event_mnHapusSatuActionPerformed

    private void mnHapusSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnHapusSemuaActionPerformed
        for (i = 0; i < tbDiagnosaPilih.getRowCount(); i++) {
            Sequel.queryu("delete from diagnosa_pasien where no_rawat='" + norawat + "' and kd_penyakit='" + tbDiagnosaPilih.getValueAt(i, 0).toString() + "'  ");
        }
        tampilDiagnosa();
        tampilDiagnosaPilih();
    }//GEN-LAST:event_mnHapusSemuaActionPerformed

    private void mnUbahPrimer1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnUbahPrimer1ActionPerformed
        String kodeProsedur = tbProsedurPilih.getValueAt(tbProsedurPilih.getSelectedRow(), 0).toString(); 
        String status = Status.getSelectedItem().toString(); 
        int prioritasOld = Sequel.cariInteger("select prioritas from prosedur_pasien where status=? and no_rawat=? and kode=?", status, norawat, kodeProsedur); 
        try { 
            Sequel.queryu("Update prosedur_pasien set prioritas='" + prioritasOld + "' where status='" + status + "' and no_rawat='" + norawat + "' and prioritas='1'"); 
            Sequel.queryu("Update prosedur_pasien set prioritas='1' where status='" + status + "' and no_rawat='" + norawat + "' and kode='" + kodeProsedur + "'"); 
        } catch (Exception e) { 
            System.out.println("Error: " + e.getMessage()); 
        } 
        tampilProsedur(); 
        tampilProsedurPilih();
    }//GEN-LAST:event_mnUbahPrimer1ActionPerformed

    private void mnHapusSatu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnHapusSatu1ActionPerformed
        Sequel.queryu("delete from prosedur_pasien where no_rawat='" + norawat + "' and kode='" + tbProsedurPilih.getValueAt(tbProsedurPilih.getSelectedRow(), 0).toString() + "'  ");
        tampilProsedur();
        tampilProsedurPilih();
    }//GEN-LAST:event_mnHapusSatu1ActionPerformed

    private void mnHapusSemua1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnHapusSemua1ActionPerformed
        for (i = 0; i < tbProsedurPilih.getRowCount(); i++) {
            Sequel.queryu("delete from prosedur_pasien where no_rawat='" + norawat + "' and kode='" + tbProsedurPilih.getValueAt(i, 0).toString() + "'  ");
        }
        tampilProsedur();
        tampilProsedurPilih();
    }//GEN-LAST:event_mnHapusSemua1ActionPerformed
                  
    private void BtnResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnResumeActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//            DlgResumePerawatan resume=new DlgResumePerawatan(null,true);
        RMRiwayatPerawatanResumePDF resume = new RMRiwayatPerawatanResumePDF(null, true);
        Date tgl1 = Tgl1.getDate();
        Date tgl2 = Tgl2.getDate();
        resume.setNoRawat(norkmMedis, namaPasien, norawat, tgl1, tgl2);
        resume.setDokter(kddokter, Tanggal, Jam, norawat,norkmMedis);
        resume.setSize(internalFrame3.getWidth(), internalFrame3.getHeight());
        resume.setLocationRelativeTo(internalFrame3);
        resume.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
        resume.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                tampilDiagnosaPilih();
                tampilProsedurPilih();
            }
        });  
    }//GEN-LAST:event_BtnResumeActionPerformed

    private void BtnResumeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnResumeKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnResumeKeyPressed

    private void TCariDiagnosaLiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TCariDiagnosaLiveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TCariDiagnosaLiveActionPerformed

    private void BtnRiwayatPerawatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnRiwayatPerawatanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnRiwayatPerawatanKeyPressed

    private void BtnRiwayatPerawatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRiwayatPerawatanActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //            DlgResumePerawatan resume=new DlgResumePerawatan(null,true);
        RMRiwayatPerawatan resume = new RMRiwayatPerawatan(null, true);
        resume.setNoRm(norkmMedis, namaPasien);
        resume.setSize(internalFrame3.getWidth(), internalFrame3.getHeight());
        resume.setLocationRelativeTo(internalFrame3);
        resume.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
        resume.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                tampilDiagnosaPilih();
                tampilProsedurPilih();
            }
        });        
    }//GEN-LAST:event_BtnRiwayatPerawatanActionPerformed

    private void StatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_StatusActionPerformed

    private void Tgl1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl1KeyPressed
        Valid.pindah(evt, BtnKeluar, Tgl2);
    }//GEN-LAST:event_Tgl1KeyPressed

    private void Tgl2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl2KeyPressed
        Valid.pindah(evt, Tgl1,Status);
    }//GEN-LAST:event_Tgl2KeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            ViewerKoding dialog = new ViewerKoding(new javax.swing.JFrame(), true);
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
    private widget.Button BtnKeluar;
    private widget.Button BtnResume;
    private widget.Button BtnRiwayatPerawatan;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll16;
    private widget.ScrollPane Scroll17;
    private widget.ScrollPane Scroll2;
    private widget.ComboBox Status;
    private widget.TextBox TCariDiagnosaLive;
    private widget.TextBox TCariProsedur;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame19;
    private widget.InternalFrame internalFrame20;
    private widget.InternalFrame internalFrame21;
    private widget.InternalFrame internalFrame22;
    private widget.InternalFrame internalFrame3;
    private widget.InternalFrame internalFrame4;
    private widget.Label jLabel132;
    private widget.Label jLabel133;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupDiagnosa;
    private javax.swing.JPopupMenu jPopupProsedur;
    private javax.swing.JTabbedPane jTabbedPane1;
    private widget.Label label18;
    private javax.swing.JMenuItem mnHapusSatu;
    private javax.swing.JMenuItem mnHapusSatu1;
    private javax.swing.JMenuItem mnHapusSemua;
    private javax.swing.JMenuItem mnHapusSemua1;
    private javax.swing.JMenuItem mnUbahPrimer;
    private javax.swing.JMenuItem mnUbahPrimer1;
    private widget.PanelBiasa panelBiasa12;
    private widget.PanelBiasa panelBiasa13;
    private widget.panelisi panelGlass8;
    public widget.Table tbDiagnosa;
    public widget.Table tbDiagnosaPilih;
    public widget.Table tbProsedur;
    public widget.Table tbProsedurPilih;
    // End of variables declaration//GEN-END:variables

    public void setJudul(String Judul, String Pages) {
//        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), Judul, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(70, 70, 70)));
        this.halaman = Pages;
    }

    public void setNoOrder(String noOrder) {
        this.noorder = noOrder;
    }

    void uploadPdf(String FileName, String docpath) {
        try {
            File file = new File("tempFile/" + FileName);
            byte[] data = new byte[(int) file.length()];
            data = FileUtils.readFileToByteArray(file);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/webapps/berkasdigital/upload.php?doc=" + docpath);
            ByteArrayBody fileData = new ByteArrayBody(data, FileName);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", fileData);
            postRequest.setEntity(reqEntity);
            HttpResponse response = (HttpResponse) httpClient.execute(postRequest);
//            System.out.println("Cookie: " + response);
//            deleteFile();
        } catch (Exception e) {
            System.out.println("Error Upload" + e);
        }
    }

    private void deleteFile() {
        File file = new File("tempFile");
        String[] myFiles;
        if (file.isDirectory()) {
            myFiles = file.list();
            for (int i = 0; i < myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]);
                myFile.delete();
            }
        }
    }

    private void saveFileNameBerkas(String noRawat, String JenisFile, String NamaFile) {

        if (Sequel.cariInteger("Select count(no_rawat) from tt_berkasdigital where jenis_file='radiologi' and no_rawat='" + noRawat + "'") > 0) {

        } else {
            Sequel.menyimpantf2("tt_berkasdigital", "?,?,?", "No.Rawat", 3,
                    new String[]{noRawat, JenisFile, NamaFile});
        }

    }

    public void setDataPasien(String noRawat, String noRkmMedis, String nmPasien, String statusKunjungan) {
        this.norawat = noRawat;
        this.norkmMedis = noRkmMedis;
        this.namaPasien = nmPasien;
        this.status = statusKunjungan;
        tampilDiagnosaPilih();
        tampilProsedurPilih();
    }
    public void setDataPasien2(String noRawat, String noRkmMedis, String nmPasien,Date tgl1,Date tgl2, String statusKunjungan) {
        this.norawat = noRawat;
        this.norkmMedis = noRkmMedis;
        this.namaPasien = nmPasien;
        Tgl1.setDate(tgl1);
        Tgl2.setDate(tgl2); 
        Status.setSelectedItem(statusKunjungan); 
        this.status = statusKunjungan;        
        tampilDiagnosaPilih();
        tampilProsedurPilih();
    }    

    private void tampilDiagnosa() {
        Valid.tabelKosong(tabModeDiagnosa);
        try {
            ps = koneksi.prepareStatement("select penyakit.kd_penyakit,penyakit.nm_penyakit,penyakit.ciri_ciri,penyakit.keterangan, "
                    + "kategori_penyakit.nm_kategori,kategori_penyakit.ciri_umum "
                    + "from  kategori_penyakit inner join penyakit "
                    + "on penyakit.kd_ktg=kategori_penyakit.kd_ktg where   NOT EXISTS (select kd_penyakit from diagnosa_pasien  where penyakit.kd_penyakit=diagnosa_pasien.kd_penyakit and no_rawat='" + norawat + "') and ("
                    + " penyakit.kd_penyakit like ? or "
                    + " penyakit.nm_penyakit like ? or "
                    + " penyakit.ciri_ciri like ? or "
                    + " penyakit.keterangan like ? or "
                    + " kategori_penyakit.nm_kategori like ? or "
                    + " kategori_penyakit.ciri_umum like ? or "
                    + " REPLACE(penyakit.kd_penyakit,'.','') like ? )  "
                    + "order by penyakit.kd_penyakit  LIMIT 100");
            try {
                ps.setString(1, "%" + TCariDiagnosaLive.getText().trim() + "%");
                ps.setString(2, "%" + TCariDiagnosaLive.getText().trim() + "%");
                ps.setString(3, "%" + TCariDiagnosaLive.getText().trim() + "%");
                ps.setString(4, "%" + TCariDiagnosaLive.getText().trim() + "%");
                ps.setString(5, "%" + TCariDiagnosaLive.getText().trim() + "%");
                ps.setString(6, "%" + TCariDiagnosaLive.getText().trim() + "%");
                ps.setString(7, "%" + TCariDiagnosaLive.getText().trim() + "%");
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabModeDiagnosa.addRow(new Object[]{false, rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6)});
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilProsedur() {
        Valid.tabelKosong(tabModeProsedur);
        try {
            ps = koneksi.prepareStatement("select * "
                    + "from   icd9 "
                    + " where   NOT EXISTS (select kode from prosedur_pasien  where icd9.kode=prosedur_pasien.kode and no_rawat='" + norawat + "') and ( kode like ? or "
                    + " deskripsi_panjang like ? or  deskripsi_pendek like ? or  REPLACE(kode,'.','') like ? )  "
                    + " order by kode LIMIT 100");
            try {
                ps.setString(1, "%" + TCariProsedur.getText().trim() + "%");
                ps.setString(2, "%" + TCariProsedur.getText().trim() + "%");
                ps.setString(3, "%" + TCariProsedur.getText().trim() + "%");
                ps.setString(4, "%" + TCariProsedur.getText().trim() + "%");
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabModeProsedur.addRow(new Object[]{false, rs.getString(1),
                        rs.getString(2),
                        rs.getString(3)});
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilDiagnosaPilih() {
        Valid.tabelKosong(tabModeDiagnosaPilih);
        try {
            ps = koneksi.prepareStatement("select reg_periksa.tgl_registrasi,diagnosa_pasien.no_rawat,reg_periksa.no_rkm_medis,concat(pasien.nm_pasien,' [ ',reg_periksa.umurdaftar,' ',reg_periksa.sttsumur,' ]') ,"
                    + "diagnosa_pasien.kd_penyakit,penyakit.nm_penyakit, diagnosa_pasien.status,diagnosa_pasien.status_penyakit,diagnosa_pasien.prioritas "
                    + "from diagnosa_pasien inner join reg_periksa inner join pasien inner join penyakit "
                    + "on diagnosa_pasien.no_rawat=reg_periksa.no_rawat and reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "and diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                    + "where reg_periksa.no_rawat=?  order by diagnosa_pasien.prioritas ASC");
            try {
                ps.setString(1, norawat.trim());
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabModeDiagnosaPilih.addRow(new Object[]{rs.getString("kd_penyakit"),
                        rs.getString("nm_penyakit"),
                        (rs.getString("prioritas").equals("1") ? "Primary" : "Secondary")});
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilProsedurPilih() {
        Valid.tabelKosong(tabModeProsedurPilih);
        try {
            ps = koneksi.prepareStatement("select * "
                    + "from   prosedur_pasien JOIN icd9 ON prosedur_pasien.kode=icd9.kode "
                    + " where no_rawat ='" + norawat + "' order by prosedur_pasien.prioritas ASC ");
            try {

                rs = ps.executeQuery();
                while (rs.next()) {
                    tabModeProsedurPilih.addRow(new Object[]{rs.getString("kode"),
                        rs.getString("deskripsi_pendek"),
                        (rs.getString("prioritas").equals("1") ? "Primary" : "Secondary")});
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

}
