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
public class WarnaTableIGD extends DefaultTableCellRenderer {
    private Connection koneksi;

    public WarnaTableIGD(Connection koneksi) {
        this.koneksi = koneksi;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Warna default untuk baris ganjil/genap
        if (row % 2 == 1) {
            component.setBackground(new Color(238, 238, 238));
        } else {
            component.setBackground(new Color(255, 255, 255));
        }

        // Pewarnaan khusus untuk kolom 1
        if (column == 1) {
            if (table.getValueAt(row, 21).toString().equals("TIDAK ADA SO")) {                
                    component.setBackground(new Color(204, 169, 221));
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
