package com.gestion.utils;

import com.gestion.config.AppConfig;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public final class ThemeManager {

    public enum Theme { DARK, LIGHT }

    private static final String DARK_MAIN  = "/css/dark-futuristic.css";
    private static final String DARK_DOCK  = "/css/interactive-dock.css";
    private static final String LIGHT_MAIN = "/css/light-industrial.css";
    private static final String LIGHT_DOCK = "/css/interactive-dock-light.css";

    private static final Path PREF_FILE = Paths.get(
            System.getProperty("user.home"), ".buscandoelser", "theme.txt");

    private static Theme current = loadPersisted();
    private static final Set<Scene> trackedScenes = new HashSet<>();

    private ThemeManager() {}

    public static Theme getTheme() { return current; }

    public static boolean isDark() { return current == Theme.DARK; }

    /** Aplica el tema actual a una Scene y la registra para futuros cambios. */
    public static void apply(Scene scene) {
        if (scene == null) return;
        trackedScenes.add(scene);
        applyStylesheets(scene.getStylesheets());
    }

    /** Aplica el tema actual a un DialogPane (no se rastrea — modal de corta vida). */
    public static void apply(DialogPane pane) {
        if (pane == null) return;
        applyStylesheets(pane.getStylesheets());
    }

    /** Cambia el tema y re-aplica a todas las scenes registradas + ventanas abiertas. */
    public static void toggle() {
        current = (current == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        persist();
        reapplyAll();
    }

    public static void setTheme(Theme t) {
        if (t == null || t == current) return;
        current = t;
        persist();
        reapplyAll();
    }

    private static void applyStylesheets(ObservableList<String> sheets) {
        String main = ThemeManager.class.getResource(currentMain()).toExternalForm();
        String dock = ThemeManager.class.getResource(currentDock()).toExternalForm();
        String otherMain = ThemeManager.class.getResource(otherMain()).toExternalForm();
        String otherDock = ThemeManager.class.getResource(otherDock()).toExternalForm();

        sheets.remove(otherMain);
        sheets.remove(otherDock);
        if (!sheets.contains(main)) sheets.add(main);
        if (!sheets.contains(dock)) sheets.add(dock);
    }

    private static void reapplyAll() {
        trackedScenes.removeIf(s -> s.getWindow() == null);
        for (Scene s : trackedScenes) applyStylesheets(s.getStylesheets());

        // Cubrir ventanas abiertas que no fueron registradas
        for (Window w : Window.getWindows()) {
            if (w instanceof Stage st && st.getScene() != null) {
                applyStylesheets(st.getScene().getStylesheets());
            }
        }
    }

    private static String currentMain() { return current == Theme.DARK ? DARK_MAIN : LIGHT_MAIN; }
    private static String currentDock() { return current == Theme.DARK ? DARK_DOCK : LIGHT_DOCK; }
    private static String otherMain()   { return current == Theme.DARK ? LIGHT_MAIN : DARK_MAIN; }
    private static String otherDock()   { return current == Theme.DARK ? LIGHT_DOCK : DARK_DOCK; }

    private static Theme loadPersisted() {
        try {
            if (Files.exists(PREF_FILE)) {
                String v = Files.readString(PREF_FILE).trim().toUpperCase();
                if ("LIGHT".equals(v)) return Theme.LIGHT;
            }
        } catch (IOException ignored) {}
        return Theme.DARK;
    }

    private static void persist() {
        try {
            Files.createDirectories(PREF_FILE.getParent());
            Files.writeString(PREF_FILE, current.name());
        } catch (IOException ignored) {}
    }
}
