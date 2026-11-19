package bridging;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class JadwalKontrolRanap {
    private static final List<String> HARI = List.of("SENIN", "SELASA", "RABU", "KAMIS", "JUMAT", "SABTU", "AKHAD");
    private static final String DOKTER_RS =
            "select kd_dokter from maping_dokter_dpjpvclaim where kd_dokter_bpjs=?";
    private static final String POLI_RS = "select kd_poli_rs from maping_poli_bpjs where kd_poli_bpjs=?";

    static final class Sesi {
        final String hari;
        final String jam;
        final int kuota;
        final boolean libur;

        Sesi(String hari, String jam, int kuota, boolean libur) {
            this.hari = hari;
            this.jam = jam;
            this.kuota = kuota;
            this.libur = libur;
        }
    }

    static final class Hasil {
        final List<Sesi> jadwalTanggal;
        final List<Sesi> jadwalMingguan;
        final boolean dariHfis;
        final int terisi;
        final int kuota;

        Hasil(List<Sesi> jadwalTanggal, List<Sesi> jadwalMingguan, boolean dariHfis, int terisi) {
            this.jadwalTanggal = jadwalTanggal;
            this.jadwalMingguan = jadwalMingguan;
            this.dariHfis = dariHfis;
            this.terisi = terisi;
            kuota = jadwalTanggal.stream().filter(sesi -> !sesi.libur).mapToInt(sesi -> sesi.kuota).sum();
        }

        boolean adaPraktik() { return jadwalTanggal.stream().anyMatch(sesi -> !sesi.libur); }
        int sisa() { return Math.max(0, kuota - terisi); }
    }

    private JadwalKontrolRanap() {
    }

    static String hari(LocalDate tanggal) {
        return HARI.get(tanggal.getDayOfWeek().getValue() - 1);
    }

    static Hasil cek(Connection db, String dokter, String poli, LocalDate tanggal, List<Sesi> hfis) throws SQLException {
        pastikanMapping(db, dokter, poli);
        List<Sesi> mingguan = mingguan(db, dokter, poli);
        List<Sesi> hariIni = hfis;
        if (hariIni == null) {
            hariIni = new ArrayList<>();
            for (Sesi sesi : mingguan) {
                if (sesi.hari.equals(hari(tanggal))) {
                    hariIni.add(sesi);
                }
            }
        }
        int terisi = terdaftar(db, dokter, poli, tanggal);
        return new Hasil(Collections.unmodifiableList(hariIni), Collections.unmodifiableList(mingguan), hfis != null, terisi);
    }

    private static void pastikanMapping(Connection db, String dokter, String poli) throws SQLException {
        String sql = "select 1 from maping_dokter_dpjpvclaim d cross join maping_poli_bpjs p "
                + "where d.kd_dokter_bpjs=? and p.kd_poli_bpjs=? limit 1";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, dokter);
            ps.setString(2, poli);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Mapping dokter atau poli BPJS ke SIMRS belum tersedia.", "02000");
                }
            }
        }
    }

    private static List<Sesi> mingguan(Connection db, String dokter, String poli) throws SQLException {
        String sql = "select distinct upper(trim(hari_kerja)),jam_mulai,jam_selesai,greatest(coalesce(kuota,0),0) "
                + "from jadwal where kd_dokter in (" + DOKTER_RS + ") and kd_poli in (" + POLI_RS + ")";
        List<Sesi> mingguan = new ArrayList<>();
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, dokter);
            ps.setString(2, poli);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mingguan.add(new Sesi(rs.getString(1), jam(rs.getString(2)) + "–" + jam(rs.getString(3)),
                            rs.getInt(4), false));
                }
            }
        }
        mingguan.sort(java.util.Comparator.comparingInt((Sesi sesi) -> HARI.indexOf(sesi.hari))
                .thenComparing(sesi -> sesi.jam));
        return mingguan;
    }

    private static String jam(String nilai) {
        return nilai == null || nilai.length() < 5 ? "—" : nilai.substring(0, 5);
    }

    private static int terdaftar(Connection db, String dokter, String poli, LocalDate tanggal) throws SQLException {
        String sql = "select count(*) from ("
                + "select case when nullif(trim(r.no_rkm_medis),'') is null then concat('RAWAT:',r.no_rawat) "
                + "else concat('RM:',trim(r.no_rkm_medis)) end as pasien "
                + "from reg_periksa r where r.kd_dokter in (" + DOKTER_RS + ") and r.kd_poli in (" + POLI_RS + ") "
                + "and r.tgl_registrasi=? and r.stts<>'Batal' "
                + "union select concat('RM:',trim(b.no_rkm_medis)) from booking_registrasi b "
                + "where b.kd_dokter in (" + DOKTER_RS + ") and b.kd_poli in (" + POLI_RS + ") "
                + "and b.tanggal_periksa=? and b.status in ('Belum','Terdaftar') "
                + "union select case when coalesce(nullif(trim(r.no_rkm_medis),''),"
                + "case when trim(j.norm) not in ('','-','0') then trim(j.norm) end) is not null "
                + "then concat('RM:',coalesce(nullif(trim(r.no_rkm_medis),''),trim(j.norm))) "
                + "else concat('JKN:',j.nobooking) end from referensi_mobilejkn_bpjs j "
                + "left join reg_periksa r on r.no_rawat=j.no_rawat "
                + "where j.kodedokter=? and j.kodepoli=? and j.tanggalperiksa=? and j.status in ('Belum','Checkin') "
                + "and (r.no_rawat is null or coalesce(r.stts,'')<>'Batal')) antrean";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            for (int indeks = 1; indeks <= 7; indeks += 3) {
                ps.setString(indeks, dokter);
                ps.setString(indeks + 1, poli);
                ps.setDate(indeks + 2, Date.valueOf(tanggal));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
