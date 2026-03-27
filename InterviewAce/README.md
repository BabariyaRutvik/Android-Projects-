# 🎙️ InterviewAce - AI-Powered Interview Preparation

InterviewAce is a modern Android application designed to help job seekers, students, and professionals master their interview skills. Using Google's **Gemini AI**, the app provides real-time, personalized feedback on interview answers, helping users improve their technical accuracy, communication, and confidence.

---

## ✨ Features

- **🤖 AI Interviewer**: Engage in simulated interviews for various job roles and difficulty levels.
- **⚡ Real-time Feedback**: Get instant analysis of your answers powered by Gemini AI, including scores for accuracy, communication, and completeness.
- **📊 Progress Tracking**: Visualize your improvement over time with detailed charts and session history.
- **🏆 Certificates**: Earn digital certificates for outstanding performance in practice sessions.
- **🔊 Smart TTS & Speech**: Voice-enabled questions and speech-to-text for a hands-free, realistic interview experience.
- **⏰ Daily Reminders**: Stay consistent with automated reminders (10 AM, 3 PM, 9 PM) to reach your daily goal.
- **🌍 Multilingual Support**: Practice in English, Hindi, or Gujarati.

---

## 🛠️ Tech Stack

- **Language**: Java 
- **UI Framework**: XML (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **AI Engine**: Google Gemini AI (Generative AI SDK)
- **Backend**: Firebase (Auth & Firestore)
- **Local Database**: SharedPreferences
- **Networking**: Retrofit & OkHttp
- **Background Tasks**: WorkManager
- **Data Viz**: MPAndroidChart

---

## 🚀 Getting Started

### 1. Prerequisites
- Android Studio Ladybug or later
- JDK 17
- A Firebase Project (for Authentication and Firestore)
- A Gemini API Key from [Google AI Studio](https://aistudio.google.com/)

### 2. Security & API Keys (Crucial)
To protect sensitive information, the **Gemini API Key** is hidden from the source code and version control.

1. Locate the `local.properties` file in your project root.
2. Add your API key there:
   ```properties
   GEMINI_API_KEY=YOUR_API_KEY_HERE
   ```
3. The app's `build.gradle.kts` will automatically inject this key into `BuildConfig` during compilation.

### 3. Setup Firebase
1. Download your `google-services.json` from the Firebase Console.
2. Place it in the `app/` directory of the project.
3. Enable Email/Google Sign-in and Firestore in your Firebase console.

---

## 📸 Screenshots

| Home | AI Interview | Feedback | Profile |
| :---: | :---: | :---: | :---: |
| <img src="screenshots/home.png" width="200"> | <img src="screenshots/interview.png" width="200"> | <img src="screenshots/feedback.png" width="200"> | <img src="screenshots/profile.png" width="200"> |

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

**Developed with ❤️ by Rutvik Babariya**
