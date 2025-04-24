package widget;

import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import usu.widget.glass.ViewPortGlass;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author usu
 */
public class ScrollPane extends JScrollPane {

    public ScrollPane() {
        super();
        setViewport(new ViewPortGlass());
        setOpaque(false);
        setBorder(new LineBorder(new Color(239, 244, 234)));
        setBackground(new Color(255, 255, 255));
        applySystemLookAndFeel();
    }

    /**
     * Method untuk mengatur tampilan sesuai dengan sistem operasi.
     */
    private void applySystemLookAndFeel() {
        try {
            // Mendapatkan Look and Feel default dari sistem operasi
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // Memperbarui UI dari komponen ScrollPane
        UIManager.getLookAndFeelDefaults().put("ScrollBar.thumb", getBackground());
        UIManager.getLookAndFeelDefaults().put("ScrollBar.thumbHighlight", getBackground());
        UIManager.getLookAndFeelDefaults().put("ScrollBar.thumbDarkShadow", getBackground());
        UIManager.getLookAndFeelDefaults().put("ScrollBar.thumbShadow", getBackground());
        UIManager.getLookAndFeelDefaults().put("ScrollBar.track", getBackground());
        UIManager.getLookAndFeelDefaults().put("ScrollBar.trackHighlight", getBackground());
    }
}
