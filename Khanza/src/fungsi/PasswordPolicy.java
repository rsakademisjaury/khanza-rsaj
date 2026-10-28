package fungsi;

import java.util.Arrays;
import javax.swing.JPasswordField;
import javax.swing.text.PlainDocument;

/** Kebijakan untuk password BARU; tidak menolak login password lama. */
public final class PasswordPolicy {
    public static final int MAX_LENGTH = 100;
    public static final String HINT = "Minimal 8 karakter: huruf besar, huruf kecil, angka, dan simbol.";
    public static final String MASK = "********";
    private PasswordPolicy() { }

    public static String problem(char[] value) {
        if (value == null || Character.codePointCount(value, 0, value.length) < 8) return HINT;
        if (value.length > MAX_LENGTH) return "Password maksimal 100 karakter.";
        boolean upper = false, lower = false, digit = false, symbol = false;
        for (int i = 0; i < value.length; ) {
            int c = Character.codePointAt(value, i);
            i += Character.charCount(c);
            if (Character.isISOControl(c) || Character.getType(c) == Character.FORMAT
                    || Character.getType(c) == Character.SURROGATE) {
                return "Password tidak boleh memuat karakter kontrol atau karakter tersembunyi.";
            }
            upper |= Character.isUpperCase(c);
            lower |= Character.isLowerCase(c);
            digit |= Character.isDigit(c);
            symbol |= !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)
                    && !Character.isSpaceChar(c)
                    && Character.getType(c) != Character.NON_SPACING_MARK
                    && Character.getType(c) != Character.COMBINING_SPACING_MARK;
        }
        return upper && lower && digit && symbol ? null : HINT;
    }

    public static void require(char[] value) {
        String problem = problem(value);
        if (problem != null) throw new IllegalArgumentException(problem);
    }

    public static boolean matches(char[] first, char[] second) { return Arrays.equals(first, second); }

    /** Jangan memakai batasInput.getKata: tanda petik/backslash tidak boleh dihapus. */
    public static void configure(JPasswordField field) {
        field.setDocument(new PlainDocument());
        field.setEchoChar('\u2022');
        field.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        field.setToolTipText(HINT);
    }
}
