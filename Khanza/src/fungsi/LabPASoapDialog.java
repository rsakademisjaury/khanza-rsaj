package fungsi;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** Pembaca SOAP/CPPT kunjungan aktif; tidak mengubah catatan klinis sumber. */
public final class LabPASoapDialog extends JDialog {
    private SwingWorker<List<Catatan>,Void> pemuat;

    public LabPASoapDialog(Window owner,Connection connection,String noRawat,String pasien){
        super(owner,"SOAP / CPPT Pasien",Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        final LabPASoapTimeline tampilan=new LabPASoapTimeline(noRawat,pasien,()->dispose(),teks->{
            try{Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(teks),null);return true;}
            catch(IllegalStateException|SecurityException ex){return false;}
        });
        setContentPane(tampilan);
        getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e->tampilan.fokusCari(),KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_DOWN_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
        Rectangle layar=owner!=null&&owner.getGraphicsConfiguration()!=null?owner.getGraphicsConfiguration().getBounds():GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        int lebar=Math.min(1120,Math.max(480,layar.width-80)),tinggi=Math.min(780,Math.max(400,layar.height-100));
        setMinimumSize(new Dimension(Math.min(720,lebar),Math.min(500,tinggi)));setSize(lebar,tinggi);setLocationRelativeTo(owner);
        addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){if(pemuat!=null)pemuat.cancel(true);tampilan.hentikan();}});
        pemuat=new SwingWorker<List<Catatan>,Void>(){
            @Override protected List<Catatan> doInBackground()throws Exception{return baca(connection,noRawat);}
            @Override protected void done(){
                if(!isDisplayable()||isCancelled())return;
                try{tampilan.setCatatan(get());}
                catch(Exception ex){tampilan.gagal();Throwable sebab=ex.getCause()==null?ex:ex.getCause();System.out.println("SOAP PA: "+sebab);}
            }
        };pemuat.execute();
    }
    public static final class Catatan {
        public final String layanan,tanggal,jam,petugas,jabatan;
        public final String subjektif,objektif,asesmen,rencana,instruksi,evaluasi,ttv,alergi;
        public final String suhu,td,nadi,rr,spo2,gcs,kesadaran,bb,tb;
        private Catatan(ResultSet r)throws SQLException{
            layanan=s(r,"layanan");tanggal=s(r,"tgl_perawatan");jam=s(r,"jam_rawat");petugas=s(r,"nama_petugas");jabatan=s(r,"jabatan");
            subjektif=s(r,"keluhan");objektif=s(r,"pemeriksaan");asesmen=s(r,"penilaian");rencana=s(r,"rtl");instruksi=s(r,"instruksi");evaluasi=s(r,"evaluasi");alergi=s(r,"alergi");
            ttv="Suhu: "+s(r,"suhu_tubuh")+" | TD: "+s(r,"tensi")+" | Nadi: "+s(r,"nadi")+" | RR: "+s(r,"respirasi")+" | SpO2: "+s(r,"spo2")
                    +"\nGCS: "+s(r,"gcs")+" | Kesadaran: "+s(r,"kesadaran")+" | BB: "+s(r,"berat")+" | TB: "+s(r,"tinggi");
            suhu=s(r,"suhu_tubuh");td=s(r,"tensi");nadi=s(r,"nadi");rr=s(r,"respirasi");spo2=s(r,"spo2");
            gcs=s(r,"gcs");kesadaran=s(r,"kesadaran");bb=s(r,"berat");tb=s(r,"tinggi");
        }
    }
    private static String s(ResultSet r,String column)throws SQLException{String s=LabPAData.text(r.getString(column));return s.isEmpty()?"-":s;}
    public static List<Catatan> baca(Connection connection,String noRawat)throws SQLException{
        if(LabPAData.text(noRawat).isEmpty())throw new SQLException("Pilih kunjungan pasien terlebih dahulu.");
        String sql=bagianSQL("pemeriksaan_ralan","Rawat Jalan")+" union all "+bagianSQL("pemeriksaan_ranap","Rawat Inap")
                +" order by tgl_perawatan desc,jam_rawat desc,layanan,nama_petugas";
        List<Catatan> result=new ArrayList<>();
        try(PreparedStatement p=connection.prepareStatement(sql)){
            p.setString(1,noRawat);p.setString(2,noRawat);
            try(ResultSet r=p.executeQuery()){while(r.next())result.add(new Catatan(r));}
        }
        return result;
    }
    private static String bagianSQL(String tabel,String layanan){
        return "select '"+layanan+"' as layanan,p.tgl_perawatan,p.jam_rawat,p.keluhan,p.pemeriksaan,p.penilaian,p.rtl,p.instruksi,p.evaluasi,"
                +"p.suhu_tubuh,p.tensi,p.nadi,p.respirasi,p.spo2,p.gcs,p.kesadaran,p.berat,p.tinggi,p.alergi,"
                +"coalesce(g.nama,p.nip) as nama_petugas,coalesce(g.jbtn,'') as jabatan from "+tabel+" p left join pegawai g on g.nik=p.nip where p.no_rawat=?";
    }
    public static String teks(List<Catatan> daftar){
        if(daftar.isEmpty())return "Belum ada SOAP/CPPT rawat jalan maupun rawat inap pada kunjungan ini.";
        StringBuilder out=new StringBuilder();int n=0;
        for(Catatan c:daftar){if(n>0)out.append("\n\n");out.append(++n).append(". ").append(c.layanan).append(" | ").append(c.tanggal).append(' ').append(c.jam)
                .append("\n").append(c.petugas).append(" | ").append(c.jabatan).append("\n\nS - Subjektif\n").append(c.subjektif)
                .append("\n\nO - Objektif\n").append(c.objektif).append("\n").append(c.ttv).append("\nAlergi: ").append(c.alergi)
                .append("\n\nA - Asesmen\n").append(c.asesmen).append("\n\nP - Rencana\n").append(c.rencana)
                .append("\n\nInstruksi\n").append(c.instruksi).append("\n\nEvaluasi\n").append(c.evaluasi);}
        return out.toString();
    }
    public static Icon ikon(){return new Icon(){
        public int getIconWidth(){return 16;}public int getIconHeight(){return 16;}
        public void paintIcon(Component c,Graphics graphics,int x,int y){Graphics2D g=(Graphics2D)graphics.create();try{
            g.translate(x,y);g.setColor(new Color(39,119,205));g.setStroke(new BasicStroke(1.3f));g.drawRoundRect(3,3,10,12,3,3);g.drawRoundRect(5,0,6,4,2,2);
            for(int n=0;n<3;n++)g.drawLine(5,7+n*3,11,7+n*3);
        }finally{g.dispose();}}
    };}
}
