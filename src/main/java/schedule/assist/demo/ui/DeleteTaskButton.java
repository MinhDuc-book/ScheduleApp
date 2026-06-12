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

        this.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10;"
        );

        this.setOnMouseEntered(e -> this.setStyle(
                "-fx-background-color: #c0392b;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10;"
        ));

        this.setOnMouseExited(e -> this.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: white;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10;"
        ));
    }
}
