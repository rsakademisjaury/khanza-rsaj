package laporan; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class ResumeSyncUtil {
    private ResumeSyncUtil() {}

    private static String safeStr(String s){ return (s == null) ? "" : s; }
    private static String safeCode(String s){ return (s == null || s.trim().isEmpty()) ? "" : s; }

    public static void syncByStatus(Connection conn, String noRawat, String status) throws Exception {
        if ("Ralan".equalsIgnoreCase(status)) {
            syncRalan(conn, noRawat);
        } else if ("Ranap".equalsIgnoreCase(status)) {
            syncRanap(conn, noRawat);
        } else {
            // fallback kalau status tidak standar
            syncRalan(conn, noRawat);
            syncRanap(conn, noRawat);
        }
    }

    public static void syncRalan(Connection conn, String noRawat) throws Exception {
        syncCommon(conn, noRawat, "Ralan", "resume_pasien", new String[]{
            "diagnosa_utama","kd_diagnosa_utama",
            "diagnosa_sekunder","kd_diagnosa_sekunder",
            "diagnosa_sekunder2","kd_diagnosa_sekunder2",
            "diagnosa_sekunder3","kd_diagnosa_sekunder3",
            "diagnosa_sekunder4","kd_diagnosa_sekunder4"
        });
    }

    public static void syncRanap(Connection conn, String noRawat) throws Exception {
        syncCommon(conn, noRawat, "Ranap", "resume_pasien_ranap", new String[]{
            "diagnosa_utama","kd_diagnosa_utama",
            "diagnosa_sekunder","kd_diagnosa_sekunder",
            "diagnosa_sekunder2","kd_diagnosa_sekunder2",
            "diagnosa_sekunder3","kd_diagnosa_sekunder3",
            "diagnosa_sekunder4","kd_diagnosa_sekunder4"
        });
    }

    private static void syncCommon(Connection conn, String noRawat, String statusFilter,
                                   String resumeTable, String[] kolomPairs) throws Exception {
        final String sqlDiag =
            "SELECT prioritas, kd_penyakit " +
            "FROM diagnosa_pasien " +
            "WHERE no_rawat=? AND LOWER(status)=LOWER(?) " +
            "ORDER BY prioritas ASC";

        String[] kd = new String[5];
        String[] nm = new String[5];

        try (PreparedStatement ps = conn.prepareStatement(sqlDiag)) {
            ps.setString(1, noRawat);
            ps.setString(2, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pr = rs.getInt("prioritas");
                    if (pr >= 1 && pr <= 5) {
                        String k = rs.getString("kd_penyakit");
                        kd[pr - 1] = k;
                        nm[pr - 1] = getNamaPenyakit(conn, k);
                    }
                }
            }
        }

        // UPDATE dulu
        String sqlUpdate = "UPDATE " + resumeTable + " SET " +
                kolomPairs[0] + "=?, "  + kolomPairs[1] + "=?, " +
                kolomPairs[2] + "=?, "  + kolomPairs[3] + "=?, " +
                kolomPairs[4] + "=?, "  + kolomPairs[5] + "=?, " +
                kolomPairs[6] + "=?, "  + kolomPairs[7] + "=?, " +
                kolomPairs[8] + "=?, "  + kolomPairs[9] + "=? "  +
                "WHERE no_rawat=?";
        int updated;
        try (PreparedStatement psU = conn.prepareStatement(sqlUpdate)) {
            psU.setString(1,  safeStr(nm[0])); psU.setString(2,  safeCode(kd[0]));
            psU.setString(3,  safeStr(nm[1])); psU.setString(4,  safeCode(kd[1]));
            psU.setString(5,  safeStr(nm[2])); psU.setString(6,  safeCode(kd[2]));
            psU.setString(7,  safeStr(nm[3])); psU.setString(8,  safeCode(kd[3]));
            psU.setString(9,  safeStr(nm[4])); psU.setString(10, safeCode(kd[4]));
            psU.setString(11, noRawat);
            updated = psU.executeUpdate();
        }

        // Jika belum ada baris resume untuk no_rawat itu → INSERT minimal
        if (updated == 0) {
            String sqlInsert = "INSERT INTO " + resumeTable + " (" +
                    "no_rawat," +
                    kolomPairs[0] + "," + kolomPairs[1] + "," +
                    kolomPairs[2] + "," + kolomPairs[3] + "," +
                    kolomPairs[4] + "," + kolomPairs[5] + "," +
                    kolomPairs[6] + "," + kolomPairs[7] + "," +
                    kolomPairs[8] + "," + kolomPairs[9] +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement psI = conn.prepareStatement(sqlInsert)) {
                psI.setString(1,  noRawat);
                psI.setString(2,  safeStr(nm[0])); psI.setString(3,  safeCode(kd[0]));
                psI.setString(4,  safeStr(nm[1])); psI.setString(5,  safeCode(kd[1]));
                psI.setString(6,  safeStr(nm[2])); psI.setString(7,  safeCode(kd[2]));
                psI.setString(8,  safeStr(nm[3])); psI.setString(9,  safeCode(kd[3]));
                psI.setString(10, safeStr(nm[4])); psI.setString(11, safeCode(kd[4]));
                psI.executeUpdate();
            }
        }
    }

    private static String getNamaPenyakit(Connection conn, String kd) throws Exception {
        if (kd == null || kd.isEmpty()) return "";
        final String sql = "SELECT nm_penyakit FROM penyakit WHERE kd_penyakit=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }
}
