package schedule.assist.demo.service;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import schedule.assist.demo.ui.Task;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class TaskServiceImpl implements TaskService {
    private AnchorPane root;
    private List<Task> taskList = new ArrayList<>();
    private TaskRepository repository;

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
        task.onChange = this::saveAll;
        root.getChildren().add(task);
        taskList.add(task);

        return task;
    }

    private void positionTaskInColumn(Task task, String dayOfWeek, double layoutY) {
        for (var node : root.getChildren()) {
            if (node instanceof HBox weekRow) {
                for (var col : weekRow.getChildren()) {
                    if (col instanceof VBox column) {
                        Object userData = column.getUserData();
                        if (userData == null) continue;

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
        task.onDelete = () -> {deleteTask(task);};    // gắn callback xóa
        task.onChange = this::saveAll;
        root.getChildren().add(task);
        taskList.add(task);

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

    @Override
    public List<Task> getAll() {
        return taskList;
    }
}
