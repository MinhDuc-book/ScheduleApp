module schedule.assist.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires com.google.gson;

    opens schedule.assist.demo.model to com.google.gson;
    exports schedule.assist.demo.ui;
}
