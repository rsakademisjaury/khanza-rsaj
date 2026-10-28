package bridging;

import fungsi.koneksiDB;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import javax.swing.SwingUtilities;

/** Report/konversi lama dipakai ulang; respons upload diverifikasi dengan hash file. */
final class SuratLepasRawatFileGateway implements SuratLepasRawatBerkas.Gateway {
    @Override public List<File> render(String surat,long job) throws Exception {
        final String prefix="SLR_"+SuratLepasRawatBerkas.hash(surat)+"_"+job;
        FutureTask<List<File>> task=new FutureTask<>(()->{
            BPJSSuratKontrol form=new BPJSSuratKontrol(null,false);
            try { return form.siapkanFileLepasRawat(surat,prefix); }
            finally { form.dispose(); }
        });
        if(SwingUtilities.isEventDispatchThread()) task.run();else SwingUtilities.invokeAndWait(task);
        try { return task.get(); }
        catch(java.util.concurrent.ExecutionException e) {
            Throwable cause=e.getCause();if(cause instanceof Exception) throw (Exception)cause;throw e;
        }
    }
    private String base() {
        return "http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/";
    }
    @Override public String upload(File file) throws Exception {
        if(!file.isFile()||file.length()==0||file.length()>64L*1024*1024||!file.getName().matches("SLR_[A-Za-z0-9_]+\\.jpg"))
            throw new Exception("File pengganti belum lengkap atau tidak valid.");
        String boundary="SLR"+UUID.randomUUID().toString().replace("-","");
        byte[] header=("--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\""+file.getName()
                +"\"\r\nContent-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] tail=("\r\n--"+boundary+"--\r\n").getBytes(StandardCharsets.US_ASCII);
        byte[] expected=Files.readAllBytes(file.toPath());
        HttpURLConnection post=(HttpURLConnection)new URL(base()+"upload.php?doc=berkasrawat/pages/upload/").openConnection();
        post.setConnectTimeout(10000);post.setReadTimeout(20000);post.setInstanceFollowRedirects(false);
        post.setRequestMethod("POST");post.setDoOutput(true);
        post.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);
        post.setFixedLengthStreamingMode((long)header.length+expected.length+tail.length);
        try {
            try(OutputStream out=post.getOutputStream()) { out.write(header);out.write(expected);out.write(tail); }
            int code=post.getResponseCode();
            if(code<200||code>=300) throw new Exception("Server berkas menolak upload (HTTP "+code+").");
            try(InputStream in=post.getInputStream()) { byte[] buffer=new byte[4096];while(in.read(buffer)!=-1) { } }
        } finally { post.disconnect(); }
        HttpURLConnection get=(HttpURLConnection)new URL(base()+"berkasrawat/pages/upload/"+file.getName()).openConnection();
        get.setConnectTimeout(10000);get.setReadTimeout(20000);get.setInstanceFollowRedirects(false);
        get.setRequestProperty("Cache-Control","no-cache");
        try {
            if(get.getResponseCode()!=200) throw new Exception("File baru belum dapat diverifikasi dari server berkas.");
            MessageDigest digest=MessageDigest.getInstance("SHA-256");long bytes=0;
            try(InputStream in=get.getInputStream()) {
                byte[] buffer=new byte[8192];int count;
                while((count=in.read(buffer))!=-1) {
                    bytes+=count;if(bytes>expected.length) throw new Exception("Isi file pada server berbeda dari cetakan baru.");
                    digest.update(buffer,0,count);
                }
            }
            if(bytes!=expected.length||!MessageDigest.isEqual(digest.digest(),MessageDigest.getInstance("SHA-256").digest(expected)))
                throw new Exception("Isi file pada server berbeda dari cetakan baru. Berkas lama tetap dipertahankan.");
        } finally { get.disconnect(); }
        return "pages/upload/"+file.getName();
    }
    @Override public void bersihkan(List<File> files) {
        for(File f:files) {
            if(!f.getName().matches("SLR_[A-Za-z0-9_]+\\.jpg")) continue;
            try { Files.deleteIfExists(f.toPath()); } catch(Exception e) { System.out.println("Berkas sementara belum terhapus: "+f.getName()); }
            String prefix=f.getName().replaceFirst("(?:_page_[0-9]+)?\\.jpg$","");
            try { Files.deleteIfExists(new File("tmpPDF",prefix+".pdf").toPath()); } catch(Exception e) { /* Hanya cache lokal. */ }
        }
    }
}
