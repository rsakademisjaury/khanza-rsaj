package widget;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;

public class ToastStyle {

    public static void apply() {
        // 🔥 Warna background merah solid (tanpa gradasi)
        UIManager.put("Toast.error.background", new Color(220, 53, 69));
        UIManager.put("Toast.error.effectColor", new Color(220, 53, 69));

        // 🟢 Warna success lebih fresh
        UIManager.put("Toast.success.background", new Color(40, 167, 69));
        UIManager.put("Toast.success.effectColor", new Color(40, 167, 69));

        // 🔵 Warna info
        UIManager.put("Toast.info.background", new Color(0, 123, 255));
        UIManager.put("Toast.info.effectColor", new Color(0, 123, 255));

        // 🟠 Warna warning
        UIManager.put("Toast.warning.background", new Color(255, 193, 7));
        UIManager.put("Toast.warning.effectColor", new Color(255, 193, 7));

        // ❌ Icon tanda silang custom
        UIManager.put("Toast.closeIcon", new ImageIcon("src/icons/close-red.png")); // ganti path sesuai file kamu
        UIManager.put("Toast.closeIconColor", Color.RED);

        // 🛎️ Icon notifikasi jenis-jenis
        UIManager.put("Toast.error.icon", new ImageIcon("src/icons/error.png"));
        UIManager.put("Toast.success.icon", new ImageIcon("src/icons/success.png"));
        UIManager.put("Toast.info.icon", new ImageIcon("src/icons/info.png"));
        UIManager.put("Toast.warning.icon", new ImageIcon("src/icons/warning.png"));

        // 📏 Ukuran dan bentuk
        UIManager.put("Toast.minimumWidth", UIScale.scale(200));
        UIManager.put("Toast.maximumWidth", UIScale.scale(400));
        UIManager.put("Toast.margin", new Insets(14, 16, 14, 16));
        UIManager.put("Toast.arc", 24); // radius sudut
        UIManager.put("Toast.showCloseButton", true);

        // 🔡 Font lebih besar
        UIManager.put("Label.font", new Font("Segoe UI", Font.BOLD, 16));

        // 🚫 Matikan gradasi kalau mau flat
        UIManager.put("Toast.useEffect", false);
    }
}
