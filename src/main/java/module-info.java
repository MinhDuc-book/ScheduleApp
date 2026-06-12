module schedule.assist.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.desktop;

    requires javafx.base;

    opens schedule.assist.demo to javafx.fxml, com.google.gson;
    opens schedule.assist.demo.model to com.google.gson;
    opens schedule.assist.demo.repository to com.google.gson;
    opens schedule.assist.demo.ui to com.google.gson;
    opens schedule.assist.demo.service to com.google.gson;
    exports schedule.assist.demo.ui;
}