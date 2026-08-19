package simrskhanza;

import java.awt.Component;

public class ToastJawaban {
    public static void show(Component owner, String message) {
        BaseToast.showSuccess(owner, message, 3500, null, null);
    }
    public static void show(Component owner, String message, int durationMs) {
        BaseToast.showSuccess(owner, message, durationMs, null, null);
    }
}