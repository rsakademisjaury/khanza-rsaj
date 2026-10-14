package fungsi;

import java.sql.Connection;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.SwingWorker;

/** Polling lock berjalan di worker; satu pekerjaan per form, tanpa antrean. */
public final class SessionAlarmPoller {
    private SwingWorker<Boolean,Void> worker;
    private final BooleanSupplier showing;
    private final Runnable play;

    public SessionAlarmPoller(BooleanSupplier showing,Runnable play) {
        this.showing=showing; this.play=play;
    }
    public void check(List<SessionAlarmQuery> queries,List<String> predicates) {
        if(worker!=null || queries.isEmpty() || !showing.getAsBoolean() || !koneksiDB.sesiDatabaseTerkunci()) return;
        if(queries.size()!=predicates.size()) throw new IllegalArgumentException("Filter alarm tidak lengkap");
        final long epoch=koneksiDB.versiSesiDatabase();
        worker=new SwingWorker<Boolean,Void>() {
            protected Boolean doInBackground() throws Exception {
                try(Connection monitor=koneksiDB.condbPemantauan()) {
                    for(int i=0;i<queries.size();i++) {
                        if(isCancelled() || epoch!=koneksiDB.versiSesiDatabase()) return false;
                        if(queries.get(i).exists(monitor,predicates.get(i))) return true;
                    }
                    return false;
                }
            }
            protected void done() {
                if(worker!=this) return;
                worker=null;
                if(!showing.getAsBoolean() || !koneksiDB.sesiDatabaseTerkunci() || epoch!=koneksiDB.versiSesiDatabase()) return;
                try { if(get()) play.run(); }
                catch(Exception e) { System.out.println("Timeout: pemantauan alarm belum berhasil; akan dicoba pada interval berikutnya."); }
            }
        };
        worker.execute();
    }
}
