// chat.js
document.addEventListener('DOMContentLoaded', () => {
    console.log("Chat.js initialization started");

    // S'assurer que la configuration nécessaire est disponible
    if (!window.APP || !window.APP.fileId) {
        console.error("Required APP configuration missing");
        return;
    }

    // Récupérer les éléments du DOM
    const win = document.getElementById('chat-window');
    const inp = document.getElementById('chat-input');
    const btn = document.getElementById('chat-send');

    if (!win || !inp || !btn) {
        console.error("Required chat elements not found");
        return;
    }

    // Fonction pour charger les messages
    function loadMessages() {
        fetch(`${window.APP.contextPath}/ChatServlet?project_id=${window.APP.fileId}`)
            .then(r => r.json())
            .then(msgs => {
                win.innerHTML = '';
                msgs.forEach(m => {
                    const messageDiv = document.createElement('div');
                    messageDiv.className = 'chat-message';

                    // Créer un span pour le nom d'utilisateur
                    const usernameSpan = document.createElement('span');
                    usernameSpan.className = 'chat-username';
                    usernameSpan.textContent = m.username + ': ';
                    messageDiv.appendChild(usernameSpan);

                    // Créer un span pour le contenu du message
                    const contentSpan = document.createElement('span');
                    contentSpan.className = 'chat-content';
                    contentSpan.textContent = m.message;
                    messageDiv.appendChild(contentSpan);

                    win.appendChild(messageDiv);
                });
                win.scrollTop = win.scrollHeight;
            })
            .catch(err => {
                console.error('Error loading chat:', err);
                win.textContent = 'Erreur chargement chat';
            });
    }

    // Envoi d'un message
    btn.addEventListener('click', function() {
        const msg = inp.value.trim();
        if (!msg) return;

        // Envoyer le message au serveur
        fetch(`${window.APP.contextPath}/ChatServlet`, {
            method: 'POST',
            headers: {'Content-Type':'application/x-www-form-urlencoded'},
            body: `project_id=${window.APP.fileId}&message=${encodeURIComponent(msg)}`
        }).then(() => {
            inp.value = '';
            loadMessages();
        });
    });

    // Permettre l'envoi du message avec la touche Entrée
    inp.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            btn.click();
        }
    });

    // Chargement initial et rafraîchissement périodique
    loadMessages();
    setInterval(loadMessages, 5000);  // Toutes les 5 secondes

    console.log("Chat.js initialization complete");
});