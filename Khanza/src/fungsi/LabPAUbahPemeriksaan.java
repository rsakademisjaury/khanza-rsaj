package fungsi;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Penggantian satu kode PA secara atomik; bukan penggantian master pemeriksaan. */
public final class LabPAUbahPemeriksaan {
    private LabPAUbahPemeriksaan() {}
    private static final String KEY="no_rawat=? and kd_jenis_prw=? and tgl_periksa=? and jam=?";
    private static final String[] MONEY={"biaya","tarif_tindakan_dokter","tarif_tindakan_petugas","bhp","kso","bagian_rs","tarif_perujuk","menejemen"};
    public static final class Pilihan {
        public final String kode,nama,jenis;
        public final BigDecimal biaya;
        public Pilihan(String kode,String nama,String jenis,BigDecimal biaya){this.kode=kode;this.nama=nama;this.jenis=jenis;this.biaya=biaya;}
    }
    public static final class Data {
        public final LabPAData.Kunci kunci;
        public String nama,jenis,noPA,noorder,pasien,status,payer;
        public BigDecimal biaya;
        private LinkedHashMap<String,Object> old;
        Data(LabPAData.Kunci k){kunci=k;}
    }
    private static String q(String s){return "`"+s.replace("`","``")+"`";}
    private static String text(Object v){return LabPAData.text(v);}
    private static BigDecimal money(Object v)throws SQLException {
        try {BigDecimal b=v==null?BigDecimal.ZERO:new BigDecimal(v.toString());if(b.signum()<0)throw new NumberFormatException();return b;}
        catch(NumberFormatException ex){throw new SQLException("Tarif pemeriksaan tidak valid.",ex);}
    }
    private static LinkedHashMap<String,Object> row(ResultSet r)throws SQLException{
        LinkedHashMap<String,Object> m=new LinkedHashMap<>();ResultSetMetaData meta=r.getMetaData();
        for(int i=1;i<=meta.getColumnCount();i++)m.put(meta.getColumnLabel(i).toLowerCase(Locale.ROOT),r.getObject(i));return m;
    }
    private static LinkedHashMap<String,Object> one(Connection c,String sql,Object... args)throws SQLException{
        try(PreparedStatement p=c.prepareStatement(sql)){bind(p,args);try(ResultSet r=p.executeQuery()){return r.next()?row(r):null;}}
    }
    private static void bind(PreparedStatement p,Object... args)throws SQLException{for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);}
    private static Object[] key(LabPAData.Kunci k){return new Object[]{k.noRawat,k.kode,k.tanggal,k.jam};}
    private static int execute(Connection c,String sql,Object... args)throws SQLException{
        try(PreparedStatement p=c.prepareStatement(sql)){bind(p,args);return p.executeUpdate();}
    }
    private static void require(boolean value,String message)throws SQLException{if(!value)throw new SQLException(message);}

    public static Data muat(Connection c,LabPAData.Kunci k)throws SQLException{return muat(c,k,false);}
    private static Data muat(Connection c,LabPAData.Kunci k,boolean lock)throws SQLException{
        require(k!=null,"Pilih pemeriksaan PA terlebih dahulu.");
        Data d=new Data(k);
        d.old=one(c,"select * from periksa_lab where "+KEY+" and kategori='PA'"+(lock?" for update":""),key(k));
        require(d.old!=null,"Pemeriksaan PA tidak ditemukan atau sudah diubah. Muat ulang daftar.");
        Map<String,Object> master=one(c,"select nm_perawatan from jns_perawatan_lab where kd_jenis_prw=?",k.kode);
        require(master!=null,"Master pemeriksaan lama tidak ditemukan.");
        d.nama=text(master.get("nm_perawatan"));d.jenis=LabPAData.jenis(c,k.kode,d.nama);
        require(LabPAData.validJenis(d.jenis),"Jenis H/S/IHC pada pemeriksaan lama belum dapat dikenali. Periksa mapping PA.");
        d.noPA=LabPAData.nomor(c,k);d.biaya=money(d.old.get("biaya"));d.status=text(d.old.get("status"));
        Map<String,Object> pasien=one(c,"select p.nm_pasien,r.kd_pj from reg_periksa r inner join pasien p on p.no_rkm_medis=r.no_rkm_medis where r.no_rawat=?",k.noRawat);
        d.pasien=pasien==null?k.noRawat:text(pasien.get("nm_pasien"));
        d.payer=pasien==null?"":text(pasien.get("kd_pj"));
        d.noorder="";
        Map<String,Object> hasil=one(c,"select noorder,jenis from rsaj_labpa_hasil where "+KEY+(lock?" for update":""),key(k));
        if(hasil!=null){
            require(text(hasil.get("jenis")).isEmpty()||d.jenis.equals(text(hasil.get("jenis"))),"Jenis hasil PA tidak sesuai mapping pemeriksaan lama.");
            d.noorder=text(hasil.get("noorder"));
        }
        if(!d.noPA.isEmpty())require(d.jenis.equals(LabPAData.jenisNomor(c,k)),"Jenis No. PA tidak sesuai pemeriksaan lama. Periksa data sebelum mengganti kode.");
        if(d.noorder.isEmpty()){
            String sql="select pl.noorder from permintaan_labpa pl inner join permintaan_pemeriksaan_labpa pp on pp.noorder=pl.noorder "
                    +"where pl.no_rawat=? and pl.tgl_hasil=? and pl.jam_hasil=? and pp.kd_jenis_prw=?"+(lock?" for update":"");
            try(PreparedStatement p=c.prepareStatement(sql)){
                bind(p,k.noRawat,k.tanggal,k.jam,k.kode);try(ResultSet r=p.executeQuery()){
                    if(r.next()){d.noorder=text(r.getString(1));require(!r.next(),"Lebih dari satu order cocok. Periksa hubungan permintaan PA sebelum mengganti kode.");}
                }
            }
        }
        if(!d.noorder.isEmpty()){
            Map<String,Object> order=one(c,"select noorder from permintaan_labpa where noorder=? and no_rawat=?"+(lock?" for update":""),d.noorder,k.noRawat);
            require(order!=null,"Order PA tidak terhubung ke kunjungan yang dipilih.");
            require(one(c,"select noorder from permintaan_pemeriksaan_labpa where noorder=? and kd_jenis_prw=?"+(lock?" for update":""),d.noorder,k.kode)!=null,"Kode pemeriksaan lama tidak ditemukan pada order PA terkait.");
        }
        return d;
    }

    public static List<Pilihan> pilihan(Connection c,Data d)throws SQLException {
        List<Pilihan> list=new ArrayList<>();
        boolean filter=filterPayer(c);
        String sql="select j.kd_jenis_prw,j.nm_perawatan,j.total_byr,j.kd_pj from jns_perawatan_lab j "
                +"where j.kategori='PA' and j.status='1' and j.kd_jenis_prw<>? order by j.nm_perawatan,j.kd_jenis_prw";
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,d.kunci.kode);try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    String kode=r.getString(1),nama=r.getString(2),jenis=LabPAData.jenis(c,kode,nama);
                    if(d.jenis.equals(jenis)&&(!filter||d.payer.equals(text(r.getString(4)))))list.add(new Pilihan(kode,nama,jenis,money(r.getObject(3))));
                }
            }
        }return list;
    }
    private static boolean filterPayer(Connection c)throws SQLException{
        Map<String,Object> setting=one(c,"select cara_bayar_lab from set_tarif limit 1");
        return setting==null||!"No".equalsIgnoreCase(text(setting.get("cara_bayar_lab")));
    }

    private static final class Table {
        final String name,engine;final LinkedHashMap<String,String> cols=new LinkedHashMap<>();
        Table(String n,String e){name=n;engine=e;}
        boolean has(String... fields){for(String f:fields)if(!cols.containsKey(f))return false;return true;}
    }
    private static Map<String,Table> tables(Connection c)throws SQLException{
        Map<String,Table> result=new LinkedHashMap<>();
        String sql="select c.TABLE_NAME,c.COLUMN_NAME,c.EXTRA,t.ENGINE from information_schema.COLUMNS c "
                +"inner join information_schema.TABLES t on t.TABLE_SCHEMA=c.TABLE_SCHEMA and t.TABLE_NAME=c.TABLE_NAME "
                +"where c.TABLE_SCHEMA=database() and t.TABLE_TYPE='BASE TABLE' "
                +"order by c.TABLE_NAME,c.ORDINAL_POSITION";
        try(PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next()){
                String name=r.getString(1);Table t=result.get(name);
                if(t==null){t=new Table(name,r.getString(4));result.put(name,t);}
                t.cols.put(r.getString(2).toLowerCase(Locale.ROOT),text(r.getString(3)));
            }
        }return result;
    }
    private static Set<String> references(Connection c)throws SQLException{
        Set<String> names=new HashSet<>(Arrays.asList("periksa_lab","permintaan_pemeriksaan_labpa","rsaj_labpa_hasil","rsaj_labpa_permintaan"));
        Map<String,Set<String>> children=new HashMap<>();
        String sql="select distinct TABLE_NAME,REFERENCED_TABLE_NAME from information_schema.KEY_COLUMN_USAGE "
                +"where CONSTRAINT_SCHEMA=database() and REFERENCED_TABLE_NAME is not null";
        try(PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next())children.computeIfAbsent(r.getString(2),key->new HashSet<>()).add(r.getString(1));
        }
        Deque<String> queue=new ArrayDeque<>(names);
        while(!queue.isEmpty())for(String child:children.getOrDefault(queue.removeFirst(),Collections.emptySet()))if(names.add(child))queue.addLast(child);
        return names;
    }
    private static void transactional(Table t)throws SQLException {
        require(t!=null,"Tabel pembaruan PA belum tersedia. Jalankan SQL patch Ubah Periksa PA.");
        require("InnoDB".equalsIgnoreCase(t.engine),"Tabel "+t.name+" harus InnoDB agar penggantian dapat dibatalkan utuh saat gagal.");
    }
    private static int lockedCount(Connection c,Table t,String where,Object... args)throws SQLException{
        int n=0;try(PreparedStatement p=c.prepareStatement("select kd_jenis_prw from "+q(t.name)+" where "+where+" for update")){
            bind(p,args);try(ResultSet r=p.executeQuery()){while(r.next())n++;}
        }return n;
    }
    private static void cloneRow(Connection c,Table t,Map<String,Object> original,String code,Map<String,Object> tariff)throws SQLException{
        List<String> cols=new ArrayList<>();List<Object> values=new ArrayList<>();
        for(Map.Entry<String,Object> e:original.entrySet()){
            String extra=t.cols.get(e.getKey());
            require(extra!=null,"Struktur tabel "+t.name+" berubah. Muat ulang aplikasi.");
            String upper=extra.toUpperCase(Locale.ROOT);
            if(extra.toLowerCase(Locale.ROOT).contains("auto_increment")||upper.contains("VIRTUAL GENERATED")||upper.contains("STORED GENERATED"))continue;
            cols.add(q(e.getKey()));
            Object value=e.getKey().equals("kd_jenis_prw")?code:e.getValue();
            if(tariff!=null){
                for(String field:MONEY)if(field.equals(e.getKey()))value=money(tariff.get(field.equals("biaya")?"total_byr":field));
            }
            values.add(value);
        }
        execute(c,"insert into "+q(t.name)+" ("+String.join(",",cols)+") values ("+String.join(",",Collections.nCopies(cols.size(),"?"))+")",values.toArray());
    }

    /** Semua perubahan, jurnal selisih, dan audit memakai transaksi yang sama. */
    public static Data ubah(Connection c,LabPAData.Kunci k,Pilihan target,String alasan,String user)throws SQLException {
        require(k!=null,"Pilih pemeriksaan PA terlebih dahulu.");
        require(target!=null,"Pilih pemeriksaan pengganti.");
        require(!text(alasan).isEmpty()&&text(alasan).length()<=500,"Isi alasan perubahan (maksimal 500 karakter).");
        require(!text(user).isEmpty(),"Identitas pengguna belum tersedia.");
        synchronized(c){
            require(c.getAutoCommit(),"Selesaikan transaksi aktif sebelum mengganti pemeriksaan PA.");
            c.setAutoCommit(false);
            try{
                Map<String,Table> meta=tables(c);
                for(String name:new String[]{"periksa_lab","rsaj_labpa_ubah_log","reg_periksa","billing","rsaj_labpa_hasil","rsaj_labpa_nomor"})transactional(meta.get(name));
                Map<String,Object> visit=one(c,"select stts from reg_periksa where no_rawat=? for update",k.noRawat);
                require(visit!=null&&!"Batal".equalsIgnoreCase(text(visit.get("stts"))),"Kunjungan tidak tersedia atau sudah dibatalkan.");
                require(one(c,"select no_rawat from billing where no_rawat=? limit 1 for update",k.noRawat)==null,"Billing sudah terverifikasi. Hubungi kasir/keuangan sebelum mengubah pemeriksaan.");
                Data d=muat(c,k,true);
                require(!k.kode.equals(target.kode),"Pilih kode pemeriksaan yang berbeda.");
                Map<String,Object> tariff=one(c,"select * from jns_perawatan_lab where kd_jenis_prw=? and kategori='PA' and status='1' for update",target.kode);
                require(tariff!=null,"Pemeriksaan pengganti sudah tidak aktif atau bukan PA.");
                String jenis=LabPAData.jenis(c,target.kode,text(tariff.get("nm_perawatan")));
                require(d.jenis.equals(jenis),"Penggantian hanya boleh dalam jenis PA yang sama: H ke H, S ke S, atau IHC ke IHC.");
                require(!filterPayer(c)||d.payer.equals(text(tariff.get("kd_pj"))),"Cara bayar tarif pengganti tidak sesuai pasien.");
                require(target.biaya!=null&&target.biaya.compareTo(money(tariff.get("total_byr")))==0,"Tarif pengganti berubah. Buka kembali pilihan pemeriksaan.");
                Object[] newkey={k.noRawat,target.kode,k.tanggal,k.jam};
                Map<Table,Integer> resultTables=new LinkedHashMap<>(),orderTables=new LinkedHashMap<>();
                Set<String> linked=references(c);
                for(Table t:meta.values()){
                    if(!t.name.toLowerCase(Locale.ROOT).contains("lab")&&!linked.contains(t.name))continue;
                    if(!t.has("kd_jenis_prw"))continue;
                    if(t.has("no_rawat","tgl_periksa","jam")){
                        int n=lockedCount(c,t,KEY,key(k));if(n==0)continue;
                        transactional(t);
                        require(!t.name.startsWith("bayar_"),"Jasa pemeriksaan sudah dibayarkan pada "+t.name+". Hubungi keuangan.");
                        require(!t.name.contains("satu_sehat")&&!t.name.contains("bridging"),"Pemeriksaan sudah memiliki data integrasi pada "+t.name+". Tinjau integrasi sebelum mengganti kode.");
                        require(!t.name.equals("detail_periksa_lab"),"Pemeriksaan memiliki parameter template PK. Tinjau data parameter sebelum mengganti kode PA.");
                        require(lockedCount(c,t,KEY,newkey)==0,"Kode pengganti sudah memiliki data pada "+t.name+" untuk hasil yang sama. Penggabungan otomatis tidak dilakukan.");
                        resultTables.put(t,n);
                    }else if(t.has("noorder")&&t.name.contains("labpa")&&!d.noorder.isEmpty()){
                        int n=lockedCount(c,t,"noorder=? and kd_jenis_prw=?",d.noorder,k.kode);if(n==0)continue;
                        transactional(t);
                        require(lockedCount(c,t,"noorder=? and kd_jenis_prw=?",d.noorder,target.kode)==0,"Kode pengganti sudah ada pada order PA terkait.");
                        orderTables.put(t,n);
                    }else if(t.has("no_rawat")&&!t.has("noorder")){
                        require(lockedCount(c,t,"no_rawat=? and kd_jenis_prw=?",k.noRawat,k.kode)==0,"Tabel terkait "+t.name+" tidak memiliki kunci tanggal/jam lengkap. Periksa strukturnya sebelum mengubah PA.");
                    }
                }
                if(!d.noorder.isEmpty()){
                    Map<String,Object> other=one(c,"select no_rawat from rsaj_labpa_hasil where noorder=? and kd_jenis_prw=? and not ("+KEY+") limit 1 for update",d.noorder,k.kode,k.noRawat,k.kode,k.tanggal,k.jam);
                    require(other==null,"Order yang sama terhubung ke hasil lain dengan kode lama. Pisahkan hubungan order sebelum mengganti kode.");
                }
                // Parent baru tersedia dahulu untuk child dengan FK RESTRICT sekalipun.
                cloneRow(c,meta.get("periksa_lab"),d.old,target.kode,tariff);
                Map<String,Object> oldOrder=null;
                if(!d.noorder.isEmpty()){
                    Table t=meta.get("permintaan_pemeriksaan_labpa");transactional(t);
                    oldOrder=one(c,"select * from permintaan_pemeriksaan_labpa where noorder=? and kd_jenis_prw=? for update",d.noorder,k.kode);
                    require(oldOrder!=null,"Detail order PA berubah. Muat ulang data.");
                    cloneRow(c,t,oldOrder,target.kode,null);
                }
                Map<String,Object> oldHasil=one(c,"select * from rsaj_labpa_hasil where "+KEY+" for update",key(k));
                if(oldHasil!=null)cloneRow(c,meta.get("rsaj_labpa_hasil"),oldHasil,target.kode,null);
                for(Map.Entry<Table,Integer> entry:resultTables.entrySet()){
                    Table t=entry.getKey();if(t.name.equals("periksa_lab")||t.name.equals("rsaj_labpa_hasil"))continue;
                    int n=execute(c,"update "+q(t.name)+" set kd_jenis_prw=? where "+KEY,target.kode,k.noRawat,k.kode,k.tanggal,k.jam);
                    require(n==entry.getValue(),"Data "+t.name+" berubah selama proses. Ulangi setelah memuat ulang.");
                }
                for(Map.Entry<Table,Integer> entry:orderTables.entrySet()){
                    Table t=entry.getKey();if(t.name.equals("permintaan_pemeriksaan_labpa"))continue;
                    int n=execute(c,"update "+q(t.name)+" set kd_jenis_prw=? where noorder=? and kd_jenis_prw=?",target.kode,d.noorder,k.kode);
                    require(n==entry.getValue(),"Detail "+t.name+" berubah selama proses.");
                }
                if(oldHasil!=null)require(execute(c,"delete from rsaj_labpa_hasil where "+KEY,key(k))==1,"Data hasil PA lama gagal dipindahkan.");
                if(oldOrder!=null)require(execute(c,"delete from permintaan_pemeriksaan_labpa where noorder=? and kd_jenis_prw=?",d.noorder,k.kode)==1,"Order lama gagal diganti.");
                require(execute(c,"delete from periksa_lab where "+KEY+" and kategori='PA'",key(k))==1,"Pemeriksaan lama gagal diganti.");
                String journal=journal(c,d,tariff,meta,target.kode,user);
                List<String> changed=new ArrayList<>();for(Table t:resultTables.keySet())changed.add(t.name);for(Table t:orderTables.keySet())changed.add(t.name);
                execute(c,"insert into rsaj_labpa_ubah_log (no_rawat,tgl_periksa,jam,kode_lama,kode_baru,jenis,no_pa,noorder,biaya_lama,biaya_baru,no_jurnal,alasan,diubah_oleh,diubah_pada,tabel_terkait) values (?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?)",
                        k.noRawat,k.tanggal,k.jam,k.kode,target.kode,d.jenis,d.noPA,d.noorder,d.biaya,money(tariff.get("total_byr")),journal,text(alasan),user,String.join(",",changed));
                c.commit();return d;
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
            finally{c.setAutoCommit(true);}
        }
    }

    private static String journal(Connection c,Data d,Map<String,Object> tariff,Map<String,Table> meta,String newCode,String user)throws SQLException{
        BigDecimal[] delta=new BigDecimal[MONEY.length];boolean changed=false;
        for(int i=0;i<MONEY.length;i++){
            delta[i]=money(tariff.get(i==0?"total_byr":MONEY[i])).subtract(money(d.old.get(MONEY[i])));
            changed|=delta[i].signum()!=0;
        }
        if(!changed)return "";
        transactional(meta.get("jurnal"));transactional(meta.get("detailjurnal"));
        boolean ri="Ranap".equalsIgnoreCase(d.status);
        require(ri||"Ralan".equalsIgnoreCase(d.status),"Status rawat hasil PA tidak valid.");
        String suffix=ri?"Ranap":"Ralan";
        String stock=ri?"Rawat_Inap":"Rawat_Jalan";
        String[][] akun={
            {"Suspen_Piutang_Laborat_"+suffix,"Laborat_"+suffix},
            {"Beban_Jasa_Medik_Dokter_Laborat_"+suffix,"Utang_Jasa_Medik_Dokter_Laborat_"+suffix},
            {"Beban_Jasa_Medik_Petugas_Laborat_"+suffix,"Utang_Jasa_Medik_Petugas_Laborat_"+suffix},
            {ri?"HPP_Persediaan_Laborat_Rawat_inap":"HPP_Persediaan_Laborat_Rawat_Jalan","Persediaan_BHP_Laborat_"+stock},
            {"Beban_Kso_Laborat_"+suffix,"Utang_Kso_Laborat_"+suffix},
            {"Beban_Jasa_Sarana_Laborat_"+suffix,"Utang_Jasa_Sarana_Laborat_"+suffix},
            {"Beban_Jasa_Perujuk_Laborat_"+suffix,"Utang_Jasa_Perujuk_Laborat_"+suffix},
            {"Beban_Jasa_Menejemen_Laborat_"+suffix,"Utang_Jasa_Menejemen_Laborat_"+suffix}
        };
        Map<String,Object> setting=one(c,"select * from "+(ri?"set_akun_ranap":"set_akun_ralan"));
        require(setting!=null,"Setting akun jurnal laboratorium belum tersedia.");
        Map<String,BigDecimal> ledger=new LinkedHashMap<>();
        for(int i=0;i<delta.length;i++)if(delta[i].signum()!=0){
            for(int side=0;side<2;side++){
                String code=text(setting.get(akun[i][side].toLowerCase(Locale.ROOT)));
                require(!code.isEmpty(),"Akun "+akun[i][side]+" belum diatur.");
                ledger.merge(code,side==0?delta[i]:delta[i].negate(),BigDecimal::add);
            }
        }
        Map<String,Object> date=one(c,"select current_date() as tanggal,current_time() as jam");String tanggal=text(date.get("tanggal"));
        Map<String,Object> max=one(c,"select coalesce(max(cast(right(no_jurnal,6) as unsigned)),0) as nomor from jurnal where tgl_jurnal=? for update",tanggal);
        long next=((Number)max.get("nomor")).longValue()+1;
        require(next<=999999,"Urutan jurnal harian sudah mencapai batas.");
        String no=String.format(Locale.ROOT,"JR%s%06d",tanggal.replace("-",""),next);
        String note="UBAH PEMERIKSAAN LAB PA "+d.kunci.kode+" -> "+newCode+" OLEH "+user;
        execute(c,"insert into jurnal (no_jurnal,no_bukti,tgl_jurnal,jam_jurnal,jenis,keterangan) values (?,?,?,?,'U',?)",no,d.kunci.noRawat,tanggal,date.get("jam"),note);
        BigDecimal sum=BigDecimal.ZERO;
        for(Map.Entry<String,BigDecimal> e:ledger.entrySet()){
            BigDecimal amount=e.getValue();sum=sum.add(amount);if(amount.signum()==0)continue;
            execute(c,"insert into detailjurnal (no_jurnal,kd_rek,debet,kredit) values (?,?,?,?)",no,e.getKey(),amount.signum()>0?amount:BigDecimal.ZERO,amount.signum()<0?amount.negate():BigDecimal.ZERO);
        }
        require(sum.signum()==0,"Jurnal perubahan pemeriksaan tidak seimbang.");return no;
    }
}
