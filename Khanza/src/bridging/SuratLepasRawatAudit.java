package bridging;

import fungsi.akses;
import fungsi.sekuel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/** Konteks audit hanya selama satu penulisan SKDP, termasuk dari form lama. */
final class SuratLepasRawatAudit implements AutoCloseable {
    private final Connection connection;

    SuratLepasRawatAudit(Connection connection, String petugas, String sumber,
            String alasan, String permintaan) throws SQLException {
        this.connection = connection;
        try (PreparedStatement ps = connection.prepareStatement(
                "SET @rsaj_slr_petugas=?, @rsaj_slr_sumber=?, @rsaj_slr_alasan=?, @rsaj_slr_permintaan=?")) {
            ps.setString(1, petugas);
            ps.setString(2, sumber);
            ps.setString(3, alasan);
            ps.setString(4, permintaan);
            ps.execute();
        }
    }

    @Override public void close() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SET @rsaj_slr_petugas=NULL, @rsaj_slr_sumber=NULL, @rsaj_slr_alasan=NULL, @rsaj_slr_permintaan=NULL")) {
            ps.execute();
        }
    }

    static boolean simpan(sekuel sequel, Connection connection, String table,
            String value, String label, int count, String[] values) {
        return simpanDenganSumber(sequel,connection,table,value,label,count,values,"BUAT_SKDP");
    }

    static boolean impor(sekuel sequel, Connection connection, String table,
            String value, String label, int count, String[] values) {
        return simpanDenganSumber(sequel,connection,table,value,label,count,values,"ENTRI_VCLAIM");
    }

    private static boolean simpanDenganSumber(sekuel sequel, Connection connection, String table,
            String value, String label, int count, String[] values, String sumber) {
        synchronized (connection) {
            try (SuratLepasRawatAudit audit = new SuratLepasRawatAudit(connection,
                    akses.getkode(), sumber, "BUAT_SKDP".equals(sumber)
                            ? "Pembuatan SKDP melalui SIMRS" : "Entri surat dari VClaim", null)) {
                return sequel.menyimpantf(table, value, label, count, values);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Gagal menyimpan konteks audit SKDP: " + e.getMessage());
                return false;
            }
        }
    }

    static boolean edit(sekuel sequel, Connection connection, String table,
            String where, String update, int count, String[] values) {
        synchronized (connection) {
            try (SuratLepasRawatAudit audit = new SuratLepasRawatAudit(connection,
                    akses.getkode(), "GANTI_SKDP", "Perubahan melalui form SKDP", null)) {
                return sequel.mengedittf(table, where, update, count, values);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Gagal menyimpan konteks audit SKDP: " + e.getMessage());
                return false;
            }
        }
    }
}
