# MediBot - Hospital Service Chatbot

## Overview
MediBot is an intelligent hospital service chatbot designed to assist patients and hospital visitors with health-related queries, doctor information, appointment scheduling, and emergency contacts. The chatbot leverages natural language processing and a dynamic knowledge base to provide helpful responses, making healthcare information easily accessible.

---

## Features
- **Conversational Health Assistant:** Handles greetings, small talk, and common health questions.
- **Dynamic Q&A:** Loads questions and answers from external files for easy updates.
- **Symptom Checker:** Provides basic advice based on reported symptoms.
- **Doctor Information:** Lists doctors and their schedules.
- **Appointment Management:** (Planned/Optional) Book and cancel appointments.
- **Learn from Users:** Can be trained with new question-answer pairs during conversation.
- **Personalized Interaction:** Remembers user names and responds accordingly.
- **Repeated Question Detection:** Alerts user if asking the same question multiple times.
- **Time-Based Greetings:** Offers greetings depending on the time of day.
- **Health Tips & Emergency Contacts:** Shares random health tips and emergency phone numbers.

---

## Technologies Used
- **Java 17+**
- **JavaFX (FXML)** for desktop GUI frontend
- **Spring Boot** (optional for REST API backend)
- **Hibernate & MySQL** (for database integration, planned)
- **Maven** for project management
- **Lombok** to reduce boilerplate code

---

## Project Structure
- `src/main/java/lk/medi/medibot/` — Core chatbot logic and GUI controllers
- `src/main/resources/data/` — Text files for Q&A, health tips, doctors list, schedule, emergency contacts, etc.
- `src/main/resources/images/` — Chatbot GUI images (e.g., greeting, annoyed, goodbye faces)

---

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven
- IntelliJ IDEA or other Java IDE
- (Optional) MySQL for database integration

### How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/medibot.git
