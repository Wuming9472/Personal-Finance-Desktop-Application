package it.unicas.project.template.address.util;

public class ThemeManager {

    // Costanti per i temi disponibili
    public static final String PRIMER_THEME_DARK = "primer-dark.css";
    public static final String PRIMER_THEME_LIGHT = "primer-light.css";
    public static final String NORD_THEME_DARK = "nord-dark.css";
    public static final String NORD_THEME_LIGHT = "nord-light.css";
    public static final String CUPERTINO_THEME_DARK = "cupertino-dark.css";
    public static final String CUPERTINO_THEME_LIGHT = "cupertino-light.css";
    public static final String DRACULA = "dracula.css";
    public static final String CASPIAN_EMBEDDED = "caspian-embedded.css";
    public static final String CASPIAN_EMBEDDED_HC = "caspian-embedded-highContrast.css";
    public static final String CASPIAN_EMBEDDED_QVGA = "caspian-embedded-qvga-highContrast.css";
    public static final String CASPIAN_EMBEDDED_QVGA_HC = "caspian-embedded-highContrast.css";

    // Variabile statica per il tema attuale
    private static String currentTheme = NORD_THEME_DARK;

    /**
     * Imposta il tema attuale
     * @param theme nome del file CSS (es. "primer-dark.css")
     */
    public static void setTheme(String theme) {
        currentTheme = theme;
    }

    /**
     * Ottiene il percorso completo del tema attuale per i resource
     * @return percorso da usare con getResource()
     */
    public static String getThemePath() {
        return "/view/css/" + currentTheme;
    }

    /**
     * Ottiene il nome del tema attuale
     * @return nome del file CSS
     */
    public static String getThemeName() {
        return currentTheme;
    }
}
