package lk.medi.medibot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<String> fetchDoctors() {
        List<String> doctorNames = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "/doctors");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();

            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonNode array = mapper.readTree(reader);
                for (JsonNode node : array) {
                    doctorNames.add(node.get("name").asText());
                }
            } else {
                // Add a fallback message to the list
                doctorNames.add("Sorry, I couldn't fetch the doctor list right now.");
            }

            conn.disconnect();
        } catch (IOException e) {
            doctorNames.clear(); // optional: remove partial results
            doctorNames.add("Sorry, I can't connect to the hospital database at this moment.");
        }

        return doctorNames;
    }


    public String bookAppointment(String name, String phone, String doctorName, LocalDate date) {
        try {
            URL url = new URL(BASE_URL + "/appointments");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Include date in the JSON
            String jsonInput = String.format(
                    "{\"patientName\":\"%s\",\"phone\":\"%s\",\"doctorName\":\"%s\",\"date\":\"%s\"}",
                    name, phone, doctorName, date.toString()
            );

            OutputStream os = conn.getOutputStream();
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            int status = conn.getResponseCode();
            if (status == 200 || status == 201) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonNode node = mapper.readTree(reader);
                conn.disconnect();
                return node.get("id").asText();
            } else {
                conn.disconnect();
                return "Sorry, I couldn't book your appointment at this moment.";
            }
        } catch (IOException e) {
            return "Sorry, I can't process the appointment now. Please try again later.";
        }
    }



    public String cancelAppointment(String appointmentId) {
        try {
            Long id = Long.parseLong(appointmentId);  // convert safely
            URL url = new URL(BASE_URL + "/appointments/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            int status = conn.getResponseCode();
            conn.disconnect();

            if (status == 200 || status == 204) {
                return "Appointment No:"+appointmentId+ " cancelled successfully.";
            } else if (status == 404) {
                return "There is no appointment with this ID. Please check and try again.";
            } else {
                return "Sorry, I can't process your request at this moment. Please try again later.";
            }
        } catch (NumberFormatException e) {
            return "Invalid appointment ID format.";
        } catch (IOException e) {
            return "Sorry, I can't process this at this moment.";
        }
    }


}
