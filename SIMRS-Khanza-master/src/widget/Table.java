package widget;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTable;
import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.JTableHeader;

/**
 *
 * @author usu
 */
public class Table extends JTable {

    /*
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    public Table() {
        super();
        setDefaults();
        applySystemLookAndFeel();
    }

    /**
     * Mengatur tampilan default tabel
     */
    private void setDefaults() {
        setBackground(new Color(255, 255, 255)); // Latar belakang tabel
        setGridColor(new Color(220, 221, 223)); // Warna grid tabel
        setForeground(new Color(50, 50, 50)); // Warna teks tabel
        setFont(getSystemFont());  // Mengatur font mengikuti sistem operasi
        setRowHeight(27); // Tinggi baris tabel
        setSelectionBackground(new Color(255, 255, 255)); // Warna latar belakang saat terpilih
        setSelectionForeground(new Color(0, 156, 215)); // Warna teks saat terpilih

        // Mengatur header tabel
        JTableHeader header = getTableHeader();
        header.setForeground(new Color(50, 50, 50)); // Warna teks header
        header.setBackground(new Color(211, 211, 211)); // Mengubah warna latar belakang header ke abu-abu terang (RGB: 211, 211, 211)
        header.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(255, 250, 250))); // Border header
        header.setFont(getSystemFont());  // Mengatur font header mengikuti sistem operasi
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 30)); // Ukuran header
    }

    /**
     * Method untuk mendapatkan font yang sesuai dengan sistem operasi.
     */
    private Font getSystemFont() {
        // Mendapatkan Look and Feel dari UIManager
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        Font font;

        // Mengatur font berdasarkan Look and Feel
        if (lookAndFeel.contains("Windows")) {
            font = new Font("Segoe UI", Font.PLAIN, 11); // Font untuk Windows
        } else if (lookAndFeel.contains("Mac")) {
            font = new Font("Lucida Grande", Font.PLAIN, 11); // Font untuk Mac
        } else {
            font = new Font("Tahoma", Font.PLAIN, 11); // Font default
        }
        
        return font;
    }

    /**
     * Method untuk mengatur tampilan sesuai dengan sistem operasi.
     */
    private void applySystemLookAndFeel() {
        try {
            // Mengatur Look and Feel agar sesuai dengan sistem operasi
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Memperbarui tampilan tabel agar sesuai dengan Look and Feel sistem operasi
        UIManager.getLookAndFeelDefaults().put("Table.background", getBackground());
        UIManager.getLookAndFeelDefaults().put("Table.gridColor", getGridColor());
        UIManager.getLookAndFeelDefaults().put("Table.foreground", getForeground());
        UIManager.getLookAndFeelDefaults().put("Table.selectionBackground", getSelectionBackground());
        UIManager.getLookAndFeelDefaults().put("Table.selectionForeground", getSelectionForeground());
    }
}
