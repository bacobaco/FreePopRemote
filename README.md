# FreePopRemote 📱📺 (v2.0.0)

[Français](#français) | [English](#english)

---

<a name="français"></a>
## 🇫🇷 Français

**FreePopRemote** est une application Android moderne conçue pour contrôler votre **Freebox Pop (Player)** via le réseau local en utilisant le protocole ADB (Android Debug Bridge). 

Cette version 2.0.0 apporte une autonomie totale et une intelligence accrue pour une expérience fluide sans configuration manuelle fastidieuse.

### ✨ Nouveautés de la Version 2.0.0
- **Auto-Découverte (mDNS)** : L'application trouve automatiquement votre Freebox sur le réseau Wi-Fi. Plus besoin de saisir l'adresse IP !
- **Connexion Persistante** : Gestion de la connexion en tâche de fond pour une réactivité instantanée même après avoir quitté l'application.
- **Mode Turbo (Seeking)** : Accélération progressive lors de l'appui long sur les flèches pour naviguer ultra-rapidement dans vos programmes.
- **Interface Premium** : Design "Dark Mode" ergonomique avec D-Pad élargi pour éviter les erreurs de manipulation.

### 🚀 Installation & Prérequis
1. **Activez le Débogage ADB** sur votre Freebox Pop (Paramètres > À propos > Cliquez 7 fois sur Build, puis dans Options développeurs > Débogage USB).
2. **Wi-Fi** : Assurez-vous que votre téléphone est sur le même réseau que la Freebox.
3. **Lancement** : Ouvrez l'appli, elle détectera la Freebox. Autorisez la connexion sur l'écran de votre TV lors de la première utilisation.

---

<a name="english"></a>
## 🇺🇸 English

**FreePopRemote** is a modern Android application designed to control your **Freebox Pop (Player)** over the local network using the ADB (Android Debug Bridge) protocol.

Version 2.0.0 introduces smart features and high persistence for a seamless experience without tedious manual configuration.

### ✨ New in Version 2.0.0
- **Auto-Discovery (mDNS)**: The app automatically finds your Freebox on the Wi-Fi network. No more manual IP entry!
- **Persistent Connection**: Background connection management for instant responsiveness even after switching apps.
- **Turbo Mode (Seeking)**: Progressive acceleration when long-pressing arrows to navigate lightning-fast through your programs.
- **Premium Interface**: Ergonomic Dark Mode design with an enlarged D-Pad to prevent accidental clicks.

### 🚀 Installation & Prerequisites
1. **Enable ADB Debugging** on your Freebox Pop (Settings > About > Tap Build 7 times, then in Developer Options > USB Debugging).
2. **Wi-Fi**: Ensure your phone is on the same network as your Freebox.
3. **Startup**: Open the app, and it will discover your Freebox. Grant permission on your TV screen during the first use.

---

## 🛠️ Stack Technique / Technical Stack

- **Language** : Kotlin
- **UI** : Jetpack Compose (Material 3)
- **Engine** : [adblib](https://github.com/tananaev/adblib)
- **Discovery** : Network Service Discovery (NSD/mDNS)

---
*Note : Cette application n'est pas une application officielle éditée par Free. / This is not an official app from Free.*
