package schedule.assist.demo.service;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import schedule.assist.demo.ui.Task;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskServiceImpl implements TaskService {
    private static final double VERTICAL_GAP = 10;
    private static final double DEFAULT_START_Y = 60;

    private AnchorPane root;
    private List<Task> taskList = new ArrayList<>();
    private TaskRepository repository;

    public TaskServiceImpl(AnchorPane root, TaskRepository repository, List<Task> taskList) {
        this.root = root;
        this.repository = repository;
        this.taskList = taskList;
    }

    private void registerTask(Task task) {
        task.onDelete = () -> deleteTask(task);
        task.onChange = this::saveAll;
        task.onSort   = () -> sortColumn(task.getDayOfWeek());
        root.getChildren().add(task);
        taskList.add(task);
    }

    // create Task card
    @Override
    public Task createTask() {
        Task task = new Task();
        registerTask(task);
        return task;
    }

    private void positionTaskInColumn(Task task, String dayOfWeek, double layoutY) {
        for (var node : root.getChildren()) {
            if (node instanceof HBox weekRow) {
                for (var col : weekRow.getChildren()) {
                    if (col instanceof VBox column) {
                        Object userData = column.getUserData();
                        if (userData == null)
                            continue;

                        if (userData.toString().equals(dayOfWeek)) {
                            // Tính X để task nằm giữa cột
                            double colX = column.localToScene(0, 0).getX();
                            double snapX = colX + (column.getWidth() - task.getPrefWidth()) / 2;
                            task.setLayoutX(snapX);
                            task.setLayoutY(layoutY);
                            return;
                        }
                    }
                }
            }
        }
    }

    // load Task card from file with same attribute
    @Override
    public void loadTask(TaskModel model) {
        Task task = new Task();
        task.setTitleTask(model.getTitleTask());
        task.setTimeOfTask(model.getTimeOfTask());
        task.setPlaceofTask(model.getPlaceofTask());
        task.setNoteOfTask(model.getNoteOfTask());
        task.setDayOfWeek(model.getDayOfWeek());
        registerTask(task);

        if (model.getDayOfWeek() != null && !model.getDayOfWeek().isEmpty()) {
            // Dùng Platform.runLater vì layout chưa tính xong khi load
            javafx.application.Platform.runLater(() -> {
                positionTaskInColumn(task, model.getDayOfWeek(), model.getLayoutY());
            });
        } else {
            // Không có cột thì dùng tọa độ cũ
            task.setLayoutX(model.getLayoutX());
            task.setLayoutY(model.getLayoutY());
        }
    }

    // delete Task -> update delete to TaskModel(which use for Model in MVC)
    @Override
    public void deleteTask(Task task) {
        root.getChildren().remove(task);
        taskList.remove(task);
        saveAll();
    }

    @Override
    public void saveAll() {
        List<TaskModel> modelList = new ArrayList<>();
        for (Task task : taskList) {
            modelList.add(task.toModel());
        }
        repository.saveAll(modelList);
    }

    /**
     * Sorts all tasks assigned to {@code dayOfWeek} by their normalised HH:mm time and repositions their layoutY so
     * they stack neatly in the column. Tasks with no day assigned, or a day different from {@code dayOfWeek}, are
     * untouched.
     */
    private void sortColumn(String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.isEmpty())
            return;

        // Collect tasks belonging to this column
        List<Task> colTasks = new ArrayList<>();
        for (Task t : taskList) {
            if (dayOfWeek.equals(t.getDayOfWeek())) {
                colTasks.add(t);
            }
        }
        if (colTasks.size() < 2)
            return;

        // Sort by time string — safe because times are normalised to HH:mm
        colTasks.sort(Comparator.comparing(Task::getTimeOfTask));

        // Stack vertically: start from the same Y as the topmost task currently is
        double startY = colTasks.stream().mapToDouble(Task::getLayoutY).min().orElse(DEFAULT_START_Y);
        double gap = VERTICAL_GAP; // vertical gap between cards
        double cardH = colTasks.get(0).getPrefHeight();

        for (int i = 0; i < colTasks.size(); i++) {
            colTasks.get(i).setLayoutY(startY + i * (cardH + gap));
        }
    }

    @Override
    public List<Task> getAll() {
        return taskList;
    }
}
