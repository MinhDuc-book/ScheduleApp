package schedule.assist.demo.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.regex.Pattern;

public class TaskEditor {
    /** Matches HH:mm where HH is 00-23 and mm is 00-59. */
    private static final Pattern TIME_24H = Pattern.compile("^([01]?\\d|2[0-3]):([0-5]?\\d)$");

    private static final String FIELD_STYLE = "-fx-background-color: #f7f8fa;" + "-fx-background-radius: 8;"
            + "-fx-border-color: #dde1e7;" + "-fx-border-radius: 8;" + "-fx-border-width: 1;" + "-fx-padding: 8 12;"
            + "-fx-font-size: 13;";

    private static final String FIELD_ERROR_STYLE = "-fx-background-color: #fff0f0;" + "-fx-background-radius: 8;"
            + "-fx-border-color: #e74c3c;" + "-fx-border-radius: 8;" + "-fx-border-width: 1.5;" + "-fx-padding: 8 12;"
            + "-fx-font-size: 13;";

    /** Returns null on a valid time (normalised to HH:mm), or an error message. */
    static String validateAndNormalise(String raw) {
        String s = raw.trim();
        if (s.isEmpty())
            return "Không được để trống";
        if (!TIME_24H.matcher(s).matches())
            return "Định dạng phải là HH:mm (VD: 09:30, 23:59)";
        // Normalise single-digit parts
        String[] parts = s.split(":");
        return String.format("%02d:%02d", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private Task task;

    public TaskEditor(Task task) {
        this.task = task;
    }

    private HBox buildHeader() {
        Region spacer = new Region();

        DeleteTaskButton deleteTaskButton = new DeleteTaskButton();
        deleteTaskButton.setOnAction(e -> task.onDelete.run());

        HBox header = new HBox(10, spacer, deleteTaskButton);
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private void playExpandAnimation(TextField titleField) {
        // Animation expand
        ScaleTransition expand = new ScaleTransition(Duration.millis(500), task);
        expand.setFromX(1.0);
        expand.setFromY(1.0);
        expand.setToX(1.1);
        expand.setToY(1.1);
        expand.play();

        expand.setOnFinished(ev -> titleField.requestFocus());
    }

    public void expandCardToEditor() {
        task.isEditing = true;

        task.getChildren().clear();

        TextField titleField = new TextField(task.titleTask);
        TextField timeField = new TextField(task.timeOfTask);
        TextField placeField = new TextField(task.placeofTask);
        TextArea noteArea = new TextArea(task.noteOfTask);

        autoFillField(titleField, timeField, placeField, noteArea);

        HBox header = buildHeader();

        task.getChildren().addAll(header, labeledField("Tiêu đề", titleField), labeledField("Thời gian", timeField),
                labeledField("Địa điểm", placeField), labeledField("Ghi chú", noteArea));

        task.setStyle(task.SCALE_STYLE);
        task.setSpacing(task.EDIT_SPACING);

        playExpandAnimation(titleField);

        // ESC handler
        setupEscapeHandlers(titleField, timeField, placeField, noteArea);
    }

    private void setupEscapeHandlers(TextField titleField, TextField timeField, TextField placeField,
            TextArea noteArea) {

        // Task level
        task.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                saveAndCollapse(titleField, timeField, placeField, noteArea);
            }
        });

        // Fields level
        for (Control field : new Control[] { titleField, timeField, placeField, noteArea }) {
            field.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    saveAndCollapse(titleField, timeField, placeField, noteArea);
                }
            });
        }

        task.setFocusTraversable(true);
        task.requestFocus();
    }

    private boolean applyFieldEdits(TextField titleField, TextField timeField, TextField placeField,
            TextArea noteArea) {

        if (!titleField.getText().trim().isEmpty())
            task.titleTask = titleField.getText().trim();

        // --- 24h validation ---
        String timeRaw = timeField.getText().trim();
        String normalisedTime = validateAndNormalise(timeRaw);
        if (normalisedTime.contains(":")) {
            task.timeOfTask = normalisedTime;
            timeField.setStyle(FIELD_STYLE); // clear any previous error
        } else {
            // Invalid — show error and keep editor open
            timeField.setStyle(FIELD_ERROR_STYLE);
            timeField.setTooltip(new Tooltip(normalisedTime));
            timeField.requestFocus();
            return false;
        }

        if (!placeField.getText().trim().isEmpty())
            task.placeofTask = placeField.getText().trim();
        task.noteOfTask = noteArea.getText().trim();

        return true;
    }

    private void restoreCardView() {
        task.isEditing = false;
        task.getChildren().clear();

        task.titleLabel.setText(task.titleTask);
        task.timeLabel.setText("🕐 " + task.timeOfTask);
        task.placeLabel.setText("📍 " + task.placeofTask);

        task.getChildren().setAll(task.titleLabel, task.timeLabel, task.placeLabel);
        task.setStyle(task.getColorStyleFromPlace());
        task.setSpacing(task.NORMAL_SPACING);
    }

    private void playCollapseAnimation() {
        // Animation collapse
        task.setScaleX(1.1);
        task.setScaleY(1.1);
        ScaleTransition collapse = new ScaleTransition(Duration.millis(300), task);
        collapse.setFromX(1.1);
        collapse.setFromY(1.1);
        collapse.setToX(1.0);
        collapse.setToY(1.0);

        collapse.play();

        collapse.setOnFinished(ev -> {
            // Reset scale
            task.setScaleX(1.0);
            task.setScaleY(1.0);
        });
    }

    private void saveAndCollapse(TextField titleField, TextField timeField, TextField placeField, TextArea noteArea) {

        if (!applyFieldEdits(titleField, timeField, placeField, noteArea))
            return;

        restoreCardView();

        try {
            task.onSort.run(); // sort column first, then save
            task.onChange.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        playCollapseAnimation();
    }

    private VBox labeledField(String labelText, Control field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #666; -fx-font-weight: bold;");
        VBox vbox = new VBox(5, lbl, field);
        vbox.setPadding(new Insets(2, 0, 4, 0));
        return vbox;
    }

    private void autoFillField(TextField titleField, TextField timeField, TextField placeField, TextArea noteArea) {
        titleField.setStyle(FIELD_STYLE);
        timeField.setStyle(FIELD_STYLE);
        placeField.setStyle(FIELD_STYLE);
        noteArea.setStyle(FIELD_STYLE + "-fx-font-size: 13;");

        titleField.setPromptText("Nhập tiêu đề...");
        timeField.setPromptText("Định dạng 24h — VD: 09:30");
        placeField.setPromptText("Nhập địa điểm...");
        noteArea.setPromptText("Ghi chú...");
        noteArea.setWrapText(true);
        noteArea.setPrefRowCount(4);
    }
}
