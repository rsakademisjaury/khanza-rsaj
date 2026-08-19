//package widget;
//
//import java.awt.Color;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Locale;
//import javax.swing.UIManager;
//import uz.ncipro.calendar.JDateTimePicker;
//
///**
// *
// * @author khanzasoft
// */
//public final class Tanggal extends JDateTimePicker {
//
//    public Tanggal() {
//        super();
//        setForeground(new Color(0, 156, 215));
//        setBackground(new Color(255, 255, 255));
//        setFont(new java.awt.Font("Tahoma", 0, 11));
//        setSize(WIDTH, 23);
//        applySystemDateFormat();
//    }
//
//    /**
//     * Method untuk mengatur format tanggal sesuai pengaturan sistem operasi.
//     */
//    private void applySystemDateFormat() {
//        // Mendapatkan format tanggal dari sistem operasi (lokal pengguna)
//        Locale locale = Locale.getDefault();  // Menggunakan lokal default dari sistem operasi
//        DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT, locale);
//
//        // Menggunakan Look and Feel sistem operasi
//        applySystemLookAndFeel();
//    }
//
//    /**
//     * Method untuk mengatur tampilan sesuai dengan sistem operasi.
//     */
//    private void applySystemLookAndFeel() {
//        try {
//            // Mendapatkan Look and Feel default dari sistem operasi
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void setFormats(DateFormat dateFormat) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
//}

package widget;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import uz.ncipro.calendar.JDateTimePicker;

/**
 * Tanggal widget yang memiliki tampilan bersih tanpa latar abu-abu.
 * 
 * @author khanzasoft
 */
public final class Tanggal extends JDateTimePicker {

    private DateFormat dateFormat;  // Simpan format tanggal

    public Tanggal() {
        super();
        setForeground(new Color(0, 156, 215));  // Warna teks
        setBackground(new Color(255, 255, 255));  // Warna latar belakang putih
        setFont(new java.awt.Font("Tahoma", 0, 11));
        setSize(WIDTH, 23);
        setOpaque(true);  // Pastikan background ditampilkan
        setBorder(null);  // Hapus border jika ada garis abu-abu di sekelilingnya
        applySystemDateFormat();
    }

    /**
     * Method untuk mengatur format tanggal sesuai pengaturan sistem operasi.
     */
    private void applySystemDateFormat() {
        // Mendapatkan format tanggal dari sistem operasi (lokal pengguna)
        Locale locale = Locale.getDefault();  // Menggunakan lokal default dari sistem operasi
        dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT, locale);  // Menyimpan format tanggal
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        // Tidak perlu mengakses editor internal, cukup atur latar belakang langsung
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        // Tidak perlu mengakses editor internal, cukup atur warna teks langsung
    }

    /**
     * Mengambil tanggal yang diformat sebagai String.
     */
    public String getFormattedDate() {
        if (getDate() != null) {
            return dateFormat.format(getDate());  // Menggunakan format yang disimpan
        } else {
            return "";  // Jika tidak ada tanggal, kembalikan string kosong
        }
    }

    /**
     * Method untuk mengatur format yang digunakan oleh komponen ini.
     */
    public void setFormats(DateFormat format) {
        this.dateFormat = format;  // Menyimpan format yang diberikan
    }

    public Object getDateEditor() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
