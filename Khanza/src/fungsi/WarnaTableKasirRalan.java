/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Owner
 */
public class WarnaTableKasirRalan extends DefaultTableCellRenderer {
    private Connection koneksi;
    
    public WarnaTableKasirRalan(Connection koneksi) {
        this.koneksi = koneksi;
    }
    
//    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
//        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//        if (row % 2 == 1){
//            component.setBackground(new Color(238, 238, 238));
//            component.setForeground(new Color(50,50,50));
//        }else{
//            component.setBackground(new Color(255,255,255));
//            component.setForeground(new Color(50,50,50));
//        } 
//        if(table.getValueAt(row,10).toString().equals("Sudah")){
//            component.setBackground(new Color(200,0,0));
//            component.setForeground(new Color(255,230,230));
//        }else if(table.getValueAt(row,10).toString().equals("Batal")){
//            component.setBackground(new Color(255,243,109));
//            component.setForeground(new Color(120,110,50));
//        }else if(table.getValueAt(row,10).toString().equals("Dirujuk")||table.getValueAt(row,10).toString().equals("Meninggal")||table.getValueAt(row,10).toString().equals("Pulang Paksa")){
//            component.setBackground(new Color(152,152,156));
//            component.setForeground(new Color(245,245,255));
//        }else if(table.getValueAt(row,10).toString().equals("Dirawat")){
//            component.setBackground(new Color(119,221,119));
//            component.setForeground(new Color(245,255,245));
//        }
//        if(table.getValueAt(row,15).toString().equals("Sudah Bayar")){
//            component.setBackground(new Color(50,50,50));
//            component.setForeground(new Color(255,255,255));
//        }
//        
//        // Logika khusus untuk pasien geriatri berdasarkan reg_periksa.umurdaftar
//        if (column == 2) {
//            try {
//                String noRawat = table.getValueAt(row, 11).toString(); // Asumsi kolom 0 = no_rawat
//
//                String sql = "SELECT rp.umurdaftar, p.jk FROM reg_periksa rp " +
//                             "JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis " +
//                             "WHERE rp.no_rawat = ?";
//                
//                PreparedStatement ps = koneksi.prepareStatement(sql);
//                ps.setString(1, noRawat);
//                ResultSet rs = ps.executeQuery();
//
//                if (rs.next()) {
//                    int umur = rs.getInt("umurdaftar");
//                    String jk = rs.getString("jk");
//
//                    if (umur >= 60) {
//                        component.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
//
//                        if (jk.equalsIgnoreCase("L")) {
//                            setText("👴 " + value.toString());
//                        } else if (jk.equalsIgnoreCase("P")) {
//                            setText("👵 " + value.toString());
//                        }
//
//                        component.setBackground(new Color(255, 200, 200)); // Merah muda
//                    }
//                }
//
//                rs.close();
//                ps.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        
//        return component;
//    }

public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    // Warna default baris ganjil/genap
    Color backgroundColor = (row % 2 == 0) ? new Color(255, 255, 255) : new Color(238, 238, 238);
    Color foregroundColor = new Color(50, 50, 50);

    // Ambil status dari kolom 10 dan 15
    String status = String.valueOf(table.getValueAt(row, 10));
    String bayar = String.valueOf(table.getValueAt(row, 15));

    // Tentukan apakah status sudah "terpenuhi"
    boolean statusTerpenuhi = status.equalsIgnoreCase("Sudah") || status.equalsIgnoreCase("Batal") ||
                               status.equalsIgnoreCase("Dirujuk") || status.equalsIgnoreCase("Meninggal") ||
                               status.equalsIgnoreCase("Pulang Paksa") || status.equalsIgnoreCase("Dirawat") ||
                               bayar.equalsIgnoreCase("Sudah Bayar");

    // Warna status jika status terpenuhi (diatur nanti jika bukan geriatri atau geriatri + status terpenuhi)
    Color warnaStatus = backgroundColor;
    Color warnaTeksStatus = foregroundColor;

    if (bayar.equalsIgnoreCase("Sudah Bayar")) {
        warnaStatus = new Color(50, 50, 50);
        warnaTeksStatus = new Color(255, 255, 255);
    } else if (status.equalsIgnoreCase("Sudah")) {
        warnaStatus = new Color(200, 0, 0);
        warnaTeksStatus = new Color(255, 230, 230);
    } else if (status.equalsIgnoreCase("Batal")) {
        warnaStatus = new Color(255, 243, 109);
        warnaTeksStatus = new Color(120, 110, 50);
    } else if (status.equalsIgnoreCase("Dirujuk") || status.equalsIgnoreCase("Meninggal") || status.equalsIgnoreCase("Pulang Paksa")) {
        warnaStatus = new Color(152, 152, 156);
        warnaTeksStatus = new Color(245, 245, 255);
    } else if (status.equalsIgnoreCase("Dirawat")) {
        warnaStatus = new Color(119, 221, 119);
        warnaTeksStatus = new Color(245, 255, 245);
    }

    // Default set warna status ke semua kolom dulu
    component.setBackground(warnaStatus);
    component.setForeground(warnaTeksStatus);

    // Logika khusus kolom no. rekam medis (kolom 2)
    if (column == 2) {
        try {
            String noRawat = table.getValueAt(row, 11).toString(); // kolom 11 = no_rawat
            String sql = "SELECT rp.umurdaftar, p.jk FROM reg_periksa rp " +
                         "JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis " +
                         "WHERE rp.no_rawat = ?";
            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int umur = rs.getInt("umurdaftar");
                String jk = rs.getString("jk");

                if (umur >= 60) {
                    // Geriatri: tambahkan emoji
                    component.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    if (jk.equalsIgnoreCase("L")) {
                        setText("👴 " + value.toString());
                    } else if (jk.equalsIgnoreCase("P")) {
                        setText("👵 " + value.toString());
                    }

                    if (!statusTerpenuhi) {
                        // Kalau belum terpenuhi, warnai merah muda
                        component.setBackground(new Color(255, 200, 200));
                        component.setForeground(new Color(50, 50, 50));
                    }
                    // Jika sudah terpenuhi, biarkan warna ikut status di atas
                }
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    return component;
}    

}
