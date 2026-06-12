package schedule.assist.demo.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import schedule.assist.demo.ui.DeleteTaskButton;
import schedule.assist.demo.model.TaskModel;

public class Task extends VBox {

    private double dragOffsetX, dragOffsetY;

    protected String titleTask = "Event";
    protected String timeOfTask = "10:00";
    protected String noteOfTask = "Note";
    protected String placeofTask = "GĐ3";

    protected static final double NORMAL_SPACING = 4;
    protected static final double EDIT_SPACING = 12;

    public Label titleLabel;
    public Label timeLabel;
    public Label placeLabel;

    public Runnable onDelete = () -> {};

    public final String BASE_STYLE = "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 16;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;";

    public final String SCALE_STYLE = "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 20;" +
            "-fx-border-color: #4A90E2;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: default;";



    public Task() {
        setupUITask();
    }

    public void setTitleTask(String t) {
        this.titleTask = t;
        if (titleLabel != null) titleLabel.setText("📅 " + t);
    }

    public void setTimeOfTask(String t) {
        this.timeOfTask = t;
        if (timeLabel != null) timeLabel.setText("🕐 " + t);
    }

    public void setPlaceofTask(String n) {
        this.placeofTask = n;
        if (placeLabel != null) placeLabel.setText("📍 " + n);
    }

    public void setNoteOfTask(String n) {
        this.noteOfTask = n;
    }

    public String getTitleTask() { return this.titleTask; }
    public String getTimeOfTask() { return this.timeOfTask; }
    public String getNoteOfTask() { return this.noteOfTask; }
    public String getPlaceofTask() { return this.placeofTask; }

    private void bringToTop() {
        AnchorPane parent = (AnchorPane) this.getParent();
        if (parent != null) {
            parent.getChildren().remove(this);
            parent.getChildren().add(this);
        }
    }

    public Task setupUITask() {
        titleLabel = new Label("📅 " + titleTask);
        timeLabel = new Label("🕐 " + timeOfTask);
        placeLabel = new Label("📍 " + placeofTask);

        this.setPrefSize(160, 85);
        this.setStyle(BASE_STYLE);
        this.setSpacing(NORMAL_SPACING);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.15));
        shadow.setOffsetY(4);
        shadow.setRadius(12);
        this.setEffect(shadow);

        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        timeLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #555;");
        placeLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #555;");

        this.getChildren().setAll(titleLabel, timeLabel, placeLabel);

        // drag
        this.setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;

            dragOffsetX = e.getSceneX() - this.getLayoutX();
            dragOffsetY = e.getSceneY() - this.getLayoutY();
            this.setStyle(BASE_STYLE.replace("-fx-cursor: hand;", "-fx-cursor: closed-hand;"));

            // Bring to front
            bringToTop();

        });

        this.setOnMouseDragged(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            this.setLayoutX(e.getSceneX() - dragOffsetX);
            this.setLayoutY(e.getSceneY() - dragOffsetY);
        });

        this.setOnMouseReleased(e -> {
            this.setStyle(BASE_STYLE);
        });

        // edit
        this.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                bringToTop();
                new TaskEditor(this).expandCardToEditor();
            }
        });

        return this;
    }

    public TaskModel toModel() {
        return new  TaskModel(titleTask, timeOfTask, placeofTask, noteOfTask,
                this.getLayoutX(), this.getLayoutY());
    }
}