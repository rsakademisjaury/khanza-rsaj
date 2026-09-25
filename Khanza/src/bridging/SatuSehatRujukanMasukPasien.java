package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.*;
import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import javax.swing.*;
import simrskhanza.DlgPasien;

/** Membuka master pasien. Tidak membuat kunjungan atau menulis reg_periksa. */
public final class SatuSehatRujukanMasukPasien {
    private static final Map<Component,DlgPasien> OPEN=new WeakHashMap<Component,DlgPasien>();
    private SatuSehatRujukanMasukPasien(){}

    public static final class State {
        public String noRm="";
        public boolean found(){return !noRm.isEmpty();}
        public String message(){
            if(!allowed())return "Akun ini tidak memiliki hak Data Pasien atau registrasi IGD.";
            return found()?"Pasien sudah memiliki No. RM "+noRm+". Buka data pasien untuk diperiksa."
                    :"Data rujukan akan diisikan ke DlgPasien. Periksa identitas dan kelengkapan sebelum Simpan.";
        }
    }
    private static final class Prepared {
        final Map<String,String> data;final State state;
        Prepared(Map<String,String> data,State state){this.data=data;this.state=state;}
    }
    /** Khusus alur rujukan masuk: hak Data Pasien ATAU hak registrasi IGD. Tidak membuka menu Data Pasien umum. */
    public static boolean allowed(){return akses.getpasien()||SatuSehatRujukanMasukRegistrasi.allowed();}
    public static State state(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail)throws SQLException{
        return match(mapPatient(detail.patient));
    }
    private static Connection connection()throws SQLException{
        Connection c=koneksiDB.condb();
        if(c==null||c.isClosed())throw new SQLException("Koneksi database pasien belum tersedia.");
        return c;
    }
    private static PreparedStatement prepare(Connection c,String sql)throws SQLException{
        PreparedStatement p=c.prepareStatement(sql);try{p.setQueryTimeout(15);}catch(SQLException ex){}return p;
    }
    public static State match(Map<String,String> data)throws SQLException{
        Connection c=connection();synchronized(c){
            Set<String> matches=new LinkedHashSet<String>();
            String identity=value(data,"no_ktp"),insurance=value(data,"no_peserta"),ihs=value(data,"ihs");
            if(!identity.isEmpty())collect(c,"SELECT no_rkm_medis,no_ktp FROM pasien WHERE no_ktp=? LIMIT 2",identity,identity,matches);
            // Cache IHS lokal tidak wajib ada pada seluruh instalasi Khanza.
            // Jika tabel satu_sehat_pasien belum tersedia, pencocokan tetap dilanjutkan
            // menggunakan NIK/no peserta sehingga pembukaan DlgPasien tidak gagal.
            if(!ihs.isEmpty())collectIhsOptional(c,ihs,identity,matches);
            if(!insurance.isEmpty())collect(c,"SELECT no_rkm_medis,no_ktp FROM pasien WHERE no_peserta=? LIMIT 2",insurance,identity,matches);
            if(matches.size()>1)throw new SQLException("Identitas rujukan cocok dengan lebih dari satu nomor RM. Periksa master pasien sebelum melanjutkan.");
            State state=new State();if(!matches.isEmpty())state.noRm=matches.iterator().next();return state;
        }
    }
    private static void collectIhsOptional(Connection c,String ihs,String identity,Set<String> matches)throws SQLException{
        try{
            collect(c,"SELECT DISTINCT p.no_rkm_medis,p.no_ktp FROM satu_sehat_pasien s JOIN pasien p ON p.no_rkm_medis=s.no_rkm_medis WHERE s.ihs_pasien=? LIMIT 2",ihs,identity,matches);
        }catch(SQLException ex){
            if(isMissingTable(ex))return;
            throw ex;
        }
    }
    private static boolean isMissingTable(SQLException ex){
        for(SQLException current=ex;current!=null;current=current.getNextException()){
            String state=safe(current.getSQLState());
            String message=safe(current.getMessage()).toLowerCase(Locale.ENGLISH);
            if("42S02".equals(state)||current.getErrorCode()==1146||message.contains("doesn't exist")||message.contains("does not exist"))return true;
        }
        return false;
    }
    private static void collect(Connection c,String sql,String key,String identity,Set<String> matches)throws SQLException{
        try(PreparedStatement p=prepare(c,sql)){
            p.setString(1,key);try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    String localIdentity=safe(r.getString(2));
                    if(!identity.isEmpty()&&!localIdentity.isEmpty()&&!"-".equals(localIdentity)&&!identity.equals(localIdentity))
                        throw new SQLException("IHS/nomor peserta rujukan terhubung ke pasien dengan identitas berbeda. Periksa master pasien.");
                    matches.add(safe(r.getString(1)));
                }
            }
        }
    }
    /** Pemeriksaan ulang tepat sebelum handler Simpan/Ganti asli dijalankan. */
    public static void validateSave(Map<String,String> source,String noRm,String identity,String insurance,boolean editing)throws SQLException{
        if(!allowed())throw new SQLException("Hak Data Pasien atau registrasi IGD tidak tersedia.");
        String original=value(source,"no_ktp");
        if(!original.isEmpty()&&!original.equals(safe(identity)))throw new SQLException("Identitas pasien berbeda dari rujukan. Periksa kembali NIK/nomor identitas sebelum menyimpan.");
        Map<String,String> current=new HashMap<String,String>(source);
        current.put("no_ktp",safe(identity));current.put("no_peserta",safe(insurance));
        State found=match(current);
        if(found.found()&&(!editing||!found.noRm.equals(safe(noRm))))
            throw new SQLException("Pasien sudah memiliki nomor RM "+found.noRm+". Gunakan data pasien tersebut agar tidak membuat duplikat.");
    }
    public static void closeFor(Component parent){DlgPasien dialog=OPEN.remove(parent);if(dialog!=null)closeDialog(dialog);}
    private static void closeDialog(DlgPasien dialog){
        try{dialog.getClass().getMethod("tutupPasienRujukanMasuk").invoke(dialog);}
        catch(Exception ex){System.err.println("Penutupan form pasien rujukan belum lengkap: "+ex.getClass().getSimpleName());}
        catch(LinkageError ex){System.err.println("Versi form pasien rujukan belum cocok.");}
        finally{dialog.dispose();}
    }
    static Method patientHook(Class<?> type)throws SQLException{
        try{
            Method hook=type.getMethod("isiPasienRujukanMasuk",Map.class,String.class);
            if(hook.getReturnType()!=Void.TYPE||Modifier.isStatic(hook.getModifiers()))throw new NoSuchMethodException();
            return hook;
        }catch(NoSuchMethodException ex){throw incompatible(ex);}
        catch(LinkageError ex){throw incompatible(ex);}
    }
    private static SQLException incompatible(Throwable ex){return new SQLException("DlgPasien dan modul rujukan belum satu versi. Pasang sumber revisi lalu Clean and Build proyek lengkap.",ex);}
    public static void open(final Component parent,final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail,final Runnable finished,final Consumer<String> failed){
        if(!allowed()){failed.accept("Akun ini tidak memiliki hak Data Pasien atau registrasi IGD.");return;}
        DlgPasien existing=OPEN.get(parent);if(existing!=null&&existing.isDisplayable()){existing.toFront();return;}
        final String operator=safe(akses.getkode());
        new SwingWorker<Prepared,Void>(){
            @Override protected Prepared doInBackground()throws Exception{
                SatuSehatRujukanIGDRanapApi api=new SatuSehatRujukanIGDRanapApi();
                JsonNode task=api.cekTask(detail.taskId);api.validateIncomingTask(task);
                if(!"accepted".equals(SatuSehatRujukanIGDRanapApi.parseDecision(task)))throw new SQLException("Rujukan belum diterima atau status telah berubah.");
                Map<String,String> data=mapPatient(detail.patient);
                String ref=SatuSehatRujukanMasukService.reference(task.path("for"));
                if(value(data,"ihs").isEmpty()||!ref.equals("Patient/"+value(data,"ihs")))throw new SQLException("Identitas pasien pada Task belum cocok. Muat ulang detail rujukan.");
                data.put("task_id",detail.taskId);return new Prepared(data,match(data));
            }
            @Override protected void done(){
                if(!parent.isShowing()){finished.run();return;}
                DlgPasien dialog=null;java.awt.event.WindowAdapter listener=null;boolean opened=false;
                try{
                    Prepared p=get();if(!allowed()||!operator.equals(safe(akses.getkode())))throw new SQLException("Sesi/hak akses Data Pasien/registrasi IGD berubah. Buka kembali rujukan.");
                    Method hook=patientHook(DlgPasien.class);
                    Window w=SwingUtilities.getWindowAncestor(parent);while(w!=null&&!(w instanceof Frame))w=w.getOwner();
                    dialog=new DlgPasien(w instanceof Frame?(Frame)w:null,false);
                    Window inbox=SwingUtilities.getWindowAncestor(parent);Component anchor=inbox instanceof RootPaneContainer?((RootPaneContainer)inbox).getContentPane():parent;
                    Point point=anchor.getLocationOnScreen();dialog.setBounds(point.x,point.y,anchor.getWidth(),anchor.getHeight());
                    final DlgPasien current=dialog;
                    listener=new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){if(OPEN.get(parent)==current)OPEN.remove(parent);finished.run();}};
                    OPEN.put(parent,dialog);dialog.addWindowListener(listener);dialog.setVisible(true);
                    // Terapkan data SETELAH window benar-benar tampil. Beberapa komponen Khanza
                    // melakukan normalisasi saat first-show; pengisian sebelum setVisible dapat
                    // terlihat kosong walaupun Map sudah berisi data.
                    try{hook.invoke(dialog,p.data,p.state.noRm);}
                    catch(InvocationTargetException ex){Throwable cause=ex.getCause();if(cause instanceof LinkageError)throw incompatible(cause);if(cause instanceof Exception)throw (Exception)cause;if(cause instanceof Error)throw (Error)cause;throw ex;}
                    dialog.revalidate();dialog.repaint();dialog.toFront();opened=true;
                }catch(Exception ex){Throwable cause=ex instanceof java.util.concurrent.ExecutionException&&ex.getCause()!=null?ex.getCause():ex;failed.accept(safe(cause.getMessage()).isEmpty()?"Data pasien belum dapat dibuka.":cause.getMessage());}
                catch(LinkageError ex){failed.accept(incompatible(ex).getMessage());}
                finally{if(!opened&&dialog!=null){if(OPEN.get(parent)==dialog)OPEN.remove(parent);if(listener!=null)dialog.removeWindowListener(listener);closeDialog(dialog);}}
            }
        }.execute();
    }

    /** Nilai demografi yang tersedia saja; nomor RM pengirim tidak menjadi RM lokal. */
    public static Map<String,String> mapPatient(JsonNode patient)throws SQLException{
        if(patient==null||!"Patient".equals(text(patient,"resourceType")))throw new SQLException("Resource Patient belum tersedia. Muat ulang detail rujukan.");
        Map<String,String> m=new LinkedHashMap<String,String>();
        put(m,"ihs",text(patient,"id"));put(m,"nm_pasien",name(preferred(patient.path("name"),"official")));
        String nik=identifier(patient,"https://fhir.kemkes.go.id/id/nik");
        if(nik.isEmpty())nik=identifierBySystemHint(patient,"nik");
        if(nik.isEmpty())nik=identifierByPattern(patient,"[0-9]{16}");
        if(!nik.isEmpty()&&!nik.matches("[0-9]{16}")){note(m,"NIK rujukan tidak berformat 16 digit; periksa identitas.");nik="";}
        String passport=identifier(patient,"https://fhir.kemkes.go.id/id/paspor");
        if(passport.isEmpty())passport=identifierBySystemHint(patient,"passport","paspor");
        put(m,"no_ktp",nik.isEmpty()?passport:nik);
        String insurance=identifier(patient,"https://fhir.kemkes.go.id/id/bpjs");
        if(insurance.isEmpty())insurance=identifierBySystemHint(patient,"bpjs","jkn","insurance");
        put(m,"no_peserta",insurance);
        if(value(m,"ihs").isEmpty())put(m,"ihs",first(identifier(patient,"https://fhir.kemkes.go.id/id/patient-ihs-number"),identifierBySystemHint(patient,"ihs-number","ihs_number")));
        String gender=text(patient,"gender").toLowerCase(Locale.ENGLISH);
        put(m,"jk",("male".equals(gender)||"m".equals(gender)||"l".equals(gender)||gender.contains("laki"))?"LAKI-LAKI":("female".equals(gender)||"f".equals(gender)||"p".equals(gender)||gender.contains("perempuan"))?"PEREMPUAN":"");
        String birth=text(patient,"birthDate");if(validBirth(birth))put(m,"tgl_lahir",birth);else note(m,"Tanggal lahir belum lengkap/valid; isi tanggal yang benar.");
        put(m,"no_tlp",telecom(patient,"phone"));put(m,"email",telecom(patient,"email"));
        address(preferred(patient.path("address"),"home"),m,"");
        put(m,"stts_nikah",marital(patient.path("maritalStatus"),gender));
        for(JsonNode e:patient.path("extension")){
            String url=text(e,"url");
            if("http://hl7.org/fhir/StructureDefinition/patient-birthPlace".equals(url)){
                JsonNode a=e.path("valueAddress");put(m,"tmp_lahir",first(text(a,"city"),text(a,"text"),join(a.path("line"),", ")));
            }else if("http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName".equals(url))put(m,"nm_ibu",text(e,"valueString"));
            else if("http://hl7.org/fhir/StructureDefinition/patient-religion".equals(url))put(m,"agama",religion(concept(e.path("valueCodeableConcept"))));
        }
        JsonNode contact=preferred(patient.path("contact"),"");
        if(contact!=null){
            put(m,"namakeluarga",name(contact.path("name")));put(m,"keluarga",relationship(contact));
            address(contact.path("address"),m,"pj");
            if(value(m,"nm_ibu").isEmpty()&&"IBU".equals(value(m,"keluarga")))put(m,"nm_ibu",value(m,"namakeluarga"));
        }
        JsonNode language=null;
        for(JsonNode c:patient.path("communication")){if(language==null)language=c.path("language");if(c.path("preferred").asBoolean()){language=c.path("language");break;}}
        if(language!=null)put(m,"bahasa",concept(language));
        if(value(m,"no_ktp").isEmpty())note(m,"NIK/identitas belum tersedia; cari pasien lama sebelum membuat RM baru.");
        if(value(m,"jk").isEmpty())note(m,"Jenis kelamin perlu diverifikasi.");
        if(value(m,"nm_pasien").isEmpty())note(m,"Nama pasien belum tersedia pada resource Patient; periksa Detail Rujukan/JSON.");
        return m;
    }
    private static String identifier(JsonNode patient,String system)throws SQLException{
        String value="";
        for(JsonNode id:patient.path("identifier"))if(system.equals(text(id,"system"))&&!"old".equals(text(id,"use"))){
            String candidate=text(id,"value");if(candidate.isEmpty())continue;
            if(!value.isEmpty()&&!value.equals(candidate))throw new SQLException("Ada lebih dari satu identitas aktif untuk sistem "+system+". Periksa data pasien rujukan.");
            value=candidate;
        }return value;
    }
    private static String identifierBySystemHint(JsonNode patient,String... hints)throws SQLException{
        String value="";
        for(JsonNode id:patient.path("identifier")){
            if("old".equals(text(id,"use")))continue;
            String system=text(id,"system").toLowerCase(Locale.ENGLISH),candidate=text(id,"value");
            if(candidate.isEmpty())continue;
            boolean match=false;for(String hint:hints)if(!safe(hint).isEmpty()&&system.contains(hint.toLowerCase(Locale.ENGLISH))){match=true;break;}
            if(!match)continue;
            if(!value.isEmpty()&&!value.equals(candidate))throw new SQLException("Ada lebih dari satu identitas aktif yang cocok pada Patient. Periksa data pasien rujukan.");
            value=candidate;
        }
        return value;
    }
    private static String identifierByPattern(JsonNode patient,String regex)throws SQLException{
        String value="";
        for(JsonNode id:patient.path("identifier")){
            if("old".equals(text(id,"use")))continue;String candidate=text(id,"value");
            if(candidate.isEmpty()||!candidate.matches(regex))continue;
            String system=text(id,"system").toLowerCase(Locale.ENGLISH);
            if(system.contains("ihs"))continue;
            if(!value.isEmpty()&&!value.equals(candidate))throw new SQLException("Ada lebih dari satu identitas aktif dengan format yang sama. Periksa data pasien rujukan.");
            value=candidate;
        }
        return value;
    }
    private static JsonNode preferred(JsonNode list,String use){
        JsonNode first=null;
        for(JsonNode n:list){if("old".equals(text(n,"use")))continue;if(first==null)first=n;if(!use.isEmpty()&&use.equals(text(n,"use")))return n;}
        return first;
    }
    private static String name(JsonNode n){
        if(n==null)return "";
        return first(text(n,"text"),joinWords(join(n.path("prefix")," "),join(n.path("given")," "),text(n,"family"),join(n.path("suffix")," ")));
    }
    private static String telecom(JsonNode p,String system){
        String result="";
        for(JsonNode t:p.path("telecom"))if(system.equals(text(t,"system"))&&!"old".equals(text(t,"use"))){String v=text(t,"value");if(v.isEmpty())continue;if(result.isEmpty())result=v;if("mobile".equals(text(t,"use")))return v;}
        return result;
    }
    private static void address(JsonNode a,Map<String,String> m,String suffix){
        if(a==null)return;
        String line=first(join(a.path("line"),", "),text(a,"text"));
        String rt="",rw="";StringBuilder codes=new StringBuilder();
        for(JsonNode e:a.path("extension"))if("https://fhir.kemkes.go.id/r4/StructureDefinition/administrativeCode".equals(text(e,"url"))){
            for(JsonNode item:e.path("extension")){
                String key=text(item,"url"),v=first(text(item,"valueCode"),text(item,"valueString"),text(item.path("valueCoding"),"display"),text(item.path("valueCoding"),"code"));
                if("rt".equalsIgnoreCase(key))rt=v;else if("rw".equalsIgnoreCase(key))rw=v;
                else if(!v.isEmpty()){
                    String human=humanRegion(v);
                    if(!human.isEmpty()){
                        if(("province".equalsIgnoreCase(key)||"provinsi".equalsIgnoreCase(key))&&value(m,"propinsi"+suffix).isEmpty())put(m,"propinsi"+suffix,human);
                        else if(("city".equalsIgnoreCase(key)||"regency".equalsIgnoreCase(key)||"kabupaten".equalsIgnoreCase(key))&&value(m,"kabupaten"+suffix).isEmpty())put(m,"kabupaten"+suffix,human);
                        else if(("district".equalsIgnoreCase(key)||"kecamatan".equalsIgnoreCase(key))&&value(m,"kecamatan"+suffix).isEmpty())put(m,"kecamatan"+suffix,human);
                        else if(("village".equalsIgnoreCase(key)||"kelurahan".equalsIgnoreCase(key))&&value(m,"kelurahan"+suffix).isEmpty())put(m,"kelurahan"+suffix,human);
                    }
                    if(codes.length()>0)codes.append(", ");codes.append(key).append("=").append(v);
                }
            }
        }
        if(!rt.isEmpty()&&!line.toUpperCase(Locale.ROOT).matches(".*\\bRT\\b.*"))line=joinWords(line,"RT "+rt);
        if(!rw.isEmpty()&&!line.toUpperCase(Locale.ROOT).matches(".*\\bRW\\b.*"))line=joinWords(line,"RW "+rw);
        String postal=text(a,"postalCode");if(!postal.isEmpty()&&!line.contains(postal))line=joinWords(line,"Kode Pos "+postal);
        put(m,"alamat"+suffix,line);
        if(value(m,"kabupaten"+suffix).isEmpty())put(m,"kabupaten"+suffix,humanRegion(text(a,"city")));
        if(value(m,"kecamatan"+suffix).isEmpty())put(m,"kecamatan"+suffix,humanRegion(text(a,"district")));
        if(value(m,"propinsi"+suffix).isEmpty())put(m,"propinsi"+suffix,humanRegion(text(a,"state")));
        // Kode wilayah FHIR berbeda dari primary key master alamat Khanza.
        if(codes.length()>0)note(m,"Kode wilayah "+(suffix.isEmpty()?"pasien":"P.J.")+": "+codes+"; cocokkan nama wilayah melalui pilihan master.");
    }
    private static String humanRegion(String s){return s.matches("[0-9. -]+")?"":s;}
    private static String marital(JsonNode c,String gender){
        for(JsonNode code:c.path("coding"))if("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus".equals(text(code,"system"))){
            String v=text(code,"code");if("M".equals(v))return "MENIKAH";if("S".equals(v))return "BELUM MENIKAH";if("D".equals(v))return "CERAI HIDUP";if("W".equals(v))return "CERAI MATI";
        }
        return choice(concept(c),new String[]{"MENIKAH","BELUM MENIKAH","JANDA","DUDHA","CERAI HIDUP","CERAI MATI"});
    }
    private static String relationship(JsonNode contact){
        for(JsonNode r:contact.path("relationship")){
            String label=choice(concept(r),new String[]{"AYAH","IBU","SUAMI","ISTRI","SAUDARA","SEPUPU","ANAK","CUCU","KAKEK","NENEK","PAMAN","BIBI","TEMAN","REKAN KERJA","MERTUA","MENANTU","KEMANAKAN","TETANGGA","DIRI SENDIRI"});
            if(!label.isEmpty())return label;
            for(JsonNode c:r.path("coding"))if("http://terminology.hl7.org/CodeSystem/v3-RoleCode".equals(text(c,"system"))){
                String v=text(c,"code");if("MTH".equals(v))return "IBU";if("FTH".equals(v))return "AYAH";if("HUSB".equals(v))return "SUAMI";if("WIFE".equals(v))return "ISTRI";if("SON".equals(v)||"DAU".equals(v)||"CHILD".equals(v))return "ANAK";
            }
        }return "";
    }
    private static String religion(String v){return choice(v,new String[]{"ISLAM","KRISTEN","KATOLIK","HINDU","BUDHA","KONG HU CHU","-"});}
    private static String choice(String v,String[] choices){for(String c:choices)if(c.equalsIgnoreCase(v))return c;return "";}
    private static String concept(JsonNode c){if(c==null)return "";String t=text(c,"text");if(!t.isEmpty())return t;for(JsonNode code:c.path("coding")){t=text(code,"display");if(!t.isEmpty())return t;}return "";}
    public static boolean validBirth(String date){try{if(!date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return false;LocalDate birth=LocalDate.parse(date);return birth.getYear()>0&&!birth.isAfter(LocalDate.now());}catch(RuntimeException ex){return false;}}
    public static String value(Map<String,String> data,String key){return safe(data.get(key));}
    private static void put(Map<String,String> m,String key,String v){if(!safe(v).isEmpty())m.put(key,safe(v));}
    private static void note(Map<String,String> m,String v){m.put("catatan",joinWords(value(m,"catatan"),v));}
    private static String text(JsonNode n,String key){if(n==null)return "";JsonNode v=n.path(key);return v.isMissingNode()||v.isNull()?"":safe(v.asText());}
    private static String first(String... values){for(String v:values)if(!safe(v).isEmpty())return safe(v);return "";}
    private static String joinWords(String... values){StringBuilder b=new StringBuilder();for(String v:values)if(!safe(v).isEmpty()){if(b.length()>0)b.append(' ');b.append(safe(v));}return b.toString();}
    private static String join(JsonNode list,String separator){StringBuilder b=new StringBuilder();for(JsonNode n:list){String v=n.isNull()?"":safe(n.asText());if(v.isEmpty())continue;if(b.length()>0)b.append(separator);b.append(v);}return b.toString();}
    private static String safe(String s){return s==null?"":s.trim();}
}
