package fungsi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/** Referensi JDBC stabil; sambungan fisik dapat dilepas ketika sesi idle. */
public final class IdleDatabaseSession {
    @FunctionalInterface public interface Factory { Connection open() throws SQLException; }
    private final Factory factory;
    private final Object mutex = new Object();
    private final Map<StatementHandle, Boolean> statements = new WeakHashMap<>();
    private final Map<String, Call> settings = new LinkedHashMap<>();
    private final ExecutorService closer = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SIMRS-idle-db-close"); t.setDaemon(true); return t;
    });
    private final Connection facade;
    private Connection physical;
    private volatile boolean suspended;
    private final AtomicLong epoch = new AtomicLong();
    private boolean closed;
    private Thread transactionOwner;
    private final java.util.Set<Thread> transactionParticipants = new java.util.HashSet<>();
    private int readers, independentWork;
    private long opened, released, blocked;

    public IdleDatabaseSession(Factory factory) {
        this.factory = factory;
        facade = proxy(Connection.class, this::connectionCall);
    }

    public Connection connection() { return facade; }
    public boolean isSuspended() { return suspended; }
    public long epoch() { return epoch.get(); }

    /** Admission terjadi sebelum lock; pekerjaan ini selesai melalui close caller. */
    public Connection openWorkConnection() throws SQLException {
        synchronized (mutex) {
            permit();
            Connection target=factory.open();
            independentWork++;
            boolean[] done={false};
            return proxy(Connection.class,(self,method,args) -> {
                if(method.getDeclaringClass()==Object.class) return objectCall(self,method.getName(),args);
                if(method.getName().equals("unwrap")) return unwrap(self,args);
                if(method.getName().equals("isWrapperFor")) return ((Class<?>)args[0]).isInstance(self);
                if(method.getName().equals("close")) {
                    synchronized(mutex) {
                        if(!done[0]) try { target.close(); }
                        finally { done[0]=true; independentWork--; }
                    }
                    return null;
                }
                return invoke(method,target,args);
            });
        }
    }

    /** Flag segera aktif; tidak menunggu JDBC di event thread Swing. */
    public void suspend() {
        suspended = true;
        epoch.incrementAndGet();
        closer.execute(() -> { synchronized (mutex) { releaseIfIdle(); } });
    }

    /** Tidak membuka socket di EDT. Query berikutnya menyambung secara lazy. */
    public void resume() { suspended = false; epoch.incrementAndGet(); }

    public String diagnostics() {
        synchronized (mutex) {
            return "locked=" + suspended + ", connected=" + (physical != null)
                    + ", resultsets=" + readers + ", transaction=" + (transactionOwner != null)
                    + ", independentWork=" + independentWork
                    + ", opened=" + opened + ", released=" + released + ", blocked=" + blocked;
        }
    }

    private void permit() throws SQLException {
        if (closed) throw new SQLException("Koneksi ditutup", "08003");
        if (suspended && transactionOwner != Thread.currentThread()
                && !transactionParticipants.contains(Thread.currentThread())) {
            blocked++;
            throw new SQLException("Sesi terkunci; query pekerjaan dijeda", "57014");
        }
        // Pertahankan pola thread aplikasi lama sebelum lock. Thread yang sudah
        // ikut transaksi boleh menyelesaikannya; thread baru tidak boleh masuk.
        if (!suspended && transactionOwner != null) transactionParticipants.add(Thread.currentThread());
    }

    private Connection physical() throws Throwable {
        permit();
        if (physical == null) {
            Connection fresh = factory.open();
            try {
                for (Call call : settings.values()) call.invoke(fresh);
                physical = fresh; opened++;
            } catch (Throwable e) { fresh.close(); throw e; }
        }
        return physical;
    }

    private void releaseIfIdle() {
        if (!suspended || transactionOwner != null || readers != 0 || physical == null) return;
        for (StatementHandle s : new ArrayList<>(statements.keySet())) s.detach();
        try { physical.close(); released++; }
        catch (SQLException e) { System.out.println("Timeout DB close: " + e.getSQLState()); }
        finally { physical = null; }
    }

    private Object connectionCall(Object self, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (method.getDeclaringClass() == Object.class) return objectCall(self, name, args);
        synchronized (mutex) {
            if (name.equals("isClosed")) return closed;
            if (name.equals("isWrapperFor")) return ((Class<?>) args[0]).isInstance(self);
            if (name.equals("unwrap")) return unwrap(self, args);
            if (name.equals("close")) {
                // Close eksplisit berbeda dari idle: tetap mengikuti kontrak JDBC.
                if (!closed) {
                    for (StatementHandle s : new ArrayList<>(statements.keySet())) s.close();
                    if (physical != null) physical.close();
                    physical = null; closed = true;
                }
                return null;
            }
            permit();
            if (name.equals("prepareStatement") || name.equals("prepareCall") || name.equals("createStatement")) {
                StatementHandle h = new StatementHandle(new Call(method, args));
                statements.put(h, Boolean.TRUE);
                return h.facade;
            }
            if (name.equals("getMetaData")) {
                return proxy(DatabaseMetaData.class, (p, m, a) -> {
                    synchronized (mutex) {
                        if (m.getDeclaringClass() == Object.class) return objectCall(p,m.getName(),a);
                        if (m.getName().equals("getConnection")) return facade;
                        if (m.getName().equals("unwrap")) return unwrap(p,a);
                        if (m.getName().equals("isWrapperFor")) return ((Class<?>) a[0]).isInstance(p);
                        Object value = invoke(m, physical().getMetaData(), a);
                        return value instanceof ResultSet ? result(null, (ResultSet) value) : value;
                    }
                });
            }
            Object value = invoke(method, physical(), args);
            if (name.equals("setAutoCommit")) {
                transactionOwner = Boolean.FALSE.equals(args[0]) ? Thread.currentThread() : null;
                if (transactionOwner == null) transactionParticipants.clear();
            } else if (name.startsWith("set") && !name.equals("setSavepoint")) {
                settings.put(name, new Call(method, args));
            }
            releaseIfIdle();
            return value;
        }
    }

    private final class StatementHandle implements InvocationHandler {
        private final Call creation;
        private final Statement facade;
        private final Map<String, Call> state = new LinkedHashMap<>();
        private final ArrayList<Call> batch = new ArrayList<>();
        private final IdentityHashMap<ResultSet, ResultHandle> results = new IdentityHashMap<>();
        private Statement target;
        private boolean closed, executedStream;
        StatementHandle(Call creation) {
            this.creation = creation;
            facade = (Statement) Proxy.newProxyInstance(IdleDatabaseSession.class.getClassLoader(),
                    new Class<?>[]{creation.method.getReturnType()}, this);
        }
        private Statement target() throws Throwable {
            permit();
            if (target == null) {
                if (executedStream) throw new SQLException("Atur ulang parameter stream sesudah reconnect", "HY010");
                target = (Statement) creation.invoke(physical());
                try {
                    for (Call call : batch) call.invoke(target);
                    for (Call call : state.values()) call.invoke(target);
                } catch (Throwable e) { target.close(); target = null; throw e; }
            }
            return target;
        }
        public Object invoke(Object self, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (method.getDeclaringClass() == Object.class) return objectCall(self,name,args);
            synchronized (mutex) {
                if (name.equals("close")) { close(); releaseIfIdle(); return null; }
                if (name.equals("isClosed")) return closed;
                if (name.equals("getConnection")) return IdleDatabaseSession.this.facade;
                if (name.equals("unwrap")) return unwrap(self,args);
                if (name.equals("isWrapperFor")) return ((Class<?>) args[0]).isInstance(self);
                if (closed) throw new SQLException("Statement ditutup", "HY010");
                permit();
                if (name.equals("clearParameters")) {
                    if(target!=null) IdleDatabaseSession.invoke(method,target,args);
                    state.entrySet().removeIf(e -> e.getKey().startsWith("parameter:"));
                    executedStream=false;
                    return null;
                }
                if (name.startsWith("execute")) {
                    closeResults();
                    Object value = IdleDatabaseSession.invoke(method,target(),args);
                    if (state.values().stream().anyMatch(c -> c.stream)) executedStream = true;
                    if (name.contains("Batch")) batch.clear();
                    if (value instanceof ResultSet) return result(this,(ResultSet)value);
                    // execute() bisa menyimpan ResultSet yang baru diminta kemudian.
                    if (Boolean.TRUE.equals(value)) result(this,target.getResultSet());
                    releaseIfIdle();
                    return value;
                }
                Object value = IdleDatabaseSession.invoke(method,target(),args);
                if (name.equals("clearBatch")) batch.clear();
                else if (name.equals("addBatch")) {
                    if (facade instanceof PreparedStatement)
                        for (Map.Entry<String,Call> e : state.entrySet())
                            if (e.getKey().startsWith("parameter:")) batch.add(e.getValue());
                    batch.add(new Call(method,args));
                }
                else if (name.startsWith("set") || name.startsWith("registerOutParameter")) {
                    boolean parameter = (facade instanceof PreparedStatement) && args != null
                            && args.length >= 2 && (args[0] instanceof Integer || args[0] instanceof String);
                    state.put(parameter ? "parameter:" + (name.startsWith("register") ? "out:" : "") + args[0] : name, new Call(method,args));
                }
                if (value instanceof ResultSet) return result(this,(ResultSet)value);
                return value;
            }
        }
        private void closeResults() throws SQLException {
            for (ResultHandle r : new ArrayList<>(results.values())) r.close();
        }
        private void close() throws SQLException {
            if (closed) return;
            closeResults();
            if (target != null) target.close();
            target = null; closed = true; state.clear(); batch.clear(); statements.remove(this);
        }
        private void detach() {
            if (target != null) try { target.close(); } catch (SQLException ignored) { }
            target = null;
        }
    }

    private ResultSet result(StatementHandle owner, ResultSet rs) {
        if (rs == null) return null;
        if (owner != null && owner.results.containsKey(rs)) return owner.results.get(rs).facade;
        ResultHandle h = new ResultHandle(owner,rs); readers++;
        if (owner != null) owner.results.put(rs,h);
        return h.facade;
    }
    private final class ResultHandle implements InvocationHandler {
        final StatementHandle owner; final ResultSet target, facade; boolean closed;
        ResultHandle(StatementHandle owner, ResultSet target) {
            this.owner=owner; this.target=target; facade=proxy(ResultSet.class,this);
        }
        public Object invoke(Object self, Method method, Object[] args) throws Throwable {
            String name=method.getName();
            if (method.getDeclaringClass()==Object.class) return objectCall(self,name,args);
            synchronized (mutex) {
                if (name.equals("close")) { close(); releaseIfIdle(); return null; }
                if (name.equals("isClosed")) return closed;
                if (name.equals("getStatement")) return owner == null ? null : owner.facade;
                if (name.equals("unwrap")) return unwrap(self,args);
                if (name.equals("isWrapperFor")) return ((Class<?>)args[0]).isInstance(self);
                if (closed) throw new SQLException("ResultSet ditutup", "HY010");
                // Hasil query/transaksi yang sudah berjalan boleh diselesaikan.
                return IdleDatabaseSession.invoke(method,target,args);
            }
        }
        void close() throws SQLException {
            if (closed) return;
            try { target.close(); }
            finally { closed=true; readers--; if(owner!=null) owner.results.remove(target); }
        }
    }
    private static final class Call {
        final Method method; final Object[] args; final boolean stream;
        Call(Method method,Object[] args) {
            this.method=method; this.args=args==null ? null : args.clone();
            stream=method.getName().contains("Stream") || method.getName().contains("Reader")
                    || (args!=null && java.util.Arrays.stream(args).anyMatch(v -> v instanceof java.io.InputStream || v instanceof java.io.Reader));
            if(this.args!=null) for(int i=0;i<this.args.length;i++)
                if(this.args[i] instanceof byte[]) this.args[i]=((byte[])this.args[i]).clone();
                else if(this.args[i] instanceof java.util.Date) this.args[i]=((java.util.Date)this.args[i]).clone();
                else if(this.args[i] instanceof java.util.Calendar) this.args[i]=((java.util.Calendar)this.args[i]).clone();
        }
        Object invoke(Object target) throws Throwable { return IdleDatabaseSession.invoke(method,target,args); }
    }
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type,InvocationHandler handler) {
        return (T)Proxy.newProxyInstance(IdleDatabaseSession.class.getClassLoader(),new Class<?>[]{type},handler);
    }
    private static Object invoke(Method method,Object target,Object[] args) throws Throwable {
        try { return method.invoke(target,args); }
        catch(InvocationTargetException e) { throw e.getCause(); }
    }
    private static Object unwrap(Object self,Object[] args) throws SQLException {
        if(((Class<?>)args[0]).isInstance(self)) return self;
        throw new SQLException("Unwrap koneksi fisik tidak didukung oleh pengelola sesi", "0A000");
    }
    private static Object objectCall(Object self,String name,Object[] args) {
        if(name.equals("equals")) return self==args[0];
        if(name.equals("hashCode")) return System.identityHashCode(self);
        return "SIMRS managed JDBC";
    }
}
