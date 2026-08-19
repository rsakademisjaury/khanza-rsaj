/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class WarnaTableIGD extends DefaultTableCellRenderer {
    private final Connection koneksi;

    // Palet
    private static final Color ZEBRA_ODD  = new Color(238, 238, 238);
    private static final Color ZEBRA_EVEN = new Color(255, 255, 255);
    private static final Color BIRU_MUDA  = new Color(204, 236, 255);
    private static final Color UNGU_SO    = new Color(204, 169, 221);

    public WarnaTableIGD(Connection koneksi) {
        this.koneksi = koneksi;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // ===== 1) Hitung base background (zebra) =====
        Color baseBg = (row % 2 == 1) ? ZEBRA_ODD : ZEBRA_EVEN;

        int colCount   = table.getColumnCount();
        int idxRiwayat = colCount - 1; // kolom terakhir: "Riwayat Terakhir"
        int idxSO      = colCount - 2; // kolom ke-2 dari belakang: "SO"

        // ===== 2) Deteksi <= 7 hari dari kolom "Riwayat Terakhir" =====
        boolean within7 = false;
        try {
            Object v = table.getValueAt(row, idxRiwayat);
            if (v != null) {
                String s = v.toString().trim().toLowerCase();
                if (!s.isEmpty() && !"-".equals(s)) {
                    if (s.contains("hari")) {
                        // ekstrak angka hari
                        String num = "";
                        Matcher m = Pattern.compile("(\\d+)").matcher(s);
                        if (m.find()) num = m.group(1);
                        if (!num.isEmpty()) {
                            int hari = Integer.parseInt(num);
                            within7 = (hari <= 7);
                        }
                    } else if (s.contains("bulan") || s.contains("tahun")) {
                        // pasti > 7 hari
                        within7 = false;
                    }
                }
            }
        } catch (Exception ignore) {}

        // ===== 3) Terapkan prioritas warna baris =====
        if (within7) {
            // seluruh baris biru muda
            baseBg = BIRU_MUDA;
        } else {
            // jika bukan <=7 hari, rule SO untuk kolom No. (kolom == 1) saja
            if (column == 1) {
                try {
                    Object vSO = table.getValueAt(row, idxSO);
                    if (vSO != null && "TIDAK ADA SO".equalsIgnoreCase(vSO.toString().trim())) {
                        baseBg = UNGU_SO;
                    }
                } catch (Exception ignore) {}
            }
        }

        // ===== 4) Pakai base background SELALU (bahkan saat selected) =====
        c.setBackground(baseBg);

        // ===== 5) Saat terpilih: ubah hanya warna teks, bukan background =====
        if (isSelected) {
            // pakai warna teks seleksi dari LAF biar konsisten tema
            c.setForeground(table.getSelectionForeground());
        } else {
            c.setForeground(table.getForeground());
        }

        return c;
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
