package schedule.assist.demo.ui;

import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import schedule.assist.demo.model.TaskModel;

public class Task extends VBox {

    private double dragOffsetX, dragOffsetY;

    protected String titleTask   = "Event";
    protected String timeOfTask  = "10:00";
    protected String placeofTask = "GĐ3";
    protected String noteOfTask  = "Note";
    protected String dayOfWeek = "";

    protected String colorTaskSchool = "-fx-background-color: #2D3F6AFF;";
    protected String colorTaskHome   = "-fx-background-color: #1A1A2EFF;";

    protected String borderSchool = "#00E8FF"; // cyan
    protected String borderHome   = "#B48EFF"; // tím nhạt

    protected static final double NORMAL_SPACING = 6;
    protected static final double EDIT_SPACING   = 12;

    public Label titleLabel;
    public Label timeLabel;
    public Label placeLabel;

    public Runnable onDelete = () -> {};
    public Runnable onChange = () -> {};
    public boolean isEditing = false;

    public String BASE_STYLE =
            "-fx-background-radius: 14;" +
                    "-fx-padding: 12 14;" +
                    "-fx-border-radius: 14;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-cursor: hand;";

    public String SCALE_STYLE =
            "-fx-background-color: #1C2B3A;" +
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
        titleLabel.setText(t);
    }

    public void setTimeOfTask(String t) {
        this.timeOfTask = t;
        timeLabel.setText("🕐 " + t);
    }

    public void setPlaceofTask(String n) {
        this.placeofTask = n;
        placeLabel.setText("📍 " + n);
        this.setStyle(getColorStyleFromPlace());
        this.applyGlow();
    }

    public void setNoteOfTask(String n) { this.noteOfTask = n; }

    public void setDayOfWeek(String n) {this.dayOfWeek = n;}

    public String getTitleTask()   { return titleTask; }
    public String getTimeOfTask()  { return timeOfTask; }
    public String getNoteOfTask()  { return noteOfTask; }
    public String getPlaceofTask() { return placeofTask; }
    public String getDayOfWeek() {return dayOfWeek;}

    private void bringToTop() {
        AnchorPane parent = (AnchorPane) this.getParent();
        if (parent != null) {
            parent.getChildren().remove(this);
            parent.getChildren().add(this);
        }
    }


    public String getColorStyleFromPlace() {
        if (placeofTask.contains("Home")) {
            // Home: nền tím đậm, viền tím nhạt
            return colorTaskHome +
                    "-fx-border-color: " + borderHome + ";" +
                    BASE_STYLE;
        }
        // School/default: nền xanh đậm, viền cyan
        return colorTaskSchool +
                "-fx-border-color: " + borderSchool + ";" +
                BASE_STYLE;
    }

    private void applyGlow() {
        DropShadow glow = new DropShadow();
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        glow.setRadius(6);
        glow.setSpread(0.6);

        if (placeofTask.contains("Home")) {
            glow.setColor(Color.web("#B48EFF", 0.8));
        } else {
            glow.setColor(Color.web("#00E8FF", 0.8));
        }

        this.setEffect(glow);
    }

    private void snapToColumn() {
        AnchorPane parent = (AnchorPane) this.getParent();
        if (parent == null) return;

        // Tìm HBox chứa các cột ngày (WeekRow là child đầu tiên của root)
        HBox weekRow = null;
        for (var node : parent.getChildren()) {
            if (node instanceof HBox) {
                weekRow = (HBox) node;
                break;
            }
        }
        if (weekRow == null) return;

        // Lấy vị trí X trung tâm của task
        double taskCenterX = this.getLayoutX() + this.getPrefWidth() / 2;

        // Tìm cột gần nhất
        VBox closestColumn = null;
        double minDistance = Double.MAX_VALUE;
        String closestDay = "";

        for (var node : weekRow.getChildren()) {
            if (node instanceof VBox col) {
                Object userData = col.getUserData();
                if (userData == null) continue;

                // Lấy vị trí X của cột trong scene
                double colX = col.localToScene(0, 0).getX();
                double colCenterX = colX + col.getWidth() / 2;
                double distance = Math.abs(taskCenterX - colCenterX);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestColumn = col;
                    closestDay = userData.toString();
                }
            }
        }

        //Chỉ snap nếu task đủ gần cột (trong vòng nửa chiều rộng cột)
        if (closestColumn != null && minDistance < closestColumn.getWidth()) {
            // Snap vị trí X vào trung tâm cột
            double colX = closestColumn.localToScene(0, 0).getX();
            double snapX = colX + (closestColumn.getWidth() - this.getPrefWidth()) / 2;
            this.setLayoutX(snapX);

            // Lưu ngày
            this.dayOfWeek = closestDay;
        } else {
            this.dayOfWeek = "";
        }
    }

    // Setup toàn bộ UI của card
    public Task setupUITask() {

        // Label tiêu đề — chữ trắn
        titleLabel = new Label(titleTask);
        titleLabel.setStyle(
                "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        timeLabel = new Label("🕐 " + timeOfTask);
        timeLabel.setStyle(
                "-fx-font-size: 12;" +
                        "-fx-text-fill: #A8D8EA;"
        );

        placeLabel = new Label("📍 " + placeofTask);
        placeLabel.setStyle(
                "-fx-font-size: 11;" +
                        "-fx-text-fill: #A8D8EA;"
        );

        this.setPrefSize(170, 80);
        this.setSpacing(NORMAL_SPACING);

        // Áp màu nền + viền theo place
        this.setStyle(getColorStyleFromPlace());
        applyGlow();


        this.getChildren().setAll(titleLabel, timeLabel, placeLabel);

        this.setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (isEditing) return;
            dragOffsetX = e.getSceneX() - this.getLayoutX();
            dragOffsetY = e.getSceneY() - this.getLayoutY();
            // Đổi cursor khi đang kéo
            this.setStyle(
                    getColorStyleFromPlace().replace("-fx-cursor: hand;", "-fx-cursor: closed-hand;")
            );
            bringToTop();
        });

        this.setOnMouseDragged(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (isEditing) return;
            this.setLayoutX(e.getSceneX() - dragOffsetX);
            this.setLayoutY(e.getSceneY() - dragOffsetY);
        });

        this.setOnMouseReleased(e -> {
            if (isEditing) return;
            this.setStyle(getColorStyleFromPlace());
            applyGlow();
            snapToColumn();
            try {
                onChange.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.setOnMouseClicked(e -> {
            if (isEditing) return;
            if (e.getButton() == MouseButton.SECONDARY) {
                bringToTop();
                new TaskEditor(this).expandCardToEditor();
            }
        });

        return this;
    }

    public TaskModel toModel() {
        return new TaskModel(titleTask, timeOfTask, placeofTask, noteOfTask, dayOfWeek,
                this.getLayoutX(), this.getLayoutY());
    }
}