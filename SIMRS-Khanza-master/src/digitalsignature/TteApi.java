package digitalsignature;

//import antrian.*;
import bridging.*;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;

public class TteApi {        
    private static final Properties prop = new Properties();
    private String Key,Consid,proxy_ip,proxy_port;
    private static String var;
    private long GetUTCdatetimeAsString;
    private String salt;
    private String generateHmacSHA256Signature;
    private byte[] hmacData;
    private Mac mac;
    private long millis;
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private SecretKeySpec secretKey;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;
    
    public TteApi(){
        try {
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            Key = prop.getProperty("SECRETKEYAPIBPJS");
            Consid = prop.getProperty("CONSIDAPIBPJS");
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
    }
    public String getHmac() {        
        GetUTCdatetimeAsString = GetUTCdatetimeAsString();        
        salt = Consid +"&"+String.valueOf(GetUTCdatetimeAsString);
	generateHmacSHA256Signature = null;
	try {
	    generateHmacSHA256Signature = generateHmacSHA256Signature(salt,Key);
	} catch (GeneralSecurityException e) {
	    // TODO Auto-generated catch block
            System.out.println("Error Signature : "+e);
	    e.printStackTrace();
	}
	return generateHmacSHA256Signature;
    }

    public String generateHmacSHA256Signature(String data, String key)throws GeneralSecurityException {
        hmacData = null;
	try {
            secretKey = new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
	    mac = Mac.getInstance("HmacSHA256");
	    mac.init(secretKey);
	    hmacData = mac.doFinal(data.getBytes("UTF-8"));
	    return new String(Base64.encode(hmacData), "UTF-8");
	} catch (UnsupportedEncodingException e) {
            System.out.println("Error Generate HMac: e");
	    throw new GeneralSecurityException(e);
	}
    }
        
    public long GetUTCdatetimeAsString(){    
        millis = System.currentTimeMillis();   
        return millis/1000;
    }
    
    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        
             SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//                proxy_ip=prop.getProperty("PROXY_IP");
//                proxy_port=prop.getProperty("PROXY_PORT");
//                int port=Integer.parseInt(proxy_port);
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_ip, port));
//            requestFactory.setProxy(proxy);
            return new RestTemplate(requestFactory);
        
        
    }
    public static String URLTTE(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("URLTTE");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }
    
    public static String NOANTRIAN(){
        try{
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            var=prop.getProperty("NOANTRIANDARIMESIN");
        }catch(Exception e){
            var=""; 
        }
        return var;
    }

}
