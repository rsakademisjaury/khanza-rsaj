package simrskhanza;

import java.awt.Component;

public class Toast {
    public static void show(Component owner, String message) {
        BaseToast.showInfo(owner, message, 3500, null, null);
    }
    public static void show(Component owner, String message, int durationMs) {
        BaseToast.showInfo(owner, message, durationMs, null, null);
    }
}