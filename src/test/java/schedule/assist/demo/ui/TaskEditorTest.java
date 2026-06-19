package schedule.assist.demo.ui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskEditor, specifically targeting the saveAndCollapse path that was producing a spurious "📅 " emoji
 * prefix on the title label.
 */
class TaskEditorTest {

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
     * Regression test for Bug #1: after a right-click → edit → ESC cycle, {@code task.titleLabel.getText()} must equal
     * {@code task.getTitleTask()} with no extra prefix.
     */
    @Test
    void saveAndCollapse_titleLabelMatchesTitleTask() throws Exception {
        Task[] holder = { null };

        // 1. Expand editor on FX thread
        CountDownLatch expandLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Task task = new Task();
            holder[0] = task;
            new TaskEditor(task).expandCardToEditor();
            expandLatch.countDown();
        });
        assertTrue(expandLatch.await(5, TimeUnit.SECONDS), "Expand did not complete");

        // 2. Wait for the 500 ms expand animation to finish
        Thread.sleep(600);

        // 3. Fire ESC to trigger saveAndCollapse
        CountDownLatch escLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            KeyEvent esc = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);
            Event.fireEvent(holder[0], esc);
            escLatch.countDown();
        });
        assertTrue(escLatch.await(5, TimeUnit.SECONDS), "ESC dispatch did not complete");

        // 4. Wait for the 300 ms collapse animation to finish
        Thread.sleep(400);

        // 5. Assert on FX thread
        CountDownLatch checkLatch = new CountDownLatch(1);
        boolean[] passed = { false };
        Platform.runLater(() -> {
            Task task = holder[0];
            passed[0] = task.getTitleTask().equals(task.titleLabel.getText());
            checkLatch.countDown();
        });
        assertTrue(checkLatch.await(5, TimeUnit.SECONDS), "Assertion latch timed out");
        assertTrue(passed[0], "titleLabel.getText() must equal getTitleTask() — no emoji prefix should be added");
    }
}
