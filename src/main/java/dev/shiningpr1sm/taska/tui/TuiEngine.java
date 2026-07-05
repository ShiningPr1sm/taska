package dev.shiningpr1sm.taska.tui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;
import dev.shiningpr1sm.taska.model.Priority;
import dev.shiningpr1sm.taska.model.Task;
import dev.shiningpr1sm.taska.model.TaskList;
import dev.shiningpr1sm.taska.service.StorageService;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyAction;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyBinding;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyBindingsManager;
import dev.shiningpr1sm.taska.tui.theme.AppFont;
import dev.shiningpr1sm.taska.tui.theme.AppTheme;
import dev.shiningpr1sm.taska.tui.theme.FontManager;
import dev.shiningpr1sm.taska.tui.theme.ThemeManager;
import dev.shiningpr1sm.taska.update.SwingUpdatePrompt;
import dev.shiningpr1sm.taska.update.UpdateApplier;
import dev.shiningpr1sm.taska.update.UpdateDialog;
import dev.shiningpr1sm.taska.update.UpdateManager;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TuiEngine {
    private final StorageService storageService = new StorageService();
    private final KeyBindingsManager keyBindings = new KeyBindingsManager();
    private final ThemeManager themeManager = new ThemeManager();
    private final FontManager fontManager = new FontManager();
    private List<TaskList> allLists;

    private WindowBasedTextGUI gui;
    private BasicWindow window;
    private SwingTerminalFrame terminal;
    private Screen screen;

    private Runnable updateTasksList;
    private ActionListBox listsBox;
    private ActionListBox tasksBox;
    private Label statusLabel;
    private Label priorityLabel;
    private Label notesLabel;
    private Label createdLabel;

    private int pendingListIndex = 0;
    private int pendingTaskIndex = 0;

    public void start() {
        allLists = storageService.loadAllLists();
        if (allLists.isEmpty()) {
            TaskList defaultList = new TaskList("Inbox");
            storageService.saveList(defaultList);
            allLists.add(defaultList);
        }

        checkForUpdatesBeforeLaunch();
        runUntilExit();
    }

    private void runUntilExit() {
        boolean restartRequested = true;
        while (restartRequested) {
            restartRequested = false;
            try {
                launchWindow();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (restartPending) {
                restartRequested = true;
                restartPending = false;
            }
        }
    }

    private void checkForUpdatesBeforeLaunch() {
        String currentVersion = UpdateManager.getCurrentVersion();
        if ("dev".equals(currentVersion)) return;

        UpdateManager.ReleaseInfo release = UpdateManager.fetchLatestRelease();
        if (release == null) return;

        if (UpdateManager.compareVersions(release.version(), currentVersion) <= 0) return;

        String skipped = UpdateManager.loadSkippedVersion();
        if (release.version().equals(skipped)) return;

        SwingUpdatePrompt.Choice choice =
                SwingUpdatePrompt.show(currentVersion, release.version(), release.notesMarkdown());

        if (choice == SwingUpdatePrompt.Choice.KEEP_OLD) {
            UpdateManager.saveSkippedVersion(release.version());
            return;
        }

        try {
            java.nio.file.Path tempJar = java.nio.file.Files.createTempFile("taska_update_", ".jar");
            UpdateManager.downloadRelease(release, tempJar);

            String downloadedVersion = UpdateManager.readJarVersion(tempJar);
            if (downloadedVersion == null || !downloadedVersion.equals(release.version())) {
                JOptionPane.showMessageDialog(null,
                        "The downloaded file failed the version check. The update has been canceled.",
                        "Update failed", JOptionPane.ERROR_MESSAGE);
                java.nio.file.Files.deleteIfExists(tempJar);
                return;
            }

            UpdateApplier.restartWithNewJar(tempJar);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "The update could not be downloaded: " + e.getMessage(),
                    "Update failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean restartPending = false;

    private List<Image> loadAppIcons() {
        List<Image> icons = new ArrayList<>();
        java.net.URL url = getClass().getResource("/icon_256.png");
        if (url != null) {
            Image base = new ImageIcon(url).getImage();
            // Явно генерируем набор размеров из одного качественного исходника —
            // так Windows гарантированно найдёт подходящий вариант без апскейла мелкого файла
            int[] sizes = {16, 24, 32, 48, 64, 128, 256};
            for (int size : sizes) {
                icons.add(base.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
        }
        return icons;
    }

    private void launchWindow() throws IOException {
        terminal = new SwingTerminalFrame(
                "taska",
                new TerminalSize(80, 24),
                null,
                fontManager.getCurrentFont().toFontConfiguration(),
                null,
                TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode
        );

        terminal.setResizable(false);
        terminal.setAutoRequestFocus(true);

        List<Image> icons = loadAppIcons();
        if (!icons.isEmpty()) {
            terminal.setIconImages(icons);
        }

        terminal.setVisible(true);

        screen = new TerminalScreen(terminal);
        screen.startScreen();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                terminal.setLocation(
                        (screenSize.width - terminal.getWidth()) / 2,
                        (screenSize.height - terminal.getHeight()) / 2
                );

        gui = new MultiWindowTextGUI(
                screen,
                new DefaultWindowManager(),
                new EmptySpace(TextColor.ANSI.BLACK)
        );

        themeManager.apply(gui);

        String topBar = buildTopBar("h - help", "taska", "v:" + VersionInfo.getVersion(), 80);
        window = new BasicWindow(topBar);

        Panel mainPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        listsBox = new ActionListBox(new TerminalSize(36, 17)) {
            @Override
            public synchronized Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
                KeyAction action = keyBindings.resolve(keyStroke);
                if (action != null) {
                    dispatchAction(action);
                    return Interactable.Result.HANDLED;
                }
                Interactable.Result result = super.handleKeyStroke(keyStroke);
                refreshListsView();
                updateTasksList.run();
                return result;
            }
        };
        leftPanel.addComponent(listsBox);
        mainPanel.addComponent(leftPanel.withBorder(Borders.singleLine("Lists")),
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Panel tasksPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        tasksBox = new ActionListBox(new TerminalSize(38, 9)) {
            @Override
            public synchronized Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
                KeyAction action = keyBindings.resolve(keyStroke);
                if (action != null) {
                    dispatchAction(action);
                    return Interactable.Result.HANDLED;
                }
                Interactable.Result result = super.handleKeyStroke(keyStroke);
                refreshDetailsPanel();
                return result;
            }
        };
        tasksPanel.addComponent(tasksBox);
        rightPanel.addComponent(tasksPanel.withBorder(Borders.singleLine("Tasks")),
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.None));

        Panel detailsPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        statusLabel = new Label("Status: -");
        priorityLabel = new Label("Priority: -");
        createdLabel = new Label("Created: -");
        notesLabel = new Label("(none)");
        detailsPanel.addComponent(statusLabel);
        detailsPanel.addComponent(priorityLabel);
        detailsPanel.addComponent(createdLabel);
        detailsPanel.addComponent(new Label("\nNotes:"));
        detailsPanel.addComponent(notesLabel);
        detailsPanel.setPreferredSize(new TerminalSize(38, 7));
        rightPanel.addComponent(detailsPanel.withBorder(Borders.singleLine("Details")),
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        mainPanel.addComponent(rightPanel,
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        updateTasksList = () -> {
            int selectedIndex = listsBox.getSelectedIndex();
            tasksBox.clearItems();

            if (selectedIndex < 0 || selectedIndex >= allLists.size()) {
                refreshDetailsPanel();
                return;
            }

            TaskList currentList = allLists.get(selectedIndex);
            List<Task> tasks = currentList.getTasks();

            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                String statusIcon = task.isDone() ? "[x] " : "[ ] ";
                final int taskIndexForThisItem = i;
                tasksBox.addItem(statusIcon + task.getTitle(),
                        () -> toggleTask(currentList, selectedIndex, taskIndexForThisItem));
            }

            refreshDetailsPanel();
        };

        refreshListsView();
        if (!allLists.isEmpty()) {
            int idx = Math.min(pendingListIndex, allLists.size() - 1);
            listsBox.setSelectedIndex(idx);
            refreshListsView();
            updateTasksList.run();
            int taskCount = allLists.get(idx).getTasks().size();
            if (taskCount > 0) {
                tasksBox.setSelectedIndex(Math.min(pendingTaskIndex, taskCount - 1));
                refreshDetailsPanel();
            }
        }

        window.setComponent(mainPanel);
        window.setHints(Collections.singletonList(Window.Hint.FULL_SCREEN));

        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                KeyAction action = keyBindings.resolve(keyStroke);
                if (action == null) return;
                hasBeenHandled.set(true);
                dispatchAction(action);
            }
        });

        gui.addWindowAndWait(window);
        screen.stopScreen();
    }

    private void relaunchWithNewFont(AppFont font) {
        if (!font.isAvailable()) {
            MessageDialog.showMessageDialog(gui, "Error", "Font \"" + font.getDisplayName() + "\" is not available on this system.");
            return;
        }

        pendingListIndex = Math.max(0, listsBox.getSelectedIndex());
        pendingTaskIndex = Math.max(0, tasksBox.getSelectedIndex());

        fontManager.setFont(font);
        restartPending = true;
        window.close();
    }

    private String buildTopBar(String left, String center, String right, int windowWidth) {
        int usable = windowWidth - 2;

        char[] bar = new char[usable];
        java.util.Arrays.fill(bar, '─');

        String leftSeg = " " + left + " ";
        String centerSeg = " " + center + " ";
        String rightSeg = " " + right + " ";

        placeSegment(bar, leftSeg, 0);
        placeSegment(bar, centerSeg, (usable - centerSeg.length()) / 2 - 2);
        placeSegment(bar, rightSeg, usable - rightSeg.length() - 6);

        return new String(bar);
    }

    private void placeSegment(char[] bar, String segment, int start) {
        for (int i = 0; i < segment.length(); i++) {
            int pos = start + i;
            if (pos >= 0 && pos < bar.length) {
                bar[pos] = segment.charAt(i);
            }
        }
    }

    private void dispatchAction(KeyAction action) {
        switch (action) {
            case QUIT -> window.close();
            case FOCUS_LISTS -> listsBox.takeFocus();
            case FOCUS_TASKS -> tasksBox.takeFocus();
            case NEW_LIST -> createNewList();
            case NEW_TASK -> createNewTask();
            case DELETE_ITEM -> handleDelete();
            case EDIT_ITEM -> handleEdit();
            case THEME_SELECT -> openThemeDialog();
            case FONT_SELECT -> openFontDialog();
            case HELP -> showHelpDialog();
            case REBIND -> openRebindDialog();
        }
    }

    private void toggleTask(TaskList currentList, int listIndex, int taskIndex) {
        List<Task> tasks = currentList.getTasks();
        if (taskIndex < 0 || taskIndex >= tasks.size()) return;

        tasks.get(taskIndex).toggleStatus();
        storageService.saveList(currentList);

        refreshListsView();
        listsBox.setSelectedIndex(listIndex);
        updateTasksList.run();
        tasksBox.setSelectedIndex(taskIndex);
    }

    private String formatListItem(TaskList list, boolean selected) {
        String marker = selected ? "| " : "  ";
        String name = list.getName();
        String progress = list.getProgressString();
        int totalWidth = 31;
        int spaces = Math.max(1, totalWidth - name.length() - progress.length());
        return marker + name + " ".repeat(spaces) + progress;
    }

    private void refreshListsView() {
        int selectedIndex = listsBox.getSelectedIndex();

        listsBox.clearItems();
        for (int i = 0; i < allLists.size(); i++) {
            TaskList list = allLists.get(i);
            boolean isSelected = (i == selectedIndex);
            listsBox.addItem(formatListItem(list, isSelected), updateTasksList::run);
        }

        if (selectedIndex >= 0 && selectedIndex < allLists.size()) {
            listsBox.setSelectedIndex(selectedIndex);
        }
    }

    private void refreshDetailsPanel() {
        int selectedListIdx = listsBox.getSelectedIndex();
        if (selectedListIdx < 0 || selectedListIdx >= allLists.size()) {
            statusLabel.setText("Status: -");
            priorityLabel.setText("Priority: -");
            createdLabel.setText("Created: -");
            notesLabel.setText("(none)");
            return;
        }

        List<Task> tasks = allLists.get(selectedListIdx).getTasks();
        int selectedTaskIdx = tasksBox.getSelectedIndex();

        if (selectedTaskIdx >= 0 && selectedTaskIdx < tasks.size()) {
            Task currentTask = tasks.get(selectedTaskIdx);
            statusLabel.setText("Status: " + (currentTask.isDone() ? "done" : "todo"));
            priorityLabel.setText("Priority: " +
                    (currentTask.getPriority() != null ? currentTask.getPriority().toString().toLowerCase() : "none"));
            createdLabel.setText("Created: " + formatCreatedAt(currentTask.getCreatedAt()));
            notesLabel.setText(wrapText(currentTask.getNotes(), 34));
        } else {
            statusLabel.setText("Status: -");
            priorityLabel.setText("Priority: -");
            createdLabel.setText("Created: -");
            notesLabel.setText("(none)");
        }
    }

    private void createNewList() {
        String name = new TextInputDialogBuilder()
                .setTitle("New List")
                .setDescription("Enter a name for the list:")
                .build()
                .showDialog(gui);

        if (name == null || name.isBlank()) return;

        TaskList newList = new TaskList(name.trim());
        storageService.saveList(newList);
        allLists.add(newList);

        refreshListsView();
        listsBox.setSelectedIndex(allLists.size() - 1);
        updateTasksList.run();
        listsBox.takeFocus();
    }

    private void createNewTask() {
        int listIdx = listsBox.getSelectedIndex();
        if (listIdx < 0 || listIdx >= allLists.size()) {
            MessageDialog.showMessageDialog(gui, "There is no list", "First, select or create a list (Ctrl+N).");
            return;
        }
        TaskList currentList = allLists.get(listIdx);

        String title = new TextInputDialogBuilder()
                .setTitle("New Task")
                .setDescription("Enter a name for the task:")
                .build()
                .showDialog(gui);
        if (title == null || title.isBlank()) return;

        Priority priority = askPriority(null);
        if (priority == null) return;

        Task newTask = new Task(title.trim(), priority, null);
        currentList.getTasks().add(newTask);
        storageService.saveList(currentList);

        refreshListsView();
        listsBox.setSelectedIndex(listIdx);
        updateTasksList.run();
        tasksBox.setSelectedIndex(currentList.getTasks().size() - 1);
        tasksBox.takeFocus();
    }

    private void handleDelete() {
        Interactable focused = gui.getFocusedInteractable();

        if (focused == listsBox) {
            deleteSelectedList();
        } else if (focused == tasksBox) {
            deleteSelectedTask();
        } else {
            MessageDialog.showMessageDialog(gui, "Deletion",
                    "Highlight “Lists” or “Tasks” (using the l / k keys) to delete an item.");
        }
    }

    private void deleteSelectedList() {
        int idx = listsBox.getSelectedIndex();
        if (idx < 0 || idx >= allLists.size()) return;
        TaskList list = allLists.get(idx);

        MessageDialogButton answer = MessageDialog.showMessageDialog(gui, "Delete the list?",
                "List \"" + list.getName() + "\" and all of its tasks will be permanently deleted.",
                MessageDialogButton.Yes, MessageDialogButton.No);

        if (answer != MessageDialogButton.Yes) return;

        storageService.deleteList(list);
        allLists.remove(idx);

        refreshListsView();
        if (!allLists.isEmpty()) {
            listsBox.setSelectedIndex(Math.min(idx, allLists.size() - 1));
        }
        updateTasksList.run();
    }

    private void deleteSelectedTask() {
        int listIdx = listsBox.getSelectedIndex();
        int taskIdx = tasksBox.getSelectedIndex();
        if (listIdx < 0 || listIdx >= allLists.size()) return;
        TaskList currentList = allLists.get(listIdx);
        if (taskIdx < 0 || taskIdx >= currentList.getTasks().size()) return;

        Task task = currentList.getTasks().get(taskIdx);
        MessageDialogButton answer = MessageDialog.showMessageDialog(gui, "Delete the task?",
                "Task \"" + task.getTitle() + "\" will be permanently deleted.",
                MessageDialogButton.Yes, MessageDialogButton.No);

        if (answer != MessageDialogButton.Yes) return;

        currentList.getTasks().remove(taskIdx);
        storageService.saveList(currentList);

        refreshListsView();
        listsBox.setSelectedIndex(listIdx);
        updateTasksList.run();
        if (!currentList.getTasks().isEmpty()) {
            tasksBox.setSelectedIndex(Math.min(taskIdx, currentList.getTasks().size() - 1));
        }
    }

    private void handleEdit() {
        Interactable focused = gui.getFocusedInteractable();

        if (focused == listsBox) {
            editSelectedList();
        } else if (focused == tasksBox) {
            editSelectedTask();
        } else {
            MessageDialog.showMessageDialog(gui, "Editing",
                    "Highlight “Lists” or “Tasks” (using the l / k keys) to edit an item.");
        }
    }

    private void editSelectedList() {
        int idx = listsBox.getSelectedIndex();
        if (idx < 0 || idx >= allLists.size()) return;
        TaskList list = allLists.get(idx);

        String newName = new TextInputDialogBuilder()
                .setTitle("Rename the list")
                .setDescription("New name:")
                .setInitialContent(list.getName())
                .build()
                .showDialog(gui);

        if (newName == null || newName.isBlank()) return;

        list.setName(newName.trim());
        storageService.saveList(list);

        refreshListsView();
        listsBox.setSelectedIndex(idx);
    }

    private void editSelectedTask() {
        int listIdx = listsBox.getSelectedIndex();
        int taskIdx = tasksBox.getSelectedIndex();
        if (listIdx < 0 || listIdx >= allLists.size()) return;
        TaskList currentList = allLists.get(listIdx);
        if (taskIdx < 0 || taskIdx >= currentList.getTasks().size()) return;
        Task task = currentList.getTasks().get(taskIdx);

        String newTitle = new TextInputDialogBuilder()
                .setTitle("Edit Task")
                .setDescription("Title:")
                .setInitialContent(task.getTitle())
                .build()
                .showDialog(gui);
        if (newTitle == null || newTitle.isBlank()) return;
        task.setTitle(newTitle.trim());

        Priority newPriority = askPriority(task.getPriority());
        if (newPriority != null) {
            task.setPriority(newPriority);
        }

        String newNotes = new TextInputDialogBuilder()
                .setTitle("Notes")
                .setDescription("Note text (can be left blank):")
                .setInitialContent(task.getNotes() != null ? task.getNotes() : "")
                .build()
                .showDialog(gui);
        if (newNotes != null) {
            task.setNotes(newNotes.trim());
        }

        storageService.saveList(currentList);
        refreshListsView();
        listsBox.setSelectedIndex(listIdx);
        updateTasksList.run();
        tasksBox.setSelectedIndex(taskIdx);
    }

    private Priority askPriority(Priority current) {
        Priority[] result = new Priority[1];

        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Task Priority")
                .setDescription(current != null ? "Current: " + current + ". Select a new one:" : "Select a priority:")
                .setCanCancel(true);

        for (Priority p : Priority.values()) {
            builder.addAction(p.name(), () -> result[0] = p);
        }

        builder.build().showDialog(gui);
        return result[0];
    }

    private static final java.time.format.DateTimeFormatter CREATED_AT_FORMAT =
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private String formatCreatedAt(java.time.LocalDateTime createdAt) {
        return createdAt != null ? createdAt.format(CREATED_AT_FORMAT) : "unknown";
    }

    private String wrapText(String text, int width) {
        if (text == null || text.isBlank()) return "(none)";

        StringBuilder result = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (String word : text.trim().split("\\s+")) {
            if (line.length() + word.length() + 1 > width) {
                result.append(line.toString().trim()).append("\n");
                line.setLength(0);
            }
            line.append(word).append(" ");
        }
        if (!line.isEmpty()) {
            result.append(line.toString().trim());
        }
        return result.toString();
    }

    private void openThemeDialog() {
        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Choosing a theme")
                .setDescription("Current: " + themeManager.getCurrentTheme().getDisplayName())
                .setCanCancel(true);

        for (AppTheme theme : AppTheme.values()) {
            builder.addAction(theme.getDisplayName(), () -> themeManager.setTheme(theme, gui));
        }

        builder.build().showDialog(gui);
    }

    private void openFontDialog() {
        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Select a font")
                .setDescription("Current: " + fontManager.getCurrentFont().getDisplayName()
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
            MessageDialog.showMessageDialog(gui, "Fonts", "None of the configured fonts were found on the system.");
            return;
        }

        builder.build().showDialog(gui);
    }

    private void showHelpDialog() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<KeyAction, KeyBinding> entry : keyBindings.getAllBindings().entrySet()) {
            sb.append(entry.getValue().serialize())
                    .append("  —  ")
                    .append(entry.getKey().getDescription())
                    .append("\n");
        }
        MessageDialog.showMessageDialog(gui, "Keyboard Shortcuts", sb.toString());
    }

    private void openRebindDialog() {
        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Rework the key")
                .setDescription("Select an action:");

        for (KeyAction action : KeyAction.values()) {
            KeyBinding current = keyBindings.getBinding(action);
            String label = "[" + (current != null ? current.serialize() : "-") + "]  " + action.getDescription();
            builder.addAction(label, () -> captureNewKeyFor(action));
        }
        builder.build().showDialog(gui);
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
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    captureWindow.close();
                    return;
                }
                keyBindings.setBinding(action, KeyBinding.fromKeyStroke(keyStroke));
                captureWindow.close();
            }
        });

        gui.addWindowAndWait(captureWindow);
    }
}