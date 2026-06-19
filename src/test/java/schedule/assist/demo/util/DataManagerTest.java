package schedule.assist.demo.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import schedule.assist.demo.model.TaskModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
class DataManagerTest {

    @TempDir
    Path tempDir;

    private Path testFilePath;

    // Helper: set the private recoveredFromCorrupt field via reflection.
    private static void setRecoveredFromCorrupt(boolean value) throws Exception {
        java.lang.reflect.Field f = DataManager.class.getDeclaredField("recoveredFromCorrupt");
        f.setAccessible(true);
        f.set(null, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        testFilePath = tempDir.resolve("my_tasks.json");
        DataManager.setFilePath(testFilePath.toString());
        setRecoveredFromCorrupt(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        java.lang.reflect.Method m = DataManager.class.getDeclaredMethod("resolveDefaultFilePath");
        m.setAccessible(true);
        DataManager.setFilePath((String) m.invoke(null));
    }

    @Test
    void loadTasks_missingFile_returnsEmptyList() {
        // File does not exist yet
        List<TaskModel> result = DataManager.loadTasks();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void loadTasks_emptyFile_returnsEmptyList() throws IOException {
        Files.createFile(testFilePath); // Create empty file
        List<TaskModel> result = DataManager.loadTasks();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void loadTasks_validJson_returnsTasks() throws IOException {
        String validJson = "[{\"titleTask\":\"Test\",\"timeOfTask\":\"12:00\",\"placeofTask\":\"Home\",\"noteOfTask\":\"Note\",\"dayOfWeek\":\"Mon\",\"layoutX\":0.0,\"layoutY\":0.0}]";
        Files.writeString(testFilePath, validJson);

        List<TaskModel> result = DataManager.loadTasks();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getTitleTask());
    }

    @Test
    void loadTasks_corruptJson_returnsEmptyListAndCreatesBackup() throws IOException {
        String corruptJson = "[{\"titleTask\":\"Test\", broken json";
        Files.writeString(testFilePath, corruptJson);

        List<TaskModel> result = DataManager.loadTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Original file should be renamed, so it shouldn't exist anymore
        assertFalse(Files.exists(testFilePath));

        // Find backup file
        File[] backups = tempDir.toFile().listFiles((dir, name) -> name.startsWith("my_tasks.json.bak."));
        assertNotNull(backups);
        assertEquals(1, backups.length);
    }

    @Test
    void loadTasks_corruptJson_createsMultipleBackups() throws IOException {
        String corruptJson = "broken json";
        Files.writeString(testFilePath, corruptJson);

        // Force a backup by loading tasks
        DataManager.loadTasks();

        // Find the first backup
        File[] firstBackups = tempDir.toFile().listFiles((dir, name) -> name.startsWith("my_tasks.json.bak."));
        assertEquals(1, firstBackups.length);

        // Recreate the corrupt file with different content
        Files.writeString(testFilePath, "another broken json");

        // Load tasks again, this should create a second backup
        DataManager.loadTasks();

        File[] allBackups = tempDir.toFile().listFiles((dir, name) -> name.startsWith("my_tasks.json.bak."));
        assertEquals(2, allBackups.length);
    }

    @Test
    void saveTasks_thenLoadTasks_returnsSameTasks() {
        TaskModel task1 = new TaskModel("T1", "10:00", "Home", "Note 1", "Mon", 0.0, 0.0);
        TaskModel task2 = new TaskModel("T2", "11:00", "School", "Note 2", "Tue", 10.0, 20.0);
        List<TaskModel> originalTasks = Arrays.asList(task1, task2);

        DataManager.saveTasks(originalTasks);

        List<TaskModel> loadedTasks = DataManager.loadTasks();

        assertNotNull(loadedTasks);
        assertEquals(2, loadedTasks.size());
        assertEquals("T1", loadedTasks.get(0).getTitleTask());
        assertEquals("T2", loadedTasks.get(1).getTitleTask());
    }

    @Test
    void saveTasks_whenRecoveredFromCorruptAndListIsEmpty_doesNotWriteToFile() throws Exception {
        String corruptJson = "[{\"titleTask\":\"Test\", broken json";
        Files.writeString(testFilePath, corruptJson);

        List<TaskModel> loaded = DataManager.loadTasks();
        assertTrue(loaded.isEmpty());
        assertTrue(DataManager.isRecoveredFromCorrupt());
        assertFalse(Files.exists(testFilePath));

        // Now save an empty list
        DataManager.saveTasks(new ArrayList<>());

        // The file should NOT have been created/written since it is guarded!
        assertFalse(Files.exists(testFilePath));
    }

    @Test
    void saveTasks_whenRecoveredFromCorruptAndListIsNull_doesNotWriteToFile() throws Exception {
        String corruptJson = "[{\"titleTask\":\"Test\", broken json";
        Files.writeString(testFilePath, corruptJson);

        List<TaskModel> loaded = DataManager.loadTasks();
        assertTrue(loaded.isEmpty());
        assertTrue(DataManager.isRecoveredFromCorrupt());

        // Now save null
        DataManager.saveTasks(null);

        // The file should NOT have been created/written
        assertFalse(Files.exists(testFilePath));
    }

    @Test
    void saveTasks_createsParentDirectories() {
        Path deepPath = tempDir.resolve("subDir1").resolve("subDir2").resolve("my_tasks.json");
        DataManager.setFilePath(deepPath.toString());

        TaskModel task = new TaskModel("T1", "10:00", "Home", "Note 1", "Mon", 0.0, 0.0);
        DataManager.saveTasks(Arrays.asList(task));

        assertTrue(Files.exists(deepPath));
    }

    @Test
    void saveTasks_nonEmptySave_resetsRecoveredFromCorruptToFalse() throws Exception {
        // Manually set recovery flag to true
        setRecoveredFromCorrupt(true);

        TaskModel task = new TaskModel("T1", "10:00", "Home", "Note 1", "Mon", 0.0, 0.0);
        DataManager.saveTasks(Arrays.asList(task));

        // Verify that the flag is reset after a non-empty save
        assertFalse(DataManager.isRecoveredFromCorrupt());
    }

    @Test
    void saveTasks_whenRecoveredFromCorruptAndListIsNotEmpty_resetsRecoveredFromCorruptFlagAndSavesTasks()
            throws Exception {
        // 1. Simulate corruption recovery by loading corrupt JSON
        String corruptJson = "[{\"titleTask\":\"Test\", broken json";
        Files.writeString(testFilePath, corruptJson);
        List<TaskModel> loaded = DataManager.loadTasks();
        assertTrue(loaded.isEmpty());
        assertTrue(DataManager.isRecoveredFromCorrupt());
        assertFalse(Files.exists(testFilePath)); // Backup was created, original file renamed/deleted

        // 2. Save a non-empty task list (e.g. adding a task)
        TaskModel task = new TaskModel("T1", "10:00", "Home", "Note 1", "Mon", 0.0, 0.0);
        DataManager.saveTasks(Arrays.asList(task));

        // 3. Verify task is successfully written and recoveredFromCorrupt is reset to false
        assertTrue(Files.exists(testFilePath));
        assertFalse(DataManager.isRecoveredFromCorrupt());

        // 4. Verify we can now delete the task and successfully save the empty list (deleting it from disk)
        DataManager.saveTasks(new ArrayList<>());
        assertTrue(Files.exists(testFilePath));

        List<TaskModel> reloaded = DataManager.loadTasks();
        assertTrue(reloaded.isEmpty());
        assertFalse(DataManager.isRecoveredFromCorrupt());
    }

    /** Full six-field round-trip — guards against silent field renames breaking JSON deserialization. */
    @Test
    void saveTasks_thenLoadTasks_preservesAllFields() {
        TaskModel original = new TaskModel("Meeting", "14:30", "Home", "Bring laptop", "Wed", 123.5, 456.75);
        DataManager.saveTasks(Arrays.asList(original));

        List<TaskModel> loaded = DataManager.loadTasks();
        assertEquals(1, loaded.size());

        TaskModel m = loaded.get(0);
        assertEquals("Meeting", m.getTitleTask());
        assertEquals("14:30", m.getTimeOfTask());
        assertEquals("Home", m.getPlaceofTask());
        assertEquals("Bring laptop", m.getNoteOfTask());
        assertEquals("Wed", m.getDayOfWeek());
        assertEquals(123.5, m.getLayoutX(), 0.001);
        assertEquals(456.75, m.getLayoutY(), 0.001);
    }
}
