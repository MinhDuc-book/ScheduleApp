package schedule.assist.demo.service;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ListSelectionView;
import schedule.assist.demo.ui.Task;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.repository.TaskRepository;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskServiceImpl implements TaskService {
    private AnchorPane root;
    private List<Task> taskList = new ArrayList<>();
    private TaskRepository repository;

    private static final double TASK_HEIGHT = 100; // chiều cao cố định + khoảng cách giữa các task
    private static final double START_Y = 90;

    public TaskServiceImpl(AnchorPane root, TaskRepository repository, List<Task> taskList) {
        this.root = root;
        this.repository = repository;
        this.taskList = taskList;
    }

    // create Task card
    @Override
    public Task createTask() {
        Task task = new Task();
        task.onDelete = () -> {deleteTask(task);};
        task.onSnap = () -> {reflowColumn(task.getDayOfWeek());};
        root.getChildren().add(task);
        taskList.add(task);

        return task;
    }

    private void reflowColumn(String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.isEmpty()) {
            return;
        }

        List<Task> taskInColumn = new ArrayList<>();
        for (Task t : taskList) {
            if (t.getDayOfWeek().equals(dayOfWeek)) {
                taskInColumn.add(t);
            }
        }
        taskInColumn.sort(Comparator.comparingInt(t -> timeToMinute(t.getTimeOfTask())));

        double y = START_Y;

        for (Task t : taskInColumn) {
            t.setLayoutY(y);
            y = y + TASK_HEIGHT;
        }
    }

    private int timeToMinute(String timeOfTask) {
        try {
            String[] parts = timeOfTask.split(":");
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            return hour*60 + minute;
        } catch (Exception e) {
            return 0;
        }

    }

    private void positionTaskInColumn(Task task, String dayOfWeek, double layoutY) {
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for (var node : root.getChildren()) {
            if (node instanceof HBox weekRow) {
                int index = 0;
                for (var col : weekRow.getChildren()) {
                    if (col instanceof VBox column) {
                        if (dayNames[index].equals(dayOfWeek)) {
                            // Tính X để task nằm giữa cột
                            double colX = column.localToScene(0, 0).getX();
                            double snapX = colX + (column.getWidth() - task.getPrefWidth()) / 2;
                            task.setLayoutX(snapX);
                            task.setLayoutY(layoutY);
                            return;
                        }
                        index++;
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
        task.onDelete = () -> {deleteTask(task);};    // gắn callback xóa
        task.onSnap = () -> {reflowColumn(task.getDayOfWeek());};
        root.getChildren().add(task);
        taskList.add(task);

        if (model.getDayOfWeek() != null && !model.getDayOfWeek().isEmpty()) {
            // Dùng Platform.runLater vì layout chưa tính xong khi load
            javafx.application.Platform.runLater(() -> {
                positionTaskInColumn(task, model.getDayOfWeek(), model.getLayoutY());
                reflowColumn(model.getDayOfWeek());
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
        String day = task.getDayOfWeek();
        root.getChildren().remove(task);
        taskList.remove(task);
        if (day != null && !day.isEmpty()) {
            reflowColumn(day); //task khác dồn lên sau khi xóa
        }
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

    @Override
    public List<Task> getAll() {
        return taskList;
    }
}
