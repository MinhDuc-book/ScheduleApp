package schedule.assist.demo.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TaskEditor {

    private final Task task;          // thẻ gốc cần chỉnh sửa
    private final AnchorPane root;    // màn hình chính để gắn overlay

    private StackPane overlay;        // nền mờ đen + form ở giữa

    public TaskEditor(Task task, AnchorPane root) {
        this.task = task;
        this.root = root;
    }

    private String fieldStyle() {
        return "-fx-background-color: #f7f8fa;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #dde1e7;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 8 12;" +
                "-fx-font-size: 18;";
    }

    public void expandToEdit() {
        //Nền mờ đen phủ toàn màn hình
        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        backdrop.prefWidthProperty().bind(root.widthProperty());
        backdrop.prefHeightProperty().bind(root.heightProperty());

        //Form chỉnh sửa
        VBox form = buildForm();

        //StackPane để căn form vào giữa backdrop
        overlay = new StackPane();
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());
        overlay.getChildren().addAll(backdrop, form);
        StackPane.setAlignment(form, Pos.CENTER);

        //Hiệu ứng fade in
        overlay.setOpacity(0);
        root.getChildren().add(overlay);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private VBox buildForm() {
        TextField titleField = new TextField(task.titleTask);
        TextField timeField  = new TextField(task.timeOfTask);
        TextField placeField = new TextField(task.placeofTask);
        TextArea  noteArea   = new TextArea(task.noteOfTask);
        autoFillField(titleField, timeField, placeField, noteArea);

        Label heading = new Label("Edit task");
        heading.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");

        DeleteTaskButton deleteTaskButton = new DeleteTaskButton();
        deleteTaskButton.setOnAction(e -> {
            close(false); // đóng overlay không restore task
            task.onDelete.run();
        });

        HBox header = new HBox(10, heading, new Region(), deleteTaskButton);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        Button saveButton = new Button("Save (ESC)");
        saveButton.setStyle(
                "-fx-background-color: #4A90E2;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );
        saveButton.setOnAction(e -> saveAndClose(titleField, timeField, placeField, noteArea));

        VBox form = new VBox(14,
                header,
                labeledField("Title", titleField),
                labeledField("Time", timeField),
                labeledField("Place", placeField),
                labeledField("Note", noteArea),
                saveButton
        );

        form.setMaxWidth(600);
        form.setMaxHeight(600);
        form.setStyle(task.SCALE_STYLE);
        form.setPadding(new Insets(24));
        form.setAlignment(Pos.CENTER);

        // ── ESC để lưu và đóng ──────────────────────────────────
        form.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                saveAndClose(titleField, timeField, placeField, noteArea);
            }
        });
        for (Control field : new Control[]{titleField, timeField, placeField, noteArea}) {
            field.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    saveAndClose(titleField, timeField, placeField, noteArea);
                }
            });
        }

        form.setFocusTraversable(true);
        // Focus vào field đầu tiên sau khi render xong
        javafx.application.Platform.runLater(titleField::requestFocus);

        return form;
    }

    private void saveAndClose(TextField titleField, TextField timeField,
                              TextField placeField, TextArea noteArea) {
        // ── Ghi data mới vào task ──────────────────────────────
        if (!titleField.getText().trim().isEmpty()) task.titleTask = titleField.getText().trim();
        if (!timeField.getText().trim().isEmpty())  task.timeOfTask = timeField.getText().trim();
        if (!placeField.getText().trim().isEmpty()) task.placeofTask = placeField.getText().trim();
        task.noteOfTask = noteArea.getText().trim();

        // Cập nhật label + màu trước khi thêm lại vào root
        task.titleLabel.setText(task.titleTask);
        task.timeLabel.setText("🕐 " + task.timeOfTask);
        task.placeLabel.setText("📍 " + task.placeofTask);
        task.setStyle(task.setColorFromPlace());
        task.applyGlow();

        close(true); // đóng overlay và restore task
        task.onSnap.run();
    }

    // ── Đóng overlay; restoreTask = true thì thêm lại task vào root ──
    private void close(boolean restoreTask) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), overlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(ev -> {
            root.getChildren().remove(overlay);
            if (restoreTask) {
                task.isEditing = false;
                root.getChildren().add(task); // thêm lại thẻ gốc với data mới
            }
        });
        fadeOut.play();
    }

    private VBox labeledField(String labelText, Control field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #A8D8EA; -fx-font-weight: bold;");
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

        titleField.setPromptText("Title...");
        timeField.setPromptText("Ex: 10:00");
        placeField.setPromptText("Place...");
        noteArea.setPromptText("Note...");
        noteArea.setWrapText(true);
        noteArea.setPrefRowCount(4);
    }
}