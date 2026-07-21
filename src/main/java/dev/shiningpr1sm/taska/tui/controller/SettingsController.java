package dev.shiningpr1sm.taska.tui.controller;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.input.KeyStroke;
import dev.shiningpr1sm.taska.tui.TuiContext;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyAction;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyBinding;
import dev.shiningpr1sm.taska.tui.theme.AppFont;
import dev.shiningpr1sm.taska.tui.theme.AppTheme;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsController {

    private final TuiContext ctx;

    public SettingsController(TuiContext ctx) {
        this.ctx = ctx;
    }

    public void openThemeDialog() {
        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Choosing a theme")
                .setDescription("Current: " + ctx.themeManager().getCurrentTheme().getDisplayName())
                .setCanCancel(true);

        for (AppTheme theme : AppTheme.values()) {
            builder.addAction(theme.getDisplayName(), () -> ctx.themeManager().setTheme(theme, ctx.gui()));
        }

        builder.build().showDialog(ctx.gui());
    }

    public void openFontDialog() {
        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Select a font")
                .setDescription("Current: " + ctx.fontManager().getCurrentFont().getDisplayName()
                        + " (The window will restart to apply the changes)")
                .setCanCancel(true);

        boolean anyAvailable = false;
        for (AppFont font : AppFont.values()) {
            if (font.isAvailable()) {
                anyAvailable = true;
                builder.addAction(font.getDisplayName(), () -> relaunchWithNewFont(font));
            }
        }

        if (!anyAvailable) {
            MessageDialog.showMessageDialog(ctx.gui(), "Fonts",
                    "None of the configured fonts were found on the system.");
            return;
        }

        builder.build().showDialog(ctx.gui());
    }

    public void showHelpDialog() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<KeyAction, KeyBinding> entry : ctx.keyBindings().getAllBindings().entrySet()) {
            sb.append(entry.getValue().serialize())
                    .append("  —  ")
                    .append(entry.getKey().getDescription())
                    .append("\n");
        }
        MessageDialog.showMessageDialog(ctx.gui(), "Keyboard Shortcuts", sb.toString());
    }

    public void openRebindDialog() {
        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Rework the key")
                .setDescription("Select an action:");

        for (KeyAction action : KeyAction.values()) {
            KeyBinding current = ctx.keyBindings().getBinding(action);
            String label = "[" + (current != null ? current.serialize() : "-") + "]  " + action.getDescription();
            builder.addAction(label, () -> captureNewKeyFor(action));
        }
        builder.build().showDialog(ctx.gui());
    }

    private void captureNewKeyFor(KeyAction action) {
        BasicWindow captureWindow = new BasicWindow("New key for: " + action.name());
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label("Press the desired key combination"));
        panel.addComponent(new Label("(ESC — cancel)"));
        captureWindow.setComponent(panel);
        captureWindow.setHints(Collections.singletonList(Window.Hint.CENTERED));

        captureWindow.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                hasBeenHandled.set(true);
                if (keyStroke.getKeyType() == com.googlecode.lanterna.input.KeyType.Escape) {
                    captureWindow.close();
                    return;
                }
                ctx.keyBindings().setBinding(action, KeyBinding.fromKeyStroke(keyStroke));
                captureWindow.close();
            }
        });

        ctx.gui().addWindowAndWait(captureWindow);
    }

    private void relaunchWithNewFont(AppFont font) {
        if (!font.isAvailable()) {
            MessageDialog.showMessageDialog(ctx.gui(), "Error",
                    "Font \"" + font.getDisplayName() + "\" is not available on this system.");
            return;
        }

        ctx.setPendingListIndex(Math.max(0, ctx.listsBox().getSelectedIndex()));
        ctx.setPendingTaskIndex(Math.max(0, ctx.tasksBox().getSelectedIndex()));

        ctx.fontManager().setFont(font);
        ctx.setRestartPending(true);
        ctx.window().close();
    }
}
