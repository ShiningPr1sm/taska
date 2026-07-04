package dev.shiningpr1sm.taska.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.shiningpr1sm.taska.model.TaskList;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class StorageService {

    private final Path storageDir = Paths.get(System.getProperty("user.home"), ".taska");
    private final ObjectMapper objectMapper;

    public StorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a folder to store the data: " + storageDir, e);
        }
    }

    private Path fileFor(TaskList taskList) {
        return storageDir.resolve(taskList.getId() + ".json");
    }

    public void saveList(TaskList taskList) {
        if (taskList.getId() == null) {
            taskList.setId(UUID.randomUUID().toString());
        }

        Path targetFile = fileFor(taskList);
        Path tempFile = storageDir.resolve(taskList.getId() + ".json.tmp");

        try {
            objectMapper.writeValue(tempFile.toFile(), taskList);
            Files.move(tempFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            throw new RuntimeException("Error saving the list: " + taskList.getName(), e);
        }
    }

    public void deleteList(TaskList taskList) {
        if (taskList.getId() == null)
            return;
        try {
            Files.deleteIfExists(fileFor(taskList));
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting a list: " + taskList.getName(), e);
        }
    }

    public List<TaskList> loadAllLists() {
        List<TaskList> lists = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(storageDir, 1)) {
            List<Path> jsonFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .toList();
            for (Path path : jsonFiles) {
                try {
                    TaskList list = objectMapper.readValue(path.toFile(), TaskList.class);

                    if (list.getId() == null) {
                        list.setId(UUID.randomUUID().toString());
                        saveList(list);
                        Files.deleteIfExists(path);
                    }
                    lists.add(list);
                } catch (IOException e) {
                    System.err.println("Error reading the file " + path.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while scanning the storage directory", e);
        }

        return lists;
    }
}