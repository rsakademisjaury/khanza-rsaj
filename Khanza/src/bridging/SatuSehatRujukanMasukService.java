package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/** Satu pemuat inbox untuk badge dan form. Seluruh callback UI berlangsung pada EDT. */
public final class SatuSehatRujukanMasukService {
    public static final int DEFAULT_POLL_SECONDS=60;
    public interface Listener { void changed(Snapshot snapshot,String error,List<Row> fresh,boolean first); }
    public static final class Row {
        public final JsonNode task;
        public final String id,patient,referrer,kind,status,decision,time;
        Row(JsonNode task,String patient,String referrer,String kind){
            this.task=task;this.id=task.path("id").asText();this.patient=patient;this.referrer=referrer;this.kind=kind;
            status=task.path("status").asText();decision=SatuSehatRujukanIGDRanapApi.parseDecision(task);
            // Jackson lama belum menyediakan asText(String defaultValue).
            JsonNode authoredOn=task.path("authoredOn");
            time=authoredOn.isMissingNode()||authoredOn.isNull()
                    ?task.path("meta").path("lastUpdated").asText()
                    :authoredOn.asText();
        }
        public boolean pending(){return SatuSehatRujukanIGDRanapApi.incomingPending(task);}
        public boolean accepted(){return "accepted".equals(decision);}
    }
    public static final class Snapshot {
        public final List<Row> rows;public final int pending;public final long time;
        Snapshot(List<Row> rows){this.rows=Collections.unmodifiableList(new ArrayList<Row>(rows));int n=0;for(Row r:rows)if(r.pending())n++;pending=n;time=System.currentTimeMillis();}
    }
    private final List<Listener> listeners=new ArrayList<Listener>();
    private final Set<String> seen=new HashSet<String>();
    private final Map<String,JsonNode> cache=new LinkedHashMap<String,JsonNode>();
    private Timer timer;private SwingWorker<Snapshot,Void> worker;
    private boolean active,again,loaded;private long generation,revision;
    private Snapshot snapshot;private String lastError="";private String operator="";

    public static int pollSeconds(){
        try{return Math.max(30,Math.min(3600,Integer.parseInt(System.getProperty("rsaj.rujukan.masuk.poll.seconds","60"))));}
        catch(RuntimeException ex){return DEFAULT_POLL_SECONDS;}
    }
    public String operator(){return operator;}
    public Snapshot snapshot(){return snapshot;}
    public boolean loading(){return worker!=null;}
    public void addListener(Listener l){listeners.add(l);if(snapshot!=null||!lastError.isEmpty())l.changed(snapshot,lastError,Collections.<Row>emptyList(),false);}
    public void removeListener(Listener l){listeners.remove(l);}
    public void start(String operator){
        stop();this.operator=operator==null?"":operator;active=true;
        timer=new Timer(pollSeconds()*1000,e->refresh());timer.setInitialDelay(1500);timer.setCoalesce(true);timer.start();
    }
    public void stop(){
        generation++;active=false;again=false;loaded=false;operator="";snapshot=null;lastError="";seen.clear();
        synchronized(cache){cache.clear();}
        if(timer!=null){timer.stop();timer=null;}if(worker!=null){worker.cancel(true);worker=null;}
    }
    /** Setelah keputusan berubah, hasil polling lama tidak boleh mengembalikan tombol Terima/Tolak. */
    public void decisionChanged(){revision++;refresh();}
    public void refresh(){
        if(!SwingUtilities.isEventDispatchThread()){SwingUtilities.invokeLater(()->refresh());return;}
        if(!active)return;if(worker!=null){again=true;return;}
        final long run=generation,version=revision;
        final SwingWorker<Snapshot,Void> task=new SwingWorker<Snapshot,Void>(){
            @Override protected Snapshot doInBackground()throws Exception{return load();}
            @Override protected void done(){
                if(run!=generation||worker!=this)return;worker=null;
                if(version!=revision){again=false;refresh();return;}
                try{
                    Snapshot next=get();boolean first=!loaded;List<Row> fresh=new ArrayList<Row>();
                    for(Row r:next.rows){if(r.pending()&&!seen.contains(r.id))fresh.add(r);seen.add(r.id);}
                    snapshot=next;lastError="";loaded=true;notifyListeners(fresh,first);
                }catch(Exception ex){
                    // Jangan mengubah jumlah menjadi nol saat koneksi/API gagal.
                    lastError="Pemeriksaan rujukan belum berhasil. Jumlah terakhir tetap ditampilkan; coba Perbarui.";
                    notifyListeners(Collections.<Row>emptyList(),false);
                }
                if(again){again=false;refresh();}
            }
        };worker=task;task.execute();
    }
    private void notifyListeners(List<Row> fresh,boolean first){for(Listener l:new ArrayList<Listener>(listeners))l.changed(snapshot,lastError,fresh,first);}
    private Snapshot load()throws Exception{
        SatuSehatRujukanIGDRanapApi api=new SatuSehatRujukanIGDRanapApi();
        String org=api.organizationId();JsonNode bundle=api.cariRujukanMasuk();Map<String,JsonNode> included=new HashMap<String,JsonNode>();
        for(JsonNode entry:bundle.path("entry")){JsonNode r=entry.path("resource");included.put(r.path("resourceType").asText()+"/"+r.path("id").asText(),r);}
        List<Row> rows=new ArrayList<Row>();
        for(JsonNode entry:bundle.path("entry")){
            if(Thread.currentThread().isInterrupted())throw new InterruptedException();
            JsonNode t=entry.path("resource");if(!isIncoming(t,org))continue;
            JsonNode cp=null;for(JsonNode ref:t.path("basedOn")){String key=reference(ref);if(key.startsWith("CarePlan/")){cp=resource(api,key,included,true);break;}}
            String kind=referralKind(t,cp,null);JsonNode encounter=null;
            if(kind.isEmpty()){
                String key=reference(t.path("encounter"));if(key.isEmpty()&&cp!=null)key=reference(cp.path("encounter"));
                encounter=resource(api,key,included,false);kind=referralKind(t,cp,encounter);
            }
            String patientRef=reference(t.path("for"));if(patientRef.isEmpty()&&cp!=null)patientRef=reference(cp.path("subject"));
            String patient=t.path("for").path("display").asText();
            if(patient.isEmpty())patient=resourceName(resource(api,patientRef,included,false),patientRef);
            String orgRef=reference(t.path("requester"));
            if(!orgRef.startsWith("Organization/")&&cp!=null)for(JsonNode contributor:cp.path("contributor")){
                String key=reference(contributor);if(key.startsWith("Organization/")){orgRef=key;break;}
            }
            String referrer=t.path("requester").path("display").asText();
            if(orgRef.startsWith("Organization/"))referrer=resourceName(resource(api,orgRef,included,false),referrer.isEmpty()?orgRef:referrer);
            rows.add(new Row(t,empty(patient,"Pasien belum teridentifikasi"),empty(referrer,"Fasyankes belum teridentifikasi"),empty(kind,"Belum diketahui")));
        }
        Collections.sort(rows,(a,b)->{if(a.pending()!=b.pending())return a.pending()?-1:1;return b.time.compareTo(a.time);});
        return new Snapshot(rows);
    }
    private JsonNode resource(SatuSehatRujukanIGDRanapApi api,String key,Map<String,JsonNode> included,boolean mutable){
        if(key==null||key.isEmpty())return null;JsonNode value=included.get(key);if(value!=null)return value;
        if(!mutable)synchronized(cache){value=cache.get(key);if(value!=null)return value;}
        try{value=api.incomingReference(key);if(value!=null&&!mutable)synchronized(cache){if(cache.size()>1500)cache.clear();cache.put(key,value);}return value;}
        catch(Exception ex){return null;}
    }
    static boolean isIncoming(JsonNode task,String org){
        if(!"Task".equals(task.path("resourceType").asText())||task.path("id").asText().isEmpty())return false;
        if(!("Organization/"+org).equals(reference(task.path("owner"))))return false;
        for(JsonNode coding:task.path("code").path("coding"))if("referral-approval-request".equals(coding.path("code").asText()))return true;
        return false;
    }
    static String referralKind(JsonNode task,JsonNode carePlan,JsonNode encounter){
        String codes=careCode(task);if(codes.isEmpty())codes=careCode(carePlan);if(!codes.isEmpty())return codes;
        if(encounter!=null){String c=encounter.path("class").path("code").asText();if("EMER".equals(c))return "IGD";if("IMP".equals(c))return "RANAP";
            // AMB adalah kunjungan asal, bukan bukti jenis pelayanan yang diminta.
            // Jangan menyembunyikan permintaan IGD/ranap hanya karena perujuk masih rawat jalan.
        }return "";
    }
    private static String careCode(JsonNode node){
        if(node==null)return "";
        if(node.isObject()){
            String code=node.path("code").isValueNode()?node.path("code").asText():"";
            // Kode kategori CarePlan mengikuti payload pengirim pada source modul ini.
            if("TK000068".equals(code)||SatuSehatRujukanIGDRanapApi.IGD_CARE_CODE.equals(code))return "IGD";
            if("736353004".equals(code)||SatuSehatRujukanIGDRanapApi.RANAP_CARE_CODE.equals(code))return "RANAP";
        }
        for(JsonNode child:node){String found=careCode(child);if(!found.isEmpty())return found;}return "";
    }
    static String reference(JsonNode ref){
        if(ref==null)return "";String s=ref.path("reference").asText();
        if(s.startsWith("http://")||s.startsWith("https://"))try{s=new java.net.URI(s).getPath();}catch(Exception ex){return "";}
        String[] parts=s.split("/");for(int i=0;i+1<parts.length;i++)if("Organization".equals(parts[i])||"Patient".equals(parts[i])||"CarePlan".equals(parts[i])||"Encounter".equals(parts[i]))return parts[i]+"/"+parts[i+1];
        return s;
    }
    static String resourceName(JsonNode r,String fallback){
        if(r==null)return fallback;if("Organization".equals(r.path("resourceType").asText()))return empty(r.path("name").asText(),fallback);
        for(JsonNode name:r.path("name")){String text=name.path("text").asText();if(!text.isEmpty())return text;
            StringBuilder b=new StringBuilder();for(JsonNode g:name.path("given")){if(b.length()>0)b.append(' ');b.append(g.asText());}String family=name.path("family").asText();if(!family.isEmpty()){if(b.length()>0)b.append(' ');b.append(family);}if(b.length()>0)return b.toString();}
        return fallback;
    }
    private static String empty(String s,String fallback){return s==null||s.trim().isEmpty()?fallback:s;}
}
