package schedule.assist.demo.ui;

import javafx.scene.control.Button;

public class DeleteTaskButton extends Button {

    public DeleteTaskButton() {
        super("🗑 Delete");

        String defaultStyle = "-fx-background-color: #e74c3c;" + "-fx-background-radius: 8;" + "-fx-text-fill: white;"
                + "-fx-font-size: 11;" + "-fx-cursor: hand;" + "-fx-padding: 4 10;";

        String hoverStyle = "-fx-background-color: #c0392b;" + "-fx-background-radius: 8;" + "-fx-text-fill: white;"
                + "-fx-font-size: 11;" + "-fx-cursor: hand;" + "-fx-padding: 4 10;";

        this.setStyle(defaultStyle);
        this.setOnMouseEntered(e -> this.setStyle(hoverStyle));
        this.setOnMouseExited(e -> this.setStyle(defaultStyle));
    }
}
