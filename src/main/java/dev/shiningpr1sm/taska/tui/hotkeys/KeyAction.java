package dev.shiningpr1sm.taska.tui.hotkeys;

public enum KeyAction {
    QUIT("Exit the app"),
    FOCUS_LISTS("Move the focus to the Lists panel"),
    FOCUS_TASKS("Move the focus to the Tasks panel"),
    NEW_LIST("Create a new list"),
    NEW_TASK("Create a new task in the current list"),
    DELETE_ITEM("Delete the selected list/task (depending on the focus)"),
    EDIT_ITEM("Edit the selected list/task (depending on the focus)"),
    THEME_SELECT("Select a design theme"),
    FONT_SELECT("Select a font"),
    HELP("Show a list of current keyboard shortcuts"),
    REBIND("Open the Key Remapping Menu");

    private final String description;

    KeyAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}