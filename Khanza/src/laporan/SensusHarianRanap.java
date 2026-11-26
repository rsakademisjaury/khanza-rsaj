package laporan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class SensusHarianRanap {
    static final Ruang SEMUA_RUANG = new Ruang("", "Semua Ruang Rawat");

    private static final int AWAL = 1;
    private static final int MASUK = 2;
    private static final int PINDAHAN = 3;
    private static final int JUMLAH_MASUK = 4;
    private static final int KELUAR_HIDUP = 5;
    private static final int DIPINDAHKAN = 6;
    private static final int MATI = 7;
    private static final int MATI_DI_BAWAH_48 = 8;
    private static final int MATI_MINIMAL_48 = 9;
    private static final int JUMLAH_KELUAR = 10;
    private static final int LAMA_RAWAT = 11;
    private static final int MASUK_KELUAR_HARI_SAMA = 12;
    private static final int SISA = 13;
    private static final int VIP = 14;
    private static final int KELAS_I = 15;
    private static final int KELAS_II = 16;
    private static final int KELAS_III = 17;
    private static final int TANPA_KELAS = 18;
    private static final String BED_AKTIF = "statusdata = '1'";

    private static final String SQL_RIWAYAT =
            "select ri.no_rawat, k.kd_bangsal, k.kelas, ri.stts_pulang, "
            + "cast(ri.tgl_masuk as char(10)) as tanggal_masuk, ri.jam_masuk, "
            + "cast(ri.tgl_keluar as char(10)) as tanggal_keluar, ri.jam_keluar "
            + "from (select distinct kandidat.no_rawat from kamar_inap kandidat %s "
            + "where kandidat.tgl_masuk < ? and (kandidat.tgl_keluar >= ? "
            + "or kandidat.stts_pulang = '-' or kandidat.tgl_keluar is null "
            + "or kandidat.tgl_keluar < kandidat.tgl_masuk) %s) episode "
            + "join kamar_inap ri on ri.no_rawat = episode.no_rawat "
            + "join kamar k on k.kd_kamar = ri.kd_kamar "
            + "order by ri.no_rawat, ri.tgl_masuk, ri.jam_masuk";

    static final class Ruang {
        final String kode;
        final String nama;

        Ruang(String kode, String nama) {
            this.kode = kode;
            this.nama = nama;
        }

        boolean semua() {
            return kode.isEmpty();
        }

        @Override
        public String toString() {
            return semua() ? nama : nama + " (" + kode + ")";
        }
    }

    static final class Rekap {
        final Integer[][] baris;
        final int tempatTidur;
        final int riwayatPerluVerifikasi;

        Rekap(Integer[][] baris, int tempatTidur, int riwayatPerluVerifikasi) {
            this.baris = baris;
            this.tempatTidur = tempatTidur;
            this.riwayatPerluVerifikasi = riwayatPerluVerifikasi;
        }
    }

    private static final class Singgah {
        final String kodeRuang;
        final String kelas;
        final String status;
        final LocalDateTime masuk;
        final LocalDateTime keluar;

        Singgah(String kodeRuang, String kelas, String status, LocalDateTime masuk, LocalDateTime keluar) {
            this.kodeRuang = kodeRuang;
            this.kelas = kelas;
            this.status = status;
            this.masuk = masuk;
            this.keluar = keluar;
        }

        boolean ditandaiPindah() {
            return "Pindah Kamar".equals(status);
        }

        boolean meninggal() {
            return "Meninggal".equals(status) || "+".equals(status);
        }
    }

    private final LocalDate awalBulan;
    private final LocalDateTime batas;
    private final Ruang ruang;
    private final Integer[][] baris;
    private int riwayatPerluVerifikasi;

    private SensusHarianRanap(YearMonth bulan, Ruang ruang, LocalDateTime sekarang) {
        this.ruang = ruang;
        awalBulan = bulan.atDay(1);
        LocalDateTime akhirBulan = bulan.plusMonths(1).atDay(1).atStartOfDay();
        batas = sekarang.isBefore(akhirBulan) ? sekarang : akhirBulan;
        baris = new Integer[bulan.lengthOfMonth()][19];
        for (int i = 0; i < baris.length; i++) {
            if (awalBulan.plusDays(i).atStartOfDay().isBefore(batas)) {
                java.util.Arrays.fill(baris[i], 0);
            }
            baris[i][0] = i + 1;
        }
    }

    static List<Ruang> daftarRuang(Connection koneksi) throws SQLException {
        List<Ruang> daftar = new ArrayList<>();
        daftar.add(SEMUA_RUANG);
        String sql = "select b.kd_bangsal, coalesce(nullif(trim(b.nm_bangsal), ''), b.kd_bangsal) as nm_bangsal from bangsal b "
                + "where exists (select 1 from kamar k where k.kd_bangsal = b.kd_bangsal and k." + BED_AKTIF + ") "
                + "order by b.nm_bangsal, b.kd_bangsal";
        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setQueryTimeout(30);
            try (ResultSet hasil = perintah.executeQuery()) {
                while (hasil.next()) {
                    daftar.add(new Ruang(hasil.getString("kd_bangsal"), hasil.getString("nm_bangsal")));
                }
            }
        }
        return daftar;
    }

    static Rekap muat(Connection koneksi, YearMonth bulan, Ruang ruang, LocalDateTime sekarang)
            throws SQLException {
        SensusHarianRanap sensus = new SensusHarianRanap(bulan, ruang, sekarang);
        if (sensus.awalBulan.atStartOfDay().isBefore(sensus.batas)) {
            sensus.bacaRiwayat(koneksi);
            sensus.hitungJumlah();
        }
        return new Rekap(sensus.baris, jumlahTempatTidur(koneksi, ruang), sensus.riwayatPerluVerifikasi);
    }

    private static int jumlahTempatTidur(Connection koneksi, Ruang ruang) throws SQLException {
        String sql = "select count(*) from kamar where " + BED_AKTIF;
        if (!ruang.semua()) {
            sql += " and kd_bangsal = ?";
        }
        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            if (!ruang.semua()) {
                perintah.setString(1, ruang.kode);
            }
            perintah.setQueryTimeout(30);
            try (ResultSet hasil = perintah.executeQuery()) {
                hasil.next();
                return hasil.getInt(1);
            }
        }
    }

    private void bacaRiwayat(Connection koneksi) throws SQLException {
        String sql = String.format(Locale.ROOT, SQL_RIWAYAT,
                ruang.semua() ? "" : "join kamar kr on kr.kd_kamar = kandidat.kd_kamar",
                ruang.semua() ? "" : "and kr.kd_bangsal = ?");
        LocalDate akhirTanggal = batas.toLocalDate();
        if (!batas.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            akhirTanggal = akhirTanggal.plusDays(1);
        }
        try (PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, akhirTanggal.toString());
            perintah.setString(2, awalBulan.toString());
            if (!ruang.semua()) {
                perintah.setString(3, ruang.kode);
            }
            perintah.setQueryTimeout(60);
            try (ResultSet hasil = perintah.executeQuery()) {
                List<Singgah> episode = new ArrayList<>();
                String noRawat = null;
                while (hasil.next()) {
                    String noRawatBaris = hasil.getString("no_rawat");
                    if (!noRawatBaris.equals(noRawat)) {
                        catatEpisode(episode);
                        episode.clear();
                        noRawat = noRawatBaris;
                    }
                    LocalDateTime masuk = waktu(hasil.getString("tanggal_masuk"), hasil.getString("jam_masuk"));
                    if (masuk == null) {
                        riwayatPerluVerifikasi++;
                    } else {
                        String status = hasil.getString("stts_pulang");
                        LocalDateTime keluar = "-".equals(status) ? null
                                : waktu(hasil.getString("tanggal_keluar"), hasil.getString("jam_keluar"));
                        episode.add(new Singgah(hasil.getString("kd_bangsal"), hasil.getString("kelas"),
                                status, masuk, keluar));
                    }
                }
                catatEpisode(episode);
            }
        }
    }

    private static LocalDateTime waktu(String tanggal, String jam) {
        if (tanggal == null || tanggal.startsWith("0000-") || jam == null) {
            return null;
        }
        try {
            return LocalDateTime.of(LocalDate.parse(tanggal), LocalTime.parse(jam));
        } catch (java.time.DateTimeException ex) {
            return null;
        }
    }

    private void catatEpisode(List<Singgah> riwayat) {
        boolean pindahan = false;
        boolean sambungDariAsal = false;
        LocalDateTime masukRS = null;
        for (int i = 0; i < riwayat.size(); i++) {
            Singgah singgah = riwayat.get(i);
            if (!singgah.masuk.isBefore(batas)) {
                break;
            }
            Singgah berikutnya = i + 1 < riwayat.size() ? riwayat.get(i + 1) : null;
            LocalDateTime keluar = singgah.keluar;
            boolean perluVerifikasi = false;
            if (keluar != null && keluar.isBefore(singgah.masuk)) {
                keluar = null;
                perluVerifikasi = true;
            }

            boolean pindah = singgah.ditandaiPindah();
            if (berikutnya != null && (keluar == null || keluar.isAfter(berikutnya.masuk))) {
                perluVerifikasi = true;
                pindah = true;
                keluar = berikutnya.masuk;
            }
            boolean pindahTerhubung = pindah && berikutnya != null;
            // Mutasi antarbangsal dicatat pada kedua rekap dengan waktu aktual masing-masing.
            boolean sambungKeTujuan = pindahTerhubung && berikutnya.masuk.equals(keluar)
                    && java.util.Objects.equals(singgah.kodeRuang, berikutnya.kodeRuang);

            if (!pindahan) {
                masukRS = singgah.masuk;
            }
            if (ruang.semua() || ruang.kode.equals(singgah.kodeRuang)) {
                if (perluVerifikasi || keluarPadaPeriode(keluar)
                        && (pindah && !pindahTerhubung || "Status Belum Lengkap".equals(singgah.status))
                        || keluar == null && !"-".equals(singgah.status)) {
                    riwayatPerluVerifikasi++;
                }
                if (!sambungDariAsal) {
                    tambah(singgah.masuk, pindahan ? PINDAHAN : MASUK, 1);
                }
                if (keluarPadaPeriode(keluar) && !sambungKeTujuan) {
                    catatKeluar(singgah, masukRS, keluar, pindah);
                }
                catatSaldo(singgah, keluar);
            }
            pindahan = pindahTerhubung;
            sambungDariAsal = sambungKeTujuan;
        }
    }

    private void catatKeluar(Singgah singgah, LocalDateTime masukRS, LocalDateTime keluar, boolean pindah) {
        if (pindah) {
            tambah(keluar, DIPINDAHKAN, 1);
            return;
        }
        if (singgah.meninggal()) {
            boolean diBawah48Jam = Duration.between(masukRS, keluar).compareTo(Duration.ofHours(48)) < 0;
            tambah(keluar, diBawah48Jam ? MATI_DI_BAWAH_48 : MATI_MINIMAL_48, 1);
        } else {
            tambah(keluar, KELUAR_HIDUP, 1);
            if (masukRS.toLocalDate().equals(keluar.toLocalDate())) {
                tambah(keluar, MASUK_KELUAR_HARI_SAMA, 1);
            }
        }
        tambah(keluar, LAMA_RAWAT, (int) ChronoUnit.DAYS.between(masukRS.toLocalDate(), keluar.toLocalDate()));
    }

    private void catatSaldo(Singgah singgah, LocalDateTime keluar) {
        int kelas = kolomKelas(singgah.kelas);
        for (int i = 0; i < baris.length && baris[i][AWAL] != null; i++) {
            LocalDateTime awal = awalBulan.plusDays(i).atStartOfDay();
            LocalDateTime akhir = awal.plusDays(1);
            if (batas.isBefore(akhir)) {
                akhir = batas;
            }
            if (adaSebelum(singgah.masuk, keluar, awal)) {
                baris[i][AWAL]++;
            }
            if (adaSebelum(singgah.masuk, keluar, akhir)) {
                baris[i][kelas]++;
            }
        }
    }

    private static boolean adaSebelum(LocalDateTime masuk, LocalDateTime keluar, LocalDateTime waktu) {
        return masuk.isBefore(waktu) && (keluar == null || !keluar.isBefore(waktu));
    }

    private boolean keluarPadaPeriode(LocalDateTime keluar) {
        return keluar != null && keluar.isBefore(batas) && !keluar.toLocalDate().isBefore(awalBulan);
    }

    private void tambah(LocalDateTime tanggal, int kolom, int nilai) {
        if (!tanggal.toLocalDate().isBefore(awalBulan) && tanggal.isBefore(batas)) {
            baris[tanggal.getDayOfMonth() - 1][kolom] += nilai;
        }
    }

    private void hitungJumlah() {
        for (Integer[] hari : baris) {
            if (hari[AWAL] != null) {
                hari[JUMLAH_MASUK] = hari[AWAL] + hari[MASUK] + hari[PINDAHAN];
                hari[MATI] = hari[MATI_DI_BAWAH_48] + hari[MATI_MINIMAL_48];
                hari[JUMLAH_KELUAR] = hari[KELUAR_HIDUP] + hari[DIPINDAHKAN] + hari[MATI];
                hari[SISA] = hari[JUMLAH_MASUK] - hari[JUMLAH_KELUAR];
            }
        }
    }

    private static int kolomKelas(String kelas) {
        String nilai = kelas == null ? "" : kelas.trim().toUpperCase(Locale.ROOT);
        switch (nilai) {
            case "KELAS VIP": case "KELAS VVIP": case "KELAS UTAMA": case "VIP": case "VVIP": case "UTAMA":
                return VIP;
            case "KELAS 1": case "KELAS I": case "1": case "I":
                return KELAS_I;
            case "KELAS 2": case "KELAS II": case "2": case "II":
                return KELAS_II;
            case "KELAS 3": case "KELAS III": case "3": case "III":
                return KELAS_III;
            default:
                return TANPA_KELAS;
        }
    }
}
