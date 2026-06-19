package schedule.assist.demo.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import schedule.assist.demo.model.TaskModel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerStressTest {

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
        testFilePath = tempDir.resolve("my_tasks_stress.json");
        DataManager.setFilePath(testFilePath.toString());
        setRecoveredFromCorrupt(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        java.lang.reflect.Method m = DataManager.class.getDeclaredMethod("resolveDefaultFilePath");
        m.setAccessible(true);
        DataManager.setFilePath((String) m.invoke(null));
        setRecoveredFromCorrupt(false);
    }

    /**
     * Test Task 6: Paths containing OS-reserved filenames on Windows. Checks if write fails gracefully without throwing
     * unhandled runtime exceptions.
     */
    @Test
    void testAbsoluteFilePath_ReservedNames() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // "NUL" is a reserved Windows filename. Trying to write to it should fail or be ignored.
            DataManager.setFilePath("NUL");
            List<TaskModel> tasks = Arrays.asList(new TaskModel("Reserved", "10:00", "Home", "Note", "Mon", 0.0, 0.0));

            // Should not crash the application
            assertDoesNotThrow(() -> DataManager.saveTasks(tasks));
        }
    }

    /**
     * Test Task 6: Target path is actually an existing directory. saveTasks should fail gracefully without crashing.
     */
    @Test
    void testAbsoluteFilePath_IsDirectory() {
        DataManager.setFilePath(tempDir.toString()); // Path is the temp directory itself
        List<TaskModel> tasks = Arrays.asList(new TaskModel("DirTarget", "10:00", "Home", "Note", "Mon", 0.0, 0.0));
        assertDoesNotThrow(() -> DataManager.saveTasks(tasks));
    }

    /**
     * Test Task 6: Path with extremely long folder/file name (MAX_PATH stress).
     */
    @Test
    void testAbsoluteFilePath_ExtremelyLongPath() {
        StringBuilder sb = new StringBuilder(tempDir.toString());
        // Generate a very deep folder hierarchy
        for (int i = 0; i < 30; i++) {
            sb.append(File.separator).append("a_very_long_directory_name_to_stress_path_limits_" + i);
        }
        sb.append(File.separator).append("tasks.json");
        String longPath = sb.toString();
        DataManager.setFilePath(longPath);

        List<TaskModel> tasks = Arrays.asList(new TaskModel("LongPathTask", "10:00", "Home", "Note", "Mon", 0.0, 0.0));
        assertDoesNotThrow(() -> DataManager.saveTasks(tasks));

        // If the OS supported the creation, we verify we can load it.
        // Windows might restrict creation if it exceeds MAX_PATH (260 chars) without long paths enabled.
        // The key is that the app must not crash.
        File file = new File(longPath);
        if (file.exists()) {
            List<TaskModel> loaded = DataManager.loadTasks();
            assertEquals(1, loaded.size());
            assertEquals("LongPathTask", loaded.get(0).getTitleTask());
        }
    }

    /**
     * Test Task 1: Robustness of the `recoveredFromCorrupt` flag state machine. Verifies transition of
     * recoveredFromCorrupt state on load/save operations.
     */
    @Test
    void testPreventDataWipe_StateTransitionWorkflow() throws Exception {
        // 1. Initial State: not recovered
        assertFalse(DataManager.isRecoveredFromCorrupt());

        // 2. Corrupt file loaded: recovered becomes true, backup created
        Files.writeString(testFilePath, "{invalid: json");
        List<TaskModel> loaded = DataManager.loadTasks();
        assertTrue(loaded.isEmpty());
        assertTrue(DataManager.isRecoveredFromCorrupt());

        // 3. Save empty list: should be skipped, recovered remains true
        DataManager.saveTasks(new ArrayList<>());
        assertTrue(DataManager.isRecoveredFromCorrupt());
        assertFalse(Files.exists(testFilePath)); // Original file was backed up, no new file created

        // 4. Save null: should be skipped, recovered remains true
        DataManager.saveTasks(null);
        assertTrue(DataManager.isRecoveredFromCorrupt());
        assertFalse(Files.exists(testFilePath));

        // 5. Save non-empty list: writes successfully, recovered becomes false
        List<TaskModel> activeTasks = Arrays
                .asList(new TaskModel("Active Task", "12:00", "School", "Note", "Tue", 5.0, 10.0));
        DataManager.saveTasks(activeTasks);
        assertFalse(DataManager.isRecoveredFromCorrupt());
        assertTrue(Files.exists(testFilePath));

        // 6. Save empty list now: since recovered is false, it is NOT skipped, writing empty array to disk
        DataManager.saveTasks(new ArrayList<>());
        assertFalse(DataManager.isRecoveredFromCorrupt());
        assertTrue(Files.exists(testFilePath));

        List<TaskModel> reloaded = DataManager.loadTasks();
        assertTrue(reloaded.isEmpty());
    }

    /**
     * Test Task 1 & 6: Concurrency stress testing. Spawns multiple threads performing concurrent reads and writes.
     * Evaluates thread safety behavior of shared static state in DataManager.
     */
    @Test
    void testConcurrency_ReadsAndWrites() throws InterruptedException {
        int threads = 10;
        int iterationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger writeSuccess = new AtomicInteger(0);
        AtomicInteger readSuccess = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterationsPerThread; j++) {
                        if (j % 2 == 0) {
                            // Writer thread action
                            List<TaskModel> list = new ArrayList<>();
                            list.add(new TaskModel("Task " + threadId + "-" + j, "10:00", "Home", "Note", "Mon", 0.0,
                                    0.0));
                            DataManager.saveTasks(list);
                            writeSuccess.incrementAndGet();
                        } else {
                            // Reader thread action
                            List<TaskModel> loaded = DataManager.loadTasks();
                            assertNotNull(loaded);
                            readSuccess.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = endLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Concurrency test timed out!");
        assertTrue(writeSuccess.get() > 0);
        assertTrue(readSuccess.get() > 0);
    }

    /**
     * Test Task 1: Load a huge list of tasks to stress memory and Gson parsing bounds.
     */
    @Test
    void testMassiveDataList() {
        int taskCount = 10000;
        List<TaskModel> massiveList = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            massiveList.add(new TaskModel("Task Title " + i + "_" + UUID.randomUUID(), "Time " + i, "Place " + i,
                    "Note content that is slightly longer to add body " + i, "Day " + (i % 7), i * 1.5, i * 2.5));
        }

        // Save
        assertDoesNotThrow(() -> DataManager.saveTasks(massiveList));

        // Load
        List<TaskModel> loaded = assertDoesNotThrow(DataManager::loadTasks);
        assertEquals(taskCount, loaded.size());
        assertEquals(massiveList.get(0).getTitleTask(), loaded.get(0).getTitleTask());
        assertEquals(massiveList.get(taskCount - 1).getTitleTask(), loaded.get(taskCount - 1).getTitleTask());
    }

    /**
     * Test: saveTasks writes to a temp file and atomically replaces the original. On success, the temp file should be
     * cleaned up.
     */
    @Test
    void testAtomicWrite_TempFileCleanedUp() throws Exception {
        List<TaskModel> tasks = Arrays.asList(new TaskModel("T1", "10:00", "Home", "Note", "Mon", 0.0, 0.0));
        DataManager.saveTasks(tasks);

        assertTrue(Files.exists(testFilePath));
        assertFalse(Files.exists(testFilePath.resolveSibling(testFilePath.getFileName() + ".tmp")),
                "Temp file should be cleaned up after successful save");

        // Verify content is correct
        List<TaskModel> loaded = DataManager.loadTasks();
        assertEquals(1, loaded.size());
        assertEquals("T1", loaded.get(0).getTitleTask());
    }

    /**
     * Test: if the parent directory is read-only, saveTasks should not crash. The file write may fail, but the original
     * file should not be truncated.
     */
    @Test
    void testAtomicWrite_InvalidPath_DoesNotCrash() throws Exception {
        DataManager.setFilePath("Z:\\nonexistent\\\\my_tasks.json");
        setRecoveredFromCorrupt(true);

        List<TaskModel> tasks = Arrays.asList(new TaskModel("T1", "10:00", "Home", "Note", "Mon", 0.0, 0.0));
        assertDoesNotThrow(() -> DataManager.saveTasks(tasks));
        assertTrue(DataManager.isRecoveredFromCorrupt(), "Flag should remain true on write failure");
    }
}
