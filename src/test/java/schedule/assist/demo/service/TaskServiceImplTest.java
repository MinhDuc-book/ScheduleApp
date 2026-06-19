package schedule.assist.demo.service;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.repository.TaskRepository;
import schedule.assist.demo.ui.Task;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TaskServiceImplTest {

    @BeforeAll
    static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void testPositionTaskInColumn_withExtraVBox_doesNotThrow() {
        AnchorPane root = new AnchorPane();
        HBox weekRow = new HBox();
        root.getChildren().add(weekRow);

        String[] dayNames = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (String day : dayNames) {
            VBox col = new VBox();
            col.setUserData(day);
            weekRow.getChildren().add(col);
        }

        VBox extraCol = new VBox();
        weekRow.getChildren().add(extraCol);

        TaskRepository dummyRepo = new TaskRepository() {
            @Override
            public List<TaskModel> loadAll() {
                return Collections.emptyList();
            }

            @Override
            public void saveAll(List<TaskModel> tasks) {
            }
        };

        TaskServiceImpl service = new TaskServiceImpl(root, dummyRepo, new ArrayList<>());

        try {
            Method method = TaskServiceImpl.class.getDeclaredMethod("positionTaskInColumn", Task.class, String.class,
                    double.class);
            method.setAccessible(true);
            Task task = new Task();
            assertDoesNotThrow(() -> {
                method.invoke(service, task, "Mon", 0.0);
            });
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Reflection setup failed: " + e.getMessage());
        }
    }

    @Test
    void saveAll_iteratesTaskList_callsRepository() {
        List<TaskModel> savedModels = new ArrayList<>();
        TaskRepository mockRepo = new TaskRepository() {
            @Override
            public List<TaskModel> loadAll() {
                return Collections.emptyList();
            }

            @Override
            public void saveAll(List<TaskModel> tasks) {
                savedModels.addAll(tasks);
            }
        };

        AnchorPane root = new AnchorPane();
        List<Task> taskList = new ArrayList<>();
        TaskServiceImpl service = new TaskServiceImpl(root, mockRepo, taskList);

        Task t1 = new Task();
        t1.setTitleTask("T1");
        Task t2 = new Task();
        t2.setTitleTask("T2");
        taskList.add(t1);
        taskList.add(t2);

        service.saveAll();

        org.junit.jupiter.api.Assertions.assertEquals(2, savedModels.size());
        org.junit.jupiter.api.Assertions.assertEquals("T1", savedModels.get(0).getTitleTask());
        org.junit.jupiter.api.Assertions.assertEquals("T2", savedModels.get(1).getTitleTask());
    }

    @Test
    void saveAll_emptyList_passesEmptyToRepository() {
        List<TaskModel> savedModels = new ArrayList<>();
        TaskRepository mockRepo = new TaskRepository() {
            @Override
            public List<TaskModel> loadAll() {
                return Collections.emptyList();
            }

            @Override
            public void saveAll(List<TaskModel> tasks) {
                savedModels.addAll(tasks);
            }
        };

        TaskServiceImpl service = new TaskServiceImpl(new AnchorPane(), mockRepo, new ArrayList<>());

        service.saveAll();

        org.junit.jupiter.api.Assertions.assertTrue(savedModels.isEmpty());
    }

    @Test
    void createTask_addsToListAndBindsOnChangeCallback() {
        boolean[] saveCalled = { false };
        List<TaskModel> savedModels = new ArrayList<>();
        TaskRepository mockRepo = new TaskRepository() {
            @Override
            public List<TaskModel> loadAll() {
                return Collections.emptyList();
            }

            @Override
            public void saveAll(List<TaskModel> tasks) {
                saveCalled[0] = true;
                savedModels.addAll(tasks);
            }
        };

        AnchorPane root = new AnchorPane();
        List<Task> taskList = new ArrayList<>();
        TaskServiceImpl service = new TaskServiceImpl(root, mockRepo, taskList);

        Task createdTask = service.createTask();

        org.junit.jupiter.api.Assertions.assertNotNull(createdTask);
        org.junit.jupiter.api.Assertions.assertTrue(taskList.contains(createdTask));
        org.junit.jupiter.api.Assertions.assertTrue(root.getChildren().contains(createdTask));

        org.junit.jupiter.api.Assertions.assertNotNull(createdTask.onChange);

        createdTask.onChange.run();
        org.junit.jupiter.api.Assertions.assertTrue(saveCalled[0]);
        org.junit.jupiter.api.Assertions.assertEquals(1, savedModels.size());
        org.junit.jupiter.api.Assertions.assertEquals("Event", savedModels.get(0).getTitleTask());
    }

    @Test
    void deleteTask_removesFromListAndTriggersSave() {
        boolean[] saveCalled = { false };
        TaskRepository mockRepo = new TaskRepository() {
            @Override
            public List<TaskModel> loadAll() {
                return Collections.emptyList();
            }

            @Override
            public void saveAll(List<TaskModel> tasks) {
                saveCalled[0] = true;
            }
        };

        AnchorPane root = new AnchorPane();
        List<Task> taskList = new ArrayList<>();
        TaskServiceImpl service = new TaskServiceImpl(root, mockRepo, taskList);

        Task task = new Task();
        root.getChildren().add(task);
        taskList.add(task);

        service.deleteTask(task);

        org.junit.jupiter.api.Assertions.assertFalse(taskList.contains(task));
        org.junit.jupiter.api.Assertions.assertFalse(root.getChildren().contains(task));
        org.junit.jupiter.api.Assertions.assertTrue(saveCalled[0]);
    }

    @Test
    void loadTask_setsAllFieldsAndAddsToListAndRoot() throws Exception {
        AnchorPane root = new AnchorPane();
        HBox weekRow = new HBox();
        VBox monCol = new VBox();
        monCol.setUserData("Mon");
        weekRow.getChildren().add(monCol);
        root.getChildren().add(weekRow);

        List<Task> taskList = new ArrayList<>();
        TaskRepository mockRepo = new TaskRepository() {
            @Override
            public List<TaskModel> loadAll() {
                return Collections.emptyList();
            }

            @Override
            public void saveAll(List<TaskModel> tasks) {
            }
        };

        TaskServiceImpl service = new TaskServiceImpl(root, mockRepo, taskList);
        TaskModel model = new TaskModel("Loaded", "09:00", "Home", "Note", "Mon", 50.0, 100.0);

        service.loadTask(model);

        // Task immediately added to list and root
        org.junit.jupiter.api.Assertions.assertEquals(1, taskList.size());
        org.junit.jupiter.api.Assertions.assertTrue(root.getChildren().contains(taskList.get(0)));

        // Fields propagated to the Task object
        Task loaded = taskList.get(0);
        org.junit.jupiter.api.Assertions.assertEquals("Loaded", loaded.getTitleTask());
        org.junit.jupiter.api.Assertions.assertEquals("09:00", loaded.getTimeOfTask());
        org.junit.jupiter.api.Assertions.assertEquals("Home", loaded.getPlaceofTask());
        org.junit.jupiter.api.Assertions.assertEquals("Note", loaded.getNoteOfTask());
        org.junit.jupiter.api.Assertions.assertEquals("Mon", loaded.getDayOfWeek());

        // Platform.runLater for column-snap was scheduled; drain the FX queue to let it run
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        javafx.application.Platform.runLater(latch::countDown);
        org.junit.jupiter.api.Assertions.assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS),
                "Platform.runLater did not complete");
    }

    @Test
    void loadTask_noDayOfWeek_usesStoredCoordinates() {
        AnchorPane root = new AnchorPane();
        List<Task> taskList = new ArrayList<>();
        TaskRepository mockRepo = new TaskRepository() {
            @Override
            public List<TaskModel> loadAll() {
                return Collections.emptyList();
            }

            @Override
            public void saveAll(List<TaskModel> tasks) {
            }
        };

        TaskServiceImpl service = new TaskServiceImpl(root, mockRepo, taskList);
        TaskModel model = new TaskModel("Free", "10:00", "School", "Note", "", 77.0, 88.0);

        service.loadTask(model);

        Task loaded = taskList.get(0);
        org.junit.jupiter.api.Assertions.assertEquals(77.0, loaded.getLayoutX(), 0.001,
                "LayoutX should come from the stored model when dayOfWeek is empty");
        org.junit.jupiter.api.Assertions.assertEquals(88.0, loaded.getLayoutY(), 0.001,
                "LayoutY should come from the stored model when dayOfWeek is empty");
    }
}
