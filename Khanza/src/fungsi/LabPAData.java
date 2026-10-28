package fungsi;

import java.sql.*;
import java.util.*;

/** Penyimpanan tambahan PA; tidak mengubah struktur tabel utama Khanza. */
public final class LabPAData {
    private LabPAData() {}
    public static final String[] JENIS = {"", "S", "H", "IHC"};
    private static final String COLUMNS = "jenis,jenis_jaringan,lokasi_jaringan,cara_mendapatkan,cairan_fiksasi,"
            + "rs_sebelumnya,no_pa_sebelumnya,diagnosa_pa,permintaan_ihc,pemeriksaan_ihc";
    private static final String UPDATE = "jenis=values(jenis),jenis_jaringan=values(jenis_jaringan),"
            + "lokasi_jaringan=values(lokasi_jaringan),cara_mendapatkan=values(cara_mendapatkan),"
            + "cairan_fiksasi=values(cairan_fiksasi),rs_sebelumnya=values(rs_sebelumnya),"
            + "no_pa_sebelumnya=values(no_pa_sebelumnya),diagnosa_pa=values(diagnosa_pa),"
            + "permintaan_ihc=values(permintaan_ihc),pemeriksaan_ihc=values(pemeriksaan_ihc),"
            + "updated_at=now(),updated_by=values(updated_by)";
    public static String text(Object value) { return value == null ? "" : value.toString().trim(); }
    public static String namaJenis(String jenis) {
        if ("S".equals(jenis)) return "Sitologi";
        if ("H".equals(jenis)) return "Histopatologi";
        if ("IHC".equals(jenis)) return "Imunohistokimia";
        return "Pilih jenis pemeriksaan";
    }
    public static boolean validJenis(String jenis) {
        return "S".equals(jenis) || "H".equals(jenis) || "IHC".equals(jenis);
    }
    public static String kenaliNama(String nama) {
        String n = text(nama).toLowerCase(Locale.ROOT);
        if (n.contains("imunohisto") || n.contains("immunohisto") || n.matches(".*\\b(ihc|ihk)\\b.*")) return "IHC";
        if (n.contains("sitolog") || n.contains("cytolog") || n.matches(".*\\b(fnab|fnac|fna)\\b.*") || n.contains("pap smear")) return "S";
        if (n.contains("histopatolog") || n.contains("histolog")) return "H";
        return "";
    }
    public static String kenaliKode(String kode) {
        String k=text(kode).toUpperCase(Locale.ROOT);
        if(k.contains("PAIHC"))return "IHC";
        if(k.contains("PAH"))return "H";
        if(k.contains("PAS"))return "S";
        return "";
    }
    public static String jenis(Connection c, String kode, String nama) throws SQLException {
        String dariKode=kenaliKode(kode);if(validJenis(dariKode))return dariKode;
        try (PreparedStatement p = c.prepareStatement("select jenis from rsaj_labpa_jenis where kd_jenis_prw=?")) {
            p.setString(1,kode);
            try (ResultSet r=p.executeQuery()) { if(r.next() && validJenis(r.getString(1))) return r.getString(1); }
        }
        return kenaliNama(nama);
    }
    public static final class Bahan {
        public String jenis="", jenisJaringan="", lokasi="", cara="", fiksasi="", rsSebelum="",
                noSebelum="", diagnosaPA="", permintaanIHC="", pemeriksaanIHC="", noorder="";
        public Bahan copy() {
            Bahan b=new Bahan(); b.jenis=jenis; b.jenisJaringan=jenisJaringan; b.lokasi=lokasi;
            b.cara=cara; b.fiksasi=fiksasi; b.rsSebelum=rsSebelum; b.noSebelum=noSebelum;
            b.diagnosaPA=diagnosaPA; b.permintaanIHC=permintaanIHC; b.pemeriksaanIHC=pemeriksaanIHC; b.noorder=noorder; return b;
        }
        public String wajibKosong() {
            if(!validJenis(jenis)) return "Jenis pemeriksaan";
            if(text(jenisJaringan).isEmpty()) return "Jenis Jaringan";
            if(text(lokasi).isEmpty()) return "Lokasi/Bahan Jaringan";
            if("IHC".equals(jenis)) {
                if(text(rsSebelum).isEmpty()) return "RS sebelumnya";
                if(text(noSebelum).isEmpty()) return "No. PA sebelumnya";
                if(text(diagnosaPA).isEmpty()) return "Diagnosa PA";
                if(text(permintaanIHC).isEmpty()) return "Permintaan IHC";
            } else {
                if(text(cara).isEmpty()) return "Cara Mendapatkan Jaringan";
                if(text(fiksasi).isEmpty()) return "Cairan Fiksasi";
            }
            return "";
        }
    }
    public static final class Kunci {
        public final String noRawat,kode,tanggal,jam;
        public Kunci(String rawat,String kode,String tanggal,String jam) {
            this.noRawat=text(rawat); this.kode=text(kode); this.tanggal=text(tanggal); this.jam=text(jam);
        }
        public int bind(PreparedStatement p,int i) throws SQLException {
            p.setString(i++,noRawat); p.setString(i++,kode); p.setString(i++,tanggal); p.setString(i++,jam); return i;
        }
        public String token() { return noRawat+"|"+kode+"|"+tanggal+"|"+jam; }
    }
    private static Bahan read(ResultSet r) throws SQLException {
        Bahan b=new Bahan(); b.jenis=text(r.getString("jenis")); b.jenisJaringan=text(r.getString("jenis_jaringan"));
        b.lokasi=text(r.getString("lokasi_jaringan")); b.cara=text(r.getString("cara_mendapatkan")); b.fiksasi=text(r.getString("cairan_fiksasi"));
        b.rsSebelum=text(r.getString("rs_sebelumnya")); b.noSebelum=text(r.getString("no_pa_sebelumnya"));
        b.diagnosaPA=text(r.getString("diagnosa_pa")); b.permintaanIHC=text(r.getString("permintaan_ihc"));
        b.pemeriksaanIHC=text(r.getString("pemeriksaan_ihc")); return b;
    }
    private static int bindBahan(PreparedStatement p,int i,Bahan b) throws SQLException {
        for(String s:new String[]{b.jenis,b.jenisJaringan,b.lokasi,b.cara,b.fiksasi,b.rsSebelum,b.noSebelum,b.diagnosaPA,b.permintaanIHC,b.pemeriksaanIHC}) p.setString(i++,text(s));
        return i;
    }
    public static void simpanPermintaan(Connection c,String order,String kode,Bahan b,String user) throws SQLException {
        if(!b.wajibKosong().isEmpty())throw new SQLException("Maaf, tidak boleh kosong");
        if(c.getAutoCommit()) throw new SQLException("Detail PA harus disimpan bersama transaksi permintaan.");
        try(PreparedStatement cek=c.prepareStatement("select noorder from permintaan_pemeriksaan_labpa where noorder=? and kd_jenis_prw=?")){
            cek.setString(1,order);cek.setString(2,kode);try(ResultSet r=cek.executeQuery()){if(!r.next())throw new SQLException("Detail pemeriksaan permintaan PA belum tersimpan.");}
        }
        try(PreparedStatement p=c.prepareStatement("insert into rsaj_labpa_permintaan (noorder,kd_jenis_prw,"+COLUMNS+",updated_at,updated_by) values (?,?,?,?,?,?,?,?,?,?,?,?,now(),?) on duplicate key update "+UPDATE)) {
            p.setString(1,order); p.setString(2,kode); int i=bindBahan(p,3,b); p.setString(i,user); p.executeUpdate();
        }
    }
    private static final String WHERE_KEY="no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?";
    public static Bahan muat(Connection c,Kunci k,String nama) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("select noorder,"+COLUMNS+" from rsaj_labpa_hasil where "+WHERE_KEY)) {
            k.bind(p,1); try(ResultSet r=p.executeQuery()) {if(r.next()){Bahan b=read(r); b.noorder=text(r.getString("noorder")); return b;}}
        }
        Bahan b=new Bahan(); b.jenis=jenis(c,k.kode,nama);
        // Jangan pilih order hanya dari no_rawat. Waktu hasil DAN kode pemeriksaan harus cocok.
        String query="select p.* from permintaan_labpa p inner join permintaan_pemeriksaan_labpa d on d.noorder=p.noorder "
                +"where p.no_rawat=? and p.tgl_hasil=? and p.jam_hasil=? and d.kd_jenis_prw=? order by p.noorder desc";
        try(PreparedStatement p=c.prepareStatement(query)) {
            p.setString(1,k.noRawat);p.setString(2,k.tanggal);p.setString(3,k.jam);p.setString(4,k.kode);
            try(ResultSet r=p.executeQuery()) {
                if(r.next()) {
                    b.noorder=text(r.getString("noorder"));
                    // Posisi ini mengikuti 20 nilai INSERT form kiriman; tidak menebak nama kolom lama.
                    b.cara=text(r.getString(14));b.lokasi=text(r.getString(15));b.fiksasi=text(r.getString(16));
                    b.rsSebelum=text(r.getString(17));b.noSebelum=text(r.getString(19));b.diagnosaPA=text(r.getString(20));
                    if(r.next()) throw new SQLException("Lebih dari satu permintaan cocok. Periksa hubungan permintaan dan hasil PA.");
                }
            }
        }
        if(!b.noorder.isEmpty()) {
            try(PreparedStatement p=c.prepareStatement("select "+COLUMNS+" from rsaj_labpa_permintaan where noorder=? and kd_jenis_prw=?")) {
                p.setString(1,b.noorder);p.setString(2,k.kode);
                try(ResultSet r=p.executeQuery()){if(r.next()){Bahan x=read(r);x.noorder=b.noorder;return x;}}
            }
        }
        return b;
    }
    public static void simpanHasil(Connection c,Kunci k,Bahan b,String user) throws SQLException {
        if(!validJenis(b.jenis))throw new SQLException("Pilih jenis pemeriksaan terlebih dahulu.");
        boolean sendiri=c.getAutoCommit();
        if(sendiri)c.setAutoCommit(false);
        try{
            try(PreparedStatement lock=c.prepareStatement("select no_rawat from periksa_lab where "+WHERE_KEY+" and kategori='PA' for update")){
                k.bind(lock,1);try(ResultSet r=lock.executeQuery()){if(!r.next())throw new SQLException("Pemeriksaan PA tidak ditemukan.");}
            }
        String issued=jenisNomor(c,k);
        if(!issued.isEmpty()&&!issued.equals(b.jenis)) throw new SQLException("Jenis pemeriksaan tidak dapat diubah setelah No. PA diterbitkan.");
        try(PreparedStatement p=c.prepareStatement("insert into rsaj_labpa_hasil (no_rawat,kd_jenis_prw,tgl_periksa,jam,noorder,"+COLUMNS+",updated_at,updated_by) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?) on duplicate key update noorder=values(noorder),"+UPDATE)) {
            int i=k.bind(p,1);p.setString(i++,b.noorder);i=bindBahan(p,i,b);p.setString(i,user);p.executeUpdate();
        }
            if(sendiri)c.commit();
        }catch(SQLException|RuntimeException ex){if(sendiri)c.rollback();throw ex;}
        finally{if(sendiri)c.setAutoCommit(true);}
    }
    public static String nomor(Connection c,Kunci k) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("select no_pa from rsaj_labpa_nomor where "+WHERE_KEY)){
            k.bind(p,1);try(ResultSet r=p.executeQuery()){return r.next()?text(r.getString(1)):"";}
        }
    }
    public static String jenisNomor(Connection c,Kunci k) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("select jenis from rsaj_labpa_nomor where "+WHERE_KEY)){
            k.bind(p,1);try(ResultSet r=p.executeQuery()){return r.next()?text(r.getString(1)):"";}
        }
    }
    public static String formatNomor(String jenis,int tahun,long urutan) {
        if(!validJenis(jenis)||urutan<1)throw new IllegalArgumentException("Jenis/urutan No. PA tidak valid.");
        return String.format(Locale.ROOT,"%s.%02d.%03d",jenis,tahun%100,urutan);
    }
    public static String terbitkan(Connection c,Kunci k,String jenis,String user) throws SQLException {
        if(!validJenis(jenis))throw new SQLException("Pilih jenis pemeriksaan terlebih dahulu.");
        synchronized(c){
            if(!c.getAutoCommit())throw new SQLException("Selesaikan transaksi aktif sebelum menerbitkan No. PA.");
            c.setAutoCommit(false);
            try{
                // Kunci baris pemeriksaan dahulu: klik ulang dan dua petugas tidak menerbitkan ulang.
                try(PreparedStatement p=c.prepareStatement("select no_rawat from periksa_lab where "+WHERE_KEY+" and kategori='PA' for update")){
                    k.bind(p,1);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Pemeriksaan PA tidak ditemukan.");}
                }
                String ada=nomor(c,k);
                if(!ada.isEmpty()){c.commit();return ada;}
                try(PreparedStatement p=c.prepareStatement("select jenis from rsaj_labpa_hasil where "+WHERE_KEY)){
                    k.bind(p,1);try(ResultSet r=p.executeQuery()){
                        if(!r.next()||!jenis.equals(r.getString(1)))throw new SQLException("Jenis pemeriksaan berubah. Muat ulang data sebelum menerbitkan nomor.");
                    }
                }
                int tahun;
                try(PreparedStatement p=c.prepareStatement("select year(current_date())" );ResultSet r=p.executeQuery()){
                    if(!r.next())throw new SQLException("Tahun server tidak dapat dibaca.");tahun=r.getInt(1);
                }
                try(PreparedStatement p=c.prepareStatement("insert ignore into rsaj_labpa_counter (jenis,tahun,nomor_terakhir) values (?,?,0)")){
                    p.setString(1,jenis);p.setInt(2,tahun);p.executeUpdate();
                }
                long next;
                try(PreparedStatement p=c.prepareStatement("select nomor_terakhir from rsaj_labpa_counter where jenis=? and tahun=? for update")){
                    p.setString(1,jenis);p.setInt(2,tahun);try(ResultSet r=p.executeQuery()){
                        if(!r.next())throw new SQLException("Urutan No. PA tidak dapat dibaca.");next=r.getLong(1)+1;
                    }
                }
                String nomor=formatNomor(jenis,tahun,next);
                try(PreparedStatement p=c.prepareStatement("insert into rsaj_labpa_nomor (no_rawat,kd_jenis_prw,tgl_periksa,jam,jenis,tahun,urutan,no_pa,issued_at,issued_by) values (?,?,?,?,?,?,?,?,now(),?)")){
                    int i=k.bind(p,1);p.setString(i++,jenis);p.setInt(i++,tahun);p.setLong(i++,next);p.setString(i++,nomor);p.setString(i,user);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("update rsaj_labpa_counter set nomor_terakhir=? where jenis=? and tahun=?")){
                    p.setLong(1,next);p.setString(2,jenis);p.setInt(3,tahun);if(p.executeUpdate()!=1)throw new SQLException("Urutan No. PA gagal diperbarui.");
                }
                c.commit();return nomor;
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
            finally{c.setAutoCommit(true);}
        }
    }
}
