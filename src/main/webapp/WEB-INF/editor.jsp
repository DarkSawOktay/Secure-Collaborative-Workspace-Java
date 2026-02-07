<%@ page session="true" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="fr" class="h-full">
<head>
    <meta charset="UTF-8"/>
    <title>Éditeur Collaboratif</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/ace/1.9.6/ace.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/drag-drop.css">
    <style>
        /* une fine barre verticale rouge pour le curseur distant */
        .ace_remote-cursor {
            position: absolute;
            background-color: rgba(255, 0, 0, 0.7);
            width: 2px !important;
            z-index: 20;
        }

        .hidden {
            display: none;
        }

        /* Styles pour l'indicateur de frappe */
        #typing-indicator {
            background-color: rgba(243, 244, 246, 0.7);
            border: 1px solid #e5e7eb;
            animation: fadeIn 0.3s ease-in;
            transition: opacity 0.3s ease-out;
        }

        .dark #typing-indicator {
            background-color: rgba(55, 65, 81, 0.7);
            border-color: #4b5563;
        }

        #typing-indicator.hidden {
            display: none;
            opacity: 0;
        }

        #typing-indicator .username {
            font-weight: 600;
            color: #4f46e5;
        }

        .dark #typing-indicator .username {
            color: #818cf8;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(5px); }
            to { opacity: 1; transform: translateY(0); }
        }

        /* Amélioration de la mise en page pour le chat et les boutons */
        .chat-container {
            width: 250px;
            display: flex;
            flex-direction: column;
            height: 100%;
        }

        .chat-input-container {
            display: flex;
            margin-top: 5px;
        }

        .chat-input-container input {
            flex-grow: 1;
            min-width: 0; /* Important pour que l'input puisse rétrécir */
        }

        .chat-input-container button {
            white-space: nowrap;
            padding: 8px;
            min-width: 70px;
        }

        /* Compact header bar */
        .compact-header {
            padding: 5px;
            background-color: rgba(229, 231, 235, 0.5);
            border-radius: 4px;
            margin-bottom: 10px;
        }

        .dark .compact-header {
            background-color: rgba(55, 65, 81, 0.5);
        }

        /* Pour les boutons headers et les autres contrôles */
        .action-buttons {
            display: flex;
            gap: 5px;
        }

        .action-buttons button {
            padding: 5px 10px;
            font-size: 0.8rem;
        }

        /* Style pour les zones d'édition */
        #filename, #filename-header {
            padding: 5px;
            height: 30px;
        }
    </style>
</head>
<body class="flex flex-col h-full bg-gray-100 text-gray-900 dark:bg-gray-900 dark:text-gray-100">

<script>
    // IMPORTANT: Récupérer le projectId de sessionStorage au chargement, s'il existe
    const storedProjectId = sessionStorage.getItem('lastProjectId');

    window.APP = {
        contextPath: '${pageContext.request.contextPath}',
        <c:choose>
        <c:when test="${not empty fileId}">
        fileId: ${fileId},
        </c:when>
        <c:otherwise>
        fileId: null,
        </c:otherwise>
        </c:choose>
        userId: ${sessionScope.utilisateur.idUtilisateur},
        fileName: '<c:out value="${fileName != null ? fileName : ''}" />',
        // IMPORTANT: Inclure le projectId s'il est disponible, sinon utiliser celui du sessionStorage
        <c:choose>
        <c:when test="${not empty projectId}">
        projectId: ${projectId},
        </c:when>
        <c:when test="${empty projectId && not empty param.projectId}">
        projectId: ${param.projectId},
        </c:when>
        <c:otherwise>
        projectId: ${not empty storedProjectId ? storedProjectId : 'null'},
        </c:otherwise>
        </c:choose>
    };

    // IMPORTANT: Stocker le projectId dans sessionStorage s'il est disponible
    if (window.APP.projectId && window.APP.projectId !== 'null') {
        console.log("Mise à jour du projectId dans sessionStorage:", window.APP.projectId);
        sessionStorage.setItem('lastProjectId', window.APP.projectId);
    }
</script>

<!-- Navbar -->
<header class="bg-white dark:bg-gray-800 shadow">
    <div class="container mx-auto flex items-center justify-between p-2">
        <div class="flex items-center space-x-6">
            <a href="${pageContext.request.contextPath}/IndexServlet" class="text-xl font-semibold hover:text-blue-600 dark:hover:text-blue-400">Mon IDE</a>
            <a href="${pageContext.request.contextPath}/ListeUtilisateursServlet" class="hover:underline">Utilisateurs</a>

            <!-- MODIFICATION: Ajouter showSelect=true au lien de l'éditeur -->
            <c:choose>
                <c:when test="${not empty projectId}">
                    <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true" class="hover:underline">Éditeur</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true" class="hover:underline">Éditeur</a>
                </c:otherwise>
            </c:choose>
        </div>


        <!-- Barre de contrôle compacte -->
        <div class="action-buttons">
            <div class="flex items-center">
                <input id="filename" type="text" value="${fn:escapeXml(fileName)}" class="w-40 border border-gray-300 rounded bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"/>
                <button id="rename-btn" class="ml-1 bg-blue-600 text-white rounded hover:bg-blue-500">Renommer</button>
            </div>

            <button id="collaborators-btn" class="bg-purple-600 text-white rounded hover:bg-purple-500 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v1h8v-1zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-1a3 3 0 00-3-3h-2a3 3 0 00-3 3v1h8z"></path>
                </svg>
                Collaborateurs
            </button>

            <div id="save-status" class="text-sm italic text-gray-600 dark:text-gray-400 self-center"></div>

            <button onclick="toggleTheme()" class="p-1 rounded bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 focus:outline-none ml-2">
                <span id="theme-icon"></span>
            </button>
        </div>
    </div>
</header>

<div class="flex flex-1 overflow-hidden">

    <!-- Sidebar -->
    <aside class="w-64 bg-gray-200 dark:bg-gray-800 p-4 overflow-y-auto">
        <div id="folder-tree"></div>
    </aside>

    <!-- Volet de collaborateurs (initialement masqué) -->
    <div id="collaborators-panel" class="fixed inset-y-0 right-0 w-80 bg-white dark:bg-gray-800 shadow-lg transform translate-x-full transition-transform duration-300 ease-in-out z-50 flex flex-col">
        <!-- En-tête du panneau -->
        <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
            <h2 class="text-lg font-semibold">Collaborateurs</h2>
            <button id="close-collaborators-panel" class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </div>

        <!-- Contenu du panneau -->
        <div class="flex-1 overflow-y-auto p-4 space-y-6">
            <!-- Collaborateurs actifs -->
            <div>
                <h3 class="font-semibold mb-2 text-gray-700 dark:text-gray-300">Collaborateurs actifs</h3>
                <div id="active-collaborators" class="space-y-2">
                    <div class="text-sm text-gray-500 dark:text-gray-400 animate-pulse">Chargement...</div>
                </div>
            </div>

            <!-- Invitations en attente -->
            <div>
                <h3 class="font-semibold mb-2 text-gray-700 dark:text-gray-300">Invitations en attente</h3>
                <div id="pending-invitations" class="space-y-2">
                    <div class="text-sm text-gray-500 dark:text-gray-400 animate-pulse">Chargement...</div>
                </div>
            </div>

            <!-- Inviter un collaborateur -->
            <div>
                <h3 class="font-semibold mb-2 text-gray-700 dark:text-gray-300">Inviter un collaborateur</h3>
                <div class="space-y-2">
                    <input type="text" id="collaborator-pseudo" placeholder="Pseudo du collaborateur" class="w-full p-2 border rounded-md bg-gray-50 dark:bg-gray-700 dark:text-gray-100">
                    <div class="flex space-x-2">
                        <select id="collaborator-role" class="flex-1 p-2 border rounded-md bg-gray-50 dark:bg-gray-700 dark:text-gray-100">
                            <option value="viewer">Lecteur (vue seule)</option>
                            <option value="editor" selected>Éditeur (modification)</option>
                        </select>
                        <button id="invite-collaborator-btn" class="px-3 py-2 bg-green-600 text-white rounded-md hover:bg-green-700">Inviter</button>
                    </div>
                    <div id="invite-status" class="text-sm"></div>
                </div>
            </div>
        </div>
    </div>

    <!-- Main Editor Area -->
    <main class="flex-1 flex flex-col p-4 overflow-hidden">
        <!-- MODIFICATION: Afficher le projet actuel si disponible -->
        <c:if test="${not empty projectId}">
            <div class="text-sm text-gray-500 dark:text-gray-400 mb-2">
                Projet: <span id="current-project-id" class="font-semibold">${projectId}</span>
            </div>
        </c:if>

        <!-- ACE Editor -->
        <div id="ace-editor" class="flex-1 border border-gray-300 rounded shadow-sm h-full"></div>
        <textarea id="initial-content" class="hidden"><c:out value="${content}" escapeXml="true"/></textarea>
    </main>

    <!-- Chat de projet -->
    <div class="chat-container bg-gray-800 dark:bg-gray-900">
        <h3 class="font-semibold mb-2 text-white">Chat de projet</h3>

        <!-- Liste des messages -->
        <div id="chat-window" class="overflow-y-auto overflow-x-hidden border border-gray-700 dark:border-gray-800 bg-gray-800 dark:bg-gray-900 rounded">
            <div class="text-gray-400 italic text-sm">Chargement du chat...</div>
        </div>

        <!-- Indicateur de frappe -->
        <div id="typing-indicator" class="text-sm text-gray-400 italic mb-2 px-2 py-1 rounded hidden">
            <span class="username">Un utilisateur</span> est en train d'écrire...
        </div>

        <!-- Zone de saisie et bouton d'envoi -->
        <div class="chat-input-container">
            <input id="chat-input" type="text" placeholder="Écrire un message…" class="bg-gray-700 dark:bg-gray-800 text-gray-100 border-gray-600 dark:border-gray-700 rounded"/>
            <button id="chat-send" class="bg-blue-600 hover:bg-blue-700 text-white rounded">Envoyer</button>
        </div>
    </div>
</div>

<script>
    // Configuration de l'utilisateur actuel pour l'indicateur de frappe
    window.APP = window.APP || {};
    window.APP.user = {
        name: "${sessionScope.utilisateur.nomUtilisateur}",
        id: ${sessionScope.utilisateur.idUtilisateur}
    };

    console.log("Informations de la session:", {
        fileId: window.APP.fileId,
        projectId: window.APP.projectId,
        storedProjectId: sessionStorage.getItem('lastProjectId'),
        userId: window.APP.userId,
        fileName: window.APP.fileName
    });
</script>

<!-- librairies externes -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/ace/1.9.6/ace.js"></script>

<!-- modules JS -->
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
<script src="${pageContext.request.contextPath}/js/ace-init.js"></script>
<script src="${pageContext.request.contextPath}/js/realtime.js"></script>
<script src="${pageContext.request.contextPath}/js/autosave.js"></script>
<script src="${pageContext.request.contextPath}/js/permissions.js"></script>
<script src="${pageContext.request.contextPath}/js/ui.js"></script>
<script src="${pageContext.request.contextPath}/js/invite.js"></script>
<script src="${pageContext.request.contextPath}/js/collaborators.js"></script>
<script src="${pageContext.request.contextPath}/js/sidebarStructure.js"></script>
<script src="${pageContext.request.contextPath}/js/chat.js"></script>
<script src="${pageContext.request.contextPath}/js/collaborationsPanel.js"></script>
<script src="${pageContext.request.contextPath}/js/typing-indicator.js"></script>
<script src="${pageContext.request.contextPath}/js/drag-drop.js"></script>

</body>
</html>