package dev.shiningpr1sm.taska.tui.theme;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import dev.shiningpr1sm.taska.AppConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ThemeManager {

    private static final Path CONFIG_PATH = AppConfig.resolve("theme.properties");
    private static final String KEY = "theme";

    private AppTheme currentTheme = AppTheme.RETRO;

    public ThemeManager() {
        load();
    }

    public AppTheme getCurrentTheme() {
        return currentTheme;
    }

    public void apply(WindowBasedTextGUI gui) {
        gui.setTheme(currentTheme.toLanternaTheme());
    }

    public void setTheme(AppTheme theme, WindowBasedTextGUI gui) {
        this.currentTheme = theme;
        apply(gui);
        save();
    }

    private void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            props.load(in);
            String saved = props.getProperty(KEY);
            if (saved != null) {
                try {
                    currentTheme = AppTheme.valueOf(saved);
                } catch (IllegalArgumentException ignored) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Properties props = new Properties();
            props.setProperty(KEY, currentTheme.name());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "taska theme selection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}