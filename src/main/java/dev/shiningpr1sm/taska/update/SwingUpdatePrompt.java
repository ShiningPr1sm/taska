package dev.shiningpr1sm.taska.update;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SwingUpdatePrompt {

    public enum Choice { UPDATE, KEEP_OLD }

    private static final Color BG_COLOR = new Color(30, 30, 34);
    private static final Color PANEL_COLOR = new Color(40, 40, 45);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color ACCENT_COLOR = new Color(88, 101, 242);
    private static final Color ACCENT_HOVER = new Color(108, 121, 255);
    private static final Color SECONDARY_COLOR = new Color(60, 60, 66);
    private static final Color SECONDARY_HOVER = new Color(75, 75, 82);

    public static Choice show(String currentVersion, String newVersion, String notesMarkdown) {
        Choice[] result = { Choice.KEEP_OLD };

        JDialog dialog = new JDialog();
        dialog.setUndecorated(false);
        dialog.setTitle("New version is here!");
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(BG_COLOR);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_COLOR);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel versionLabel = new JLabel(currentVersion + "   →   " + newVersion, SwingConstants.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        versionLabel.setForeground(ACCENT_COLOR);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, versionLabel.getPreferredSize().height));

        JLabel whatsNewLabel = new JLabel("What's new?");
        whatsNewLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        whatsNewLabel.setForeground(TEXT_COLOR);
        whatsNewLabel.setBorder(new EmptyBorder(12, 0, 6, 0));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BG_COLOR);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        whatsNewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(versionLabel);
        topPanel.add(whatsNewLabel);

        String notesHtml = "<html><body style='font-family: Segoe UI, Segoe UI Emoji; font-size: 12px; "
                + "color: rgb(230,230,230); background-color: rgb(40,40,45); margin: 4px;'>"
                + MarkdownUtil.toPlainText(notesMarkdown).replace("\n", "<br/>")
                + "</body></html>";

        JEditorPane editorPane = new JEditorPane("text/html", notesHtml);
        editorPane.setEditable(false);
        editorPane.setOpaque(true);
        editorPane.setBackground(PANEL_COLOR);
        editorPane.setCaret(new javax.swing.text.DefaultCaret() {
            @Override
            public void paint(Graphics g) {}

            @Override
            public boolean isVisible() { return false; }
        });
        editorPane.setHighlighter(null);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(440, 260));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 66), 1));
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        RoundedButton updateButton = new RoundedButton("Update", ACCENT_COLOR, ACCENT_HOVER, Color.WHITE);
        RoundedButton keepButton = new RoundedButton("Keep old version", SECONDARY_COLOR, SECONDARY_HOVER, TEXT_COLOR);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        buttonPanel.add(keepButton);
        buttonPanel.add(updateButton);

        updateButton.addActionListener(e -> {
            result[0] = Choice.UPDATE;
            dialog.dispose();
        });
        keepButton.addActionListener(e -> {
            result[0] = Choice.KEEP_OLD;
            dialog.dispose();
        });

        root.add(topPanel, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        return result[0];
    }

    private static void styleScrollBar(JScrollBar bar) {
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT_COLOR;
                this.trackColor = PANEL_COLOR;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) { return invisibleButton(); }

            @Override
            protected JButton createIncreaseButton(int orientation) { return invisibleButton(); }

            private JButton invisibleButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                        thumbBounds.width - 8, thumbBounds.height - 4, 6, 6);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });
    }

    private static class RoundedButton extends JButton {
        private final Color base;
        private final Color hover;
        private boolean isHovered = false;

        RoundedButton(String text, Color base, Color hover, Color textColor) {
            super(text);
            this.base = base;
            this.hover = hover;

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(textColor);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 24, 10, 24));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered ? hover : base);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}