package freehand;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class PanelTTD extends JPanel {
    private final List<List<Point>> lines = new ArrayList<>(); // Semua garis
    private List<Point> currentLine = null; // Garis aktif saat ini

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3.0F));
        g2d.setColor(Color.BLACK);

        // Gambar semua garis yang sudah selesai
        for (List<Point> line : lines) {
            for (int i = 0; i < line.size() - 1; i++) {
                Point p1 = line.get(i);
                Point p2 = line.get(i + 1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // Gambar garis yang sedang aktif (jika ada)
        if (currentLine != null) {
            for (int i = 0; i < currentLine.size() - 1; i++) {
                Point p1 = currentLine.get(i);
                Point p2 = currentLine.get(i + 1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    public void startNewLine() {
        currentLine = new ArrayList<>(); // Buat garis baru
    }

    public void addPoint(Point point) {
        if (currentLine != null) {
            currentLine.add(point); // Tambahkan titik ke garis aktif
            repaint(); // Gambar ulang
        }
    }

    public void finishLine() {
        if (currentLine != null) {
            lines.add(currentLine); // Simpan garis ke daftar permanen
            currentLine = null; // Bersihkan garis aktif
            repaint(); // Gambar ulang
        }
    }

    public void clearAll() {
        lines.clear(); // Hapus semua garis
        currentLine = null;
        repaint();
    }
}
