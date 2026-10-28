package bridging;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mengganti referensi aktif secara atomik sesudah seluruh file baru terverifikasi. */
final class SuratLepasRawatBerkas {
    interface Gateway {
        List<File> render(String surat,long job) throws Exception;
        String upload(File file) throws Exception;
        void bersihkan(List<File> files);
    }
    static final class Ref {
        final String rawat,kode,path;
        Ref(String rawat,String kode,String path) { this.rawat=rawat; this.kode=kode; this.path=path; }
        String key() { return rawat+"|"+kode+"|"+path; }
    }
    private static final class Job {
        long id; String surat,sep,rm,rawat,petugas; LocalDate tanggal;
    }
    private final SuratLepasRawatService.Connections connections;
    private final Gateway gateway;
    SuratLepasRawatBerkas(SuratLepasRawatService.Connections connections,Gateway gateway) {
        this.connections=connections; this.gateway=gateway;
    }
    static void siap(Connection c) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM information_schema.TRIGGERS "
                + "WHERE TRIGGER_SCHEMA=DATABASE() AND TRIGGER_NAME='rsaj_slr_document_job'");ResultSet rs=ps.executeQuery()) {
            if(!rs.next()||rs.getInt(1)!=1) throw new SQLException("Jalankan rsaj_surat_lepas_rawat_r2.sql terlebih dahulu.");
        }
        try(PreparedStatement ps=c.prepareStatement("SELECT j.id,b.id FROM rsaj_lepas_rawat_berkas_job j "
                + "LEFT JOIN rsaj_lepas_rawat_berkas b ON 1=0 LIMIT 0")) { ps.executeQuery().close(); }
        // Penukaran referensi hanya dijalankan pada tabel transaksi.
        try(PreparedStatement ps=c.prepareStatement("SELECT ENGINE FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='berkas_digital_perawatan'");ResultSet rs=ps.executeQuery()) {
            if(!rs.next()||!"InnoDB".equalsIgnoreCase(rs.getString(1)))
                throw new SQLException("Berkas digital memerlukan tabel InnoDB agar penggantian dapat dibatalkan jika gagal.");
        }
        try(PreparedStatement ps=c.prepareStatement("SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() "
                + "AND TABLE_NAME='berkas_digital_perawatan_meta'");ResultSet rs=ps.executeQuery()) {
            if(rs.next()&&!"InnoDB".equalsIgnoreCase(rs.getString(1)))
                throw new SQLException("Metadata berkas digital perlu InnoDB agar penggantian dan metadata tetap atomik.");
        }
    }

    String proses(String surat) throws Exception {
        try(Connection c=connections.open()) {
            siap(c);
            String lock="slr.berkas."+hash(surat);
            try(PreparedStatement ps=c.prepareStatement("SELECT GET_LOCK(?,0)")) {
                ps.setString(1,lock);
                try(ResultSet rs=ps.executeQuery()) {
                    if(!rs.next()||rs.getInt(1)!=1) throw new Exception("Pembaruan berkas surat ini sedang diproses petugas lain.");
                }
            }
            Job job=null; List<File> files=new ArrayList<>();
            try {
                job=job(c,surat);
                if(job==null) return "Berkas digital sudah sesuai / tidak ada penggantian tertunda.";
                Job active=job;
                pastikanTanggal(c,active,false);
                status(c,active.id,"PROSES","Menyiapkan pengganti berkas Lepas Rawat.");
                List<Ref> old=berkasLama(c,active);
                if(old.isEmpty()) {
                    c.setAutoCommit(false);pastikanTanggal(c,active,true);
                    selesai(c,active.id,active.surat,"Belum ada berkas Lepas Rawat yang sudah diunggah.");
                    c.commit();c.setAutoCommit(true);
                    return "Belum ada berkas Lepas Rawat yang perlu diganti.";
                }
                files=gateway.render(active.surat,active.id);
                if(files==null||files.isEmpty()) throw new Exception("Cetakan Lepas Rawat baru belum berhasil dibuat.");
                List<String> baru=new ArrayList<>();
                for(File f:files) {
                    String path=gateway.upload(f);
                    if(path==null||!path.matches("pages/upload/[A-Za-z0-9_.-]+\\.jpg")||baru.contains(path))
                        throw new Exception("Lokasi file pengganti tidak valid/berulang.");
                    baru.add(path);
                }
                ganti(c,active,old,baru);
                return "Berkas Lepas Rawat lama sudah diganti otomatis dengan tanggal kontrol terbaru.";
            } catch(Exception e) {
                if(!c.getAutoCommit()) { c.rollback(); c.setAutoCommit(true); }
                if(job!=null) status(c,job.id,"GAGAL",e.getMessage()==null?"Pembaruan berkas belum selesai.":e.getMessage());
                throw e;
            } finally {
                if(files!=null) gateway.bersihkan(files);
                try(PreparedStatement ps=c.prepareStatement("SELECT RELEASE_LOCK(?)")) { ps.setString(1,lock); ps.execute(); }
            }
        }
    }
    private Job job(Connection c,String surat) throws Exception {
        try(PreparedStatement ps=c.prepareStatement("SELECT j.*,s.nomr,s.no_rawat FROM rsaj_lepas_rawat_berkas_job j "
                + "INNER JOIN bridging_sep s ON s.no_sep=j.no_sep WHERE j.no_surat=? "
                + "AND j.status IN ('MENUNGGU','PROSES','GAGAL') ORDER BY j.id DESC LIMIT 1")) {
            ps.setString(1,surat);
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next()) return null;
                Job j=new Job(); j.id=rs.getLong("id"); j.surat=surat; j.sep=rs.getString("no_sep");
                j.rm=rs.getString("nomr"); j.rawat=rs.getString("no_rawat"); j.petugas=rs.getString("petugas");
                Date d=rs.getDate("tgl_sesudah"); if(d==null) throw new Exception("Tanggal kontrol baru belum lengkap.");
                j.tanggal=d.toLocalDate(); return j;
            }
        }
    }
    private void pastikanTanggal(Connection c,Job j,boolean lock) throws Exception {
        try(PreparedStatement ps=c.prepareStatement("SELECT no_sep,tgl_rencana FROM bridging_surat_kontrol_bpjs "
                + "WHERE no_surat=?"+(lock?" FOR UPDATE":""))) {
            ps.setString(1,j.surat);
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next()||!j.sep.equals(rs.getString(1))||rs.getDate(2)==null||!j.tanggal.equals(rs.getDate(2).toLocalDate()))
                    throw new Exception("Tanggal/SEP surat telah berubah. Muat ulang dan perbarui berkas kembali.");
            }
        }
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM rsaj_lepas_rawat_kirim WHERE no_surat=? "
                + "AND status IN ('DIPROSES','PERLU_CEK') LIMIT 1")) {
            ps.setString(1,j.surat);
            try(ResultSet rs=ps.executeQuery()) { if(rs.next()) throw new Exception("Pastikan status tanggal BPJS sebelum mengganti berkas."); }
        }
        if(lock) try(PreparedStatement ps=c.prepareStatement("SELECT id FROM rsaj_lepas_rawat_berkas_job WHERE no_surat=? "
                + "AND status IN ('MENUNGGU','PROSES','GAGAL') ORDER BY id DESC LIMIT 1 FOR UPDATE")) {
            ps.setString(1,j.surat);try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next()||rs.getLong(1)!=j.id) throw new Exception("Ada pekerjaan berkas lebih baru. Muat ulang daftar surat.");
            }
        }
    }
    private static final String CANDIDATES="SELECT b.no_rawat,b.kode,b.lokasi_file,r.tgl_registrasi FROM berkas_digital_perawatan b "
            + "INNER JOIN reg_periksa r ON r.no_rawat=b.no_rawat INNER JOIN master_berkas_digital m ON m.kode=b.kode "
            + "WHERE r.no_rkm_medis=? AND m.nama LIKE '%Lepas Rawat%' ORDER BY r.tgl_registrasi DESC,b.lokasi_file";

    List<Ref> kandidat(String surat) throws Exception {
        try(Connection c=connections.open()) {
            siap(c); String rm;
            try(PreparedStatement ps=c.prepareStatement("SELECT s.nomr FROM bridging_sep s INNER JOIN bridging_surat_kontrol_bpjs k "
                    + "ON k.no_sep=s.no_sep WHERE k.no_surat=? AND s.jnspelayanan='1'")) {
                ps.setString(1,surat);try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new Exception("Surat tidak ditemukan."); rm=rs.getString(1); }
            }
            List<Ref> refs=new ArrayList<>();
            try(PreparedStatement ps=c.prepareStatement(CANDIDATES)) {
                ps.setString(1,rm);try(ResultSet rs=ps.executeQuery()) { while(rs.next()) refs.add(new Ref(rs.getString(1),rs.getString(2),rs.getString(3))); }
            }
            return refs;
        }
    }
    void tautkan(String surat,List<Ref> pilihan,String petugas) throws Exception {
        if(pilihan==null||pilihan.isEmpty()) throw new Exception("Pilih berkas lama yang sesuai dengan surat ini.");
        Set<String> boleh=new LinkedHashSet<>();for(Ref r:kandidat(surat)) boleh.add(r.key());
        try(Connection c=connections.open()) {
            c.setAutoCommit(false);
            try {
                for(Ref ref:pilihan) {
                    if(!boleh.contains(ref.key())) throw new Exception("Berkas bukan milik pasien/jenis Lepas Rawat yang dipilih.");
                    if(tertautLain(c,surat,ref)) throw new Exception("Berkas sudah terkait dengan nomor SKDP lain.");
                    catat(c,surat,ref,null,"PILIH_PETUGAS",petugas,null,true);
                }
                c.commit();
            } catch(Exception e) { c.rollback(); throw e; }
        }
    }
    private List<Ref> berkasLama(Connection c,Job j) throws Exception {
        Map<String,Ref> hasil=new LinkedHashMap<>();
        try(PreparedStatement ps=c.prepareStatement("SELECT b.no_rawat,b.kode,b.lokasi_file FROM rsaj_lepas_rawat_berkas x "
                + "INNER JOIN berkas_digital_perawatan b ON b.no_rawat=x.no_rawat AND b.kode=x.kode AND b.lokasi_file=x.lokasi_file "
                + "INNER JOIN reg_periksa r ON r.no_rawat=b.no_rawat WHERE x.no_surat=? AND x.aktif=1 AND r.no_rkm_medis=?")) {
            ps.setString(1,j.surat);ps.setString(2,j.rm);
            try(ResultSet rs=ps.executeQuery()) { while(rs.next()) {
                Ref r=new Ref(rs.getString(1),rs.getString(2),rs.getString(3));
                if(tertautLain(c,j.surat,r)) throw new Exception("Satu berkas terkait beberapa SKDP. Verifikasi kaitannya sebelum mengganti berkas.");
                hasil.put(r.key(),r);
            } }
        }
        Set<LocalDate> dates=new LinkedHashSet<>();
        try(PreparedStatement ps=c.prepareStatement("SELECT tgl_sebelum FROM rsaj_lepas_rawat_berkas_job WHERE no_surat=? AND id<=?")) {
            ps.setString(1,j.surat);ps.setLong(2,j.id);try(ResultSet rs=ps.executeQuery()) { while(rs.next()) if(rs.getDate(1)!=null) dates.add(rs.getDate(1).toLocalDate()); }
        }
        boolean ambigu=false;
        try(PreparedStatement ps=c.prepareStatement(CANDIDATES)) {
            ps.setString(1,j.rm);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    Ref ref=new Ref(rs.getString(1),rs.getString(2),rs.getString(3));
                    Date day=rs.getDate(4);boolean ranap=j.rawat.equals(ref.rawat);
                    if(!ranap&&(day==null||!dates.contains(day.toLocalDate()))) continue;
                    if(hasil.containsKey(ref.key())||tertautLain(c,j.surat,ref)) continue;
                    boolean nama=ref.path.matches("pages/upload/[A-Za-z0-9_.-]*_"+java.util.regex.Pattern.quote(ref.rawat.replace("/",""))+"_LepasRawat(?:_page_[0-9]+)?\\.jpg");
                    boolean tunggal;
                    try(PreparedStatement cek=c.prepareStatement("SELECT COUNT(*) FROM bridging_surat_kontrol_bpjs k "
                            + "INNER JOIN bridging_sep s ON s.no_sep=k.no_sep WHERE s.nomr=? AND k.no_surat<>? AND "
                            + (ranap?"k.no_sep=?":"k.tgl_rencana=?"))) {
                        cek.setString(1,j.rm);cek.setString(2,j.surat);if(ranap) cek.setString(3,j.sep);else cek.setDate(3,day);
                        try(ResultSet n=cek.executeQuery()) { n.next();tunggal=n.getInt(1)==0; }
                    }
                    if(nama&&tunggal) hasil.put(ref.key(),ref);else ambigu=true;
                }
            }
        }
        if(hasil.isEmpty()&&ambigu) throw new Exception("Berkas lama belum dapat dipastikan nomor SKDP-nya. Gunakan Pilih Berkas Lama, lalu Perbarui Berkas.");
        return new ArrayList<>(hasil.values());
    }
    private static boolean tertautLain(Connection c,String surat,Ref r) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM rsaj_lepas_rawat_berkas WHERE no_rawat=? AND kode=? "
                + "AND lokasi_file=? AND no_surat<>? AND aktif=1 LIMIT 1")) {
            ps.setString(1,r.rawat);ps.setString(2,r.kode);ps.setString(3,r.path);ps.setString(4,surat);
            try(ResultSet rs=ps.executeQuery()) { return rs.next(); }
        }
    }
    private void ganti(Connection c,Job j,List<Ref> old,List<String> files) throws Exception {
        c.setAutoCommit(false);
        try {
            pastikanTanggal(c,j,true);
            for(Ref r:old) {
                try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM berkas_digital_perawatan WHERE no_rawat=? AND kode=? AND lokasi_file=? FOR UPDATE")) {
                    ps.setString(1,r.rawat);ps.setString(2,r.kode);ps.setString(3,r.path);
                    try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new Exception("Daftar berkas lama berubah saat upload. Muat ulang sebelum mencoba lagi."); }
                }
            }
            Set<String> tujuan=new LinkedHashSet<>();
            for(Ref r:old) if(tujuan.add(r.rawat+"|"+r.kode)) for(String path:files) {
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO berkas_digital_perawatan (no_rawat,kode,lokasi_file) VALUES (?,?,?)")) {
                    ps.setString(1,r.rawat);ps.setString(2,r.kode);ps.setString(3,path);ps.executeUpdate();
                }
                Ref baru=new Ref(r.rawat,r.kode,path);catat(c,j.surat,baru,j.tanggal,"GANTI_OTOMATIS",j.petugas,j.id,true);
                metadata(c,baru,j.petugas,false);
            }
            for(Ref r:old) {
                catat(c,j.surat,r,null,"VERSI_LAMA",j.petugas,j.id,false);
                try(PreparedStatement ps=c.prepareStatement("UPDATE rsaj_lepas_rawat_berkas SET aktif=0,diganti_oleh=?,diganti_pada=NOW() "
                        + "WHERE no_surat=? AND no_rawat=? AND kode=? AND lokasi_file=?")) {
                    ps.setLong(1,j.id);ps.setString(2,j.surat);ps.setString(3,r.rawat);ps.setString(4,r.kode);ps.setString(5,r.path);ps.executeUpdate();
                }
                metadata(c,r,j.petugas,true);
                try(PreparedStatement ps=c.prepareStatement("DELETE FROM berkas_digital_perawatan WHERE no_rawat=? AND kode=? AND lokasi_file=?")) {
                    ps.setString(1,r.rawat);ps.setString(2,r.kode);ps.setString(3,r.path);
                    if(ps.executeUpdate()!=1) throw new Exception("Berkas lama berubah; penukaran dibatalkan.");
                }
            }
            selesai(c,j.id,j.surat,"Berkas tanggal "+j.tanggal+" terunggah; referensi lama diganti.");
            c.commit();c.setAutoCommit(true);
        } catch(Exception e) { c.rollback();c.setAutoCommit(true);throw e; }
    }
    private static void catat(Connection c,String surat,Ref r,LocalDate tanggal,String sumber,String petugas,Long job,boolean aktif) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO rsaj_lepas_rawat_berkas "
                + "(no_surat,no_rawat,kode,lokasi_file,tgl_kontrol,aktif,sumber,petugas,job_id,dibuat) VALUES (?,?,?,?,?,?,?,?,?,NOW()) "
                + "ON DUPLICATE KEY UPDATE aktif=VALUES(aktif)")) {
            ps.setString(1,surat);ps.setString(2,r.rawat);ps.setString(3,r.kode);ps.setString(4,r.path);
            ps.setDate(5,tanggal==null?null:Date.valueOf(tanggal));ps.setInt(6,aktif?1:0);ps.setString(7,sumber);ps.setString(8,petugas);
            if(job==null) ps.setNull(9,java.sql.Types.BIGINT);else ps.setLong(9,job);ps.executeUpdate();
        }
    }
    static void catatUploadManual(Connection c,String surat,String rawat,String kode,String path,String petugas) {
        try {
            if(!tabel(c,"rsaj_lepas_rawat_berkas")) return;
            catat(c,surat,new Ref(rawat,kode,path),null,"UPLOAD_MANUAL",petugas,null,true);
        } catch(SQLException e) { System.out.println("Pemetaan Lepas Rawat belum tersimpan: "+e.getMessage()); }
    }
    private static boolean tabel(Connection c,String name) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
            ps.setString(1,name);try(ResultSet rs=ps.executeQuery()) { return rs.next(); }
        }
    }
    private static void metadata(Connection c,Ref r,String user,boolean hapus) throws SQLException {
        if(!tabel(c,"berkas_digital_perawatan_meta")) return;
        if(hapus) {
            try(PreparedStatement ps=c.prepareStatement("UPDATE berkas_digital_perawatan_meta SET deleted_at=NOW() "
                    + "WHERE no_rawat=? AND kode=? AND lokasi_file=? AND deleted_at IS NULL")) {
                ps.setString(1,r.rawat);ps.setString(2,r.kode);ps.setString(3,r.path);ps.executeUpdate();
            }
        } else {
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO berkas_digital_perawatan_meta "
                    + "(no_rawat,kode,lokasi_file,nama_file_asli,uploaded_at,uploaded_by) VALUES (?,?,?,?,NOW(),?)")) {
                ps.setString(1,r.rawat);ps.setString(2,r.kode);ps.setString(3,r.path);
                ps.setString(4,r.path.substring(r.path.lastIndexOf('/')+1));ps.setString(5,user);ps.executeUpdate();
            }
        }
    }
    private static void status(Connection c,long id,String state,String msg) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("UPDATE rsaj_lepas_rawat_berkas_job SET status=?,pesan=?,diperbarui=NOW() "
                + "WHERE id=? AND status NOT IN ('SELESAI','LEWATI')")) {
            ps.setString(1,state);ps.setString(2,msg);ps.setLong(3,id);ps.executeUpdate();
        }
    }
    private static void selesai(Connection c,long id,String surat,String message) throws SQLException {
        status(c,id,"SELESAI",message);
        try(PreparedStatement ps=c.prepareStatement("UPDATE rsaj_lepas_rawat_berkas_job SET status='LEWATI',pesan='Sudah diganti oleh versi tanggal lebih baru.',diperbarui=NOW() "
                + "WHERE no_surat=? AND id<? AND status IN ('MENUNGGU','PROSES','GAGAL')")) {
            ps.setString(1,surat);ps.setLong(2,id);ps.executeUpdate();
        }
    }
    static String hash(String text) throws Exception {
        byte[] bytes=java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"));
        StringBuilder result=new StringBuilder();for(byte b:bytes) result.append(String.format("%02x",b&255));return result.substring(0,40);
    }
}
