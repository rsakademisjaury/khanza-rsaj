package bridging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

/**
 * Generate PDF klaim gabungan dengan urutan:
 * SEP -> Resume -> Billing -> Individual -> (Lab) -> (Radiologi teks+kesan) -> (Resep/Obat)
 * Disimpan ke baseDir/yyyy/MM/dd/<no_sep>.pdf
 * Jika nama bentrok → <no_sep>_v2.pdf, dst.
 */
public class KlaimPacketService {

    private final Path baseDir;
    private final Connection conn;
    private final ZoneId zoneId;

    public KlaimPacketService(Path baseDir, Connection conn, ZoneId zoneId) {
        this.baseDir = Objects.requireNonNull(baseDir, "baseDir null");
        this.conn    = Objects.requireNonNull(conn, "conn null");
        this.zoneId  = zoneId != null ? zoneId : ZoneId.systemDefault();
    }

    public static class Item {
        public final String code;       // "SEP", "RESUME", dll
        public final String jasperPath; // path .jasper (resource/file)
        public final Map<String,Object> params;
        public Item(String code, String jasperPath, Map<String,Object> params) {
            this.code = code; this.jasperPath = jasperPath; this.params = params;
        }
    }

    public Path generatePacket(String noRawat,
                               String noSep,
                               Date tglKunjungan,
                               boolean includeLab,
                               boolean includeRad,
                               boolean includeResep) throws Exception {

        if (noSep == null || noSep.trim().isEmpty()) {
            throw new IllegalArgumentException("no_sep wajib ada (untuk nama file).");
        }
        if (tglKunjungan == null) {
            throw new IllegalArgumentException("tglKunjungan tidak boleh null.");
        }

        List<Item> manifest = buildDefaultManifest(noRawat, noSep, includeLab, includeRad, includeResep);

        Path tempDir = Files.createTempDirectory("klaim_" + sanitize(noSep) + "_");
        List<Path> parts = new ArrayList<>();
        int idx = 1;

        for (Item it : manifest) {
            try (InputStream jasper = openReport(it.jasperPath)) {
                if (jasper == null) {
                    System.err.println("[KLAIM] Template tidak ditemukan: " + it.jasperPath + " (skip)");
                    continue;
                }
                JasperPrint jp = JasperFillManager.fillReport(jasper, it.params, conn);
                if (jp == null || jp.getPages() == null || jp.getPages().isEmpty()) {
                    System.out.println("[KLAIM] Report kosong: " + it.code + " (skip)");
                    continue;
                }
                String outName = String.format("%02d_%s.pdf", idx++, it.code.toLowerCase());
                Path outFile = tempDir.resolve(outName);
                JasperExportManager.exportReportToPdfFile(jp, outFile.toString());
                parts.add(outFile);
            } catch (Exception ex) {
                System.err.println("[KLAIM] Gagal render " + it.code + " : " + ex.getMessage());
            }
        }

        if (parts.isEmpty()) {
            cleanupQuietly(tempDir);
            throw new IllegalStateException("Semua report kosong/gagal. Paket klaim tidak dibuat.");
        }

        LocalDate d = tglKunjungan.toInstant().atZone(zoneId).toLocalDate();
        Path destDir = baseDir
                .resolve(String.format("%04d", d.getYear()))
                .resolve(String.format("%02d", d.getMonthValue()))
                .resolve(String.format("%02d", d.getDayOfMonth()));
        Files.createDirectories(destDir); // idempotent; tidak bikin kalau sudah ada

        Path finalPath = resolveUnique(destDir, sanitize(noSep) + ".pdf");
        mergePdfs(parts, finalPath);
        cleanupQuietly(tempDir);

        return finalPath;
    }

    private List<Item> buildDefaultManifest(String noRawat, String noSep,
                                            boolean includeLab, boolean includeRad, boolean includeResep) {
        List<Item> list = new ArrayList<>();

        list.add(new Item("SEP",
                "/rpt/rptSEP.jasper",                    // GANTI sesuai path-mu
                mapOf("no_sep", noSep, "norawat", noRawat)));

        list.add(new Item("RESUME",
                "/rpt/rptResumeMedis.jasper",            // GANTI
                mapOf("no_rawat", noRawat)));

        list.add(new Item("BILLING",
                "/rpt/rptBillingKlaim.jasper",           // GANTI
                mapOf("no_rawat", noRawat)));

        list.add(new Item("INDIVIDUAL",
                "/rpt/rptEklaimIndividual.jasper",       // GANTI
                mapOf("no_rawat", noRawat, "no_sep", noSep)));

        if (includeLab) {
            list.add(new Item("LAB",
                    "/rpt/rptHasilLabKlaim.jasper",      // GANTI
                    mapOf("no_rawat", noRawat)));
        }

        if (includeRad) {
            list.add(new Item("RAD",
                    "/rpt/rptHasilRadiologiText.jasper", // GANTI (teks+kesan saja)
                    mapOf("no_rawat", noRawat)));
        }

        if (includeResep) {
            list.add(new Item("RESEP",
                    "/rpt/rptResepObatKlaim.jasper",      // GANTI
                    mapOf("no_rawat", noRawat)));
        }

        return list;
    }

    private InputStream openReport(String jasperPath) throws IOException {
        InputStream in = KlaimPacketService.class.getResourceAsStream(jasperPath);
        if (in != null) return in;
        Path p = Paths.get(jasperPath);
        return Files.exists(p) ? Files.newInputStream(p) : null;
    }

    private void mergePdfs(List<Path> parts, Path dest) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(dest.toString());
        for (Path p : parts) merger.addSource(p.toFile());
        merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
    }

    private Path resolveUnique(Path dir, String fileName) {
        Path candidate = dir.resolve(fileName);
        if (!Files.exists(candidate)) return candidate;
        String base = fileName, ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) { base = fileName.substring(0, dot); ext = fileName.substring(dot); }
        int v = 2;
        while (true) {
            Path c = dir.resolve(base + "_v" + v + ext);
            if (!Files.exists(c)) return c;
            v++;
        }
    }

    private void cleanupQuietly(Path dir) {
        if (dir == null) return;
        try (var s = Files.walk(dir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private static Map<String,Object> mapOf(Object... kv) {
        Map<String,Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i+1]);
        return m;
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
