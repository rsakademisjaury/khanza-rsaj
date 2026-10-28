package setting;

import fungsi.AccountPasswordService;
import fungsi.PasswordPolicy;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/** Konfirmasi/reset hanya menerima password baru, tidak pernah memuat password DB. */
final class PasswordDialogs {
    private PasswordDialogs() { }

    static boolean confirm(Component parent, JPasswordField input) {
        char[] password = input.getPassword();
        try {
            String problem = PasswordPolicy.problem(password);
            if (problem != null) { JOptionPane.showMessageDialog(parent, problem); input.requestFocusInWindow(); return false; }
            JPasswordField confirmation = new JPasswordField(22); PasswordPolicy.configure(confirmation);
            JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
            panel.add(new JLabel("Ulangi password baru:")); panel.add(confirmation);
            try {
                if (JOptionPane.showConfirmDialog(parent, panel, "Konfirmasi password baru",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return false;
                char[] repeat = confirmation.getPassword();
                try {
                    if (!PasswordPolicy.matches(password, repeat)) { JOptionPane.showMessageDialog(parent, "Konfirmasi password baru tidak sama."); return false; }
                    return true;
                } finally { Arrays.fill(repeat, '\0'); }
            } finally { confirmation.setText(""); }
        } finally { Arrays.fill(password, '\0'); }
    }

    static boolean reset(Component parent, String username, AccountPasswordService.Account account) {
        JPasswordField first = new JPasswordField(24), second = new JPasswordField(24);
        PasswordPolicy.configure(first); PasswordPolicy.configure(second);
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.add(new JLabel("Akun: " + username));
        panel.add(new JLabel(PasswordPolicy.HINT));
        panel.add(new JLabel("Password baru:")); panel.add(first);
        panel.add(new JLabel("Ulangi password baru:")); panel.add(second);
        try {
            while (JOptionPane.showConfirmDialog(parent, panel, "Reset Password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                char[] password = first.getPassword(), repeat = second.getPassword();
                try {
                    String problem = PasswordPolicy.problem(password);
                    if (problem != null) { JOptionPane.showMessageDialog(parent, problem); continue; }
                    if (!PasswordPolicy.matches(password, repeat)) { JOptionPane.showMessageDialog(parent, "Konfirmasi password baru tidak sama."); continue; }
                    boolean saved = new AccountPasswordService(koneksiDB::condbVerifikasiSesi).reset(account, username, password);
                    if (!saved) { JOptionPane.showMessageDialog(parent, "Akun tidak ditemukan. Muat ulang daftar akun."); return false; }
                    new sekuel().SimpanTrack("RESET PASSWORD " + account.name() + " akun=" + username + " [PASSWORD DISEMBUNYIKAN]");
                    JOptionPane.showMessageDialog(parent, "Password baru berhasil disimpan."); return true;
                } catch (java.sql.SQLException e) {
                    failure(parent); return false;
                } finally { Arrays.fill(password, '\0'); Arrays.fill(repeat, '\0'); }
            }
            return false;
        } finally { first.setText(""); second.setText(""); }
    }

    static void failure(Component parent) {
        JOptionPane.showMessageDialog(parent, "Password/data akun belum tersimpan. Periksa koneksi, akun duplikat, atau hubungi IT.");
    }
}
