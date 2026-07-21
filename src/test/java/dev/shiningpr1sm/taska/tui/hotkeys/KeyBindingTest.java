package dev.shiningpr1sm.taska.tui.hotkeys;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyBindingTest {

    @Test
    void matchesCharacterKey() {
        KeyBinding binding = KeyBinding.ofCharacter('q', false, false);
        KeyStroke stroke = new KeyStroke('q', false, false);
        assertTrue(binding.matches(stroke));
    }

    @Test
    void matchesCaseInsensitive() {
        KeyBinding binding = KeyBinding.ofCharacter('a', false, false);
        KeyStroke stroke = new KeyStroke('A', false, false);
        assertTrue(binding.matches(stroke));
    }

    @Test
    void doesNotMatchDifferentKey() {
        KeyBinding binding = KeyBinding.ofCharacter('q', false, false);
        KeyStroke stroke = new KeyStroke('w', false, false);
        assertFalse(binding.matches(stroke));
    }

    @Test
    void matchesCtrlModifier() {
        KeyBinding binding = KeyBinding.ofCharacter('n', true, false);
        KeyStroke stroke = new KeyStroke('n', true, false);
        assertTrue(binding.matches(stroke));
    }

    @Test
    void doesNotMatchWithoutCtrl() {
        KeyBinding binding = KeyBinding.ofCharacter('n', true, false);
        KeyStroke stroke = new KeyStroke('n', false, false);
        assertFalse(binding.matches(stroke));
    }

    @Test
    void matchesSpecialKey() {
        KeyBinding binding = KeyBinding.ofSpecial(KeyType.Escape, false, false);
        KeyStroke stroke = new KeyStroke(KeyType.Escape);
        assertTrue(binding.matches(stroke));
    }

    @Test
    void doesNotMatchSpecialKeyWithCtrl() {
        KeyBinding binding = KeyBinding.ofSpecial(KeyType.Escape, false, false);
        KeyStroke stroke = new KeyStroke(KeyType.Escape, true, false);
        assertFalse(binding.matches(stroke));
    }

    @Test
    void serializeCharacter() {
        assertEquals("q", KeyBinding.ofCharacter('q', false, false).serialize());
    }

    @Test
    void serializeCtrlCharacter() {
        assertEquals("CTRL+n", KeyBinding.ofCharacter('n', true, false).serialize());
    }

    @Test
    void serializeAltCharacter() {
        assertEquals("ALT+x", KeyBinding.ofCharacter('x', false, true).serialize());
    }

    @Test
    void serializeCtrlAltCharacter() {
        assertEquals("CTRL+ALT+z", KeyBinding.ofCharacter('z', true, true).serialize());
    }

    @Test
    void serializeSpecialKey() {
        assertEquals("Escape", KeyBinding.ofSpecial(KeyType.Escape, false, false).serialize());
    }

    @Test
    void serializeCtrlSpecialKey() {
        assertEquals("CTRL+F1", KeyBinding.ofSpecial(KeyType.F1, true, false).serialize());
    }

    @Test
    void parseSingleCharacter() {
        KeyBinding parsed = KeyBinding.parse("q");
        assertEquals("q", parsed.serialize());
    }

    @Test
    void parseCtrlCombo() {
        KeyBinding parsed = KeyBinding.parse("CTRL+n");
        assertEquals("CTRL+n", parsed.serialize());
    }

    @Test
    void parseAltCombo() {
        KeyBinding parsed = KeyBinding.parse("ALT+x");
        assertEquals("ALT+x", parsed.serialize());
    }

    @Test
    void parseSpecialKey() {
        KeyBinding parsed = KeyBinding.parse("Escape");
        assertEquals("Escape", parsed.serialize());
    }

    @Test
    void parseCaseInsensitivePrefix() {
        KeyBinding parsed = KeyBinding.parse("ctrl+N");
        assertEquals("CTRL+n", parsed.serialize());
    }

    @Test
    void roundTripSerializeParse() {
        KeyBinding original = KeyBinding.ofCharacter('a', true, true);
        KeyBinding parsed = KeyBinding.parse(original.serialize());
        assertEquals(original.serialize(), parsed.serialize());
    }

    @Test
    void roundTripSpecialKey() {
        KeyBinding original = KeyBinding.ofSpecial(KeyType.F5, false, false);
        KeyBinding parsed = KeyBinding.parse(original.serialize());
        assertEquals("F5", parsed.serialize());
    }
}
