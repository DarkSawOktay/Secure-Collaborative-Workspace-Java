<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // Vérifier si l'utilisateur est connecté
    models.Utilisateurs utilisateur = (models.Utilisateurs)session.getAttribute("utilisateur");
    if (utilisateur == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Récupérer l'ID de l'autre utilisateur avec gestion d'erreur
    int withId = 0;
    String peerName = ""; // Nom de l'utilisateur avec qui on chatte

    try {
        // Récupérer et valider le paramètre 'with'
        String withParam = request.getParameter("with");
        if (withParam == null || withParam.isEmpty()) {
            // Paramètre manquant
%>
<div style="color: red; text-align: center; margin: 20px;">
    Erreur : ID utilisateur manquant. Veuillez spécifier un utilisateur en utilisant le paramètre "with".
    <br><br>
    <a href="${pageContext.request.contextPath}/IndexServlet">Retour à l'accueil</a>
</div>
<%
        return; // Arrêter l'exécution de la page
    }

    // Convertir le paramètre en entier
    withId = Integer.parseInt(withParam);


    try {
        java.sql.Connection conn = models.DBConnection.getConnection();
        java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT nom FROM utilisateurs WHERE id = ?");
        stmt.setInt(1, withId);
        java.sql.ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            peerName = rs.getString("nom");
        } else {
            peerName = "Utilisateur " + withId;
        }

        rs.close();
        stmt.close();
        conn.close();
    } catch (Exception e) {
        // En cas d'erreur, utiliser un nom par défaut
        peerName = "Utilisateur " + withId;
        e.printStackTrace();
    }

} catch (NumberFormatException e) {
    // Le paramètre n'est pas un nombre valide
%>
<div style="color: red; text-align: center; margin: 20px;">
    Erreur : L'ID utilisateur spécifié n'est pas valide.
    <br><br>
    <a href="${pageContext.request.contextPath}/IndexServlet">Retour à l'accueil</a>
</div>
<%
        return; // Arrêter l'exécution de la page
    }
%>
<!DOCTYPE html>
<html lang="fr" class="h-full">
<head>
    <meta charset="UTF-8">
    <title>Chat avec <%= peerName %></title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        #messages {
            display: flex;
            flex-direction: column;
            padding: 1rem;
            gap: 0.5rem;
        }

        .message-sent {
            background-color: #3b82f6;
            color: white;
            border-radius: 1rem;
            padding: 0.5rem 1rem;
            margin-left: auto;
            margin-right: 1rem;
            max-width: 80%;
            word-wrap: break-word;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        }

        .message-received {
            background-color: #4b5563;
            color: white;
            border-radius: 1rem;
            padding: 0.5rem 1rem;
            margin-right: auto;
            margin-left: 1rem;
            max-width: 80%;
            word-wrap: break-word;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        }

        #typing-indicator {
            margin: 0.5rem;
            padding: 0.5rem 1rem;
            background-color: rgba(55, 65, 81, 0.3);
            border-radius: 0.5rem;
            color: #e5e7eb;
            transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out;
        }

        #typing-indicator.hidden {
            display: none;
            opacity: 0;
            transform: translateY(10px);
        }

        .typing-dots {
            display: inline-flex;
            align-items: center;
        }

        .typing-dots span {
            width: 5px;
            height: 5px;
            margin: 0 2px;
            background-color: #e5e7eb;
            border-radius: 50%;
            display: inline-block;
            animation: pulse 1.5s infinite;
        }

        .typing-dots span:nth-child(2) {
            animation-delay: 0.2s;
        }

        .typing-dots span:nth-child(3) {
            animation-delay: 0.4s;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(0.8); opacity: 0.5; }
            50% { transform: scale(1); opacity: 1; }
        }
    </style>
</head>
<body class="flex flex-col h-full bg-gray-900 text-white">
<!-- Données cachées pour JavaScript -->
<input type="hidden" id="user-id" value="${utilisateur.getIdUtilisateur()}">
<input type="hidden" id="user-name" value="${utilisateur.getNomUtilisateur()}">
<input type="hidden" id="peer-id" value="<%= withId %>">
<input type="hidden" id="peer-name" value="<%= peerName %>">
<input type="hidden" id="context-path" value="${pageContext.request.contextPath}">

<!-- Header -->
<header class="bg-gray-800 p-4 shadow-md">
    <div class="container mx-auto flex items-center justify-between">
        <div class="flex items-center">
            <a href="${pageContext.request.contextPath}/IndexServlet" class="text-xl font-semibold text-blue-400 mr-4">
                Retour
            </a>
            <h1 class="text-xl font-semibold">Chat avec <%= peerName %></h1>
        </div>
    </div>
</header>


<!-- Messages Container -->
<div id="messages" class="flex-1 p-4 overflow-y-auto">
    <div class="text-center text-gray-400 mb-4">Début de la conversation</div>
</div>

<!-- Typing Indicator -->
<div id="typing-indicator" class="hidden">
    <span class="username">L'autre utilisateur</span> est en train d'écrire
    <div class="typing-dots inline-block">
        <span></span>
        <span></span>
        <span></span>
    </div>
</div>

<!-- Input Area -->
<div class="p-3 bg-gray-800 border-t border-gray-700">
    <div class="flex rounded overflow-hidden">
        <input id="msg-input" type="text" placeholder="Écrire un message..." class="flex-1 p-3 bg-gray-700 border-none outline-none text-white" />
        <button id="send-btn" class="px-6 py-3 bg-blue-600 text-white font-medium">
            Envoyer
        </button>
    </div>
</div>

<!-- Scripts à la fin du body -->
<script src="${pageContext.request.contextPath}/js/chat-page.js"></script>
<script>
    console.log("Page chat.jsp chargée, vérification configuration:", {
        myId: document.getElementById('user-id').value,
        myName: document.getElementById('user-name').value,
        peerId: document.getElementById('peer-id').value,
        contextPath: document.getElementById('context-path').value
    });
</script>

</body>
</html>