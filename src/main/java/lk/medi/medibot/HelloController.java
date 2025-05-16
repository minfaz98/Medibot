package lk.medi.medibot;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import lk.medi.medibot.medibot.BotLogic;

import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField userInput;

    @FXML
    private ImageView chatBotImageView;


    private BotLogic botLogic;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set the default image for the chatbot
        botLogic = new BotLogic(chatBotImageView);
        // Load both images
        Image defaultImage = new Image(getClass().getResource("/images/smile.png").toExternalForm());
        Image hoverImage = new Image(getClass().getResource("/images/hi.png").toExternalForm());

        // Set default image initially
        chatBotImageView.setImage(defaultImage);

        // Set hover event
        chatBotImageView.setOnMouseEntered(e -> chatBotImageView.setImage(hoverImage));
        chatBotImageView.setCursor(Cursor.HAND);


        // Set exit event to revert to original
        chatBotImageView.setOnMouseExited(e -> chatBotImageView.setImage(defaultImage));
        String greeting = botLogic.welcomeMessage();
        chatArea.appendText("Medibot: " + greeting + "\n");
    }


    @FXML
    protected void handleSend() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) return;

        chatArea.appendText("You: " + input + "\n");

        String response;
        if (botLogic.isTrainingMode()) {
            // If we are in training mode, treat this input as the answer
            response = botLogic.trainBot(input);
        } else {
            // Normal chat input
            response = botLogic.getResponse(input);
        }

        chatArea.appendText("Sofi: " + response + "\n");
        userInput.clear();
    }
}
