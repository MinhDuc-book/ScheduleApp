package schedule.assist.demo.ui;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.service.TaskService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddTaskButtonTest {

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void action_createsTask_setsLayout_andTriggersSave() {
        boolean[] onChangeTriggered = { false };

        Task controlledTask = new Task();
        controlledTask.onChange = () -> {
            // The onChange should be triggered AFTER the layout is set
            assertTrue(controlledTask.getLayoutX() > 0, "LayoutX should be set before onChange runs");
            assertTrue(controlledTask.getLayoutY() > 0, "LayoutY should be set before onChange runs");
            onChangeTriggered[0] = true;
        };

        TaskService mockTaskService = new TaskService() {
            @Override
            public Task createTask() {
                return controlledTask;
            }

            @Override
            public void loadTask(TaskModel model) {
            }

            @Override
            public void deleteTask(Task task) {
            }

            @Override
            public void saveAll() {
            }

            @Override
            public List<Task> getAll() {
                return Collections.emptyList();
            }
        };

        AddTaskButton button = new AddTaskButton(mockTaskService);

        // Trigger the action manually
        button.fire();

        assertTrue(onChangeTriggered[0], "Task onChange callback was not executed");
        assertEquals(HelloApplication.SCREEN_W / 2 - 100, controlledTask.getLayoutX(), 0.1);
        assertEquals(HelloApplication.SCREEN_H / 2, controlledTask.getLayoutY(), 0.1);
    }

    @Test
    void action_whenParentIsNull_usesConstantsAsFallback() {
        Task controlledTask = new Task();

        TaskService mockTaskService = new TaskService() {
            @Override
            public Task createTask() {
                return controlledTask;
            }

            @Override
            public void loadTask(TaskModel m) {
            }

            @Override
            public void deleteTask(Task t) {
            }

            @Override
            public void saveAll() {
            }

            @Override
            public List<Task> getAll() {
                return Collections.emptyList();
            }
        };

        AddTaskButton button = new AddTaskButton(mockTaskService);
        // Explicitly verify the button has no parent — this is the precondition for the fallback path.
        assertNull(button.getParent(), "Button must not be in a scene graph for this test");

        button.fire();

        assertEquals(HelloApplication.SCREEN_W / 2.0 - 100, controlledTask.getLayoutX(), 0.1,
                "LayoutX must fall back to SCREEN_W / 2 - 100 when parent is null");
        assertEquals(HelloApplication.SCREEN_H / 2.0, controlledTask.getLayoutY(), 0.1,
                "LayoutY must fall back to SCREEN_H / 2 when parent is null");
    }
}
