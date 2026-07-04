package dev.shiningpr1sm.taska.tui.theme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class FontManager {

    private static final Path CONFIG_PATH =
            Paths.get(System.getProperty("user.home"), ".taska", "font.properties");
    private static final String KEY = "font";

    private AppFont currentFont = AppFont.CONSOLAS;

    public FontManager() {
        load();
    }

    public AppFont getCurrentFont() {
        return currentFont;
    }

    public void setFont(AppFont font) {
        this.currentFont = font;
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
                    currentFont = AppFont.valueOf(saved);
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
            props.setProperty(KEY, currentFont.name());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "taska font selection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}