package schedule.assist.demo.e2e;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.ui.HelloApplication;
import schedule.assist.demo.ui.Task;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JFXTestUtils {

    public static void waitForFxEvents() {
        if (Platform.isFxApplicationThread()) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void simulateButtonFire(Button button) {
        Platform.runLater(button::fire);
        waitForFxEvents();
    }

    public static void simulateRightClick(Node node) {
        Platform.runLater(() -> {
            MouseEvent rightClick = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.SECONDARY, 1,
                    false, false, false, false, false, true, false, false, false, true, null);
            Event.fireEvent(node, rightClick);
        });
        waitForFxEvents();
    }

    public static void simulateKeyPress(Node target, KeyCode code) {
        Platform.runLater(() -> {
            KeyEvent press = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
            Event.fireEvent(target, press);
        });
        waitForFxEvents();
    }

    public static void simulateDragAndDrop(Node node, double startX, double startY, double endX, double endY) {
        Platform.runLater(() -> {
            MouseEvent pressed = new MouseEvent(MouseEvent.MOUSE_PRESSED, startX, startY, startX, startY,
                    MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, true, null);
            Event.fireEvent(node, pressed);
        });
        waitForFxEvents();

        Platform.runLater(() -> {
            MouseEvent dragged = new MouseEvent(MouseEvent.MOUSE_DRAGGED, endX, endY, endX, endY, MouseButton.PRIMARY,
                    1, false, false, false, false, true, false, false, false, false, true, null);
            Event.fireEvent(node, dragged);
        });
        waitForFxEvents();

        Platform.runLater(() -> {
            MouseEvent released = new MouseEvent(MouseEvent.MOUSE_RELEASED, endX, endY, endX, endY, MouseButton.PRIMARY,
                    1, false, false, false, false, true, false, false, false, false, true, null);
            Event.fireEvent(node, released);
        });
        waitForFxEvents();
    }

    /**
     * Simulates a window close by invoking the save logic directly on the provided app instance, then closing the
     * stage. This avoids Platform.exit() (which would kill the test JVM) and the fragile Class.forName lookup of the
     * test class.
     */
    public static void simulateWindowClose(Stage stage, HelloApplication app) {
        Platform.runLater(() -> {
            javafx.event.EventHandler<WindowEvent> originalHandler = stage.getOnCloseRequest();
            stage.setOnCloseRequest(e -> {
                try {
                    if (app != null) {
                        Field taskListField = HelloApplication.class.getDeclaredField("taskList");
                        taskListField.setAccessible(true);
                        List<?> taskList = (List<?>) taskListField.get(app);

                        Field repoField = HelloApplication.class.getDeclaredField("taskRepository");
                        repoField.setAccessible(true);
                        Object taskRepository = repoField.get(app);

                        if (taskList != null && taskRepository != null) {
                            List<TaskModel> modelList = new ArrayList<>();
                            for (Object taskObj : taskList) {
                                if (taskObj instanceof Task t) {
                                    modelList.add(t.toModel());
                                }
                            }
                            // Delegate to repository — DataManager's internal guard handles
                            // the recoveredFromCorrupt + empty-list skip logic.
                            Method saveAllMethod = taskRepository.getClass().getMethod("saveAll", List.class);
                            saveAllMethod.invoke(taskRepository, modelList);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                stage.close();
            });
            Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            stage.setOnCloseRequest(originalHandler);
        });
        waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> List<T> findNodes(Parent parent, Class<T> clazz) {
        List<T> results = new ArrayList<>();
        if (clazz.isInstance(parent)) {
            results.add((T) parent);
        }
        findNodesRecursive(parent, clazz, results);
        return results;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Node> void findNodesRecursive(Parent parent, Class<T> clazz, List<T> results) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (clazz.isInstance(node)) {
                results.add((T) node);
            }
            if (node instanceof Parent) {
                findNodesRecursive((Parent) node, clazz, results);
            }
        }
    }
}
