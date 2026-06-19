package schedule.assist.demo.ui;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import schedule.assist.demo.model.TaskModel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Task}, covering the toModel() serialisation path and coordinate preservation.
 */
class TaskTest {

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    /**
     * Verifies that toModel() captures the exact layoutX/layoutY values so that a rename of the underlying fields would
     * cause a test failure before hitting production.
     */
    @Test
    void toModel_preservesCoordinates() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] passed = { false };

        Platform.runLater(() -> {
            Task task = new Task();
            task.setLayoutX(123.5);
            task.setLayoutY(456.75);

            TaskModel model = task.toModel();

            assertEquals(123.5, model.getLayoutX(), 0.001, "layoutX must survive toModel()");
            assertEquals(456.75, model.getLayoutY(), 0.001, "layoutY must survive toModel()");
            passed[0] = true;
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(passed[0]);
    }

    /** toModel() must also preserve all string fields (guard against JSON field renames). */
    @Test
    void toModel_preservesAllFields() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] passed = { false };

        Platform.runLater(() -> {
            Task task = new Task();
            task.setTitleTask("Meeting");
            task.setTimeOfTask("14:30");
            task.setPlaceofTask("Home");
            task.setNoteOfTask("Bring laptop");
            task.setDayOfWeek("Wed");
            task.setLayoutX(10.0);
            task.setLayoutY(20.0);

            TaskModel m = task.toModel();

            assertEquals("Meeting", m.getTitleTask());
            assertEquals("14:30", m.getTimeOfTask());
            assertEquals("Home", m.getPlaceofTask());
            assertEquals("Bring laptop", m.getNoteOfTask());
            assertEquals("Wed", m.getDayOfWeek());
            assertEquals(10.0, m.getLayoutX(), 0.001);
            assertEquals(20.0, m.getLayoutY(), 0.001);
            passed[0] = true;
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(passed[0]);
    }
}
