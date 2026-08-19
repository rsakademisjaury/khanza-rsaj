/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package digital_klaim;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Owner
 */
public class WarnaTableDiagnosa extends DefaultTableCellRenderer {
    // Konstanta untuk warna
    private static final Color ODD_ROW_COLOR = new Color(247, 255, 243);
    private static final Color EVEN_ROW_COLOR = new Color(255, 255, 255);
    private static final Color PRIMARY_COLOR = new Color(0, 51, 102);
    private static final Color SECONDARY_COLOR = new Color(0, 112, 112);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Mendapatkan komponen yang akan digunakan sebagai renderer
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Mendapatkan nilai dari kolom ketiga
        String status = table.getValueAt(row, 2).toString();

        // Mengatur warna latar belakang berdasarkan apakah baris tersebut ganjil atau genap
        if (row % 2 == 1) {
            component.setBackground(ODD_ROW_COLOR);
        } else {
            component.setBackground(EVEN_ROW_COLOR);
        }

        // Mengatur warna latar belakang dan warna teks berdasarkan nilai di kolom ketiga
        if (status.equals("Primary")) {
            component.setBackground(PRIMARY_COLOR);
            component.setForeground(Color.WHITE);
        } else if (status.equals("Secondary")) {
            component.setBackground(SECONDARY_COLOR);
            component.setForeground(Color.WHITE);
        }
            
    // Tambahkan kondisi jika baris dipilih
        if (table.getSelectionModel().isSelectedIndex(row)) {
            component.setBackground(new Color(0, 0, 255)); // Latar belakang biru tua
            component.setForeground(new Color(255, 255, 255)); // Teks puti
        } 
        return component;
    }
}

