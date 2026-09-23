# FoodLik

FoodLik is a modern Android application for food discovery and delivery, built with Java and following clean architecture principles.

## 🚀 Features

*   **User Authentication**: Secure Login and Signup screens to manage user accounts.
*   **Restaurant Discovery**: Browse a variety of restaurants with detailed menus.
*   **Cart Management**: Easily add items to your cart and manage your orders.
*   **Order Tracking**: Real-time tracking of your food orders.
*   **Order History**: Keep track of your past cravings and reorder with ease.
*   **User Settings**: Personalize your profile and application preferences.
*   **Interactive Maps**: Integrated Google Maps for restaurant location and delivery tracking.

## 🛠 Tech Stack

*   **Language**: Java
*   **UI Framework**: Android Material Design, ConstraintLayout
*   **Image Loading**: [Glide](https://github.com/bumptech/glide)
*   **Networking**: [Retrofit](https://square.github.io/retrofit/) with GSON converter
*   **Maps**: Google Play Services Maps
*   **Architecture**: Fragment-based UI with a structured data layer (Model-Adapter-Network).

## 📋 Prerequisites

*   Android Studio Jellyfish or newer.
*   Android SDK 35 (Compile SDK).
*   Minimum SDK: API 23 (Android 6.0 Marshmallow).
*   Google Maps API Key (required for map features).

## ⚙️ Installation & Setup

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/FoodLik.git
    ```
2.  **Open in Android Studio**:
    Open the `android` folder as an existing project.
3.  **API Configuration**:
    Update the `DEFAULT_API_BASE_URL` in `app/build.gradle` or via project properties to point to your backend service.
4.  **Google Maps API**:
    Add your API key in `AndroidManifest.xml` or `secrets.xml` (if configured).
5.  **Build & Run**:
    Sync Gradle and run the app on an emulator or a physical device.

## 📁 Project Structure

*   `ma.ensa.foodlik.ui`: Contains Fragments and Activities for the user interface.
*   `ma.ensa.foodlik.adapter`: RecyclerView adapters for lists (Restaurants, Menu, etc.).
*   `ma.ensa.foodlik.network`: Retrofit client and API interface definitions.
*   `ma.ensa.foodlik.model`: Data models and POJOs.
*   `ma.ensa.foodlik.data`: Data handling and repositories.

---
Developed as part of the Mobile Development course (S8/DEVMOBILE).
