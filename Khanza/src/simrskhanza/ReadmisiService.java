package simrskhanza;

import fungsi.koneksiDB;
import java.sql.*;
import java.time.*;
import java.util.*;

/** Kejadian readmisi terpisah dari nomor rawat hasil penggabungan. */
final class ReadmisiService {
    interface Connections { Connection open() throws SQLException; }
    private final Connections connections;
    ReadmisiService(){this(() -> koneksiDB.condbTransaksi());}
    ReadmisiService(Connections connections){this.connections=connections;}
    static final class Data {
        long id; int versi;
        String referensi="",asal="",masuk="",rm="",nama="",kamar="",bangsal="",ruangan="",diagnosa="";
        String alasan="",keterangan="",sumber="",alasanKoreksi="";
        LocalDateTime pulang,datang;
        Data copy(){
            Data d=new Data();d.id=id;d.versi=versi;d.referensi=referensi;d.asal=asal;d.masuk=masuk;d.rm=rm;d.nama=nama;
            d.kamar=kamar;d.bangsal=bangsal;d.ruangan=ruangan;d.diagnosa=diagnosa;d.alasan=alasan;d.keterangan=keterangan;
            d.sumber=sumber;d.alasanKoreksi=alasanKoreksi;d.pulang=pulang;d.datang=datang;return d;
        }
        boolean klinisSama(Data d){
            return d!=null && Objects.equals(rm,d.rm) && Objects.equals(asal,d.asal) && Objects.equals(masuk,d.masuk)
                && Objects.equals(pulang,d.pulang) && Objects.equals(datang,d.datang) && Objects.equals(kamar,d.kamar)
                && Objects.equals(bangsal,d.bangsal) && Objects.equals(ruangan,d.ruangan) && Objects.equals(diagnosa,d.diagnosa);
        }
        @Override public String toString(){return pulang+" -> "+datang+" / "+ruangan;}
    }
    private void siap(Connection c)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT k.id,a.kejadian_id FROM rsaj_readmisi_kejadian k "
                +"LEFT JOIN rsaj_readmisi_riwayat a ON 1=0 LIMIT 0");ResultSet r=p.executeQuery()){}
        catch(SQLException e){
            if(e.getErrorCode()==1146 || "42S02".equals(e.getSQLState()))
                throw new SQLException("Penyimpanan Readmisi V2 belum tersedia. Jalankan rsaj_readmisi_kejadian_v2.sql terlebih dahulu.",e);
            throw e;
        }
    }
    /** Konteks pasien boleh masih aktif; tidak mensyaratkan kunjungan berikutnya atau nomor berbeda. */
    Data konteks(String referensi)throws Exception{
        try(Connection c=connections.open()){siap(c);return konteks(c,referensi);}
    }
    private Data konteks(Connection c,String referensi)throws Exception{
        try(PreparedStatement p=c.prepareStatement("SELECT r.no_rawat,r.no_rkm_medis,p.nm_pasien FROM reg_periksa r "
                +"JOIN pasien p ON p.no_rkm_medis=r.no_rkm_medis WHERE r.no_rawat=? AND r.status_lanjut='Ranap' AND r.stts<>'Batal'")){
            p.setString(1,referensi);
            try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new Exception("Nomor rawat referensi tidak ditemukan atau bukan kunjungan Ranap yang valid.");
                Data d=new Data();d.referensi=r.getString(1);d.rm=r.getString(2);d.nama=r.getString(3);return d;
            }
        }
    }
    Data baca(long id)throws Exception{
        try(Connection c=connections.open()){siap(c);Data d=baca(c,id,false);if(d==null)throw new Exception("Catatan readmisi tidak ditemukan.");return d;}
    }
    private Data baca(Connection c,long id,boolean lock)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT * FROM rsaj_readmisi_kejadian WHERE id=?"+(lock?" FOR UPDATE":""))){
            p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }
    private static final class Segmen {
        String rawat,kamar,bangsal,ruang,diagnosa,status;
        LocalDateTime masuk,pulang,registrasi;
        boolean benarPulang(){return pulang!=null && masuk!=null && !pulang.isBefore(masuk) && !status.isEmpty()
            && !status.equals("-") && !status.equalsIgnoreCase("Pindah Kamar") && !status.equalsIgnoreCase("Status Belum Lengkap");}
    }
    List<Data> riwayat(String referensi,String rm)throws Exception{
        try(Connection c=connections.open()){
            siap(c);Data pasien=konteks(c,referensi);
            if(!pasien.rm.equals(rm))throw new Exception("Nomor rawat referensi tidak sesuai RM pasien.");
            return riwayat(c,pasien);
        }
    }
    private List<Data> riwayat(Connection c,Data pasien)throws Exception{
        List<Segmen> segments=new ArrayList<>();
        // Tidak memakai nomor rawat sebagai batas episode: segmen kamar yang sudah digabung tetap dibaca.
        String sql="SELECT k.no_rawat,k.kd_kamar,k.tgl_masuk,k.jam_masuk,k.tgl_keluar,k.jam_keluar,k.stts_pulang,k.diagnosa_akhir,"
            +"b.kd_bangsal,b.nm_bangsal,r.tgl_registrasi,r.jam_reg FROM kamar_inap k "
            +"JOIN reg_periksa r ON r.no_rawat=k.no_rawat LEFT JOIN kamar km ON km.kd_kamar=k.kd_kamar "
            +"LEFT JOIN bangsal b ON b.kd_bangsal=km.kd_bangsal WHERE r.no_rkm_medis=? AND r.status_lanjut='Ranap' AND r.stts<>'Batal' "
            +"ORDER BY k.tgl_masuk DESC,k.jam_masuk DESC,k.no_rawat,k.kd_kamar LIMIT 2000";
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,pasien.rm);try(ResultSet r=p.executeQuery()){while(r.next()){
                Segmen s=new Segmen();s.rawat=teks(r,"no_rawat");s.kamar=teks(r,"kd_kamar");s.bangsal=teks(r,"kd_bangsal");
                s.ruang=teks(r,"nm_bangsal");s.diagnosa=teks(r,"diagnosa_akhir");s.status=teks(r,"stts_pulang");
                s.masuk=waktu(r,"tgl_masuk","jam_masuk");s.pulang=waktu(r,"tgl_keluar","jam_keluar");
                s.registrasi=waktu(r,"tgl_registrasi","jam_reg");if(s.masuk!=null)segments.add(s);
            }}
        }
        segments.sort(Comparator.comparing((Segmen s)->s.masuk).thenComparing(s->s.rawat).thenComparing(s->s.kamar));
        LinkedHashMap<String,Data> found=new LinkedHashMap<>();
        for(Segmen out:segments){
            if(!out.benarPulang())continue;
            Segmen next=null;boolean ambiguous=false;
            for(Segmen in:segments){
                if(in==out || in.masuk.isBefore(out.pulang))continue;
                if(next==null){next=in;continue;}
                if(in.masuk.isAfter(next.masuk))break;
                // Dua episode berbeda dengan waktu masuk sama perlu peninjauan manual.
                if(!in.rawat.equals(next.rawat))ambiguous=true;
            }
            if(next==null || ambiguous)continue;
            // Jangan usulkan pulang bila ada segmen lain yang masih melintasi waktu pulang tersebut.
            boolean overlap=false;
            for(Segmen other:segments)if(other!=out && other.masuk.isBefore(out.pulang)
                    && (other.pulang==null || other.pulang.isAfter(out.pulang))){overlap=true;break;}
            if(overlap)continue;
            Data d=pasien.copy();d.asal=out.rawat;d.masuk=next.rawat;d.pulang=out.pulang;d.datang=next.masuk;
            // Registrasi tetap digunakan bila episode terpisah dan waktunya masih mewakili masuk kembali.
            if(!out.rawat.equals(next.rawat) && next.registrasi!=null && !next.registrasi.isBefore(out.pulang)
                    && !next.registrasi.isAfter(next.masuk))d.datang=next.registrasi;
            try{validasiJarak(d.pulang,d.datang);}catch(Exception e){continue;}
            d.kamar=out.kamar;d.bangsal=out.bangsal;d.ruangan=out.ruang;d.diagnosa=out.diagnosa;d.sumber="RIWAYAT";
            found.putIfAbsent(d.pulang+"|"+d.datang,d);
        }
        List<Data> result=new ArrayList<>(found.values());result.sort(Comparator.comparing((Data d)->d.datang).reversed());return result;
    }
    static void validasiJarak(LocalDateTime pulang,LocalDateTime masuk)throws Exception{
        if(pulang==null || masuk==null)throw new Exception("Tanggal dan jam pulang sebelumnya serta masuk kembali wajib diisi.");
        Duration gap=Duration.between(pulang,masuk);
        if(gap.isNegative() || gap.compareTo(Duration.ofDays(5))>0)
            throw new Exception("Masuk kembali harus sesudah/sama dengan pulang, dengan selisih maksimal 120 jam (5 hari).");
    }
    static void validasi(Data d,String petugas,boolean boleh)throws Exception{
        if(!boleh)throw new Exception("Anda tidak memiliki hak akses Kamar Inap.");
        if(d==null)throw new Exception("Muat data pasien terlebih dahulu.");
        if(petugas==null || petugas.trim().isEmpty() || petugas.length()>50)throw new Exception("Identitas petugas tidak valid.");
        if(d.rm.isEmpty() || d.referensi.isEmpty() || d.rm.length()>15 || d.referensi.length()>17)throw new Exception("Identitas pasien/referensi tidak valid.");
        validasiJarak(d.pulang,d.datang);
        if(d.alasan.trim().isEmpty())throw new Exception("Alasan readmisi wajib diisi.");
        if(d.ruangan.trim().isEmpty())throw new Exception("Ruangan kepulangan asal wajib diisi atau dipilih dari riwayat.");
        if(!Arrays.asList("RIWAYAT","MANUAL","MIGRASI").contains(d.sumber))throw new Exception("Pilih riwayat atau aktifkan koreksi manual terlebih dahulu.");
        if(d.sumber.equals("MANUAL") && d.alasanKoreksi.trim().isEmpty())throw new Exception("Alasan koreksi/input manual wajib diisi.");
        for(String value:Arrays.asList(d.alasan,d.keterangan,d.diagnosa,d.alasanKoreksi))
            if(value.length()>4000)throw new Exception("Maksimal 4.000 karakter per catatan.");
        if(d.ruangan.length()>100 || d.nama.length()>100 || d.asal.length()>17 || d.masuk.length()>17)
            throw new Exception("Panjang identitas/ruangan melebihi kapasitas penyimpanan.");
    }
    private static final String COLUMNS="no_rawat_referensi,no_rawat_pulang,no_rawat_masuk,no_rkm_medis,nama_pasien,"
            +"tgl_jam_pulang,tgl_jam_masuk,kd_kamar,kd_bangsal,ruangan,diagnosa,alasan_readmisi,keterangan,sumber_data,alasan_koreksi";
    Data simpan(Data input,String petugas,boolean boleh)throws Exception{
        Data d=input==null?null:input.copy();validasi(d,petugas,boleh);
        d.alasan=d.alasan.trim();d.keterangan=d.keterangan.trim();d.alasanKoreksi=d.alasanKoreksi.trim();d.ruangan=d.ruangan.trim();
        try(Connection c=connections.open()){
            siap(c);c.setAutoCommit(false);
            try{
                Data old=d.id==0?null:baca(c,d.id,true);
                if(d.id!=0 && (old==null || old.versi!=d.versi))throw new Exception("Catatan sudah diubah petugas lain. Muat ulang sebelum menyimpan.");
                if(old!=null && !old.rm.equals(d.rm))throw new Exception("RM catatan tersimpan tidak boleh diganti.");
                // Referensi yang hilang setelah penggabungan boleh dipertahankan pada catatan lama.
                try{
                    Data pasien=konteks(c,d.referensi);if(!pasien.rm.equals(d.rm))throw new Exception("Nomor rawat referensi tidak sesuai RM pasien.");
                    d.nama=pasien.nama;
                }catch(Exception e){
                    if(old==null || !old.referensi.equals(d.referensi) || !old.rm.equals(d.rm))throw e;
                    d.nama=old.nama;
                }
                boolean changed=old==null || !d.klinisSama(old);
                if(old!=null && changed && d.alasanKoreksi.isEmpty())throw new Exception("Perubahan tanggal/diagnosa/ruangan memerlukan alasan koreksi.");
                if(d.sumber.equals("MIGRASI") && (old==null || changed))throw new Exception("Untuk mengubah data migrasi, pilih riwayat atau gunakan koreksi manual.");
                if(d.sumber.equals("RIWAYAT") && changed){
                    boolean match=false;for(Data h:riwayat(c,d))if(d.klinisSama(h)){match=true;break;}
                    if(!match)throw new Exception("Riwayat sumber sudah berubah/tidak tersedia. Pilih ulang riwayat atau gunakan koreksi manual dengan alasan.");
                }
                String sql=old==null?"INSERT INTO rsaj_readmisi_kejadian ("+COLUMNS+",petugas_buat,petugas_ubah) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                    :"UPDATE rsaj_readmisi_kejadian SET no_rawat_referensi=?,no_rawat_pulang=?,no_rawat_masuk=?,no_rkm_medis=?,nama_pasien=?,"
                    +"tgl_jam_pulang=?,tgl_jam_masuk=?,kd_kamar=?,kd_bangsal=?,ruangan=?,diagnosa=?,alasan_readmisi=?,keterangan=?,sumber_data=?,alasan_koreksi=?,"
                    +"petugas_ubah=?,versi=versi+1 WHERE id=? AND versi=?";
                try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
                    int i=bind(p,d);if(old==null)p.setString(i++,petugas);p.setString(i++,petugas);
                    if(old!=null){p.setLong(i++,d.id);p.setInt(i++,d.versi);}
                    if(p.executeUpdate()!=1)throw new Exception("Catatan sudah diubah petugas lain. Muat ulang sebelum menyimpan.");
                    if(old==null)try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("ID kejadian tidak tersedia.");d.id=keys.getLong(1);}
                }
                d.versi=old==null?1:old.versi+1;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO rsaj_readmisi_riwayat (kejadian_id,versi,aksi,"+COLUMNS+",petugas) "
                        +"SELECT id,versi,?,"+COLUMNS+",? FROM rsaj_readmisi_kejadian WHERE id=?")){
                    p.setString(1,old==null?"BUAT":"UBAH");p.setString(2,petugas);p.setLong(3,d.id);
                    if(p.executeUpdate()!=1)throw new SQLException("Audit readmisi gagal dicatat.");
                }
                c.commit();return d;
            }catch(Exception e){
                try{c.rollback();}catch(SQLException ex){e.addSuppressed(ex);}
                if(e instanceof SQLException && ((SQLException)e).getErrorCode()==1062)
                    throw new Exception("Kejadian untuk RM, waktu pulang dan waktu masuk tersebut sudah tercatat. Buka catatan dari rekap.",e);
                throw e;
            }
        }
    }
    private static int bind(PreparedStatement p,Data d)throws SQLException{
        int i=1;p.setString(i++,d.referensi);nullable(p,i++,d.asal);nullable(p,i++,d.masuk);p.setString(i++,d.rm);p.setString(i++,d.nama);
        p.setTimestamp(i++,Timestamp.valueOf(d.pulang));p.setTimestamp(i++,Timestamp.valueOf(d.datang));p.setString(i++,d.kamar);p.setString(i++,d.bangsal);
        p.setString(i++,d.ruangan);p.setString(i++,d.diagnosa);p.setString(i++,d.alasan);p.setString(i++,d.keterangan);p.setString(i++,d.sumber);p.setString(i++,d.alasanKoreksi);return i;
    }
    private static void nullable(PreparedStatement p,int i,String s)throws SQLException{if(s==null || s.isEmpty())p.setNull(i,Types.VARCHAR);else p.setString(i,s);}
    List<Data> rekap(LocalDate awal,LocalDate akhir,String ruang,String cari)throws Exception{
        if(awal==null || akhir==null || !akhir.isAfter(awal))throw new Exception("Periode rekap tidak valid.");
        List<Data> result=new ArrayList<>();
        try(Connection c=connections.open()){
            siap(c);try(PreparedStatement p=c.prepareStatement("SELECT * FROM rsaj_readmisi_kejadian WHERE tgl_jam_masuk>=? AND tgl_jam_masuk<? "
                    +"AND (?='' OR ruangan=?) AND (?='' OR no_rkm_medis LIKE ? ESCAPE '!' OR nama_pasien LIKE ? ESCAPE '!') ORDER BY tgl_jam_masuk,id")){
                p.setTimestamp(1,Timestamp.valueOf(awal.atStartOfDay()));p.setTimestamp(2,Timestamp.valueOf(akhir.atStartOfDay()));
                p.setString(3,ruang);p.setString(4,ruang);p.setString(5,cari);
                String pattern="%"+cari.replace("!","!!").replace("%","!%").replace("_","!_")+"%";p.setString(6,pattern);p.setString(7,pattern);
                try(ResultSet r=p.executeQuery()){while(r.next())result.add(map(r));}
            }
        }return result;
    }
    List<String> ruangan()throws Exception{
        List<String> result=new ArrayList<>();try(Connection c=connections.open()){
            siap(c);try(PreparedStatement p=c.prepareStatement("SELECT DISTINCT ruangan FROM rsaj_readmisi_kejadian ORDER BY ruangan");ResultSet r=p.executeQuery()){while(r.next())result.add(r.getString(1));}
        }return result;
    }
    List<String[]> audit(long id)throws Exception{
        List<String[]> result=new ArrayList<>();try(Connection c=connections.open()){
            siap(c);try(PreparedStatement p=c.prepareStatement("SELECT versi,aksi,dicatat_pada,petugas,tgl_jam_pulang,tgl_jam_masuk,ruangan,diagnosa,alasan_readmisi,keterangan,sumber_data,alasan_koreksi "
                    +"FROM rsaj_readmisi_riwayat WHERE kejadian_id=? ORDER BY versi DESC")){
                p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next()){String[] row=new String[12];for(int i=0;i<12;i++)row[i]=r.getString(i+1);result.add(row);}}
            }
        }return result;
    }
    private static Data map(ResultSet r)throws SQLException{
        Data d=new Data();d.id=r.getLong("id");d.versi=r.getInt("versi");d.referensi=teks(r,"no_rawat_referensi");d.asal=teks(r,"no_rawat_pulang");d.masuk=teks(r,"no_rawat_masuk");
        d.rm=teks(r,"no_rkm_medis");d.nama=teks(r,"nama_pasien");d.kamar=teks(r,"kd_kamar");d.bangsal=teks(r,"kd_bangsal");d.ruangan=teks(r,"ruangan");
        d.diagnosa=teks(r,"diagnosa");d.alasan=teks(r,"alasan_readmisi");d.keterangan=teks(r,"keterangan");d.sumber=teks(r,"sumber_data");d.alasanKoreksi=teks(r,"alasan_koreksi");
        d.pulang=r.getTimestamp("tgl_jam_pulang").toLocalDateTime();d.datang=r.getTimestamp("tgl_jam_masuk").toLocalDateTime();return d;
    }
    private static String teks(ResultSet r,String field)throws SQLException{String s=r.getString(field);return s==null?"":s;}
    private static LocalDateTime waktu(ResultSet r,String date,String time)throws SQLException{
        String d=r.getString(date),t=r.getString(time);if(d==null || t==null || d.startsWith("0000-00-00") || d.isEmpty() || t.isEmpty())return null;
        try{return LocalDateTime.parse(d+"T"+t);}catch(java.time.format.DateTimeParseException e){return null;}
    }
}
