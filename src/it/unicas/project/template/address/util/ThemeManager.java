package it.unicas.project.template.address.util;

import javafx.application.Platform;
import javafx.scene.Scene;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Gestisce il tema dell'applicazione in base alle impostazioni di sistema.
 * Su Windows rileva automaticamente se il sistema è in modalità chiara o scura
 * e applica il CSS corrispondente (primer-light.css o primer-dark.css).
 */
public class ThemeManager {

    private static ThemeManager instance;

    private final List<Scene> managedScenes = new ArrayList<>();
    private boolean isDarkMode;
    private ScheduledExecutorService themeWatcher;

    // Percorsi CSS relativi alla cartella view
    private static final String LIGHT_THEME_CSS = "/it/unicas/project/template/address/view/css/primer-light.css";
    private static final String DARK_THEME_CSS = "/it/unicas/project/template/address/view/css/primer-dark.css";
    private static final String COLLAPSIBLE_MENU_CSS = "/it/unicas/project/template/address/view/css/collapsible-menu.css";

    private ThemeManager() {
        this.isDarkMode = detectSystemDarkMode();
    }

    /**
     * Ottiene l'istanza singleton del ThemeManager.
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Rileva se il sistema operativo è in modalità scura.
     * Supporta Windows 10/11 leggendo il registro di sistema.
     */
    public boolean detectSystemDarkMode() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return detectWindowsDarkMode();
        } else if (os.contains("mac")) {
            return detectMacOSDarkMode();
        } else if (os.contains("nux") || os.contains("nix")) {
            return detectLinuxDarkMode();
        }

        // Default: modalità chiara
        return false;
    }

    /**
     * Rileva la modalità scura su Windows leggendo il registro.
     * Chiave: HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize
     * Valore: AppsUseLightTheme (0 = dark, 1 = light)
     */
    private boolean detectWindowsDarkMode() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "reg", "query",
                "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("AppsUseLightTheme")) {
                        // Il formato è: "    AppsUseLightTheme    REG_DWORD    0x0" o "0x1"
                        // 0x0 = Dark mode, 0x1 = Light mode
                        return line.contains("0x0");
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Errore nel rilevamento tema Windows: " + e.getMessage());
        }
        return false;
    }

    /**
     * Rileva la modalità scura su macOS.
     */
    private boolean detectMacOSDarkMode() {
        try {
            ProcessBuilder pb = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.toLowerCase().contains("dark")) {
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            // Se il comando fallisce, probabilmente è in modalità chiara
        }
        return false;
    }

    /**
     * Rileva la modalità scura su Linux (GTK).
     */
    private boolean detectLinuxDarkMode() {
        try {
            // Prova con gsettings (GNOME)
            ProcessBuilder pb = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.toLowerCase().contains("dark")) {
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            // Ignora errori
        }
        return false;
    }

    /**
     * Restituisce true se il sistema è in modalità scura.
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Restituisce il percorso del CSS del tema corrente.
     */
    public String getCurrentThemeCssPath() {
        return isDarkMode ? DARK_THEME_CSS : LIGHT_THEME_CSS;
    }

    /**
     * Restituisce l'URL del CSS del tema corrente.
     */
    public String getCurrentThemeCssUrl() {
        return getClass().getResource(getCurrentThemeCssPath()).toExternalForm();
    }

    /**
     * Restituisce l'URL del CSS per il menu collassabile.
     */
    public String getCollapsibleMenuCssUrl() {
        return getClass().getResource(COLLAPSIBLE_MENU_CSS).toExternalForm();
    }

    /**
     * Applica il tema corrente a una scena.
     * Rimuove eventuali temi precedenti e applica quello corretto.
     */
    public void applyTheme(Scene scene) {
        if (scene == null) return;

        // Rimuovi i CSS dei temi precedenti
        scene.getStylesheets().removeIf(css ->
            css.contains("primer-light.css") || css.contains("primer-dark.css")
        );

        // Aggiungi il tema corrente all'inizio
        String themeCss = getCurrentThemeCssUrl();
        if (!scene.getStylesheets().contains(themeCss)) {
            scene.getStylesheets().add(0, themeCss);
        }
    }

    /**
     * Applica il tema corrente a una scena e aggiunge anche il CSS del menu collassabile.
     * Usa questo metodo per il RootLayout.
     */
    public void applyThemeWithCollapsibleMenu(Scene scene) {
        if (scene == null) return;

        // Rimuovi tutti i CSS dei temi
        scene.getStylesheets().removeIf(css ->
            css.contains("primer-light.css") ||
            css.contains("primer-dark.css") ||
            css.contains("collapsible-menu.css")
        );

        // Aggiungi il tema corrente e il menu collassabile
        scene.getStylesheets().add(getCurrentThemeCssUrl());
        scene.getStylesheets().add(getCollapsibleMenuCssUrl());
    }

    /**
     * Registra una scena per il monitoraggio automatico del tema.
     * Quando il tema di sistema cambia, la scena viene aggiornata automaticamente.
     */
    public void registerScene(Scene scene) {
        if (scene != null && !managedScenes.contains(scene)) {
            managedScenes.add(scene);
            applyTheme(scene);
        }
    }

    /**
     * Rimuove una scena dal monitoraggio.
     */
    public void unregisterScene(Scene scene) {
        managedScenes.remove(scene);
    }

    /**
     * Avvia il monitoraggio automatico del tema di sistema.
     * Controlla ogni 2 secondi se il tema è cambiato.
     */
    public void startThemeWatcher() {
        if (themeWatcher != null && !themeWatcher.isShutdown()) {
            return; // Già in esecuzione
        }

        themeWatcher = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ThemeWatcher");
            t.setDaemon(true);
            return t;
        });

        themeWatcher.scheduleAtFixedRate(() -> {
            boolean newDarkMode = detectSystemDarkMode();
            if (newDarkMode != isDarkMode) {
                isDarkMode = newDarkMode;
                Platform.runLater(this::updateAllScenes);
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    /**
     * Ferma il monitoraggio del tema di sistema.
     */
    public void stopThemeWatcher() {
        if (themeWatcher != null) {
            themeWatcher.shutdown();
            themeWatcher = null;
        }
    }

    /**
     * Aggiorna tutte le scene registrate con il tema corrente.
     */
    private void updateAllScenes() {
        for (Scene scene : managedScenes) {
            applyTheme(scene);
        }
    }

    /**
     * Forza il refresh del tema (rileva di nuovo e applica).
     */
    public void refreshTheme() {
        isDarkMode = detectSystemDarkMode();
        updateAllScenes();
    }

    /**
     * Imposta manualmente la modalità scura (utile per testing o preferenze utente).
     */
    public void setDarkMode(boolean darkMode) {
        if (this.isDarkMode != darkMode) {
            this.isDarkMode = darkMode;
            updateAllScenes();
        }
    }
}
