package schedule.assist.demo.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import schedule.assist.demo.model.TaskModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
class DataManagerAdversarialTest {

    @TempDir
    Path tempDir;

    private Path testFilePath;
    private String originalFilePath;

    @BeforeEach
    void setUp() throws Exception {
        // Retrieve original file path to restore it later
        Method getFilePathMethod = DataManager.class.getDeclaredMethod("resolveDefaultFilePath");
        getFilePathMethod.setAccessible(true);
        originalFilePath = (String) getFilePathMethod.invoke(null);

        testFilePath = tempDir.resolve("my_tasks.json");
        DataManager.setFilePath(testFilePath.toString());
        DataManager.recoveredFromCorrupt = false;
    }

    @AfterEach
    void tearDown() {
        DataManager.setFilePath(originalFilePath);
        DataManager.recoveredFromCorrupt = false;
    }

    @Test
    void testResolveDefaultFilePath_ReflectiveVerification() throws Exception {
        Method method = DataManager.class.getDeclaredMethod("resolveDefaultFilePath");
        method.setAccessible(true);
        String resolvedPath = (String) method.invoke(null);

        assertNotNull(resolvedPath);
        File file = new File(resolvedPath);
        assertTrue(file.isAbsolute(), "Resolved default file path must be absolute");
        assertEquals("my_tasks.json", file.getName());

        String parentName = file.getParentFile().getName();
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                assertEquals("ScheduleAssistant", parentName);
            } else {
                assertEquals(".schedule-assistant", parentName);
            }
        } else {
            assertEquals(".schedule-assistant", parentName);
        }
    }

    @Test
    void testResolveDefaultFilePath_LinuxFallbackSimulation() throws Exception {
        String originalOs = System.getProperty("os.name");
        String originalUserHome = System.getProperty("user.home");
        try {
            System.setProperty("os.name", "Linux");
            System.setProperty("user.home", tempDir.toString());

            Method method = DataManager.class.getDeclaredMethod("resolveDefaultFilePath");
            method.setAccessible(true);
            String resolved = (String) method.invoke(null);

            File expectedFile = new File(tempDir.toFile(), ".schedule-assistant/my_tasks.json");
            assertEquals(expectedFile.getAbsolutePath(), resolved);
        } finally {
            if (originalOs != null) System.setProperty("os.name", originalOs);
            if (originalUserHome != null) System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void testBackupFileNameCollisionsInSameSecond() throws IOException {
        // Write corrupt JSON to test file
        String corruptJson = "{invalid-json-content:[";
        
        // Trigger backup 5 times in rapid succession
        for (int i = 0; i < 5; i++) {
            Files.writeString(testFilePath, corruptJson);
            List<TaskModel> result = DataManager.loadTasks();
            assertTrue(result.isEmpty());
            assertTrue(DataManager.recoveredFromCorrupt);
        }

        // Check the directory to verify multiple backup files are created with counter suffix
        File[] backups = tempDir.toFile().listFiles((dir, name) -> name.startsWith("my_tasks.json.bak."));
        assertNotNull(backups);
        assertEquals(5, backups.length, "Should have 5 backup files due to successive corruption loads");
    }

    @Test
    void testBackupRenameFailureHandlesGracefully() throws IOException {
        // Write corrupt JSON
        String corruptJson = "{corrupt-json-data";
        Files.writeString(testFilePath, corruptJson);

        // Keep a read stream open to lock the file (on Windows, this prevents renaming)
        try (FileInputStream fis = new FileInputStream(testFilePath.toFile())) {
            // Load tasks should detect corrupt JSON and try to backup
            // Because file is locked, renameTo should fail, but method should handle it gracefully
            List<TaskModel> result = DataManager.loadTasks();
            
            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertTrue(DataManager.recoveredFromCorrupt);
            
            // File should still exist at the original location since rename failed
            assertTrue(Files.exists(testFilePath));
            
            // Now attempt to save tasks with an empty list
            // Since recoveredFromCorrupt is true and list is empty, it must skip save
            // This prevents overwriting the locked/corrupt file
            DataManager.saveTasks(new ArrayList<>());
            
            // Verify content is still the corrupt content and has not been cleared or overwritten
            String currentContent = Files.readString(testFilePath);
            assertEquals(corruptJson, currentContent);
        }
    }

    @Test
    void testSaveTasks_WritePermissionsFailure_DoesNotResetFlag() {
        // Configure path to an invalid/non-writable directory to trigger IOException in saveTasks
        // On Windows, writing to a directory path or an invalid drive will fail.
        String invalidPath = "Z:\\nonexistent_directory_392842\\my_tasks.json";
        DataManager.setFilePath(invalidPath);
        DataManager.recoveredFromCorrupt = true;

        TaskModel task = new TaskModel("T1", "10:00", "Home", "Note 1", "Mon", 0.0, 0.0);
        
        // Should not throw exception
        assertDoesNotThrow(() -> DataManager.saveTasks(Collections.singletonList(task)));

        // Since the write failed (IOException caught), the recoveredFromCorrupt flag must NOT be reset to false
        assertTrue(DataManager.recoveredFromCorrupt, "recoveredFromCorrupt should remain true if save failed");
    }

    @Test
    void testSaveTasks_ConcurrentCalls() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        List<TaskModel> tasks = new ArrayList<>();
        tasks.add(new TaskModel("T1", "10:00", "Home", "Note 1", "Mon", 0.0, 0.0));

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    DataManager.saveTasks(tasks);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(5, TimeUnit.SECONDS);
        assertTrue(finished, "All concurrent save tasks should complete within timeout");
        executor.shutdown();
    }
}
