/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import java.sql.Connection; // Tambahkan import untuk koneksi
import java.sql.PreparedStatement; // Import PreparedStatement
import java.sql.ResultSet; // Import ResultSet
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Owner
 */
public class WarnaTableReg extends DefaultTableCellRenderer {
     private Connection koneksi;

    // Constructor yang menerima Connection
    public WarnaTableReg(Connection koneksi) {
        this.koneksi = koneksi; // Ambil koneksi dari parameter
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Set warna latar belakang baris secara bergantian
        if (row % 2 == 1) {
            component.setBackground(new Color(238, 238, 238)); // Warna abu-abu muda untuk baris ganjil
        } else {
            component.setBackground(new Color(255, 255, 255)); // Warna putih untuk baris genap
        }

        // Atur warna khusus berdasarkan selisih waktu untuk kolom 1 dan 9
        if (column == 1 || column == 9) {
            try {
                // Mengambil hari sekarang
                LocalDate today = LocalDate.now();

                // Query untuk mengambil jam_mulai dari tabel jadwal
                String sql = "SELECT jam_mulai FROM jadwal WHERE kd_dokter = ? AND hari_kerja = ?";
                PreparedStatement ps = koneksi.prepareStatement(sql);
                
                // Ambil kd_dokter dari kolom 5 dan kd_poli dari kolom yang sesuai
                ps.setString(1, table.getValueAt(row, 5).toString()); // Ambil kd_dokter dari kolom 5
//                ps.setString(2, table.getValueAt(row, /* index kolom kd_poli */).toString()); // Ambil kd_poli dari kolom yang sesuai
                ps.setInt(2, today.getDayOfWeek().getValue()); // Menggunakan hari sekarang

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    LocalTime jamMulai = LocalTime.parse(rs.getString("jam_mulai")); // Ambil jam_mulai dari database

                    // Hitung selisih waktu dalam menit
                    long selisihMenit = Duration.between(LocalTime.now(), jamMulai).toMinutes();

                    // Atur warna latar belakang berdasarkan selisih waktu
                    if (selisihMenit > 30) {
                        component.setBackground(new Color(255, 200, 200)); // Merah muda jika selisih > 30 menit
                    } else {
                        // Kembali ke warna default tabel berdasarkan baris
                        if (row % 2 == 1) {
                            component.setBackground(new Color(238, 238, 238)); // Warna abu-abu muda untuk baris ganjil
                        } else {
                            component.setBackground(new Color(255, 255, 255)); // Warna putih untuk baris genap
                        }
                    }
                }
                // Jika tidak ada jadwal, tidak tampilkan pesan
                
            } catch (Exception e) {
                e.printStackTrace();
                // Hapus pesan kesalahan di sini
            }
        }

        // Atur warna khusus untuk kolom 1
        if (column == 1) {
            if (table.getValueAt(row, 12).toString().equals("BPJS Kesehatan")) {
                if (table.getValueAt(row, 25).toString().equals("Belum Sidik Jari")) {
                    component.setBackground(new Color(210, 255, 139));                
                } else {
                    if (table.getValueAt(row, 24).toString().equals("SEP Belum Terbit")) {
                        component.setBackground(new Color(219, 165, 7));    
                    }
                }
            }
        }

        return component;
    }    
}  


//    @Override
//    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
//        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//        if (row % 2 == 1){
//            component.setBackground(new Color(238, 238, 238));
//        }else{
//            component.setBackground(new Color(255,255,255));
//        }  
//        
//        if ((column == 1)){
//            if(table.getValueAt(row,12).toString().equals("BPJS Kesehatan")){
//            if(table.getValueAt(row,25).toString().equals("Belum Sidik Jari")){
//                component.setBackground(new Color(210,255,139));                
//            }else{
//                if(table.getValueAt(row,24).toString().equals("SEP Belum Terbit")){
//                component.setBackground(new Color(219, 165, 7));    
//                 }
//            }
//            } 
//        }   
//        return component;
//    }    
//}
