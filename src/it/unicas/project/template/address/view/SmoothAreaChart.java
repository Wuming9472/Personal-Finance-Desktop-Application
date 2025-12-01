package it.unicas.project.template.address.view;

import javafx.collections.ObservableList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
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
    public SmoothAreaChart() {
        this((Axis<X>) new CategoryAxis(), (Axis<Y>) new NumberAxis());
        configureXAxis();
    }

    public SmoothAreaChart(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
        configureXAxis();
        configureYAxis();
    }

    public SmoothAreaChart(Axis<X> xAxis, Axis<Y> yAxis, ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
        configureXAxis();
        configureYAxis();
    }

    private void configureXAxis() {
        if (getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) getXAxis();
            xAxis.setTickLabelRotation(0);
        }
    }

    private void configureYAxis() {
        if (getYAxis() instanceof NumberAxis) {
            NumberAxis yAxis = (NumberAxis) getYAxis();
            yAxis.setForceZeroInRange(true);
            yAxis.setAutoRanging(true);
        }
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
        List<Point2D> points = new ArrayList<>();
        for (PathElement elem : line.getElements()) {
            if (elem instanceof MoveTo) {
                points.add(new Point2D(((MoveTo) elem).getX(), ((MoveTo) elem).getY()));
            } else if (elem instanceof LineTo) {
                points.add(new Point2D(((LineTo) elem).getX(), ((LineTo) elem).getY()));
            }
        }

        if (points.size() < 2) return;

        List<PathElement> smoothElements = new ArrayList<>();
        double yZero = getZeroDisplayPosition();

        smoothElements.add(new MoveTo(points.get(0).x, clampToBaseline(points.get(0).y, yZero)));

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);

            // Gestione speciale per primo e ultimo segmento (evita loop)
            if (i == 0) {
                // Primo segmento: usa una curva più semplice
                Point2D cp1 = new Point2D(
                        p1.x + (p2.x - p1.x) * 0.3,
                        p1.y
                );
                Point2D cp2 = new Point2D(
                        p2.x - (p2.x - p1.x) * 0.3,
                        p2.y
                );

                smoothElements.add(new CubicCurveTo(
                        cp1.x, clampToBaseline(cp1.y, yZero),
                        cp2.x, clampToBaseline(cp2.y, yZero),
                        p2.x, clampToBaseline(p2.y, yZero)
                ));
            } else if (i == points.size() - 2) {
                // Ultimo segmento: usa una curva più semplice
                Point2D cp1 = new Point2D(
                        p1.x + (p2.x - p1.x) * 0.3,
                        p1.y
                );
                Point2D cp2 = new Point2D(
                        p2.x - (p2.x - p1.x) * 0.3,
                        p2.y
                );

                smoothElements.add(new CubicCurveTo(
                        cp1.x, clampToBaseline(cp1.y, yZero),
                        cp2.x, clampToBaseline(cp2.y, yZero),
                        p2.x, clampToBaseline(p2.y, yZero)
                ));
            } else {
                // Segmenti interni: usa Catmull-Rom
                Point2D p0 = points.get(i - 1);
                Point2D p3 = points.get(i + 2);

                Point2D cp1 = getControlPoint(p0, p1, p2, false);
                Point2D cp2 = getControlPoint(p1, p2, p3, true);

                smoothElements.add(new CubicCurveTo(
                        cp1.x, clampToBaseline(cp1.y, yZero),
                        cp2.x, clampToBaseline(cp2.y, yZero),
                        p2.x, clampToBaseline(p2.y, yZero)
                ));
            }
        }

        line.getElements().setAll(smoothElements);

        if (fill != null) {
            List<PathElement> fillElements = new ArrayList<>(smoothElements);
            fillElements.add(new LineTo(points.get(points.size() - 1).x, yZero));
            fillElements.add(new LineTo(points.get(0).x, yZero));
            fillElements.add(new javafx.scene.shape.ClosePath());
            fill.getElements().setAll(fillElements);
        }
    }

    private double getZeroDisplayPosition() {
        double yZero = ((Axis) getYAxis()).getDisplayPosition(0);
        return Double.isNaN(yZero) ? getHeight() : yZero;
    }

    private Point2D getControlPoint(Point2D p0, Point2D p1, Point2D p2, boolean isSecond) {
        // Tensione ridotta per evitare loop e curve eccessive
        double tension = 0.25;
        double d1 = Math.sqrt(Math.pow(p1.x - p0.x, 2) + Math.pow(p1.y - p0.y, 2));
        double d2 = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));

        if (d1 < 0.001 || d2 < 0.001) return new Point2D(p1.x, p1.y);

        double cpX, cpY;
        if (isSecond) {
            cpX = p1.x - (tension * (p2.x - p0.x) * d2) / (d1 + d2);
            cpY = p1.y - (tension * (p2.y - p0.y) * d2) / (d1 + d2);
        } else {
            cpX = p1.x + (tension * (p2.x - p0.x) * d1) / (d1 + d2);
            cpY = p1.y + (tension * (p2.y - p0.y) * d1) / (d1 + d2);
        }

        // Vincola i control point per evitare che tornino indietro
        if (isSecond) {
            cpX = Math.min(cpX, p2.x);
        } else {
            cpX = Math.max(cpX, p1.x);
        }

        return new Point2D(cpX, cpY);
    }

    private double clampToBaseline(double value, double baseline) {
        return Math.min(value, baseline);
    }

    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }
}