// typing-indicator.js - Version améliorée avec affichage personnalisé des utilisateurs qui écrivent
document.addEventListener('DOMContentLoaded', function() {
    console.log("Typing indicator initialization started");

    // Vérifier si la configuration requise existe
    if (typeof window.APP === 'undefined' || !window.APP.fileId || !window.APP.userId) {
        console.error("APP configuration missing for typing indicator");
        return;
    }

    // 1. Récupérer ou créer l'élément d'indicateur de frappe
    const typingIndicator = document.getElementById('typing-indicator') || createTypingIndicator();

    // 2. Établir la connexion WebSocket si elle n'existe pas déjà
    const wsConnection = setupWebSocket();

    // 3. Configurer l'écouteur d'événements pour détecter la frappe
    setupTypingDetection();

    // Liste des utilisateurs actuellement en train d'écrire (userId => {username, timestamp})
    const activeTypers = new Map();

    // Timer pour nettoyer les utilisateurs qui ont arrêté d'écrire
    let cleanupTimer = null;

    /**
     * Crée l'élément d'indicateur de frappe s'il n'existe pas déjà
     */
    function createTypingIndicator() {
        const existingIndicator = document.getElementById('typing-indicator');
        if (existingIndicator) {
            return existingIndicator;
        }

        const indicator = document.createElement('div');
        indicator.id = 'typing-indicator';
        indicator.className = 'text-sm text-gray-400 italic mb-2 px-2 py-1 rounded hidden';

        // Le contenu sera généré dynamiquement
        indicator.innerHTML = '<span class="typers"></span>';

        const chatWindow = document.getElementById('chat-window');
        if (chatWindow && chatWindow.parentNode) {
            chatWindow.parentNode.insertBefore(indicator, chatWindow.nextSibling);
            console.log("Typing indicator created and added to DOM");
            return indicator;
        } else {
            // Fallback: ajouter directement au body
            document.body.appendChild(indicator);
            console.log("Typing indicator created as overlay");
            return indicator;
        }
    }

    /**
     * Configure la connexion WebSocket pour l'indicateur de frappe
     */
    function setupWebSocket() {
        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${location.host}${window.APP.contextPath}/links-websocket`;

        // Utiliser une connexion WebSocket existante si disponible
        if (window.typingWs && window.typingWs.readyState === WebSocket.OPEN) {
            console.log("Using existing WebSocket connection");
            return window.typingWs;
        }

        console.log("Setting up new WebSocket connection for typing indicator:", wsUrl);
        const ws = new WebSocket(wsUrl);

        ws.onopen = function() {
            console.log("WebSocket connected for typing indicator");
            // S'abonner au canal
            ws.send(JSON.stringify({
                type: 'subscribe',
                fileId: window.APP.fileId,
                userId: window.APP.userId
            }));
        };

        ws.onmessage = function(event) {
            try {
                const msg = JSON.parse(event.data);

                // Traiter uniquement les messages de type "typing"
                if (msg.type === 'typing' && msg.fileId == window.APP.fileId) {
                    // Ignorer nos propres messages
                    if (msg.userId == window.APP.userId) {
                        return;
                    }

                    handleTypingStatus(msg.userId, msg.username || "Utilisateur " + msg.userId, msg.isTyping);
                }
            } catch (error) {
                console.error("Error processing message:", error);
            }
        };

        ws.onerror = function(error) {
            console.error("WebSocket error:", error);
        };

        ws.onclose = function() {
            console.log("WebSocket closed, reconnecting in 5s...");
            setTimeout(() => {
                window.typingWs = setupWebSocket();
            }, 5000);
        };

        // Stocker la connexion pour une réutilisation future
        window.typingWs = ws;
        return ws;
    }

    /**
     * Configure la détection de frappe dans la zone de chat
     */
    function setupTypingDetection() {
        const chatInput = document.getElementById('chat-input');
        if (!chatInput) {
            console.error("Chat input element not found");
            return;
        }

        let typingTimeout = null;
        let isTyping = false;

        chatInput.addEventListener('input', function() {
            if (!isTyping) {
                isTyping = true;
                sendTypingStatus(true);
            }

            // Réinitialiser le timer
            clearTimeout(typingTimeout);
            typingTimeout = setTimeout(function() {
                isTyping = false;
                sendTypingStatus(false);
            }, 2000);
        });

        // S'assurer que le statut est réinitialisé lors de l'envoi d'un message
        const sendButton = document.getElementById('chat-send');
        if (sendButton) {
            sendButton.addEventListener('click', function() {
                if (isTyping) {
                    isTyping = false;
                    clearTimeout(typingTimeout);
                    sendTypingStatus(false);
                }
            });
        }
    }

    /**
     * Envoie le statut de frappe au serveur
     */
    function sendTypingStatus(typing) {
        if (window.typingWs && window.typingWs.readyState === WebSocket.OPEN) {
            const username = window.APP.user ? window.APP.user.name : 'Utilisateur';

            window.typingWs.send(JSON.stringify({
                type: 'typing',
                fileId: window.APP.fileId,
                userId: window.APP.userId,
                isTyping: typing,
                username: username
            }));
        }
    }

    /**
     * Gère le statut de frappe d'un utilisateur
     * @param {number} userId - ID de l'utilisateur
     * @param {string} username - Nom d'utilisateur
     * @param {boolean} isTyping - Si l'utilisateur est en train d'écrire
     */
    function handleTypingStatus(userId, username, isTyping) {
        if (isTyping) {
            // Ajouter ou mettre à jour l'utilisateur dans la liste des tapeurs
            activeTypers.set(userId, {
                username: username,
                timestamp: Date.now()
            });
        } else {
            // Supprimer l'utilisateur de la liste
            activeTypers.delete(userId);
        }

        // Mettre à jour l'affichage
        updateTypingIndicator();

        // Configurer le nettoyage automatique si ce n'est pas déjà fait
        if (isTyping && !cleanupTimer) {
            cleanupTimer = setInterval(cleanupOldTypers, 3000);
        } else if (!isTyping && activeTypers.size === 0 && cleanupTimer) {
            clearInterval(cleanupTimer);
            cleanupTimer = null;
        }
    }

    /**
     * Nettoie les utilisateurs qui n'ont pas envoyé de mise à jour depuis trop longtemps
     */
    function cleanupOldTypers() {
        const now = Date.now();
        let changed = false;

        activeTypers.forEach((data, userId) => {
            // Si pas de mise à jour depuis 5 secondes, supprimer
            if (now - data.timestamp > 5000) {
                activeTypers.delete(userId);
                changed = true;
            }
        });

        if (changed) {
            updateTypingIndicator();
        }

        // Si plus personne n'écrit, arrêter le timer
        if (activeTypers.size === 0) {
            clearInterval(cleanupTimer);
            cleanupTimer = null;
        }
    }

    /**
     * Met à jour l'indicateur de frappe en fonction des utilisateurs actifs
     */
    function updateTypingIndicator() {
        if (activeTypers.size === 0) {
            typingIndicator.classList.add('hidden');
            return;
        }

        // Collecter les noms d'utilisateurs qui écrivent (sans l'utilisateur actuel)
        const typingUsernames = Array.from(activeTypers.values())
            .map(data => data.username)
            .filter(name => name !== window.APP.user.name);

        // Si personne d'autre n'écrit, masquer l'indicateur
        if (typingUsernames.length === 0) {
            typingIndicator.classList.add('hidden');
            return;
        }

        // Construire le texte avec la liste des utilisateurs
        let typingText = '';
        if (typingUsernames.length === 1) {
            typingText = `${typingUsernames[0]} est en train d'écrire...`;
        } else {
            typingText = `${typingUsernames.join(', ')} sont en train d'écrire...`;
        }

        // Mettre à jour le contenu et afficher
        const typersElement = typingIndicator.querySelector('.typers');
        if (typersElement) {
            typersElement.textContent = typingText;
        } else {
            typingIndicator.textContent = typingText;
        }

        typingIndicator.classList.remove('hidden');
    }
});