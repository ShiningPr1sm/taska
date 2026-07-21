package dev.shiningpr1sm.taska.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskListTest {

    @Test
    void constructorSetsNameAndId() {
        TaskList list = new TaskList("My List");
        assertEquals("My List", list.getName());
        assertNotNull(list.getId());
        assertFalse(list.getId().isEmpty());
    }

    @Test
    void noArgConstructorHasEmptyTasks() {
        TaskList list = new TaskList();
        assertNotNull(list.getTasks());
        assertTrue(list.getTasks().isEmpty());
    }

    @Test
    void uniqueIds() {
        TaskList a = new TaskList("A");
        TaskList b = new TaskList("B");
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void progressStringEmpty() {
        TaskList list = new TaskList("Empty");
        assertEquals("0/0", list.getProgressString());
    }

    @Test
    void progressStringAllTodo() {
        TaskList list = new TaskList("List");
        list.getTasks().add(new Task("T1", Priority.LOW, null));
        list.getTasks().add(new Task("T2", Priority.HIGH, null));
        assertEquals("0/2", list.getProgressString());
    }

    @Test
    void progressStringAllDone() {
        TaskList list = new TaskList("List");
        Task t1 = new Task("T1", Priority.LOW, null);
        Task t2 = new Task("T2", Priority.HIGH, null);
        t1.toggleStatus();
        t2.toggleStatus();
        list.getTasks().add(t1);
        list.getTasks().add(t2);
        assertEquals("2/2", list.getProgressString());
    }

    @Test
    void progressStringMixed() {
        TaskList list = new TaskList("List");
        Task t1 = new Task("T1", Priority.LOW, null);
        Task t2 = new Task("T2", Priority.HIGH, null);
        Task t3 = new Task("T3", Priority.MEDIUM, null);
        t2.toggleStatus();
        list.getTasks().add(t1);
        list.getTasks().add(t2);
        list.getTasks().add(t3);
        assertEquals("1/3", list.getProgressString());
    }

    @Test
    void setNameWorks() {
        TaskList list = new TaskList("Old");
        list.setName("New");
        assertEquals("New", list.getName());
    }

    @Test
    void setIdWorks() {
        TaskList list = new TaskList("List");
        list.setId("custom-id");
        assertEquals("custom-id", list.getId());
    }
}
