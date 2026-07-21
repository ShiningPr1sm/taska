package dev.shiningpr1sm.taska;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {

    public static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".taska");

    public static Path resolve(String filename) {
        return DATA_DIR.resolve(filename);
    }

    private AppConfig() {
    }
}
