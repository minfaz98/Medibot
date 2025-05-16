module lk.medi.medibot {
    requires javafx.controls;
    requires javafx.fxml;


    opens lk.medi.medibot to javafx.fxml;
    exports lk.medi.medibot;
}