# FreePopRemote 📱📺

**FreePopRemote** est une application Android moderne conçue pour contrôler votre **Freebox Pop (Player)** via le réseau local en utilisant le protocole ADB (Android Debug Bridge). 

Oubliez la télécommande physique égarée ! Avec une interface sombre, élégante et ergonomique, pilotez votre expérience TV directement depuis votre smartphone.

---

## ✨ Fonctionnalités

- **Contrôle Complet** : Pavé numérique, touches de direction (D-Pad), volume et changement de chaînes.
- **Raccourcis Intelligents** : Bouton Power, Home, Retour et Paramètres.
- **Connexion Persistante** : Gestion automatique des clés de sécurité pour une connexion sans interruption.
- **Interface Moderne** : Design "Dark Mode" optimisé avec Jetpack Compose.
- **Configuration Facile** : Changement d'adresse IP à la volée.

---

## 🚀 Installation & Prérequis

### 1. Préparer votre Freebox Pop
Pour que l'application puisse communiquer avec votre Freebox, vous devez activer le **Débogage USB (réseau)** :

1. Allez dans les **Paramètres** de votre Freebox Pop (roue dentée en haut à droite).
2. Naviguez vers **Préférences relatives à l'appareil** > **À propos**.
3. Descendez tout en bas sur **Build** et cliquez 7 fois dessus jusqu'à voir le message "Vous êtes maintenant développeur".
4. Revenez en arrière et allez dans le nouveau menu **Options pour les développeurs**.
5. Activez l'option **Débogage USB**.

### 2. Récupérer l'adresse IP
1. Allez dans **Paramètres** > **Réseau et Internet**.
2. Notez l'adresse IP de votre Freebox (ex: `192.168.1.XX`).

### 3. Installer l'application
- Clonez ce dépôt.
- Ouvrez le projet dans **Android Studio**.
- Compilez et installez l'APK sur votre smartphone.
- Au premier lancement, saisissez l'IP de votre Freebox. **Une fenêtre apparaîtra sur votre TV** vous demandant d'autoriser le débogage : cochez "Toujours autoriser" et validez.

---

## 🛠️ Stack Technique

- **Langage** : Kotlin
- **UI** : Jetpack Compose (Material 3)
- **Communication** : [adblib](https://github.com/tananaev/adblib) pour les commandes réseau.
- **Asynchronisme** : Kotlin Coroutines.

---

## 📸 Aperçu

| Navigation | Contrôle Volume/CH | Paramètres IP |
| :---: | :---: | :---: |
| D-Pad tactile | Rockers ergonomiques | Configuration simple |

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Si vous souhaitez améliorer le design ou ajouter des fonctionnalités (recherche vocale, lancement d'apps directes comme Netflix), n'hésitez pas à :
1. Forker le projet.
2. Créer une branche pour votre fonctionnalité.
3. Envoyer une Pull Request.

---

## ⚖️ Licence

Ce projet est distribué sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

---
*Note : Cette application n'est pas une application officielle éditée par Free.*
