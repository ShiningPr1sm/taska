package dev.shiningpr1sm.taska.tui.hotkeys;

import com.googlecode.lanterna.input.KeyStroke;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class KeyBindingsManager {

    private static final Path CONFIG_PATH =
            Paths.get(System.getProperty("user.home"), ".taska", "keybindings.properties");

    private final Map<KeyAction, KeyBinding> bindings = new LinkedHashMap<>();

    public KeyBindingsManager() {
        loadDefaults();
        load();
    }

    private void loadDefaults() {
        bindings.put(KeyAction.QUIT, KeyBinding.ofCharacter('q', false, false));
        bindings.put(KeyAction.FOCUS_LISTS, KeyBinding.ofCharacter('l', false, false));
        bindings.put(KeyAction.FOCUS_TASKS, KeyBinding.ofCharacter('k', false, false));
        bindings.put(KeyAction.NEW_LIST, KeyBinding.ofCharacter('n', true, false)); // Ctrl+N
        bindings.put(KeyAction.NEW_TASK, KeyBinding.ofCharacter('a', false, false));
        bindings.put(KeyAction.DELETE_ITEM, KeyBinding.ofCharacter('d', false, false));
        bindings.put(KeyAction.EDIT_ITEM, KeyBinding.ofCharacter('e', false, false));
        bindings.put(KeyAction.THEME_SELECT, KeyBinding.ofCharacter('t', false, false));
        bindings.put(KeyAction.FONT_SELECT, KeyBinding.ofCharacter('f', false, false));
        bindings.put(KeyAction.HELP, KeyBinding.ofCharacter('h', false, false));
        bindings.put(KeyAction.REBIND, KeyBinding.ofCharacter('r', true, false));
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            props.load(in);
            for (KeyAction action : KeyAction.values()) {
                String value = props.getProperty(action.name());
                if (value != null && !value.isBlank()) {
                    try {
                        bindings.put(action, KeyBinding.parse(value));
                    } catch (Exception ignored) {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Properties props = new Properties();
            for (Map.Entry<KeyAction, KeyBinding> entry : bindings.entrySet()) {
                props.setProperty(entry.getKey().name(), entry.getValue().serialize());
            }
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "taska keybindings — you can edit them manually or by pressing Ctrl+R in the app");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public KeyAction resolve(KeyStroke keyStroke) {
        for (Map.Entry<KeyAction, KeyBinding> entry : bindings.entrySet()) {
            if (entry.getValue().matches(keyStroke)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static final List<KeyAction> PRIORITY_ORDER = List.of(
            KeyAction.QUIT,
            KeyAction.FOCUS_LISTS,
            KeyAction.FOCUS_TASKS,
            KeyAction.NEW_LIST,
            KeyAction.NEW_TASK,
            KeyAction.DELETE_ITEM,
            KeyAction.EDIT_ITEM,
            KeyAction.THEME_SELECT,
            KeyAction.FONT_SELECT,
            KeyAction.HELP,
            KeyAction.REBIND
    );

    public KeyBinding getBinding(KeyAction action) {
        return bindings.get(action);
    }

    public void setBinding(KeyAction action, KeyBinding binding) {
        bindings.put(action, binding);
        save();
    }

    public Map<KeyAction, KeyBinding> getAllBindings() {
        return Collections.unmodifiableMap(bindings);
    }
}