/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile 
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */

package fungsi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
//import javax.swing.text.Document;
//import javax.swing.text.Element;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;  // Explicitly specify the correct Document class
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import uz.ncipro.calendar.JDateTimePicker;

/**
 *
 * @author Owner
 */
public final class sekuel {
    private javax.swing.ImageIcon icon = null;
    private String folder,AKTIFKANTRACKSQL = koneksiDB.AKTIFKANTRACKSQL();
    private final Connection connect=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private int angka=0;
    private double angka2=0;
    private String dicari="";
    private Date tanggal=new Date();
    private boolean bool=false;
    private final DecimalFormat df2 = new DecimalFormat("####");
    private SimpleDateFormat formattanggal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date waktumulai,kegiatan;
    private long bedawaktu=0;
    
    public sekuel(){
        super();
    }


    public void menyimpan(String table,String value,String sama){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try{                  
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);            
                JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada "+sama+" yang sama dimasukkan sebelumnya...!");
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            SimpanTrack("insert into "+table+" values("+value+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e); 
        }            
    }
    
    public void menyimpan2(String table,String value,String sama){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try{                  
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);    
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            
            SimpanTrack("insert into "+table+" values("+value+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e); 
        }            
    }
    
    public boolean menyimpantf(String table,String value,String sama){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            ps.executeUpdate();
            if(ps != null){
                ps.close();
            }  
            
            SimpanTrack("insert into "+table+" values("+value+")");
            return true;           
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e); 
            JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada "+sama+" yang sama dimasukkan sebelumnya...!");
            return false;
        }            
    }
    
    public boolean menyimpantf2(String table,String value,String sama){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            ps.executeUpdate();
            if(ps != null){
                ps.close();
            }  
            
            SimpanTrack("insert into "+table+" values("+value+")");
            return true;           
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e); 
            return false;
        }            
    }
    
    public boolean menyimpantf(String table,String value,int i,String[] a,String acuan_field,String update,int j,String[] b){
        bool=false;
        try{ 
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            for(angka=1;angka<=i;angka++){
                ps.setString(angka,a[angka-1]);
            }            
            ps.executeUpdate();
            
            if(ps != null){
                ps.close();
            } 
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
            bool=true;
        }catch(Exception e){
            try {
                ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
                for(angka=1;angka<=j;angka++){
                    ps.setString(angka,b[angka-1]);
                } 
                ps.executeUpdate();   
                
                if(ps != null){
                    ps.close();
                } 
                
                if(AKTIFKANTRACKSQL.equals("yes")){
                    dicari="";
                    for(angka=1;angka<=i;angka++){
                        dicari=dicari+"|"+b[angka-1];
                    }
                }
                SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
                bool=true;
            } catch (Exception e2) {
                bool=false;
                System.out.println("Notifikasi : "+e2);
            }                         
        }
        return bool;
    }
    
    public void menyimpan(String table,String value,String sama,int i,String[] a){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
                
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);            
                JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada "+sama+" yang sama dimasukkan sebelumnya...!");
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+table+" "+e); 
        }            
    }
    
    public void menyimpan2(String table,String value,String sama,int i,String[] a){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);            
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);            
        }    
    }
    
    public void menyimpan3(String table,String value,String sama,int i,String[] a){
        try {
            ps=connect.prepareStatement("insert ignore into "+table+" values("+value+")");
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);            
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);            
        }    
    }
    
    public boolean menyimpantf(String table,String value,String sama,int i,String[] a){        
        try{             
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            for(angka=1;angka<=i;angka++){
                ps.setString(angka,a[angka-1]);
            }            
            ps.executeUpdate();
            
            if(ps != null){
                ps.close();
            }
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
            return true;
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);  
            if(e.toString().contains("Duplicate")){
                JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada "+sama+" yang sama dimasukkan sebelumnya...!");
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Ada kesalahan Query...!");
            }
            return false;
        }
    }
    
    public boolean menyimpantf2(String table,String value,String sama,int i,String[] a){
        bool=true;
        try{ 
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try {
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
                bool=true;
            } catch (Exception e) {
                bool=false;
                System.out.println("Notifikasi : "+e);  
            } finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        }catch(Exception e){
            bool=false;
            System.out.println("Notifikasi : "+e);  
        }
        return bool;
    }
    
    public boolean menyimpantf2(String table,String value,int i,String[] a){
        bool=true;
        try{ 
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try {
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
                bool=true;
            } catch (Exception e) {
                bool=false;
                System.out.println("Notifikasi : "+e);  
            } finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        }catch(Exception e){
            bool=false;
            System.out.println("Notifikasi : "+e);  
        }
        return bool;
    }
    
    public void menyimpan(String table,String value,int i,String[] a){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try{                 
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);            
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);   
        }            
    }
    
    public void menyimpanignore(String table,String value,int i,String[] a){
        try {
            ps=connect.prepareStatement("insert ignore into "+table+" values("+value+")");
            try{                 
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e); ;            
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);   
        }            
    }
    
    public void menyimpan2(String table,String value,int i,String[] a){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try{                 
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                }            
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi "+table+" : "+e);            
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        } catch (Exception e) { 
        }            
    }
    
    public void menyimpan(String table,String value,int i,String[] a,String acuan_field,String update,int j,String[] b){
        try{ 
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            for(angka=1;angka<=i;angka++){
                ps.setString(angka,a[angka-1]);
            }            
            ps.executeUpdate();
            
            if(ps != null){
                ps.close();
            } 
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        }catch(Exception e){
            try {
                ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
                for(angka=1;angka<=j;angka++){
                    ps.setString(angka,b[angka-1]);
                } 
                ps.executeUpdate();   
                
                if(ps != null){
                    ps.close();
                } 
                
                if(AKTIFKANTRACKSQL.equals("yes")){
                    dicari="";
                    for(angka=1;angka<=i;angka++){
                        dicari=dicari+"|"+b[angka-1];
                    }
                }
                SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
            } catch (Exception e2) {
                System.out.println("Notifikasi : "+e2);
            }                         
        }
    }
    
    public void menyimpan2(String table,String value,int i,String[] a,String acuan_field,String update,int j,String[] b){
        try{ 
            dicari="";
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            for(angka=1;angka<=i;angka++){
                dicari=dicari+", "+a[angka-1];
                ps.setString(angka,a[angka-1]);
            }            
            ps.executeUpdate();
            
            if(ps != null){
                  ps.close();
            } 
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        }catch(Exception e){
            try {
                ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
                for(angka=1;angka<=j;angka++){
                    ps.setString(angka,b[angka-1]);
                } 
                ps.executeUpdate(); 
                
                if(ps != null){
                    ps.close();
                } 
                
                if(AKTIFKANTRACKSQL.equals("yes")){
                    dicari="";
                    for(angka=1;angka<=i;angka++){
                        dicari=dicari+"|"+b[angka-1];
                    }
                }
                SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
            } catch (Exception e2) {                
                System.out.println("Notifikasi : "+e2);
            }            
        }
    }
    
    public void menyimpan3(String table,String value,int i,String[] a,String acuan_field,String update,int j,String[] b){
        try{ 
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            for(angka=1;angka<=i;angka++){
                ps.setString(angka,a[angka-1]);
            }            
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(null,"Proses simpan berhasil..!!");
            if(ps != null){
                ps.close();
            } 
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("insert into "+table+" values("+dicari+")");
        }catch(Exception e){
            try {
                ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
                for(angka=1;angka<=j;angka++){
                    ps.setString(angka,b[angka-1]);
                } 
                ps.executeUpdate();   
                
                JOptionPane.showMessageDialog(null,"Proses simpan berhasil..!!");
                if(ps != null){
                    ps.close();
                } 
                if(AKTIFKANTRACKSQL.equals("yes")){
                    dicari="";
                    for(angka=1;angka<=i;angka++){
                        dicari=dicari+"|"+b[angka-1];
                    }
                }
                SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
            } catch (Exception e2) {
                System.out.println("Notifikasi : "+e2);
            }                         
        }
    }
    
    public void menyimpan(String table,String value){
        try {
            ps=connect.prepareStatement("insert into "+table+" values("+value+")");
            try{
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);         
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            SimpanTrack("insert into "+table+" values("+value+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);  
        }
    }
    
    public void menyimpanignore(String table,String value){
        try {
            ps=connect.prepareStatement("insert ignore into "+table+" values("+value+")");
            try{
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);         
            }finally{
                if(ps != null){
                    ps.close();
                }                
            }
            SimpanTrack("insert into "+table+" values("+value+")");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);  
        }
    }
    
    public void menyimpan(String table,String isisimpan,String isiedit,String acuan_field){
        try{            
            ps=connect.prepareStatement("insert into "+table+" values("+isisimpan+")");
            ps.executeUpdate();   
            if(ps != null){
                ps.close();
            }  
            SimpanTrack("insert into "+table+" values("+isisimpan+")");
        }catch(Exception e){
            try {
                ps=connect.prepareStatement("update "+table+" set "+isiedit+" where "+acuan_field);
                ps.executeUpdate();
                if(ps != null){
                    ps.close();
                }  
                SimpanTrack("update "+table+" set "+isiedit+" where "+acuan_field);
            } catch (Exception ex) {
                System.out.println("Notifikasi Edit : "+ex);
            }
        }
    }

    public void menyimpan(String table,String value,String sama,JTextField AlmGb){
        try {
            ps = connect.prepareStatement("insert into "+table+" values("+value+",?)");
            try{                        
                ps.setBinaryStream(1, new FileInputStream(AlmGb.getText()), new File(AlmGb.getText()).length());
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada "+sama+" yang sama dimasukkan sebelumnya...!");
            }finally{
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
    }
    
    public void menyimpan(String table,String value,String sama,JTextField AlmGb,JTextField AlmPhoto){
        try {
            ps = connect.prepareStatement("insert into "+table+" values("+value+",?,?)");
            try{                        
                ps.setBinaryStream(1, new FileInputStream(AlmGb.getText()), new File(AlmGb.getText()).length());
                ps.setBinaryStream(2, new FileInputStream(AlmPhoto.getText()), new File(AlmPhoto.getText()).length());
                ps.executeUpdate();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, gagal menyimpan data. Kemungkinan ada "+sama+" yang sama dimasukkan sebelumnya...!");
            }finally{
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    

    public void meghapus(String table,String field,String nilai_field) {
        try {
            ps=connect.prepareStatement("delete from "+table+" where "+field+"=?");
            try{       
                ps.setString(1,nilai_field);
                ps.executeUpdate(); 
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, data gagal dihapus. Kemungkinan data tersebut masih dipakai di table lain...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack("delete from "+table+" where "+field+"='"+nilai_field+"'");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public boolean meghapustf(String table,String field,String nilai_field) {
        bool=true;
        try {
            ps=connect.prepareStatement("delete from "+table+" where "+field+"=?");
            try{       
                ps.setString(1,nilai_field);
                ps.executeUpdate();
                bool=true;
             }catch(Exception e){
                bool=false;
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, data gagal dihapus. Kemungkinan data tersebut masih dipakai di table lain...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack("delete from "+table+" where "+field+"='"+nilai_field+"'");
        } catch (Exception e) {
            bool=false;
            System.out.println("Notifikasi : "+e);
        }
        return bool;
    }
    
    public void meghapus(String table,String field,String field2,String nilai_field,String nilai_field2) {
        try {
            ps=connect.prepareStatement("delete from "+table+" where "+field+"=? and "+field2+"=?");
            try{       
                ps.setString(1,nilai_field);
                ps.setString(2,nilai_field2);
                ps.executeUpdate(); 
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, data gagal dihapus. Kemungkinan data tersebut masih dipakai di table lain...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack("delete from "+table+" where "+field+"='"+nilai_field+"' and "+field2+"='"+nilai_field2+"'");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public void meghapus2(String table,String field,String nilai_field) {
        try {
            ps=connect.prepareStatement("delete from "+table+" where "+field+"=?");
            try{       
                ps.setString(1,nilai_field);
                ps.executeUpdate(); 
                JOptionPane.showMessageDialog(null,"Proses hapus berhasil...!!!!");
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, data gagal dihapus. Kemungkinan data tersebut masih dipakai di table lain...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack("delete from "+table+" where "+field+"='"+nilai_field+"'");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public void meghapus3(String table,String field,String nilai_field) {
        try {
            ps=connect.prepareStatement("delete from "+table+" where "+field+"=?");
            try{       
                ps.setString(1,nilai_field);
                ps.executeUpdate(); 
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack("delete from "+table+" where "+field+"='"+nilai_field+"'");
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public void mengedit(String table,String acuan_field,String update){
        try {
            ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{                        
                ps.executeUpdate();       
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Gagal Mengedit. Mungkin kode sudah digunakan sebelumnya...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack("update "+table+" set "+update+" where "+acuan_field);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public boolean mengedittf(String table,String acuan_field,String update){
        bool=true;
        try {
            ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{                        
                ps.executeUpdate();  
                bool=true;
             }catch(Exception e){
                bool=false;
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Gagal Mengedit. Mungkin kode sudah digunakan sebelumnya...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack("update "+table+" set "+update+" where "+acuan_field);
        } catch (Exception e) {
            bool=false;
            System.out.println("Notifikasi : "+e);
        }
        return bool;
    }
    
    public void mengedit(String table,String acuan_field,String update,int i,String[] a){
        try {
            ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate();       
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Gagal Mengedit. Periksa kembali data...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }    
    }
    
    public void mengedit2(String table,String acuan_field,String update,int i,String[] a){
        try {
            ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate();   
                JOptionPane.showMessageDialog(null,"Proses edit berhasil...!!!!");
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Gagal mengedit. Periksa kembali data...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }    
    }
    
    public void mengedit3(String table,String acuan_field,String update,int i,String[] a){
        try {
            ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate();       
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }    
    }
    
    public boolean mengedittf(String table,String acuan_field,String update,int i,String[] a){
        bool=true;
        try {
            ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate();       
                bool=true;
             }catch(Exception e){
                bool=false;
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Gagal Mengedit. Periksa kembali data...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
        } catch (Exception e) {
            bool=false;
            System.out.println("Notifikasi : "+e);
        }   
        return bool;
    }
    
    public boolean mengedittf2(String table,String acuan_field,String update,int i,String[] a){
        bool=true;
        try {
            ps=connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate();       
                bool=true;
             }catch(Exception e){
                bool=false;
                System.out.println("Notifikasi : "+e);
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack("update "+table+" set "+update+" "+dicari+" where "+acuan_field);
        } catch (Exception e) {
            bool=false;
            System.out.println("Notifikasi : "+e);
        }   
        return bool;
    }
    
    public void mengedit(String table,String acuan_field,String update,JTextField AlmGb){
        try {
            ps = connect.prepareStatement("update "+table+" set "+update+" where "+acuan_field);
            try{            
                ps.setBinaryStream(1, new FileInputStream(AlmGb.getText()), new File(AlmGb.getText()).length());
                ps.executeUpdate();
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Pilih dulu data yang mau anda edit...\n Klik data pada table untuk memilih...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    public void query(String qry){
        try {
            ps=connect.prepareStatement(qry);
            try{
                ps.executeQuery();
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Query tidak bisa dijalankan...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack(qry);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }            
    }

    public void queryu(String qry){
        try {
            ps=connect.prepareStatement(qry);
            try{                            
                ps.executeUpdate(); 
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Query tidak bisa dijalankan...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            
            SimpanTrack(qry);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public boolean queryutf(String qry){
        bool=false;
        try {
            ps=connect.prepareStatement(qry);
            try{                            
                ps.executeUpdate(); 
                bool=true;
             }catch(Exception e){
                bool=false;
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Query tidak bisa dijalankan...!!!!");                
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack(qry);
        } catch (Exception e) {
            bool=false;
            System.out.println("Notifikasi : "+e);
        }
        return bool;
    }
    
    public boolean queryutf2(String qry){
        bool=false;
        try {
            ps=connect.prepareStatement(qry);
            try{                            
                ps.executeUpdate(); 
                bool=true;
             }catch(Exception e){
                bool=false;
                System.out.println("Notifikasi : "+e);           
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack(qry);
        } catch (Exception e) {
            bool=false;
            System.out.println("Notifikasi : "+e);
        }
        return bool;
    }
    
    public void queryu(String qry,String parameter){
        try {
            ps=connect.prepareStatement(qry);
            try{
                ps.setString(1,parameter);
                ps.executeUpdate();
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
                JOptionPane.showMessageDialog(null,"Maaf, Query tidak bisa dijalankan...!!!!");
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack(qry);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }    
    }
    
    
    public void queryu2(String qry){
        try {
            ps=connect.prepareStatement(qry);
            try{                            
                ps.executeUpdate(); 
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            SimpanTrack(qry);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public void queryu2(String qry,int i,String[] a){
        try {
            try{            
                ps=connect.prepareStatement(qry);
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate(); 
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack(qry+" "+dicari);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public boolean queryu2tf(String qry,int i,String[] a){
        bool=false;
        try {
            try{            
                ps=connect.prepareStatement(qry);
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate(); 
                bool=true;
             }catch(Exception e){
                bool=false;
                System.out.println("Notifikasi : "+e);
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack(qry+" "+dicari);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return bool;
    }
    
    public void queryu3(String qry,int i,String[] a){
        try {
            try{            
                ps=connect.prepareStatement(qry);
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate(); 
             }catch(Exception e){
                System.out.println("Notifikasi : "+e);
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack(qry+" "+dicari);
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public void queryu4(String qry,int i,String[] a){
        try {
            try{            
                ps=connect.prepareStatement(qry);
                for(angka=1;angka<=i;angka++){
                    ps.setString(angka,a[angka-1]);
                } 
                ps.executeUpdate(); 
             }catch(Exception e){
             }finally{
                if(ps != null){
                    ps.close();
                }
            }
            if(AKTIFKANTRACKSQL.equals("yes")){
                dicari="";
                for(angka=1;angka<=i;angka++){
                    dicari=dicari+"|"+a[angka-1];
                }
            }
            SimpanTrack(qry+" "+dicari);
        } catch (Exception e) {
        }
    }
    
    public void AutoComitFalse(){
        try {
            connect.setAutoCommit(false);
        } catch (Exception e) {
        }
    }
    
    public void AutoComitTrue(){
        try {
            connect.setAutoCommit(true);
        } catch (Exception e) {
        }
    }
    
    public void Commit(){
        try {
            connect.commit();
        } catch (Exception e) {
        }
    }
     
    public void RollBack(){
        try {
            connect.rollback();
        } catch (Exception e) {
            System.out.println("Notif : "+e);
            JOptionPane.showMessageDialog(null,"Gagal melakukan rollback..!");
        }
    }
    
    public void cariIsi(String sql,JComboBox cmb){
        try {
            ps=connect.prepareStatement(sql);
            try{  
                rs=ps.executeQuery();
                if(rs.next()){
                    String dicari=rs.getString(1);
                    cmb.setSelectedItem(dicari);
                }else{
                    cmb.setSelectedItem("");
                }    
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }   
    }

    public void cariIsi(String sql,JDateTimePicker dtp){
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();
                if(rs.next()){
                    try {
                        dtp.setDisplayFormat("yyyy-MM-dd");
                        dtp.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString(1)));
                        dtp.setDisplayFormat("dd-MM-yyyy");
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }       
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    public void cariIsi(String sql,JTextField txt){
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();
                if(rs.next()){
                    txt.setText(rs.getString(1));
                }else{
                    txt.setText("");
                }  
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public int cariRegistrasi(String norawat){
        angka=0;
        angka=cariInteger("select count(billing.no_rawat) from billing where billing.no_rawat=?",norawat)+
              cariInteger("select count(reg_periksa.no_rawat) from reg_periksa where reg_periksa.no_rawat=? and reg_periksa.stts='Batal'",norawat);
        return angka;
    }
    
    public boolean cekTanggalRegistrasi(String tanggalregistrasi,String tanggalinputdata){
//        bool=false;
//        try {
//            waktumulai = formattanggal.parse(tanggalregistrasi);
//            kegiatan = formattanggal.parse(tanggalinputdata);
//            bedawaktu = (kegiatan.getTime()-waktumulai.getTime())/1000;
//            if(bedawaktu<0){
//                bool=false;
//                JOptionPane.showMessageDialog(null,"Maaf, jam input data / perubahan data minimal di jam "+tanggalregistrasi+" !");
//            }else{
//                bool=true;
//            }
//        } catch (Exception ex) {
//            bool=false;
//            System.out.println("Notif : "+ex);
//        }
//        return bool;
return true;
    }
    
    public boolean cekTanggal48jam(String tanggalmulai,String tanggalinputdata){
        bool=false;
        try {
            waktumulai = formattanggal.parse(tanggalmulai);
            kegiatan = formattanggal.parse(tanggalinputdata);
            bedawaktu = (kegiatan.getTime()-waktumulai.getTime())/1000;
            if(bedawaktu>2592000){
                bool=false;
                JOptionPane.showMessageDialog(null,"Maaf, perubahan data / penghapusan data tidak boleh lebih dari 2 x 24 jam !");
            }else{
                bool=true;
            }
        } catch (Exception ex) {
            bool=false;
            System.out.println("Notif : "+ex);
        }
        return bool;

//// Mengabaikan pembatasan waktu, selalu mengembalikan true
//    return true;
    }
    
    public String ambiltanggalsekarang(){
        return formattanggal.format(new Date());
    }
    
    public void cariIsi(String sql,JTextField txt,String kunci){
        try {
            ps=connect.prepareStatement(sql);
            try{
                ps.setString(1,kunci);
                rs=ps.executeQuery();
                if(rs.next()){
                    txt.setText(rs.getString(1));
                }else{
                    txt.setText("");
                }   
            }catch(SQLException e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    } 
    
    public void cariIsi(String sql,JTextArea txt,String kunci){
        try {
            ps=connect.prepareStatement(sql);
            try{
                ps.setString(1,kunci);
                rs=ps.executeQuery();
                if(rs.next()){
                    txt.setText(rs.getString(1));
                }else{
                    txt.setText("");
                }   
            }catch(SQLException e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    

    public void cariIsi(String sql,JLabel txt){
        try {
            ps=connect.prepareStatement(sql);
            try{
                rs=ps.executeQuery();
                if(rs.next()){
                    txt.setText(rs.getString(1));
                }else{
                    txt.setText("");
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }
    
    public String cariIsi(String sql){
        dicari="";
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();            
                if(rs.next()){
                    dicari=rs.getString(1);
                }else{
                    dicari="";
                }   
            }catch(Exception e){
                dicari="";
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        
        return dicari;
    }
    
// === Ambil 1 kolom (String) dengan banyak parameter (varargs) ===
public String cariIsi(String sql, String... params){
    String hasil = "";
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        ps = connect.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String p = params[i];
                if (p == null) {
                    ps.setNull(i + 1, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(i + 1, p);
                }
            }
        }
        rs = ps.executeQuery();
        if (rs.next()) {
            hasil = rs.getString(1);
            if (hasil == null) hasil = "";
        }
    } catch (Exception e) {
        hasil = "";
        System.out.println("Notifikasi : " + e);
    } finally {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (ps != null) ps.close(); } catch (Exception e) {}
    }
    return hasil;
}

// === Convenience: tepat 2 parameter ===
public String cariIsi(String sql, String p1, String p2){
    return cariIsi(sql, new String[]{p1, p2});
}
    
    
    public ByteArrayInputStream cariGambar(String sql){
        ByteArrayInputStream inputStream=null;
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();            
                if(rs.next()){                
                    inputStream = new ByteArrayInputStream(rs.getBytes(1));
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return inputStream;
    }
    
    public String cariIsi(String sql,String data){
        dicari="";
        try {
            ps=connect.prepareStatement(sql);
            try{                            
                ps.setString(1,data);
                rs=ps.executeQuery();            
                if(rs.next()){
                    dicari=rs.getString(1);
                }else{
                    dicari="";
                }   
            }catch(Exception e){
                dicari="";
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null ){
                    rs.close();
                }

                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return dicari;
    }
    
    public Date cariIsi2(String sql){
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();            
                if(rs.next()){
                    tanggal=rs.getDate(1);
                }else{
                    tanggal=new Date();
                }   
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        return tanggal;
    }

    public Integer cariInteger(String sql){
        angka=0;
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();            
                if(rs.next()){
                    angka=rs.getInt(1);
                }else{
                    angka=0;
                } 
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka;
    }
    
    public Integer cariIntegerCount(String sql){
        angka=0;
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();            
                while(rs.next()){
                    angka=angka+rs.getInt(1);
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka;
    }
    
    public Integer cariInteger(String sql,String data){
        angka=0;
        try {
            ps=connect.prepareStatement(sql);
            try{
                ps.setString(1,data);
                rs=ps.executeQuery();            
                if(rs.next()){
                    angka=rs.getInt(1);
                }else{
                    angka=0;
                }  
            }catch(Exception e){
                angka=0;
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka;
    }
    
    public Integer cariInteger(String sql,String data,String data2){
        angka=0;
        try {
            ps=connect.prepareStatement(sql);
            try{
                ps.setString(1,data);
                ps.setString(2,data2);
                rs=ps.executeQuery();            
                if(rs.next()){
                    angka=rs.getInt(1);
                }else{
                    angka=0;
                }  
            }catch(Exception e){
                angka=0;
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka;
    }
    
    public Integer cariInteger(String sql,String data,String data2,String data3){
        angka=0;
        try {
            ps=connect.prepareStatement(sql);
            try{
                ps.setString(1,data);
                ps.setString(2,data2);
                ps.setString(3,data3);
                rs=ps.executeQuery();            
                if(rs.next()){
                    angka=rs.getInt(1);
                }else{
                    angka=0;
                }  
            }catch(Exception e){
                angka=0;
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka;
    }
    
    public Integer cariInteger(String sql,String data,String data2,String data3,String data4){
        angka=0;
        try {
            ps=connect.prepareStatement(sql);
            try{
                ps.setString(1,data);
                ps.setString(2,data2);
                ps.setString(3,data3);
                ps.setString(4,data4);
                rs=ps.executeQuery();            
                if(rs.next()){
                    angka=rs.getInt(1);
                }else{
                    angka=0;
                }  
            }catch(Exception e){
                angka=0;
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka;
    }
    
    public Integer cariInteger2(String sql){
        angka=0;
        try {
            ps=connect.prepareStatement(sql);
            try{
                rs=ps.executeQuery();            
                rs.last();
                angka=rs.getRow();
                if(angka<1){
                    angka=0;
                }   
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka;
    }

    public void cariIsiAngka(String sql,JTextField txt){
        try {
            ps=connect.prepareStatement(sql);
            try{
                rs=ps.executeQuery();
                if(rs.next()){
                    txt.setText(df2.format(rs.getDouble(1)));
                }else{
                    txt.setText("0");
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    public void cariIsiAngka(String sql,JLabel txt) {
        try {
            ps=connect.prepareStatement(sql);
            try{
                rs=ps.executeQuery();
                if(rs.next()){
                    txt.setText(df2.format(rs.getDouble(1)));
                }else{
                    txt.setText("0");
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }            
    }
    
    public double cariIsiAngka(String sql) {
        angka2=0;
        try {
            ps=connect.prepareStatement(sql);
            try{            
                rs=ps.executeQuery();
                if(rs.next()){
                    angka2=rs.getDouble(1);
                }else{
                    angka2=0;
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka2;
    }
    
    public double cariIsiAngka(String sql,String data) {
        angka2=0;
        try {
            ps=connect.prepareStatement(sql);
            try{            
                ps.setString(1,data);
                rs=ps.executeQuery();
                if(rs.next()){
                    angka2=rs.getDouble(1);
                }else{
                    angka2=0;
                }
                //rs.close();
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka2;
    }
    
    public double cariIsiAngka2(String sql,String data,String data2) {
        angka2=0;
        try {
            ps=connect.prepareStatement(sql);
            try{            
                ps.setString(1,data);
                ps.setString(2,data2);
                rs=ps.executeQuery();
                if(rs.next()){
                    angka2=rs.getDouble(1);
                }else{
                    angka2=0;
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return angka2;
    }

    public void cariGambar(String sql,JLabel txt){        
        try {
            ps=connect.prepareStatement(sql);
            try{
                rs=ps.executeQuery();
                if(rs.next()){
                    icon = new javax.swing.ImageIcon(rs.getBlob(1).getBytes(1L, (int) rs.getBlob(1).length()));
                    createThumbnail();
                    txt.setIcon(icon);
                }else{
                    txt.setText(null);
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
    }

    public void cariGambar(String sql,java.awt.Canvas txt,String text){
        try {
            ps=connect.prepareStatement(sql);
            try {
                rs = ps.executeQuery();
                for (int I = 0; rs.next(); I++) {
                    ((Painter) txt).setImage(gambar(text));
                    Blob blob = rs.getBlob(5);
                    ((Painter) txt).setImageIcon(new javax.swing.ImageIcon(
                        blob.getBytes(1, (int) (blob.length()))));
                }  
            } catch (Exception ex) {
                cetak(ex.toString());
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
    }
    
    public void SimpanTrack(String sql){
        if(AKTIFKANTRACKSQL.equals("yes")){
            try {
                ps=connect.prepareStatement("insert into trackersql values(now(),?,?)");
                try{       
                    ps.setString(1,akses.getalamatip()+" "+sql);
                    ps.setString(2,akses.getkode());
                    ps.executeUpdate(); 
                 }catch(Exception e){
                    System.out.println("Notifikasi : "+e);
                 }finally{
                    if(ps != null){
                        ps.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            }
        }
    }
    
    public String cariString(String sql){
        dicari="";
        try {
            ps=connect.prepareStatement(sql);
            try{
                rs=ps.executeQuery();            
                if(rs.next()){
                    dicari=rs.getString(1);
                }else{
                    dicari="";
                }
            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }finally{
                if(rs != null){
                    rs.close();
                }
                
                if(ps != null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
            
        return dicari;
    }

    private String gambar(String id) {
        return folder + File.separator + id.trim() + ".jpg";
    }
    
    public void Tabel(javax.swing.JTable tb,int lebar[]){
      tb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      angka=tb.getColumnCount();
      for(int i=0;i < angka;i++){
          javax.swing.table.TableColumn tbc = tb.getColumnModel().getColumn(i);
          tbc.setPreferredWidth(lebar[i]);
          //tb.setRowHeight(17);
      }
  }

    private void createThumbnail() {
        int maxDim = 150;
        try {
            Image inImage = icon.getImage();

            double scale = (double) maxDim / (double) inImage.getHeight(null);
            if (inImage.getWidth(null) > inImage.getHeight(null)) {
                scale = (double) maxDim / (double) inImage.getWidth(null);
            }

            int scaledW = (int) (scale * inImage.getWidth(null));
            int scaledH = (int) (scale * inImage.getHeight(null));

            BufferedImage outImage = new BufferedImage(scaledW, scaledH,
            BufferedImage.TYPE_INT_RGB);

            AffineTransform tx = new AffineTransform();

            if (scale < 1.0d) {
                tx.scale(scale, scale);
            }

            Graphics2D g2d = outImage.createGraphics();
            g2d.drawImage(inImage, tx, null);
            g2d.dispose();

            new javax.swing.ImageIcon(outImage);
        } catch (Exception e) {
        }
    }

    private void cetak(String str) {
        System.out.println(str);
    }

    public class Painter extends Canvas {

        Image image;

        private void setImage(String file) {
            URL url = null;
            try {
                url = new File(file).toURI().toURL();
            } catch (MalformedURLException ex) {
                cetak(ex.toString());
            }
            image = getToolkit().getImage(url);
            repaint();
        }
        private void setImageIcon(ImageIcon file) {
            image = file.getImage();
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            double d = image.getHeight(this) / this.getHeight();
            double w = image.getWidth(this) / d;
            double x = this.getWidth() / 2 - w / 2;
            g.drawImage(image, (int) x, 0, (int) (w), this.getHeight(), this);
        }

        private void cetak(String str) {
            System.out.println(str);
        }
    }    
    
   public class NIOCopier {
        public NIOCopier(String asal, String tujuan) throws IOException {
            FileOutputStream outFile;
            try (FileInputStream inFile = new FileInputStream(asal)) {
                outFile = new FileOutputStream(tujuan);
                FileChannel outChannel;
                try (FileChannel inChannel = inFile.getChannel()) {
                    outChannel = outFile.getChannel();
                    for (ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                            inChannel.read(buffer) != -1;
                            buffer.clear()) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            outChannel.write(buffer);
                        }
                    }
                }
            outChannel.close();
            }
            outFile.close();
        }
    }

public static Connection getConnection() throws SQLException {
 try {
            // Lokasi file database.xml
            File xmlFile = new File("setting/database.xml");
            
            // Cek apakah file XML ada
            if (!xmlFile.exists()) {
                throw new SQLException("File database.xml tidak ditemukan pada path: " + xmlFile.getAbsolutePath());
            }

            // Buat DocumentBuilder untuk membaca file XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Normalisasi struktur XML
            doc.getDocumentElement().normalize();

            // Ambil elemen root <database>
            Element root = (Element) doc.getElementsByTagName("database").item(0);

            // Ambil elemen <url>, <username>, dan <password>
            String url = root.getElementsByTagName("url").item(0).getTextContent();
            String username = root.getElementsByTagName("username").item(0).getTextContent();
            String password = root.getElementsByTagName("password").item(0).getTextContent();

            // Debug output (opsional)
            System.out.println("URL: " + url);
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);

            // Menggunakan informasi dari XML untuk koneksi database
            Connection conn = DriverManager.getConnection(url, username, password);
            
            return conn;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Gagal membaca file database.xml atau membuat koneksi database: " + e.getMessage());
        }
    }

public String cariIsi3(String sql, String data1, String data2,String data3) {
    String dicari = "";
    try {
        ps = connect.prepareStatement(sql);
        try {
            ps.setString(1, data1);
            ps.setString(2, data2);
            ps.setString(3, data3);
            rs = ps.executeQuery();
            if (rs.next()) {
                dicari = rs.getString(1);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi: " + e);
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    } catch (Exception e) {
        System.out.println("Notifikasi: " + e);
    }
    return dicari;
}

public static String LamaJam(String waktuMulai, String waktuSelesai) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(waktuMulai);
            Date end = sdf.parse(waktuSelesai);

            long diffInMillies = end.getTime() - start.getTime();
            long diffInHours = diffInMillies / (60 * 60 * 1000);
            long diffInMinutes = (diffInMillies % (60 * 60 * 1000)) / (60 * 1000);

            return diffInHours + " jam " + diffInMinutes + " menit";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

public static long LamaMenit(String mulai, String selesai) {
    try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = sdf.parse(mulai);
        Date d2 = sdf.parse(selesai);
        return (d2.getTime() - d1.getTime()) / (60 * 1000);
    } catch (Exception e) {
        return 0;
    }
}

public static long selisihtime(String waktuAwal, String waktuAkhir) {
    try {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date awal = format.parse(waktuAwal);
        Date akhir = format.parse(waktuAkhir);
        return (akhir.getTime() - awal.getTime()) / 1000; // dalam detik
    } catch (Exception e) {
        return 0;
    }
}

public static ResultSet getResultSet(String query) {
    ResultSet rs = null;
    try {
        Connection conn = koneksiDB.condb(); // pastikan ini ambil koneksi aktif
        PreparedStatement ps = conn.prepareStatement(query);
        rs = ps.executeQuery();
    } catch (Exception e) {
        System.out.println("Error getResultSet: " + e);
    }
    return rs;
}

public double cariIsiAngka(String sql, String[] param) {
    angka2 = 0;
    try {
        ps = connect.prepareStatement(sql);
        for (int i = 0; i < param.length; i++) {
            ps.setString(i + 1, param[i]);
        }

        try {
            rs = ps.executeQuery();
            if (rs.next()) {
                angka2 = rs.getDouble(1);
            } else {
                angka2 = 0;
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    } catch (Exception e) {
        System.out.println("Notifikasi : " + e);
    }

    return angka2;
}

public boolean mengeditStok(String kodeBarang, double jumlah, boolean stokKeluar) {
    boolean hasil = true;
    PreparedStatement ps = null;

    try {
        String query = stokKeluar 
            ? "UPDATE ipsrsbarang SET stok = stok - ? WHERE kode_brng = ?" 
            : "UPDATE ipsrsbarang SET stok = stok + ? WHERE kode_brng = ?";

        ps = connect.prepareStatement(query);
        ps.setDouble(1, jumlah);
        ps.setString(2, kodeBarang);

        ps.executeUpdate();
    } catch (Exception e) {
        hasil = false;
        System.out.println("Notifikasi mengeditStok: " + e);
        JOptionPane.showMessageDialog(null, "Gagal update stok barang " + kodeBarang);
    } finally {
        try {
            if (ps != null) ps.close();
        } catch (Exception e) {
            System.out.println("Notifikasi close ps: " + e);
        }
    }

    return hasil;
}

public String cariString(String sql, String[] param){
    String dicari = "";
    try {
        ps = connect.prepareStatement(sql);
        for (int i = 0; i < param.length; i++) {
            ps.setString(i + 1, param[i]);
        }
        rs = ps.executeQuery();
        if(rs.next()){
            dicari = rs.getString(1);
        }
    } catch (Exception e) {
        System.out.println("Notifikasi cariString: " + e);
    } finally {
        try {
            if(rs != null) rs.close();
            if(ps != null) ps.close();
        } catch (Exception e) {
            System.out.println("Notifikasi closing: " + e);
        }
    }
    return dicari;
}

public boolean SimpanDariSelect(String tableTujuan, String fieldTujuan, String selectSql, String[] parameter) {

    boolean sukses = true;
    PreparedStatement ps = null;

    try {

        if (tableTujuan == null || tableTujuan.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama tabel tujuan kosong");
        }

        if (fieldTujuan == null || fieldTujuan.trim().isEmpty()) {
            throw new IllegalArgumentException("Field tujuan kosong");
        }

        if (selectSql == null || selectSql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL SELECT kosong");
        }

        String sql =
                "INSERT INTO " +
                tableTujuan +
                " (" +
                fieldTujuan +
                ") " +
                selectSql;

        System.out.println("SQL : " + sql);

        ps = connect.prepareStatement(sql);

        if (parameter != null) {
            for (int i = 0; i < parameter.length; i++) {
                ps.setString(i + 1, parameter[i]);
            }
        }

        ps.executeUpdate();

        if (AKTIFKANTRACKSQL.equals("yes")) {

            String trackSql = sql;

            if (parameter != null) {
                for (String param : parameter) {

                    String nilai =
                            (param == null)
                            ? "NULL"
                            : "'" + param.replace("'", "''") + "'";

                    trackSql = trackSql.replaceFirst(
                            "\\?",
                            Matcher.quoteReplacement(nilai)
                    );
                }
            }

            SimpanTrack(trackSql);
        }

    } catch (Exception e) {
        sukses = false;
        System.out.println("Notifikasi : " + e);
    } finally {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
        }
    }

    return sukses;
}

public boolean SimpanData(String table, String[] fields, String[] values) {
    boolean sukses = true;
    PreparedStatement ps = null;

    try {

        if (table == null || table.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama tabel kosong");
        }

        if (fields == null || values == null) {
            throw new IllegalArgumentException("Field atau value kosong");
        }

        if (fields.length != values.length) {
            throw new IllegalArgumentException(
                "Jumlah field (" + fields.length +
                ") tidak sama dengan jumlah value (" +
                values.length + ")"
            );
        }

        String fieldList = String.join(",", fields);

        StringBuilder param = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {
            param.append("?");

            if (i < fields.length - 1) {
                param.append(",");
            }
        }

        String sql =
                "INSERT INTO " +
                table +
                " (" +
                fieldList +
                ") VALUES (" +
                param +
                ")";

        System.out.println("SQL : " + sql);

        ps = connect.prepareStatement(sql);

        for (int i = 0; i < values.length; i++) {
            ps.setString(i + 1, values[i]);
        }

        ps.executeUpdate();

        if (AKTIFKANTRACKSQL.equals("yes")) {

            StringBuilder track = new StringBuilder();

            track.append("INSERT INTO ")
                 .append(table)
                 .append(" (")
                 .append(fieldList)
                 .append(") VALUES (");

            for (int i = 0; i < values.length; i++) {

                if (values[i] == null) {
                    track.append("NULL");
                } else {
                    track.append("'")
                         .append(values[i].replace("'", "''"))
                         .append("'");
                }

                if (i < values.length - 1) {
                    track.append(",");
                }
            }

            track.append(")");

            SimpanTrack(track.toString());
        }

    } catch (Exception e) {
        sukses = false;
        System.out.println("Notifikasi : " + e);
    } finally {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
        }
    }

    return sukses;
}

public String CariPetugas(String kode) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        JsonNode response;
        FileReader myObj=null;
        String iyem="";
        try {
            myObj = new FileReader("./cache/petugas.iyem");
            root = mapper.readTree(myObj);
            response = root.path("petugas");
            if(response.isArray()){
                for(JsonNode list:response){
                    if(list.path("NIP").asText().equalsIgnoreCase(kode)){
                        iyem=list.path("NamaPetugas").asText();
                        break;
                    }
                }
            }
            myObj.close();
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }finally {
            if (myObj != null) try { myObj.close(); } catch (Exception e) {}
            response = null;
            root = null;
        }
        if(iyem.equals("")){
            iyem=cariIsi("select petugas.nama from petugas where petugas.nip=?",kode);
        }
        return iyem;
    }

    public String CariDokter(String kode) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        JsonNode response;
        FileReader myObj=null;
        String iyem="";
        try {
            myObj = new FileReader("./cache/dokter.iyem");
            root = mapper.readTree(myObj);
            response = root.path("dokter");
            if(response.isArray()){
                for(JsonNode list:response){
                    if(list.path("KodeDokter").asText().equalsIgnoreCase(kode)){
                        iyem=list.path("NamaDokter").asText();
                        break;
                    }
                }
            }
            myObj.close();
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }finally {
            if (myObj != null) try { myObj.close(); } catch (Exception e) {}
            response = null;
            root = null;
        }
        if(iyem.equals("")){
            iyem=cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?",kode);
        }
        return iyem;
    }
    
}
