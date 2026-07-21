package dev.shiningpr1sm.taska.tui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import dev.shiningpr1sm.taska.model.TaskList;
import dev.shiningpr1sm.taska.service.StorageService;
import dev.shiningpr1sm.taska.tui.hotkeys.KeyBindingsManager;
import dev.shiningpr1sm.taska.tui.theme.FontManager;
import dev.shiningpr1sm.taska.tui.theme.ThemeManager;

import java.util.List;
import java.util.function.BiConsumer;

public record TuiContext(
        StorageService storageService,
        KeyBindingsManager keyBindings,
        ThemeManager themeManager,
        FontManager fontManager,
        List<TaskList> allLists,
        WindowBasedTextGUI gui,
        BasicWindow window,
        ActionListBox listsBox,
        ActionListBox tasksBox,
        com.googlecode.lanterna.gui2.Label statusLabel,
        com.googlecode.lanterna.gui2.Label priorityLabel,
        com.googlecode.lanterna.gui2.Label createdLabel,
        com.googlecode.lanterna.gui2.Label notesLabel,
        TextColor[] taskDotColors,
        Runnable refreshListsView,
        Runnable refreshTasksView,
        Runnable refreshDetailsPanel,
        BiConsumer<Integer, Integer> toggleTask,
        boolean[] restartFlag,
        int[] pendingIndices
) {
    public int pendingListIndex() { return pendingIndices[0]; }
    public int pendingTaskIndex() { return pendingIndices[1]; }
    public void setPendingListIndex(int i) { pendingIndices[0] = i; }
    public void setPendingTaskIndex(int i) { pendingIndices[1] = i; }
    public boolean isRestartPending() { return restartFlag[0]; }
    public void setRestartPending(boolean v) { restartFlag[0] = v; }
}
