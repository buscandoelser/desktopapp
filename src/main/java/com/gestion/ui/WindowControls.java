package com.gestion.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

/**
 * Custom window-control buttons (minimize / maximize-restore / close) and a
 * drag helper for use with {@code StageStyle.UNDECORATED} stages.
 *
 * Typical usage:
 *   HBox controls = WindowControls.createControls(stage, true);
 *   header.getChildren().add(controls);
 *   WindowControls.attachDragHandler(header, stage);
 */
public final class WindowControls {

    private WindowControls() {}

    // ─── SVG icons (10×10 viewBox, filled shapes — no stroke needed) ──────────

    private static final String SVG_MIN =
        "M0,5 H10 V6 H0 Z";

    private static final String SVG_MAX =
        "M0,0 H10 V1 H0 Z" +
        " M0,9 H10 V10 H0 Z" +
        " M0,0 H1 V10 H0 Z" +
        " M9,0 H10 V10 H9 Z";

    private static final String SVG_RESTORE =
        "M0,3 H8 V4 H0 Z" +
        " M0,9 H8 V10 H0 Z" +
        " M0,3 H1 V10 H0 Z" +
        " M7,3 H8 V10 H7 Z" +
        " M2,0 H10 V8 H9 V1 H3 V3 H2 Z";

    private static final String SVG_CLOSE =
        "M0,1.4 L1.4,0 L5,3.6 L8.6,0 L10,1.4 L6.4,5 L10,8.6 L8.6,10 L5,6.4 L1.4,10 L0,8.6 L3.6,5 Z";

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Builds the three control buttons (or two if includeMaximize=false).
     * Returns an HBox styled with .win-controls.
     */
    public static HBox createControls(Stage stage, boolean includeMaximize) {
        HBox box = new HBox();
        box.getStyleClass().add("win-controls");
        box.setAlignment(Pos.CENTER_RIGHT);

        Button btnMin   = makeButton(SVG_MIN,   "win-ctrl");
        Button btnClose = makeButton(SVG_CLOSE, "win-ctrl", "win-ctrl-close");

        btnMin.setOnAction(e   -> stage.setIconified(true));
        btnClose.setOnAction(e -> stage.close());

        if (includeMaximize) {
            Button btnMax = makeButton(SVG_MAX, "win-ctrl");
            btnMax.setOnAction(e -> toggleMaximize(stage));

            // Swap shape on the maximized state
            stage.maximizedProperty().addListener((obs, oldV, newV) -> {
                Region icon = (Region) btnMax.getGraphic();
                String shape = newV ? SVG_RESTORE : SVG_MAX;
                icon.setStyle(String.format("-fx-shape: \"%s\"; -fx-scale-shape: true;", shape));
            });

            box.getChildren().addAll(btnMin, btnMax, btnClose);
        } else {
            box.getChildren().addAll(btnMin, btnClose);
        }

        return box;
    }

    /**
     * Allows the user to drag the window by pressing on the given node.
     * Double-click on direct hits of the node toggles maximize.
     */
    public static void attachDragHandler(Node draggable, Stage stage) {
        final double[] off = new double[2];

        draggable.setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            // Only react if the press landed on the bar itself, not on a child control
            if (!isOnBar(e.getTarget(), draggable)) return;
            off[0] = e.getScreenX() - stage.getX();
            off[1] = e.getScreenY() - stage.getY();
        });

        draggable.setOnMouseDragged(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (!isOnBar(e.getTarget(), draggable)) return;
            if (stage.isMaximized()) return;
            stage.setX(e.getScreenX() - off[0]);
            stage.setY(e.getScreenY() - off[1]);
        });

        draggable.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (e.getClickCount() != 2) return;
            if (!isOnBar(e.getTarget(), draggable)) return;
            if (stage.isResizable()) toggleMaximize(stage);
        });
    }

    // ─── Internals ────────────────────────────────────────────────────────────

    private static Button makeButton(String svg, String... styleClasses) {
        Region icon = new Region();
        icon.getStyleClass().add("win-ctrl-icon");
        icon.setPrefSize(11, 11);
        icon.setMinSize(11, 11);
        icon.setMaxSize(11, 11);
        icon.setStyle(String.format("-fx-shape: \"%s\"; -fx-scale-shape: true;", svg));

        Button btn = new Button();
        btn.setGraphic(icon);
        for (String c : styleClasses) btn.getStyleClass().add(c);
        btn.setFocusTraversable(false);
        return btn;
    }

    /** Manual maximize so UNDECORATED stages restore to a sane default size. */
    private static void toggleMaximize(Stage stage) {
        stage.setMaximized(!stage.isMaximized());
    }

    /** True only when the event target is the bar itself (not a button/child). */
    private static boolean isOnBar(Object target, Node bar) {
        return target == bar;
    }
}
