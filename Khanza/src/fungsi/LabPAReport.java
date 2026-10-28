package fungsi;

import java.io.File;
import java.util.*;
import java.awt.image.BufferedImage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

/** Report PA per pemeriksaan. Tidak menggunakan temporary_lab yang dipakai modul lain. */
public final class LabPAReport {
    private LabPAReport() {}
    private static final Map<String,JasperReport> CACHE=new HashMap<>();
    private static final Map<String,Long> STAMP=new HashMap<>();
    private static final String COMPILER_JDK="net.sf.jasperreports.engine.design.JRJdk13Compiler";
    private static synchronized JasperReport compile(File source)throws Exception{
        String key=source.getCanonicalPath();long stamp=source.lastModified()^source.length();
        JasperReport report=CACHE.get(key);
        if(report!=null&&Long.valueOf(stamp).equals(STAMP.get(key)))return report;
        // Jangan biarkan auto-detection memilih ECJ/JDT yang bentrok dengan Jasper lama.
        try{Class.forName("com.sun.tools.javac.Main");}
        catch(ClassNotFoundException ex){
            throw new java.io.IOException("Compiler JDK untuk report PA tidak tersedia. Jalankan SIMRS menggunakan JDK lengkap; pada Java 8 pastikan tools.jar JDK dapat diakses aplikasi.",ex);
        }catch(LinkageError ex){throw masalahLibraryPA(ex);}
        java.nio.file.Path temp=java.nio.file.Files.createTempDirectory("khanza-pa-report-");
        try{
            SimpleJasperReportsContext context=new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
            // Seluruh pengaturan lokal pada context PA. Tidak mengubah System/JRProperties global.
            context.setProperty("net.sf.jasperreports.compiler.java",COMPILER_JDK);
            context.setProperty("net.sf.jasperreports.compiler.class",COMPILER_JDK);
            context.setProperty("net.sf.jasperreports.compiler.classpath",classpathPA());
            context.setProperty("net.sf.jasperreports.compiler.temp.dir",temp.toFile().getAbsolutePath());
            context.setProperty("net.sf.jasperreports.compiler.keep.java.file","false");
            try{
                report=JasperCompileManager.getInstance(context).compile(source.getAbsolutePath());
            }catch(LinkageError ex){throw masalahLibraryPA(ex);}
            CACHE.put(key,report);STAMP.put(key,stamp);return report;
        }catch(LinkageError ex){throw masalahLibraryPA(ex);}
        finally{
            // Hanya direktori unik milik kompilasi ini, bukan folder report/source pengguna.
            File folder=temp.toFile();File[] files=folder.listFiles();
            if(files!=null)for(File file:files)if(file.isFile()&&!file.delete())file.deleteOnExit();
            if(!folder.delete())folder.deleteOnExit();
        }
    }
    private static java.io.IOException masalahLibraryPA(LinkageError ex){
        return new java.io.IOException("Library report PA belum kompatibel. Kompilasi sudah diarahkan ke JDK, bukan Eclipse JDT. Detail: "+ex.toString(),ex);
    }
    private static String classpathPA(){
        java.util.LinkedHashSet<String> entries=new java.util.LinkedHashSet<>();
        String runtime=System.getProperty("java.class.path","");
        for(String path:runtime.split(java.util.regex.Pattern.quote(File.pathSeparator)))if(!path.trim().isEmpty())entries.add(path);
        for(Class<?> type:new Class<?>[]{LabPAReport.class,JasperReport.class,JRMapCollectionDataSource.class}){
            try{
                java.security.CodeSource origin=type.getProtectionDomain().getCodeSource();
                if(origin!=null&&"file".equals(origin.getLocation().getProtocol()))entries.add(new File(origin.getLocation().toURI()).getAbsolutePath());
            }catch(Exception ex){System.out.println("Classpath report PA: "+ex.getClass().getSimpleName());}
        }
        for(ClassLoader loader=Thread.currentThread().getContextClassLoader();loader!=null;loader=loader.getParent()){
            if(loader instanceof java.net.URLClassLoader)for(java.net.URL url:((java.net.URLClassLoader)loader).getURLs()){
                try{if("file".equals(url.getProtocol()))entries.add(new File(url.toURI()).getAbsolutePath());}catch(Exception ignored){}
            }
        }
        // java -jar tidak selalu menampilkan semua JAR manifest di java.class.path.
        java.util.List<File> folders=new java.util.ArrayList<>();folders.add(new File("lib"));
        for(String entry:new java.util.ArrayList<>(entries)){
            File file=new File(entry).getAbsoluteFile();
            if(file.isFile()&&file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".jar")){
                folders.add(file.getParentFile());folders.add(new File(file.getParentFile(),"lib"));
            }
        }
        for(File folder:folders){
            File[] jars=folder.listFiles((dir,name)->name.toLowerCase(java.util.Locale.ROOT).endsWith(".jar"));
            if(jars!=null){java.util.Arrays.sort(jars);for(File jar:jars)entries.add(jar.getAbsolutePath());}
        }
        StringBuilder result=new StringBuilder();
        for(String entry:entries){if(result.length()>0)result.append(File.pathSeparator);result.append(entry);}
        return result.toString();
    }
    public static java.awt.Image image(Object value){
        try{
            if(value instanceof java.awt.Image)return (java.awt.Image)value;
            if(value instanceof javax.swing.ImageIcon)return ((javax.swing.ImageIcon)value).getImage();
            if(value instanceof byte[])return javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream((byte[])value));
            if(value instanceof java.io.InputStream)return javax.imageio.ImageIO.read((java.io.InputStream)value);
            if(value instanceof java.sql.Blob){try(java.io.InputStream in=((java.sql.Blob)value).getBinaryStream()){return javax.imageio.ImageIO.read(in);}}
        }catch(Exception ex){System.out.println("Logo report PA: "+ex.getMessage());}
        return null;
    }
    public static List<Map<String,?>> rows(LabPAData.Bahan b,Map<String,Object> klinis,boolean permintaan) {
        List<Map<String,?>> rows=new ArrayList<>();
        if(permintaan)add(rows,"Jenis Jaringan",b.jenisJaringan);
        addBahan(rows,"Lokasi/Bahan Jaringan",b.lokasi,permintaan,false);
        if("IHC".equals(b.jenis)){
            addBahan(rows,"RS dan No. PA sebelumnya",b.rsSebelum+" / "+b.noSebelum,permintaan,false);
            addBahan(rows,"Permintaan IHC",b.permintaanIHC,permintaan,true);
            add(rows,"Diagnosa PA",b.diagnosaPA);
            if(!permintaan)add(rows,"Pemeriksaan IHC",b.pemeriksaanIHC);
        }else{
            addBahan(rows,"Cara Mendapatkan Jaringan",b.cara,permintaan,false);
            addBahan(rows,"Cairan Fiksasi",b.fiksasi,permintaan,true);
            add(rows,"Diagnosa Klinik",klinis.get("diagnosa_klinik"));add(rows,"Ket. Klinik",klinis.get("keterangan_klinik"));
            if(!permintaan){add(rows,"Makroskopik",klinis.get("makroskopik"));add(rows,"Mikroskopik",klinis.get("mikroskopik"));}
        }
        if(!permintaan){add(rows,"Kesimpulan",klinis.get("kesimpulan"));add(rows,"Catatan",klinis.get("catatan"));}
        return rows;
    }
    private static void addBahan(List<Map<String,?>> rows,String label,Object value,boolean permintaan,boolean akhir){
        add(rows,label,value);
        if(!permintaan){
            Map<String,Object> r=new HashMap<>(rows.get(rows.size()-1));
            r.put("bahan",Boolean.TRUE);r.put("akhir_bahan",Boolean.valueOf(akhir));rows.set(rows.size()-1,r);
        }
    }
    private static void add(List<Map<String,?>> rows,String label,Object value){
        Map<String,Object> r=new HashMap<>();r.put("label",label);r.put("isi",LabPAData.text(value).isEmpty()?"-":LabPAData.text(value));
        r.put("tebal",Boolean.valueOf(label.equals("Kesimpulan")));r.put("bahan",Boolean.FALSE);r.put("akhir_bahan",Boolean.FALSE);rows.add(r);
    }
    public static JasperPrint fill(String jenis,boolean permintaan,Map<String,Object> params,List<Map<String,?>> rows)throws Exception{
        if(!LabPAData.validJenis(jenis))throw new IllegalArgumentException("Jenis pemeriksaan belum dipilih.");
        String nama=permintaan?"rptPermintaanPA_RS_Akademis":"rptHasilPA_"+jenis+"_RS_Akademis";
        File source=new File("report",nama+".jrxml");
        if(!source.isFile())throw new java.io.IOException("File report belum dipasang: "+source.getPath());
        Map<String,Object> p=new HashMap<>(params);
        p.put("logo",image(params.get("logo")));
        if(!permintaan){
            // Format tampilan saja; waktu dan identitas tersimpan tidak diubah.
            p.put("tanggal_terima",tanggalTampilPA(p.get("tanggal_terima")));
            p.put("tanggal_jawab",tanggalTampilPA(p.get("tanggal_jawab")));
            String jk=LabPAData.text(p.get("jkel"));
            if("L".equalsIgnoreCase(jk))p.put("jkel","Laki-Laki");
            else if("P".equalsIgnoreCase(jk))p.put("jkel","Perempuan");
        }
        p.put("judul",(permintaan?"PERMINTAAN PEMERIKSAAN ":"HASIL PEMERIKSAAN ")+LabPAData.namaJenis(jenis).toUpperCase(Locale.ROOT));
        p.put("qrImage",qr(LabPAData.text(p.get("finger"))));
        p.put("labelTanggal",permintaan?"Tgl. Permintaan":"Diterima Tgl.");
        p.put("labelJawab",permintaan?"Pengambilan Bahan":"Dijawab Tgl.");
        p.put("labelPenanda",permintaan?"Dokter Perujuk":"Ahli Patologi Anatomi");
        p.put("penanda",permintaan?LabPAData.text(p.get("pengirim")):LabPAData.text(p.get("penjab")));
        // Compile JRXML dengan versi Jasper milik SIMRS agar tidak bentrok serialisasi .jasper.
        JasperReport report=compile(source);
        try{return JasperFillManager.fillReport(report,p,new JRMapCollectionDataSource(rows));}
        catch(LinkageError ex){throw masalahLibraryPA(ex);}
    }
    private static String tanggalTampilPA(Object value){
        String s=LabPAData.text(value);
        if(s.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*"))return s.substring(8,10)+"-"+s.substring(5,7)+"-"+s.substring(0,4)+s.substring(10);
        return s;
    }
    public static void preview(JasperPrint hasil,String judul){
        JasperViewer v=new JasperViewer(hasil,false);v.setTitle(judul);
        java.awt.Dimension d=java.awt.Toolkit.getDefaultToolkit().getScreenSize();v.setSize(d.width-50,d.height-50);
        v.setLocationRelativeTo(null);v.setModalExclusionType(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);v.setVisible(true);
    }
    private static BufferedImage qr(String value){
        if(value.isEmpty())return null;
        try{
            // ZXing sudah digunakan banyak instalasi Khanza; refleksi tidak menambah dependency compile.
            Class<?> formatClass=Class.forName("com.google.zxing.BarcodeFormat");
            Object format=formatClass.getField("QR_CODE").get(null);
            Class<?> writerClass=Class.forName("com.google.zxing.qrcode.QRCodeWriter");
            Object writer=writerClass.newInstance();
            Object matrix=writerClass.getMethod("encode",String.class,formatClass,int.class,int.class).invoke(writer,value,180,180);
            java.lang.reflect.Method get=matrix.getClass().getMethod("get",int.class,int.class);
            BufferedImage image=new BufferedImage(180,180,BufferedImage.TYPE_INT_RGB);
            for(int y=0;y<180;y++)for(int x=0;x<180;x++)image.setRGB(x,y,Boolean.TRUE.equals(get.invoke(matrix,x,y))?0xff000000:0xffffffff);
            return image;
        }catch(Exception ex){System.out.println("QR report PA tidak tersedia: "+ex.getClass().getSimpleName());return null;}
    }
}
