package bridging;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

/** Pembuka draf dari tabel IGD/ranap. Tidak mengubah data kunjungan atau memanggil API. */
public final class SatuSehatRujukanIGDRanapLauncher {
    // Semua akses map dilakukan pada EDT. Cegah dua pemuatan dari menu yang sama.
    private static final Map<Component, Boolean> MEMUAT = new WeakHashMap<Component, Boolean>();

    private SatuSehatRujukanIGDRanapLauncher() { }

    public static void bukaIGD(Component parent, JTable table, Connection connection, String operator) {
        buka(parent, table, connection, operator, "IGD");
    }

    public static void bukaRawatInap(Component parent, JTable table, Connection connection, String operator) {
        buka(parent, table, connection, operator, "RANAP");
    }

    private static void buka(final Component parent, final JTable table, final Connection connection,
                             final String operator, final String jenis) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> buka(parent, table, connection, operator, jenis));
            return;
        }
        if (MEMUAT.containsKey(parent)) return;

        final Pilihan pilihan;
        final String operatorAktif = teks(operator);
        try {
            if (operatorAktif.isEmpty()) throw new IllegalArgumentException("Sesi operator belum dikenali. Buka melalui akun Khanza yang aktif.");
            pilihan = ambilPilihan(table, jenis);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Rujukan " + jenis, JOptionPane.INFORMATION_MESSAGE);
            if (table != null) table.requestFocusInWindow();
            return;
        }

        final Cursor cursorAwal = parent.getCursor();
        MEMUAT.put(parent, Boolean.TRUE);
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return cocokkanKunjungan(connection, pilihan);
            }

            @Override protected void done() {
                // Pulihkan kursor sebelum dialog modal tampil.
                parent.setCursor(cursorAwal);
                try {
                    String noRawat = get();
                    if (!parent.isShowing()) return;
                    Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
                    SatuSehatRujukanIGDRanap dialog = new SatuSehatRujukanIGDRanap(owner, true);
                    dialog.setOperator(operatorAktif);
                    dialog.bukaKunjungan(noRawat, pilihan.jenis);
                    dialog.ikutiForm(parent);
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    if (parent.isShowing()) {
                        Throwable sebab = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
                        String pesan = sebab instanceof SQLException && "45000".equals(((SQLException) sebab).getSQLState())
                                ? sebab.getMessage()
                                : "Form rujukan belum dapat dibuka. Periksa koneksi database dan pemasangan modul tahap 1.";
                        JOptionPane.showMessageDialog(parent, pesan, "Rujukan " + jenis, JOptionPane.WARNING_MESSAGE);
                    }
                } finally {
                    MEMUAT.remove(parent);
                }
            }
        }.execute();
    }

    static final class Pilihan {
        final String noRawat, noRm, rawatInduk, jenis;
        Pilihan(String noRawat, String noRm, String rawatInduk, String jenis) {
            this.noRawat = noRawat; this.noRm = noRm; this.rawatInduk = rawatInduk; this.jenis = jenis;
        }
    }

    /** Snapshot dari model tabel; tetap benar ketika baris disortir atau kolom dipindahkan. */
    static Pilihan ambilPilihan(JTable table, String jenis) {
        if (!"IGD".equals(jenis) && !"RANAP".equals(jenis)) throw new IllegalArgumentException("Jenis rujukan tidak dikenali.");
        if (table == null || table.getSelectedRow() < 0) throw new IllegalArgumentException("Pilih pasien pada tabel terlebih dahulu.");
        TableModel model = table.getModel();
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        int kolomRawat = "IGD".equals(jenis) ? 2 : 0;
        int kolomRm = "IGD".equals(jenis) ? 7 : 1;
        if (row < 0 || row >= model.getRowCount() || model.getColumnCount() <= kolomRm)
            throw new IllegalArgumentException("Susunan tabel pasien belum sesuai. Muat ulang daftar pasien.");
        String noRawat = teks(model.getValueAt(row, kolomRawat));
        String noRm = teks(model.getValueAt(row, kolomRm));
        if (noRm.isEmpty()) throw new IllegalArgumentException("Nomor RM pasien terpilih belum tersedia.");
        String induk = "";
        if (noRawat.isEmpty()) {
            if ("IGD".equals(jenis)) throw new IllegalArgumentException("Nomor rawat pasien terpilih belum tersedia.");
            // Source DlgKamarInap menempatkan baris bayi bernomor rawat kosong sesudah induknya.
            // Cari pada MODEL, bukan urutan layar, lalu cocokkan RM bayi di database.
            for (int i = row - 1; i >= 0 && induk.isEmpty(); i--) induk = teks(model.getValueAt(i, 0));
            if (induk.isEmpty()) throw new IllegalArgumentException("Induk ranap gabung belum ditemukan. Muat ulang daftar pasien.");
        }
        return new Pilihan(noRawat, noRm, induk, jenis);
    }

    /** Hanya SELECT. Tidak memakai nomor rawat dari textbox yang mungkin milik pasien sebelumnya. */
    static String cocokkanKunjungan(Connection connection, Pilihan pilihan) throws SQLException {
        if (connection == null || connection.isClosed()) throw new SQLException("Koneksi tidak tersedia.");
        String sql = pilihan.noRawat.isEmpty()
                ? "SELECT DISTINCT rg.no_rawat2 FROM ranap_gabung rg "
                    + "JOIN reg_periksa rp ON rp.no_rawat=rg.no_rawat2 "
                    + "WHERE rg.no_rawat=? AND rp.no_rkm_medis=?"
                : "SELECT no_rawat FROM reg_periksa WHERE no_rawat=? AND no_rkm_medis=?";
        synchronized (connection) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                try { p.setQueryTimeout(15); } catch (SQLException unsupported) { /* Driver lama. */ }
                p.setString(1, pilihan.noRawat.isEmpty() ? pilihan.rawatInduk : pilihan.noRawat);
                p.setString(2, pilihan.noRm);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Kunjungan dan RM pasien terpilih tidak cocok atau sudah berubah. Muat ulang daftar pasien.", "45000");
                    String noRawat = teks(r.getString(1));
                    if (noRawat.isEmpty() || r.next()) throw new SQLException("Kunjungan ranap gabung belum dapat ditentukan secara unik. Periksa data penggabungan pasien.", "45000");
                    return noRawat;
                }
            }
        }
    }

    private static String teks(Object value) { return value == null ? "" : value.toString().trim(); }
}
