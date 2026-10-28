package fungsi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Identitas OS/komputer, dibagi semua aplikasi dan tidak disimpan di folder JAR. */
public final class LoginDeviceIdentity {
    @FunctionalInterface
    public interface Provider { String id() throws SQLException; }
    private static String cached;
    private LoginDeviceIdentity() { }

    public static synchronized String getId() throws SQLException {
        if (cached != null) return cached;
        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            Path common;
            if (os.contains("win")) {
                String directory = System.getenv("PROGRAMDATA");
                common = Path.of(directory == null || directory.isEmpty() ? "C:\\ProgramData" : directory,
                        "RSAJ", "SIMRS", "device-id");
            } else if (os.contains("mac")) {
                common = Path.of("/Library/Application Support/RSAJ/SIMRS/device-id");
            } else common = Path.of("/var/tmp/rsaj-simrs/device-id");
            String machine = "";
            if (!Files.isRegularFile(common)) {
                try { machine = machineId(os); }
                catch (IOException unavailable) { machine = ""; }
            }
            cached = resolve(machine, common);
            return cached;
        } catch (Exception exception) {
            throw new SQLException("Identitas komputer SIMRS belum tersedia. Hubungi IT.", exception);
        }
    }

    private static String machineId(String os) throws IOException, InterruptedException {
        if (os.contains("win")) {
            Process process = new ProcessBuilder("reg", "query",
                    "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid", "/reg:64")
                    .redirectErrorStream(true).start();
            if (!process.waitFor(3, TimeUnit.SECONDS)) { process.destroyForcibly(); return ""; }
            String result = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Matcher guid = Pattern.compile("(?i)MachineGuid\\s+REG_SZ\\s+([0-9a-f-]{36})").matcher(result);
            return process.exitValue() == 0 && guid.find() ? "windows:" + guid.group(1).toLowerCase(Locale.ROOT) : "";
        }
        if (!os.contains("mac")) {
            for (String name : new String[]{"/etc/machine-id", "/var/lib/dbus/machine-id"}) {
                Path file = Path.of(name);
                if (Files.isReadable(file)) {
                    String value = Files.readString(file).trim().toLowerCase(Locale.ROOT);
                    if (value.matches("[0-9a-f]{32}") && !value.matches("0{32}")) return "linux:" + value;
                }
            }
        }
        return "";
    }

    // File kanonis dibagi per komputer. Lock antar proses mencegah tiga aplikasi
    // yang pertama kali berjalan memperoleh ID berbeda.
    static synchronized String resolve(String machine, Path common) throws Exception {
        if (Files.isRegularFile(common)) {
            if (Files.size(common) > 1024) throw new IOException("Identitas komputer tidak valid.");
            String existing = Files.readString(common).trim();
            if (existing.matches("[0-9a-f]{64}")) return existing;
        }
        Files.createDirectories(common.getParent());
        try (FileChannel channel = FileChannel.open(common, StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);
             FileLock lock = channel.lock()) {
            if (channel.size() > 1024) throw new IOException("Identitas komputer tidak valid.");
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            while (buffer.hasRemaining() && channel.read(buffer) > 0) { }
            String value = new String(buffer.array(), StandardCharsets.US_ASCII).trim();
            if (value.isEmpty()) {
                if (machine != null && !machine.isEmpty()) value = hash(machine);
                else {
                    byte[] random = new byte[32]; new SecureRandom().nextBytes(random);
                    value = hex(random);
                }
                channel.position(0);
                ByteBuffer output = ByteBuffer.wrap(value.getBytes(StandardCharsets.US_ASCII));
                while (output.hasRemaining()) channel.write(output);
                channel.force(true);
            }
            if (!value.matches("[0-9a-f]{64}")) throw new IOException("Identitas komputer tidak valid.");
            return value;
        }
    }

    private static String hash(String value) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    private static String hex(byte[] bytes) {
        StringBuilder value = new StringBuilder(bytes.length * 2);
        for (byte part : bytes) value.append(String.format(Locale.ROOT, "%02x", part & 0xff));
        return value.toString();
    }
}
