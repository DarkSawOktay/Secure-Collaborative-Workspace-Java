<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=d00000&height=180&section=header&text=Secure%20Workspace%20Java&fontSize=45&fontAlignY=35&animation=fadeIn&fontColor=ffffff"/>
  
  <br>

  ![Java](https://img.shields.io/badge/Backend-Java%20EE-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
  ![WebSockets](https://img.shields.io/badge/Network-WebSockets-000000?style=for-the-badge&logo=socket.io&logoColor=white)
  ![Security](https://img.shields.io/badge/Security-RBAC%20%26%20Auth-red?style=for-the-badge&logo=security&logoColor=white)
  ![MariaDB](https://img.shields.io/badge/Database-MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)

</div>

<br>

## 📄 About The Project

Ce projet est une plateforme de **travail collaboratif en temps réel** (similaire à un Drive partagé avec Chat intégré). Développé en **Java EE (Servlets/JSP)**, il permet aux utilisateurs de gérer des fichiers, de partager des dossiers et de communiquer en direct.

> *This project was originally developed as a group assignment. This repository highlights my focus on **Backend Security** and **Real-Time Architecture**.*

---

## 👨‍💻 My Contribution (Cybersecurity & Backend Focus)

Au sein de l'équipe, je me suis concentré sur l'architecture backend et la sécurisation des échanges :

### 🔐 1. Access Control (RBAC) & Security
Implémentation d'un système de **contrôle d'accès basé sur les rôles** pour prévenir les failles de type *Broken Access Control* (OWASP #1).
* **Middleware de Vérification :** Création de filtres (Servlets) pour vérifier les permissions avant chaque action critique (Lecture/Écriture/Suppression).
* **Session Management :** Gestion sécurisée des sessions utilisateurs pour éviter le *Session Hijacking*.
* **Secure Database Calls :** Utilisation de requêtes préparées pour bloquer les **Injections SQL**.

### 📡 2. Real-Time Architecture (WebSockets)
Développement du module de Chat et de Notifications en temps réel.
* **Java WebSockets (`@ServerEndpoint`) :** Gestion des connexions persistantes pour le chat multi-utilisateurs.
* **Concurrency Handling :** Gestion des accès concurrents pour éviter les conflits lors de l'édition simultanée ou de l'envoi de messages.

---

## 🛠️ Tech Stack

* **Backend:** Java 17, Jakarta EE (Servlets), WebSockets.
* **Database:** MariaDB (MySQL-compatible).
* **Frontend:** JSP, JavaScript (AJAX & WebSocket API), CSS3.
* **Tools:** Maven, Tomcat 10, Git.

---

## 💻 How to Run

### Prerequisites
* JDK 17+
* Apache Tomcat 10+
* MariaDB Server

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/DarkSawOktay/Secure-Collaborative-Workspace-Java.git
```


2. **Database Setup:**
Import the SQL scripts located in `sql/` folder into your MariaDB database:
```sql
source sql/init.sql;
source sql/user.sql;
```


3. **Configuration:**
For security reasons, this project uses Environment Variables.
Set the `DB_PASSWORD` variable on your machine, or update defaults in `src/main/java/models/DBConnection.java` (for local dev only).

4. **Build & Deploy:**
```bash
mvn clean package
```


Deploy the generated `.war` file to your Tomcat `webapps` folder.
5. **Access:**
Open browser at `http://localhost:8080/Secure-Collaborative-Workspace-Java`

---

<div align="center">
<sub>Portfolio Project by Oktay Gencer</sub>
</div>
