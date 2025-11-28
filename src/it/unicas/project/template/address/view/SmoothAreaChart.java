package it.unicas.project.template.address.view;

import javafx.collections.ObservableList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis; // Importante
import javafx.scene.chart.NumberAxis;   // Importante
import javafx.scene.chart.XYChart;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.CubicCurveTo;

import java.util.ArrayList;
import java.util.List;

public class SmoothAreaChart<X, Y> extends AreaChart<X, Y> {

    // --- COSTRUTTORE AGGIUNTO PER FXML ---
    // Risolve l'errore "NoSuchMethodException: <init>()"
    public SmoothAreaChart() {
        // Creiamo degli assi di default per permettere l'inizializzazione da FXML
        // Il casting (Axis<X>) è necessario per soddisfare i generici
        super((Axis<X>) new CategoryAxis(), (Axis<Y>) new NumberAxis());
    }
    // -------------------------------------

    public SmoothAreaChart(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    public SmoothAreaChart(Axis<X> xAxis, Axis<Y> yAxis, ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (Series<X, Y> series : getData()) {
            Path seriesLine = (Path) series.getNode().lookup(".chart-series-area-line");
            Path seriesFill = (Path) series.getNode().lookup(".chart-series-area-fill");

            if (seriesLine != null && !series.getData().isEmpty()) {
                smoothPath(seriesLine, seriesFill);
            }
        }
    }

    private void smoothPath(Path line, Path fill) {
        // Estrai i punti dal Path originale (che sono linee rette)
        List<Point2D> points = new ArrayList<>();
        for (PathElement elem : line.getElements()) {
            if (elem instanceof MoveTo) {
                points.add(new Point2D(((MoveTo) elem).getX(), ((MoveTo) elem).getY()));
            } else if (elem instanceof LineTo) {
                points.add(new Point2D(((LineTo) elem).getX(), ((LineTo) elem).getY()));
            }
        }

        if (points.size() < 2) return;

        // Genera i segmenti curvi
        List<PathElement> smoothElements = new ArrayList<>();
        smoothElements.add(new MoveTo(points.get(0).x, points.get(0).y));

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p0 = (i > 0) ? points.get(i - 1) : points.get(i);
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);
            Point2D p3 = (i < points.size() - 2) ? points.get(i + 2) : points.get(i + 1);

            Point2D cp1 = getControlPoint(p0, p1, p2, false);
            Point2D cp2 = getControlPoint(p1, p2, p3, true);

            smoothElements.add(new CubicCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y));
        }

        // Applica al percorso della linea
        line.getElements().setAll(smoothElements);

        // Applica al percorso di riempimento (Area)
        if (fill != null) {
            List<PathElement> fillElements = new ArrayList<>(smoothElements);
            // Chiudi il percorso di riempimento verso l'asse X
            double yZero = ((Axis) getYAxis()).getDisplayPosition(0);
            if (Double.isNaN(yZero)) yZero = getHeight(); // Fallback se l'asse non è pronto

            fillElements.add(new LineTo(points.get(points.size() - 1).x, yZero));
            fillElements.add(new LineTo(points.get(0).x, yZero));
            fillElements.add(new javafx.scene.shape.ClosePath());

            fill.getElements().setAll(fillElements);
        }
    }

    // Calcola i punti di controllo per la curva di Catmull-Rom spline convertita in Bezier
    private Point2D getControlPoint(Point2D p0, Point2D p1, Point2D p2, boolean isSecond) {
        double tension = 0.3; // Aggiusta questo valore per curve più o meno strette (0.0 = linee rette, 1.0 = molto curve)
        double d1 = Math.sqrt(Math.pow(p1.x - p0.x, 2) + Math.pow(p1.y - p0.y, 2));
        double d2 = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));

        if (d1 == 0 || d2 == 0) return new Point2D(p1.x, p1.y);

        if (isSecond) {
            return new Point2D(
                    p1.x - (tension * (p2.x - p0.x) * d2) / (d1 + d2),
                    p1.y - (tension * (p2.y - p0.y) * d2) / (d1 + d2)
            );
        } else {
            return new Point2D(
                    p1.x + (tension * (p2.x - p0.x) * d1) / (d1 + d2),
                    p1.y + (tension * (p2.y - p0.y) * d1) / (d1 + d2)
            );
        }
    }

    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }
}