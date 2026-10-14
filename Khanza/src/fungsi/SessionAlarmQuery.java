package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Snapshot filter form di EDT; hanya SELECT 1 LIMIT 1 di worker alarm. */
public final class SessionAlarmQuery {
    private final String sql;
    private final Map<Integer,String> parameters = new LinkedHashMap<>();

    public SessionAlarmQuery(String originalSelect) { sql = originalSelect; }
    public void setString(int index,String value) { parameters.put(index,value); }

    public boolean exists(Connection connection,String pendingPredicate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(existenceSql(sql,pendingPredicate))) {
            statement.setQueryTimeout(5);
            for (Map.Entry<Integer,String> p : parameters.entrySet()) statement.setString(p.getKey(),p.getValue());
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }

    /** Potong projection dan sort saja; join/filter/parameter asli dipertahankan. */
    static String existenceSql(String original,String predicate) throws SQLException {
        int from=clause(original,"from",0);
        int where=clause(original,"where",from);
        if(from<0 || where<0) throw new SQLException("Query alarm tidak didukung", "42000");
        int order=clause(original,"order by",where);
        int limit=clause(original,"limit",where);
        int end=original.length();
        if(order>=0) end=Math.min(end,order);
        if(limit>=0) end=Math.min(end,limit);
        int group=clause(original,"group by",where);
        int having=clause(original,"having",where);
        if(having>=0) throw new SQLException("HAVING alarm memerlukan query khusus", "42000");
        // Grouping untuk tampilan tidak diperlukan untuk uji keberadaan.
        if(group>=0) end=Math.min(end,group);
        return "SELECT 1 " + original.substring(from,where) + "WHERE ("+predicate+") AND ("
                + original.substring(where+5,end).trim() + ") LIMIT 1";
    }

    private static int clause(String sql,String keyword,int start) {
        int depth=0; char quote=0;
        for(int i=0;i<sql.length();i++) {
            char c=sql.charAt(i);
            if(quote!=0) {
                if(c=='\\') { i++; continue; }
                if(c==quote) {
                    if(i+1<sql.length() && sql.charAt(i+1)==quote) i++;
                    else quote=0;
                }
                continue;
            }
            if(c=='\'' || c=='"' || c=='`') { quote=c; continue; }
            if(c=='(') { depth++; continue; }
            if(c==')') { depth--; continue; }
            if(depth==0 && i>=start && sql.regionMatches(true,i,keyword,0,keyword.length())
                    && (i==0 || !Character.isJavaIdentifierPart(sql.charAt(i-1)))
                    && (i+keyword.length()==sql.length() || !Character.isJavaIdentifierPart(sql.charAt(i+keyword.length())))) return i;
        }
        return -1;
    }
}
