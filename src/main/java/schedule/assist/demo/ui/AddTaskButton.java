package schedule.assist.demo.ui;

import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.service.TaskService;
import schedule.assist.demo.ui.Task;
import schedule.assist.demo.util.DataManager;

import java.util.ArrayList;
import java.util.List;

import static schedule.assist.demo.ui.HelloApplication.SCREEN_H;
import static schedule.assist.demo.ui.HelloApplication.SCREEN_W;

public class AddTaskButton extends Button {
    private double dragOffsetX, dragOffsetY;
    private static final double BUTTON_SIZE = 60.0;
    private final String DEFAULT_STYLE =
            "-fx-background-color: #4A90E2;" +
                    "-fx-background-radius: 30;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 28;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 0;";
    private final String HOVER_STYLE =
            "-fx-background-color: #357ABD;" +
                    "-fx-background-radius: 30;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 28;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 0;";

    public AddTaskButton(TaskService taskService) {
        super("+");

        this.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);

        this.setStyle(DEFAULT_STYLE);
        this.setOnMouseEntered(e -> this.setStyle(HOVER_STYLE));
        this.setOnMouseExited(e -> this.setStyle(DEFAULT_STYLE));

        // Đổ bóng cho nút
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.25));
        shadow.setOffsetY(4);
        shadow.setRadius(10);
        this.setEffect(shadow);

        this.setOnAction(e -> {
            Task task = taskService.createTask();
            task.setLayoutX(SCREEN_W / 2 - 100);
            task.setLayoutY(SCREEN_H / 2);
        });
    }


}