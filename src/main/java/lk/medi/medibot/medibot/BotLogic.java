package lk.medi.medibot.medibot;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.text.similarity.FuzzyScore;

import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class BotLogic {
    private final Map<String, String> learnedResponses = new HashMap<>();
    private final Map<String, String> qnaMap = new HashMap<>();
    private final Map<String, String> smallTalkMap = new HashMap<>();

    private String lastQuestion = "";
    private String userName = "";
    private String questionToLearn = "";

    private int repeatCount = 0;
    private boolean trainingMode = false;
    private boolean awaitingNameInput = false;


    private static final String LEARNED_FILE = "learned_data.txt";

    private final Image goodbyeImage = new Image(Objects.requireNonNull(getClass().getResource("/images/bye.png")).toExternalForm());
    private final Image defaultImage = new Image(Objects.requireNonNull(getClass().getResource("/images/smile.png")).toExternalForm());
    private final Image warningImage = new Image(Objects.requireNonNull(getClass().getResource("/images/annoyed.png")).toExternalForm());
    private final Image hiImage = new Image(Objects.requireNonNull(getClass().getResource("/images/hi.png")).toExternalForm());

    private final ImageView chatBotImageView;
    private final Random random = new Random();

    public BotLogic(ImageView chatBotImageView) {
        this.chatBotImageView = chatBotImageView;
        loadLearnedResponses();
        loadQnA();
    }

    private void initializeSmallTalk() {
        smallTalkMap.put("greetings", "Greetings! What can I do for you?");
        smallTalkMap.put("help", "Sure! What do you need help with?");
        smallTalkMap.put("advice", "Of course! What do you need advice on?");
        smallTalkMap.put("help me", "I can assist you with health-related queries, provide information, and more.");
        smallTalkMap.put("weather", "I don't have real-time weather data, but I can help with health tips!");
        smallTalkMap.put("joke", "Why did the scarecrow win an award? Because he was outstanding in his field!");
        smallTalkMap.put("story", "Once upon a time, there was a chatbot named Sofi who helped people stay healthy.");
        smallTalkMap.put("color", "I like the color of health!");
        smallTalkMap.put("food", "I love to talk about healthy foods!");
        smallTalkMap.put("movie", "I love health documentaries!");
        smallTalkMap.put("book", "I love health-related articles!");
        smallTalkMap.put("song", "I love songs that promote health and wellness!");
        smallTalkMap.put("hobby", "I enjoy learning about health and wellness!");

    }

    public String getResponse(String input) {
        input = input.trim().toLowerCase();

        if (input.matches("(?i).*\\b(bye|see you|exit|goodbye)\\b.*")) {
            chatBotImageView.setImage(goodbyeImage);
            return "Goodbye! Have a good health, " + (userName.isEmpty() ? "Dear" : userName);
        }

        if (input.matches("(?i).*\\b(hello|hi|hey|greetings|helo|hii)\\b.*")) {
            chatBotImageView.setImage(hiImage);
            return getRandomGreeting();
        }

        chatBotImageView.setImage(defaultImage);


        if (input.matches(".*\\b(good morning|good afternoon|good evening)\\b.*"))
            return getTimeBasedGreeting()+ (userName.isEmpty() ? " Dear" : userName)+ "! How can I help you?";

        if (input.contains("sorry") || input.contains("apologize") ) {
            repeatCount = 0;
            chatBotImageView.setImage(defaultImage);
            return "Okay, let's continue.";
        }

        if (input.equals(lastQuestion)) {
            repeatCount++;
            if (repeatCount >= 3) {
                chatBotImageView.setImage(warningImage);
                return "Why are you asking the same question again and again?";
            }
        } else {
            repeatCount = 1;
            lastQuestion = input;
        }


        // Name-related queries
        if (input.contains("your name") || input.contains("who are you")
                || input.contains("what is your name") || input.contains("introduce yourself") || input.endsWith("name")) {
            if (userName.isEmpty()) {
                awaitingNameInput = true;
                return "I am Sofi, your medical assistant. What is your name?";
            }
            return "My name is Sofiya, you can call me Sofi!";
        }

        if(input.contains("sofi"))
            return "how can i help you "+(userName.isEmpty() ? " Dear" : userName)+ "?";

        if (awaitingNameInput) {
            if (input.matches("[a-zA-Z ]{2,}")) {
                userName = input.trim();
                awaitingNameInput = false;
                return "Nice to meet you, " + userName + "!";
            } else {
                return "Sorry, I didn't catch your name. Could you repeat?";
            }
        }

        if (input.startsWith("my name is ") || input.startsWith("i am ")) {
            userName = input.replaceFirst("my name is |i am ", "").trim();
            return "Nice to meet you, " + userName + "!";
        }

        if (input.contains("good night") || input.contains("sleep well"))
            return "Good night, take care " + (userName.isEmpty() ? "dear" : userName) + "!";


        if (input.contains("thank") || input.contains("thanks") || input.contains("appreciate") || input.contains("grateful") || input.contains("thank you"))
            return "You're welcome! I'm here to help.";
        if (input.contains("how old are you") || input.endsWith("age") || input.contains("when were you born") || input.contains("your age"))
            return "I am a program, I don't age.";
        if (input.contains("what can you do") || input.contains("services") || input.endsWith("services") || input.contains("what are your services") || input.contains("what can you help with"))
            return "I can provide health tips,Doctors Details, emergency contacts, doctor schedules, and book appointments.";
        if (input.contains("how are you") || input.contains("how's it going") || input.contains("how's life")) {
            return List.of("I'm fine", "I am okay", "Not bad", "Good", "Alright","doing well","great,thanks for asking" ).get(random.nextInt(7));
        }

        if (input.contains("health tip") || input.contains("health") || input.contains("health advice")|| input.endsWith("tips"))
            return getRandomLineFromFile("healthtips.txt");
        if (input.contains("emergency") || input.contains("contacts") || input.endsWith("contacts") || input.endsWith("emergency contacts"))
            return getLinesFromFile("emergency.txt");
        if (input.contains("schedule") || input.contains("doctor schedule") || input.contains("doctor's schedule") || input.endsWith("schedule") || input.contains("show schedule"))
            return getLinesFromFile("schedule.txt");
        if (input.contains("list doctors") || input.contains("doctors") || input.contains("doctor") || input.endsWith("doctors") || input.contains("doctor list") || input.contains("show doctors"))
            return getLinesFromFile("doctors.txt");


        if (learnedResponses.containsKey(input))
            return learnedResponses.get(input);


        // Handle user rejection to teach the bot
        if (trainingMode && (input.contains("no") || input.contains("not now") || input.contains("later"))) {
            trainingMode = false;
            questionToLearn = "";
            return "Okay, I will learn it later.";
        }

        String smallTalk = getBestMatch(input, smallTalkMap);
        if (smallTalk != null)
            return smallTalk;

        String qnaResponse = getBestMatch(input, qnaMap);
        if (qnaResponse != null)
            return qnaResponse;

        // Trigger training mode if input is a question and not known
        if (input.startsWith("what is") || input.startsWith("who is") || input.startsWith("tell me about") || input.endsWith("?")) {
            trainingMode = true;
            questionToLearn = input;
            return "I don't know the answer to that. Would you like to teach me? If so, please type your answer directly";
        }


        return "Can you please rephrase your question? I don't understand.";
    }

    public String trainBot(String answer) {
        if (trainingMode && !questionToLearn.isEmpty()) {
            learnedResponses.put(questionToLearn, answer);
            appendLearnedResponse(questionToLearn, answer);
            trainingMode = false;
            questionToLearn = "";
            return "Thanks! I've learned a new response.";
        }
        return "I'm not in training mode right now.";
    }

    private void loadLearnedResponses() {
        loadResponsesFromFile("/data/" + LEARNED_FILE, learnedResponses);

        File externalFile = new File(System.getProperty("user.home") + "/medibot/" + LEARNED_FILE);
        if (externalFile.exists()) loadResponsesFromFile(externalFile.getAbsolutePath(), learnedResponses);
    }

    private void loadQnA() {
        loadResponsesFromFile("/data/qna.txt", qnaMap);
    }

    private void loadResponsesFromFile(String path, Map<String, String> map) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                path.startsWith("/") ? getClass().getResourceAsStream(path) : new FileInputStream(path)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    map.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Failed to load responses from: " + path);
        }
    }

    private void appendLearnedResponse(String question, String answer) {
        File dir = new File(System.getProperty("user.home") + "/medibot/");
        dir.mkdirs();
        File file = new File(dir, LEARNED_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(question + "=" + answer);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Failed to save learned response.");
        }
    }

    private String getSmallTalkResponse(String input) {
        return smallTalkMap.entrySet().stream()
                .filter(entry -> input.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public String getBestMatch(String input, Map<String, String> responseMap) {
        // First check if input contains any exact key from the map
        String exactMatch = responseMap.entrySet().stream()
                .filter(entry -> input.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (exactMatch != null) {
            return exactMatch;}
        // If no exact match found, proceed with fuzzy matching
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.ENGLISH);
        String bestMatch = null;
        int maxScore = 0;
        // Define a minimum threshold for a "good enough" match
        final int MIN_SCORE_THRESHOLD = 3; // Adjust based on testing

        for (String key : responseMap.keySet()) {
            int score = fuzzyScore.fuzzyScore(input.toLowerCase(), key.toLowerCase());

            // Only consider matches that meet the threshold
            if (score > maxScore && score >= MIN_SCORE_THRESHOLD) {
                maxScore = score;
                bestMatch = key;
            }
        }
        return bestMatch != null ? responseMap.get(bestMatch) : null;
    }





    private String getTimeBasedGreeting() {
        int hour = LocalTime.now().getHour();
        return hour < 12 ? "Good morning " : hour < 17 ? "Good afternoon " : "Good evening ";
    }

    private String getRandomGreeting() {
        String[] greetings = {
                "Hello "+userName+ "! How can I help you today?",
                "Hi "+userName+ "! Need any assistance?",
                "Hey "+userName+"! How can I assist?",
                "Greetings "+userName+"! What can I do for you?"

        };
        return greetings[random.nextInt(greetings.length)];
    }

    private String getRandomLineFromFile(String fileName) {
        List<String> lines = readLines(fileName);
        return lines.isEmpty() ? "No data available." : lines.get(random.nextInt(lines.size()));
    }

    private String getLinesFromFile(String fileName) {
        return String.join("\n", readLines(fileName));
    }

    private List<String> readLines(String fileName) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/data/" + fileName)))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
            return lines;
        } catch (IOException | NullPointerException e) {
            return List.of("File not found: " + fileName);
        }
    }

    public String welcomeMessage() {
        String[] welcomeMessages = {
                "Welcome to MediBot! How can I assist you today?",
                "Hello! I'm here to help you with your health queries.",
                "Hi there! What can I do for you today?",
                "Greetings! How can I make your day better?",
                "Welcome! Let's talk about your health.",
                "Welcome back!"
        };
        return welcomeMessages[random.nextInt(welcomeMessages.length)];
    }

    public boolean isTrainingMode() {
        return trainingMode;
    }

}
