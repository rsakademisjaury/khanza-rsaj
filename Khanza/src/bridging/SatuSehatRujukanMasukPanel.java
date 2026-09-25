package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Inbox berdiri sendiri. Daftar, detail dan respons API memakai satu CardLayout. */
public final class SatuSehatRujukanMasukPanel extends JPanel {
    private static final Color INK=new Color(25,49,69),MUTED=new Color(91,113,131),BLUE=new Color(10,112,182),BG=new Color(244,247,251),LINE=new Color(219,229,238),LIGHT=new Color(233,244,252),GREEN=new Color(26,111,79),AMBER=new Color(142,86,16);
    private static final Font BODY=new Font("Segoe UI",Font.PLAIN,13),SMALL=new Font("Segoe UI",Font.PLAIN,12),STRONG=new Font("Segoe UI Semibold",Font.BOLD,14);
    private final SatuSehatRujukanMasukService service;
    private final SatuSehatRujukanIGDRanapApi apiClient=new SatuSehatRujukanIGDRanapApi();
    private final JPanel pages=new JPanel(new CardLayout()),detailHost=new JPanel(new BorderLayout(0,12)),apiHost=new JPanel(new BorderLayout(0,10));
    private final JLabel title=label("Rujukan Masuk",new Font("Segoe UI Semibold",Font.BOLD,23),INK),status=label("Memuat rujukan masuk...",SMALL,MUTED);
    private final JTextArea message=readOnly(2);private final JPanel notice=new JPanel(new BorderLayout());
    private final Timer toastTimer=new Timer(6500,e->notice.setVisible(false));
    private final List<SatuSehatRujukanMasukService.Row> rows=new ArrayList<SatuSehatRujukanMasukService.Row>();
    private final DefaultTableModel tableModel=new DefaultTableModel(new String[]{"No.","Task ID","Pasien","Fasyankes perujuk","Jenis","Status","Keputusan","Aksi"},0){
        @Override public boolean isCellEditable(int r,int c){return c==7&&!busy;}
    };
    private final JTable table=workflowTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<DefaultTableModel>(tableModel);
    private final JTextField search=new JTextField();
    private final JButton reload=button("Perbarui",Color.WHITE,BLUE),close=button("Keluar",Color.WHITE,MUTED);
    private JButton register;private JLabel registrationStatus;
    private Runnable onClose;private boolean busy,disposed;private long detailGeneration;
    private SatuSehatRujukanIGDRanapApi.IncomingReferralDetail currentDetail;
    private final SatuSehatRujukanMasukService.Listener listener=(snapshot,error,fresh,first)->updateList(snapshot,error);

    public SatuSehatRujukanMasukPanel(SatuSehatRujukanMasukService service){
        this.service=service;setLayout(new BorderLayout(0,12));setBackground(BG);setBorder(BorderFactory.createEmptyBorder(16,20,14,20));
        JPanel top=new JPanel(new BorderLayout(12,10));top.setOpaque(false);
        JPanel head=new JPanel(new BorderLayout(12,0));head.setOpaque(false);head.add(title,BorderLayout.WEST);top.add(head,BorderLayout.NORTH);
        JPanel actions=flow();JButton api=button("Respon API",Color.WHITE,BLUE);api.setIcon(new LineIcon("code",BLUE,16));api.addActionListener(e->showApi());
        reload.setIcon(new LineIcon("reload",BLUE,16));reload.addActionListener(e->{service.refresh();status.setText("Memperbarui daftar rujukan...");});
        close.addActionListener(e->requestClose());actions.add(api);actions.add(Box.createHorizontalStrut(6));actions.add(reload);actions.add(Box.createHorizontalStrut(6));actions.add(close);
        notice.setBackground(LIGHT);notice.setBorder(BorderFactory.createEmptyBorder(9,12,9,12));notice.add(message,BorderLayout.CENTER);notice.setVisible(false);toastTimer.setRepeats(false);top.add(notice,BorderLayout.SOUTH);add(top,BorderLayout.NORTH);
        pages.setOpaque(false);pages.add(listPage(),"list");detailHost.setOpaque(false);apiHost.setOpaque(false);pages.add(detailHost,"detail");pages.add(apiHost,"api");add(pages,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(12,0));footer.setOpaque(false);footer.add(actions,BorderLayout.WEST);footer.add(status,BorderLayout.EAST);add(footer,BorderLayout.SOUTH);
        service.addListener(listener);service.refresh();
    }
    public void setOnClose(Runnable action){onClose=action;}
    public void requestClose(){if(busy){toast("Tunggu proses rujukan selesai sebelum menutup form.",true);return;}if(onClose!=null)onClose.run();}
    public void shutdown(){disposed=true;detailGeneration++;toastTimer.stop();service.removeListener(listener);SatuSehatRujukanMasukPasien.closeFor(this);}
    private JPanel listPage(){
        JPanel p=new JPanel(new BorderLayout(0,12));p.setOpaque(false);
        JPanel bar=new JPanel(new BorderLayout(12,0));bar.setOpaque(false);bar.add(label("Rujukan menunggu keputusan ditampilkan lebih dahulu",BODY,MUTED),BorderLayout.WEST);
        search.setFont(BODY);search.setPreferredSize(new Dimension(340,36));search.setToolTipText("Cari RS, pasien, Task ID, atau jenis rujukan");search.getAccessibleContext().setAccessibleName("Cari rujukan masuk");
        JPanel searchBox=new JPanel(new BorderLayout(8,0));searchBox.setOpaque(false);searchBox.add(label("Cari",SMALL,MUTED),BorderLayout.WEST);searchBox.add(search,BorderLayout.CENTER);bar.add(searchBox,BorderLayout.EAST);p.add(bar,BorderLayout.NORTH);
        table.setName("tabel_rujukan_masuk");table.setRowSorter(sorter);table.setRowHeight(46);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionBackground(BLUE);table.setSelectionForeground(Color.WHITE);
        int[] widths={55,200,210,265,110,120,120,245};for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        DefaultTableCellRenderer numberRenderer=new DefaultTableCellRenderer();numberRenderer.setHorizontalAlignment(SwingConstants.CENTER);table.getColumnModel().getColumn(0).setCellRenderer(numberRenderer);
        TableColumn action=table.getColumnModel().getColumn(7);action.setMinWidth(240);action.setCellRenderer(new ActionCell());action.setCellEditor(new ActionCell());sorter.setSortable(0,false);sorter.setSortable(7,false);
        search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filter();}public void removeUpdate(DocumentEvent e){filter();}public void changedUpdate(DocumentEvent e){filter();}});
        table.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2&&table.getSelectedRow()>=0){int row=table.convertRowIndexToModel(table.getSelectedRow());if(row<rows.size()&&rows.get(row).accepted())loadDetail(rows.get(row).id);}}});
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"rujukanAction");table.getActionMap().put("rujukanAction",new AbstractAction(){@Override public void actionPerformed(java.awt.event.ActionEvent e){int vr=table.getSelectedRow();if(vr<0||busy)return;SatuSehatRujukanMasukService.Row r=rows.get(table.convertRowIndexToModel(vr));if(r.accepted())loadDetail(r.id);else if(r.pending()){int vc=table.convertColumnIndexToView(7);table.editCellAt(vr,vc);Component editor=table.getEditorComponent();if(editor instanceof Container&&((Container)editor).getComponentCount()>0)((Container)editor).getComponent(0).requestFocusInWindow();}}});
        JScrollPane listScroll=scroll(table);listScroll.setColumnHeaderView(table.getTableHeader());p.add(listScroll,BorderLayout.CENTER);return p;
    }
    private void filter(){final String key=search.getText().trim().toLowerCase(java.util.Locale.ROOT);sorter.setRowFilter(key.isEmpty()?null:new RowFilter<DefaultTableModel,Integer>(){@Override public boolean include(Entry<? extends DefaultTableModel,? extends Integer> e){for(int i=1;i<=6;i++)if(String.valueOf(e.getValue(i)).toLowerCase(java.util.Locale.ROOT).contains(key))return true;return false;}});}
    private void updateList(SatuSehatRujukanMasukService.Snapshot snapshot,String error){
        if(disposed)return;if(table.isEditing())table.getCellEditor().cancelCellEditing();
        if(snapshot!=null){String selected="";int vr=table.getSelectedRow();if(vr>=0){int mr=table.convertRowIndexToModel(vr);if(mr<rows.size())selected=rows.get(mr).id;}
            rows.clear();rows.addAll(snapshot.rows);tableModel.setRowCount(0);
            for(int i=0;i<rows.size();i++){SatuSehatRujukanMasukService.Row r=rows.get(i);tableModel.addRow(new Object[]{i+1,r.id,r.patient,r.referrer,r.kind,r.status,r.decision.isEmpty()?(r.pending()?"Menunggu":"Belum ada keputusan"):r.accepted()?"Diterima":"Ditolak",r.id});}
            for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(selected)){int view=table.convertRowIndexToView(i);if(view>=0)table.setRowSelectionInterval(view,view);break;}
            if(!busy)status.setText(snapshot.pending+" menunggu keputusan • "+rows.size()+" rujukan • Diperbarui "+new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(snapshot.time)));
        }
        if(error!=null&&!error.isEmpty())toast(error,true);
    }
    private final class ActionCell extends AbstractCellEditor implements TableCellRenderer,TableCellEditor {
        private String id="";
        private JPanel cell(int modelRow,boolean editor,boolean selected){
            JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,6,5)){
                @Override public void validate(){doLayout();}
            };p.setBackground(selected?BLUE:Color.WHITE);
            if(modelRow<0||modelRow>=rows.size())return p;final SatuSehatRujukanMasukService.Row r=rows.get(modelRow);id=r.id;
            if(r.pending()){addAction(p,"Terima","check",GREEN,editor,()->respond(r.id,true));addAction(p,"Tolak","reject",AMBER,editor,()->respond(r.id,false));}
            else if(r.accepted())addAction(p,"Lihat Detail","eye",BLUE,editor,()->loadDetail(r.id));
            return p;
        }
        private void addAction(JPanel p,String text,String icon,Color color,boolean editor,Runnable action){JButton b=button(text,Color.WHITE,color);b.setIcon(new LineIcon(icon,color,15));b.setBorder(BorderFactory.createEmptyBorder(6,9,6,9));b.setEnabled(!busy);if(editor)b.addActionListener(e->{fireEditingStopped();action.run();});p.add(b);}
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean selected,boolean focus,int row,int col){return cell(t.convertRowIndexToModel(row),false,selected);}
        @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean selected,int row,int col){JPanel p=cell(t.convertRowIndexToModel(row),true,true);p.setSize(t.getColumnModel().getColumn(col).getWidth(),t.getRowHeight(row));p.doLayout();return p;}
        @Override public Object getCellEditorValue(){return id;}
    }
    private void respond(final String taskId,final boolean accepted){
        if(busy||disposed)return;SatuSehatRujukanMasukService.Row selected=findRow(taskId);if(selected==null||!selected.pending())return;
        if(JOptionPane.showConfirmDialog(this,(accepted?"Terima":"Tolak")+" rujukan untuk "+selected.patient+" dari "+selected.referrer+"?","Konfirmasi rujukan",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        setBusy(true,(accepted?"Menerima":"Menolak")+" rujukan...");
        new SwingWorker<JsonNode,Void>(){@Override protected JsonNode doInBackground()throws Exception{return apiClient.responRujukanMasuk(taskId,accepted);}
            @Override protected void done(){if(disposed)return;try{JsonNode confirmed=get();for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(taskId)){SatuSehatRujukanMasukService.Row old=rows.get(i);rows.set(i,new SatuSehatRujukanMasukService.Row(confirmed,old.patient,old.referrer,old.kind));tableModel.setValueAt(confirmed.path("status").asText(),i,5);tableModel.setValueAt(accepted?"Diterima":"Ditolak",i,6);break;}setBusy(false,"Keputusan berhasil dikonfirmasi.");table.repaint();toast("Rujukan berhasil "+(accepted?"diterima.":"ditolak."),false);}
            catch(Exception ex){setBusy(false,"Keputusan belum dapat dikonfirmasi.");failure(ex);}finally{service.decisionChanged();}}
        }.execute();
    }
    private SatuSehatRujukanMasukService.Row findRow(String id){for(SatuSehatRujukanMasukService.Row r:rows)if(r.id.equals(id))return r;return null;}
    private void loadDetail(final String taskId){
        if(busy||disposed)return;SatuSehatRujukanMasukService.Row row=findRow(taskId);if(row!=null&&!row.accepted()){toast("Detail tersedia setelah rujukan diterima.",true);return;}
        final long run=++detailGeneration;setBusy(true,"Memuat detail rujukan...");
        new SwingWorker<SatuSehatRujukanIGDRanapApi.IncomingReferralDetail,Void>(){
            @Override protected SatuSehatRujukanIGDRanapApi.IncomingReferralDetail doInBackground()throws Exception{
                SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d=apiClient.detailRujukanMasuk(taskId);
                if(!"accepted".equals(SatuSehatRujukanIGDRanapApi.parseDecision(d.task)))throw new java.io.IOException("Rujukan belum diterima atau statusnya telah berubah. Perbarui daftar.");return d;
            }
            @Override protected void done(){if(disposed||run!=detailGeneration)return;try{currentDetail=get();setBusy(false,"Detail rujukan dimuat.");showIncomingDetail(currentDetail);}catch(Exception ex){setBusy(false,"Detail belum dapat dimuat.");failure(ex);service.refresh();}}
        }.execute();
    }
    private void showIncomingDetail(final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        detailHost.removeAll();title.setText("Detail Rujukan Masuk");
        JPanel identity=card(SatuSehatRujukanMasukService.resourceName(d.patient,"Pasien rujukan"),"Dari "+SatuSehatRujukanMasukService.resourceName(d.referrerOrganization,referenceText(d.task.path("requester")))+" • No. Rujukan Nasional: "+or(d.nationalReferralNumber,"Belum terbit"));detailHost.add(identity,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(BODY);tabs.addTab("Ringkasan",scroll(incomingSummaryPanel(d)));tabs.addTab("Data Klinis",scroll(incomingClinicalPanel(d)));tabs.addTab("CarePlan",scroll(incomingCarePlanPanel(d)));tabs.addTab("Resource FHIR",incomingResourcePanel(d));tabs.addTab("JSON",incomingJsonPanel(d));detailHost.add(tabs,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,8));bottom.setOpaque(false);registrationStatus=label("Mencocokkan data pasien...",SMALL,MUTED);bottom.add(registrationStatus,BorderLayout.NORTH);
        JPanel actions=flow();JButton back=button("Kembali ke Daftar",Color.WHITE,BLUE),refresh=button("Muat Ulang Detail",Color.WHITE,BLUE);back.addActionListener(e->{if(!busy){title.setText("Rujukan Masuk");((CardLayout)pages.getLayout()).show(pages,"list");}});refresh.setIcon(new LineIcon("reload",BLUE,15));refresh.addActionListener(e->loadDetail(d.taskId));
        register=button("Daftarkan Pasien",BLUE,Color.WHITE);register.setIcon(new LineIcon("plus",Color.WHITE,16));register.setEnabled(!busy&&SatuSehatRujukanMasukPasien.allowed());register.addActionListener(e->registerPatient(d));
        actions.add(back);actions.add(Box.createHorizontalStrut(6));actions.add(refresh);bottom.add(actions,BorderLayout.WEST);bottom.add(register,BorderLayout.EAST);detailHost.add(bottom,BorderLayout.SOUTH);
        ((CardLayout)pages.getLayout()).show(pages,"detail");detailHost.revalidate();detailHost.repaint();checkRegistration(d);
    }
    private void checkRegistration(final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        new SwingWorker<SatuSehatRujukanMasukPasien.State,Void>(){
            @Override protected SatuSehatRujukanMasukPasien.State doInBackground()throws Exception{return SatuSehatRujukanMasukPasien.state(d);}
            @Override protected void done(){if(disposed||currentDetail!=d)return;try{SatuSehatRujukanMasukPasien.State s=get();registrationStatus.setText(s.message());register.setText(s.found()?"Buka Data Pasien":"Daftarkan Pasien");register.setEnabled(!busy&&SatuSehatRujukanMasukPasien.allowed());}
                catch(Exception ex){Throwable cause=ex instanceof java.util.concurrent.ExecutionException&&ex.getCause()!=null?ex.getCause():ex;String error=cause.getMessage();registrationStatus.setText((error==null||error.trim().isEmpty()?"Data pasien belum dapat dicocokkan. Periksa koneksi database.":error)+" Anda tetap dapat membuka Data Pasien; validasi ulang dilakukan sebelum Simpan/Ganti.");register.setEnabled(!busy&&SatuSehatRujukanMasukPasien.allowed());}}
        }.execute();
    }
    private void registerPatient(final SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        if(busy||disposed||!SatuSehatRujukanMasukPasien.allowed())return;
        setBusy(true,"Menyiapkan form Data Pasien...");
        SatuSehatRujukanMasukPasien.open(this,d,()->{if(disposed)return;setBusy(false,"Kembali dari Data Pasien.");checkRegistration(d);},error->{if(disposed)return;setBusy(false,"Pendaftaran belum dapat dibuka.");toast(error,true);checkRegistration(d);});
    }
    private void showApi(){
        if(busy)return;apiHost.removeAll();JTextArea text=new JTextArea("REQUEST\n"+apiClient.getLastMethod()+" "+apiClient.getLastUrl()+"\n"+prettyJson(apiClient.getLastRequest())+"\n\nRESPONSE\n"+prettyJson(apiClient.getLastResponse()));text.setEditable(false);text.setFont(new Font("Consolas",Font.PLAIN,12));text.setCaretPosition(0);apiHost.add(scroll(text),BorderLayout.CENTER);
        JPanel controls=flow();JButton back=button("Kembali",Color.WHITE,BLUE),copy=button("Salin Respons",Color.WHITE,BLUE);back.addActionListener(e->{title.setText(currentDetail==null?"Rujukan Masuk":"Detail Rujukan Masuk");((CardLayout)pages.getLayout()).show(pages,currentDetail==null?"list":"detail");});copy.addActionListener(e->{try{Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.getText()),null);toast("Respons berhasil disalin.",false);}catch(RuntimeException ex){toast("Clipboard belum tersedia.",true);}});controls.add(back);controls.add(Box.createHorizontalStrut(8));controls.add(copy);apiHost.add(controls,BorderLayout.SOUTH);title.setText("Respon API");((CardLayout)pages.getLayout()).show(pages,"api");
    }
    private void setBusy(boolean value,String text){busy=value;status.setText(text);reload.setEnabled(!value);close.setEnabled(!value);table.repaint();if(register!=null&&value)register.setEnabled(false);}
    private void failure(Exception ex){Throwable cause=ex instanceof java.util.concurrent.ExecutionException&&ex.getCause()!=null?ex.getCause():ex;toast(or(cause.getMessage(),"Proses rujukan belum berhasil. Periksa Respon API."),true);}
    private void toast(String text,boolean error){notice.setBackground(error?new Color(255,244,225):LIGHT);message.setText(text);message.setForeground(error?AMBER:BLUE);notice.setVisible(true);toastTimer.restart();revalidate();}
    private JPanel incomingSummaryPanel(SatuSehatRujukanIGDRanapApi.IncomingReferralDetail d){
        Vertical p=new Vertical();String referrer=incomingResourceName(d.referrerOrganization,referenceText(d.task==null?null:d.task.path("requester")));String patient=incomingResourceName(d.patient,referenceText(d.task==null?null:d.task.path("for")));
        String nik=incomingIdentifier(d.patient,"nik"),gender=jsonText(d.patient==null?null:d.patient.path("gender"),"—"),birth=jsonText(d.patient==null?null:d.patient.path("birthDate"),"—");
        JPanel r=detailCard("Identitas rujukan",new String[][]{{"Fasyankes perujuk",referrer},{"Task ID",or(d.taskId,"—")},{"Status / keputusan",jsonText(d.task==null?null:d.task.path("status"),"—")+(apiClient.parseDecision(d.task).isEmpty()?"":" / "+apiClient.parseDecision(d.task))},{"CarePlan ID",or(d.carePlanId,"—")},{"ServiceRequest ID",or(d.serviceRequestId,"Belum dikirim / belum ditemukan")},{"Nomor Rujukan Nasional",or(d.nationalReferralNumber,"Belum terbit / belum ditemukan")},{"Waktu tugas",incomingTaskTime(d.task)},{"Jenis rujukan",incomingReferralType(d)}});p.add(r);p.add(Box.createVerticalStrut(12));
        JPanel pt=detailCard("Pasien & kunjungan asal",new String[][]{{"Nama pasien",patient},{"IHS pasien",incomingId(d.patient,"Patient")},{"NIK",or(nik,"—")},{"Jenis kelamin / tanggal lahir",gender+" / "+birth},{"Encounter",incomingId(d.encounter,"Encounter")},{"Status / kelas Encounter",incomingEncounterStatus(d.encounter)},{"Periode Encounter",incomingEncounterPeriod(d.encounter)},{"Lokasi / unit",incomingEncounterLocations(d.encounter)}});p.add(pt);return p;
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
    private static String incomingTaskTime(JsonNode t){if(t==null)return "—";String a=jsonText(t.path("authoredOn"),""),e=jsonText(t.path("executionPeriod").path("start"),"");return or(a,or(e,"—"));}
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
    private static JLabel label(String s,Font f,Color c){JLabel l=new JLabel(s);l.setFont(f);l.setForeground(c);l.setAlignmentX(0);return l;}
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
        b.setBorder(BorderFactory.createEmptyBorder(9,13,9,13));b.setUI(new BasicButtonUI(){@Override public void paint(Graphics g,JComponent c){Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(c.isEnabled()?bg:new Color(232,238,243));p.fillRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,12,12);p.setColor(LINE);p.drawRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,12,12);p.dispose();super.paint(g,c);}
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
