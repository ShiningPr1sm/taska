package dev.shiningpr1sm.taska.update;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;

import java.util.List;

public class UpdateDialog {

    public enum Choice { UPDATE, KEEP_OLD }

    public static Choice show(WindowBasedTextGUI gui, String currentVersion, String newVersion, String notesMarkdown) {
        Choice[] result = { Choice.KEEP_OLD };

        BasicWindow window = new BasicWindow("New version is here!");
        window.setHints(List.of(Window.Hint.CENTERED));

        Panel root = new Panel(new LinearLayout(Direction.VERTICAL));

        root.addComponent(new Label(currentVersion + "  ->  " + newVersion));
        root.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        root.addComponent(new Label("What's new?"));

        TextBox notesBox = new TextBox(
                new TerminalSize(50, 12),
                MarkdownUtil.toPlainText(notesMarkdown),
                TextBox.Style.MULTI_LINE
        );
        notesBox.setReadOnly(true);
        root.addComponent(notesBox);

        root.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Button updateButton = new Button("Update", () -> {
            result[0] = Choice.UPDATE;
            window.close();
        });
        Button keepButton = new Button("Keep old version", () -> {
            result[0] = Choice.KEEP_OLD;
            window.close();
        });
        buttonPanel.addComponent(updateButton);
        buttonPanel.addComponent(new EmptySpace(new TerminalSize(2, 1)));
        buttonPanel.addComponent(keepButton);
        root.addComponent(buttonPanel);

        window.setComponent(root);
        gui.addWindowAndWait(window);

        return result[0];
    }
}