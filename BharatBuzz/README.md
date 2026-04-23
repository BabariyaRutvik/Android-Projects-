# BharatBuzz - Stay Updated with the Latest News

**BharatBuzz** is a modern, fast, and feature-rich news application designed to keep users informed with the latest happenings across India and the world. With support for multiple languages and a personalized experience, BharatBuzz brings the news that matters to you, right at your fingertips.

## 🚀 Features

- **Multi-Category News**: Browse news across various categories including Sports, Technology, Business, Health, Entertainment, and Politics.
- **Multi-Language Support**: Access news in your preferred language. Currently supports:
  - English 🇺🇸
  - Hindi (हिंदी) 🇮🇳
  - Gujarati (ગુજરાતી) 🇮🇳
- **Personalized Experience**: 
  - **Bookmarks**: Save your favorite articles to read later.
  - **Search**: Quickly find news on specific topics.
- **Smart UI/UX**:
  - **Shimmer Effect**: Smooth loading experience.
  - **Swipe to Refresh**: Get the latest updates with a simple gesture.
  - **Dark Mode**: Comfortable reading experience in low-light environments.
- **User Authentication**: Secure login using Firebase and Google Sign-In.
- **Push Notifications**: Stay notified about breaking news.

## 🛠️ Tech Stack

- **Language**: Java
- **Architecture**: MVVM / MVC with ViewBinding
- **Networking**: [Retrofit](https://square.github.io/retrofit/) with Gson Converter
- **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Image Loading**: [Glide](https://github.com/bumptech/glide)
- **Backend & Auth**: Firebase Authentication, Firestore, and Realtime Database
- **Navigation**: Jetpack Navigation Component
- **UI Components**: Material Design 3, Shimmer, CircleImageView, SwipeRefreshLayout
- **Background Tasks**: WorkManager

## 📸 Screenshots

| Home Screen | Categories | Article View | Settings |
| :---: | :---: | :---: | :---: |
| ![Home](https://via.placeholder.com/200x400?text=Home+Screen) | ![Categories](https://via.placeholder.com/200x400?text=Categories) | ![Article](https://via.placeholder.com/200x400?text=Article+View) | ![Settings](https://via.placeholder.com/200x400?text=Settings) |

*(Note: Replace placeholders with actual app screenshots)*

## 📦 Project Structure

```text
com.example.bharatbuzz
├── Database       # Room database and DAO configurations
├── NewsActivity   # Main and Full Screen activities
├── NewsAdapter    # RecyclerView and ViewPager adapters
├── NewsFragment   # Categorized fragments (Sports, Tech, etc.)
├── NewsModel      # Data models/POJOs for API responses
├── Notification   # Firebase Cloud Messaging handlers
└── Service        # API clients and LocaleHelper for language switching
```

## ⚙️ Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/BharatBuzz.git
   ```
2. **Open in Android Studio**:
   Open the cloned folder in Android Studio.
3. **Firebase Setup**:
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add your Android app with the package name `com.example.bharatbuzz`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Enable Email/Password and Google Sign-In in the Firebase Auth section.
4. **Build and Run**:
   Sync Gradle and run the app on an emulator or physical device.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
Developed by [Rutvik](https://github.com/your-username)
