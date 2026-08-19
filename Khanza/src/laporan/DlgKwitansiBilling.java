package laporan;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
//import net.sf.jasperreports.view.JRViewer;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.swing.JRViewer;

public class DlgKwitansiBilling extends JDialog {

    private final Connection koneksi;

    public DlgKwitansiBilling(Window owner, Connection conn) {
        super(owner, "Kwitansi Billing", ModalityType.MODELESS);
        this.koneksi = conn;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
    }

    public void tampil(String noRawat,
                       String namaRS,
                       String alamatRS,
                       String kontakRS,
                       InputStream logo,
                       String payer,
                       BigDecimal ppn,
                       String namaPetugas,
                       InputStream ttdPetugas) {

        try {
            BigDecimal grand = cariGrandTotal(noRawat);
            String terbilang = terbilangID(grand.longValue()) + " Rupiah";
            String lokasiTgl = "Makassar , " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HHmmss"));

            Map<String, Object> params = new HashMap<>();
            params.put("P_NO_RAWAT", noRawat);
            params.put("P_NAMA_RS", namaRS);
            params.put("P_ALAMAT_RS", alamatRS);
            params.put("P_KONTAK_RS", kontakRS);
            params.put("P_LOGO", logo);
            params.put("P_PAYER", payer == null ? "BPJS Kesehatan" : payer);
            params.put("P_PPN", ppn == null ? BigDecimal.ZERO : ppn);
            params.put("P_TERBILANG", terbilang);
            params.put("P_LOKASI_TGL", lokasiTgl);
            params.put("P_NAMA_PETUGAS", namaPetugas);
            params.put("P_TTD", ttdPetugas);

            JasperReport jasper;
            InputStream jasperBin = getClass().getResourceAsStream("/report/rptKwitansiBilling_Billing.jasper");
            if (jasperBin != null) {
                jasper = (JasperReport) JRLoader.loadObject(jasperBin);
            } else {
                InputStream jrxml = getClass().getResourceAsStream("/report/rptKwitansiBilling_Billing.jrxml");
                if (jrxml == null) {
                    throw new RuntimeException("Report tidak ditemukan di /report/rptKwitansiBilling_Billing.(jasper|jrxml)");
                }
                jasper = JasperCompileManager.compileReport(jrxml);
            }

            JasperPrint print = JasperFillManager.fillReport(jasper, params, koneksi);
            getContentPane().removeAll();
            getContentPane().add(new JRViewer(print), BorderLayout.CENTER);
            revalidate();
            setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Gagal menampilkan kwitansi:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal cariGrandTotal(String noRawat) throws SQLException {
        String sql = "SELECT IFNULL(SUM(totalbiaya),0) FROM billing WHERE no_rawat=?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    private static final String[] angka = {
            "", "Satu", "Dua", "Tiga", "Empat", "Lima", "Enam",
            "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"
    };

    public static String terbilangID(long n) {
        if (n == 0) return "Nol";
        if (n < 0) return "Minus " + terbilangID(-n);
        if (n < 12) return angka[(int) n];
        if (n < 20) return terbilangID(n - 10) + " Belas";
        if (n < 100) return terbilangID(n / 10) + " Puluh" + sisa(n % 10);
        if (n < 200) return "Seratus" + sisa(n - 100);
        if (n < 1000) return terbilangID(n / 100) + " Ratus" + sisa(n % 100);
        if (n < 2000) return "Seribu" + sisa(n - 1000);
        if (n < 1000000) return terbilangID(n / 1000) + " Ribu" + sisa(n % 1000);
        if (n < 1000000000L) return terbilangID(n / 1000000) + " Juta" + sisa(n % 1000000);
        if (n < 1000000000000L) return terbilangID(n / 1000000000L) + " Miliar" + sisa(n % 1000000000L);
        return terbilangID(n / 1000000000000L) + " Triliun" + sisa(n % 1000000000000L);
    }

    private static String sisa(long n) {
        return n == 0 ? "" : " " + terbilangID(n);
    }
}
