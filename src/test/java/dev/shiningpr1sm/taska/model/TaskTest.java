package dev.shiningpr1sm.taska.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void constructorSetsDefaults() {
        Task task = new Task("Test", Priority.HIGH, LocalDate.now());
        assertEquals("Test", task.getTitle());
        assertEquals(Priority.HIGH, task.getPriority());
        assertFalse(task.isDone());
        assertNotNull(task.getTags());
        assertTrue(task.getTags().isEmpty());
        assertEquals("", task.getNotes());
        assertNotNull(task.getCreatedAt());
    }

    @Test
    void noArgConstructorDefaults() {
        Task task = new Task();
        assertNull(task.getTitle());
        assertNull(task.getPriority());
        assertFalse(task.isDone());
        assertNotNull(task.getTags());
        assertEquals("", task.getNotes());
    }

    @Test
    void toggleStatusMarksDone() {
        Task task = new Task("T", Priority.LOW, null);
        assertFalse(task.isDone());
        assertNull(task.getCompletedAt());

        task.toggleStatus();
        assertTrue(task.isDone());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void toggleStatusTwiceResets() {
        Task task = new Task("T", Priority.LOW, null);
        task.toggleStatus();
        task.toggleStatus();

        assertFalse(task.isDone());
        assertNull(task.getCompletedAt());
    }

    @Test
    void setDoneDirectlyDoesNotSetCompletedAt() {
        Task task = new Task("T", Priority.LOW, null);
        task.setDone(true);

        assertTrue(task.isDone());
        assertNull(task.getCompletedAt());
    }

    @Test
    void settersWork() {
        Task task = new Task();
        task.setTitle("New Title");
        assertEquals("New Title", task.getTitle());

        task.setPriority(Priority.MEDIUM);
        assertEquals(Priority.MEDIUM, task.getPriority());

        task.setNotes("some notes");
        assertEquals("some notes", task.getNotes());

        LocalDate date = LocalDate.of(2025, 6, 15);
        task.setDueDate(date);
        assertEquals(date, task.getDueDate());

        LocalDateTime dt = LocalDateTime.of(2025, 6, 15, 10, 30);
        task.setCreatedAt(dt);
        assertEquals(dt, task.getCreatedAt());

        task.setCompletedAt(dt);
        assertEquals(dt, task.getCompletedAt());
    }
}
