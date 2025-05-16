package lk.medi.medibot.medibot;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

public class BotLogic {
    private final Map<String, String> learnedResponses = new HashMap<>();
    private final Map<String, Integer> repeatedQuestions = new HashMap<>();
    private String lastQuestion = "";
    private int repeatCount = 0;
    private String userName = "";
    private boolean trainingMode = false;
    private String questionToLearn = "";
    private boolean awaitingNameInput = false;
    private final String LEARNED_FILE = "learned_data.txt";

    private ImageView chatBotImageView;
    private Image goodbyeImage = new Image(getClass().getResource("/images/bye.png").toExternalForm());
    private boolean isGoodbye = false;

    Random random = new Random();

    public BotLogic() {
        loadLearnedResponses(); // Load Q&A from file at startup
    }
    public BotLogic(ImageView chatBotImageView) {
        this.chatBotImageView = chatBotImageView;
    }

    public String getResponse(String input) {
        input = input.trim().toLowerCase();

        // Time-based greeting
        if (input.contains("hello") || input.contains("hi") || input.contains("hey") || input.contains("greetings") ) {
            return getRandomGreeting();
        }
        if(input.contains("good morning") || input.contains("good afternoon") || input.contains("good evening")){
            return getTimeBasedGreeting()+" How can I help you?";
        }

        if (input.toLowerCase().contains("sorry") || input.toLowerCase().contains("apologize") || input.toLowerCase().contains("excuse me")) {
            repeatCount = 0;

            // Optional: Reset image to default
            Image defaultImage = new Image(getClass().getResource("/images/smile.png").toExternalForm());
            chatBotImageView.setImage(defaultImage);

            return "Okay, let's continue.";
        }

        if (input.equals(lastQuestion)) {
            repeatCount++;
            if (repeatCount >= 4) {
                // Change the image
                Image warningImage = new Image(getClass().getResource("/images/annoyed.png").toExternalForm());
                chatBotImageView.setImage(warningImage);

                return "Why are you asking the same question again and again?";
            }
        } else {
            repeatCount = 1;
            lastQuestion = input;
        }


        if (input.contains("what is your name") || input.contains("who are you") || input.contains("your name")) {
            awaitingNameInput = true; // Set flag to expect user's name next
            return "I am Sofi, your hospital assistant. What is your name?";
        }
        if (awaitingNameInput) {
            userName = input.trim();
            awaitingNameInput = false; // Reset the flag
            return "Nice to meet you, " + userName + "!";
        }


        if (input.startsWith("my name is ")) {
            userName = input.substring("my name is ".length()).trim();
            return "Nice to meet you, " + userName + "!";
        }

        if (input.equals("bye")|| input.contains("see you later") || input.contains("goodbye") || input.contains("exit")) {
            // Change the image
            isGoodbye = true;
            chatBotImageView.setImage(goodbyeImage);
            return "Have a good health, " + (userName.isEmpty() ? "dear" : userName) + "!";

        }

        if(input.contains("thank you") || input.contains("thanks") || input.contains("thank you so much")) {
            return "You're welcome! I'm here to help.";
        }
        if (input.contains("what is your age") || input.contains("how old are you")) {
            return "I am a computer program, so I don't have an age like humans do.";
        }
        if (input.contains("what is your purpose") || input.contains("what can you do") || input.contains("what are you")) {
            return "I am here to assist you with your health-related queries and provide information.";
        }
        if(input.contains("service") || input.contains("services") || input.contains("what services do you provide")  ) {
            return "I can provide health tips, emergency contacts, doctor schedules,doctor lists, and more.";
        }

        if (input.contains("how are you") || input.contains("how are you doing") || input.contains("how are you doing today")) {
            String[] replies = {"I'm fine", "I am ok", "Not bad dear", "Good", "Alright"};
            return replies[random.nextInt(replies.length)];
        }

        if (input.contains("health tip")|| input.contains("tip") || input.contains("give me a health tip")) {
            return getRandomLineFromFile("healthtips.txt");
        }

        if (input.contains("emergency")|| input.contains("contacts") || input.contains("emergency numbers")) {
            return getLinesFromFile("emergency.txt");
        }

        if (input.contains("list doctors")|| input.contains("doctors") || input.contains("doctor") || input.contains("doctor list")) {
            return getLinesFromFile("doctors.txt");
        }

        if (input.contains("schedule")  || input.contains("doctor's schedule")) {
            return getLinesFromFile("schedule.txt");
        }

        // If known
        if (learnedResponses.containsKey(input)) {
            return learnedResponses.get(input);
        }

        // Enter training mode
        trainingMode = true;
        questionToLearn = input;
        return "You tell the answer please";
    }

    public boolean isTrainingMode() {
        return trainingMode;
    }

    public String trainBot(String answer) {
        if (trainingMode && questionToLearn != null && !questionToLearn.isEmpty()) {
            learnedResponses.put(questionToLearn, answer);
            appendLearnedResponse(questionToLearn, answer); // Save to file
            trainingMode = false;
            questionToLearn = "";
            return "Thanks! I've learned a new response.";
        }
        return "I'm not in training mode right now.";
    }

    private void loadLearnedResponses() {
        try (InputStream is = getClass().getResourceAsStream("/data/" + LEARNED_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    learnedResponses.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("No existing learned data found.");
        }
    }

    private void appendLearnedResponse(String question, String answer) {
        try {
            File file = new File("src/main/resources/data/" + LEARNED_FILE); // Works in development
            file.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(question + "=" + answer);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to save learned response: " + e.getMessage());
        }
    }

    private String getTimeBasedGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning!";
        else if (hour < 17) return "Good afternoon!";
        else return "Good evening!";
    }

    private String getRandomLineFromFile(String filename) {
        List<String> lines = readFileLines(filename);
        if (lines.isEmpty()) return "No tips available.";
        return lines.get(random.nextInt(lines.size()));
    }

    private String getLinesFromFile(String filename) {
        List<String> lines = readFileLines(filename);
        return String.join("\n", lines);
    }

    private List<String> readFileLines(String filename) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/data/" + filename)))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException | NullPointerException e) {
            return List.of("File not found: " + filename);
        }
        return lines;
    }
    public String getRandomGreeting() {
        String[] greetings = {
                "Hello! How can I help you today?",
                "Hi there! Need any assistance?",
                "Hey! How can I assist?",
                "Greetings! What can I do for you?",

        };

        // Generate a random number between 0 and the length of the greetings array
        int n = random.nextInt(greetings.length); // Random number from 0 to 9
        return greetings[n];
    }

    public String welcomeMessage() {
        String[] welcomeMessages = {
                "Welcome to MediBot! How can I assist you today?",
                "Welcome to MediBot! Your health assistant.",
                "Hello! I'm here to help you with your health queries.",
                "Hi there! What can I do for you today?",
                "Greetings! How can I make your day better?",
                "Welcome! Let's talk about your health.",
                "Welcome back!",
        };

        // Generate a random number between 0 and the length of the greetings array
        int n = random.nextInt(welcomeMessages.length); // Random number from 0 to 9
        return welcomeMessages[n];
    }


}
