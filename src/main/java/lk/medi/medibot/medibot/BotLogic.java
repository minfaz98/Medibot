package lk.medi.medibot.medibot;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

public class BotLogic {
    private final Map<String, String> learnedResponses = new HashMap<>();
    private final Map<String, Integer> repeatedQuestions = new HashMap<>();
    private final Map<String, String> qnaMap = new HashMap<>();
    private String lastQuestion = "";
    private int repeatCount = 0;
    private String userName = "";
    private boolean trainingMode = false;
    private String questionToLearn = "";
    private boolean awaitingNameInput = false;
    private final String LEARNED_FILE = "learned_data.txt";

    private ImageView chatBotImageView;
    private final Image goodbyeImage = new Image(getClass().getResource("/images/bye.png").toExternalForm());
    private boolean isGoodbye = false;

    Random random = new Random();

//    public BotLogic() {
//        loadLearnedResponses();
//        loadQnA(); // Load Q&A from file at startup
//    }

    public BotLogic(ImageView chatBotImageView) {
        this.chatBotImageView = chatBotImageView;
        loadLearnedResponses();
        loadQnA(); // Ensure QnA is loaded
    }
    Map<String, String> smallTalkMap = new HashMap<>();
    {
        smallTalkMap.put("greetings", "Greetings! What can I do for you?");
        smallTalkMap.put("what'up", "Not much, just here to help you!");
        smallTalkMap.put("Need help", "Sure! What do you need help with?");
        smallTalkMap.put("Need advice", "Of course! What do you need advice on?");
        smallTalkMap.put("how can you help me", "I can assist you with health-related queries, provide information, and more.");
        smallTalkMap.put("how's the weather", "I don't have real-time weather data, but I can help with health tips!");
        smallTalkMap.put("tell me a joke", "Why did the scarecrow win an award? Because he was outstanding in his field!");
        smallTalkMap.put("tell me a story", "Once upon a time, in a land of health and wellness, there was a chatbot named Sofi who helped people stay healthy.");
        smallTalkMap.put("what's your favorite color", "I don't have a favorite color, but I like the color of health!");
        smallTalkMap.put("what's your favorite food", "I don't eat, but I love to talk about healthy foods!");
        smallTalkMap.put("what's your favorite movie", "I don't watch movies, but I love health documentaries!");
        smallTalkMap.put("what's your favorite book", "I don't read books, but I love health-related articles!");

    }
    // Small talk responses
    public String answerSmallTalk(String input) {
        input = input.trim().toLowerCase();
        String question = input.toLowerCase();
        for (String key : smallTalkMap.keySet()) {
            String keyLower = key.toLowerCase();
            if (keyLower.contains(question)) {
                return smallTalkMap.get(key);
            }
        }
        return null; // return null if no small talk match
    }




    public String getResponse(String input) {
        input = input.trim().toLowerCase();


        // Time-based greeting
        if (input.contains("hello") || input.contains("hi") || input.contains("hey") || input.contains("greetings")) {
            return getRandomGreeting();
        }

        if (input.contains("good morning") || input.contains("good afternoon") || input.contains("good evening")) {
            return getTimeBasedGreeting() + " How can I help you?";
        }

        if (input.contains("sorry") || input.contains("apologize") || input.contains("excuse me")) {
            repeatCount = 0;
            Image defaultImage = new Image(getClass().getResource("/images/smile.png").toExternalForm());
            chatBotImageView.setImage(defaultImage);
            return "Okay, let's continue.";
        }

        if (input.equals(lastQuestion)) {
            repeatCount++;
            if (repeatCount >= 4) {
                Image warningImage = new Image(getClass().getResource("/images/annoyed.png").toExternalForm());
                chatBotImageView.setImage(warningImage);
                return "Why are you asking the same question again and again?";
            }
        } else {
            repeatCount = 1;
            lastQuestion = input;
        }
        if (input.startsWith("my name is ")) {
            userName = input.substring("my name is ".length()).trim();
            return "Nice to meet you, " + userName + "!";
        }

        if (input.contains("what is your name") || input.contains("who are you") || input.contains("your name") || input.contains("name")) {
            awaitingNameInput = true;
            return "I am Sofi, your hospital assistant. What is your name?";
        }

        if (awaitingNameInput) {
            userName = input.trim();
            awaitingNameInput = false;
            return "Nice to meet you, " + userName + "!";
        }

        // Small talk
        String smallTalkResponse = answerSmallTalk(input);
        if (smallTalkResponse != null) {
            return smallTalkResponse;
        }

        // File-based QnA
        String qnaAnswer = getAnswer(input);
        if (qnaAnswer != null) {
            return qnaAnswer;
        }
        if (input.contains("goodnight") || input.contains("good night")){
            return "Good night,Take care";
        }

        if (input.equals("bye") || input.contains("see you later") || input.contains("goodbye") || input.contains("exit") ) {
            isGoodbye = true;
            chatBotImageView.setImage(goodbyeImage);
            return "Have a good health, " + (userName.isEmpty() ? "dear" : userName) + "!";
        }

        if (input.contains("thank you") || input.contains("thanks") || input.contains("thank you so much")) {
            return "You're welcome! I'm here to help.";
        }

        if (input.contains("what is your age") || input.contains("how old are you") || input.contains("age")) {
            return "I am a computer program, so I don't have an age like humans do.";
        }

        if (input.contains("what is your purpose") || input.contains("what can you do") || input.contains("what are you")) {
            return "I am here to assist you with your health-related queries and provide information.";
        }

        if (input.contains("service") || input.contains("services") || input.contains("what services do you provide")) {
            return "I can provide health tips, emergency contacts, doctor schedules, doctor lists, and more.";
        }

        if (input.contains("how are you") || input.contains("how are you doing") || input.contains("how's it going")) {
            String[] replies = {"I'm fine", "I am ok", "Not bad dear", "Good", "Alright"};
            return replies[random.nextInt(replies.length)];
        }

        if (input.contains("health tip") || input.contains("tip") || input.contains("give me a health tip")) {
            return getRandomLineFromFile("healthtips.txt");
        }

        if (input.contains("emergency") || input.contains("contacts") || input.contains("emergency numbers")) {
            return getLinesFromFile("emergency.txt");
        }

        if (input.contains("list doctors") || input.contains("doctors") || input.contains("doctors list")) {
            return getLinesFromFile("doctors.txt");
        }

        if (input.contains("schedule") || input.contains("doctor's schedule") || input.contains("doctor schedule")) {
            return getLinesFromFile("schedule.txt");
        }

        // Learned responses
        if (learnedResponses.containsKey(input)) {
            return learnedResponses.get(input);
        }


        // Training mode prompt
        if (input.startsWith("what is") || input.startsWith("who is") || input.startsWith("tell me about") || input.startsWith("explain")) {
            trainingMode = true;
            questionToLearn = input;
            return "I don't know the answer to that. Would you like to teach me?";
        }

        return "Can you please rephrase your question? I don't understand.";
    }

    public boolean isTrainingMode() {
        return trainingMode;
    }

    public String trainBot(String answer) {
        if (trainingMode && questionToLearn != null && !questionToLearn.isEmpty()) {
            learnedResponses.put(questionToLearn, answer);
            appendLearnedResponse(questionToLearn, answer);
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
            File file = new File("src/main/resources/data/" + LEARNED_FILE);
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
                "Greetings! What can I do for you?"
        };
        return greetings[random.nextInt(greetings.length)];
    }

    public String welcomeMessage() {
        String[] welcomeMessages = {
                "Welcome to MediBot! How can I assist you today?",
                "Welcome to MediBot! Your health assistant.",
                "Hello! I'm here to help you with your health queries.",
                "Hi there! What can I do for you today?",
                "Greetings! How can I make your day better?",
                "Welcome! Let's talk about your health.",
                "Welcome back!"
        };
        return welcomeMessages[random.nextInt(welcomeMessages.length)];
    }

    private void loadQnA() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/data/qna.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    qnaMap.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Could not load qna.txt");
        }
    }

    public String getAnswer(String input) {
        String lowerInput = input.toLowerCase();
        for (String key : qnaMap.keySet()) {
            if (lowerInput.contains(key)) {
                return qnaMap.get(key);
            }
        }
        return null;
    }
}
