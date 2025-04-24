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
public class WarnaTableLab extends DefaultTableCellRenderer {
     private Connection koneksi;

    // Constructor yang menerima Connection
    public WarnaTableLab(Connection koneksi) {
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

        // Atur warna khusus untuk kolom 1
        if (column == 12) {
            if (table.getValueAt(row, 12).toString().toLowerCase().contains("cito")) {
                component.setBackground(new Color(238, 75, 43));                
                } else {
                    if (!table.getValueAt(row, 12).toString().toLowerCase().contains("cito")) {
                        return component;
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
