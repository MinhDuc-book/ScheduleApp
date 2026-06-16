package schedule.assist.demo.ui;

import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import schedule.assist.demo.service.TaskService;

import static schedule.assist.demo.ui.HelloApplication.SCREEN_H;
import static schedule.assist.demo.ui.HelloApplication.SCREEN_W;

public class AddTaskButton extends Button {

    private static final double BUTTON_SIZE = 55.0;

    // ── Màu accent xanh lá — khác với cyan của task và tím của home ──
    private final String DEFAULT_STYLE =
            "-fx-background-color: #0D2137;" +   // nền tối giống task
                    "-fx-background-radius: 30;" +
                    "-fx-text-fill: #39FF14;" +           // chữ xanh lá neon
                    "-fx-font-size: 28;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: #39FF14;" +        // viền xanh lá
                    "-fx-border-radius: 30;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-padding: 0;";

    private final String HOVER_STYLE =
            "-fx-background-color: #0D2137;" +
                    "-fx-background-radius: 30;" +
                    "-fx-text-fill: #39FF14;" +
                    "-fx-font-size: 28;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: #39FF14;" +
                    "-fx-border-radius: 30;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-padding: 0;";

    public AddTaskButton(TaskService taskService) {
        super("+");

        this.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);

        this.setStyle(DEFAULT_STYLE);

        // ── Glow xanh lá khi bình thường ─────────────────────────
        DropShadow defaultGlow = new DropShadow();
        defaultGlow.setColor(Color.web("#39FF14", 0.6));
        defaultGlow.setOffsetX(0);
        defaultGlow.setOffsetY(0);
        defaultGlow.setRadius(15);
        defaultGlow.setSpread(0.3);
        this.setEffect(defaultGlow);

        // ── Glow mạnh hơn khi hover ───────────────────────────────
        DropShadow hoverGlow = new DropShadow();
        hoverGlow.setColor(Color.web("#39FF14", 0.9));
        hoverGlow.setOffsetX(0);
        hoverGlow.setOffsetY(0);
        hoverGlow.setRadius(25);
        hoverGlow.setSpread(0.5);

        this.setOnMouseEntered(e -> {
            this.setStyle(HOVER_STYLE);
            this.setEffect(hoverGlow); // ✅ glow mạnh hơn khi hover
        });

        this.setOnMouseExited(e -> {
            this.setStyle(DEFAULT_STYLE);
            this.setEffect(defaultGlow); // ✅ về glow bình thường
        });

        // ── Tạo task mới ở giữa màn hình ─────────────────────────
        this.setOnAction(e -> {
            Task task = taskService.createTask();
            double width = SCREEN_W;
            double height = SCREEN_H;
            if (this.getParent() instanceof javafx.scene.layout.Region root) {
                if (root.getWidth() > 0) {
                    width = root.getWidth();
                } else if (root.getPrefWidth() > 0) {
                    width = root.getPrefWidth();
                }
                if (root.getHeight() > 0) {
                    height = root.getHeight();
                } else if (root.getPrefHeight() > 0) {
                    height = root.getPrefHeight();
                }
            }
            task.setLayoutX(width / 2.0 - 100);
            task.setLayoutY(height / 2.0);
            task.onChange.run();
        });
    }
}