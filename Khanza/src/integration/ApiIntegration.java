package integration;

import bridging.*;
import fungsi.koneksiDB;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class ApiIntegration {        
    private static final Properties prop = new Properties();
    private String Key,pass,proxy_ip,proxy_port;
//    private Scheme scheme;
//    private HttpComponentsClientHttpRequestFactory factory;
    private SimpleClientHttpRequestFactory requestFactory;
    public ApiIntegration(){
        try {            
            prop.loadFromXML(new FileInputStream("setting/database.xml"));   
            pass = koneksiDB.PASSCORONA();
//            pass = "123456";
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
    }
    public String getHmac() { 
//         System.out.println("Notifikasi : "+pass);
//        try {                    
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] hashInBytes = md.digest(pass.getBytes(StandardCharsets.UTF_8));
//
//            StringBuilder sb = new StringBuilder();
//            for (byte b : hashInBytes) {
//                sb.append(String.format("%02x", b));
//            }
//            Key=sb.toString();            
//        } catch (Exception ex) {
//            System.out.println("Notifikasi : "+ex);
//        }
//System.out.println("Notifikasi : "+pass);
        try {                    
           
            Key=pass;            
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
	return Key;
    }
     public String getHmacEncript() { 
//         System.out.println("Notifikasi : "+pass);
        try {                    
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(pass.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            Key=sb.toString();            
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
	return Key;
    }

    public long GetUTCdatetimeAsString(){    
        long millis = System.currentTimeMillis(); 
        
        return millis/1000;
    }
    
    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        if( prop.getProperty("PROXY").equals("YES")){
            
//            System.out.println("Notif Cari Poli : "+prop.getProperty("PROXY"));
            requestFactory = new SimpleClientHttpRequestFactory();
            proxy_ip=prop.getProperty("PROXY_IP");
            proxy_port=prop.getProperty("PROXY_PORT");
            int port=Integer.parseInt(proxy_port);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_ip, port));
            requestFactory.setProxy(proxy);
          //  System.out.println("Notif Cari Poli : "+requestFactory);
            return new RestTemplate(requestFactory);
        }
        else
        {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        javax.net.ssl.TrustManager[] trustManagers= {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
            }
        };
        sslContext.init(null,trustManagers , new SecureRandom());
        SSLSocketFactory sslFactory=new SSLSocketFactory(sslContext,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme scheme=new Scheme("https",443,sslFactory);
        HttpComponentsClientHttpRequestFactory factory=new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }
    }

}
