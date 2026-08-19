/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fungsi;

import AESsecurity.EnkripsiAES;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author khanzasoft
 */
public class koneksiDB {
    private static Connection connection=null;
    private static final Properties prop = new Properties();  
    private static final MysqlDataSource dataSource=new MysqlDataSource();
    private static String var="";
    
    public koneksiDB(){} 
    
    private static synchronized void loadConfig() throws Exception{
        prop.clear();
        File fileEksternal=new File("setting/database.xml");
        InputStream input=null;
        if(fileEksternal.exists()){
            input=new FileInputStream(fileEksternal);
        }else{
            input=koneksiDB.class.getResourceAsStream("/setting/database.xml");
        }
        if(input==null){
            throw new Exception("setting/database.xml tidak ditemukan, baik di luar maupun di dalam JAR");
        }
        try(InputStream in=input){
            prop.loadFromXML(in);
        }
    }
    
    public static Connection condb(){ 
        if(connection == null){
            try{
                loadConfig();
                dataSource.setURL("jdbc:mysql://"+EnkripsiAES.decrypt(prop.getProperty("HOST"))+":"+EnkripsiAES.decrypt(prop.getProperty("PORT"))+"/"+EnkripsiAES.decrypt(prop.getProperty("DATABASE"))+"?zeroDateTimeBehavior=convertToNull&autoReconnect=true&useCompression=true");
                dataSource.setUser(EnkripsiAES.decrypt(prop.getProperty("USER")));
                dataSource.setPassword(EnkripsiAES.decrypt(prop.getProperty("PAS")));
                connection=dataSource.getConnection();       
                System.out.println("");
            }catch(Exception e){
                System.out.println("Notif : "+e);
                try {
                    if((connection==null)||(connection.isClosed())){
                        loadConfig();
                        dataSource.setURL("jdbc:mysql://"+EnkripsiAES.decrypt(prop.getProperty("HOST"))+":"+EnkripsiAES.decrypt(prop.getProperty("PORT"))+"/"+EnkripsiAES.decrypt(prop.getProperty("DATABASE"))+"?zeroDateTimeBehavior=convertToNull&amp;autoReconnect=true&amp;cachePrepStmts=true");
                        dataSource.setUser(EnkripsiAES.decrypt(prop.getProperty("USER")));
                        dataSource.setPassword(EnkripsiAES.decrypt(prop.getProperty("PAS")));
                        connection=dataSource.getConnection();  
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,"Koneksi Putus : "+e);
                }
            }
        }
        return connection;        
    }
    
    public static String HOST(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("HOST"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DATABASE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("DATABASE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PORT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USER(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USER"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CARICEPAT(){
        try{
            loadConfig();
            var=prop.getProperty("CARICEPAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HOSTHYBRIDWEB(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("HOSTHYBRIDWEB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERHYBRIDWEB(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERHYBRIDWEB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASHYBRIDWEB(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASHYBRIDWEB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HYBRIDWEB(){
        try{
            loadConfig();
            var=prop.getProperty("HYBRIDWEB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORTWEB(){
        try{
            loadConfig();
            var=prop.getProperty("PORTWEB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ANTRIAN(){
        try{
            loadConfig();
            var=prop.getProperty("ANTRIAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMAPOTEK(){
        try{
            loadConfig();
            var=prop.getProperty("ALARMAPOTEK");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FORMALARMAPOTEK(){
        try{
            loadConfig();
            var=prop.getProperty("FORMALARMAPOTEK");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMLAB(){
        try{
            loadConfig();
            var=prop.getProperty("ALARMLAB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FORMALARMLAB(){
        try{
            loadConfig();
            var=prop.getProperty("FORMALARMLAB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMRADIOLOGI(){
        try{
            loadConfig();
            var=prop.getProperty("ALARMRADIOLOGI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FORMALARMRADIOLOGI(){
        try{
            loadConfig();
            var=prop.getProperty("FORMALARMRADIOLOGI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMRSISRUTE(){
        try{
            loadConfig();
            var=prop.getProperty("ALARMRSISRUTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMBOOKINGPERIKSA(){
        try{
            loadConfig();
            var=prop.getProperty("ALARMBOOKINGPERIKSA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMPERMINTAANRANAP(){
        try{
            loadConfig();
            var=prop.getProperty("ALARMPERMINTAANRANAP");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMPENGADUANPASIEN(){
        try{
            loadConfig();
            var=prop.getProperty("ALARMPENGADUANPASIEN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String MENUTRANSPARAN(){
        try{
            loadConfig();
            var=prop.getProperty("MENUTRANSPARAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIBPJS(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIBPJS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIBPJS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIBPJS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIAPLICARE(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIAPLICARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIAPLICARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIAPLICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIAPLICARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIAPLICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIAPLICARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIAPLICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIMOBILEJKN(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIMOBILEJKN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIMOBILEJKN(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIMOBILEJKN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIMOBILEJKN(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIMOBILEJKN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIMOBILEJKN(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIMOBILEJKN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIAPOTEKBPJS(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIAPOTEKBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIAPOTEKBPJS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIAPOTEKBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIAPOTEKBPJS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIAPOTEKBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIAPOTEKBPJS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIAPOTEKBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String JADIKANPIUTANGAPOTEKBPJS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("JADIKANPIUTANGAPOTEKBPJS"));
        }catch(Exception e){
            var="no"; 
        }
        return var;
    }
    
    public static String URLAPIPCARE(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIPCARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIPCARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIPCARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIPCARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSPCARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERPCARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DIVREGPCARE(){
        try{
            loadConfig();
            var=prop.getProperty("DIVREGPCARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KACABPCARE(){
        try{
            loadConfig();
            var=prop.getProperty("KACABPCARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISISRUTE(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPISISRUTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSISRUTE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("IDSISRUTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSSISRUTE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSSISRUTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISIRS(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPISIRS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSIRS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("IDSIRS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSSIRS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSSIRS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPICORONA(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPICORONA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDCORONA(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("IDCORONA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSCORONA(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSCORONA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISITT(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPISITT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSITT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("IDSITT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSSITT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSSITT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KABUPATENSITT(){
        try{
            loadConfig();
            var=prop.getProperty("KABUPATENSITT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KAMARAKTIFRANAP(){
        try{
            loadConfig();
            var=prop.getProperty("KAMARAKTIFRANAP").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DOKTERAKTIFKASIRRALAN(){
        try{
            loadConfig();
            var=prop.getProperty("DOKTERAKTIFKASIRRALAN").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String POLIAKTIFKASIRRALAN(){
        try{
            loadConfig();
            var=prop.getProperty("POLIAKTIFKASIRRALAN").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String RUANGANAKTIFINVENTARIS(){
        try{
            loadConfig();
            var=prop.getProperty("RUANGANAKTIFINVENTARIS").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String BASENOREG(){
        try{
            loadConfig();
            var=prop.getProperty("BASENOREG");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VALIDASIULANGBERIOBAT(){
        try{
            loadConfig();
            var=prop.getProperty("VALIDASIULANGBERIOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URUTNOREG(){
        try{
            loadConfig();
            var=prop.getProperty("URUTNOREG");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String JADWALDOKTERDIREGISTRASI(){
        try{
            loadConfig();
            var=prop.getProperty("JADWALDOKTERDIREGISTRASI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IPPRINTERTRACER(){
        try{
            loadConfig();
            var=prop.getProperty("IPPRINTERTRACER");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIINHEALTH(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIINHEALTH");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String TOKENINHEALTH(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("TOKENINHEALTH"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PEMBULATANHARGAOBAT(){
        try{
            loadConfig();
            var=prop.getProperty("PEMBULATANHARGAOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANBATCHOBAT(){
        try{
            loadConfig();
            var=prop.getProperty("AKTIFKANBATCHOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CETAKRINCIANOBAT(){
        try{
            loadConfig();
            var=prop.getProperty("CETAKRINCIANOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANBILLINGPARSIAL(){
        try{
            loadConfig();
            var=prop.getProperty("AKTIFKANBILLINGPARSIAL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLDUKCAPILJAKARTA(){
        try{
            loadConfig();
            var=prop.getProperty("URLDUKCAPILJAKARTA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERDUKCAPILJAKARTA(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERDUKCAPILJAKARTA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSDUKCAPILJAKARTA(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSDUKCAPILJAKARTA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VAR1DUKCAPILJAKARTA(){
        try{
            loadConfig();
            var=prop.getProperty("VAR1DUKCAPILJAKARTA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VAR2DUKCAPILJAKARTA(){
        try{
            loadConfig();
            var=prop.getProperty("VAR2DUKCAPILJAKARTA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLDUKCAPIL(){
        try{
            loadConfig();
            var=prop.getProperty("URLDUKCAPIL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERDUKCAPIL(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERDUKCAPIL"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSDUKCAPIL(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSDUKCAPIL"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IPUSERDUKCAPIL(){
        try{
            loadConfig();
            var=prop.getProperty("IPUSERDUKCAPIL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANTRACKSQL(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("AKTIFKANTRACKSQL"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HOSTWSLICA(){
        try{
            loadConfig();
            var=prop.getProperty("HOSTWSLICA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KEYWSLICA(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("KEYWSLICA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DEPOAKTIFOBAT(){
        try{
            loadConfig();
            var=prop.getProperty("DEPOAKTIFOBAT").replaceAll("'","");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String STOKKOSONGRESEP(){
        try{
            loadConfig();
            var=prop.getProperty("STOKKOSONGRESEP");
        }catch(Exception e){
            var="no"; 
        }
        return var;
    }
    
    public static String HPPFARMASI(){
        try{
            loadConfig();
            if(prop.getProperty("HPPFARMASI").equals("h_beli")){
                var="h_beli";
            }else{
                var="dasar";
            }
        }catch(Exception e){
            var="dasar"; 
        }
        return var;
    }
    
    public static String HPPTOKO(){
        try{
            loadConfig();
            if(prop.getProperty("HPPTOKO").equals("h_beli")){
                var="h_beli";
            }else{
                var="dasar";
            }
        }catch(Exception e){
            var="dasar"; 
        }
        return var;
    }
    
    public static String URLAPIMEDQLAB(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIMEDQLAB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIMEDQLAB(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIMEDQLAB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIMEDQLAB(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIMEDQLAB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLCARESTREAM(){
        try{
            loadConfig();
            var=prop.getProperty("URLCARESTREAM");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISOFTMEDIX(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPISOFTMEDIX");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PRODUCTSOFTMEDIX(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PRODUCTSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VERSIONSOFTMEDIX(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("VERSIONSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERIDSOFTMEDIX(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERIDSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KEYSOFTMEDIX(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("KEYSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String RESEPRAJALKEPLAN(){
        try{
            loadConfig();
            var=prop.getProperty("RESEPRAJALKEPLAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DIAGNOSARUJUKANMASUKAPIBPJS(){
        try{
            loadConfig();
            var=prop.getProperty("DIAGNOSARUJUKANMASUKAPIBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANWARNARALAN(){
        try{
            loadConfig();
            var=prop.getProperty("AKTIFKANWARNARALAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CLIENTIDSATUSEHAT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CLIENTIDSATUSEHAT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYSATUSEHAT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYSATUSEHAT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSATUSEHAT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("IDSATUSEHAT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAUTHSATUSEHAT(){
        try{
            loadConfig();
            var=prop.getProperty("URLAUTHSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLFHIRSATUSEHAT(){
        try{
            loadConfig();
            var=prop.getProperty("URLFHIRSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KELURAHANSATUSEHAT(){
        try{
            loadConfig();
            var=prop.getProperty("KELURAHANSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KECAMATANSATUSEHAT(){
        try{
            loadConfig();
            var=prop.getProperty("KECAMATANSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KABUPATENSATUSEHAT(){
        try{
            loadConfig();
            var=prop.getProperty("KABUPATENSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PROPINSISATUSEHAT(){
        try{
            loadConfig();
            var=prop.getProperty("PROPINSISATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KODEPOSSATUSEHAT(){
        try{
            loadConfig();
            var=prop.getProperty("KODEPOSSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERORTHANC(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERORTHANC"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSORTHANC(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSORTHANC"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORTORTHANC(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PORTORTHANC"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLORTHANC(){
        try{
            loadConfig();
            var=prop.getProperty("URLORTHANC");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ADDANTRIANAPIMOBILEJKN(){
        try{
            loadConfig();
            var=prop.getProperty("ADDANTRIANAPIMOBILEJKN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String JADIKANBOOKINGSURATKONTROLAPIBPJS(){
        try{
            loadConfig();
            var=prop.getProperty("JADIKANBOOKINGSURATKONTROLAPIBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIICARE(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPIICARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIICARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIICARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIICARE(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISMARTCLAIM(){
        try{
            loadConfig();
            var=prop.getProperty("URLAPISMARTCLAIM");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPISMARTCLAIM(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPISMARTCLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPISMARTCLAIM(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPISMARTCLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPISMARTCLAIM(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPISMARTCLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String TANGGALMUNDUR(){
        try{
            loadConfig();
            var=prop.getProperty("TANGGALMUNDUR");
        }catch(Exception e){
            var="yes"; 
        }
        return var;
    }
    
    public static String URLMOBILEJKNFKTP(){
        try{
            loadConfig();
            var=prop.getProperty("URLMOBILEJKNFKTP");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYMOBILEJKNFKTP(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDMOBILEJKNFKTP(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYMOBILEJKNFKTP(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSMOBILEJKNFKTP(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("PASSMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERMOBILEJKNFKTP(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("USERMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHPEMBAYARANPIHAKKETIGA(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHPEMBAYARANPIHAKKETIGA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHPEMBAYARANPAJAK(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHPEMBAYARANPAJAK"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHPEMBAYARANVIRTUALACCOUNT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHPEMBAYARANVIRTUALACCOUNT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHACK(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHACK"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHMT940(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHMT940"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIHOST(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIHOST"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPORT(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPORT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIUSER(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIUSER"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPAS(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPAS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KUNCIDOKTERRANAP(){
        try{
            loadConfig();
            var=EnkripsiAES.decrypt(prop.getProperty("KUNCIDOKTERRANAP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ADDANTRIANAPIMOBILEJKNFKTP(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("ADDANTRIANAPIMOBILEJKNFKTP");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String NOTIFWAKONSUL(){
        try{
            loadConfig();
            var=prop.getProperty("NOTIFWAKONSUL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String NOTIFWASURATKONTROL(){
        try{
            loadConfig();
            var=prop.getProperty("NOTIFWASURATKONTROL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDGROUPWAKONSUL(){
        try{
            loadConfig();
            var=prop.getProperty("IDGROUPWAKONSUL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDGROUPWAINFORMASI(){
        try{
            loadConfig();
            var=prop.getProperty("IDGROUPWAINFORMASI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDGROUPWASEPHAPUS(){
        try{
            loadConfig();
            var=prop.getProperty("IDGROUPWASEPHAPUS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FOLDERFILEWA(){
        try{
            loadConfig();
            var=prop.getProperty("FOLDERFILEWA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String NOMORADMIN(){
        try{
            loadConfig();
            var=prop.getProperty("NOMORADMIN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PESANWAKONTROLBPJS(){
        try{
            loadConfig();
            var=prop.getProperty("PESANWAKONTROLBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String JADWALPENDAFTARANWA(){
        try{
            loadConfig();
            var=prop.getProperty("JADWALPENDAFTARANWA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    
    public static String URLAPIESIGN(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("URLAPIESIGN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERNAMEAPIESIGN(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("USERNAMEAPIESIGN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSAPIESIGN(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("PASSAPIESIGN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPFILEESIGNHOST(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPFILEESIGNHOST"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPFILEESIGNPORT(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPFILEESIGNPORT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPFILEESIGNUSER(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPFILEESIGNUSER"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPFILEESIGNPAS(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPFILEESIGNPAS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPFILEESIGNFOLDER(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPFILEESIGNFOLDER"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAKSESFILEESIGN(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("URLAKSESFILEESIGN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISERTISIGN(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("URLAPISERTISIGN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String APIKEYSERTISIGN(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=EnkripsiAES.decrypt(prop.getProperty("APIKEYSERTISIGN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLDOKUMENSERTISIGN(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("URLDOKUMENSERTISIGN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FOLDERTTE(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("FOLDERTTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String NORESEPAKTIF(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("NORESEPAKTIF");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KODEKEMENKES(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("KODEKEMENKES");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KODERS(){
        try (FileInputStream fis = new FileInputStream("setting/database.xml")) {
            prop.loadFromXML(fis);
            var=prop.getProperty("KODERS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
   
}