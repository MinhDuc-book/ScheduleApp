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
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void testPositionTaskInColumn_withExtraVBox_doesNotThrow() {
        AnchorPane root = new AnchorPane();
        HBox weekRow = new HBox();
        root.getChildren().add(weekRow);

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : dayNames) {
            VBox col = new VBox();
            col.setUserData(day);
            weekRow.getChildren().add(col);
        }

        VBox extraCol = new VBox();
        weekRow.getChildren().add(extraCol);

        TaskRepository dummyRepo = new TaskRepository() {
            @Override public List<TaskModel> loadAll() { return Collections.emptyList(); }
            @Override public void saveAll(List<TaskModel> tasks) {}
        };

        TaskServiceImpl service = new TaskServiceImpl(root, dummyRepo, new ArrayList<>());

        try {
            Method method = TaskServiceImpl.class.getDeclaredMethod("positionTaskInColumn", Task.class, String.class, double.class);
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
            @Override public List<TaskModel> loadAll() { return Collections.emptyList(); }
            @Override public void saveAll(List<TaskModel> tasks) { savedModels.addAll(tasks); }
        };

        AnchorPane root = new AnchorPane();
        List<Task> taskList = new ArrayList<>();
        TaskServiceImpl service = new TaskServiceImpl(root, mockRepo, taskList);

        Task t1 = new Task(); t1.setTitleTask("T1");
        Task t2 = new Task(); t2.setTitleTask("T2");
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
            @Override public List<TaskModel> loadAll() { return Collections.emptyList(); }
            @Override public void saveAll(List<TaskModel> tasks) { savedModels.addAll(tasks); }
        };

        TaskServiceImpl service = new TaskServiceImpl(new AnchorPane(), mockRepo, new ArrayList<>());
        
        service.saveAll();

        org.junit.jupiter.api.Assertions.assertTrue(savedModels.isEmpty());
    }

    @Test
    void createTask_addsToListAndBindsOnChangeCallback() {
        boolean[] saveCalled = {false};
        List<TaskModel> savedModels = new ArrayList<>();
        TaskRepository mockRepo = new TaskRepository() {
            @Override public List<TaskModel> loadAll() { return Collections.emptyList(); }
            @Override public void saveAll(List<TaskModel> tasks) { 
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
        boolean[] saveCalled = {false};
        TaskRepository mockRepo = new TaskRepository() {
            @Override public List<TaskModel> loadAll() { return Collections.emptyList(); }
            @Override public void saveAll(List<TaskModel> tasks) { saveCalled[0] = true; }
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
}
