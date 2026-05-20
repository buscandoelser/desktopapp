package com.gestion.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A mobile-style bottom navigation dock for JavaFX.
 *
 * Usage:
 *   List<InteractiveDock.DockItem> items = List.of(
 *       new InteractiveDock.DockItem("dashboard", "Panel",    SVG_HOME),
 *       new InteractiveDock.DockItem("internos",  "Internos", SVG_PEOPLE)
 *   );
 *   InteractiveDock dock = new InteractiveDock(items);
 *   dock.setOnSelectionChanged(id -> loadModule(id));
 *   dock.selectItem(0);
 */
public class InteractiveDock extends BorderPane {

    // ─── Model ────────────────────────────────────────────────────────────────

    public static class DockItem {
        public final String id;
        public final String label;
        public final String svgContent; // SVG path data for -fx-shape

        public DockItem(String id, String label, String svgContent) {
            this.id = id;
            this.label = label;
            this.svgContent = svgContent;
        }
    }

    // ─── SVG icon constants (Material Design, 24×24 viewBox) ─────────────────

    public static final String SVG_HOME =
        "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";

    public static final String SVG_PEOPLE =
        "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3z" +
        "m-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3z" +
        "m0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5C15 13.17 10.33 13 8 13z" +
        "m8 0c-.29 0-.62.02-.97.05C16.19 13.9 17 15.03 17 16.5V19h6v-2.5C23 14.17 18.33 13 16 13z";

    public static final String SVG_MONEY =
        "M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21" +
        "c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13" +
        "c2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2" +
        "c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z";

    public static final String SVG_CARD =
        "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2z" +
        "m0 14H4v-6h16v6zm0-10H4V6h16v2z";

    public static final String SVG_CHART =
        "M5 9.2h3V19H5V9.2zM10.6 5h2.8v14h-2.8V5zm5.6 8H19v6h-2.8v-6z";

    public static final String SVG_GEAR =
        "M19.43 12.98c.04-.32.07-.64.07-.98s-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64" +
        "l-2-3.46c-.12-.22-.39-.3-.61-.22l-2.49 1c-.52-.4-1.08-.73-1.69-.98l-.38-2.65" +
        "C14.46 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98" +
        "l-2.49-1c-.23-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65" +
        "c-.04.32-.07.65-.07.98s.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64" +
        "l2 3.46c.12.22.39.3.61.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65" +
        "c.03.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.59 1.69-.98" +
        "l2.49 1c.23.09.49 0 .61-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65z" +
        "M12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z";

    // ─── Internal state ───────────────────────────────────────────────────────

    private final List<DockItem> items;
    private final List<VBox>   cells      = new ArrayList<>();
    private final List<Region> iconNodes  = new ArrayList<>();
    private final List<Label>  labelNodes = new ArrayList<>();

    private final HBox      itemRow   = new HBox();
    private final Pane      overlay   = new Pane();
    private final Rectangle indicator = new Rectangle();

    private int              activeIndex          = -1;
    private Consumer<String> onSelectionChanged;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public InteractiveDock(List<DockItem> items) {
        this.items = List.copyOf(items);
        getStyleClass().add("dock-root");

        // Stylesheets aplicados a nivel de Scene vía ThemeManager — no cargamos acá

        buildIndicator();
        buildItems();

        StackPane stack = new StackPane(itemRow, overlay);
        stack.getStyleClass().add("dock-stack");
        setCenter(stack);

        widthProperty().addListener((obs, o, w) -> {
            if (w.doubleValue() > 0 && activeIndex >= 0) placeIndicator(activeIndex, false);
        });
        overlay.heightProperty().addListener((obs, o, h) -> {
            if (h.doubleValue() > 0 && activeIndex >= 0) placeIndicator(activeIndex, false);
        });
    }

    // ─── Build: indicator ─────────────────────────────────────────────────────

    private void buildIndicator() {
        indicator.setHeight(3);
        indicator.setArcWidth(3);
        indicator.setArcHeight(3);
        indicator.getStyleClass().add("dock-indicator");
        indicator.setMouseTransparent(true);
        overlay.setMouseTransparent(true);
        overlay.getChildren().add(indicator);
    }

    // ─── Build: items ─────────────────────────────────────────────────────────

    private void buildItems() {
        itemRow.setAlignment(Pos.CENTER);
        itemRow.getStyleClass().add("dock-item-row");

        for (int i = 0; i < items.size(); i++) {
            DockItem item = items.get(i);
            final int idx = i;

            // Icon — Region uses -fx-shape so CSS fully controls colour
            Region icon = new Region();
            icon.getStyleClass().add("dock-icon");
            icon.setPrefSize(22, 22);
            icon.setMinSize(22, 22);
            icon.setMaxSize(22, 22);
            icon.setStyle(String.format("-fx-shape: '%s'; -fx-scale-shape: true;", item.svgContent));

            StackPane iconWrap = new StackPane(icon);
            iconWrap.getStyleClass().add("dock-icon-wrap");
            iconWrap.setPrefSize(36, 36);
            iconWrap.setMaxSize(36, 36);

            Label lbl = new Label(item.label);
            lbl.getStyleClass().add("dock-label");

            VBox cell = new VBox(5, iconWrap, lbl);
            cell.setAlignment(Pos.CENTER);
            cell.getStyleClass().add("dock-cell");
            HBox.setHgrow(cell, Priority.ALWAYS);
            cell.setMaxWidth(Double.MAX_VALUE);

            cell.setOnMouseClicked(e -> selectItem(idx));
            cell.setOnMouseEntered(e -> { if (idx != activeIndex) applyHover(idx, true); });
            cell.setOnMouseExited(e -> applyHover(idx, false));

            cells.add(cell);
            iconNodes.add(icon);
            labelNodes.add(lbl);
            itemRow.getChildren().add(cell);
        }
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Select the item at zero-based index and fire the selection callback. */
    public void selectItem(int index) {
        if (index < 0 || index >= items.size() || index == activeIndex) return;
        activeIndex = index;

        for (int i = 0; i < cells.size(); i++) applyActive(i, i == index);

        bounceIcon(iconNodes.get(index));
        placeIndicator(index, true);

        if (onSelectionChanged != null) onSelectionChanged.accept(items.get(index).id);
    }

    /** Select the item whose id matches, or no-op if not found. */
    public void selectById(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(id)) { selectItem(i); return; }
        }
    }

    /** Register a callback that receives the selected item's id. */
    public void setOnSelectionChanged(Consumer<String> listener) {
        this.onSelectionChanged = listener;
    }

    /** Returns the id of the currently active item, or null if none selected yet. */
    public String getActiveId() {
        return activeIndex >= 0 ? items.get(activeIndex).id : null;
    }

    // ─── Animation ────────────────────────────────────────────────────────────

    private void bounceIcon(Region icon) {
        ScaleTransition up = new ScaleTransition(Duration.millis(110), icon);
        up.setToX(1.40); up.setToY(1.40);

        ScaleTransition down = new ScaleTransition(Duration.millis(170), icon);
        down.setToX(1.0); down.setToY(1.0);
        down.setInterpolator(Interpolator.EASE_OUT);

        new SequentialTransition(up, down).play();
    }

    private void placeIndicator(int index, boolean animate) {
        double totalW = getWidth();
        if (totalW <= 0 || items.isEmpty()) return;

        double cellW   = totalW / items.size();
        double indW    = Math.min(cellW * 0.44, 50);
        double targetX = cellW * index + (cellW - indW) / 2.0;
        double dockH   = overlay.getHeight() > 0 ? overlay.getHeight() : getPrefHeight();
        double targetY = dockH - indicator.getHeight() - 3;

        indicator.setWidth(indW);
        indicator.setLayoutY(targetY);

        if (animate && activeIndex >= 0) {
            new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(indicator.layoutXProperty(), indicator.getLayoutX(), Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(300),
                    new KeyValue(indicator.layoutXProperty(), targetX, Interpolator.EASE_BOTH))
            ).play();
        } else {
            indicator.setLayoutX(targetX);
        }
    }

    // ─── Style helpers ────────────────────────────────────────────────────────

    private void applyActive(int idx, boolean active) {
        VBox   cell = cells.get(idx);
        Region icon = iconNodes.get(idx);
        Label  lbl  = labelNodes.get(idx);

        if (active) {
            cell.getStyleClass().add("dock-cell-active");
            icon.getStyleClass().add("dock-icon-active");
            lbl.getStyleClass().add("dock-label-active");
        } else {
            cell.getStyleClass().removeAll("dock-cell-active");
            icon.getStyleClass().removeAll("dock-icon-active", "dock-icon-hover");
            lbl.getStyleClass().removeAll("dock-label-active", "dock-label-hover");
        }
    }

    private void applyHover(int idx, boolean on) {
        if (idx == activeIndex) return;
        Region icon = iconNodes.get(idx);
        Label  lbl  = labelNodes.get(idx);
        if (on) {
            icon.getStyleClass().add("dock-icon-hover");
            lbl.getStyleClass().add("dock-label-hover");
        } else {
            icon.getStyleClass().removeAll("dock-icon-hover");
            lbl.getStyleClass().removeAll("dock-label-hover");
        }
    }
}
