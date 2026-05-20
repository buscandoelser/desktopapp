package com.gestion.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * Donut-style capacity ring with animated sweep + animated percentage label.
 *
 * Usage:
 *   CapacityRing ring = new CapacityRing(180, 16);
 *   ring.setSubText("ocupación");
 *   ring.setProgress(0.6, true);   // 60% with smooth animation
 */
public class CapacityRing extends StackPane {

    private final Arc   bgArc;
    private final Arc   fgArc;
    private final Label lblPercent;
    private final Label lblSub;

    private final DoubleProperty animatedPct = new SimpleDoubleProperty(0);

    /**
     * @param size      total widget size (square), e.g. 180
     * @param thickness ring stroke width, e.g. 16
     */
    public CapacityRing(double size, double thickness) {
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);
        getStyleClass().add("capacity-ring");

        double radius = (size - thickness) / 2.0;
        double cx     = size / 2.0;
        double cy     = size / 2.0;

        // ── Background ring ──────────────────────────────────────────────
        bgArc = new Arc(cx, cy, radius, radius, 0, 360);
        bgArc.setType(ArcType.OPEN);
        bgArc.setFill(null);
        bgArc.setStroke(Color.web("#EFE5C9"));
        bgArc.setStrokeWidth(thickness);
        bgArc.setStrokeLineCap(StrokeLineCap.ROUND);

        // ── Foreground (progress) ring — starts at 90° (top), sweeps clockwise ──
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#F4B23A")),
            new Stop(1.0, Color.web("#C98A1B"))
        );
        fgArc = new Arc(cx, cy, radius, radius, 90, 0);
        fgArc.setType(ArcType.OPEN);
        fgArc.setFill(null);
        fgArc.setStroke(gradient);
        fgArc.setStrokeWidth(thickness);
        fgArc.setStrokeLineCap(StrokeLineCap.ROUND);

        Pane arcLayer = new Pane(bgArc, fgArc);
        arcLayer.setPrefSize(size, size);
        arcLayer.setMaxSize(size, size);
        arcLayer.setMinSize(size, size);
        arcLayer.setMouseTransparent(true);

        // ── Center labels ────────────────────────────────────────────────
        lblPercent = new Label("0%");
        lblPercent.getStyleClass().add("capacity-ring-percent");

        lblSub = new Label("");
        lblSub.getStyleClass().add("capacity-ring-sub");

        VBox center = new VBox(2, lblPercent, lblSub);
        center.setAlignment(Pos.CENTER);
        center.setMouseTransparent(true);

        // Bind percentage label to the animated property
        animatedPct.addListener((obs, o, n) ->
            lblPercent.setText(Math.round(n.doubleValue() * 100) + "%")
        );

        getChildren().addAll(arcLayer, center);
    }

    /** Sets the progress (0.0 – 1.0). When animate=true, sweep and label tween smoothly. */
    public void setProgress(double pct, boolean animate) {
        pct = Math.max(0, Math.min(1, pct));
        double targetLength = -pct * 360.0;

        if (animate) {
            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(fgArc.lengthProperty(), fgArc.getLength(), Interpolator.EASE_OUT),
                    new KeyValue(animatedPct, animatedPct.get(), Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(950),
                    new KeyValue(fgArc.lengthProperty(), targetLength, Interpolator.EASE_OUT),
                    new KeyValue(animatedPct, pct, Interpolator.EASE_OUT))
            );
            tl.play();
        } else {
            fgArc.setLength(targetLength);
            animatedPct.set(pct);
        }
    }

    /** Small subtitle shown under the % inside the ring (e.g. "ocupación"). */
    public void setSubText(String text) { lblSub.setText(text); }

    /** Override the inactive-track colour. */
    public void setTrackColor(Color color)    { bgArc.setStroke(color); }

    /** Override the progress stroke (use any Paint — solid colour or gradient). */
    public void setProgressPaint(javafx.scene.paint.Paint paint) { fgArc.setStroke(paint); }
}
