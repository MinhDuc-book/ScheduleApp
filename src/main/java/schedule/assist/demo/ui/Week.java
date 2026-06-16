package schedule.assist.demo.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class Week {

    private final String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public HBox createWeekView() {
        HBox weekRow = new HBox(10);
        weekRow.setAlignment(Pos.CENTER);
        weekRow.setPadding(new Insets(40));
        weekRow.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 7; i++) {
            VBox card = createDayCard(dayNames[i]);
            HBox.setHgrow(card, Priority.ALWAYS); // giãn đều theo chiều ngang
            weekRow.getChildren().add(card);
        }

        return weekRow;
    }

    private VBox createDayCard(String dayName) {
        VBox card = new VBox(10);
        card.setPrefSize(180, 600);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: #2A3D4AFF;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #00E8FF;" +   // viền cyan giống task
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 10;"
        );

        // ── Glow effect cho viền ──────────────────────────────
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00E8FF", 0.5)); // cyan, 50% opacity
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        glow.setRadius(15);
        glow.setSpread(0.2); // nhẹ hơn task (task là 0.9) để không bị chói
        card.setEffect(glow);

        Label label = new Label(dayName);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

        card.getChildren().add(label);
        card.setUserData(dayName);
        return card;
    }
}