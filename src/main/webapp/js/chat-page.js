document.addEventListener('DOMContentLoaded', function() {
    console.log('Initialisation du chat entre amis...');

    // Récupération des éléments DOM - Suppression de debugWSState
    const elements = {
        messagesContainer: document.getElementById('messages'),
        typingIndicator: document.getElementById('typing-indicator'),
        msgInput: document.getElementById('msg-input'),
        sendBtn: document.getElementById('send-btn')
    };

    // Récupération des données utilisateur
    const myId = parseInt(document.getElementById('user-id').value, 10);
    const myName = document.getElementById('user-name').value;
    const peerId = parseInt(document.getElementById('peer-id').value, 10);
    const peerName = document.getElementById('peer-name').value; // Ajouté pour utiliser le nom du destinataire
    const contextPath = document.getElementById('context-path').value;

    console.log('Information utilisateur:', {
        myId: myId,
        myName: myName,
        peerId: peerId,
        peerName: peerName,
        contextPath: contextPath
    });

    // Variables d'état
    const state = {
        socket: null,
        isConnected: false,
        isTyping: false,
        typingTimeout: null,
        reconnectAttempts: 0,
        maxReconnectAttempts: 5
    };

    /**
     * Initialise la connexion WebSocket
     */
    function initWebSocket() {
        const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
        const wsUrl = protocol + '://' + window.location.host + contextPath + '/user-messages-ws';

        console.log('Tentative de connexion WebSocket à:', wsUrl);

        try {
            state.socket = new WebSocket(wsUrl);

            state.socket.onopen = function(event) {
                console.log('WebSocket connecté pour le chat utilisateur');
                state.isConnected = true;
                state.reconnectAttempts = 0;

                // AJOUT: Envoyer un message de souscription comme dans le chat de projet
                const subscribeMsg = {
                    type: 'subscribe',
                    fileId: peerId, // On utilise peerId comme identifiant unique
                    userId: myId
                };
                state.socket.send(JSON.stringify(subscribeMsg));
            };

            state.socket.onmessage = function(event) {
                console.log('Message WebSocket reçu:', event.data);
                handleWebSocketMessage(event.data);
            };

            state.socket.onclose = function(event) {
                console.log('WebSocket fermé. Code:', event.code, 'Raison:', event.reason);
                state.isConnected = false;

                // Tentative de reconnexion si la fermeture n'était pas volontaire
                if (event.code !== 1000 && event.code !== 1001) {
                    attemptReconnect();
                }
            };

            state.socket.onerror = function(error) {
                console.error('Erreur WebSocket:', error);
            };
        } catch (error) {
            console.error('Erreur lors de la création de la WebSocket:', error);
            setTimeout(attemptReconnect, 5000);
        }
    }

    /**
     * Tente de se reconnecter au serveur WebSocket
     */
    function attemptReconnect() {
        if (state.reconnectAttempts >= state.maxReconnectAttempts) {
            console.log('Nombre maximum de tentatives de reconnexion atteint');
            return;
        }

        state.reconnectAttempts++;
        const delay = Math.min(1000 * Math.pow(2, state.reconnectAttempts - 1), 30000);

        console.log(`Tentative de reconnexion dans ${delay/1000} secondes...`);
        setTimeout(function() {
            console.log(`Tentative de reconnexion #${state.reconnectAttempts}`);
            initWebSocket();
        }, delay);
    }

    /**
     * Gère les messages reçus via WebSocket
     */
    function handleWebSocketMessage(data) {
        try {
            const message = JSON.parse(data);
            console.log('Message parsé:', message);


            // Les messages envoyés par WebSocketLinksUpdater utilisent fileId et userId
            if (message.fileId !== undefined &&
                message.fileId !== peerId &&
                message.userId !== peerId) {
                return;
            }


            if (message.type === 'change' && message.text) {
                // C'est un message de chat
                const isSent = message.userId === myId;
                const userName = isSent ? myName : peerName;
                displayMessage(message.text, isSent, userName);

                // Si message reçu, masquer l'indicateur de frappe
                if (!isSent) {
                    hideTypingIndicator();
                }
            }
            else if (message.type === 'typing') {
                // Gestion de l'indicateur de frappe
                if (message.userId !== myId) {
                    if (message.isTyping) {
                        showTypingIndicator(peerName);
                    } else {
                        hideTypingIndicator();
                    }
                }
            }
            else if (message.type === 'error') {
                console.error('Erreur reçue du serveur:', message.message);
                showErrorMessage(message.message);
            }
        } catch (error) {
            console.error('Erreur lors du traitement du message WebSocket:', error);
        }
    }

    /**
     * Affiche un message dans la zone de chat
     */
    function displayMessage(text, isSent, username) {
        const div = document.createElement('div');
        div.className = isSent ? 'message-sent' : 'message-received';

        let content = '';

        // Ajouter le nom d'utilisateur pour les messages reçus
        if (!isSent && username) {
            content += '<div class="text-xs text-gray-300 mb-1">' + username + '</div>';
        }

        // Ajouter le texte du message
        content += '<div>' + text + '</div>';

        // Ajouter l'heure
        content += '<div class="text-xs text-right mt-1 opacity-75">' + new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + '</div>';

        div.innerHTML = content;
        elements.messagesContainer.appendChild(div);

        // Scroll vers le bas
        elements.messagesContainer.scrollTop = elements.messagesContainer.scrollHeight;

        console.log(`Message ${isSent ? 'envoyé' : 'reçu'} affiché: ${text}`);
    }

    /**
     * Affiche un message d'erreur
     */
    function showErrorMessage(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'bg-red-600 text-white p-2 rounded text-center mx-auto my-2';
        errorDiv.textContent = message;

        elements.messagesContainer.appendChild(errorDiv);
        elements.messagesContainer.scrollTop = elements.messagesContainer.scrollHeight;

        // Supprimer après quelques secondes
        setTimeout(function() {
            if (errorDiv.parentNode) {
                errorDiv.parentNode.removeChild(errorDiv);
            }
        }, 5000);

        console.error('Message d\'erreur affiché:', message);
    }

    /**
     * Affiche l'indicateur de frappe
     */
    function showTypingIndicator(username) {
        if (elements.typingIndicator) {
            const usernameEl = elements.typingIndicator.querySelector('.username');
            if (usernameEl) {
                usernameEl.textContent = username || 'L\'autre utilisateur';
            }
            elements.typingIndicator.classList.remove('hidden');
        }
    }

    /**
     * Masque l'indicateur de frappe
     */
    function hideTypingIndicator() {
        if (elements.typingIndicator) {
            elements.typingIndicator.classList.add('hidden');
        }
    }

    /**
     * Envoie une notification de frappe
     */
    function sendTypingNotification(isTyping) {
        if (!state.isConnected || !state.socket || state.socket.readyState !== WebSocket.OPEN) {
            return;
        }

        const notification = {
            type: 'typing',
            to: peerId,
            fileId: peerId,
            userId: myId,
            username: myName,
            isTyping: isTyping
        };

        state.socket.send(JSON.stringify(notification));
    }

    /**
     * Envoie un message via WebSocket
     */
    function sendMessage() {
        const text = elements.msgInput.value.trim();
        if (!text) return;

        // Vérifier la connexion
        if (!state.isConnected || !state.socket || state.socket.readyState !== WebSocket.OPEN) {
            showErrorMessage('Vous êtes déconnecté. Impossible d\'envoyer le message.');
            return;
        }

        const message = {
            type: 'change',
            fileId: peerId,
            userId: myId,
            username: myName,
            text: text
        };

        console.log('Envoi de message:', message);
        state.socket.send(JSON.stringify(message));

        // Vider le champ de saisie
        elements.msgInput.value = '';
        elements.msgInput.focus();

        // Indiquer que l'utilisateur a arrêté de taper
        sendTypingNotification(false);
    }

    /**
     * Charge l'historique des messages depuis le serveur
     */
    function loadMessageHistory() {
        const url = contextPath + '/ChatMessageServlet?with=' + peerId;
        console.log('Chargement de l\'historique des messages depuis:', url);

        fetch(url)
            .then(response => {
                console.log('Réponse reçue, statut:', response.status);
                if (!response.ok) {
                    throw new Error(`Erreur HTTP! status: ${response.status}`);
                }
                return response.json();
            })
            .then(messages => {
                console.log(`${messages.length} messages chargés depuis l'historique`);

                // Afficher les messages
                messages.forEach(msg => {
                    displayMessage(
                        msg.message,
                        msg.isSent,
                        msg.isSent ? myName : peerName
                    );
                });
            })
            .catch(error => {
                console.error('Erreur lors du chargement de l\'historique des messages:', error);
                showErrorMessage('Erreur lors du chargement des messages précédents');
            });
    }

    /**
     * Initialise les écouteurs d'événements
     */
    function setupEventListeners() {
        // Envoi de message via le bouton
        if (elements.sendBtn) {
            elements.sendBtn.addEventListener('click', function(event) {
                event.preventDefault();
                sendMessage();
            });
        }

        // Envoi de message via la touche Entrée
        if (elements.msgInput) {
            elements.msgInput.addEventListener('keypress', function(event) {
                if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    sendMessage();
                }
            });

            // Détection de frappe pour l'indicateur
            elements.msgInput.addEventListener('input', function() {
                if (!state.isTyping) {
                    state.isTyping = true;
                    sendTypingNotification(true);
                }

                // Réinitialiser le timeout
                clearTimeout(state.typingTimeout);
                state.typingTimeout = setTimeout(function() {
                    state.isTyping = false;
                    sendTypingNotification(false);
                }, 2000);
            });
        }

        // Gestion de la fermeture de la page
        window.addEventListener('beforeunload', function() {
            // Fermer proprement la connexion WebSocket
            if (state.socket && state.isConnected) {
                state.socket.close(1000, "Fermeture volontaire");
            }
        });
    }

    /**
     * Initialise le chat
     */
    function initChat() {
        console.log('Initialisation du chat avec l\'utilisateur ID:', peerId, 'Nom:', peerName);

        // Initialiser les écouteurs d'événements
        setupEventListeners();

        // Charger l'historique des messages
        loadMessageHistory();

        // Initialiser la connexion WebSocket
        initWebSocket();

        // Focus sur le champ de saisie
        if (elements.msgInput) {
            setTimeout(() => elements.msgInput.focus(), 500);
        }
    }

    // Démarrer le chat
    initChat();
});