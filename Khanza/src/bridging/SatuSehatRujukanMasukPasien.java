package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import javax.swing.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
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
                    :"Periksa data yang tersedia dari rujukan sebelum membuat nomor RM.";
        }
    }
    private static final class Prepared {
        final Map<String,String> data;final State state;
        Prepared(Map<String,String> data,State state){this.data=data;this.state=state;}
    }
    /** Khusus alur rujukan masuk: hak Data Pasien ATAU hak registrasi IGD. Tidak membuka menu Data Pasien umum. */
    public static boolean allowed(){return akses.getpasien()||SatuSehatRujukanMasukRegistrasi.allowed();}
    public static State state(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail)throws SQLException{
        return match(mapDetailPatient(detail,detail.task));
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
            if(!isUnknownColumn(ex))throw ex;
        }
        // Cache lama bisa berisi IHS dan NIK sementara No. RM kosong atau sudah berubah.
        try{
            collect(c,"SELECT DISTINCT p.no_rkm_medis,p.no_ktp FROM satu_sehat_pasien s JOIN pasien p ON p.no_ktp=s.no_ktp WHERE s.ihs_pasien=? AND s.no_ktp REGEXP '^[0-9]{16}$' AND s.no_ktp<>'0000000000000000' LIMIT 2",ihs,identity,matches);
        }catch(SQLException ex){
            if(!isMissingTable(ex)&&!isUnknownColumn(ex))throw ex;
        }
    }
    private static boolean isUnknownColumn(SQLException ex){return "42S22".equals(ex.getSQLState())||ex.getErrorCode()==1054;}
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
                    String rm=safe(r.getString(1));
                    if(rm.isEmpty()||"-".equals(rm))continue;
                    String localIdentity=safe(r.getString(2));
                    if(!identity.isEmpty()&&!localIdentity.isEmpty()&&!"-".equals(localIdentity)&&!identity.equals(localIdentity))
                        throw new SQLException("IHS/nomor peserta rujukan terhubung ke pasien dengan identitas berbeda. Periksa master pasien.");
                    matches.add(rm);
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
        try{dialog.tutupPasienRujukanMasuk();}
        catch(Exception ex){System.err.println("Penutupan form pasien rujukan belum lengkap: "+ex.getClass().getSimpleName());}
        catch(LinkageError ex){System.err.println("Versi form pasien rujukan belum cocok.");}
        finally{dialog.dispose();}
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
                Map<String,String> data=mapDetailPatient(detail,task);
                SatuSehatRujukanMasukPasien.State state=match(data);
                String nik=value(data,"no_ktp");
                if(!state.found()&&needsDemographics(data)&&isNik(nik)){
                    JsonNode result=null;
                    try{result=searchPatientByNik(nik,value(data,"ihs"));}
                    catch(Exception ex){
                        note(data,"Pencarian demografi melalui NIK belum berhasil.");
                    }
                    if(result!=null){
                        Map<String,String> verified=mapPatient(result);
                        String resultNik=value(verified,"no_ktp");
                        if(isNik(resultNik)&&!nik.equals(resultNik))throw new SQLException("NIK pada hasil SATUSEHAT berbeda. Periksa identitas pasien rujukan.");
                        verified.remove("no_ktp");
                        mergePatientData(data,verified);
                        data.put("no_ktp",nik);
                        state=match(data);
                    }
                }
                addMissingFieldNotes(data);
                data.put("task_id",detail.taskId);return new Prepared(data,state);
            }
            @Override protected void done(){
                if(!parent.isShowing()){finished.run();return;}
                DlgPasien dialog=null;java.awt.event.WindowAdapter listener=null;boolean opened=false;
                try{
                    Prepared p=get();if(!allowed()||!operator.equals(safe(akses.getkode())))throw new SQLException("Sesi/hak akses Data Pasien/registrasi IGD berubah. Buka kembali rujukan.");
                    Window w=SwingUtilities.getWindowAncestor(parent);while(w!=null&&!(w instanceof Frame))w=w.getOwner();
                    dialog=new DlgPasien(w instanceof Frame?(Frame)w:null,false);
                    Window inbox=SwingUtilities.getWindowAncestor(parent);Component anchor=inbox instanceof RootPaneContainer?((RootPaneContainer)inbox).getContentPane():parent;
                    Point point=anchor.getLocationOnScreen();dialog.setBounds(point.x,point.y,anchor.getWidth(),anchor.getHeight());
                    final DlgPasien current=dialog;
                    listener=new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){if(OPEN.get(parent)==current)OPEN.remove(parent);finished.run();}};
                    dialog.isiPasienRujukanMasuk(p.data,p.state.noRm);
                    OPEN.put(parent,dialog);dialog.addWindowListener(listener);dialog.setVisible(true);
                    dialog.toFront();opened=true;
                }catch(Exception ex){Throwable cause=ex instanceof java.util.concurrent.ExecutionException&&ex.getCause()!=null?ex.getCause():ex;failed.accept(safe(cause.getMessage()).isEmpty()?"Data pasien belum dapat dibuka.":cause.getMessage());}
                catch(LinkageError ex){failed.accept(incompatible(ex).getMessage());}
                finally{if(!opened&&dialog!=null){if(OPEN.get(parent)==dialog)OPEN.remove(parent);if(listener!=null)dialog.removeWindowListener(listener);closeDialog(dialog);}}
            }
        }.execute();
    }

    static Map<String,String> mapDetailPatient(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail,JsonNode task)throws SQLException{
        if(task==null)throw new SQLException("Task rujukan belum tersedia. Muat ulang detail rujukan.");
        JsonNode taskPatient=task.path("for");
        String ref=SatuSehatRujukanMasukService.reference(taskPatient);
        if(ref.isEmpty()&&detail.carePlan!=null){
            // Gunakan subjek CarePlan hanya jika Task yang dimuat ulang masih merujuk CarePlan tersebut.
            String carePlanRef="CarePlan/"+text(detail.carePlan,"id");
            for(JsonNode basedOn:task.path("basedOn"))if(carePlanRef.equals(SatuSehatRujukanMasukService.reference(basedOn))){
                taskPatient=detail.carePlan.path("subject");
                ref=SatuSehatRujukanMasukService.reference(taskPatient);
                break;
            }
        }
        if(!ref.matches("Patient/[A-Za-z0-9.\\-]{1,64}"))throw new SQLException("IHS pasien pada rujukan belum tersedia. Muat ulang detail rujukan.");
        String ihs=ref.substring("Patient/".length());
        Map<String,String> data=new LinkedHashMap<String,String>();
        data.put("ihs",ihs);
        if("Patient".equals(text(detail.patient,"resourceType"))){
            Map<String,String> primary=mapPatient(detail.patient);
            if(!ihs.equals(value(primary,"ihs")))throw new SQLException("Identitas pasien pada rujukan belum cocok. Muat ulang detail rujukan.");
            mergePatientData(data,primary);
        }
        for(SatuSehatRujukanIGDRanapApi.IncomingReferencedResource resource:detail.references){
            if("Patient".equals(text(resource.resource,"resourceType"))&&ihs.equals(text(resource.resource,"id"))&&resource.resource!=detail.patient){
                mergePatientData(data,mapPatient(resource.resource));
            }
        }
        mergePatientReference(data,ref,taskPatient);
        if(detail.carePlan!=null)mergePatientReference(data,ref,detail.carePlan.path("subject"));
        if(detail.serviceRequest!=null)mergePatientReference(data,ref,detail.serviceRequest.path("subject"));
        if(detail.encounter!=null)mergePatientReference(data,ref,detail.encounter.path("subject"));
        return data;
    }

    private static void mergePatientData(Map<String,String> data,Map<String,String> source)throws SQLException{
        for(Map.Entry<String,String> entry:source.entrySet()){
            String key=entry.getKey(),current=value(data,key),candidate=entry.getValue();
            if(current.isEmpty())data.put(key,candidate);
            else if("no_ktp".equals(key)&&isNik(current)!=isNik(candidate)){
                if(isNik(candidate))data.put(key,candidate);
            }
            else if(("no_ktp".equals(key)||"no_peserta".equals(key))&&!current.equals(candidate))
                throw new SQLException("Identitas dari sumber rujukan berbeda. Periksa data pasien sebelum melanjutkan.");
        }
    }

    private static void mergePatientReference(Map<String,String> data,String ref,JsonNode patientRef)throws SQLException{
        if(!ref.equals(SatuSehatRujukanMasukService.reference(patientRef)))return;
        JsonNode identifier=patientRef.path("identifier");
        String system=text(identifier,"system").toLowerCase(Locale.ROOT);
        String identity=identifierValue(identifier);
        Map<String,String> source=new LinkedHashMap<String,String>();
        String display=usable(text(patientRef,"display"));
        if(!display.equals(ref)&&!display.equals(value(data,"ihs"))&&!display.startsWith("Patient/"))put(source,"nm_pasien",display);
        if(system.endsWith("/nik")&&isNik(identity))put(source,"no_ktp",identity);
        if(system.endsWith("/bpjs")||system.endsWith("/jkn"))put(source,"no_peserta",identity);
        mergePatientData(data,source);
    }

    private static JsonNode searchPatientByNik(String nik,String expectedIhs)throws Exception{
        ApiSatuSehat api=new ApiSatuSehat();
        HttpHeaders headers=new HttpHeaders();
        headers.add("Accept","application/fhir+json, application/json");
        String token=safe(api.getValidToken());
        if(token.isEmpty())throw new IOException("Token SATUSEHAT belum tersedia.");
        headers.add("Authorization","Bearer "+token);
        String base=koneksiDB.URLFHIRSATUSEHAT().replaceFirst("/+$","");
        String identifier=URLEncoder.encode("https://fhir.kemkes.go.id/id/nik|"+nik,"UTF-8");
        URI uri=URI.create(base+"/Patient?identifier="+identifier);
        if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null)throw new IOException("URL FHIR SATUSEHAT harus memakai HTTPS.");
        SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory(){
            @Override protected void prepareConnection(HttpURLConnection connection,String method)throws IOException{
                super.prepareConnection(connection,method);
                // Spring 3.1.0.M2 belum menyediakan setter timeout pada factory.
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(12000);
            }
        };
        String body=new RestTemplate(factory).exchange(uri,HttpMethod.GET,new HttpEntity<String>(headers),String.class).getBody();
        return body==null?null:patientFromNikSearch(new ObjectMapper().readTree(body),expectedIhs);
    }

    static JsonNode patientFromNikSearch(JsonNode bundle,String expectedIhs){
        if(!"Bundle".equals(text(bundle,"resourceType"))||expectedIhs.isEmpty())return null;
        for(JsonNode link:bundle.path("link"))if("next".equals(text(link,"relation")))return null;
        JsonNode match=null;
        for(JsonNode entry:bundle.path("entry")){
            JsonNode patient=entry.path("resource");
            if(!"Patient".equals(text(patient,"resourceType")))continue;
            if(!expectedIhs.equals(text(patient,"id"))||match!=null)return null;
            match=patient;
        }
        return match;
    }

    private static boolean needsDemographics(Map<String,String> data){
        return value(data,"nm_pasien").isEmpty()||value(data,"jk").isEmpty()||value(data,"tgl_lahir").isEmpty();
    }

    private static void addMissingFieldNotes(Map<String,String> data){
        if(value(data,"nm_pasien").isEmpty())note(data,"Nama pasien belum tersedia.");
        if(value(data,"jk").isEmpty())note(data,"Jenis kelamin perlu diisi sesuai identitas pasien.");
        if(value(data,"tgl_lahir").isEmpty())note(data,"Tanggal lahir perlu diisi sesuai identitas pasien.");
        if(value(data,"no_ktp").isEmpty())note(data,"NIK belum tersedia dari rujukan.");
        if(value(data,"no_peserta").isEmpty())note(data,"Nomor peserta belum tersedia.");
    }

    /** Nilai demografi yang tersedia saja; nomor RM pengirim tidak menjadi RM lokal. */
    public static Map<String,String> mapPatient(JsonNode patient)throws SQLException{
        if(patient==null||!"Patient".equals(text(patient,"resourceType")))throw new SQLException("Resource Patient belum tersedia. Muat ulang detail rujukan.");
        Map<String,String> m=new LinkedHashMap<String,String>();
        put(m,"ihs",text(patient,"id"));
        String patientName=name(preferred(patient.path("name"),"official"));
        if(patientName.isEmpty())for(JsonNode n:patient.path("name"))if(!"old".equals(text(n,"use"))){
            patientName=name(n);if(!patientName.isEmpty())break;
        }
        put(m,"nm_pasien",patientName);
        String nik=identifier(patient,"https://fhir.kemkes.go.id/id/nik");
        if(nik.isEmpty())nik=identifierBySystemHint(patient,"nik");
        if(!isNik(nik))nik="";
        String passport=identifier(patient,"https://fhir.kemkes.go.id/id/paspor");
        if(passport.isEmpty())passport=identifierBySystemHint(patient,"passport","paspor");
        put(m,"no_ktp",nik.isEmpty()?passport:nik);
        String insurance=identifier(patient,"https://fhir.kemkes.go.id/id/bpjs");
        if(insurance.isEmpty())insurance=identifierBySystemHint(patient,"bpjs","jkn","insurance","no-peserta","no_peserta","nomor-peserta","nomor_peserta");
        put(m,"no_peserta",insurance);
        if(value(m,"ihs").isEmpty())put(m,"ihs",first(identifier(patient,"https://fhir.kemkes.go.id/id/patient-ihs-number"),identifierBySystemHint(patient,"ihs-number","ihs_number")));
        String gender=text(patient,"gender").toLowerCase(Locale.ENGLISH);
        put(m,"jk",("male".equals(gender)||"m".equals(gender)||"l".equals(gender)||gender.contains("laki"))?"LAKI-LAKI":("female".equals(gender)||"f".equals(gender)||"p".equals(gender)||gender.contains("perempuan"))?"PEREMPUAN":"");
        String birth=text(patient,"birthDate");if(validBirth(birth))put(m,"tgl_lahir",birth);
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
        return m;
    }
    private static String identifier(JsonNode patient,String system)throws SQLException{
        String value="";
        for(JsonNode id:patient.path("identifier"))if(system.equals(text(id,"system"))&&!"old".equals(text(id,"use"))){
            String candidate=identifierValue(id);if(candidate.isEmpty())continue;
            if(!value.isEmpty()&&!value.equals(candidate))throw new SQLException("Ada lebih dari satu identitas aktif untuk sistem "+system+". Periksa data pasien rujukan.");
            value=candidate;
        }return value;
    }
    private static String identifierBySystemHint(JsonNode patient,String... hints)throws SQLException{
        String value="";
        for(JsonNode id:patient.path("identifier")){
            if("old".equals(text(id,"use")))continue;
            String system=text(id,"system").toLowerCase(Locale.ENGLISH),candidate=identifierValue(id);
            if(candidate.isEmpty())continue;
            boolean match=false;for(String hint:hints)if(!safe(hint).isEmpty()&&system.contains(hint.toLowerCase(Locale.ENGLISH))){match=true;break;}
            if(!match)continue;
            if(!value.isEmpty()&&!value.equals(candidate))throw new SQLException("Ada lebih dari satu identitas aktif yang cocok pada Patient. Periksa data pasien rujukan.");
            value=candidate;
        }
        return value;
    }
    private static String identifierValue(JsonNode identifier){
        String v=usable(text(identifier,"value"));
        return "old".equals(text(identifier,"use"))||v.indexOf('*')>=0||v.indexOf('#')>=0?"":v;
    }
    private static JsonNode preferred(JsonNode list,String use){
        JsonNode first=null;
        for(JsonNode n:list){if("old".equals(text(n,"use")))continue;if(first==null)first=n;if(!use.isEmpty()&&use.equals(text(n,"use")))return n;}
        return first;
    }
    private static String name(JsonNode n){
        if(n==null)return "";
        return first(usable(text(n,"text")),usable(joinWords(join(n.path("prefix")," "),join(n.path("given")," "),text(n,"family"),join(n.path("suffix")," "))));
    }
    private static String telecom(JsonNode p,String system){
        String result="";
        for(JsonNode t:p.path("telecom"))if(system.equals(text(t,"system"))&&!"old".equals(text(t,"use"))){String v=usable(text(t,"value"));if(v.isEmpty())continue;if(result.isEmpty())result=v;if("mobile".equals(text(t,"use")))return v;}
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
    private static boolean isNik(String value){return value.matches("[0-9]{16}")&&!"0000000000000000".equals(value);}
    private static String usable(String value){
        String v=safe(value);
        if(v.contains("**")||v.contains("##")||v.matches("(?i)(-|—|unknown|null|redacted|masked|x{3,})"))return "";
        return v;
    }
    private static void put(Map<String,String> m,String key,String v){String clean=usable(v);if(!clean.isEmpty())m.put(key,clean);}
    private static void note(Map<String,String> m,String v){m.put("catatan",joinWords(value(m,"catatan"),v));}
    private static String text(JsonNode n,String key){if(n==null)return "";JsonNode v=n.path(key);return v.isMissingNode()||v.isNull()?"":safe(v.asText());}
    private static String first(String... values){for(String v:values)if(!safe(v).isEmpty())return safe(v);return "";}
    private static String joinWords(String... values){StringBuilder b=new StringBuilder();for(String v:values)if(!safe(v).isEmpty()){if(b.length()>0)b.append(' ');b.append(safe(v));}return b.toString();}
    private static String join(JsonNode list,String separator){StringBuilder b=new StringBuilder();for(JsonNode n:list){String v=n.isNull()?"":safe(n.asText());if(v.isEmpty())continue;if(b.length()>0)b.append(separator);b.append(v);}return b.toString();}
    private static String safe(String s){return s==null?"":s.trim();}
}
