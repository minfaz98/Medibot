package lk.medi.medibot;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lk.medi.medibot.medibot.BotLogic;
import lk.medi.medibot.service.AppointmentService;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML private TextArea chatArea;
    @FXML private TextField userInput;

    @FXML private ImageView chatBotImageView;
    @FXML private VBox appointmentPane;
    @FXML private VBox cancelPane;
    @FXML private ComboBox<String> doctorDropdown;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField cancelIdField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Button bookButton;



    private BotLogic botLogic;
    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        botLogic = new BotLogic(chatBotImageView);
        setupChatbotImage();
        chatArea.appendText("MediBot: " + botLogic.welcomeMessage() + "\n");
        loadDoctorDropdown();
        userInput.setOnAction(event -> handleSend());
        cancelIdField.setOnAction(event -> handleCancelAppointment());
        bookButton.setOnAction(event -> handleBookAppointment());

    }

    private void setupChatbotImage() {
        Image defaultImage = new Image(Objects.requireNonNull(getClass().getResource("/images/smile.png")).toExternalForm());
        Image hoverImage = new Image(Objects.requireNonNull(getClass().getResource("/images/hi.png")).toExternalForm());

        chatBotImageView.setImage(defaultImage);
        chatBotImageView.setOnMouseEntered(e -> chatBotImageView.setImage(hoverImage));
        chatBotImageView.setOnMouseClicked(e -> {
            chatArea.appendText("Sofi: " + botLogic.welcomeMessage() + "\n");
            userInput.requestFocus();
        });
        chatBotImageView.setCursor(Cursor.HAND);
        chatBotImageView.setOnMouseExited(e -> chatBotImageView.setImage(defaultImage));
    }

    @FXML
    protected void handleSend() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) return;

        chatArea.appendText("You: " + input + "\n");

        String response;
            if (input.toLowerCase().contains("book appointment")|| input.toLowerCase().contains("make appointment") || input.toLowerCase().contains("book")) {
                Platform.runLater(() -> appointmentPane.setVisible(true));
                response = "Sure! Please fill the form below to book an appointment.";
            } else if (input.toLowerCase().contains("cancel")|| input.toLowerCase().contains("delete appointment") || input.toLowerCase().contains("remove appointment")) {
                Platform.runLater(() -> cancelPane.setVisible(true));
                response = "Please enter your appointment ID to cancel.";
            } else {
                response = botLogic.getResponse(input);
            }


        chatArea.appendText("Sofi: " + response + "\n");
        userInput.clear();
    }

    private void loadDoctorDropdown() {
        new Thread(() -> {
            try {
                List<String> doctors = appointmentService.fetchDoctors();
                Platform.runLater(() -> {
                    doctorDropdown.getItems().clear();
                    doctorDropdown.getItems().addAll(doctors);
                });
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load doctor list.");
            }
        }).start();
    }

    @FXML
    protected void handleBookAppointment() {
        String doctor = doctorDropdown.getValue();
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate date = datePicker.getValue();

        if (doctor == null || name.isEmpty() || phone.isEmpty() || date == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
            chatArea.appendText("Sofi: Please fill in all fields.\n");
            return;
        }

        new Thread(() -> {
            try {
                String id = appointmentService.bookAppointment(name, phone, doctor, date);

                // Check if booking was successful by checking if ID is numeric
                if (id != null && id.matches("\\d+")) {
                    Platform.runLater(() -> {
                        chatArea.appendText("Sofi: Appointment booked successfully!\n");
                        chatArea.appendText("Doctor: " + doctor + "\nPatient: " + name + " | Phone: " + phone + "\n");
                        chatArea.appendText("Appointment ID: " + id + "\n");
                        chatArea.appendText("Please come after 3pm on " + date + " to consult the doctor.\n");
                        hideAppointmentPane();
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Booking Failed", id);
                        chatArea.appendText("Sofi: " + id + "\n");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Booking failed.");
                    chatArea.appendText("Sofi: Booking failed.\n");
                });
            }
        }).start();
    }

    @FXML
    protected void handleCancelAppointment() {
        String appointmentId = cancelIdField.getText().trim();
        if (appointmentId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing ID", "Please enter your Appointment ID.");
            chatArea.appendText("Sofi: " + "Missing ID-Please enter your Appointment ID." + "\n");
            return;
        }


        new Thread(() -> {
            try {
               String responseMessage = appointmentService.cancelAppointment(appointmentId);
                Platform.runLater(() -> {
                    chatArea.appendText("Sofi:"+  responseMessage + "\n");
                    cancelIdField.clear();

                    if(responseMessage.equals("Appointment No:"+appointmentId+ " cancelled successfully.")) {
                        hideCancelPane();
                    }

                });
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Cancelation failed.");
            }
        }).start();
    }

    @FXML
    protected void hideAppointmentPane() {
        appointmentPane.setVisible(false);
        doctorDropdown.setValue(null);
        nameField.clear();
        phoneField.clear();
        datePicker.setValue(null);
    }

    @FXML
    protected void hideCancelPane() {
        cancelPane.setVisible(false);
        cancelIdField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
