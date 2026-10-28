package fungsi;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

/** Gambar CAPTCHA lokal tanpa layanan internet atau dependensi tambahan. */
public final class LoginCaptcha {
    public static final int WIDTH = 196;
    public static final int HEIGHT = 48;
    public static final int LENGTH = 5;
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private LoginCaptcha() { }

    public static final class Challenge {
        private final String id;
        private final BufferedImage image;
        // Hanya hash yang dikirim ke penyimpanan. Jawaban tidak masuk DTO UI.
        private final String answerHash;

        private Challenge(String id, BufferedImage image, String answerHash) {
            this.id = id;
            this.image = image;
            this.answerHash = answerHash;
        }
        public String getId() { return id; }
        public BufferedImage getImage() { return image; }
        String answerHash() { return answerHash; }
    }

    static Challenge generate() {
        byte[] token = new byte[32];
        RANDOM.nextBytes(token);
        StringBuilder id = new StringBuilder(64);
        for (byte part : token) id.append(String.format(Locale.ROOT, "%02x", part & 0xff));
        StringBuilder answer = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) answer.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return new Challenge(id.toString(), render(answer.toString()), hashAnswer(id.toString(), answer.toString()));
    }

    static String hashAnswer(String id, String answer) {
        try {
            String normalized = answer == null ? "" : answer.trim().toUpperCase(Locale.ROOT);
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((id + ":" + normalized).getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(64);
            for (byte part : digest) hash.append(String.format(Locale.ROOT, "%02x", part & 0xff));
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 tidak tersedia.", exception);
        }
    }

    private static BufferedImage render(String answer) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(246, 250, 253));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            for (int i = 0; i < 65; i++) {
                g.setColor(new Color(177 + RANDOM.nextInt(45), 198 + RANDOM.nextInt(35), 220 + RANDOM.nextInt(25)));
                g.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            for (int i = 0; i < LENGTH; i++) {
                AffineTransform original = g.getTransform();
                int x = 17 + i * 33;
                int y = 33 + RANDOM.nextInt(5) - 2;
                g.rotate(Math.toRadians(RANDOM.nextInt(17) - 8), x + 10, y - 12);
                g.setColor(new Color(20 + RANDOM.nextInt(30), 61 + RANDOM.nextInt(35), 95 + RANDOM.nextInt(40)));
                g.drawString(answer.substring(i, i + 1), x, y);
                g.setTransform(original);
            }
            g.setColor(new Color(122, 159, 184, 115));
            g.drawArc(-5, 17, WIDTH + 15, 23, 0, 180);
        } finally { g.dispose(); }
        return image;
    }
}
