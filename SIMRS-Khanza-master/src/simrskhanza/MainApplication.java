package simrskhanza;

import javax.swing.*;
import java.awt.*;

public class MainApplication extends JFrame {

    public MainApplication() {
        setTitle("SIMRS Khanza - Aplikasi Utama");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null); // Pusatkan jendela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Contoh komponen
        JLabel welcomeLabel = new JLabel("Selamat datang di SIMRS Khanza!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(welcomeLabel, BorderLayout.CENTER);

        setVisible(true);
    }
}
