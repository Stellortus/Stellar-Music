
# Stellar Music 🎵

**Stellar Music** is a modern, lightweight music streaming client for Android, built entirely with **Kotlin** and **Jetpack Compose**.

> **Note:** This application is a client-side interface only. It **does not** bundle any music files or media resources. It functions as a frontend that dynamically requests audio streams and metadata from a user-configured backend server.

## 🚀 Features

- **Modern UI:** Built 100% with Jetpack Compose for a fluid, declarative UI experience.
- **Dynamic Streaming:** Fetches audio streams and album art from a remote server.
- **Asynchronous Networking:** Uses **Ktor Client** for efficient, non-blocking API requests.
- **Clean Architecture:** Separation of concerns between UI, Data, and Network layers (MVVM).
- **State Management:** Reactive UI states using Kotlin Flow and Compose State.
- **Offline Metadata:** Previously loaded songs, playlists, and playlist details remain available after restarting without a network connection.

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Networking:** [Ktor Client](https://ktor.io/docs/client.html)
- **Local Storage:** Room, with separate `tracks.db` and `playlists.db` databases.

## Local data flow

Compose calls `TrackManager.getMusicList`, `PlaylistManager.listPlaylists`, and `PlaylistManager.getPlaylist` using ordinary suspend functions. A cache hit returns the saved snapshot immediately and starts one background synchronization. A cache miss waits for the network result to be saved, then reads the database again before returning. Screens do not observe cache changes; synchronized data appears on the next call. Song pages are cached separately by their server offset, and concurrent synchronization of the same key is shared.

Signing out or invalidating a session cancels cache requests and clears both databases. Audio uses the existing stream cache; metadata caching does not download audio files. Search continues to use the network.

## ⚙️ Configuration (Crucial Step)

Because this app relies on a custom backend, you must configure the server endpoint before building.


1.  Navigate to the network configuration file (`app/src/main/java/top/stellortus/stellarmusic/network/ServerInfo.kt`).
2.  Update the `BASE_URL` constant to point to your running server.

```kotlin
package top.stellortus.stellarmusic.network

const val Domain = "example.com"
```

*Note: Ensure your server returns data compatible with the app's data models, see [Stellar-Music-Server]()*

## 🏗 Getting Started

### Prerequisites

- Android Studio Ladybug (or newer)
- JDK 17+
- Android SDK 34+

### Installation

1.  **Clone the repository**
    ```bash
    git clone https://github.com/Stellortus/Stellar-Music.git
    ```

2.  **Open in Android Studio**
    Open the project folder in Android Studio.

3.  **Configure the Server**
    As mentioned in the **Configuration** section, ensure `Domain` points to a valid server.

4.  **Build and Run**
    - Select your emulator or physical device.
    - Click the **Run** button (green arrow).

## ⚠️ Disclaimer

This app is a client interface and does not host, store, or distribute copyrighted music. The developer is not responsible for the content served by third-party backend servers configured by the user.
