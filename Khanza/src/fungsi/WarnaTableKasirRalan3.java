package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class WarnaTableKasirRalan3 extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
       Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Atur warna latar belakang baris secara bergantian
        if (row % 2 == 1){
            component.setBackground(new Color(238, 238, 238)); // Warna abu-abu muda untuk baris ganjil
            component.setForeground(new Color(50,50,50));
        }else{
            component.setBackground(new Color(255,255,255)); // Warna putih untuk baris genap
            component.setForeground(new Color(50,50,50));         
        }

        // Periksa apakah nilai pada kolom 18 adalah null sebelum memanggil toString()
        Object statusValue = table.getValueAt(row, 20);
        if (statusValue != null && statusValue.toString().equals("Belum dijawab")){
            component.setBackground(new Color(195,230,252)); // Warna latar belakang
            component.setForeground(new Color(50,50,50)); // Warna teks putih                                              
        }        
        
        return component;
    }
}
