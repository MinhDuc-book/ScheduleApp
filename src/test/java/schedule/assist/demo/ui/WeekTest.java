package schedule.assist.demo.ui;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WeekTest {

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
    void testCreateWeekView() {
        Week week = new Week();
        HBox weekView = week.createWeekView();

        assertEquals(7, weekView.getChildren().size());

        String[] expectedDays = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int i = 0; i < 7; i++) {
            VBox col = (VBox) weekView.getChildren().get(i);
            assertNotNull(col.getUserData());
            assertEquals(expectedDays[i], col.getUserData().toString());
        }
    }
}
