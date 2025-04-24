package widget;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Kelas untuk membuat bentuk polygon dengan sudut membulat.
 */
public class PolygonCorner {

    /**
     * Menghasilkan GeneralPath dengan sudut membulat berdasarkan daftar titik.
     * 
     * @param points  Daftar titik-titik untuk membentuk polygon.
     * @param arcSize Ukuran radius sudut membulat.
     * @return GeneralPath yang merepresentasikan polygon dengan sudut membulat.
     */
    public GeneralPath getRoundedGeneralPathFromPoints(List<Point2D> points, float arcSize) {
        // Menambahkan titik awal di akhir daftar untuk menutup polygon
        points.add(points.get(0));
        points.add(points.get(1));

        GeneralPath path = new GeneralPath();
        Point2D startPoint = calculatePoint(points.get(points.size() - 1), points.get(points.size() - 2), arcSize);
        path.moveTo(startPoint.getX(), startPoint.getY());

        // Menggambar setiap sisi polygon dengan sudut membulat
        for (int i = 1; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i - 1);
            Point2D p2 = points.get(i);
            Point2D p3 = points.get(i + 1);

            Point2D mPoint = calculatePoint(p1, p2, arcSize);
            path.lineTo(mPoint.getX(), mPoint.getY());

            mPoint = calculatePoint(p3, p2, arcSize);
            path.curveTo(p2.getX(), p2.getY(), p2.getX(), p2.getY(), mPoint.getX(), mPoint.getY());
        }
        return path;
    }

    /**
     * Menghitung titik pada garis yang menghubungkan dua titik dengan ukuran sudut membulat.
     * 
     * @param p1      Titik pertama.
     * @param p2      Titik kedua.
     * @param arcSize Ukuran radius sudut membulat.
     * @return Titik yang dihitung.
     */
    private Point2D calculatePoint(Point2D p1, Point2D p2, float arcSize) {
        double distance = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
        double proportion = arcSize / distance;
        double deltaX = (p1.getX() - p2.getX()) * proportion;
        double deltaY = (p1.getY() - p2.getY()) * proportion;
        double calculatedX = p2.getX() + deltaX;
        double calculatedY = p2.getY() + deltaY;
        return new Point2D.Double(calculatedX, calculatedY);
    }
}
