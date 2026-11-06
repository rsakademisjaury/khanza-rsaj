package bridging;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Bridge kecil, bukan distribusi Chromium. Dependency native dipisah dari library utama SIMRS. */
final class SsrmeEdgeLoader {
    private static URLClassLoader loader;
    static synchronized Class<?> type() throws Exception {
        if(loader==null){
            Path code=Paths.get(SsrmeViewer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path root=(Files.isRegularFile(code)?code.getParent():code).resolve("runtime/ssrme-edge/lib");
            String setting=System.getProperty("rsaj.ssrme.edge","").trim();
            if(!setting.isEmpty())root=Paths.get(setting).resolve("lib");
            else if(!Files.isDirectory(root))root=Paths.get("runtime/ssrme-edge/lib").toAbsolutePath();
            List<URL> urls=new ArrayList<URL>();try(java.util.stream.Stream<Path> paths=Files.list(root)){for(Path p:(Iterable<Path>)paths.filter(x->x.toString().endsWith(".jar"))::iterator)urls.add(p.toUri().toURL());}
            loader=new URLClassLoader(urls.toArray(new URL[0]),SsrmeBrowser.class.getClassLoader()){
                @Override public URL getResource(String name){
                    // Native DLL harus berasal dari versi JAR bridge, bukan JNA lama milik SIMRS.
                    URL own=findResource(name);return own!=null?own:super.getResource(name);
                }
                @Override protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{
                    if(name.startsWith("ca.weblite.")||name.startsWith("com.sun.jna.")||name.startsWith("rsaj.ssrme.edge.")){
                        Class<?> c=findLoadedClass(name);if(c==null)c=findClass(name);if(resolve)resolveClass(c);return c;
                    }return super.loadClass(name,resolve);
                }
            };
        }
        return Class.forName("rsaj.ssrme.edge.EdgeBrowser",true,loader);
    }
}
