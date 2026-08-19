package raven.toast;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.*;
import javax.swing.*;

public class ToastManager {
    private static final List<ToastNotification> activeToasts = new ArrayList<>();

    public static void showToast(Frame owner, String title, String message, Icon icon, Runnable onView, Runnable onSkip) {
        ToastNotification toast = new ToastNotification(owner, title, message, icon, onView, onSkip);
        positionToast(toast);
        activeToasts.add(toast);
    }

    private static void positionToast(ToastNotification toast) {
        int gap = 10;
        int height = 100;
        int index = activeToasts.size();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - toast.getWidth() - 20;
        int y = screenSize.height - ((height + gap) * (index + 1));
        toast.setLocation(x, y);
        toast.setVisible(true);
    }

    public static void removeToast(ToastNotification toast) {
        activeToasts.remove(toast);
        repositionToasts();
    }

    private static void repositionToasts() {
        for (int i = 0; i < activeToasts.size(); i++) {
            ToastNotification toast = activeToasts.get(i);
            int gap = 10;
            int height = 100;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width - toast.getWidth() - 20;
            int y = screenSize.height - ((height + gap) * (i + 1));
            toast.setLocation(x, y);
        }
    }
}