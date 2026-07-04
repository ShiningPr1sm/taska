package dev.shiningpr1sm.taska.tui.theme;

import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

public enum AppFont {
    CONSOLAS("Consolas", "Consolas"),
    COURIER_NEW("Courier New", "Courier New"),
    LUCIDA_CONSOLE("Lucida Console", "Lucida Console"),
    DEJAVU_SANS_MONO("DejaVu Sans Mono", "DejaVu Sans Mono"),
    SYSTEM_MONO("System-based single-width", Font.MONOSPACED);

    private final String displayName;
    private final String awtFontName;

    AppFont(String displayName, String awtFontName) {
        this.displayName = displayName;
        this.awtFontName = awtFontName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAvailable() {
        Font font = new Font(awtFontName, Font.PLAIN, 16);

        if (Font.MONOSPACED.equals(awtFontName)) {
            return true;
        }

        String actualFamily = font.getFamily();
        boolean nameMatches = font.getFontName().equalsIgnoreCase(awtFontName)
                || actualFamily.equalsIgnoreCase(awtFontName);
        if (!nameMatches) {
            return false;
        }

        return isMonospaced(font);
    }

    private boolean isMonospaced(Font font) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        FontMetrics metrics = img.getGraphics().getFontMetrics(font);
        int iWidth = metrics.charWidth('i');
        int mWidth = metrics.charWidth('W');
        return iWidth == mWidth;
    }

    public SwingTerminalFontConfiguration toFontConfiguration() {
        Font font = new Font(awtFontName, Font.PLAIN, 16);
        return SwingTerminalFontConfiguration.newInstance(font);
    }

    public static String[] listSystemFontFamilies() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }
}