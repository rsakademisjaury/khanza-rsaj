/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fungsi;

import AESsecurity.EnkripsiAES;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.FileInputStream;
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
    private static final Properties propVer = new Properties();
    private static final MysqlDataSource dataSource=new MysqlDataSource();
    private static String var="";
    
    public koneksiDB(){} 
    public static Connection condb(){ 
        if(connection == null){
            try{
                prop.loadFromXML(new FileInputStream("setting/database.xml"));
//                propVer.loadFromXML(new FileInputStream("setting/version.xml"));
                dataSource.setURL("jdbc:mysql://"+EnkripsiAES.decrypt(prop.getProperty("HOST"))+":"+EnkripsiAES.decrypt(prop.getProperty("PORT"))+"/"+EnkripsiAES.decrypt(prop.getProperty("DATABASE"))+"?zeroDateTimeBehavior=convertToNull&autoReconnect=true&useCompression=true");
//                dataSource.setURL("jdbc:mysql://"+EnkripsiAES.decrypt(prop.getProperty("HOST"))+":"+EnkripsiAES.decrypt(prop.getProperty("PORT"))+"/"+EnkripsiAES.decrypt(prop.getProperty("DATABASE"))+"?zeroDateTimeBehavior=convertToNull&autoReconnect=true&useCompression=true");
                dataSource.setUser(EnkripsiAES.decrypt(prop.getProperty("USER")));
                dataSource.setPassword(EnkripsiAES.decrypt(prop.getProperty("PAS")));
                connection=dataSource.getConnection();       
                System.out.println(""+                        
                        "      _                            _             _                _     \n"+
                        "  ___(_)_ __ ___  _ __ ___    __ _| | ____ _  __| | ___ _ __ ___ (_)___ \n"+
                        " / __| | '_ ` _ \\| '__/ __|  / _` | |/ / _` |/ _` |/ _ \\ '_ ` _ \\| / __| \n"+
                        " \\__ \\ | | | | | | |  \\__ \\ | (_| |   < (_| | (_| |  __/ | | | | | \\__ \\ \n"+
                        " |___/_|_| |_| |_|_|  |___/  \\__,_|_|\\_\\__,_|\\__,_|\\___|_| |_| |_|_|___/ \n"+
                        "                                                                    \n"+
                        "                                              Powered by SIMKES Khanza                       \n"+
                        "");
                
                
            }catch(Exception e){
                System.out.println("Notif : "+e);
                try {
                    if(connection.isClosed()){
                        prop.loadFromXML(new FileInputStream("setting/database.xml"));
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
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("HOST"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DATABASE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("DATABASE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PORT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USER(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USER"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CARICEPAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("CARICEPAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HOSTHYBRIDWEB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("HOSTHYBRIDWEB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERHYBRIDWEB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERHYBRIDWEB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASHYBRIDWEB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASHYBRIDWEB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HYBRIDWEB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("HYBRIDWEB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORTWEB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("PORTWEB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ANTRIAN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ANTRIAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMAPOTEK(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ALARMAPOTEK");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FORMALARMAPOTEK(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("FORMALARMAPOTEK");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMLAB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ALARMLAB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FORMALARMLAB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("FORMALARMLAB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMRADIOLOGI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ALARMRADIOLOGI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String FORMALARMRADIOLOGI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("FORMALARMRADIOLOGI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMRSISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ALARMRSISRUTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMBOOKINGPERIKSA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ALARMBOOKINGPERIKSA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMPERMINTAANRANAP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ALARMPERMINTAANRANAP");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ALARMPENGADUANPASIEN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ALARMPENGADUANPASIEN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String MENUTRANSPARAN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("MENUTRANSPARAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIAPLICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIAPLICARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIAPLICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIAPLICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIAPLICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIAPLICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIAPLICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIAPLICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIMOBILEJKN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIMOBILEJKN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIMOBILEJKN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIMOBILEJKN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIMOBILEJKN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIMOBILEJKN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIMOBILEJKN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIMOBILEJKN"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIAPOTEKBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIAPOTEKBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIAPOTEKBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIAPOTEKBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIAPOTEKBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIAPOTEKBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIAPOTEKBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIAPOTEKBPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIAΡΟΤΕΚΒPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIAΡΟΤΕΚΒPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIAPΟΤΕΚΒPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIAPΟΤΕΚΒPJS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }       
    
    public static String JADIKANPIUTANGAPOTEKBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("JADIKANPIUTANGAPOTEKBPJS"));
        }catch(Exception e){
            var="no"; 
        }
        return var;
    }
    
    public static String URLAPIPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIPCARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERPCARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DIVREGPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("DIVREGPCARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KACABPCARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KACABPCARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLFINGERPRINTBPJS() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var = prop.getProperty("URLFINGERPRINTBPJS");
        } catch (Exception e) {
            var = "";
        }
        return var;
    }

    public static String URLAPLIKASIFINGERPRINTBPJS() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var = prop.getProperty("URLAPLIKASIFINGERPRINTBPJS");
        } catch (Exception e) {
            var = "";
        }
        return var;
    }

    public static String USERFINGERPRINTBPJS() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var = EnkripsiAES.decrypt(prop.getProperty("USERFINGERPRINTBPJS"));
        } catch (Exception e) {
            var = "";
        }
        return var;
    }

    public static String PASSWORDFINGERPRINTBPJS() {
        try {
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var = EnkripsiAES.decrypt(prop.getProperty("PASSWORDFINGERPRINTBPJS"));
        } catch (Exception e) {
            var = "";
        }
        return var;
    }
    
    public static String URLAPISISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPISISRUTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("IDSISRUTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSSISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSSISRUTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISIRS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPISIRS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSIRS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("IDSIRS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSSIRS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSSIRS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPICORONA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPICORONA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDCORONA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("IDCORONA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSCORONA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSCORONA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISITT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPISITT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSITT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("IDSITT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSSITT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSSITT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KABUPATENSITT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KABUPATENSITT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KAMARAKTIFRANAP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KAMARAKTIFRANAP").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DOKTERAKTIFKASIRRALAN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("DOKTERAKTIFKASIRRALAN").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String POLIAKTIFKASIRRALAN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("POLIAKTIFKASIRRALAN").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String RUANGANAKTIFINVENTARIS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("RUANGANAKTIFINVENTARIS").replaceAll("'","");;
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String BASENOREG(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("BASENOREG");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VALIDASIULANGBERIOBAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("VALIDASIULANGBERIOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URUTNOREG(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URUTNOREG");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String JADWALDOKTERDIREGISTRASI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("JADWALDOKTERDIREGISTRASI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IPPRINTERTRACER(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("IPPRINTERTRACER");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIINHEALTH(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIINHEALTH");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String TOKENINHEALTH(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("TOKENINHEALTH"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PEMBULATANHARGAOBAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("PEMBULATANHARGAOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANBATCHOBAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("AKTIFKANBATCHOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CETAKRINCIANOBAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("CETAKRINCIANOBAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANBILLINGPARSIAL(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("AKTIFKANBILLINGPARSIAL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLDUKCAPILJAKARTA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLDUKCAPILJAKARTA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERDUKCAPILJAKARTA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERDUKCAPILJAKARTA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSDUKCAPILJAKARTA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSDUKCAPILJAKARTA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VAR1DUKCAPILJAKARTA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("VAR1DUKCAPILJAKARTA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VAR2DUKCAPILJAKARTA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("VAR2DUKCAPILJAKARTA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLDUKCAPIL(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLDUKCAPIL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERDUKCAPIL(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERDUKCAPIL"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSDUKCAPIL(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSDUKCAPIL"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IPUSERDUKCAPIL(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("IPUSERDUKCAPIL");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANTRACKSQL(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("AKTIFKANTRACKSQL"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HOSTWSLICA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("HOSTWSLICA");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KEYWSLICA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("KEYWSLICA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DEPOAKTIFOBAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("DEPOAKTIFOBAT").replaceAll("'","");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String STOKKOSONGRESEP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("STOKKOSONGRESEP");
        }catch(Exception e){
            var="no"; 
        }
        return var;
    }
    
    public static String HPPFARMASI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
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
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
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
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIMEDQLAB");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIMEDQLAB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIMEDQLAB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIMEDQLAB(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIMEDQLAB"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLCARESTREAM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLCARESTREAM");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISOFTMEDIX(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPISOFTMEDIX");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PRODUCTSOFTMEDIX(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PRODUCTSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String VERSIONSOFTMEDIX(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("VERSIONSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERIDSOFTMEDIX(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERIDSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KEYSOFTMEDIX(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("KEYSOFTMEDIX"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String RESEPRAJALKEPLAN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("RESEPRAJALKEPLAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String DIAGNOSARUJUKANMASUKAPIBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("DIAGNOSARUJUKANMASUKAPIBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String AKTIFKANWARNARALAN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("AKTIFKANWARNARALAN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CLIENTIDSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CLIENTIDSATUSEHAT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYSATUSEHAT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("IDSATUSEHAT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAUTHSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAUTHSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLFHIRSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLFHIRSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KELURAHANSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KELURAHANSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KECAMATANSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KECAMATANSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KABUPATENSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KABUPATENSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PROPINSISATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("PROPINSISATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KODEPOSSATUSEHAT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KODEPOSSATUSEHAT");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERORTHANC(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERORTHANC"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSORTHANC(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSORTHANC"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORTORTHANC(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PORTORTHANC"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLORTHANC(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLORTHANC");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String ADDANTRIANAPIMOBILEJKN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("ADDANTRIANAPIMOBILEJKN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String JADIKANBOOKINGSURATKONTROLAPIBPJS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("JADIKANBOOKINGSURATKONTROLAPIBPJS");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIICARE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIICARE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIICARE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPISMARTCLAIM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPISMARTCLAIM");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPISMARTCLAIM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPISMARTCLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPISMARTCLAIM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPISMARTCLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPISMARTCLAIM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPISMARTCLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String TANGGALMUNDUR(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("TANGGALMUNDUR");
        }catch(Exception e){
            var="yes"; 
        }
        return var;
    }
    
    public static String URLMOBILEJKNFKTP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLMOBILEJKNFKTP");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYMOBILEJKNFKTP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDMOBILEJKNFKTP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYMOBILEJKNFKTP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSMOBILEJKNFKTP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERMOBILEJKNFKTP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERMOBILEJKNFKTP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHPEMBAYARANPIHAKKETIGA(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHPEMBAYARANPIHAKKETIGA"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHPEMBAYARANPAJAK(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHPEMBAYARANPAJAK"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPATHPEMBAYARANVIRTUALACCOUNT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPATHPEMBAYARANVIRTUALACCOUNT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIHOST(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIHOST"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPORT(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPORT"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIUSER(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIUSER"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SFTPMANDIRIPAS(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SFTPMANDIRIPAS"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KUNCIDOKTERRANAP(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("KUNCIDOKTERRANAP"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KEYEKLAIM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("KEYEKLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String WSEKLAIM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("WSEKLAIM"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String KELASRSEKLAIM(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("KELASRSEKLAIM");
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

    public static String URLSERVERTTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLSERVERTTE");
        }catch(Exception e){
            var=""; 
}
        return var;
    }
    
    public static String URLSERVERVPN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLSERVERVPN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIINTEGRATION(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIINTEGRATION");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDINTEGRATION(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("IDINTEGRATION"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSINTEGRATION(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSINTEGRATION"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String URLAPIINTEGRATIONFASKES(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIINTEGRATIONFASKES");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String IDINTEGRATIONFASKES(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("IDINTEGRATIONFASKES"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSINTEGRATIONFASKES(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSINTEGRATIONFASKES"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HOSTHYBRIDWEBTTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("HOSTHYBRIDWEBTTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERHYBRIDWEBTTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERHYBRIDWEBTTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASHYBRIDWEBTTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASHYBRIDWEBTTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HYBRIDWEBTTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("HYBRIDWEBTTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORTWEBTTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("PORTWEBTTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HOSTHYBRIDWEBFARMASI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("HOSTHYBRIDWEBFARMASI"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERHYBRIDWEBFARMASI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERHYBRIDWEBFARMASI"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASHYBRIDWEBFARMASI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASHYBRIDWEBFARMASI"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String HYBRIDWEBFARMASI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("HYBRIDWEBFARMASI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PORTWEBFARMASI(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("PORTWEBFARMASI");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
        
    public static String USERNAMEAPIBSRE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERNAMEAPIBSRE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String PASSWORDAPIBSRE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("PASSWORDAPIBSRE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }    

public static String DELETE_TOKEN() {
    return "19620710";
}   
    
/*
 * Tempelkan method-method ini di file:
 * src/fungsi/koneksiDB.java
 *
 * Letakkan berdekatan dengan method BPJS yang sudah ada, misalnya setelah:
 *   URLAPIBPJS()
 *   SECRETKEYAPIBPJS()
 *   CONSIDAPIBPJS()
 *   USERKEYAPIBPJS()
 *
 * Catatan:
 * - URL, ENV, KODEFASKES, KODEPPK dibaca plain prop.getProperty().
 * - CONSID, SECRETKEY, USERKEY dibaca dengan EnkripsiAES.decrypt(...), sama seperti credential BPJS lain.
 */

public static String URLAPIBPJSSISRUTE(){
    try{
        prop.loadFromXML(new FileInputStream("setting/database.xml"));
        var=prop.getProperty("URLAPIBPJSSISRUTE");
    }catch(Exception e){
        var="";
    }
    return var;
}

public static String CONSIDAPIBPJSSISRUTE(){
    try{
        prop.loadFromXML(new FileInputStream("setting/database.xml"));
        var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIBPJSSISRUTE"));
    }catch(Exception e){
        var="";
    }
    return var;
}

public static String SECRETKEYAPIBPJSSISRUTE(){
    try{
        prop.loadFromXML(new FileInputStream("setting/database.xml"));
        var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIBPJSSISRUTE"));
    }catch(Exception e){
        var="";
    }
    return var;
}

public static String USERKEYAPIBPJSSISRUTE(){
    try{
        prop.loadFromXML(new FileInputStream("setting/database.xml"));
        var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIBPJSSISRUTE"));
    }catch(Exception e){
        var="";
    }
    return var;
}

public static String KODEFASKESSATUSEHATRUJUKAN(){
    try{
        prop.loadFromXML(new FileInputStream("setting/database.xml"));
        var=prop.getProperty("KODEFASKESSATUSEHATRUJUKAN");
    }catch(Exception e){
        var="";
    }
    return var;
}

public static String KODEPPKBPJSSISRUTE(){
    try{
        prop.loadFromXML(new FileInputStream("setting/database.xml"));
        var=prop.getProperty("KODEPPKBPJSSISRUTE");
    }catch(Exception e){
        var="";
    }
    return var;
}

public static String ENVBPJSSISRUTE(){
    try{
        prop.loadFromXML(new FileInputStream("setting/database.xml"));
        var=prop.getProperty("ENVBPJSSISRUTE");
    }catch(Exception e){
        var="dev";
    }
    return var;
}

public static String URLAPIBPJSRUJUKANSISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLAPIBPJSRUJUKANSISRUTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String SECRETKEYAPIBPJSRUJUKANSISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("SECRETKEYAPIBPJSRUJUKANSISRUTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String CONSIDAPIBPJSRUJUKANSISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("CONSIDAPIBPJSRUJUKANSISRUTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String USERKEYAPIBPJSRUJUKANSISRUTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=EnkripsiAES.decrypt(prop.getProperty("USERKEYAPIBPJSRUJUKANSISRUTE"));
        }catch(Exception e){
            var=""; 
        }
        return var;
    }

}

