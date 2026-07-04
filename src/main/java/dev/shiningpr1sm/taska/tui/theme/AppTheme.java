package dev.shiningpr1sm.taska.tui.theme;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;

public enum AppTheme {

    PURPLE("Purple") {
        @Override
        public Theme toLanternaTheme() {
            return SimpleTheme.makeTheme(
                    true,
                    new TextColor.RGB(230, 220, 255), // baseForeground
                    new TextColor.RGB(40, 15, 60),    // baseBackground
                    new TextColor.RGB(230, 220, 255), // editableForeground
                    new TextColor.RGB(60, 25, 90),    // editableBackground
                    TextColor.ANSI.WHITE,                      // selectedForeground
                    new TextColor.RGB(150, 80, 220),  // selectedBackground
                    new TextColor.RGB(25, 10, 40)     // guiBackground
            );
        }
    },
    PINK("Pink") {
        @Override
        public Theme toLanternaTheme() {
            return SimpleTheme.makeTheme(
                    true,
                    new TextColor.RGB(255, 235, 240),
                    new TextColor.RGB(70, 25, 45),
                    new TextColor.RGB(255, 235, 240),
                    new TextColor.RGB(95, 35, 60),
                    TextColor.ANSI.WHITE,
                    new TextColor.RGB(225, 90, 150),
                    new TextColor.RGB(45, 15, 30)
            );
        }
    },
    GREEN("Green") {
        @Override
        public Theme toLanternaTheme() {
            return SimpleTheme.makeTheme(
                    true,
                    new TextColor.RGB(210, 255, 210),
                    new TextColor.RGB(10, 40, 20),
                    new TextColor.RGB(210, 255, 210),
                    new TextColor.RGB(15, 60, 30),
                    TextColor.ANSI.WHITE,
                    new TextColor.RGB(60, 160, 80),
                    new TextColor.RGB(5, 25, 12)
            );
        }
    },
    CLASSIC("Classic") {
        @Override
        public Theme toLanternaTheme() {
            return SimpleTheme.makeTheme(
                    false,
                    TextColor.ANSI.BLACK,
                    new TextColor.RGB(245, 245, 245),
                    TextColor.ANSI.BLACK,
                    TextColor.ANSI.WHITE,
                    TextColor.ANSI.WHITE,
                    new TextColor.RGB(90, 130, 220),
                    new TextColor.RGB(225, 225, 225)
            );
        }
    },
    DARK("Dark") {
        @Override
        public Theme toLanternaTheme() {
            return SimpleTheme.makeTheme(
                    true,
                    new TextColor.RGB(220, 220, 220),
                    TextColor.ANSI.BLACK,
                    new TextColor.RGB(220, 220, 220),
                    new TextColor.RGB(30, 30, 30),
                    TextColor.ANSI.WHITE,
                    new TextColor.RGB(70, 70, 70),
                    TextColor.ANSI.BLACK
            );
        }
    },
    COAL("Coal") {
        @Override
        public Theme toLanternaTheme() {
            return SimpleTheme.makeTheme(
                    true,
                    new TextColor.RGB(210, 210, 210),
                    new TextColor.RGB(45, 45, 45),
                    new TextColor.RGB(210, 210, 210),
                    new TextColor.RGB(60, 60, 60),
                    TextColor.ANSI.WHITE,
                    new TextColor.RGB(100, 100, 100),
                    new TextColor.RGB(30, 30, 30)
            );
        }
    },
    RETRO("Retro-style") {
        @Override
        public Theme toLanternaTheme() {
            Theme registered = LanternaThemes.getRegisteredTheme("default");
            return registered != null ? registered : LanternaThemes.getDefaultTheme();
        }
    };

    private final String displayName;

    AppTheme(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract Theme toLanternaTheme();
}