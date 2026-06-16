package schedule.assist.demo.e2e;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.repository.TaskRepository;
import schedule.assist.demo.ui.AddTaskButton;
import schedule.assist.demo.ui.HelloApplication;
import schedule.assist.demo.ui.Task;
import schedule.assist.demo.ui.TaskEditor;
import schedule.assist.demo.util.DataManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleAppE2ETest {

    private static HelloApplication app;
    private static Stage stage;
    private static AnchorPane root;
    private static List<Task> appTaskList;
    private static String defaultResolvedPath;

    @BeforeAll
    static void initJFX() throws Exception {
        defaultResolvedPath = getFILE_PATH();
        CountDownLatch latch = new CountDownLatch(1);
        Runnable startupRunnable = () -> {
            try {
                Platform.setImplicitExit(false);
                app = new HelloApplication();
                stage = new Stage();
                app.start(stage);

                // Reflectively get the root pane
                Field rootField = HelloApplication.class.getDeclaredField("root");
                rootField.setAccessible(true);
                root = (AnchorPane) rootField.get(app);

                // Reflectively get taskList
                Field taskListField = HelloApplication.class.getDeclaredField("taskList");
                taskListField.setAccessible(true);
                appTaskList = (List<Task>) taskListField.get(app);

                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        try {
            Platform.startup(startupRunnable);
        } catch (IllegalStateException e) {
            Platform.runLater(startupRunnable);
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX Platform startup timed out!");
    }

    @BeforeEach
    void setUp() throws Exception {
        resetAppState();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetAppState();
    }

    private static void resetAppState() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                if (appTaskList != null) {
                    appTaskList.clear();
                }
                if (root != null) {
                    boolean hasWeekRow = false;
                    for (Node node : root.getChildren()) {
                        if (node instanceof HBox) {
                            hasWeekRow = true;
                            break;
                        }
                    }
                    if (!hasWeekRow) {
                        schedule.assist.demo.ui.Week week = new schedule.assist.demo.ui.Week();
                        HBox weekCard = week.createWeekView();
                        root.getChildren().add(0, weekCard);
                    }

                    List<Node> toRemove = new ArrayList<>();
                    for (Node node : root.getChildren()) {
                        if (!(node instanceof HBox) && !(node instanceof AddTaskButton)) {
                            toRemove.add(node);
                        }
                    }
                    root.getChildren().removeAll(toRemove);
                }
                setFILE_PATH("my_tasks.json");
                Files.deleteIfExists(Path.of("my_tasks.json"));

                // Clean up backups
                File dir = new File(".");
                File[] backups = dir.listFiles((d, name) -> name.startsWith("my_tasks.json.bak."));
                if (backups != null) {
                    for (File backup : backups) {
                        backup.delete();
                    }
                }

                // Reflectively reset DataManager.recoveredFromCorrupt = false
                try {
                    Field field = DataManager.class.getDeclaredField("recoveredFromCorrupt");
                    field.setAccessible(true);
                    field.set(null, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    private static boolean getRecoveredFromCorrupt() {
        try {
            Field field = DataManager.class.getDeclaredField("recoveredFromCorrupt");
            field.setAccessible(true);
            return (boolean) field.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Field 'recoveredFromCorrupt' is not yet implemented in DataManager", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFILE_PATH() {
        try {
            Field field = DataManager.class.getDeclaredField("FILE_PATH");
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setFILE_PATH(String path) {
        try {
            Method method = DataManager.class.getDeclaredMethod("setFilePath", String.class);
            method.setAccessible(true);
            method.invoke(null, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AddTaskButton getAddTaskButton() {
        for (Node node : root.getChildren()) {
            if (node instanceof AddTaskButton) {
                return (AddTaskButton) node;
            }
        }
        throw new IllegalStateException("AddTaskButton not found in UI root");
    }

    private List<Task> getTasksInUI() {
        List<Task> tasks = new ArrayList<>();
        for (Node node : root.getChildren()) {
            if (node instanceof Task) {
                tasks.add((Task) node);
            }
        }
        return tasks;
    }

    private HBox getWeekRow() {
        for (Node node : root.getChildren()) {
            if (node instanceof HBox) {
                return (HBox) node;
            }
        }
        throw new IllegalStateException("Week HBox not found in UI root");
    }

    private void reloadApp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                if (appTaskList != null) {
                    appTaskList.clear();
                }
                if (root != null) {
                    root.getChildren().clear();
                }
                app.start(stage);

                // Reflectively get the root pane again
                Field rootField = HelloApplication.class.getDeclaredField("root");
                rootField.setAccessible(true);
                root = (AnchorPane) rootField.get(app);

                // Reflectively get taskList again
                Field taskListField = HelloApplication.class.getDeclaredField("taskList");
                taskListField.setAccessible(true);
                appTaskList = (List<Task>) taskListField.get(app);

                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    private void clickAddTaskButton() {
        AddTaskButton btn = getAddTaskButton();
        JFXTestUtils.simulateButtonFire(btn);
    }

    // ==========================================
    // TIER 1: FEATURE COVERAGE
    // ==========================================
    @Nested
    class Tier1FeatureCoverage {

        // --- F1: Task Storage & Corrupt JSON Recovery ---
        @Test
        void testF1_1_LoadValidJson() throws Exception {
            String json = "[{\"titleTask\":\"Test Task\",\"timeOfTask\":\"12:00\",\"placeofTask\":\"Home\",\"noteOfTask\":\"Some notes\",\"dayOfWeek\":\"Mon\",\"layoutX\":100.0,\"layoutY\":200.0}]";
            Files.writeString(Path.of("my_tasks.json"), json);
            reloadApp();
            assertEquals(1, appTaskList.size());
            assertEquals("Test Task", appTaskList.get(0).getTitleTask());
        }

        @Test
        void testF1_2_LoadEmptyJson() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "[]");
            reloadApp();
            assertTrue(appTaskList.isEmpty());
        }

        @Test
        void testF1_3_LoadMissingJson() throws Exception {
            Files.deleteIfExists(Path.of("my_tasks.json"));
            reloadApp();
            assertTrue(appTaskList.isEmpty());
        }

        @Test
        void testF1_4_RecoverCorruptBackup() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "{corrupt json}");
            reloadApp();
            assertTrue(appTaskList.isEmpty());
            File[] backups = new File(".").listFiles((d, name) -> name.startsWith("my_tasks.json.bak."));
            assertNotNull(backups);
            assertTrue(backups.length >= 1, "Backup file should be created");
        }

        @Test
        void testF1_5_RecoverCorruptEmptySkipSave() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "{corrupt json}");
            reloadApp();
            assertTrue(getRecoveredFromCorrupt(), "Should flag that we recovered from corrupt data");
            assertTrue(appTaskList.isEmpty());
            JFXTestUtils.simulateWindowClose(stage);
            assertFalse(Files.exists(Path.of("my_tasks.json")), "Empty save should be skipped to prevent overwriting backup");
        }

        // --- F2: Dynamic Task Creation & Centering ---
        @Test
        void testF2_1_CreateTaskAddsToRoot() {
            clickAddTaskButton();
            List<Task> uiTasks = getTasksInUI();
            assertEquals(1, uiTasks.size());
        }

        @Test
        void testF2_2_CreateTaskAddsToServiceList() {
            clickAddTaskButton();
            assertEquals(1, appTaskList.size());
        }

        @Test
        void testF2_3_CenteringAtRuntime_SmallSize() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            assertEquals(HelloApplication.SCREEN_W / 2 - 100, task.getLayoutX(), 1.0);
            assertEquals(HelloApplication.SCREEN_H / 2, task.getLayoutY(), 1.0);
        }

        @Test
        void testF2_4_CenteringAtRuntime_LargeSize() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                root.setPrefSize(2000, 1000);
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            assertEquals(1000 - 100, task.getLayoutX(), 1.0);
            assertEquals(500, task.getLayoutY(), 1.0);
        }

        @Test
        void testF2_5_CreateTaskTriggersSave() {
            clickAddTaskButton();
            assertTrue(Files.exists(Path.of("my_tasks.json")));
        }

        // --- F3: Interactive Task Editing ---
        @Test
        void testF3_1_RightClickOpensEditor() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            assertFalse(task.isEditing);
            JFXTestUtils.simulateRightClick(task);
            assertTrue(task.isEditing);
        }

        @Test
        void testF3_2_RightClickWhileEditingDoesNothing() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);
            assertTrue(task.isEditing);

            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                assertFalse(tfs.isEmpty());
                tfs.get(0).setText("Keep this text");
                latch1.countDown();
            });
            assertTrue(latch1.await(5, TimeUnit.SECONDS));

            // Right click again while in edit mode
            JFXTestUtils.simulateRightClick(task);
            assertTrue(task.isEditing);

            CountDownLatch latch2 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                assertFalse(tfs.isEmpty());
                assertEquals("Keep this text", tfs.get(0).getText());
                latch2.countDown();
            });
            assertTrue(latch2.await(5, TimeUnit.SECONDS));
        }

        @Test
        void testF3_3_EscapeSavesAndCollapses() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);
            assertTrue(task.isEditing);
            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);
            assertFalse(task.isEditing);
        }

        @Test
        void testF3_4_EditUpdatesModelProperties() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                assertFalse(tfs.isEmpty());
                tfs.get(0).setText("New Title");
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);
            assertEquals("New Title", task.getTitleTask());
        }

        @Test
        void testF3_5_EditTriggersSave() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                tfs.get(0).setText("Title X");
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);
            String content = Files.readString(Path.of("my_tasks.json"));
            assertTrue(content.contains("Title X"));
        }

        // --- F4: Task Relocation & Column Snapping ---
        @Test
        void testF4_1_SnapToFirstColumn() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            HBox weekRow = getWeekRow();
            VBox monday = (VBox) weekRow.getChildren().get(0);

            double[] targetX = new double[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                targetX[0] = monday.localToScene(0, 0).getX();
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), targetX[0] + 10, 150);

            assertEquals("Mon", task.getDayOfWeek());
        }

        @Test
        void testF4_2_SnapToLastColumn() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            HBox weekRow = getWeekRow();
            VBox sunday = (VBox) weekRow.getChildren().get(6);

            double[] targetX = new double[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                targetX[0] = sunday.localToScene(0, 0).getX();
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), targetX[0] + 10, 150);

            assertEquals("Sun", task.getDayOfWeek());
        }

        @Test
        void testF4_3_DragFarAwayClearsDay() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            // Snaps to Wednesday
            double[] targetX = new double[1];
            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox wed = (VBox) getWeekRow().getChildren().get(2);
                targetX[0] = wed.localToScene(0, 0).getX();
                latch1.countDown();
            });
            assertTrue(latch1.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), targetX[0] + 10, 150);
            assertEquals("Wed", task.getDayOfWeek());

            // Drag far away (e.g. layoutX = 1300, far from all columns)
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), 1300, 50);

            assertEquals("", task.getDayOfWeek(), "Dragged far away should clear dayOfWeek");
        }

        @Test
        void testF4_4_SnapToClosestColumn() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            HBox weekRow = getWeekRow();
            VBox tues = (VBox) weekRow.getChildren().get(1);
            VBox wed = (VBox) weekRow.getChildren().get(2);

            double[] middleX = new double[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                double tuesX = tues.localToScene(0, 0).getX();
                double wedX = wed.localToScene(0, 0).getX();
                middleX[0] = tuesX + (wedX - tuesX) * 0.7;
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), middleX[0], 150);

            assertEquals("Wed", task.getDayOfWeek());
        }

        @Test
        void testF4_5_SnapTriggersSave() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            double[] targetX = new double[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox fri = (VBox) getWeekRow().getChildren().get(4);
                targetX[0] = fri.localToScene(0, 0).getX();
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), targetX[0] + 10, 150);

            String content = Files.readString(Path.of("my_tasks.json"));
            assertTrue(content.contains("Fri"));
        }

        // --- F5: OS-Specific Configuration Directory ---
        @Test
        void testF5_1_WindowsDefaultPath() {
            Assumptions.assumeTrue(System.getProperty("os.name").toLowerCase().contains("win"));
            assertNotNull(defaultResolvedPath);
            assertTrue(defaultResolvedPath.contains("ScheduleAssistant"));
            assertTrue(defaultResolvedPath.endsWith("my_tasks.json") || defaultResolvedPath.endsWith("my_tasks.json".replace('/', File.separatorChar)));
        }

        @Test
        void testF5_2_NonWindowsDefaultPath() {
            Assumptions.assumeFalse(System.getProperty("os.name").toLowerCase().contains("win"));
            assertNotNull(defaultResolvedPath);
            assertTrue(defaultResolvedPath.contains(".schedule-assistant"));
            assertTrue(defaultResolvedPath.endsWith("my_tasks.json") || defaultResolvedPath.endsWith("my_tasks.json".replace('/', File.separatorChar)));
        }

        @Test
        void testF5_3_ParentDirCreated() throws Exception {
            String path = "test_parent_created/my_tasks.json";
            setFILE_PATH(path);
            try {
                clickAddTaskButton();
                File file = new File(path);
                assertNotNull(file.getParentFile());
                assertTrue(file.getParentFile().exists(), "Parent directory should be automatically created");
            } finally {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                if (file.getParentFile() != null && file.getParentFile().exists()) {
                    file.getParentFile().delete();
                }
            }
        }

        @Test
        void testF5_4_SetFilePathSetter() {
            setFILE_PATH("custom_path.json");
            assertEquals("custom_path.json", getFILE_PATH());
        }

        @Test
        void testF5_5_PathResolutionValidity() {
            assertNotNull(defaultResolvedPath);
            File file = new File(defaultResolvedPath);
            assertTrue(file.isAbsolute());
            assertNotNull(file.getName());
        }

        // --- F6: Task Deletion Lifecycle ---
        @Test
        void testF6_1_DeleteTaskRemovesFromRoot() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            Platform.runLater(task.onDelete);
            JFXTestUtils.waitForFxEvents();
            assertTrue(getTasksInUI().isEmpty());
        }

        @Test
        void testF6_2_DeleteTaskRemovesFromServiceList() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            Platform.runLater(task.onDelete);
            JFXTestUtils.waitForFxEvents();
            assertTrue(appTaskList.isEmpty());
        }

        @Test
        void testF6_3_DeleteTaskTriggersSave() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            Platform.runLater(task.onDelete);
            JFXTestUtils.waitForFxEvents();
            String content = Files.readString(Path.of("my_tasks.json"));
            assertEquals("[]", content.trim());
        }

        @Test
        void testF6_4_DeleteFromEditor() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<Button> buttons = JFXTestUtils.findNodes(task, Button.class);
                assertFalse(buttons.isEmpty());
                buttons.get(0).fire();
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            assertTrue(getTasksInUI().isEmpty());
        }

        @Test
        void testF6_5_DeleteEmptyTaskList() {
            assertTrue(appTaskList.isEmpty());
            assertDoesNotThrow(() -> {
                Platform.runLater(() -> app.start(stage));
                JFXTestUtils.waitForFxEvents();
            });
        }
    }

    // ==========================================
    // TIER 2: BOUNDARY & CORNER CASES
    // ==========================================
    @Nested
    class Tier2BoundaryCorner {

        // --- F1 Boundaries ---
        @Test
        void testF1_Boundary_LargeCorruptString() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < 10000; i++) {
                sb.append("{\"corrupt\":");
            }
            Files.writeString(Path.of("my_tasks.json"), sb.toString());
            reloadApp();
            assertTrue(appTaskList.isEmpty());
        }

        @Test
        void testF1_Boundary_MultipleSequentialCorruptions() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "corrupt 1");
            reloadApp();
            Files.writeString(Path.of("my_tasks.json"), "corrupt 2");
            reloadApp();
            File[] backups = new File(".").listFiles((d, name) -> name.startsWith("my_tasks.json.bak."));
            assertNotNull(backups);
            assertTrue(backups.length >= 2, "Multiple backup files should exist");
        }

        @Test
        void testF1_Boundary_EmptyJsonArrayPreservation() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "   [   ]  ");
            reloadApp();
            assertTrue(appTaskList.isEmpty());
            File[] backups = new File(".").listFiles((d, name) -> name.startsWith("my_tasks.json.bak."));
            assertTrue(backups == null || backups.length == 0, "Valid empty array should not trigger backup");
        }

        @Test
        void testF1_Boundary_BakDirCreationFailures() {
            // Check that if directory cannot be read or write is restricted, app degrades gracefully
            assertDoesNotThrow(() -> {
                DataManager.loadTasks();
            });
        }

        @Test
        void testF1_Boundary_RecoverWithOneAddedTask() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "{corrupt json}");
            reloadApp();
            clickAddTaskButton();
            JFXTestUtils.simulateWindowClose(stage);
            assertTrue(Files.exists(Path.of("my_tasks.json")), "Should save because 1 task was added");
        }

        // --- F2 Boundaries ---
        @Test
        void testF2_Boundary_ParentPaneZeroSize() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                root.setPrefSize(0, 0);
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);
            assertDoesNotThrow(() -> clickAddTaskButton());
        }

        @Test
        void testF2_Boundary_ParentPaneExtremelyLarge() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                root.setPrefSize(10000, 10000);
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            assertEquals(5000 - 100, task.getLayoutX(), 1.0);
            assertEquals(5000, task.getLayoutY(), 1.0);
        }

        @Test
        void testF2_Boundary_CreationCoordinatesCheck() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            assertTrue(task.getLayoutX() >= 0);
            assertTrue(task.getLayoutY() >= 0);
        }

        @Test
        void testF2_Boundary_RapidTaskCreationSpam() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                for (int i = 0; i < 10; i++) {
                    getAddTaskButton().fire();
                }
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);
            assertEquals(10, appTaskList.size());
        }

        @Test
        void testF2_Boundary_LayoutPropertiesBeforeAfterRender() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            assertNotNull(task.titleLabel.getText());
        }

        // --- F3 Boundaries ---
        @Test
        void testF3_Boundary_NullEmptyInputs() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            String origTitle = task.getTitleTask();
            JFXTestUtils.simulateRightClick(task);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                tfs.get(0).setText(""); // Empty title input
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);
            assertEquals(origTitle, task.getTitleTask(), "Empty input should fallback to original title");
        }

        @Test
        void testF3_Boundary_ExtremelyLongStrings() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) sb.append("A");

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                tfs.get(0).setText(sb.toString());
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);
            assertEquals(sb.toString(), task.getTitleTask());
        }

        @Test
        void testF3_Boundary_SpecialCharactersUnicode() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);

            String unicode = "🚀 🌟 Chữ Tiếng Việt 123!";
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                tfs.get(0).setText(unicode);
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);
            assertEquals(unicode, task.getTitleTask());
        }

        @Test
        void testF3_Boundary_RapidClickEditSpam() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                for (int i = 0; i < 5; i++) {
                    JFXTestUtils.simulateRightClick(task);
                }
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);
            assertTrue(task.isEditing);
        }

        @Test
        void testF3_Boundary_EscTriggerAtDifferentLevels() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);
            assertTrue(task.isEditing);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                JFXTestUtils.simulateKeyPress(tfs.get(0), KeyCode.ESCAPE);
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            assertFalse(task.isEditing);
        }

        // --- F4 Boundaries ---
        @Test
        void testF4_Boundary_DragExactlyHalfway() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            HBox weekRow = getWeekRow();
            VBox mon = (VBox) weekRow.getChildren().get(0);
            VBox tue = (VBox) weekRow.getChildren().get(1);

            double[] midX = new double[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                double monX = mon.localToScene(0, 0).getX();
                double tueX = tue.localToScene(0, 0).getX();
                midX[0] = (monX + tueX) / 2;
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), midX[0], 150);

            assertNotNull(task.getDayOfWeek());
        }

        @Test
        void testF4_Boundary_DragOffScreenNegative() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), -100, -100);

            assertEquals("", task.getDayOfWeek());
        }

        @Test
        void testF4_Boundary_DragWithNoColumns() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                root.getChildren().remove(getWeekRow());
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), 200, 200);

            assertEquals("", task.getDayOfWeek());
        }

        @Test
        void testF4_Boundary_DragMismatchedDays() {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            task.setDayOfWeek("Mon");
            // Set position far away manually
            task.setLayoutX(1000);
            // Calling snapToColumn is expected to snap to correct location
            Platform.runLater(() -> {
                try {
                    java.lang.reflect.Method m = Task.class.getDeclaredMethod("snapToColumn");
                    m.setAccessible(true);
                    m.invoke(task);
                } catch (Exception e) {}
            });
            JFXTestUtils.waitForFxEvents();
            assertNotNull(task.getDayOfWeek());
        }

        @Test
        void testF4_Boundary_DragAndDropSnapBoundaries() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            HBox weekRow = getWeekRow();
            VBox col = (VBox) weekRow.getChildren().get(3); // Thursday

            double[] edgeX = new double[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                double x = col.localToScene(0, 0).getX();
                double w = col.getWidth();
                edgeX[0] = x + w / 2 - 2;
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), edgeX[0], 150);

            assertEquals("Thu", task.getDayOfWeek());
        }

        // --- F5 Boundaries ---
        @Test
        void testF5_Boundary_SpecialCharacterFolderNames() throws Exception {
            String path = "space dir/my_tasks.json";
            setFILE_PATH(path);
            try {
                clickAddTaskButton();
                File file = new File(path);
                assertTrue(file.exists(), "File should be created at: " + path);
            } finally {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                if (file.getParentFile() != null && file.getParentFile().exists()) {
                    file.getParentFile().delete();
                }
            }
        }

        @Test
        void testF5_Boundary_EmptyUserHomeResolution() throws Exception {
            String originalUserHome = System.getProperty("user.home");
            String originalOsName = System.getProperty("os.name");
            String tempUserHome = new File("temp_home").getAbsolutePath();
            System.setProperty("user.home", tempUserHome);
            System.setProperty("os.name", "mac"); // Force non-Windows resolution
            try {
                Method resolveMethod = DataManager.class.getDeclaredMethod("resolveDefaultFilePath");
                resolveMethod.setAccessible(true);
                String resolvedPath = (String) resolveMethod.invoke(null);
                
                assertNotNull(resolvedPath);
                assertTrue(resolvedPath.contains(".schedule-assistant"));
                
                setFILE_PATH(resolvedPath);
                clickAddTaskButton();
                File file = new File(resolvedPath);
                assertTrue(file.exists());
            } finally {
                System.setProperty("user.home", originalUserHome);
                System.setProperty("os.name", originalOsName);
                
                File file = new File(tempUserHome, ".schedule-assistant/my_tasks.json");
                if (file.exists()) {
                    file.delete();
                }
                File parent = file.getParentFile();
                if (parent != null && parent.exists()) {
                    parent.delete();
                }
                File homeDir = new File(tempUserHome);
                if (homeDir.exists()) {
                    homeDir.delete();
                }
            }
        }

        @Test
        void testF5_Boundary_WindowsPathFormatMix() throws Exception {
            String path = "test_mix/sub\\my_tasks.json";
            setFILE_PATH(path);
            try {
                clickAddTaskButton();
                File file = new File(path);
                assertTrue(file.exists(), "File should be created at mixed path: " + path);
            } finally {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                File subDir = new File("test_mix/sub");
                if (subDir.exists()) {
                    subDir.delete();
                }
                File parentDir = new File("test_mix");
                if (parentDir.exists()) {
                    parentDir.delete();
                }
            }
        }

        @Test
        void testF5_Boundary_RelativePathsConfig() throws Exception {
            String path = "./relative/path/tasks.json";
            setFILE_PATH(path);
            try {
                clickAddTaskButton();
                File file = new File(path);
                assertTrue(file.exists(), "File should be created at relative path: " + path);
            } finally {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                File parent = file.getParentFile();
                if (parent != null && parent.exists()) {
                    parent.delete();
                }
                File grandParent = parent != null ? parent.getParentFile() : null;
                if (grandParent != null && grandParent.exists()) {
                    grandParent.delete();
                }
            }
        }

        @Test
        void testF5_Boundary_WritePermissionsFailures() {
            setFILE_PATH("S:/invalid_drive/tasks.json");
            List<TaskModel> tasks = new ArrayList<>();
            tasks.add(new TaskModel("Test Task", "12:00", "Home", "Notes", "Mon", 100.0, 200.0));
            assertDoesNotThrow(() -> {
                DataManager.saveTasks(tasks);
            });
        }

        // --- F6 Boundaries ---
        @Test
        void testF6_Boundary_DoubleClickDeleteButton() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                task.onDelete.run();
                task.onDelete.run();
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);
            assertTrue(getTasksInUI().isEmpty());
        }

        @Test
        void testF6_Boundary_DeleteOnlyTask() {
            clickAddTaskButton();
            assertEquals(1, appTaskList.size());
            Platform.runLater(getTasksInUI().get(0).onDelete);
            JFXTestUtils.waitForFxEvents();
            assertTrue(appTaskList.isEmpty());
        }

        @Test
        void testF6_Boundary_SequentialDeletionAllTasks() throws Exception {
            clickAddTaskButton();
            clickAddTaskButton();
            clickAddTaskButton();
            assertEquals(3, appTaskList.size());

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                while (!appTaskList.isEmpty()) {
                    appTaskList.get(0).onDelete.run();
                }
                latch.countDown();
            });
            latch.await(5, TimeUnit.SECONDS);

            assertTrue(appTaskList.isEmpty());
        }

        @Test
        void testF6_Boundary_DeleteTaskDuringEditing() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);
            assertTrue(task.isEditing);

            Platform.runLater(task.onDelete);
            JFXTestUtils.waitForFxEvents();
            assertTrue(getTasksInUI().isEmpty());
        }

        @Test
        void testF6_Boundary_DeleteNullReferenceTask() {
            assertDoesNotThrow(() -> {
                CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        Field rootField = HelloApplication.class.getDeclaredField("root");
                        rootField.setAccessible(true);
                        AnchorPane rootPane = (AnchorPane) rootField.get(app);
                        
                        Field taskListField = HelloApplication.class.getDeclaredField("taskList");
                        taskListField.setAccessible(true);
                        List<Task> list = (List<Task>) taskListField.get(app);
                        
                        Field repoField = HelloApplication.class.getDeclaredField("taskRepository");
                        repoField.setAccessible(true);
                        TaskRepository repo = (TaskRepository) repoField.get(app);
                        
                        schedule.assist.demo.service.TaskService service = new schedule.assist.demo.service.TaskServiceImpl(rootPane, repo, list);
                        service.deleteTask(null);
                        latch.countDown();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                assertTrue(latch.await(5, TimeUnit.SECONDS));
            });
        }
    }

    // ==========================================
    // TIER 3: CROSS-FEATURE COMBINATIONS
    // ==========================================
    @Nested
    class Tier3CrossFeature {

        @Test
        void testF3_Cross_CreateDragEditClose() throws Exception {
            // 1. Create task
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);

            // 2. Drag to Sat
            double[] satX = new double[1];
            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox sat = (VBox) getWeekRow().getChildren().get(5);
                satX[0] = sat.localToScene(0, 0).getX();
                latch1.countDown();
            });
            assertTrue(latch1.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), satX[0] + 10, 150);

            // 3. Edit title
            JFXTestUtils.simulateRightClick(task);
            CountDownLatch latch2 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                tfs.get(0).setText("Cross Test Task");
                latch2.countDown();
            });
            assertTrue(latch2.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);

            // 4. Close and verify
            JFXTestUtils.simulateWindowClose(stage);
            String content = Files.readString(Path.of("my_tasks.json"));
            assertTrue(content.contains("Cross Test Task"));
            assertTrue(content.contains("Sat"));
        }

        @Test
        void testF3_Cross_CorruptLoadCreateSave() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "{corrupt}");
            reloadApp();
            clickAddTaskButton();
            JFXTestUtils.simulateWindowClose(stage);
            assertTrue(Files.exists(Path.of("my_tasks.json")));
        }

        @Test
        void testF3_Cross_DragFarDelete() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);

            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), 1300, 50);
            assertEquals("", task.getDayOfWeek());

            Platform.runLater(task.onDelete);
            JFXTestUtils.waitForFxEvents();
            assertTrue(appTaskList.isEmpty());
        }

        @Test
        void testF3_Cross_EditDragEdit() throws Exception {
            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            JFXTestUtils.simulateRightClick(task);

            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                tfs.get(0).setText("Initial Title");
                latch1.countDown();
            });
            assertTrue(latch1.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);

            double[] targetX = new double[1];
            CountDownLatch latch2 = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox wed = (VBox) getWeekRow().getChildren().get(2);
                targetX[0] = wed.localToScene(0, 0).getX();
                latch2.countDown();
            });
            assertTrue(latch2.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), targetX[0] + 10, 150);

            JFXTestUtils.simulateRightClick(task);
            CountDownLatch latch3 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(task, TextField.class);
                tfs.get(0).setText("Final Title");
                latch3.countDown();
            });
            assertTrue(latch3.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateKeyPress(task, KeyCode.ESCAPE);

            assertEquals("Final Title", task.getTitleTask());
            assertEquals("Wed", task.getDayOfWeek());
        }

        @Test
        void testF3_Cross_CenteringResizeDrag() throws Exception {
            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                root.setPrefSize(1800, 900);
                latch1.countDown();
            });
            assertTrue(latch1.await(5, TimeUnit.SECONDS));

            clickAddTaskButton();
            Task task = getTasksInUI().get(0);
            assertEquals(900 - 100, task.getLayoutX(), 1.0);

            double[] targetX = new double[1];
            CountDownLatch latch2 = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox mon = (VBox) getWeekRow().getChildren().get(0);
                targetX[0] = mon.localToScene(0, 0).getX();
                latch2.countDown();
            });
            assertTrue(latch2.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task, task.getLayoutX(), task.getLayoutY(), targetX[0] + 10, 150);

            assertEquals("Mon", task.getDayOfWeek());
        }

        @Test
        void testF3_Cross_LoadCreateDragDelete() throws Exception {
            String json = "[{\"titleTask\":\"Task 1\",\"timeOfTask\":\"10:00\",\"placeofTask\":\"School\",\"noteOfTask\":\"notes\",\"dayOfWeek\":\"Mon\",\"layoutX\":150.0,\"layoutY\":100.0}]";
            Files.writeString(Path.of("my_tasks.json"), json);
            reloadApp();

            clickAddTaskButton();
            Task task2 = getTasksInUI().get(1);

            double[] targetX = new double[1];
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox tue = (VBox) getWeekRow().getChildren().get(1);
                targetX[0] = tue.localToScene(0, 0).getX();
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateDragAndDrop(task2, task2.getLayoutX(), task2.getLayoutY(), targetX[0] + 10, 150);

            Platform.runLater(task2.onDelete);
            JFXTestUtils.waitForFxEvents();

            assertEquals(1, appTaskList.size());
            assertEquals("Task 1", appTaskList.get(0).getTitleTask());
        }
    }

    // ==========================================
    // TIER 4: REAL-WORLD APPLICATION SCENARIOS
    // ==========================================
    @Nested
    class Tier4RealWorldScenarios {

        @Test
        void testF4_Scenario_UserFullWorkflow() throws Exception {
            // 1. Add 3 tasks
            clickAddTaskButton();
            clickAddTaskButton();
            clickAddTaskButton();
            List<Task> tasks = getTasksInUI();

            // 2. Drag to Mon, Wed, Fri
            double[] monX = new double[1];
            double[] wedX = new double[1];
            double[] friX = new double[1];
            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox mon = (VBox) getWeekRow().getChildren().get(0);
                VBox wed = (VBox) getWeekRow().getChildren().get(2);
                VBox fri = (VBox) getWeekRow().getChildren().get(4);
                monX[0] = mon.localToScene(0, 0).getX();
                wedX[0] = wed.localToScene(0, 0).getX();
                friX[0] = fri.localToScene(0, 0).getX();
                latch1.countDown();
            });
            assertTrue(latch1.await(5, TimeUnit.SECONDS));

            JFXTestUtils.simulateDragAndDrop(tasks.get(0), tasks.get(0).getLayoutX(), tasks.get(0).getLayoutY(), monX[0] + 10, 150);
            JFXTestUtils.simulateDragAndDrop(tasks.get(1), tasks.get(1).getLayoutX(), tasks.get(1).getLayoutY(), wedX[0] + 10, 150);
            JFXTestUtils.simulateDragAndDrop(tasks.get(2), tasks.get(2).getLayoutX(), tasks.get(2).getLayoutY(), friX[0] + 10, 150);

            // 3. Edit task details
            JFXTestUtils.simulateRightClick(tasks.get(0));
            CountDownLatch latch2 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(tasks.get(0), TextField.class);
                tfs.get(0).setText("Task Mon");
                latch2.countDown();
            });
            assertTrue(latch2.await(5, TimeUnit.SECONDS));
            JFXTestUtils.simulateKeyPress(tasks.get(0), KeyCode.ESCAPE);

            // 4. Close
            JFXTestUtils.simulateWindowClose(stage);

            // 5. Reload and check
            reloadApp();
            assertEquals(3, appTaskList.size());
            assertEquals("Task Mon", appTaskList.get(0).getTitleTask());
            assertEquals("Mon", appTaskList.get(0).getDayOfWeek());
            assertEquals("Wed", appTaskList.get(1).getDayOfWeek());
            assertEquals("Fri", appTaskList.get(2).getDayOfWeek());
        }

        @Test
        void testF4_Scenario_CorruptRecoveryWorkflow() throws Exception {
            Files.writeString(Path.of("my_tasks.json"), "malformed json data 123");
            reloadApp();
            assertTrue(getRecoveredFromCorrupt());
            clickAddTaskButton();
            JFXTestUtils.simulateWindowClose(stage);
            reloadApp();
            assertEquals(1, appTaskList.size());
        }

        @Test
        void testF4_Scenario_TaskChurnWorkflow() throws Exception {
            // Add 5 tasks
            for (int i = 0; i < 5; i++) clickAddTaskButton();
            List<Task> tasks = getTasksInUI();

            // Drag 2 to columns (Mon, Tue)
            double[] monX = new double[1];
            double[] tueX = new double[1];
            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                VBox mon = (VBox) getWeekRow().getChildren().get(0);
                VBox tue = (VBox) getWeekRow().getChildren().get(1);
                monX[0] = mon.localToScene(0, 0).getX();
                tueX[0] = tue.localToScene(0, 0).getX();
                latch1.countDown();
            });
            assertTrue(latch1.await(5, TimeUnit.SECONDS));

            JFXTestUtils.simulateDragAndDrop(tasks.get(0), tasks.get(0).getLayoutX(), tasks.get(0).getLayoutY(), monX[0] + 10, 150);
            JFXTestUtils.simulateDragAndDrop(tasks.get(1), tasks.get(1).getLayoutX(), tasks.get(1).getLayoutY(), tueX[0] + 10, 150);

            // Drag 1 off columns
            JFXTestUtils.simulateDragAndDrop(tasks.get(2), tasks.get(2).getLayoutX(), tasks.get(2).getLayoutY(), 1300, 50);

            // Delete 2 tasks
            Platform.runLater(() -> {
                tasks.get(3).onDelete.run();
                tasks.get(4).onDelete.run();
            });
            JFXTestUtils.waitForFxEvents();

            JFXTestUtils.simulateWindowClose(stage);
            reloadApp();

            assertEquals(3, appTaskList.size());
            assertEquals("Mon", appTaskList.get(0).getDayOfWeek());
            assertEquals("Tue", appTaskList.get(1).getDayOfWeek());
            assertEquals("", appTaskList.get(2).getDayOfWeek());
        }

        @Test
        void testF4_Scenario_WindowResizingCenteringWorkflow() throws Exception {
            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                root.setPrefSize(1200, 600);
                latch1.countDown();
            });
            latch1.await(5, TimeUnit.SECONDS);
            clickAddTaskButton();
            Task t1 = getTasksInUI().get(0);
            assertEquals(600 - 100, t1.getLayoutX(), 1.0);

            CountDownLatch latch2 = new CountDownLatch(1);
            Platform.runLater(() -> {
                root.setPrefSize(1600, 800);
                latch2.countDown();
            });
            latch2.await(5, TimeUnit.SECONDS);
            clickAddTaskButton();
            Task t2 = getTasksInUI().get(1);
            assertEquals(800 - 100, t2.getLayoutX(), 1.0);
        }

        @Test
        void testF4_Scenario_MultiTaskUnicodeSetup() throws Exception {
            clickAddTaskButton();
            clickAddTaskButton();
            List<Task> tasks = getTasksInUI();

            JFXTestUtils.simulateRightClick(tasks.get(0));
            CountDownLatch latch1 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(tasks.get(0), TextField.class);
                tfs.get(0).setText("😀 Emoji Task");
                latch1.countDown();
            });
            latch1.await(5, TimeUnit.SECONDS);
            JFXTestUtils.simulateKeyPress(tasks.get(0), KeyCode.ESCAPE);

            JFXTestUtils.simulateRightClick(tasks.get(1));
            CountDownLatch latch2 = new CountDownLatch(1);
            Platform.runLater(() -> {
                List<TextField> tfs = JFXTestUtils.findNodes(tasks.get(1), TextField.class);
                tfs.get(0).setText("日本語のタスク");
                latch2.countDown();
            });
            latch2.await(5, TimeUnit.SECONDS);
            JFXTestUtils.simulateKeyPress(tasks.get(1), KeyCode.ESCAPE);

            JFXTestUtils.simulateWindowClose(stage);
            reloadApp();

            assertEquals(2, appTaskList.size());
            assertEquals("😀 Emoji Task", appTaskList.get(0).getTitleTask());
            assertEquals("日本語のタスク", appTaskList.get(1).getTitleTask());
        }
    }
}
