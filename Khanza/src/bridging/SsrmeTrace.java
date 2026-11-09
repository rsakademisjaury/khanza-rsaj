package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.*;

/** Riwayat dalam memori sesi pasien saja. Redaksi dilakukan sebelum data masuk model UI. */
public final class SsrmeTrace {
    private static final ObjectMapper JSON=new ObjectMapper();
    private final DefaultListModel<Entry> rows=new DefaultListModel<Entry>();
    private SsrmeUi.WindowFrame dialog;
    private boolean closed;
    public static final class Entry {
        public final String time,method,endpoint,request,response;
        public final int http; public final long duration;
        public Entry(String time,String method,String endpoint,int http,long duration,String request,String response) {
            this.time=time;this.method=method;this.endpoint=cleanEndpoint(endpoint);this.http=http;this.duration=duration;
            this.request=cleanJson(request);this.response=cleanJson(response);
        }
        @Override public String toString(){return method+" · "+(http==0?"PROSES / KONEKSI":http)+" · "+endpoint;}
    }
    public void add(Entry entry) {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(()->add(entry)); return; }
        if(closed)return;
        if(rows.size()>=80)rows.remove(0);rows.addElement(entry);
    }
    public void clear(){closed=true;rows.clear();if(dialog!=null)dialog.dispose();dialog=null;}
    public JButton button(Window owner){JButton b=SsrmeLauncher.button("{ JSON }");b.setToolTipText("Request dan response SSRME");b.addActionListener(e->show(owner));return b;}
    public void show(Window owner) {
        if(dialog!=null&&dialog.isDisplayable()){
            if(dialog.modalOwner==owner){dialog.toFront();return;}dialog.dispose();
        }
        dialog=new SsrmeUi.WindowFrame(owner,"SSRME · Request / Response");
        JPanel titleBar=new JPanel(new BorderLayout());titleBar.setBackground(Color.WHITE);titleBar.setPreferredSize(new java.awt.Dimension(0,50));
        titleBar.setBorder(BorderFactory.createEmptyBorder(0,12,0,12));JLabel title=SsrmeUi.label("SSRME · Request / Response",true);title.setFont(new Font("Segoe UI Semibold",Font.PLAIN,17));titleBar.add(title);dialog.add(titleBar,BorderLayout.NORTH);
        JList<Entry> list=new JList<Entry>(rows);list.setFont(new Font("Segoe UI",Font.PLAIN,12));list.setFixedCellHeight(50);
        list.setCellRenderer(new DefaultListCellRenderer(){@Override public java.awt.Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean selected,boolean focus){
            JLabel label=(JLabel)super.getListCellRendererComponent(l,v,i,selected,focus);label.putClientProperty("html.disable",Boolean.TRUE);
            label.setBorder(BorderFactory.createEmptyBorder(7,9,7,9));if(!selected&&v instanceof Entry){Entry e=(Entry)v;label.setBackground(e.http>=400?new Color(255,240,240):new Color(239,248,245));}return label;
        }});
        JTextPane code=new JTextPane();code.setEditable(false);code.setFont(new Font("Consolas",Font.PLAIN,12));code.setBackground(Color.WHITE);
        code.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        JTextArea lines=new JTextArea("1");lines.setEditable(false);lines.setFont(code.getFont());lines.setBackground(new Color(245,247,250));lines.setForeground(new Color(140,150,165));
        lines.setBorder(BorderFactory.createEmptyBorder(10,8,10,8));
        JScrollPane scroll=new JScrollPane(code);scroll.setRowHeaderView(lines);
        JLabel header=new JLabel("Pilih transaksi · raw (json)");header.setFont(new Font("Segoe UI Semibold",Font.PLAIN,13));header.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        JPanel detail=new JPanel(new BorderLayout());detail.add(header,BorderLayout.NORTH);detail.add(scroll,BorderLayout.CENTER);
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(list),detail);split.setDividerLocation(310);split.setResizeWeight(.25);dialog.add(split);
        final String[] shown={""};
        Runnable render=()->{Entry e=list.getSelectedValue();if(e==null)return;shown[0]="// "+e.time+"\n// "+e.method+" "+e.endpoint+"\n// HTTP "+e.http+" · "+e.duration+" ms\n\n/* REQUEST */\n"+e.request+"\n\n/* RESPONSE */\n"+e.response;
            header.setText("raw (json) · HTTP "+e.http+" · "+e.duration+" ms");style(code,shown[0]);StringBuilder n=new StringBuilder();int total=shown[0].split("\n",-1).length;for(int i=1;i<=total;i++)n.append(i).append('\n');lines.setText(n.toString());code.setCaretPosition(0);};
        list.addListSelectionListener(e->{if(!e.getValueIsAdjusting())render.run();});
        javax.swing.event.ListDataListener live=new javax.swing.event.ListDataListener(){public void intervalAdded(javax.swing.event.ListDataEvent e){list.setSelectedIndex(rows.size()-1);}public void intervalRemoved(javax.swing.event.ListDataEvent e){}public void contentsChanged(javax.swing.event.ListDataEvent e){}};
        rows.addListDataListener(live);dialog.addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){rows.removeListDataListener(live);}});
        JButton copy=SsrmeLauncher.button("Copy"),close=SsrmeLauncher.button("Tutup");
        copy.addActionListener(e->java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(shown[0]),null));
        final SsrmeUi.WindowFrame shownDialog=dialog;close.addActionListener(e->shownDialog.dispose());dialog.add(SsrmeUi.footer(null,copy,close),BorderLayout.SOUTH);
        if(rows.size()>0)list.setSelectedIndex(rows.size()-1);else code.setText("Belum ada transaksi. Riwayat akan bertambah saat permintaan diproses.");
        dialog.setVisible(true);
    }
    public static String now(){return ZonedDateTime.now(java.time.ZoneId.of("Asia/Makassar")).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss 'WITA'"));}
    public static String cleanEndpoint(String value){try{URI u=URI.create(value);return u.getScheme()+"://"+u.getHost()+(u.getPort()>0?":"+u.getPort():"")+u.getPath()+(u.getRawQuery()==null?"":"?[PARAMETERS REDACTED]");}catch(Exception e){return "[ENDPOINT]";}}
    public static String cleanJson(String text){
        if(text==null||text.isEmpty())return "{}";
        try{JsonNode node=JSON.readTree(text);node=scrub(node,"",0);String value=JSON.writerWithDefaultPrettyPrinter().writeValueAsString(node);return value.length()>16000?"{\"truncated\":true}":value;}
        catch(Exception e){return "{\"message\":\"Respons bukan JSON atau belum tersedia\"}";}
    }
    private static JsonNode scrub(JsonNode node,String key,int depth){
        if(depth>10)return com.fasterxml.jackson.databind.node.TextNode.valueOf("[DEPTH LIMIT]");
        if(key.matches("(?i).*?(token|secret|password|authorization|ticket|launch|shlink|verificationurl|consentid).*" )||key.equalsIgnoreCase("client_id"))
            return com.fasterxml.jackson.databind.node.TextNode.valueOf("[REDACTED]");
        if(node==null)return JSON.createObjectNode();
        if(node.isObject()){
            ObjectNode result=JSON.createObjectNode();Iterator<Map.Entry<String,JsonNode>> fields=node.fields();int count=0;
            while(fields.hasNext()){Map.Entry<String,JsonNode> f=fields.next();if(++count>40){result.put("_truncated",true);break;}result.set(f.getKey(),scrub(f.getValue(),f.getKey(),depth+1));}return result;
        }
        if(node.isArray()){
            com.fasterxml.jackson.databind.node.ArrayNode result=JSON.createArrayNode();int count=0;for(JsonNode v:node){if(++count>40)break;result.add(scrub(v,key,depth+1));}return result;
        }
        if(node.isTextual()){
            String value=node.asText().replaceAll("(?i)Bearer\\s+[A-Za-z0-9._~+\\/=\\-]+","Bearer [REDACTED]").replaceAll("[0-9]{12}([0-9]{4})","************$1")
                .replaceAll("(?i)([?&](?:launch|shlink|token|ticket)=)[^&\\s\"<>]+","$1[REDACTED]")
                .replaceAll("(?i)https://[^\\s\"<>]+/rekammedis[^\\s\"<>]*","[SSRME LINK REDACTED]");
            if(key.matches("(?i)(patient_id|practitioner_id|nik|no_ktp|id|value)")&&value.matches(".*[0-9]{8,}.*"))value="***"+value.substring(Math.max(0,value.length()-4));
            return com.fasterxml.jackson.databind.node.TextNode.valueOf(value);
        }
        return node;
    }
    private static void style(JTextPane pane,String value){
        StyledDocument doc=pane.getStyledDocument();try{doc.remove(0,doc.getLength());doc.insertString(0,value,null);
            SimpleAttributeSet normal=new SimpleAttributeSet();StyleConstants.setFontFamily(normal,"Consolas");StyleConstants.setFontSize(normal,12);StyleConstants.setForeground(normal,new Color(45,55,70));doc.setCharacterAttributes(0,value.length(),normal,true);
            Matcher m=Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"|//[^\\n]*|/\\*.*?\\*/|\\b(?:true|false|null|[0-9]+)\\b",Pattern.DOTALL).matcher(value);
            while(m.find()){SimpleAttributeSet a=new SimpleAttributeSet();String t=m.group();boolean key=t.startsWith("\"")&&value.substring(m.end()).matches("(?s)^\\s*:.*");StyleConstants.setForeground(a,t.startsWith("/")?new Color(135,145,155):key?new Color(190,50,90):t.startsWith("\"")?new Color(30,130,75):new Color(35,95,190));doc.setCharacterAttributes(m.start(),m.end()-m.start(),a,false);}
        }catch(BadLocationException ignored){}}
}
