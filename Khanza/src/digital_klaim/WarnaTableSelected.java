package digital_klaim;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pewarnaan TEKS (foreground) pada kolom [colStart..colEnd] (default 0..10)
 * berdasarkan kombinasi:
 *  - Hijau  : (7) "Sudah Bayar"  && (9) "Belum Coding"
 *  - Oranye : (7) "Belum Bayar"  && (9) "Belum Coding"
 *  - Ungu   : (7) "Sudah Bayar"  && (9) "Sudah Coding"
 * Background zebra/seleksi tetap dipertahankan.
 * Index yang digunakan adalah index VIEW (urutan tampilan kolom).
 */
public class WarnaTableSelected extends DefaultTableCellRenderer {

    // Background zebra & seleksi (tetap)
    private static final Color ROW_EVEN_BG = new Color(255, 255, 255);
    private static final Color ROW_ODD_BG  = new Color(238, 238, 238);
    private static final Color SELECT_BG   = new Color(216, 240, 249);

    // Warna teks
    private static final Color BASE_TEXT   = new Color(50, 50, 50);
    private static final Color GREEN_TEXT  = new Color(28, 27, 23);   // hitam
    private static final Color ORANGE_TEXT = new Color(227, 30, 36);   // maroon
    private static final Color PURPLE_TEXT = new Color(126, 87, 194);  // ungu muda (kontras & kebaca)

    // Konfigurasi kolom (VIEW index)
    private final int colStatusBayar;  // default 7
    private final int colStatusCoding; // default 9
    private int colStart;              // default 0
    private int colEnd;                // default 10 (inklusif)

    public WarnaTableSelected() {
        this(7, 9, 0, 10);
    }

    public WarnaTableSelected(int colStatusBayar, int colStatusCoding, int colStart, int colEnd) {
        this.colStatusBayar  = colStatusBayar;
        this.colStatusCoding = colStatusCoding;
        this.colStart = Math.max(0, colStart);
        this.colEnd   = Math.max(this.colStart, colEnd);
        setOpaque(true);
    }

    /** Atur ulang rentang kolom yang diwarnai teks (inklusif). */
    public void setColoredRange(int startInclusive, int endInclusive) {
        this.colStart = Math.max(0, startInclusive);
        this.colEnd   = Math.max(this.colStart, endInclusive);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Background tetap zebra/seleksi
        if (isSelected) {
            c.setBackground(SELECT_BG);
        } else {
            c.setBackground((row % 2 == 1) ? ROW_ODD_BG : ROW_EVEN_BG);
        }

        // Default warna teks
        c.setForeground(BASE_TEXT);

        // Warnai hanya kolom dalam rentang
        int lastCol = table.getColumnCount() - 1;
        int effectiveEnd = Math.min(colEnd, lastCol);
        if (column < colStart || column > effectiveEnd) return c;

        // Ambil nilai referensi
        String bayar  = safeStr(table.getValueAt(row, colStatusBayar));
        String coding = safeStr(table.getValueAt(row, colStatusCoding));

        boolean sudahBayar  = "sudah bayar".equalsIgnoreCase(bayar);
        boolean belumBayar  = "belum bayar".equalsIgnoreCase(bayar);
        boolean sudahCoding = "Final Klaim".equalsIgnoreCase(coding);
        boolean belumCoding = "belum coding".equalsIgnoreCase(coding);

        // Terapkan aturan warna TEKS
        if (sudahBayar && belumCoding) {
            c.setForeground(GREEN_TEXT);      // hijau
        } else if (belumBayar && belumCoding) {
            c.setForeground(ORANGE_TEXT);     // oranye
        } else if (sudahBayar && sudahCoding) {
            c.setForeground(PURPLE_TEXT);     // ungu muda
        } // selain itu: tetap BASE_TEXT

        return c;
    }

    private static String safeStr(Object o) {
        return (o == null) ? "" : String.valueOf(o).trim();
    }
}
