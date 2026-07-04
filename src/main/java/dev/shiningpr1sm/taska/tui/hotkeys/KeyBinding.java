package dev.shiningpr1sm.taska.tui.hotkeys;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

public final class KeyBinding {

    private final KeyType keyType; // (ENTER, F1, ESCAPE...)
    private final Character character;
    private final boolean ctrl;
    private final boolean alt;

    private KeyBinding(KeyType keyType, Character character, boolean ctrl, boolean alt) {
        this.keyType = keyType;
        this.character = character;
        this.ctrl = ctrl;
        this.alt = alt;
    }

    public static KeyBinding ofCharacter(char c, boolean ctrl, boolean alt) {
        return new KeyBinding(null, Character.toLowerCase(c), ctrl, alt);
    }

    public static KeyBinding ofSpecial(KeyType type, boolean ctrl, boolean alt) {
        return new KeyBinding(type, null, ctrl, alt);
    }

    public static KeyBinding fromKeyStroke(KeyStroke keyStroke) {
        if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() != null) {
            return ofCharacter(keyStroke.getCharacter(), keyStroke.isCtrlDown(), keyStroke.isAltDown());
        }
        return ofSpecial(keyStroke.getKeyType(), keyStroke.isCtrlDown(), keyStroke.isAltDown());
    }

    public boolean matches(KeyStroke keyStroke) {
        if (keyStroke.isCtrlDown() != ctrl) return false;
        if (keyStroke.isAltDown() != alt) return false;

        if (character != null) {
            return keyStroke.getKeyType() == KeyType.Character
                    && keyStroke.getCharacter() != null
                    && Character.toLowerCase(keyStroke.getCharacter()) == character;
        } else {
            return keyStroke.getKeyType() == keyType;
        }
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        if (ctrl) sb.append("CTRL+");
        if (alt) sb.append("ALT+");
        sb.append(character != null ? character.toString() : keyType.name());
        return sb.toString();
    }

    public static KeyBinding parse(String spec) {
        String s = spec.trim();
        boolean ctrl = false, alt = false;

        while (true) {
            String upper = s.toUpperCase();
            if (upper.startsWith("CTRL+")) { ctrl = true; s = s.substring(5); }
            else if (upper.startsWith("ALT+")) { alt = true; s = s.substring(4); }
            else break;
        }

        if (s.length() == 1) {
            return ofCharacter(s.charAt(0), ctrl, alt);
        }
        try {
            return ofSpecial(KeyType.valueOf(s), ctrl, alt);
        } catch (IllegalArgumentException e) {
            return ofCharacter(s.charAt(0), ctrl, alt);
        }
    }

    @Override
    public String toString() {
        return serialize();
    }
}