package schedule.assist.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class Week {

    private final String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public HBox createWeekView() {
        HBox weekRow = new HBox(10);
        weekRow.setAlignment(Pos.CENTER);         // Căn giữa theo chiều ngang
        weekRow.setPadding(new Insets(40));        // Khoảng cách với mép ngoài
        weekRow.setMaxWidth(Double.MAX_VALUE);     // Cho phép giãn full width để căn giữa có tác dụng

        for (int i = 0; i < 7; i++) {
            weekRow.getChildren().add(createDayCard(dayNames[i]));
        }

        return weekRow;
    }

    private VBox createDayCard(String dayName) {
        VBox card = new VBox(10);
        card.setPrefSize(180, 600);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10;"
        );

        Label label = new Label(dayName);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        card.getChildren().add(label);
        return card;
    }
}