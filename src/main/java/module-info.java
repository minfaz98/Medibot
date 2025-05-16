module lk.medi.medibot {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens lk.medi.medibot to javafx.fxml;
    exports lk.medi.medibot;
    exports lk.medi.medibot.service;
    opens lk.medi.medibot.service to javafx.fxml;
}