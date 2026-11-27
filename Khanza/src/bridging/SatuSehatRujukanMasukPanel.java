package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/** Rujukan masuk model kartu grid. Detail dan pendaftaran menggunakan alur rujukan yang sama. */
public final class SatuSehatRujukanMasukPanel extends JPanel {
    private static final Color INK=new Color(25,49,69),MUTED=new Color(91,113,131),BLUE=new Color(18,100,207),BG=new Color(243,246,251),LINE=new Color(224,231,240),LIGHT=new Color(237,244,255),GREEN=new Color(32,115,82),AMBER=new Color(154,101,20),RED=new Color(170,71,82);
    private static final Color WAIT_BG=new Color(255,246,229),ACCEPT_BG=new Color(234,246,239),REJECT_BG=new Color(252,240,241);
    private static final Font BODY=new Font("Segoe UI",Font.PLAIN,13),SMALL=new Font("Segoe UI",Font.PLAIN,12),STRONG=new Font("Segoe UI Semibold",Font.BOLD,15);
    private static final Instant NO_SENT_TIME=Instant.MIN;
    private static final ZoneId WITA=ZoneId.of("Asia/Makassar");
    private static final DateTimeFormatter SENT_FORMAT=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(WITA);
    private final SatuSehatRujukanMasukService service;
    private SatuSehatRujukanIGDRanapApi apiClient=new SatuSehatRujukanIGDRanapApi();
    private final JPanel pages=new JPanel(new CardLayout()),detailHost=new JPanel(new BorderLayout(0,12)),apiHost=new JPanel(new BorderLayout(0,10));
    private final JLabel status=label("Memuat rujukan masuk...",SMALL,MUTED),listCount=label("0 rujukan",SMALL,MUTED);
    private final JTextArea message=readOnly(2);
    private final JPanel notice=new JPanel(new BorderLayout());
    private final Timer toastTimer=new Timer(6500,e->notice.setVisible(false));
    private final List<SatuSehatRujukanMasukService.Row> rows=new ArrayList<SatuSehatRujukanMasukService.Row>();
    private final CardGrid cards=new CardGrid();
    private JScrollPane cardsScroll;
    private final JTextField search=new SearchField();
    private final JButton reload=button("Perbarui",Color.WHITE,BLUE),close=button("Keluar",Color.WHITE,MUTED),apiButton=button("Respon API",Color.WHITE,BLUE);
    private final JToggleButton[] filters=new JToggleButton[4];
    private final JLabel[] counts={label("0",new Font("Segoe UI Semibold",Font.BOLD,24),INK),label("0",new Font("Segoe UI Semibold",Font.BOLD,24),INK),label("0",new Font("Segoe UI Semibold",Font.BOLD,24),INK)};
    private final List<JButton> cardButtons=new ArrayList<JButton>(),detailButtons=new ArrayList<JButton>();
    private JButton register;
    private JTextArea registrationStatus;
    private Runnable onClose;
    private boolean busy,disposed;
    private int statusFilter;
    private String selectedTaskId="",currentPage="list",apiReturnPage="list";
    private long detailGeneration;
    private SwingWorker<SatuSehatRujukanIGDRanapApi.IncomingReferralDetail,Void> detailWorker;
    private SatuSehatRujukanIGDRanapApi.IncomingReferralDetail currentDetail;
    private final SatuSehatRujukanMasukService.Listener listener=(snapshot,error,fresh,first)->updateList(snapshot,error);

    public SatuSehatRujukanMasukPanel(SatuSehatRujukanMasukService service){
        this.service=service;setLayout(new BorderLayout());setBackground(BG);
        add(filterBar(),BorderLayout.WEST);
        JPanel main=new JPanel(new BorderLayout(0,12));main.setOpaque(false);main.setBorder(BorderFactory.createEmptyBorder(23,24,14,24));
        notice.setBackground(LIGHT);notice.setBorder(BorderFactory.createEmptyBorder(9,12,9,12));notice.add(message,BorderLayout.CENTER);notice.setVisible(false);toastTimer.setRepeats(false);
        main.add(notice,BorderLayout.NORTH);pages.setOpaque(false);pages.add(listPage(),"list");
        detailHost.setOpaque(false);apiHost.setOpaque(false);
        pages.add(pageSurface(detailHost),"detail");pages.add(pageSurface(apiHost),"api");main.add(pages,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(12,8));footer.setOpaque(false);footer.add(status,BorderLayout.NORTH);
        JPanel actions=flow();apiButton.setIcon(new LineIcon("code",BLUE,16));apiButton.addActionListener(e->showApi());
        close.setIcon(new LineIcon("exit",MUTED,15));close.addActionListener(e->requestClose());
        actions.add(apiButton);actions.add(Box.createHorizontalStrut(8));actions.add(close);footer.add(actions,BorderLayout.WEST);footer.add(listCount,BorderLayout.EAST);main.add(footer,BorderLayout.SOUTH);
        add(main,BorderLayout.CENTER);service.addListener(listener);service.refresh();
    }

    private JPanel pageSurface(JPanel content){
        RoundPanel surface=new RoundPanel(Color.WHITE,LINE,16);surface.setLayout(new BorderLayout());surface.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));surface.add(content);return surface;
    }

    public void setOnClose(Runnable action){onClose=action;}
    public void requestClose(){if(busy){toast("Tunggu proses rujukan selesai sebelum menutup form.",true);return;}if(onClose!=null)onClose.run();}
    public void shutdown(){disposed=true;cancelDetail();toastTimer.stop();service.removeListener(listener);SatuSehatRujukanMasukPasien.closeFor(this);}

    private JPanel filterBar(){
        JPanel aside=new JPanel(new BorderLayout());aside.setBackground(Color.WHITE);aside.setPreferredSize(new Dimension(185,200));
        aside.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,0,1,LINE),BorderFactory.createEmptyBorder(26,13,20,13)));
        JPanel menu=vertical();menu.add(label("KOTAK RUJUKAN",new Font("Segoe UI",Font.PLAIN,11),MUTED));menu.add(Box.createVerticalStrut(18));
        String[] names={"Semua rujukan","Menunggu","Diterima","Ditolak"},icons={"inbox","clock","check","reject"};ButtonGroup group=new ButtonGroup();
        for(int i=0;i<filters.length;i++){
            final int choice=i;JToggleButton item=new JToggleButton(names[i]);item.setIcon(new LineIcon(icons[i],MUTED,17));item.setIconTextGap(9);
            item.setFont(BODY);item.setOpaque(false);item.setContentAreaFilled(false);item.setHorizontalAlignment(SwingConstants.LEFT);item.setAlignmentX(0);
            item.setBorder(BorderFactory.createEmptyBorder(11,11,11,11));item.setMaximumSize(new Dimension(Integer.MAX_VALUE,43));
            item.setUI(new BasicButtonUI(){
                @Override public void paint(Graphics graphics,JComponent component){
                    AbstractButton control=(AbstractButton)component;Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(control.isSelected()||control.getModel().isRollover()){g.setColor(LIGHT);g.fillRoundRect(0,0,component.getWidth()-1,component.getHeight()-1,11,11);}
                    if(control.hasFocus()){g.setColor(BLUE);g.drawRoundRect(1,1,component.getWidth()-3,component.getHeight()-3,11,11);}g.dispose();
                    component.setForeground(control.isSelected()?BLUE:MUTED);super.paint(graphics,component);
                }
            });
            item.addActionListener(e->{statusFilter=choice;filter();});group.add(item);menu.add(item);menu.add(Box.createVerticalStrut(7));filters[i]=item;
        }
        filters[0].setSelected(true);aside.add(menu,BorderLayout.NORTH);return aside;
    }

    private JPanel listPage(){
        JPanel page=new JPanel(new BorderLayout(0,18));page.setOpaque(false);
        JPanel tools=new JPanel(new BorderLayout(0,16));tools.setOpaque(false);
        JPanel heading=new JPanel(new BorderLayout(0,5));heading.setOpaque(false);
        heading.add(label("KOORDINASI PELAYANAN",new Font("Segoe UI",Font.PLAIN,11),MUTED),BorderLayout.NORTH);
        JPanel titleRow=new JPanel(new BorderLayout(12,0));titleRow.setOpaque(false);
        titleRow.add(label("Rujukan Masuk",new Font("Segoe UI Semibold",Font.BOLD,25),INK),BorderLayout.CENTER);
        JPanel metrics=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));metrics.setOpaque(false);
        String[] names={"Menunggu keputusan","Diterima","Ditolak"};Color[] colors={AMBER,GREEN,RED};
        for(int i=0;i<counts.length;i++){
            JPanel metric=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));metric.setOpaque(false);JPanel mark=new JPanel();mark.setBackground(colors[i]);mark.setPreferredSize(new Dimension(3,28));
            metric.add(mark);metric.add(counts[i]);metric.add(label(names[i],new Font("Segoe UI",Font.PLAIN,11),MUTED));if(i>0)metrics.add(Box.createHorizontalStrut(9));metrics.add(metric);
        }
        titleRow.add(metrics,BorderLayout.EAST);heading.add(titleRow,BorderLayout.CENTER);
        heading.add(label("Rujukan terbaru dari fasilitas kesehatan pengirim.",BODY,MUTED),BorderLayout.SOUTH);
        heading.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,LINE),BorderFactory.createEmptyBorder(0,0,17,0)));tools.add(heading,BorderLayout.NORTH);
        JPanel searchRow=new JPanel(new BorderLayout(12,0));searchRow.setOpaque(false);search.setFont(BODY);search.setBackground(Color.WHITE);search.setPreferredSize(new Dimension(350,39));
        search.setToolTipText("Cari pasien, IHS, rumah sakit, Task ID, jenis, atau waktu kirim");search.getAccessibleContext().setAccessibleName("Cari rujukan masuk");
        reload.setIcon(new LineIcon("reload",BLUE,16));reload.addActionListener(e->{service.refresh();status.setText("Memperbarui daftar rujukan...");});
        JPanel update=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0));update.setOpaque(false);update.add(label("Terbaru lebih dahulu · WITA",SMALL,MUTED));update.add(reload);
        searchRow.add(search,BorderLayout.CENTER);searchRow.add(update,BorderLayout.EAST);tools.add(searchRow,BorderLayout.SOUTH);page.add(tools,BorderLayout.NORTH);
        search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filter();}public void removeUpdate(DocumentEvent e){filter();}public void changedUpdate(DocumentEvent e){filter();}});
        cards.setName("grid_kartu_rujukan_masuk");cardsScroll=scroll(cards);cardsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);page.add(cardsScroll,BorderLayout.CENTER);
        rebuildCards();return page;
    }

    private void showPage(String name){currentPage=name;((CardLayout)pages.getLayout()).show(pages,name);}
    private void showCards(){
        if(busy||disposed)return;cancelDetail();currentDetail=null;selectedTaskId="";register=null;registrationStatus=null;detailButtons.clear();showPage("list");
    }
    private void filter(){showCards();rebuildCards();cardsScroll.getViewport().setViewPosition(new Point(0,0));}
    private boolean matches(SatuSehatRujukanMasukService.Row row,String key){
        if((statusFilter==1&&!row.pending())||(statusFilter==2&&!row.accepted())||(statusFilter==3&&!"rejected".equals(row.decision)))return false;
        String content=row.patient+" "+row.referrer+" "+row.id+" "+row.kind+" "+row.status+" "+decisionLabel(row)+" "+patientReference(row)+" "+formatSentTime(sentTime(row.task));
        return content.toLowerCase(Locale.ROOT).contains(key);
    }

    private void rebuildCards(){
        Point position=cardsScroll==null?new Point(0,0):cardsScroll.getViewport().getViewPosition();cards.removeAll();cardButtons.clear();
        String key=search.getText().trim().toLowerCase(Locale.ROOT);int visible=0;
        for(SatuSehatRujukanMasukService.Row row:rows)if(matches(row,key)){cards.add(referralCard(row));visible++;}
        if(visible==0){JPanel empty=empty("inbox",rows.isEmpty()?"Belum ada rujukan":"Rujukan tidak ditemukan",rows.isEmpty()?"Daftar rujukan akan muncul setelah sinkronisasi berhasil.":"Coba kata pencarian atau pilihan status lain.");empty.putClientProperty("rujukan.empty",Boolean.TRUE);cards.add(empty);}
        listCount.setText(visible+" dari "+rows.size()+" rujukan");updateCounts();cards.revalidate();cards.repaint();
        if(cardsScroll!=null){int max=Math.max(0,cards.getPreferredSize().height-cardsScroll.getViewport().getExtentSize().height);cardsScroll.getViewport().setViewPosition(new Point(0,Math.min(position.y,max)));}
    }

    private void updateCounts(){
        int waiting=0,accepted=0,rejected=0;
        for(SatuSehatRujukanMasukService.Row row:rows){if(row.pending())waiting++;if(row.accepted())accepted++;if("rejected".equals(row.decision))rejected++;}
        counts[0].setText(String.valueOf(waiting));counts[1].setText(String.valueOf(accepted));counts[2].setText(String.valueOf(rejected));
        int[] totals={rows.size(),waiting,accepted,rejected};for(int i=0;i<filters.length;i++)filters[i].getAccessibleContext().setAccessibleDescription(totals[i]+" rujukan");
    }

    static Instant sentTime(JsonNode task){Instant authored=parseSentTime(task.path("authoredOn").asText());return authored.equals(NO_SENT_TIME)?parseSentTime(task.path("executionPeriod").path("start").asText()):authored;}
    private static Instant parseSentTime(String text){try{return OffsetDateTime.parse(text.trim()).toInstant();}catch(DateTimeParseException ex){return NO_SENT_TIME;}}
    static String formatSentTime(Instant time){return time==null||NO_SENT_TIME.equals(time)?"—":SENT_FORMAT.format(time);}

    private void updateList(SatuSehatRujukanMasukService.Snapshot snapshot,String error){
        if(disposed)return;
        if(snapshot!=null){
            SatuSehatRujukanMasukService.Row previous=findRow(selectedTaskId);rows.clear();rows.addAll(snapshot.rows);
            rows.sort((a,b)->{int time=sentTime(b.task).compareTo(sentTime(a.task));return time==0?a.id.compareTo(b.id):time;});
            SatuSehatRujukanMasukService.Row latest=findRow(selectedTaskId);boolean changed=previous==null||latest==null||!previous.task.equals(latest.task);rebuildCards();
            if(!busy&&!"list".equals(currentPage)&&!selectedTaskId.isEmpty()&&changed){if(latest==null)showCards();else selectReferral(latest);}
            if(!busy&&detailWorker==null)status.setText(snapshot.pending+" menunggu keputusan • Diperbarui "+DateTimeFormatter.ofPattern("HH:mm:ss").withZone(WITA).format(Instant.ofEpochMilli(snapshot.time))+" WITA");
        }
        if(error!=null&&!error.isEmpty())toast(error,true);
    }

    private static String patientReference(SatuSehatRujukanMasukService.Row row){return SatuSehatRujukanMasukService.reference(row.task.path("for"));}
    private static String patientName(SatuSehatRujukanMasukService.Row row){String name=or(row.patient,"");String ref=patientReference(row);return name.isEmpty()||name.equals(ref)||name.startsWith("Patient/")?"Pasien rujukan":name;}
    private static String decisionLabel(SatuSehatRujukanMasukService.Row row){if(row.pending())return "Menunggu";if(row.accepted())return "Diterima";if("rejected".equals(row.decision))return "Ditolak";return "Belum diputuskan";}
    private static JLabel decisionPill(SatuSehatRujukanMasukService.Row row){
        Color fill=BG,ink=MUTED;String icon="history";
        if(row.pending()){fill=WAIT_BG;ink=AMBER;icon="clock";}else if(row.accepted()){fill=ACCEPT_BG;ink=GREEN;icon="check";}else if("rejected".equals(row.decision)){fill=REJECT_BG;ink=RED;icon="reject";}
        JLabel status=pill(decisionLabel(row),fill,ink);status.setIcon(new LineIcon(icon,ink,12));status.setIconTextGap(5);return status;
    }

    private JPanel referralCard(SatuSehatRujukanMasukService.Row row){
        RoundPanel tile=new RoundPanel(Color.WHITE,LINE,16);tile.setLayout(new BorderLayout(0,10));tile.setBorder(BorderFactory.createEmptyBorder(15,13,15,13));tile.putClientProperty("rujukan.taskId",row.id);tile.getAccessibleContext().setAccessibleName("Rujukan "+patientName(row)+", "+decisionLabel(row));
        JPanel top=new JPanel(new BorderLayout(8,0));top.setOpaque(false);JLabel kind=pill(or(row.kind,"Belum diketahui"),LIGHT,BLUE);kind.setIcon(new LineIcon("send",BLUE,12));kind.setIconTextGap(5);top.add(kind,BorderLayout.WEST);
        JPanel badge=flow();badge.add(decisionPill(row));top.add(badge,BorderLayout.EAST);tile.add(top,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(0,8));body.setOpaque(false);
        JPanel person=new JPanel(new BorderLayout(11,0));person.setOpaque(false);
        RoundPanel avatar=new RoundPanel(BG,BG,12);avatar.setLayout(new BorderLayout());avatar.setPreferredSize(new Dimension(41,41));avatar.add(new JLabel(new LineIcon("user",MUTED,23)));person.add(avatar,BorderLayout.WEST);
        JPanel identity=vertical();JLabel name=label(patientName(row),STRONG,INK);name.setToolTipText(or(row.patient,"Pasien rujukan"));identity.add(name);identity.add(Box.createVerticalStrut(4));
        String ref=patientReference(row);String ihs=ref.startsWith("Patient/")?"IHS "+ref.substring(8):or(ref,"IHS belum tersedia");JLabel id=label(ihs,SMALL,MUTED);id.setToolTipText(ihs);identity.add(id);person.add(identity,BorderLayout.CENTER);body.add(person,BorderLayout.NORTH);
        JPanel meta=vertical();JLabel hospital=label(or(row.referrer,"Fasyankes perujuk belum tersedia"),SMALL,MUTED);hospital.setIcon(new LineIcon("hospital",MUTED,15));hospital.setIconTextGap(7);hospital.setToolTipText(row.referrer);meta.add(hospital);meta.add(Box.createVerticalStrut(5));
        JLabel time=label(formatSentTime(sentTime(row.task))+" WITA",SMALL,MUTED);time.setIcon(new LineIcon("clock",MUTED,15));time.setIconTextGap(7);meta.add(time);body.add(meta,BorderLayout.CENTER);tile.add(body,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(6,0));bottom.setOpaque(false);bottom.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,LINE),BorderFactory.createEmptyBorder(8,0,0,0)));
        JLabel task=label("Task · "+row.id,new Font("Segoe UI",Font.PLAIN,11),MUTED);task.setToolTipText(row.id);bottom.add(task,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));actions.setOpaque(false);
        if(row.pending()){
            actions.add(cardButton("Terima","check",BLUE,Color.WHITE,row.id,()->respond(row.id,true)));actions.add(cardButton("Tolak","reject",Color.WHITE,RED,row.id,()->respond(row.id,false)));
        }else if(row.accepted())actions.add(cardButton("Lihat Detail","eye",Color.WHITE,BLUE,row.id,()->selectReferral(findRow(row.id))));
        else actions.add(label("rejected".equals(row.decision)?"Rujukan ditutup":decisionLabel(row),SMALL,MUTED));
        bottom.add(actions,BorderLayout.EAST);tile.add(bottom,BorderLayout.SOUTH);
        tile.setMinimumSize(new Dimension(task.getPreferredSize().width+actions.getPreferredSize().width+6+tile.getInsets().left+tile.getInsets().right,CardGrid.HEIGHT));return tile;
    }

    private JButton cardButton(String text,String icon,Color background,Color foreground,String taskId,Runnable action){
        JButton control=button(text,background,foreground);control.setIcon(new LineIcon(icon,foreground,12));control.setFont(new Font("Segoe UI",Font.PLAIN,11));control.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        control.putClientProperty("rujukan.taskId",taskId);control.setEnabled(!busy);control.addActionListener(e->action.run());cardButtons.add(control);return control;
    }

    private static final class CardGrid extends JPanel implements Scrollable {
        private static final int GAP=15,HEIGHT=236;
        CardGrid(){setLayout(null);setOpaque(false);setBorder(BorderFactory.createEmptyBorder(0,0,3,3));}
        private int columns(int width){
            int minimum=420;for(Component tile:getComponents())minimum=Math.max(minimum,tile.getMinimumSize().width);
            return Math.max(1,Math.min(3,(Math.max(0,width-3)+GAP)/(minimum+GAP)));
        }
        private boolean empty(){return getComponentCount()==1&&Boolean.TRUE.equals(((JComponent)getComponent(0)).getClientProperty("rujukan.empty"));}
        @Override public Dimension getPreferredSize(){
            int width=getParent() instanceof JViewport?((JViewport)getParent()).getExtentSize().width:getWidth();if(width<=0)width=800;
            int columns=empty()?1:columns(width);int lines=(getComponentCount()+columns-1)/columns;return new Dimension(width,Math.max(0,lines*HEIGHT+(lines-1)*GAP)+3);
        }
        @Override public void doLayout(){
            int width=Math.max(0,getWidth()-3),columns=empty()?1:columns(getWidth());int tileWidth=Math.max(0,(width-(columns-1)*GAP)/columns);
            for(int i=0;i<getComponentCount();i++)getComponent(i).setBounds((i%columns)*(tileWidth+GAP),(i/columns)*(HEIGHT+GAP),tileWidth,HEIGHT);
        }
        @Override public void setBounds(int x,int y,int width,int height){int previous=getWidth();super.setBounds(x,y,width,height);if(previous!=width)revalidate();}
        public Dimension getPreferredScrollableViewportSize(){return new Dimension(800,530);}
        public int getScrollableUnitIncrement(Rectangle area,int orientation,int direction){return 20;}
        public int getScrollableBlockIncrement(Rectangle area,int orientation,int direction){return Math.max(20,area.height-30);}
        public boolean getScrollableTracksViewportWidth(){return true;}public boolean getScrollableTracksViewportHeight(){return false;}
    }

    private void cancelDetail(){detailGeneration++;if(detailWorker!=null){detailWorker.cancel(true);detailWorker=null;}}
    private void selectReferral(SatuSehatRujukanMasukService.Row row){
        if(busy||disposed)return;if(row==null){showCards();return;}cancelDetail();currentDetail=null;selectedTaskId=row.id;
        showPreview(row,row.pending()?"Rujukan menunggu keputusan penerima.":row.accepted()?"Memuat detail rujukan...":"Keputusan rujukan tercatat pada status di atas.");if(row.accepted())loadDetail(row.id);
    }

    private JButton detailButton(String text,String icon,Color background,Color foreground,Runnable action){
        JButton item=button(text,background,foreground);item.setIcon(new LineIcon(icon,foreground,15));item.setEnabled(!busy);item.addActionListener(e->action.run());detailButtons.add(item);return item;
    }
    private void refreshPreview(){detailHost.revalidate();detailHost.repaint();showPage("detail");}

    private JPanel detailHeading(String patient,String referrer,SatuSehatRujukanMasukService.Row row){
        JPanel heading=new JPanel(new BorderLayout(12,0));heading.setOpaque(false);
        String text="DETAIL RUJUKAN MASUK  —  "+patient+"  ·  Dari "+referrer;
        JLabel title=label(text,new Font("Segoe UI Semibold",Font.BOLD,15),INK);title.setToolTipText(text);heading.add(title,BorderLayout.CENTER);
        if(row!=null){JPanel badge=flow();badge.add(decisionPill(row));heading.add(badge,BorderLayout.EAST);}return heading;
    }

    private void showPreview(SatuSehatRujukanMasukService.Row row,String text){
        detailHost.removeAll();detailButtons.clear();register=null;registrationStatus=null;
        if(row==null){detailHost.add(empty("inbox","Pilih rujukan",text),BorderLayout.CENTER);refreshPreview();return;}
        detailHost.add(detailHeading(patientName(row),or(row.referrer,"—"),row),BorderLayout.NORTH);
        Vertical summary=new Vertical();summary.add(summaryCard("Identitas rujukan",new String[][]{{"Fasyankes perujuk",or(row.referrer,"—")},{"Jenis pelayanan",or(row.kind,"Belum diketahui")},{"Waktu rujukan dikirim",formatSentTime(sentTime(row.task))+" WITA"},{"Status Task",or(row.status,"—")},{"Keputusan",decisionLabel(row)},{"Task ID",row.id}}));
        summary.add(Box.createVerticalStrut(12));summary.add(note(text,row.pending()?WAIT_BG:LIGHT,row.pending()?AMBER:BLUE));detailHost.add(scroll(summary),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);
        actions.add(detailButton("Kembali ke Kartu","back",Color.WHITE,BLUE,()->showCards()));
        if(row.pending()){
            actions.add(detailButton("Terima Rujukan","check",BLUE,Color.WHITE,()->respond(row.id,true)));
            actions.add(detailButton("Tolak","reject",Color.WHITE,RED,()->respond(row.id,false)));
        }else if(row.accepted()){
            JButton retry=detailButton("Muat Ulang Detail","reload",Color.WHITE,BLUE,()->loadDetail(row.id));retry.setEnabled(detailWorker==null&&!busy);actions.add(retry);
        }
        detailHost.add(actions,BorderLayout.SOUTH);refreshPreview();
    }

    private void respond(final String taskId,final boolean accepted){
        if(busy||disposed)return;SatuSehatRujukanMasukService.Row selected=findRow(taskId);if(selected==null||!selected.pending())return;
        cancelDetail();currentDetail=null;selectedTaskId=taskId;
        if(JOptionPane.showConfirmDialog(this,(accepted?"Terima":"Tolak")+" rujukan untuk "+selected.patient+" dari "+selected.referrer+"?","Konfirmasi rujukan",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        selected=findRow(taskId);if(selected==null||!selected.pending()||!taskId.equals(selectedTaskId)||busy||disposed)return;
        setBusy(true,(accepted?"Menerima":"Menolak")+" rujukan...");
        final SatuSehatRujukanIGDRanapApi responseApi=new SatuSehatRujukanIGDRanapApi();
        new SwingWorker<JsonNode,Void>(){
            @Override protected JsonNode doInBackground()throws Exception{return responseApi.responRujukanMasuk(taskId,accepted);}
            @Override protected void done(){
                if(disposed)return;apiClient=responseApi;
                try{
                    JsonNode confirmed=get();
                    for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(taskId)){
                        SatuSehatRujukanMasukService.Row old=rows.get(i);rows.set(i,new SatuSehatRujukanMasukService.Row(confirmed,old.patient,old.referrer,old.kind));break;
                    }
                    setBusy(false,"Keputusan berhasil dikonfirmasi.");showCards();rebuildCards();toast("Rujukan berhasil "+(accepted?"diterima.":"ditolak."),false);
                }catch(Exception ex){setBusy(false,"Keputusan belum dapat dikonfirmasi.");showCards();rebuildCards();failure(ex);}
                finally{service.decisionChanged();}
            }
        }.execute();
    }

    private SatuSehatRujukanMasukService.Row findRow(String id){for(SatuSehatRujukanMasukService.Row row:rows)if(row.id.equals(id))return row;return null;}
    private void loadDetail(final String taskId){
        if(busy||disposed||!taskId.equals(selectedTaskId))return;
        SatuSehatRujukanMasukService.Row row=findRow(taskId);if(row==null||!row.accepted())return;
        cancelDetail();currentDetail=null;final long run=detailGeneration;
        final SatuSehatRujukanIGDRanapApi detailApi=new SatuSehatRujukanIGDRanapApi();
        detailWorker=new SwingWorker<SatuSehatRujukanIGDRanapApi.IncomingReferralDetail,Void>(){
            @Override protected SatuSehatRujukanIGDRanapApi.IncomingReferralDetail doInBackground()throws Exception{
                SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail=detailApi.detailRujukanMasuk(taskId);
                if(!"accepted".equals(SatuSehatRujukanIGDRanapApi.parseDecision(detail.task)))throw new java.io.IOException("Rujukan belum diterima atau statusnya berubah. Perbarui daftar.");return detail;
            }
            @Override protected void done(){
                if(disposed||run!=detailGeneration||!taskId.equals(selectedTaskId))return;
                detailWorker=null;apiClient=detailApi;SatuSehatRujukanMasukService.Row selected=findRow(taskId);
                if(selected==null||!selected.accepted()){selectReferral(selected);return;}
                try{currentDetail=get();status.setText("Detail rujukan dimuat.");showIncomingDetail(currentDetail);}
                catch(Exception ex){currentDetail=null;status.setText("Detail belum dapat dimuat.");showPreview(selected,"Detail belum dapat dimuat. Gunakan Muat Ulang Detail atau periksa Respon API.");failure(ex);}
            }
        };
        showPreview(row,"Memuat data rujukan dari SATUSEHAT...");status.setText("Memuat detail rujukan...");detailWorker.execute();
    }

    private void showIncomingDetail(final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail){
        detailHost.removeAll();detailButtons.clear();
        SatuSehatRujukanMasukService.Row row=findRow(detail.taskId);
        String patient=SatuSehatRujukanMasukService.resourceName(detail.patient,row==null?"Pasien rujukan":patientName(row));
        String referrer=SatuSehatRujukanMasukService.resourceName(detail.referrerOrganization,row==null?"—":row.referrer);
        detailHost.add(detailHeading(patient,referrer,row),BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(BODY);tabs.setBackground(Color.WHITE);tabs.setForeground(INK);
        tabs.setUI(new BasicTabbedPaneUI(){
            @Override protected void installDefaults(){super.installDefaults();tabInsets=new Insets(9,12,9,12);contentBorderInsets=new Insets(12,0,0,0);}
            @Override protected void paintTabBackground(Graphics graphics,int placement,int index,int x,int y,int w,int h,boolean selected){Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(selected?LIGHT:Color.WHITE);g.fillRoundRect(x,y,w,h,10,10);g.dispose();}
            @Override protected void paintTabBorder(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){}
            @Override protected void paintContentBorder(Graphics g,int placement,int selected){}
        });
        tabs.addTab("Ringkasan",scroll(incomingSummaryPanel(detail)));tabs.addTab("Data Klinis",scroll(incomingClinicalPanel(detail)));tabs.addTab("CarePlan",scroll(incomingCarePlanPanel(detail)));tabs.addTab("Resource FHIR",incomingResourcePanel(detail));tabs.addTab("JSON",incomingJsonPanel(detail));detailHost.add(tabs,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,10));bottom.setOpaque(false);registrationStatus=readOnly(2);registrationStatus.setColumns(40);registrationStatus.setText("Mencocokkan data pasien...");bottom.add(registrationStatus,BorderLayout.NORTH);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);
        actions.add(detailButton("Kembali ke Kartu","back",Color.WHITE,BLUE,()->showCards()));
        actions.add(detailButton("Muat Ulang","reload",Color.WHITE,BLUE,()->loadDetail(detail.taskId)));
        register=detailButton("Daftarkan Pasien","plus",BLUE,Color.WHITE,()->registerPatient(detail));register.setEnabled(!busy&&SatuSehatRujukanMasukPasien.allowed());actions.add(register);
        bottom.add(actions,BorderLayout.SOUTH);detailHost.add(bottom,BorderLayout.SOUTH);refreshPreview();checkRegistration(detail);
    }

    private void checkRegistration(final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail){
        if(currentDetail!=detail||register==null)return;
        final JButton target=register;final JTextArea description=registrationStatus;
        new SwingWorker<SatuSehatRujukanMasukPasien.State,Void>(){
            @Override protected SatuSehatRujukanMasukPasien.State doInBackground()throws Exception{return SatuSehatRujukanMasukPasien.state(detail);}
            @Override protected void done(){
                if(disposed||currentDetail!=detail||register!=target)return;
                try{SatuSehatRujukanMasukPasien.State state=get();description.setText(state.message());target.setText(state.found()?"Buka Data Pasien":"Daftarkan Pasien");}
                catch(Exception ex){description.setText("Data pasien belum dapat dicocokkan. Pencocokan diulang saat Data Pasien dibuka.");}
                target.setEnabled(!busy&&SatuSehatRujukanMasukPasien.allowed());
            }
        }.execute();
    }

    private void registerPatient(final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail detail){
        SatuSehatRujukanMasukService.Row row=findRow(detail.taskId);
        if(busy||disposed||currentDetail!=detail||row==null||!row.accepted()||!SatuSehatRujukanMasukPasien.allowed())return;
        setBusy(true,"Menyiapkan form Data Pasien...");
        SatuSehatRujukanMasukPasien.open(this,detail,()->{if(disposed)return;setBusy(false,"Kembali dari Data Pasien.");checkRegistration(detail);},error->{if(disposed)return;setBusy(false,"Pendaftaran belum dapat dibuka.");toast(error,true);checkRegistration(detail);});
    }

    private void showApi(){
        if(busy)return;if(detailWorker!=null){toast("Detail sedang dimuat. Respon API dapat dibuka setelah proses selesai.",false);return;}
        apiHost.removeAll();JTextArea text=new JTextArea("REQUEST\n"+apiClient.getLastMethod()+" "+apiClient.getLastUrl()+"\n"+prettyJson(apiClient.getLastRequest())+"\n\nRESPONSE\n"+prettyJson(apiClient.getLastResponse()));
        text.setEditable(false);text.setFont(new Font("Consolas",Font.PLAIN,12));text.setCaretPosition(0);apiHost.add(scroll(text),BorderLayout.CENTER);
        if(!"api".equals(currentPage))apiReturnPage=currentPage;
        JPanel controls=flow();JButton back=button("Kembali",Color.WHITE,BLUE),copy=button("Salin Respons",Color.WHITE,BLUE);
        back.setIcon(new LineIcon("back",BLUE,15));back.addActionListener(e->showPage(apiReturnPage));
        copy.addActionListener(e->{try{Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.getText()),null);toast("Respons berhasil disalin.",false);}catch(RuntimeException ex){toast("Clipboard belum tersedia.",true);}});
        controls.add(back);controls.add(Box.createHorizontalStrut(8));controls.add(copy);apiHost.add(controls,BorderLayout.SOUTH);apiHost.revalidate();apiHost.repaint();showPage("api");
    }

    private void setBusy(boolean value,String text){
        busy=value;status.setText(text);reload.setEnabled(!value);close.setEnabled(!value);apiButton.setEnabled(!value);search.setEnabled(!value);
        for(JToggleButton filter:filters)filter.setEnabled(!value);for(JButton action:detailButtons)action.setEnabled(!value);
        for(JButton action:cardButtons)action.setEnabled(!value);
        if(register!=null)register.setEnabled(!value&&currentDetail!=null&&SatuSehatRujukanMasukPasien.allowed());
        if(!value&&currentDetail!=null){SatuSehatRujukanMasukService.Row selected=findRow(currentDetail.taskId);if(selected==null||!selected.accepted())selectReferral(selected);}
    }
    private void failure(Exception ex){Throwable cause=ex instanceof java.util.concurrent.ExecutionException&&ex.getCause()!=null?ex.getCause():ex;toast(or(cause.getMessage(),"Proses rujukan belum berhasil. Periksa Respon API."),true);}
    private void toast(String text,boolean error){notice.setBackground(error?WAIT_BG:LIGHT);message.setText(text);message.setForeground(error?AMBER:BLUE);notice.setVisible(true);toastTimer.restart();revalidate();}

    private JPanel summaryCard(String heading,String[][] values){
        JPanel panel=card(heading,null);
        JPanel fields=new JPanel(new GridLayout(0,2,18,12));fields.setOpaque(false);fields.setAlignmentX(Component.LEFT_ALIGNMENT);
        for(String[] pair:values){
            JPanel field=vertical();field.add(label(pair[0],SMALL,MUTED));field.add(Box.createVerticalStrut(4));
            JTextArea value=readOnly(2);value.setColumns(20);value.setFont(BODY);value.setForeground(INK);value.setText(or(pair[1],"—"));field.add(value);fields.add(field);
        }
        panel.add(fields);return panel;
    }

    private JPanel incomingSummaryPanel(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        Vertical p=new Vertical();
        SatuSehatRujukanMasukService.Row row=findRow(d.taskId);
        String referrer=incomingResourceName(d.referrerOrganization,referenceText(d.task==null?null:d.task.path("requester")));
        String patient=incomingResourceName(d.patient,referenceText(d.task==null?null:d.task.path("for")));
        String nik=incomingIdentifier(d.patient,"nik"),gender=jsonText(d.patient==null?null:d.patient.path("gender"),"—"),birth=jsonText(d.patient==null?null:d.patient.path("birthDate"),"—");
        String kind=row==null?incomingReferralType(d):or(row.kind,incomingReferralType(d));
        p.add(summaryCard("Ringkasan rujukan",new String[][]{
            {"Fasyankes perujuk",referrer},{"Jenis pelayanan",kind},
            {"Waktu rujukan dikirim",d.task==null?"—":formatSentTime(sentTime(d.task))+" WITA"},{"Status / keputusan",jsonText(d.task==null?null:d.task.path("status"),"—")+" / "+SatuSehatRujukanIGDRanapApi.parseDecision(d.task)},
            {"IHS pasien",incomingId(d.patient,"Patient")},{"Nomor Rujukan Nasional",or(d.nationalReferralNumber,"Belum terbit / belum ditemukan")}
        }));
        p.add(Box.createVerticalStrut(12));
        p.add(detailCard("Pasien & kunjungan asal",new String[][]{{"Nama pasien",patient},{"NIK",or(nik,"—")},{"Jenis kelamin / tanggal lahir",gender+" / "+birth},{"Encounter",incomingId(d.encounter,"Encounter")},{"Status / kelas Encounter",incomingEncounterStatus(d.encounter)},{"Periode Encounter",incomingEncounterPeriod(d.encounter)},{"Lokasi / unit",incomingEncounterLocations(d.encounter)}}));
        p.add(Box.createVerticalStrut(12));
        p.add(detailCard("Referensi rujukan",new String[][]{{"Task ID",or(d.taskId,"—")},{"CarePlan ID",or(d.carePlanId,"—")},{"ServiceRequest ID",or(d.serviceRequestId,"Belum dikirim / belum ditemukan")},{"Jenis rujukan",incomingReferralType(d)}}));
        return p;
    }
    private JPanel incomingClinicalPanel(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        Vertical p=new Vertical();StringBuilder dx=new StringBuilder();for(JsonNode c:d.conditions){String x=incomingCondition(c);if(!x.isEmpty()){if(dx.length()>0)dx.append("\n");dx.append("• ").append(x);}}if(dx.length()==0)dx.append("—");
        String reason=incomingCarePlanReason(d.carePlan);String specialty=incomingCarePlanSpeciality(d.carePlan);String author=incomingResourceName(d.authorPractitioner,referenceDisplayText(d.carePlan==null?null:d.carePlan.path("author")));
        String priority=jsonText(d.serviceRequest==null?null:d.serviceRequest.path("priority"),"—"),occ=jsonText(d.serviceRequest==null?null:d.serviceRequest.path("occurrenceDateTime"),"—"),instruction=jsonText(d.serviceRequest==null?null:d.serviceRequest.path("patientInstruction"),"—"),performerType=incomingCoding(d.serviceRequest==null?null:d.serviceRequest.path("performerType"));
        p.add(detailCard("Data klinis rujukan",new String[][]{{"Diagnosis / Condition",dx.toString()},{"Alasan / kebutuhan rujukan",or(reason,"—")},{"Clinical Speciality",or(specialty,"—")},{"Dokter / author CarePlan",author},{"Jenis tenaga pelaksana",or(performerType,"—")},{"Prioritas ServiceRequest",priority},{"Waktu pelayanan / rujukan",occ},{"Instruksi pasien",instruction}}));
        if(!d.warnings.isEmpty()){p.add(Box.createVerticalStrut(12));p.add(detailCard("Catatan penelusuran",new String[][]{{"Catatan",joinLines(d.warnings)}}));}return p;
    }

    /**
     * Tampilan CarePlan dibuat eksplisit + fallback seluruh path FHIR. Tujuannya agar
     * data yang sudah ada pada JSON tidak lagi hilang hanya karena dikirim pada elemen
     * CarePlan yang berbeda oleh RME/fasyankes lain.
     */
    private JPanel incomingCarePlanPanel(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        Vertical p=new Vertical();final JsonNode cp=d==null?null:d.carePlan;
        if(cp==null||cp.isMissingNode()||cp.isNull()){
            p.add(detailCard("CarePlan",new String[][]{{"Status","CarePlan belum dapat dimuat dari Task.basedOn maupun ServiceRequest.basedOn."}}));return p;
        }
        String categories=incomingCodingList(cp.path("category"));
        String period=incomingPeriod(cp.path("period"));
        String contributor=incomingReferenceList(cp.path("contributor"));
        String addresses=incomingReferenceList(cp.path("addresses"));
        String supporting=incomingReferenceList(cp.path("supportingInfo"));
        String goals=incomingReferenceList(cp.path("goal"));
        String notes=incomingAnnotationList(cp.path("note"));
        p.add(detailCard("Ringkasan CarePlan",new String[][]{
            {"CarePlan ID",incomingId(cp,"CarePlan")},{"Status",jsonText(cp.path("status"),"—")},{"Intent",jsonText(cp.path("intent"),"—")},
            {"Judul",jsonText(cp.path("title"),"—")},{"Kategori",or(categories,"—")},{"Deskripsi",jsonText(cp.path("description"),"—")},
            {"Subject",referenceDisplayText(cp.path("subject"))},{"Encounter",referenceDisplayText(cp.path("encounter"))},{"Periode",or(period,"—")},
            {"Dibuat",jsonText(cp.path("created"),"—")},{"Author",referenceDisplayText(cp.path("author"))},{"Contributor / Fasyankes perujuk",or(contributor,"—")},
            {"Addresses / diagnosis",or(addresses,"—")},{"Supporting info",or(supporting,"—")},{"Goal",or(goals,"—")},{"Catatan",or(notes,"—")}
        }));

        JsonNode activities=cp.path("activity");
        if(activities.isArray()&&activities.size()>0){
            p.add(Box.createVerticalStrut(12));
            DefaultTableModel am=model(new String[]{"No","Jenis / Kode","Status","Deskripsi","Alasan","Pelaksana / Lokasi","Jadwal / Referensi"});
            int n=1;for(JsonNode a:activities){JsonNode detail=a.path("detail");
                String kind=jsonText(detail.path("kind"),"");String code=incomingCoding(detail.path("code"));String kindCode=joinNonEmpty(" • ",kind,code);
                String status=jsonText(detail.path("status"),"");String desc=jsonText(detail.path("description"),"");
                String reasons=joinNonEmpty("\n",incomingCodingList(detail.path("reasonCode")),incomingReferenceList(detail.path("reasonReference")));
                String performer=joinNonEmpty("\n",incomingReferenceList(detail.path("performer")),referenceDisplayText(detail.path("location")));
                String schedule=incomingActivitySchedule(detail);String ref=referenceDisplayText(a.path("reference"));String schedRef=joinNonEmpty("\n",schedule,ref);
                String progress=incomingAnnotationList(a.path("progress"));if(!progress.isEmpty())desc=joinNonEmpty("\n",desc,"Progress: "+progress);
                String outcome=joinNonEmpty("\n",incomingCodingList(a.path("outcomeCodeableConcept")),incomingReferenceList(a.path("outcomeReference")));if(!outcome.isEmpty())desc=joinNonEmpty("\n",desc,"Outcome: "+outcome);
                am.addRow(new Object[]{String.valueOf(n++),or(kindCode,"—"),or(status,"—"),or(desc,"—"),or(reasons,"—"),or(performer,"—"),or(schedRef,"—")});
            }
            JTable at=workflowTable(am);at.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);fitTableColumns(at,18,0);JScrollPane asp=scroll(at);asp.setPreferredSize(new Dimension(980,Math.min(260,60+activities.size()*28)));JPanel ac=card("Aktivitas CarePlan","Seluruh CarePlan.activity dibaca, bukan hanya activity[0].detail.code.");ac.add(asp);p.add(ac);
        }

        p.add(Box.createVerticalStrut(12));
        final List<String[]> flat=new ArrayList<String[]>();flattenFhirForDisplay(cp,"CarePlan",flat,0);
        DefaultTableModel fm=model(new String[]{"Path FHIR","Nilai"});for(String[] row:flat)fm.addRow(new Object[]{row[0],row[1]});
        JTable ft=workflowTable(fm);ft.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);if(ft.getColumnModel().getColumnCount()>0)ft.getColumnModel().getColumn(0).setPreferredWidth(420);JScrollPane fsp=scroll(ft);fsp.setPreferredSize(new Dimension(980,320));
        JPanel all=card("Semua elemen CarePlan","Fallback lengkap: setiap nilai non-kosong yang ada di JSON CarePlan ditampilkan di tabel ini. Jadi bila data ada di JSON, data tersebut tidak boleh lagi berubah menjadi tanda — pada tab CarePlan.");all.add(fsp);p.add(all);return p;
    }
    private JPanel incomingResourcePanel(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        DefaultTableModel m=model(new String[]{"Peran","Resource / Referensi","Keterangan","Status"});for(SatuSehatRujukanIGDRanapApi.IncomingReferencedResource x:d.references)m.addRow(new Object[]{x.role,x.reference,or(x.display,"—"),x.error==null||x.error.isEmpty()?"Tersedia":"Gagal: "+x.error});
        JTable t=workflowTable(m);t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);fitTableColumns(t,18,0);JPanel p=new JPanel(new BorderLayout());p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));p.add(scroll(t),BorderLayout.CENTER);return p;
    }
    private JPanel incomingJsonPanel(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        final String text=incomingRawJson(d);JPanel p=new JPanel(new BorderLayout(0,8));p.setBackground(Color.WHITE);JButton copy=button("Salin Semua JSON",Color.WHITE,BLUE);copy.setIcon(new LineIcon("copy",BLUE,14));copy.addActionListener(e->{try{Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text),null);toast("JSON detail rujukan berhasil disalin.",false);}catch(RuntimeException ex){toast("Clipboard sedang tidak tersedia.",true);}});JPanel top=flow();top.add(copy);p.add(top,BorderLayout.NORTH);
        JTextArea a=new JTextArea(text);a.setEditable(false);a.setFont(new Font("Consolas",Font.PLAIN,12));a.setForeground(INK);a.setBackground(Color.WHITE);a.setCaretPosition(0);p.add(scroll(a),BorderLayout.CENTER);return p;
    }
    private JPanel detailCard(String titleText,String[][] rows){JPanel c=card(titleText,null);JPanel g=new JPanel(new GridBagLayout());g.setOpaque(false);g.setAlignmentX(Component.LEFT_ALIGNMENT);int y=0;for(String[] row:rows){JLabel k=label(row[0],SMALL,MUTED);GridBagConstraints a=new GridBagConstraints();a.gridx=0;a.gridy=y;a.weightx=0;a.anchor=GridBagConstraints.NORTHWEST;a.insets=new Insets(5,0,5,18);g.add(k,a);JTextArea v=readOnly(Math.max(1,countLines(row[1])));v.setText(or(row[1],"—"));v.setForeground(INK);v.setFont(BODY);GridBagConstraints b=new GridBagConstraints();b.gridx=1;b.gridy=y;b.weightx=1;b.fill=GridBagConstraints.HORIZONTAL;b.anchor=GridBagConstraints.NORTHWEST;b.insets=new Insets(5,0,5,0);g.add(v,b);y++;}c.add(g);return c;}
    private static int countLines(String s){if(s==null||s.isEmpty())return 1;int n=1;for(int i=0;i<s.length();i++)if(s.charAt(i)=='\n')n++;return Math.min(5,n);}
    private static String joinLines(List<String> rows){StringBuilder b=new StringBuilder();if(rows!=null)for(String s:rows)if(s!=null&&!s.trim().isEmpty()){if(b.length()>0)b.append('\n');b.append("• ").append(s);}return b.toString();}
    private static String incomingId(JsonNode r,String type){if(r==null)return "—";String id=jsonText(r.path("id"),"");return id.isEmpty()?"—":(type==null||type.isEmpty()?id:type+"/"+id);}
    private static String incomingResourceName(JsonNode r,String fallback){if(r==null)return or(fallback,"—");String rt=jsonText(r.path("resourceType"),"");if("Organization".equals(rt)){String n=jsonText(r.path("name"),"");return n.isEmpty()?or(fallback,"—"):n+" (Organization/"+jsonText(r.path("id"),"—")+")";}JsonNode names=r.path("name");if(names.isArray()&&names.size()>0){JsonNode n=names.get(0);String text=jsonText(n.path("text"),"");if(!text.isEmpty())return text;StringBuilder b=new StringBuilder();JsonNode given=n.path("given");if(given.isArray())for(JsonNode x:given){String q=jsonText(x,"");if(!q.isEmpty()){if(b.length()>0)b.append(' ');b.append(q);}}String family=jsonText(n.path("family"),"");if(!family.isEmpty()){if(b.length()>0)b.append(' ');b.append(family);}if(b.length()>0)return b.toString();}return or(fallback,"—");}
    private static String incomingIdentifier(JsonNode r,String hint){if(r==null)return "";JsonNode ids=r.path("identifier");if(!ids.isArray())return "";String fallback="";for(JsonNode id:ids){String sys=jsonText(id.path("system"),"").toLowerCase(java.util.Locale.ENGLISH),v=jsonText(id.path("value"),"");if(v.isEmpty())continue;if(fallback.isEmpty())fallback=v;if(hint!=null&&!hint.isEmpty()&&sys.contains(hint.toLowerCase(java.util.Locale.ENGLISH)))return v;if("nik".equalsIgnoreCase(hint)&&v.matches("\\d{16}"))return v;}return "nik".equalsIgnoreCase(hint)?"":fallback;}
    private static String incomingReferralType(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){String x=incomingCoding(d.serviceRequest==null?null:d.serviceRequest.path("code"));if(!x.isEmpty())return x;JsonNode cats=d.carePlan==null?null:d.carePlan.path("category");if(cats!=null&&cats.isArray())for(JsonNode c:cats){String s=incomingCoding(c);if(!s.isEmpty()&&!s.toLowerCase(java.util.Locale.ENGLISH).contains("patient referral"))return s;}return "—";}
    private static String incomingEncounterStatus(JsonNode e){if(e==null)return "—";String status=jsonText(e.path("status"),"—"),code=jsonText(e.path("class").path("code"),""),display=jsonText(e.path("class").path("display"),"");return status+(code.isEmpty()?"":" / "+code+(display.isEmpty()?"":" - "+display));}
    private static String incomingEncounterPeriod(JsonNode e){if(e==null)return "—";String a=jsonText(e.path("period").path("start"),""),b=jsonText(e.path("period").path("end"),"");return a.isEmpty()&&b.isEmpty()?"—":or(a,"?")+" s/d "+or(b,"masih berlangsung");}
    private static String incomingEncounterLocations(JsonNode e){if(e==null)return "—";StringBuilder b=new StringBuilder();JsonNode a=e.path("location");if(a.isArray())for(JsonNode x:a){String d=jsonText(x.path("location").path("display"),referenceText(x.path("location")));if(!d.isEmpty()){if(b.length()>0)b.append(", ");b.append(d);}}String sp=jsonText(e.path("serviceProvider").path("display"),"");if(!sp.isEmpty()){if(b.length()>0)b.append(" • ");b.append(sp);}return b.length()==0?"—":b.toString();}
    private static String incomingCondition(JsonNode c){if(c==null)return "";String code=incomingCoding(c.path("code"));return code.isEmpty()?incomingId(c,"Condition"):code;}
    private static String incomingCarePlanReason(JsonNode cp){
        if(cp==null)return "";StringBuilder b=new StringBuilder();appendUniqueLine(b,jsonText(cp.path("description"),""));appendUniqueLine(b,incomingAnnotationList(cp.path("note")));
        JsonNode a=cp.path("activity");if(a.isArray())for(JsonNode x:a){JsonNode d=x.path("detail");appendUniqueLine(b,jsonText(d.path("description"),""));appendUniqueLine(b,incomingCodingList(d.path("reasonCode")));appendUniqueLine(b,incomingReferenceList(d.path("reasonReference")));}
        return b.toString();
    }
    private static String incomingCarePlanSpeciality(JsonNode cp){
        if(cp==null)return "";String fallback="";JsonNode a=cp.path("activity");if(a.isArray())for(JsonNode x:a){JsonNode cc=x.path("detail").path("code");JsonNode codings=cc.path("coding");if(codings.isArray())for(JsonNode c:codings){String system=jsonText(c.path("system"),"").toLowerCase(java.util.Locale.ENGLISH);String value=incomingCodingSingle(c);if(fallback.isEmpty()&&!value.isEmpty())fallback=value;if(system.contains("clinical-speciality")&&!value.isEmpty())return value;}String s=incomingCoding(cc);if(fallback.isEmpty()&&!s.isEmpty())fallback=s;}return fallback;
    }
    private static String incomingCodingSingle(JsonNode c){if(c==null)return "";String code=jsonText(c.path("code"),""),display=jsonText(c.path("display"),"");if(!code.isEmpty()&&!display.isEmpty())return code+" - "+display;if(!display.isEmpty())return display;return code;}
    private static String incomingCoding(JsonNode cc){if(cc==null||cc.isMissingNode()||cc.isNull())return "";if(cc.has("code")&&!cc.has("coding"))return incomingCodingSingle(cc);String text=jsonText(cc.path("text"),"");JsonNode a=cc.path("coding");if(a.isArray()&&a.size()>0){StringBuilder b=new StringBuilder();for(JsonNode c:a){String v=incomingCodingSingle(c);if(!v.isEmpty()){if(b.length()>0)b.append("; ");b.append(v);}}if(b.length()>0)return b.toString();}return text;}
    private static String incomingCodingList(JsonNode values){if(values==null||values.isMissingNode()||values.isNull())return "";StringBuilder b=new StringBuilder();if(values.isArray()){for(JsonNode v:values)appendUniqueLine(b,incomingCoding(v));}else appendUniqueLine(b,incomingCoding(values));return b.toString();}
    private static String referenceDisplayText(JsonNode ref){if(ref==null||ref.isMissingNode()||ref.isNull())return "—";String display=jsonText(ref.path("display"),""),reference=jsonText(ref.path("reference"),"");if(!display.isEmpty()&&!reference.isEmpty())return display+" ("+reference+")";if(!display.isEmpty())return display;if(!reference.isEmpty())return reference;return "—";}
    private static String incomingReferenceList(JsonNode refs){if(refs==null||refs.isMissingNode()||refs.isNull())return "";StringBuilder b=new StringBuilder();if(refs.isArray()){for(JsonNode r:refs){String v=referenceDisplayText(r);if(!"—".equals(v))appendUniqueLine(b,v);}}else{String v=referenceDisplayText(refs);if(!"—".equals(v))appendUniqueLine(b,v);}return b.toString();}
    private static String incomingAnnotationList(JsonNode notes){if(notes==null||notes.isMissingNode()||notes.isNull())return "";StringBuilder b=new StringBuilder();if(notes.isArray()){for(JsonNode n:notes){String t=jsonText(n.path("text"),n.isValueNode()?n.asText():"");appendUniqueLine(b,t);}}else appendUniqueLine(b,jsonText(notes.path("text"),notes.isValueNode()?notes.asText():""));return b.toString();}
    private static String incomingPeriod(JsonNode p){if(p==null||p.isMissingNode()||p.isNull())return "";String a=jsonText(p.path("start"),""),z=jsonText(p.path("end"),"");if(a.isEmpty()&&z.isEmpty())return "";return or(a,"?")+" s/d "+or(z,"masih berlangsung");}
    private static String incomingActivitySchedule(JsonNode d){if(d==null)return "";String s=jsonText(d.path("scheduledString"),"");if(!s.isEmpty())return s;String period=incomingPeriod(d.path("scheduledPeriod"));if(!period.isEmpty())return period;JsonNode timing=d.path("scheduledTiming");if(!timing.isMissingNode()&&!timing.isNull()){StringBuilder b=new StringBuilder();JsonNode event=timing.path("event");if(event.isArray())for(JsonNode e:event)appendUniqueLine(b,e.asText());String code=incomingCoding(timing.path("code"));appendUniqueLine(b,code);if(b.length()>0)return b.toString();return timing.toString();}return "";}
    private static String joinNonEmpty(String sep,String... values){StringBuilder b=new StringBuilder();if(values!=null)for(String v:values)if(v!=null&&!v.trim().isEmpty()&&!"—".equals(v.trim())){if(b.length()>0)b.append(sep);b.append(v.trim());}return b.toString();}
    private static void appendUniqueLine(StringBuilder b,String value){if(b==null||value==null)return;String v=value.trim();if(v.isEmpty()||"—".equals(v))return;String current=b.toString();for(String line:v.split("\n")){String q=line.trim();if(q.isEmpty())continue;boolean exists=false;for(String old:current.split("\n"))if(old.trim().equals(q)){exists=true;break;}if(!exists){if(b.length()>0)b.append('\n');b.append(q);current=b.toString();}}}
    private static void flattenFhirForDisplay(JsonNode node,String path,List<String[]> out,int depth){
        if(node==null||out==null||node.isMissingNode()||node.isNull()||depth>24)return;
        if(node.isValueNode()){String v=node.asText();if(v!=null&&!v.trim().isEmpty())out.add(new String[]{path,v});return;}
        if(node.isArray()){int i=0;for(JsonNode x:node)flattenFhirForDisplay(x,path+"["+(i++)+"]",out,depth+1);return;}
        java.util.Iterator<Map.Entry<String,JsonNode>> it=node.fields();while(it.hasNext()){Map.Entry<String,JsonNode> e=it.next();flattenFhirForDisplay(e.getValue(),path+"."+e.getKey(),out,depth+1);}
    }
    private static String incomingRawJson(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){StringBuilder b=new StringBuilder();appendJsonSection(b,"TASK",d.task);appendJsonSection(b,"CAREPLAN",d.carePlan);appendJsonSection(b,"SERVICE REQUEST",d.serviceRequest);appendJsonSection(b,"PATIENT",d.patient);appendJsonSection(b,"ENCOUNTER",d.encounter);appendJsonSection(b,"ORGANIZATION PERUJUK",d.referrerOrganization);appendJsonSection(b,"PRACTITIONER / AUTHOR",d.authorPractitioner);int i=1;for(JsonNode c:d.conditions)appendJsonSection(b,"CONDITION "+(i++),c);for(SatuSehatRujukanIGDRanapApi.IncomingReferencedResource x:d.references)if(x.resource!=null&&!containsJsonResource(d,x.resource))appendJsonSection(b,x.role.toUpperCase(java.util.Locale.ENGLISH),x.resource);return b.toString();}
    private static boolean containsJsonResource(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d,JsonNode r){if(r==null)return true;if(r==d.task||r==d.carePlan||r==d.serviceRequest||r==d.patient||r==d.encounter||r==d.referrerOrganization||r==d.authorPractitioner)return true;for(JsonNode c:d.conditions)if(c==r)return true;return false;}
    private static void appendJsonSection(StringBuilder b,String title,JsonNode n){if(n==null)return;if(b.length()>0)b.append("\n\n");b.append("===== ").append(title).append(" =====\n");b.append(prettyJson(n.toString()));}

    private static String referenceText(JsonNode ref){if(ref==null)return "";String display=ref.path("display").asText(),reference=ref.path("reference").asText();return display.isEmpty()?reference:display+" ("+reference+")";}
    private static String jsonText(JsonNode n,String fallback){return n==null||n.isMissingNode()||n.isNull()||n.asText().trim().isEmpty()?fallback:n.asText();}
    private static String prettyJson(String raw){if(raw==null||raw.trim().isEmpty())return "";try{com.fasterxml.jackson.databind.ObjectMapper m=new com.fasterxml.jackson.databind.ObjectMapper();return m.writerWithDefaultPrettyPrinter().writeValueAsString(m.readTree(raw));}catch(Exception ex){return raw;}}
    private static String or(String s,String fallback){return s==null||s.trim().isEmpty()?fallback:s;}
    private static JLabel label(String s,Font f,Color c){JLabel l=new JLabel(s);l.putClientProperty("html.disable",Boolean.TRUE);l.setFont(f);l.setForeground(c);l.setAlignmentX(0);return l;}
    private static JLabel pill(String s,final Color bg,Color fg){JLabel l=new JLabel(s){@Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(bg);p.fillRoundRect(0,0,getWidth(),getHeight(),10,10);p.dispose();super.paintComponent(g);}};l.setFont(SMALL);l.setForeground(fg);l.setAlignmentX(0);l.setBorder(BorderFactory.createEmptyBorder(7,11,7,11));return l;}
    private static JPanel vertical(){JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setOpaque(false);p.setAlignmentX(0);return p;}
    private static JPanel flow(){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));p.setOpaque(false);p.setAlignmentX(0);return p;}
    private static JPanel grid(){JPanel p=new JPanel(new GridBagLayout());p.setOpaque(false);p.setAlignmentX(0);return p;}
    private static JPanel card(String title,String sub){RoundPanel p=new RoundPanel(Color.WHITE,LINE,18);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setAlignmentX(0);p.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        if(title!=null){p.add(label(title,STRONG,INK));p.add(Box.createVerticalStrut(7));}if(sub!=null){JTextArea t=readOnly(1);t.setText(sub);p.add(t);p.add(Box.createVerticalStrut(7));}return p;}
    private static JTextArea readOnly(int rows){JTextArea t=new JTextArea(rows,1){@Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}};t.setEditable(false);t.setFont(SMALL);t.setForeground(MUTED);t.setOpaque(false);t.setLineWrap(true);t.setWrapStyleWord(true);t.setAlignmentX(0);return t;}
    private static JPanel note(String s,Color bg,Color fg){RoundPanel p=new RoundPanel(bg,bg,12);p.setLayout(new BorderLayout());p.setBorder(BorderFactory.createEmptyBorder(11,12,11,12));JTextArea t=readOnly(3);t.setText(s);t.setForeground(fg);p.add(t);p.setAlignmentX(0);return p;}
    private static JPanel empty(String icon,String title,String message){JPanel p=vertical();p.setBorder(BorderFactory.createEmptyBorder(28,20,28,20));JLabel i=new JLabel(new LineIcon(icon,BLUE,36));i.setAlignmentX(.5f);p.add(i);p.add(Box.createVerticalStrut(14));JLabel l=label(title,STRONG,INK);l.setAlignmentX(.5f);p.add(l);p.add(Box.createVerticalStrut(12));JTextArea t=readOnly(3);t.setText(message);p.add(t);return p;}
    private static JButton button(String text,final Color bg,Color fg){JButton b=new JButton(text);b.setFont(BODY);b.setForeground(fg);b.setBackground(bg);b.setOpaque(false);b.setContentAreaFilled(false);b.setAlignmentX(0);
        b.setBorder(BorderFactory.createEmptyBorder(9,13,9,13));b.setUI(new BasicButtonUI(){@Override public void paint(Graphics g,JComponent c){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(c.isEnabled()?bg:new Color(232,238,243));p.fillRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,10,10);p.setColor(c.hasFocus()?BLUE:BLUE.equals(bg)?BLUE:BLUE.equals(fg)?new Color(185,208,239):RED.equals(fg)?new Color(238,207,212):LINE);p.drawRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,10,10);p.dispose();super.paint(g,c);}
            @Override protected void paintText(Graphics g,AbstractButton b,Rectangle r,String text){if(b.isEnabled())super.paintText(g,b,r,text);else{g.setColor(MUTED);g.drawString(text,r.x,r.y+g.getFontMetrics().getAscent());}}
        });b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private static DefaultTableModel model(String[] headers){return new DefaultTableModel(headers,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    private static JScrollPane scroll(Component c){JScrollPane s=new JScrollPane(c);s.setBorder(BorderFactory.createEmptyBorder());s.getViewport().setBackground(c instanceof JTable?Color.WHITE:BG);s.getVerticalScrollBar().setUnitIncrement(18);s.getVerticalScrollBar().setPreferredSize(new Dimension(9,0));
        s.getVerticalScrollBar().setUI(new BasicScrollBarUI(){@Override protected void configureScrollBarColors(){thumbColor=new Color(181,199,214);trackColor=BG;}@Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}@Override protected JButton createIncreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}});s.setAlignmentX(0);return s;}
    private static final class Vertical extends JPanel implements Scrollable {
        Vertical(){setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setOpaque(false);}
        public Dimension getPreferredScrollableViewportSize(){return new Dimension(1000,650);}
        public int getScrollableUnitIncrement(Rectangle r,int o,int d){return 18;}
        public int getScrollableBlockIncrement(Rectangle r,int o,int d){return Math.max(18,r.height-30);}
        public boolean getScrollableTracksViewportWidth(){return true;}public boolean getScrollableTracksViewportHeight(){return false;}
    }
    private static final class RoundPanel extends JPanel {
        final Color fill,stroke;final int radius;RoundPanel(Color fill,Color stroke,int radius){this.fill=fill;this.stroke=stroke;this.radius=radius;setOpaque(false);}
        @Override protected void paintComponent(Graphics g){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(fill);p.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);p.setColor(stroke);p.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);p.dispose();super.paintComponent(g);}
    }
    private static final class SearchField extends JTextField {
        SearchField(){
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(2,35,2,11));
            addFocusListener(new FocusAdapter(){
                @Override public void focusGained(FocusEvent e){repaint();}
                @Override public void focusLost(FocusEvent e){repaint();}
            });
        }
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
            g.setColor(hasFocus()?BLUE:LINE);
            g.setStroke(new BasicStroke(hasFocus()?2f:1f));
            g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
            g.dispose();
            super.paintComponent(graphics);
            new LineIcon("search",MUTED,16).paintIcon(this,graphics,11,(getHeight()-16)/2);
            if(getText().isEmpty()&&!hasFocus()){
                Graphics2D text=(Graphics2D)graphics.create();text.setColor(MUTED);text.setFont(getFont());
                String hint="Cari pasien, IHS, atau rumah sakit...";FontMetrics metrics=text.getFontMetrics();
                if(metrics.stringWidth(hint)>getWidth()-47)hint="Cari rujukan...";
                text.drawString(hint,35,(getHeight()-metrics.getHeight())/2+metrics.getAscent());text.dispose();
            }
        }
    }
    private static final class LineIcon implements Icon {
        final String kind;final Color color;final int size;LineIcon(String kind,Color color,int size){this.kind=kind;this.color=color;this.size=size;}
        public int getIconWidth(){return size;}public int getIconHeight(){return size;}
        public void paintIcon(Component c,Graphics graphics,int x,int y){Graphics2D g=(Graphics2D)graphics.create();g.translate(x,y);g.scale(size/24.0,size/24.0);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(color);g.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if("hospital".equals(kind)){g.drawRoundRect(4,3,16,19,3,3);g.drawLine(9,8,15,8);g.drawLine(12,5,12,11);g.drawRect(10,16,4,6);}
            else if("check".equals(kind)){g.drawLine(4,12,10,18);g.drawLine(10,18,21,5);}
            else if("reject".equals(kind)){g.drawLine(5,5,19,19);g.drawLine(5,19,19,5);}
            else if("user".equals(kind)){g.drawOval(8,3,8,8);g.drawArc(4,13,16,15,0,180);}
            else if("send".equals(kind)){g.drawLine(3,4,22,12);g.drawLine(22,12,3,21);g.drawLine(3,21,6,12);g.drawLine(6,12,3,4);g.drawLine(6,12,22,12);}
            else if("inbox".equals(kind)){g.drawRoundRect(3,5,18,16,3,3);g.drawLine(3,14,8,14);g.drawLine(8,14,10,17);g.drawLine(10,17,14,17);g.drawLine(14,17,16,14);g.drawLine(16,14,21,14);}
            else if("history".equals(kind)){g.drawArc(3,3,18,18,-40,310);g.drawLine(12,7,12,12);g.drawLine(12,12,16,14);g.drawLine(3,2,3,8);g.drawLine(3,8,8,8);}
            else if("search".equals(kind)){g.drawOval(3,3,13,13);g.drawLine(15,15,22,22);}
            else if("reload".equals(kind)){g.drawArc(4,4,16,16,40,290);g.drawLine(20,3,20,9);g.drawLine(20,9,14,9);}
            else if("clock".equals(kind)){g.drawOval(3,3,18,18);g.drawLine(12,6,12,12);g.drawLine(12,12,16,14);}
            else if("back".equals(kind)){g.drawLine(10,5,3,12);g.drawLine(3,12,10,19);g.drawLine(3,12,21,12);}
            else if("exit".equals(kind)){g.drawLine(10,3,3,3);g.drawLine(3,3,3,21);g.drawLine(3,21,10,21);g.drawLine(9,12,21,12);g.drawLine(16,7,21,12);g.drawLine(21,12,16,17);}
            else if("copy".equals(kind)){g.drawRoundRect(7,7,13,14,2,2);g.drawLine(4,16,4,3);g.drawLine(4,3,16,3);}
            else if("print".equals(kind)){g.drawRect(6,3,12,6);g.drawRoundRect(3,9,18,9,2,2);g.drawRect(6,14,12,7);g.drawLine(17,12,18,12);}
            else if("save".equals(kind)){g.drawRoundRect(3,3,18,18,2,2);g.drawRect(7,3,10,6);g.drawRect(7,14,10,7);}
            else if("plus".equals(kind)){g.drawOval(4,4,16,16);g.drawLine(12,8,12,16);g.drawLine(8,12,16,12);}
            else if("eye".equals(kind)){g.drawOval(7,8,10,8);g.drawArc(2,5,20,14,15,150);g.drawArc(2,5,20,14,195,150);g.fillOval(10,10,4,4);}
            else{g.drawLine(8,6,2,12);g.drawLine(2,12,8,18);g.drawLine(16,6,22,12);g.drawLine(22,12,16,18);}g.dispose();}
    }
    private static JTable workflowTable(DefaultTableModel model){JTable t=new JTable(model);t.setFont(BODY);t.setRowHeight(32);t.setGridColor(LINE);t.setShowVerticalLines(false);t.setFillsViewportHeight(true);t.setSelectionBackground(BLUE);t.setSelectionForeground(Color.WHITE);t.getTableHeader().setFont(STRONG);t.getTableHeader().setPreferredSize(new Dimension(100,38));t.setAutoCreateRowSorter(true);return t;}
    private static void fitTableColumns(JTable table,int padding,int maxWidth){for(int c=0;c<table.getColumnCount();c++){int width=table.getFontMetrics(STRONG).stringWidth(table.getColumnName(c))+padding;for(int r=0;r<table.getRowCount();r++)width=Math.max(width,table.getFontMetrics(BODY).stringWidth(String.valueOf(table.getValueAt(r,c)))+padding);table.getColumnModel().getColumn(c).setPreferredWidth(Math.min(maxWidth>0?maxWidth:520,Math.max(80,width)));}}
}
