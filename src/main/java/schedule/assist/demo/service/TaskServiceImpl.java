package schedule.assist.demo.service;

import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.ListSelectionView;
import schedule.assist.demo.ui.Task;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.repository.TaskRepository;

import javax.swing.*;
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
        root.getChildren().add(task);
        taskList.add(task);

        return task;
    }

    // load Task card from file with same attribute
    @Override
    public void loadTask(TaskModel model) {
        Task task = new Task();
        task.setTitleTask(model.getTitleTask());
        task.setTimeOfTask(model.getTimeOfTask());
        task.setPlaceofTask(model.getPlaceofTask());
        task.setNoteOfTask(model.getNoteOfTask());
        task.setLayoutX(model.getLayoutX());
        task.setLayoutY(model.getLayoutY());
        task.onDelete = () -> {deleteTask(task);};    // gắn callback xóa
        root.getChildren().add(task);
        taskList.add(task);
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
