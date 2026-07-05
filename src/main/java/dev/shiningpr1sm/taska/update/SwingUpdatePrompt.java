package dev.shiningpr1sm.taska.update;

import javax.swing.*;
import java.awt.*;

public class SwingUpdatePrompt {

    public enum Choice { UPDATE, KEEP_OLD }

    public static Choice show(String currentVersion, String newVersion, String notesMarkdown) {
        JTextArea textArea = new JTextArea(MarkdownUtil.toPlainText(notesMarkdown));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(420, 280));

        JLabel versionLabel = new JLabel(currentVersion + "   ->   " + newVersion, SwingConstants.CENTER);
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD, 14f));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(versionLabel, BorderLayout.CENTER);
        topPanel.add(new JLabel("What's new?"), BorderLayout.SOUTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        Object[] options = {"Update", "Keep old version"};
        int result = JOptionPane.showOptionDialog(
                null,
                panel,
                "New version is here!",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        return result == 0 ? Choice.UPDATE : Choice.KEEP_OLD;
    }
}