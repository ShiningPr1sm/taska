package dev.shiningpr1sm.taska.tui.controller;

import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import dev.shiningpr1sm.taska.UiConstants;
import dev.shiningpr1sm.taska.model.TaskList;
import dev.shiningpr1sm.taska.tui.TuiContext;

public class ListController {

    private final TuiContext ctx;

    public ListController(TuiContext ctx) {
        this.ctx = ctx;
    }

    public void createNewList() {
        String name = new TextInputDialogBuilder()
                .setTitle("New List")
                .setDescription("Enter a name for the list:")
                .build()
                .showDialog(ctx.gui());

        if (name == null || name.isBlank()) return;

        TaskList newList = new TaskList(name.trim());
        ctx.storageService().saveList(newList);
        ctx.allLists().add(newList);

        ctx.refreshListsView().run();
        ctx.listsBox().setSelectedIndex(ctx.allLists().size() - 1);
        ctx.refreshTasksView().run();
        ctx.listsBox().takeFocus();
    }

    public void deleteSelectedList() {
        int idx = ctx.listsBox().getSelectedIndex();
        if (idx < 0 || idx >= ctx.allLists().size()) return;
        TaskList list = ctx.allLists().get(idx);

        MessageDialogButton answer = MessageDialog.showMessageDialog(ctx.gui(), "Delete the list?",
                "List \"" + list.getName() + "\" and all of its tasks will be permanently deleted.",
                MessageDialogButton.Yes, MessageDialogButton.No);

        if (answer != MessageDialogButton.Yes) return;

        ctx.storageService().deleteList(list);
        ctx.allLists().remove(idx);

        ctx.refreshListsView().run();
        if (!ctx.allLists().isEmpty()) {
            ctx.listsBox().setSelectedIndex(Math.min(idx, ctx.allLists().size() - 1));
        }
        ctx.refreshTasksView().run();
    }

    public void editSelectedList() {
        int idx = ctx.listsBox().getSelectedIndex();
        if (idx < 0 || idx >= ctx.allLists().size()) return;
        TaskList list = ctx.allLists().get(idx);

        String newName = new TextInputDialogBuilder()
                .setTitle("Rename the list")
                .setDescription("New name:")
                .setInitialContent(list.getName())
                .build()
                .showDialog(ctx.gui());

        if (newName == null || newName.isBlank()) return;

        list.setName(newName.trim());
        ctx.storageService().saveList(list);

        ctx.refreshListsView().run();
        ctx.listsBox().setSelectedIndex(idx);
    }

    public String formatListItem(TaskList list, boolean selected) {
        String marker = selected ? "| " : "  ";
        String name = list.getName();
        String progress = list.getProgressString();
        int totalWidth = UiConstants.LIST_ITEM_FORMAT_WIDTH;
        int spaces = Math.max(1, totalWidth - name.length() - progress.length());
        return marker + name + " ".repeat(spaces) + progress;
    }
}
