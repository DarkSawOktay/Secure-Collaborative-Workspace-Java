// friendsList.js
document.addEventListener('DOMContentLoaded', () => {
    const ul  = document.getElementById('friends-list');
    const ctx = window.APP.contextPath;

    if (!ul) {
        console.error('friendsList.js : #friends-list manquant');
        return;
    }

    function loadFriendsList() {
        ul.innerHTML = '';
        fetch(`${ctx}/ListFriendsServlet`)
            .then(res => res.json())
            .then(friends => {
                if (friends.length === 0) {
                    ul.innerHTML = '<li class="text-gray-500">Aucun ami pour le moment</li>';
                    return;
                }

                friends.forEach(f => {
                    const li = document.createElement('li');
                    // Complètement retirer les classes de background et ajouter uniquement bordures et espacement
                    li.className = 'flex items-center justify-between p-2 mb-2 border-b border-gray-200 dark:border-gray-700';

                    li.innerHTML = `
                <span class="flex items-center">
                  ${f.nom} ${f.online ? '<span class="text-green-600 ml-2">●</span>' : '<span class="text-gray-400 ml-2">●</span>'}
                </span>
                <button data-id="${f.id}"
                        class="chat-btn px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-500">
                  Chat
                </button>
              `;
                    ul.appendChild(li);
                });
            })
            .catch(err => {
                console.error('Erreur chargement amis:', err);
                ul.innerHTML = '<li class="text-red-500">Erreur de chargement</li>';
            });
    }

    // Déléguer l'ouverture du chat avec le paramètre with inclus
    ul.addEventListener('click', e => {
        if (e.target.classList.contains('chat-btn')) {
            const peerId = e.target.dataset.id;
            if (!peerId) {
                console.error('ID ami manquant');
                return;
            }

            // Rediriger vers la page de chat avec le paramètre with
            window.location.href = `${ctx}/chat.jsp?with=${peerId}`;

        }
    });

    // Rendre accessible à inviteFriend.js pour rafraîchir après ajout
    window.loadFriendsList = loadFriendsList;

    // Chargement initial
    loadFriendsList();
    console.log('Liste d\'amis initialisée');
});