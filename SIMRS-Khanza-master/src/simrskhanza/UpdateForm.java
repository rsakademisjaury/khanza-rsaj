package simrskhanza;

import javax.swing.*;
import java.awt.*;

public class UpdateForm extends JFrame {

    // Deklarasi komponen
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JPanel jPanel1;

    public UpdateForm() {
        // Inisialisasi komponen
        jPanel1 = new JPanel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        progressBar = new JProgressBar();

        // Pengaturan dasar JFrame
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 350));
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setUndecorated(true);
        setPreferredSize(new Dimension(600, 350));
        setType(Window.Type.POPUP);
        getContentPane().setLayout(null);

        // Pengaturan jPanel1
        jPanel1.setBackground(new Color(0, 156, 215));
        jPanel1.setLayout(null);

        // Pengaturan jLabel5
        jLabel5.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        jLabel5.setForeground(new Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(SwingConstants.LEFT);
        jLabel5.setIcon(new ImageIcon(getClass().getResource("/picture/updatesystem.png")));
        jPanel1.add(jLabel5);
        jLabel5.setBounds(70, 110, 146, 135);

        // Pengaturan jLabel6
        jLabel6.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        jLabel6.setForeground(new Color(255, 255, 255));
        jLabel6.setText("Periksa Pembaruan Sistem");
        jPanel1.add(jLabel6);
        jLabel6.setBounds(70, 50, 300, 30);

        // Pengaturan progressBar
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);        
        jPanel1.add(progressBar);        
        
        progressBar.setBackground(new java.awt.Color(204, 204, 204));
        progressBar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        progressBar.setForeground(new java.awt.Color(51, 204, 0));
        progressBar.setToolTipText("Loading");
        progressBar.setBorderPainted(false);
        progressBar.setFocusable(false);
        progressBar.setName("Loading"); // NOI18N
        progressBar.setOpaque(true);
        progressBar.setPreferredSize(new java.awt.Dimension(146, 10));
        
        progressBar.setBounds(50, 297, 500, 10);
        progressBar.getAccessibleContext().setAccessibleName("Loading");
        progressBar.getAccessibleContext().setAccessibleParent(progressBar);

        // Status label
        statusLabel = new JLabel("⏳ Memulai pembaruan...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(255, 255, 255));
        jPanel1.add(statusLabel);
        statusLabel.setBounds(50, 270, 500, 20);

        // Menambahkan panel ke frame
        getContentPane().add(jPanel1);
        jPanel1.setBounds(0, 0, 600, 350);

        setSize(600, 350);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Update progress bar
    public void updateProgress(int progress, String step) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            statusLabel.setText("⏳ " + step + " - " + progress + "%");
        });
    }

    // Simulasi proses update
    public static void main(String[] args) {
        UpdateForm form = new UpdateForm();

        new Thread(() -> {
            try {
                // Simulasi proses download
                for (int i = 0; i <= 100; i += 10) {
                    form.updateProgress(i, "⬇️ Mengunduh file pembaruan");
                    Thread.sleep(300);
                }

                // Simulasi ekstraksi file
                for (int i = 0; i <= 100; i += 20) {
                    form.updateProgress(i, "🔧 Mengekstrak file");
                    Thread.sleep(300);
                }

                // Simulasi copy file
                for (int i = 0; i <= 100; i += 25) {
                    form.updateProgress(i, "🔧 Menyalin file ke sistem");
                    Thread.sleep(300);
                }

                // Selesai
                form.updateProgress(100, "Pembaruan selesai");
                Thread.sleep(1000);
                form.dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

// Update status tanpa mengubah progress bar
public void updateStatus(String status) {
    SwingUtilities.invokeLater(() -> {
        statusLabel.setText(status);
    });
}

// Update progress bar tanpa teks tambahan
public void updateProgres(int progress) {
    SwingUtilities.invokeLater(() -> {
        progressBar.setValue(progress);
    });
}

public void updateProgress(int progress) {
    SwingUtilities.invokeLater(() -> {
        progressBar.setValue(progress);
    });        
}
    
}
