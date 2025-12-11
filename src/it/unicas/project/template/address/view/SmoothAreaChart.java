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

/**
 * Estensione di {@link AreaChart} che renderizza le linee con curve smussate
 * invece di segmenti rettilinei.
 * <p>
 * Utilizza l'algoritmo Catmull-Rom per calcolare i punti di controllo delle
 * curve di Bézier cubiche, producendo un effetto visivo più gradevole e
 * professionale rispetto al grafico ad area standard.
 
 *
 * <b>Esempio di utilizzo:</b>
 * <pre>{@code
 * SmoothAreaChart<String, Number> chart = new SmoothAreaChart<>();
 * chart.getData().add(series);
 * }</pre>
 *
 * @param <X> il tipo di dati dell'asse X
 * @param <Y> il tipo di dati dell'asse Y
 *
 * @author Personal Finance Team
 * @version 1.0
 * @see AreaChart
 */
public class SmoothAreaChart<X, Y> extends AreaChart<X, Y> {

    /**
     * Costruttore di default per compatibilità con FXML.
     * <p>
     * Crea un grafico con {@link CategoryAxis} per l'asse X e
     * {@link NumberAxis} per l'asse Y.
     
     */
    @SuppressWarnings("unchecked")
    public SmoothAreaChart() {
        this((Axis<X>) new CategoryAxis(), (Axis<Y>) new NumberAxis());
    }

    /**
     * Crea un nuovo SmoothAreaChart con gli assi specificati.
     *
     * @param xAxis l'asse X del grafico
     * @param yAxis l'asse Y del grafico
     */
    public SmoothAreaChart(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
        configureYAxis();
    }

    /**
     * Crea un nuovo SmoothAreaChart con gli assi e i dati specificati.
     *
     * @param xAxis l'asse X del grafico
     * @param yAxis l'asse Y del grafico
     * @param data  la lista osservabile delle serie di dati da visualizzare
     */
    public SmoothAreaChart(Axis<X> xAxis, Axis<Y> yAxis, ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
        configureYAxis();
    }

    /**
     * Configura l'asse X per una migliore visualizzazione.
     * <p>
     * Se l'asse X è di tipo {@link CategoryAxis}, ruota le etichette
     * di -45 gradi per evitare sovrapposizioni.
     
     */
    private void configureXAxis() {
        if (getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) getXAxis();
            xAxis.setTickLabelRotation(-45);
        }
    }

    /**
     * Configura l'asse Y per una migliore visualizzazione.
     * <p>
     * Se l'asse Y è di tipo {@link NumberAxis}, forza l'inclusione
     * dello zero nel range e abilita l'auto-ranging.
     
     */
    private void configureYAxis() {
        if (getYAxis() instanceof NumberAxis) {
            NumberAxis yAxis = (NumberAxis) getYAxis();
            yAxis.setForceZeroInRange(true);
            yAxis.setAutoRanging(true);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Override del metodo per applicare lo smoothing alle linee del grafico
     * dopo il layout standard. Per ogni serie di dati, trova i path della
     * linea e dell'area riempita e applica l'algoritmo di smoothing.
     
     */
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

    /**
     * Applica lo smoothing a un path, convertendo i segmenti lineari
     * in curve di Bézier cubiche.
     * <p>
     * L'algoritmo:
     * <ol>
     *   <li>Estrae i punti dal path originale</li>
     *   <li>Per il primo e l'ultimo segmento usa curve semplificate</li>
     *   <li>Per i segmenti interni usa l'interpolazione Catmull-Rom</li>
     *   <li>Aggiorna sia la linea che l'area riempita</li>
     * </ol>
     
     *
     * @param line il path della linea da smussare
     * @param fill il path dell'area riempita da smussare (può essere null)
     */
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

            if (i == 0) {
                // Primo segmento: curva semplificata per evitare loop
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
                // Ultimo segmento: curva semplificata per evitare loop
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
                // Segmenti interni: interpolazione Catmull-Rom
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

    /**
     * Calcola la posizione Y corrispondente al valore zero sull'asse Y.
     * <p>
     * Utilizzato per determinare la baseline dell'area riempita del grafico.
     
     *
     * @return la posizione in pixel dello zero, o l'altezza del grafico se non disponibile
     */
    private double getZeroDisplayPosition() {
        @SuppressWarnings("unchecked")
        double yZero = ((Axis<Number>) getYAxis()).getDisplayPosition(0);
        return Double.isNaN(yZero) ? getHeight() : yZero;
    }

    /**
     * Calcola un punto di controllo per una curva di Bézier usando
     * l'interpolazione Catmull-Rom.
     * <p>
     * L'algoritmo considera tre punti consecutivi per calcolare la tangente
     * e posizionare il punto di controllo in modo da ottenere una curva
     * fluida e naturale.
     
     *
     * @param p0       il punto precedente
     * @param p1       il punto corrente
     * @param p2       il punto successivo
     * @param isSecond {@code true} per calcolare il secondo punto di controllo,
     *                 {@code false} per il primo
     * @return il punto di controllo calcolato
     */
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

    /**
     * Limita un valore Y alla baseline del grafico.
     * <p>
     * Assicura che i punti della curva non superino la linea di base,
     * evitando artefatti visivi nell'area riempita.
     
     *
     * @param value    il valore Y da limitare
     * @param baseline la posizione della baseline
     * @return il valore limitato
     */
    private double clampToBaseline(double value, double baseline) {
        return Math.min(value, baseline);
    }

    /**
     * Classe interna per rappresentare un punto 2D.
     * <p>
     * Utilizzata internamente per i calcoli geometrici delle curve.
     
     */
    private static class Point2D {
        /** Coordinata X del punto. */
        double x;
        /** Coordinata Y del punto. */
        double y;

        /**
         * Crea un nuovo punto con le coordinate specificate.
         *
         * @param x la coordinata X
         * @param y la coordinata Y
         */
        Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}