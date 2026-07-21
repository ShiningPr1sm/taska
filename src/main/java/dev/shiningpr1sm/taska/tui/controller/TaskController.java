package dev.shiningpr1sm.taska.tui.controller;

import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import dev.shiningpr1sm.taska.UiConstants;
import dev.shiningpr1sm.taska.model.Priority;
import dev.shiningpr1sm.taska.model.Task;
import dev.shiningpr1sm.taska.model.TaskList;
import dev.shiningpr1sm.taska.tui.TuiContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskController {

    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TuiContext ctx;

    public TaskController(TuiContext ctx) {
        this.ctx = ctx;
    }

    public void toggleTask(int listIndex, int taskIndex) {
        List<Task> tasks = ctx.allLists().get(listIndex).getTasks();
        if (taskIndex < 0 || taskIndex >= tasks.size()) return;

        tasks.get(taskIndex).toggleStatus();
        ctx.storageService().saveList(ctx.allLists().get(listIndex));

        ctx.refreshListsView().run();
        ctx.listsBox().setSelectedIndex(listIndex);
        ctx.refreshTasksView().run();
        ctx.tasksBox().setSelectedIndex(taskIndex);
    }

    public void createNewTask() {
        int listIdx = ctx.listsBox().getSelectedIndex();
        if (listIdx < 0 || listIdx >= ctx.allLists().size()) {
            MessageDialog.showMessageDialog(ctx.gui(), "There is no list",
                    "First, select or create a list (Ctrl+N).");
            return;
        }
        TaskList currentList = ctx.allLists().get(listIdx);

        String title = new TextInputDialogBuilder()
                .setTitle("New Task")
                .setDescription("Enter a name for the task:")
                .build()
                .showDialog(ctx.gui());
        if (title == null || title.isBlank()) return;

        Priority priority = askPriority(null);
        if (priority == null) return;

        Task newTask = new Task(title.trim(), priority, null);
        currentList.getTasks().add(newTask);
        ctx.storageService().saveList(currentList);

        ctx.refreshListsView().run();
        ctx.listsBox().setSelectedIndex(listIdx);
        ctx.refreshTasksView().run();
        ctx.tasksBox().setSelectedIndex(currentList.getTasks().size() - 1);
        ctx.tasksBox().takeFocus();
    }

    public void deleteSelectedTask() {
        int listIdx = ctx.listsBox().getSelectedIndex();
        int taskIdx = ctx.tasksBox().getSelectedIndex();
        if (listIdx < 0 || listIdx >= ctx.allLists().size()) return;
        TaskList currentList = ctx.allLists().get(listIdx);
        if (taskIdx < 0 || taskIdx >= currentList.getTasks().size()) return;

        Task task = currentList.getTasks().get(taskIdx);
        MessageDialogButton answer = MessageDialog.showMessageDialog(ctx.gui(), "Delete the task?",
                "Task \"" + task.getTitle() + "\" will be permanently deleted.",
                MessageDialogButton.Yes, MessageDialogButton.No);

        if (answer != MessageDialogButton.Yes) return;

        currentList.getTasks().remove(taskIdx);
        ctx.storageService().saveList(currentList);

        ctx.refreshListsView().run();
        ctx.listsBox().setSelectedIndex(listIdx);
        ctx.refreshTasksView().run();
        if (!currentList.getTasks().isEmpty()) {
            ctx.tasksBox().setSelectedIndex(Math.min(taskIdx, currentList.getTasks().size() - 1));
        }
    }

    public void editSelectedTask() {
        int listIdx = ctx.listsBox().getSelectedIndex();
        int taskIdx = ctx.tasksBox().getSelectedIndex();
        if (listIdx < 0 || listIdx >= ctx.allLists().size()) return;
        TaskList currentList = ctx.allLists().get(listIdx);
        if (taskIdx < 0 || taskIdx >= currentList.getTasks().size()) return;
        Task task = currentList.getTasks().get(taskIdx);

        String newTitle = new TextInputDialogBuilder()
                .setTitle("Edit Task")
                .setDescription("Title:")
                .setInitialContent(task.getTitle())
                .build()
                .showDialog(ctx.gui());
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
                .showDialog(ctx.gui());
        if (newNotes != null) {
            task.setNotes(newNotes.trim());
        }

        ctx.storageService().saveList(currentList);
        ctx.refreshListsView().run();
        ctx.listsBox().setSelectedIndex(listIdx);
        ctx.refreshTasksView().run();
        ctx.tasksBox().setSelectedIndex(taskIdx);
    }

    public String formatCreatedAt(java.time.LocalDateTime createdAt) {
        return createdAt != null ? createdAt.format(CREATED_AT_FORMAT) : "unknown";
    }

    public String wrapText(String text, int width) {
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

    public Priority askPriority(Priority current) {
        Priority[] result = new Priority[1];

        ActionListDialogBuilder builder = new ActionListDialogBuilder()
                .setTitle("Task Priority")
                .setDescription(current != null ? "Current: " + current + ". Select a new one:" : "Select a priority:")
                .setCanCancel(true);

        for (Priority p : Priority.values()) {
            builder.addAction(p.name(), () -> result[0] = p);
        }

        builder.build().showDialog(ctx.gui());
        return result[0];
    }
}
