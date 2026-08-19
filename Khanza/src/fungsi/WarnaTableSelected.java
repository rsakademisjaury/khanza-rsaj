/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Owner
 */
public class WarnaTableSelected extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
/*        if (row % 2 == 1){
            component.setBackground(new Color(255,244,244));
        }else{
            component.setBackground(new Color(255,255,255));
        } 
        return component;
    }

}*/

        if (isSelected) {
            component.setBackground(new Color(0, 0, 255)); // Latar belakang biru tua
            component.setForeground(new Color(255, 255, 255)); // Teks putih
        } else {
            if (row % 2 == 1) {
                component.setBackground(new Color(255, 244, 244)); // Warna Pink Putih
            } else {
                component.setBackground(new Color(255, 255, 255)); // Warna Putih
            }
            component.setForeground(new Color(0, 0, 0)); // Teks hitam
        }

        return component;
    }
}

