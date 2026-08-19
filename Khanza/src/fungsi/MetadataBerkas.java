package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Helper global untuk mencatat metadata upload berkas digital perawatan.
 *
 * Fungsi utama:
 * - Menyimpan waktu upload (uploaded_at)
 * - Menyimpan user yang melakukan upload (uploaded_by)
 * - Menghindari duplikasi metadata aktif untuk file yang sama
 * - Kegagalan metadata tidak menggagalkan proses upload utama
 *
 * Contoh pemakaian:
 *
 * MetadataBerkas.simpan(
 *     koneksi,
 *     noRawat,
 *     kodeberkas,
 *     filePath,
 *     namaFile,
 *     akses.getkode()
 * );
 */
public class MetadataBerkas {

    private MetadataBerkas() {
        // Utility class, tidak perlu dibuat object.
    }

    public static void simpan(
            Connection koneksi,
            String noRawat,
            String kode,
            String lokasiFile,
            String namaFileAsli,
            String userUpload
    ) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (koneksi == null) {
                return;
            }

            noRawat = aman(noRawat);
            kode = aman(kode);
            lokasiFile = aman(lokasiFile);
            namaFileAsli = aman(namaFileAsli);
            userUpload = aman(userUpload);

            if (noRawat.equals("") || kode.equals("") || lokasiFile.equals("")) {
                return;
            }

            if (userUpload.equals("")) {
                userUpload = "system";
            }

            /*
             * Cek apakah tabel metadata tersedia.
             * Jika belum tersedia, proses upload utama tetap aman.
             */
            ps = koneksi.prepareStatement(
                    "SELECT COUNT(*) AS jumlah " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema=DATABASE() " +
                    "AND table_name='berkas_digital_perawatan_meta'"
            );

            rs = ps.executeQuery();

            boolean tabelAda = false;
            if (rs.next()) {
                tabelAda = rs.getInt("jumlah") > 0;
            }

            tutup(rs);
            rs = null;

            tutup(ps);
            ps = null;

            if (!tabelAda) {
                System.out.println(
                        "MetadataBerkas : tabel berkas_digital_perawatan_meta belum tersedia."
                );
                return;
            }

            /*
             * Cek apakah metadata aktif untuk file ini sudah ada.
             */
            ps = koneksi.prepareStatement(
                    "SELECT id_meta " +
                    "FROM berkas_digital_perawatan_meta " +
                    "WHERE no_rawat=? " +
                    "AND kode=? " +
                    "AND lokasi_file=? " +
                    "AND deleted_at IS NULL " +
                    "ORDER BY id_meta DESC " +
                    "LIMIT 1"
            );

            ps.setString(1, noRawat);
            ps.setString(2, kode);
            ps.setString(3, lokasiFile);

            rs = ps.executeQuery();

            if (rs.next()) {
                long idMeta = rs.getLong("id_meta");

                tutup(rs);
                rs = null;

                tutup(ps);
                ps = null;

                /*
                 * Metadata sudah ada.
                 * Perbarui nama file, waktu upload, dan user uploader.
                 */
                ps = koneksi.prepareStatement(
                        "UPDATE berkas_digital_perawatan_meta SET " +
                        "nama_file_asli=?, " +
                        "uploaded_at=NOW(), " +
                        "uploaded_by=? " +
                        "WHERE id_meta=?"
                );

                ps.setString(1, namaFileAsli);
                ps.setString(2, userUpload);
                ps.setLong(3, idMeta);

                ps.executeUpdate();

            } else {
                tutup(rs);
                rs = null;

                tutup(ps);
                ps = null;

                /*
                 * Metadata belum ada.
                 * Buat record metadata baru.
                 */
                ps = koneksi.prepareStatement(
                        "INSERT INTO berkas_digital_perawatan_meta " +
                        "(no_rawat, kode, lokasi_file, nama_file_asli, uploaded_at, uploaded_by) " +
                        "VALUES (?,?,?,?,NOW(),?)"
                );

                ps.setString(1, noRawat);
                ps.setString(2, kode);
                ps.setString(3, lokasiFile);
                ps.setString(4, namaFileAsli);
                ps.setString(5, userUpload);

                ps.executeUpdate();
            }

            System.out.println(
                    "Metadata upload tercatat : " +
                    noRawat + " | " +
                    lokasiFile + " | " +
                    userUpload
            );

        } catch (Exception e) {
            /*
             * Kegagalan pencatatan metadata tidak boleh
             * menggagalkan proses upload utama.
             */
            System.out.println(
                    "Notif MetadataBerkas.simpan() : " + e
            );

        } finally {
            tutup(rs);
            tutup(ps);
        }
    }

    private static String aman(String nilai) {
        return nilai == null ? "" : nilai.trim();
    }

    private static void tutup(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            // Abaikan kegagalan close.
        }
    }

    private static void tutup(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            // Abaikan kegagalan close.
        }
    }
}
