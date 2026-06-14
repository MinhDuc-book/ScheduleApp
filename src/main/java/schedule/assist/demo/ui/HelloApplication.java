package schedule.assist.demo.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import schedule.assist.demo.service.TaskServiceImpl;
import schedule.assist.demo.ui.AddTaskButton;
import schedule.assist.demo.ui.Week;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.repository.JsonTaskRepository;
import schedule.assist.demo.repository.TaskRepository;
import schedule.assist.demo.service.TaskService;
import schedule.assist.demo.util.DataManager;

import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {

    public static final int SCREEN_W = 1400;
    public static final int SCREEN_H = 650;

    private AnchorPane root;
    private List<Task> taskList = new ArrayList<>();
    private TaskRepository taskRepository = new JsonTaskRepository();


    @Override
    public void start(Stage stage) {
        root = new AnchorPane();
        root.setStyle("-fx-background-color: #0A192F;");

        TaskService taskService = new TaskServiceImpl(root, taskRepository, taskList);

        Week week = new Week();
        AddTaskButton buttonAddTask = new AddTaskButton(taskService);

        HBox weekCard = week.createWeekView();
        root.getChildren().add(weekCard);

        // load Task from file
        for (TaskModel model : taskRepository.loadAll()) {
            taskService.loadTask(model);
        }

        //vị trí ban đầu của nút
        buttonAddTask.setLayoutX(1370);
        buttonAddTask.setLayoutY(SCREEN_H - 550);
        root.getChildren().add(buttonAddTask);

        stage.setScene(new Scene(root));
        stage.setMaximized(true);
        stage.setTitle("Schedule Assistant");
        stage.show();

        // Khi đóng app — save toàn bộ list
        stage.setOnCloseRequest(e -> {
            taskService.saveAll();
        });
    }
}