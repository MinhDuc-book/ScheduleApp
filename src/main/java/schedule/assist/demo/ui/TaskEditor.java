package schedule.assist.demo.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import schedule.assist.demo.ui.DeleteTaskButton;

public class TaskEditor{
    private Task task;

    private String fieldStyle() {
        return "-fx-background-color: #f7f8fa;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #dde1e7;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 8 12;" +
                "-fx-font-size: 13;";
    }

    public TaskEditor(Task task) {
        this.task = task;
    }
    public void expandCardToEditor() {

        task.getChildren().clear();

        TextField titleField = new TextField(task.titleTask);
        TextField timeField = new TextField(task.timeOfTask);
        TextField placeField = new TextField(task.placeofTask);
        TextArea noteArea = new TextArea(task.noteOfTask);

        autoFillField(titleField, timeField, placeField, noteArea);

        Label heading = new Label("       ");
        heading.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        DeleteTaskButton deleteTaskButton = new DeleteTaskButton();
        deleteTaskButton.setOnAction(e -> task.onDelete.run());

        HBox header = new HBox(10, heading, deleteTaskButton);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        task.getChildren().addAll(
                header,
                labeledField("Tiêu đề", titleField),
                labeledField("Thời gian", timeField),
                labeledField("Địa điểm", placeField),
                labeledField("Ghi chú", noteArea)
        );

        task.setStyle(task.SCALE_STYLE);
        task.setSpacing(task.EDIT_SPACING);

        // Animation expand
        ScaleTransition expand = new ScaleTransition(Duration.millis(300), task);
        expand.setFromX(1.0);
        expand.setFromY(1.0);
        expand.setToX(1.1);
        expand.setToY(1.1);
        expand.play();

        expand.setOnFinished(ev -> titleField.requestFocus());

        // ESC handler
        setupEscapeHandlers(titleField, timeField, placeField, noteArea);
    }

    private void setupEscapeHandlers(TextField titleField, TextField timeField,
                                     TextField placeField, TextArea noteArea) {
        // Task level
        task.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                saveAndCollapse(titleField, timeField, placeField, noteArea);
            }
        });

        // Fields level
        for (Control field : new Control[]{titleField, timeField, placeField, noteArea}) {
            field.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    saveAndCollapse(titleField, timeField, placeField, noteArea);
                }
            });
        }

        task.setFocusTraversable(true);
        task.requestFocus();
    }

    private void saveAndCollapse(TextField titleField, TextField timeField,
                                 TextField placeField, TextArea noteArea) {


        if (!titleField.getText().trim().isEmpty()) task.titleTask = titleField.getText().trim();
        if (!timeField.getText().trim().isEmpty()) task.timeOfTask = timeField.getText().trim();
        if (!placeField.getText().trim().isEmpty()) task.placeofTask = placeField.getText().trim();
        task.setColorFromPlace();
        task.noteOfTask = noteArea.getText().trim();

        // Animation collapse
        ScaleTransition collapse = new ScaleTransition(Duration.millis(300), task);
        collapse.setFromX(1.1);
        collapse.setFromY(1.1);
        collapse.setToX(1.0);
        collapse.setToY(1.0);

        FadeTransition fade = new FadeTransition(Duration.millis(150), task);
        fade.setFromValue(1.0);
        fade.setToValue(1.0);

        collapse.play();
        fade.play();

        collapse.setOnFinished(ev -> {
            task.getChildren().clear();

            task.titleLabel.setText("📅 " + task.titleTask);
            task.timeLabel.setText("🕐 " + task.timeOfTask);
            task.placeLabel.setText("📍 " + task.placeofTask);



            task.getChildren().setAll(task.titleLabel, task.timeLabel, task.placeLabel);
            task.setStyle(task.setColorFromPlace());
            task.setSpacing(task.NORMAL_SPACING);

            // Reset scale
            task.setScaleX(1.0);
            task.setScaleY(1.0);
        });

    }

    private VBox labeledField(String labelText, Control field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #666; -fx-font-weight: bold;");
        VBox vbox = new VBox(5, lbl, field);
        vbox.setPadding(new Insets(2, 0, 4, 0));
        return vbox;
    }

    private void autoFillField(TextField titleField, TextField timeField,
                               TextField placeField, TextArea noteArea) {
        titleField.setStyle(fieldStyle());
        timeField.setStyle(fieldStyle());
        placeField.setStyle(fieldStyle());
        noteArea.setStyle(fieldStyle() + "-fx-font-size: 13;");

        titleField.setPromptText("Nhập tiêu đề...");
        timeField.setPromptText("Ví dụ: 10:00");
        placeField.setPromptText("Nhập địa điểm...");
        noteArea.setPromptText("Ghi chú...");
        noteArea.setWrapText(true);
        noteArea.setPrefRowCount(4);
    }
}
