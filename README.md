# Athena 2.0

Athena 2.0 is a modern, desktop AI chat application built with Java Swing, featuring a sleek interface and powerful AI integration. It provides an intuitive chat experience with Mistral AI, complete with conversation persistence and customizable themes.

## Features

### Modern User Interface

- **Dark/Light Mode Toggle**: Switch between dark and light themes with a single click
- **Gradient Design**: Beautiful gradient backgrounds and chat bubbles for a premium look
- **Rounded Elements**: Modern rounded buttons, input fields, and chat bubbles
- **Custom Scrollbars**: Styled scrollbars that match the theme
- **Responsive Layout**: Adaptive UI that works across different window sizes

### AI Integration

- **Mistral AI Powered**: Integrated with Mistral AI's latest large language model
- **Real-time Responses**: Asynchronous API calls for smooth user experience
- **Typing Indicator**: Visual feedback when AI is generating responses
- **Conversation Context**: Maintains full conversation history for coherent responses

### Chat Features

- **Message Bubbles**: Distinctive chat bubbles for user and AI messages
- **Timestamps**: Automatic timestamping of all messages
- **Input Validation**: Prevents sending empty messages

### Data Persistence

- **MySQL Database**: Automatic database creation and table setup
- **Chat History**: Saves all conversations to local MySQL database

### Additional Features

- **Keyboard Shortcuts**: Enter key support for sending messages
- **Error Handling**: Graceful error handling for network issues and API failures
- **Cross-platform**: Runs on Windows, macOS, and Linux

## Technology Stack

- **Language**: Java 17+
- **UI Framework**: Java Swing with custom components
- **Build Tool**: Gradle
- **AI API**: Mistral AI (mistral-large-latest model)
- **HTTP Client**: OkHttp3
- **JSON Processing**: Jackson
- **Database**: MySQL
- **Look & Feel**: FlatLaf

## Dependencies

```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
    implementation 'com.formdev:flatlaf:3.4'
    implementation 'mysql:mysql-connector-java:8.0.33'
}
```

## Prerequisites

- Java 17 or higher
- MySQL Server running locally
- Internet connection for AI API calls

## Setup Instructions

1. **Clone the repository**:

   ```bash
   git clone <repository-url>
   cd athena-2.0
   ```

2. **Configure MySQL**:

   - Ensure MySQL server is running on localhost:3306
   - Update database credentials in `DatabaseManager.java` if needed:
     ```java
     private static final String DB_USER = "your-username";
     private static final String DB_PASSWORD = "your-password";
     ```

3. **Build the project**:

   ```bash
   ./gradlew build
   ```

4. **Run the application**:
   ```bash
   ./gradlew run
   ```

## Project Structure

```
athena-2.0/
├── app/
│   ├── src/main/java/org/example/
│   │   ├── App.java                 # Main application class with UI
│   │   ├── DatabaseManager.java     # MySQL database operations
│   │   └── Requests.java            # Mistral AI API integration
│   └── build.gradle                 # Module dependencies
├── gradle/
├── gradlew                          # Gradle wrapper
├── gradlew.bat
├── settings.gradle
└── README.md
```

## Key Components

### App.java

The main application class containing:

- Custom UI components (ModernButton, ChatBubble, GradientPanel)
- Theme management system
- Message handling and display logic
- Event listeners for user interactions

### DatabaseManager.java

Handles all database operations:

- Automatic database and table creation
- Chat history persistence
- Connection management

### Requests.java

Manages AI API communication:

- HTTP requests to Mistral AI
- JSON serialization/deserialization
- Error handling for API responses

## Customization

### Themes

The application supports extensive theming through color constants in `App.java`:

- Primary colors for backgrounds
- Gradient definitions for chat bubbles
- Text and accent colors

### API Configuration

Modify `Requests.java` to change:

- AI model selection
- API endpoints
- Authentication keys

### Database Schema

The chat history table structure can be modified in `DatabaseManager.java`:

```sql
CREATE TABLE chat_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_message TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Mistral AI for providing the language model
- FlatLaf for enhanced Swing look and feel
