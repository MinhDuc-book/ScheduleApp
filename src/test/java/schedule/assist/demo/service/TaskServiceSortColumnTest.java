package schedule.assist.demo.service;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import schedule.assist.demo.repository.JsonTaskRepository;
import schedule.assist.demo.ui.Task;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskServiceSortColumnTest {

    @BeforeAll
    static void initJfx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void testSortColumn() throws Exception {
        AnchorPane root = new AnchorPane();
        List<Task> taskList = new ArrayList<>();
        TaskServiceImpl service = new TaskServiceImpl(root, new JsonTaskRepository(), taskList);

        Task t1 = service.createTask();
        t1.setDayOfWeek("Mon");
        t1.setTimeOfTask("10:00");
        t1.setLayoutY(100);

        Task t2 = service.createTask();
        t2.setDayOfWeek("Mon");
        t2.setTimeOfTask("09:00");
        t2.setLayoutY(200);

        Method sortColumn = TaskServiceImpl.class.getDeclaredMethod("sortColumn", String.class);
        sortColumn.setAccessible(true);
        sortColumn.invoke(service, "Mon");

        // t2 is 09:00, t1 is 10:00
        // they should be sorted such that t2 is first, starting at the minimum Y (100)
        assertTrue(t2.getLayoutY() < t1.getLayoutY());
        assertEquals(100.0, t2.getLayoutY());
        assertEquals(100.0 + t2.getPrefHeight() + 10.0, t1.getLayoutY());
    }
}
