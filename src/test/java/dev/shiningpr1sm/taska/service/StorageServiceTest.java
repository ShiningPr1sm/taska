package dev.shiningpr1sm.taska.service;

import dev.shiningpr1sm.taska.model.Priority;
import dev.shiningpr1sm.taska.model.Task;
import dev.shiningpr1sm.taska.model.TaskList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(tempDir);
    }

    @Test
    void saveAndLoadList() {
        TaskList list = new TaskList("Test List");
        list.getTasks().add(new Task("Task 1", Priority.HIGH, null));
        list.getTasks().add(new Task("Task 2", Priority.LOW, null));

        storageService.saveList(list);

        List<TaskList> loaded = storageService.loadAllLists();
        assertEquals(1, loaded.size());
        assertEquals("Test List", loaded.get(0).getName());
        assertEquals(2, loaded.get(0).getTasks().size());
    }

    @Test
    void saveMultipleLists() {
        TaskList a = new TaskList("List A");
        TaskList b = new TaskList("List B");

        storageService.saveList(a);
        storageService.saveList(b);

        List<TaskList> loaded = storageService.loadAllLists();
        assertEquals(2, loaded.size());
    }

    @Test
    void deleteList() {
        TaskList list = new TaskList("To Delete");
        storageService.saveList(list);
        assertEquals(1, storageService.loadAllLists().size());

        storageService.deleteList(list);
        assertEquals(0, storageService.loadAllLists().size());
    }

    @Test
    void deleteNonexistentListDoesNotThrow() {
        TaskList list = new TaskList("Never Saved");
        assertDoesNotThrow(() -> storageService.deleteList(list));
    }

    @Test
    void saveOverwritesExistingList() {
        TaskList list = new TaskList("Original");
        storageService.saveList(list);

        list.setName("Updated");
        storageService.saveList(list);

        List<TaskList> loaded = storageService.loadAllLists();
        assertEquals(1, loaded.size());
        assertEquals("Updated", loaded.get(0).getName());
    }

    @Test
    void loadAllListsHandlesCorruptedFile() throws Exception {
        java.nio.file.Files.writeString(tempDir.resolve("corrupted.json"), "{not valid json!!!");

        TaskList valid = new TaskList("Valid");
        storageService.saveList(valid);

        List<TaskList> loaded = storageService.loadAllLists();
        assertEquals(1, loaded.size());
        assertEquals("Valid", loaded.get(0).getName());
    }

    @Test
    void emptyDirectoryReturnsEmptyList() {
        List<TaskList> loaded = storageService.loadAllLists();
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void taskPrioritySurvivesSerialization() {
        TaskList list = new TaskList("Prio Test");
        list.getTasks().add(new Task("T1", Priority.HIGH, null));
        list.getTasks().add(new Task("T2", Priority.LOW, null));

        storageService.saveList(list);
        TaskList loaded = storageService.loadAllLists().get(0);

        assertEquals(Priority.HIGH, loaded.getTasks().get(0).getPriority());
        assertEquals(Priority.LOW, loaded.getTasks().get(1).getPriority());
    }
}
