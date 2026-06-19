package schedule.assist.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskModelTest {
    @Test
    void testNoArgConstructor() {
        TaskModel model = new TaskModel();
        assertEquals("Sự kiện", model.getTitleTask());
    }

    @Test
    void testAllGetters() {
        TaskModel model = new TaskModel("Title", "12:00", "Home", "Note", "Mon", 100.0, 200.0);
        assertEquals("Title", model.getTitleTask());
        assertEquals("12:00", model.getTimeOfTask());
        assertEquals("Home", model.getPlaceofTask());
        assertEquals("Note", model.getNoteOfTask());
        assertEquals("Mon", model.getDayOfWeek());
        assertEquals(100.0, model.getLayoutX());
        assertEquals(200.0, model.getLayoutY());
    }
}
