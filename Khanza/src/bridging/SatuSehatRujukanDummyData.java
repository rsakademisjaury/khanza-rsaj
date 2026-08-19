package bridging;

/**
 * Data dummy resmi untuk uji SATUSEHAT STG / BPJS DEV.
 *
 * Sumber awal: dummy.xlsx yang diberikan untuk kebutuhan testing.
 * Ubah default data di getDefault() bila pihak BPJS/SatuSehat meminta pasangan
 * NIK pasien/dokter dummy yang lain.
 */
public final class SatuSehatRujukanDummyData {

    private SatuSehatRujukanDummyData() {}

    public static final class Data {
        public final String puskesmas;
        public final String nikPasien;
        public final String namaPasien;
        public final String ihsPasien;
        public final String noKartuJkn;
        public final String nikDokter;
        public final String namaDokter;
        public final String ihsDokter;

        public Data(String puskesmas, String nikPasien, String namaPasien,
                    String ihsPasien, String noKartuJkn, String nikDokter,
                    String namaDokter, String ihsDokter) {
            this.puskesmas = safe(puskesmas);
            this.nikPasien = safe(nikPasien);
            this.namaPasien = safe(namaPasien);
            this.ihsPasien = safe(ihsPasien);
            this.noKartuJkn = safe(noKartuJkn);
            this.nikDokter = safe(nikDokter);
            this.namaDokter = safe(namaDokter);
            this.ihsDokter = safe(ihsDokter);
        }

        private static String safe(String s) {
            return s == null ? "" : s.trim();
        }
    }

    public static Data getDefault() {
        return CIBUNTU_1;
    }

    public static final Data CIBUNTU_1 = new Data(
            "CIBUNTU",
            "3273154807710007",
            "patient_cibuntu_1",
            "P36506927381",
            "0001257561235",
            "3217050810920006",
            "doctor_cibuntu_1",
            "10028977993"
    );

    public static final Data PAGARSIH_2 = new Data(
            "PAGARSIH",
            "3273045909960002",
            "patient_pagarsih_2",
            "P37868936822",
            "0001465050137",
            "3277034911890002",
            "doctor_pagarsih_2",
            "10017858452"
    );

    public static final Data BABAKAN_SURABAYA_6 = new Data(
            "BABAKAN SURABAYA",
            "3273160905020005",
            "patient_babakansurabaya_6",
            "P37481602400",
            "0001796906125",
            "3204086406870005",
            "doctor_babakansurabaya_2",
            "10011646313"
    );
}
