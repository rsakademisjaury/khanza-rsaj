package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.*;
import java.sql.*;
import java.util.UUID;
import java.util.function.Consumer;
import javax.swing.*;
import simrskhanza.DlgIGD;

/** Pengaitan rujukan ke registrasi asli DlgIGD. Tidak menyisipkan reg_periksa sendiri. */
public final class SatuSehatRujukanMasukRegistrasi {
    public static final String TABLE="satusehat_rujukan_masuk_registrasi";
    private static final java.util.Map<Component,DlgIGD> OPEN=new java.util.WeakHashMap<Component,DlgIGD>();
    private SatuSehatRujukanMasukRegistrasi(){}
    public static final class Context {
        public String taskId="",carePlanId="",serviceRequestId="",nationalNumber="",patientIhs="",nik="",name="",birth="",gender="",contact="",address="",insurance="",referrer="",operator="",noRm="",claimToken="";
    }
    public static final class State {
        public String status="",noRawat="",noRm="";public boolean visitExists;
        public boolean registered(){return "TERDAFTAR".equals(status)&&visitExists;}
        public boolean available(){return status.isEmpty();}
        public String message(){if(registered())return "Sudah terdaftar di IGD • No. Rawat "+noRawat+" • No. RM "+noRm;
            if(status.isEmpty())return allowed()?"Belum didaftarkan. Dokter dan penjamin diverifikasi pada registrasi IGD.":"Akun ini tidak memiliki hak pendaftaran IGD.";
            return "Pendaftaran sedang diproses / perlu diperiksa petugas. "+(noRawat.isEmpty()?"":"No. Rawat "+noRawat+". ")+"Tidak membuat kunjungan baru untuk Task yang sama.";}
    }
    public static boolean allowed(){return akses.getjml1()>=1||akses.getigd();}
    public static void closeFor(Component parent){DlgIGD dialog=OPEN.remove(parent);if(dialog!=null)closeDialog(dialog);}
    /** Periksa kontrak pada class yang benar-benar dimuat, sebelum membuat window. */
    static java.lang.reflect.Method registrationHook(Class<?> type)throws SQLException{
        try{
            java.lang.reflect.Method hook=type.getMethod("konfigurasiRujukanMasukSatuSehat",Context.class);
            if(hook.getReturnType()!=Void.TYPE||java.lang.reflect.Modifier.isStatic(hook.getModifiers()))throw new NoSuchMethodException("Kontrak registrasi tidak cocok");
            return hook;
        }catch(NoSuchMethodException ex){throw incompatibleRegistration(ex);}
        catch(LinkageError ex){throw incompatibleRegistration(ex);}
    }
    static void configureRegistration(java.lang.reflect.Method hook,Object dialog,Context context)throws Exception{
        try{hook.invoke(dialog,context);}
        catch(java.lang.reflect.InvocationTargetException ex){
            Throwable cause=ex.getCause();
            if(cause instanceof LinkageError)throw incompatibleRegistration(cause);
            if(cause instanceof Exception)throw (Exception)cause;
            if(cause instanceof Error)throw (Error)cause;
            throw ex;
        }catch(LinkageError ex){throw incompatibleRegistration(ex);}
    }
    private static SQLException incompatibleRegistration(Throwable cause){
        return new SQLException("Modul DlgIGD dan Rujukan Masuk belum satu versi. Lakukan Clean and Build, lalu jalankan ulang aplikasi dari hasil build yang sama.",cause);
    }
    private static void closeDialog(DlgIGD dialog){
        try{
            try{dialog.getClass().getMethod("tutupRujukanMasukSatuSehat").invoke(dialog);}
            catch(NoSuchMethodException ex){dialog.getButton().doClick();}
        }catch(Exception ex){System.err.println("Penutupan registrasi rujukan: "+ex);}
        catch(LinkageError ex){System.err.println("Versi class registrasi rujukan: "+ex);}
        finally{dialog.dispose();}
    }
    private static Connection connection()throws SQLException{Connection c=koneksiDB.condb();if(c==null||c.isClosed())throw new SQLException("Koneksi database belum tersedia.");return c;}
    private static PreparedStatement prepare(Connection c,String sql)throws SQLException{PreparedStatement p=c.prepareStatement(sql);try{p.setQueryTimeout(15);}catch(SQLException ex){}return p;}
    public static State state(String taskId)throws SQLException{
        Connection c=connection();synchronized(c){return state(c,taskId);}
    }
    private static State state(Connection c,String taskId)throws SQLException{
        State s=new State();try(PreparedStatement p=prepare(c,"SELECT m.status,m.no_rawat,m.no_rkm_medis,r.no_rawat AS existing_visit FROM "+TABLE+" m LEFT JOIN reg_periksa r ON r.no_rawat=m.no_rawat AND r.no_rkm_medis=m.no_rkm_medis AND r.kd_poli='IGDK' WHERE m.task_id=?")){
            p.setString(1,taskId);try(ResultSet r=p.executeQuery()){if(r.next()){s.status=safe(r.getString(1));s.noRawat=safe(r.getString(2));s.noRm=safe(r.getString(3));s.visitExists=r.getString(4)!=null;}}}return s;
    }
    public static void open(final Component parent,final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail,final String operator,final Runnable finished,final Consumer<String> failed){
        if(!allowed()){failed.accept("Akun ini tidak memiliki hak pendaftaran IGD.");return;}
        new SwingWorker<Context,Void>(){
            @Override protected Context doInBackground()throws Exception{
                SatuSehatRujukanIGDRanapApi api=new SatuSehatRujukanIGDRanapApi();JsonNode task=api.cekTask(detail.taskId);api.validateIncomingTask(task);
                if(!"accepted".equals(SatuSehatRujukanIGDRanapApi.parseDecision(task)))throw new SQLException("Rujukan belum diterima atau status telah berubah.");
                State existing=state(detail.taskId);if(!existing.available())throw new SQLException(existing.message());
                Context ctx=context(detail,operator);
                String patientRef=SatuSehatRujukanMasukService.reference(task.path("for"));
                if(!patientRef.isEmpty()&&!ctx.patientIhs.isEmpty()&&!patientRef.equals("Patient/"+ctx.patientIhs))throw new SQLException("Identitas pasien pada Task telah berubah. Muat ulang detail rujukan.");
                Connection c=connection();synchronized(c){if(!ctx.nik.isEmpty())try(PreparedStatement p=prepare(c,"SELECT no_rkm_medis FROM pasien WHERE no_ktp=? LIMIT 2")){
                    p.setString(1,ctx.nik);try(ResultSet r=p.executeQuery()){if(r.next())ctx.noRm=r.getString(1);if(r.next())throw new SQLException("NIK pasien terhubung ke lebih dari satu nomor RM. Periksa master pasien sebelum mendaftarkan rujukan.");}}
                }return ctx;
            }
            @Override protected void done(){
                if(!parent.isShowing()){finished.run();return;}
                DlgIGD dialog=null;
                java.awt.event.WindowAdapter closedListener=null;
                boolean opened=false;
                try{Context ctx=get();if(!allowed())throw new SQLException("Hak pendaftaran IGD tidak tersedia.");
                    final java.lang.reflect.Method hook=registrationHook(DlgIGD.class);
                    Window w=SwingUtilities.getWindowAncestor(parent);while(w!=null&&!(w instanceof Frame))w=w.getOwner();
                    // DlgIGD beserta picker pasien/dokter asli memakai window modeless.
                    dialog=new DlgIGD(w instanceof Frame?(Frame)w:null,false);
                    dialog.isCek();dialog.emptTeks();configureRegistration(hook,dialog,ctx);
                    Window inbox=SwingUtilities.getWindowAncestor(parent);Component anchor=inbox instanceof RootPaneContainer?((RootPaneContainer)inbox).getContentPane():parent;
                    Point point=anchor.getLocationOnScreen();dialog.setBounds(point.x,point.y,anchor.getWidth(),anchor.getHeight());
                    final DlgIGD registeredDialog=dialog;
                    closedListener=new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){if(OPEN.get(parent)==registeredDialog)OPEN.remove(parent);finished.run();}};
                    OPEN.put(parent,dialog);dialog.addWindowListener(closedListener);
                    dialog.setVisible(true);opened=true;
                }catch(Exception ex){Throwable cause=ex instanceof java.util.concurrent.ExecutionException&&ex.getCause()!=null?ex.getCause():ex;failed.accept(safe(cause.getMessage()).isEmpty()?"Pendaftaran IGD belum dapat dibuka.":cause.getMessage());}
                catch(LinkageError ex){failed.accept(incompatibleRegistration(ex).getMessage());}
                finally{if(!opened&&dialog!=null){if(OPEN.get(parent)==dialog)OPEN.remove(parent);if(closedListener!=null)dialog.removeWindowListener(closedListener);closeDialog(dialog);}}
            }
        }.execute();
    }
    private static Context context(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d,String operator){
        Context c=new Context();c.taskId=d.taskId;c.carePlanId=d.carePlanId;c.serviceRequestId=d.serviceRequestId;c.nationalNumber=d.nationalReferralNumber;c.operator=safe(operator);
        JsonNode p=d.patient;c.name=SatuSehatRujukanMasukService.resourceName(p,"");
        if(p!=null){c.patientIhs=p.path("id").asText();c.birth=p.path("birthDate").asText();String sex=p.path("gender").asText();c.gender="male".equals(sex)?"L":"female".equals(sex)?"P":"";
            for(JsonNode id:p.path("identifier")){String system=id.path("system").asText().toLowerCase(java.util.Locale.ROOT);String value=id.path("value").asText();if(system.contains("nik")&&value.matches("[0-9]{16}"))c.nik=value;if(system.contains("bpjs"))c.insurance=value;}
            for(JsonNode contact:p.path("telecom"))if("phone".equals(contact.path("system").asText())){c.contact=contact.path("value").asText();break;}
            for(JsonNode address:p.path("address")){c.address=address.path("text").asText();if(c.address.isEmpty()){StringBuilder b=new StringBuilder();for(JsonNode line:address.path("line")){if(b.length()>0)b.append(", ");b.append(line.asText());}c.address=b.toString();}if(!c.address.isEmpty())break;}
        }
        c.referrer=SatuSehatRujukanMasukService.resourceName(d.referrerOrganization,d.task.path("requester").path("display").asText());return c;
    }
    /** Reservasi atomik per Task, bertahan bila hasil simpan tidak pasti. Tidak mengambil alih transaksi induk. */
    public static void claim(Context ctx,String noRm)throws SQLException{
        if(!allowed())throw new SQLException("Akun ini tidak memiliki hak pendaftaran IGD.");
        if(ctx==null||safe(ctx.taskId).isEmpty()||safe(noRm).isEmpty())throw new SQLException("Task rujukan dan nomor RM wajib tersedia.");
        String activeOperator=safe(akses.getkode());if(activeOperator.isEmpty()&&akses.getjml1()>=1)activeOperator="Admin Utama";
        if(!activeOperator.equals(ctx.operator))throw new SQLException("Sesi operator berubah. Buka kembali rujukan dari akun yang sedang aktif.");
        Connection c=connection();synchronized(c){
            if(!c.getAutoCommit())throw new SQLException("Ada transaksi database lain yang sedang berjalan. Selesaikan transaksi tersebut sebelum mendaftarkan pasien.");
            State old=state(c,ctx.taskId);if(!old.available())throw new SQLException(old.message());
            try(PreparedStatement p=prepare(c,"SELECT nm_pasien,no_ktp,tgl_lahir,jk FROM pasien WHERE no_rkm_medis=?")){
                p.setString(1,noRm);try(ResultSet r=p.executeQuery()){
                    if(!r.next())throw new SQLException("Nomor RM tidak ditemukan di master pasien.");
                    String nik=safe(r.getString(2));if(!ctx.nik.isEmpty()&&!ctx.nik.equals(nik))throw new SQLException("NIK pasien yang dipilih berbeda dengan NIK pasien rujukan.");
                    if(ctx.nik.isEmpty()&&JOptionPane.showConfirmDialog(null,"NIK rujukan belum tersedia. Pastikan identitas berikut pasien yang sama:\nRujukan: "+ctx.name+" • "+ctx.birth+"\nPasien lokal: "+r.getString(1)+" • "+r.getString(3)+" • RM "+noRm,"Verifikasi identitas pasien",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
                        throw new SQLException("Pendaftaran dibatalkan untuk verifikasi identitas pasien.");
                }
            }
            try(PreparedStatement p=prepare(c,"SELECT no_rawat FROM reg_periksa WHERE no_rkm_medis=? AND kd_poli='IGDK' AND stts<>'Batal' AND status_bayar='Belum Bayar' LIMIT 1")){
                p.setString(1,noRm);try(ResultSet r=p.executeQuery()){if(r.next())throw new SQLException("Pasien sudah memiliki kunjungan IGD aktif: "+r.getString(1)+". Periksa kunjungan tersebut sebelum membuat pendaftaran baru.");}
            }
            String token=UUID.randomUUID().toString();
            try(PreparedStatement p=prepare(c,"INSERT INTO "+TABLE+" (task_id,careplan_id,servicerequest_id,no_rujukan_nasional,patient_ihs,no_rkm_medis,operator,status,claim_token) VALUES (?,?,?,?,?,?,?,'DIPROSES',?)")){
                String[] values={ctx.taskId,ctx.carePlanId,ctx.serviceRequestId,ctx.nationalNumber,ctx.patientIhs,noRm,ctx.operator,token};for(int i=0;i<values.length;i++)p.setString(i+1,safe(values[i]));p.executeUpdate();ctx.claimToken=token;ctx.noRm=noRm;
            }catch(SQLException ex){if("23000".equals(ex.getSQLState()))throw new SQLException("Rujukan sedang/sudah didaftarkan oleh petugas lain. Perbarui status pendaftaran.");throw ex;}
        }
    }
    /** Dipanggil hanya saat validasi selesai tanpa memulai INSERT reg_periksa. */
    public static void releaseBeforeInsert(Context ctx)throws SQLException{mutate(ctx,"DELETE FROM "+TABLE+" WHERE task_id=? AND claim_token=? AND status='DIPROSES'");}
    public static void uncertain(Context ctx)throws SQLException{mutate(ctx,"UPDATE "+TABLE+" SET status='PERLU_CEK' WHERE task_id=? AND claim_token=? AND status='DIPROSES'");}
    private static void mutate(Context ctx,String sql)throws SQLException{
        if(ctx==null||ctx.claimToken.isEmpty())return;Connection c=connection();synchronized(c){try(PreparedStatement p=prepare(c,sql)){p.setString(1,ctx.taskId);p.setString(2,ctx.claimToken);p.executeUpdate();}}
    }
    public static void saved(Context ctx,String noRawat)throws SQLException{
        Connection c=connection();synchronized(c){
            try(PreparedStatement p=prepare(c,"SELECT no_rawat FROM reg_periksa WHERE no_rawat=? AND no_rkm_medis=? AND kd_poli='IGDK'")){
                p.setString(1,noRawat);p.setString(2,ctx.noRm);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Hasil registrasi IGD belum dapat diverifikasi.");}
            }
            try(PreparedStatement p=prepare(c,"UPDATE "+TABLE+" SET no_rawat=?,status='TERDAFTAR' WHERE task_id=? AND claim_token=? AND status='DIPROSES'")){
                p.setString(1,noRawat);p.setString(2,ctx.taskId);p.setString(3,ctx.claimToken);if(p.executeUpdate()!=1)throw new SQLException("Kunjungan sudah tersimpan, tetapi pengaitan Task memerlukan pemeriksaan petugas.");
            }
        }
    }
    private static String safe(String s){return s==null?"":s.trim();}
}
