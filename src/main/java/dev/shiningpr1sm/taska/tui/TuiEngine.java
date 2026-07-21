package dev.shiningpr1sm.taska.tui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;
import dev.shiningpr1sm.taska.UiConstants;
import dev.shiningpr1sm.taska.model.Priority;
import dev.shiningpr1sm.taska.model.Task;
import dev.shiningpr1sm.taska.model.TaskList;
import dev.shiningpr1sm.taska.service.StorageService;
import dev.shiningpr1sm.taska.tui.controller.ListController;
import dev.shiningpr1sm.taska.tui.controller.SettingsController;
import dev.shiningpr1sm.taska.tui.controller.TaskController;
import dev.shiningpr1sm.taska.tui.controller.UpdateController;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyAction;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyBindingsManager;
import dev.shiningpr1sm.taska.tui.theme.FontManager;
import dev.shiningpr1sm.taska.tui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TuiEngine {
    private final StorageService storageService = new StorageService();
    private final KeyBindingsManager keyBindings = new KeyBindingsManager();
    private final ThemeManager themeManager = new ThemeManager();
    private final FontManager fontManager = new FontManager();
    private final UpdateController updateController = new UpdateController();
    private List<TaskList> allLists;

    private WindowBasedTextGUI gui;
    private BasicWindow window;
    private SwingTerminalFrame terminal;
    private Screen screen;

    private ActionListBox listsBox;
    private ActionListBox tasksBox;
    private Label statusLabel;
    private Label priorityLabel;
    private Label notesLabel;
    private Label createdLabel;
    private TextColor[] taskDotColors;

    private boolean restartPending = false;
    private int pendingListIndex = 0;
    private int pendingTaskIndex = 0;

    private ListController listController;
    private TaskController taskController;
    private SettingsController settingsController;

    public void start() {
        allLists = storageService.loadAllLists();
        if (allLists.isEmpty()) {
            TaskList defaultList = new TaskList("Inbox");
            storageService.saveList(defaultList);
            allLists.add(defaultList);
        }

        updateController.checkForUpdatesBeforeLaunch();
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

    private void launchWindow() throws IOException {
        terminal = new SwingTerminalFrame(
                UiConstants.APP_TITLE,
                new TerminalSize(UiConstants.TERMINAL_WIDTH, UiConstants.TERMINAL_HEIGHT),
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

        String topBar = buildTopBar("h - help", UiConstants.APP_TITLE,
                "v:" + VersionInfo.getVersion(), UiConstants.TOP_BAR_WIDTH);
        window = new BasicWindow(topBar);

        Panel mainPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        listsBox = new ActionListBox(new TerminalSize(UiConstants.LISTS_BOX_WIDTH, UiConstants.LISTS_BOX_HEIGHT)) {
            @Override
            public synchronized Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
                KeyAction action = keyBindings.resolve(keyStroke);
                if (action != null) {
                    dispatchAction(action);
                    return Interactable.Result.HANDLED;
                }
                Interactable.Result result = super.handleKeyStroke(keyStroke);
                refreshListsView();
                refreshTasksView();
                return result;
            }
        };
        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        leftPanel.addComponent(listsBox);
        mainPanel.addComponent(leftPanel.withBorder(Borders.singleLine("Lists")),
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        tasksBox = new ActionListBox(new TerminalSize(UiConstants.TASKS_BOX_WIDTH, UiConstants.TASKS_BOX_HEIGHT)) {
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
        tasksBox.setListItemRenderer(new PriorityDotRenderer());

        Panel tasksPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        tasksPanel.addComponent(tasksBox);

        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));
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
        detailsPanel.setPreferredSize(new TerminalSize(UiConstants.DETAILS_PANEL_WIDTH, UiConstants.DETAILS_PANEL_HEIGHT));
        rightPanel.addComponent(detailsPanel.withBorder(Borders.singleLine("Details")),
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        mainPanel.addComponent(rightPanel,
                LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow));

        TuiContext ctx = buildContext();
        listController = new ListController(ctx);
        taskController = new TaskController(ctx);
        settingsController = new SettingsController(ctx);

        refreshListsView();
        if (!allLists.isEmpty()) {
            int idx = Math.min(pendingListIndex, allLists.size() - 1);
            listsBox.setSelectedIndex(idx);
            refreshListsView();
            refreshTasksView();
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

    private TuiContext buildContext() {
        Runnable refreshListsView = this::refreshListsView;
        Runnable refreshTasksView = this::refreshTasksView;
        Runnable refreshDetailsPanel = this::refreshDetailsPanel;
        return new TuiContext(
                storageService, keyBindings, themeManager, fontManager,
                allLists, gui, window, listsBox, tasksBox,
                statusLabel, priorityLabel, createdLabel, notesLabel,
                taskDotColors,
                refreshListsView, refreshTasksView, refreshDetailsPanel,
                (listIdx, taskIdx) -> taskController.toggleTask(listIdx, taskIdx),
                new boolean[]{restartPending},
                new int[]{pendingListIndex, pendingTaskIndex}
        );
    }

    private void dispatchAction(KeyAction action) {
        switch (action) {
            case QUIT -> window.close();
            case FOCUS_LISTS -> listsBox.takeFocus();
            case FOCUS_TASKS -> tasksBox.takeFocus();
            case NEW_LIST -> listController.createNewList();
            case NEW_TASK -> taskController.createNewTask();
            case DELETE_ITEM -> handleDelete();
            case EDIT_ITEM -> handleEdit();
            case THEME_SELECT -> settingsController.openThemeDialog();
            case FONT_SELECT -> settingsController.openFontDialog();
            case HELP -> settingsController.showHelpDialog();
            case REBIND -> settingsController.openRebindDialog();
        }
    }

    private void handleDelete() {
        Interactable focused = gui.getFocusedInteractable();
        if (focused == listsBox) {
            listController.deleteSelectedList();
        } else if (focused == tasksBox) {
            taskController.deleteSelectedTask();
        } else {
            com.googlecode.lanterna.gui2.dialogs.MessageDialog.showMessageDialog(gui, "Deletion",
                    "Highlight \u201cLists\u201d or \u201cTasks\u201d (using the l / k keys) to delete an item.");
        }
    }

    private void handleEdit() {
        Interactable focused = gui.getFocusedInteractable();
        if (focused == listsBox) {
            listController.editSelectedList();
        } else if (focused == tasksBox) {
            taskController.editSelectedTask();
        } else {
            com.googlecode.lanterna.gui2.dialogs.MessageDialog.showMessageDialog(gui, "Editing",
                    "Highlight \u201cLists\u201d or \u201cTasks\u201d (using the l / k keys) to edit an item.");
        }
    }

    private void refreshListsView() {
        int selectedIndex = listsBox.getSelectedIndex();
        listsBox.clearItems();
        for (int i = 0; i < allLists.size(); i++) {
            TaskList list = allLists.get(i);
            boolean isSelected = (i == selectedIndex);
            listsBox.addItem(listController.formatListItem(list, isSelected), this::refreshTasksView);
        }
        if (selectedIndex >= 0 && selectedIndex < allLists.size()) {
            listsBox.setSelectedIndex(selectedIndex);
        }
    }

    private void refreshTasksView() {
        int selectedIndex = listsBox.getSelectedIndex();
        tasksBox.clearItems();

        if (selectedIndex < 0 || selectedIndex >= allLists.size()) {
            taskDotColors = new TextColor[0];
            refreshDetailsPanel();
            return;
        }

        TaskList currentList = allLists.get(selectedIndex);
        List<Task> tasks = currentList.getTasks();
        taskDotColors = new TextColor[tasks.size()];

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            String statusIcon = task.isDone() ? "[x] " : "[ ] ";
            taskDotColors[i] = dotColorFor(task.getPriority());
            final int taskIndex = i;
            final int listIndex = selectedIndex;
            tasksBox.addItem("\u2588 " + statusIcon + task.getTitle(),
                    () -> taskController.toggleTask(listIndex, taskIndex));
        }
        refreshDetailsPanel();
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
            createdLabel.setText("Created: " + taskController.formatCreatedAt(currentTask.getCreatedAt()));
            notesLabel.setText(taskController.wrapText(currentTask.getNotes(), UiConstants.NOTES_WRAP_WIDTH));
        } else {
            statusLabel.setText("Status: -");
            priorityLabel.setText("Priority: -");
            createdLabel.setText("Created: -");
            notesLabel.setText("(none)");
        }
    }

    private TextColor dotColorFor(Priority priority) {
        if (priority == null) return TextColor.ANSI.WHITE;
        return switch (priority) {
            case LOW -> new TextColor.RGB(0x1B, 0x9F, 0x20);
            case MEDIUM -> new TextColor.RGB(0xDA, 0x94, 0x17);
            case HIGH -> new TextColor.RGB(0xE0, 0x11, 0x11);
        };
    }

    private List<Image> loadAppIcons() {
        List<Image> icons = new ArrayList<>();
        java.net.URL url = getClass().getResource("/project_icon.png");
        if (url != null) {
            Image base = new ImageIcon(url).getImage();
            int[] sizes = {16, 24, 32, 48, 64, 128, 256};
            for (int size : sizes) {
                icons.add(base.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
        }
        return icons;
    }

    private String buildTopBar(String left, String center, String right, int windowWidth) {
        int usable = windowWidth - 2;
        char[] bar = new char[usable];
        java.util.Arrays.fill(bar, '\u2500');

        String leftSeg = " " + left + " ";
        String centerSeg = " " + center + " ";
        String rightSeg = " " + right + " ";

        placeSegment(bar, leftSeg, 0);
        placeSegment(bar, centerSeg, (usable - centerSeg.length()) / 2 - 2);
        placeSegment(bar, rightSeg, usable - rightSeg.length() - 4);

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

    private class PriorityDotRenderer extends AbstractListBox.ListItemRenderer<Runnable, ActionListBox> {

        private static final String DOT = "\u2588 ";

        @Override
        public void drawItem(TextGUIGraphics graphics, ActionListBox listBox, int index,
                             Runnable item, boolean selected, boolean focused) {
            ThemeDefinition themeDefinition = listBox.getThemeDefinition();
            if (selected && focused) {
                graphics.applyThemeStyle(themeDefinition.getSelected());
            } else {
                graphics.applyThemeStyle(themeDefinition.getNormal());
            }
            graphics.fill(' ');

            String label = item != null ? item.toString() : "";
            if (!label.startsWith(DOT)) {
                graphics.putString(0, 0, label);
                return;
            }

            TextColor dotColor = (taskDotColors != null && index < taskDotColors.length)
                    ? taskDotColors[index]
                    : TextColor.ANSI.WHITE;
            TextColor originalForeground = graphics.getForegroundColor();

            graphics.setForegroundColor(dotColor);
            graphics.putString(0, 0, DOT);

            graphics.setForegroundColor(originalForeground);
            graphics.putString(DOT.length(), 0, label.substring(DOT.length()));
        }
    }
}
