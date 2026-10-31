package permintaan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/** Susunan tampilan PA; indeks kolom tetap sesuai laporan dan tindakan lama. */
final class PermintaanLabPATable {
    private static final int[] KOLOM_ISIAN = {12, 13, 16, 17, 18, 19, 20, 21, 22, 23};

    private PermintaanLabPATable() {}

    static final class Pemeriksaan {
        final String nama;
        final String jenis;

        Pemeriksaan(String nama, String jenis) {
            this.nama = nama;
            this.jenis = jenis;
        }
    }

    private static String namaJenis(String jenis) {
        String kode = jenis == null ? "" : jenis.trim().toUpperCase(Locale.ROOT);
        switch (kode) {
            case "H": return "HISTOPATOLOGI";
            case "S": return "SITOLOGI";
            case "IHC": return "IMMUNOHISTOKIMIA";
            default: return "BELUM DIPETAKAN";
        }
    }

    private static String[] barisKosong() {
        String[] baris = new String[25];
        Arrays.fill(baris, "");
        return baris;
    }

    static String teks(DefaultTableModel model, int baris, int kolom) {
        Object nilai = model.getValueAt(baris, kolom);
        return nilai == null ? "" : nilai.toString();
    }

    static void tambahPermintaan(DefaultTableModel model, String[] data, List<Pemeriksaan> pemeriksaan) {
        String[] pasien = data.clone();
        for (int kolom : KOLOM_ISIAN) {
            pasien[kolom] = "";
        }
        pasien[24] = "";
        model.addRow(pasien);

        Map<String, List<String>> kelompok = new LinkedHashMap<>();
        for (Pemeriksaan item : pemeriksaan) {
            if (item != null && item.nama != null && !item.nama.trim().isEmpty()) {
                // Jenis berasal dari rsaj_labpa_jenis, terhubung lewat kd_jenis_prw.
                String jenis = namaJenis(item.jenis);
                kelompok.computeIfAbsent(jenis, key -> new ArrayList<>()).add(item.nama.trim());
            }
        }
        if (kelompok.isEmpty()) {
            kelompok.put("PEMERIKSAAN PA", Arrays.asList(""));
        }
        for (Map.Entry<String, List<String>> entry : kelompok.entrySet()) {
            String[] jenis = barisKosong();
            jenis[2] = entry.getKey();
            model.addRow(jenis);
            for (String nama : entry.getValue()) {
                String[] detail = barisKosong();
                detail[2] = "  " + (nama.isEmpty() ? "Belum ada pemeriksaan" : nama);
                detail[24] = nama;
                for (int kolom : KOLOM_ISIAN) {
                    detail[kolom] = data[kolom] == null ? "" : data[kolom];
                }
                model.addRow(detail);
            }
        }
        model.addRow(barisKosong());
    }

    static int barisInduk(DefaultTableModel model, int baris) {
        if (baris < 0 || baris >= model.getRowCount() || teks(model, baris, 2).isEmpty()) {
            return -1;
        }
        for (int indeks = baris; indeks >= 0; indeks--) {
            if (!teks(model, indeks, 0).isEmpty()) {
                return indeks;
            }
            if (teks(model, indeks, 2).isEmpty()) {
                break;
            }
        }
        return -1;
    }

    /** Satukan kembali data satu order untuk tombol tindakan dan laporan lama. */
    static String[] dataPermintaan(DefaultTableModel model, int baris) {
        int induk = barisInduk(model, baris);
        if (induk == -1) {
            return null;
        }
        String[] data = barisKosong();
        for (int kolom = 0; kolom < data.length; kolom++) {
            data[kolom] = teks(model, induk, kolom);
        }
        List<String> nama = new ArrayList<>();
        boolean isianDisalin = false;
        for (int indeks = induk + 1; indeks < model.getRowCount(); indeks++) {
            if (!teks(model, indeks, 0).isEmpty() || teks(model, indeks, 2).isEmpty()) {
                break;
            }
            if (teks(model, indeks, 2).startsWith("  ")) {
                if (!isianDisalin) {
                    for (int kolom : KOLOM_ISIAN) {
                        data[kolom] = teks(model, indeks, kolom);
                    }
                    isianDisalin = true;
                }
                if (!teks(model, indeks, 24).isEmpty()) {
                    nama.add(teks(model, indeks, 24));
                }
            }
        }
        data[24] = String.join(", ", nama);
        return data;
    }

    static int hitungCitoBelumSampel(DefaultTableModel model) {
        int jumlah = 0;
        for (int baris = 0; baris < model.getRowCount(); baris++) {
            if (!teks(model, baris, 0).isEmpty() && teks(model, baris, 5).isEmpty()) {
                String[] data = dataPermintaan(model, baris);
                if (data != null && data[12].toLowerCase(Locale.ROOT).contains("cito")) {
                    jumlah++;
                }
            }
        }
        return jumlah;
    }

    static void perbaruiSampel(DefaultTableModel model, String noorder, String tanggal, String jam) {
        int baris = cariPermintaan(model, noorder);
        if (baris != -1) {
            model.setValueAt(tanggal, baris, 5);
            model.setValueAt(jam, baris, 6);
        }
    }

    static int cariPermintaan(DefaultTableModel model, String noorder) {
        for (int baris = 0; baris < model.getRowCount(); baris++) {
            if (!noorder.isEmpty() && noorder.equals(teks(model, baris, 0))) {
                return baris;
            }
        }
        return -1;
    }
}
