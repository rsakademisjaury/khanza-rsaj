package widget;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTabbedPane;
import javax.swing.BorderFactory;

public class TabPane extends JTabbedPane {

    public TabPane() {
        super();
        // Mengatur latar belakang dan warna teks untuk tampilan Windows
        setBackground(Color.WHITE); // Menggunakan warna putih untuk latar belakang
        setForeground(new Color(0, 0, 0)); // Menggunakan warna hitam untuk teks

        // Mengatur border sesuai dengan tampilan Windows
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        // Mengatur font sesuai dengan standar Windows
        setFont(getFont().deriveFont(12f)); // Menggunakan ukuran font yang lebih sesuai untuk pengguna

        // Menambahkan pengaturan tinggi tab
        setTabHeight(60); // Atur tinggi tab sesuai dengan kebutuhan, misalnya 30 piksel
    }

    /**
     * Method untuk mengatur tinggi tab.
     *
     * @param height tinggi tab dalam piksel
     */
    private void setTabHeight(int height) {
        // Mengatur ukuran preferensi tab
        for (int i = 0; i < getTabCount(); i++) {
            getTabComponentAt(i).setPreferredSize(new Dimension(getWidth(), height));
        }
    }
}
