package bridging;

import java.awt.Component;

/** Kontrak tampilan browser agar launcher SSRME tidak bergantung pada engine tertentu. */
public interface SsrmeBrowser {
    Component component();
    void reload();
    void close();
}
