package schedule.assist.demo.ui;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeleteTaskButtonTest {

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
    void testStylingApplied() {
        DeleteTaskButton btn = new DeleteTaskButton();

        assertNotNull(btn.getStyle());
        assertFalse(btn.getStyle().isEmpty());
        assertNotNull(btn.getStyleClass());
    }
}
