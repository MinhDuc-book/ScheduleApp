package schedule.assist.demo.ui;

import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

import static schedule.assist.demo.ui.HelloApplication.SCREEN_H;

public class DeleteTaskButton extends Button {

    private static final double BUTTON_SIZE = 20;

    public DeleteTaskButton() {
        super("🗑 Delete");

        String defaultStyle =
                "-fx-background-color: #e74c3c;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10;";

        String hoverStyle =
                "-fx-background-color: #c0392b;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10;";

        this.setStyle(defaultStyle);
        this.setOnMouseEntered(e -> this.setStyle(hoverStyle));
        this.setOnMouseExited(e -> this.setStyle(defaultStyle));
    }
}
