// webapp/js/friendRequests.js
document.addEventListener('DOMContentLoaded', () => {
    const ctx = window.APP.contextPath;
    const requestsList = document.getElementById('friend-requests-list');
    const noRequestsEl = document.getElementById('no-friend-requests');

    if (!requestsList) return;

    function loadFriendRequests() {
        fetch(`${ctx}/GetFriendRequestsServlet`)
            .then(response => response.json())
            .then(data => {
                if (data.success && data.requests) {
                    if (data.requests.length === 0) {
                        requestsList.innerHTML = '';
                        noRequestsEl.classList.remove('hidden');
                        return;
                    }

                    noRequestsEl.classList.add('hidden');
                    requestsList.innerHTML = '';

                    data.requests.forEach(request => {
                        const li = document.createElement('li');
                        li.className = 'flex items-center justify-between p-4 bg-white dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600';

                        li.innerHTML = `
                            <div>
                                <strong>${request.expediteurNom}</strong> vous a envoyé une demande d'ami
                                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">${new Date(request.dateDemande).toLocaleString()}</p>
                            </div>
                            <div class="flex space-x-2">
                                <button data-id="${request.requestId}" data-action="accept"
                                        class="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 text-sm">
                                    Accepter
                                </button>
                                <button data-id="${request.requestId}" data-action="reject"
                                        class="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 text-sm">
                                    Refuser
                                </button>
                            </div>
                        `;

                        requestsList.appendChild(li);
                    });

                    // Ajouter les événements aux boutons
                    setupRequestButtons();
                } else {
                    console.error('Erreur lors du chargement des demandes d\'ami:', data.message);
                    requestsList.innerHTML = '<li class="text-red-500">Erreur de chargement</li>';
                    noRequestsEl.classList.add('hidden');
                }
            })
            .catch(error => {
                console.error('Erreur réseau:', error);
                requestsList.innerHTML = '<li class="text-red-500">Erreur de connexion</li>';
                noRequestsEl.classList.add('hidden');
            });
    }

    function setupRequestButtons() {
        requestsList.querySelectorAll('button[data-action]').forEach(btn => {
            btn.addEventListener('click', e => {
                const requestId = e.target.dataset.id;
                const action = e.target.dataset.action;

                fetch(`${ctx}/RespondFriendRequestServlet`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `requestId=${requestId}&action=${action}`
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            // Recharger la liste après l'action
                            loadFriendRequests();
                        } else {
                            alert(`Erreur: ${data.message || 'Une erreur est survenue'}`);
                        }
                    })
                    .catch(error => {
                        console.error('Erreur réseau:', error);
                        alert('Erreur de connexion');
                    });
            });
        });
    }

    // Chargement initial
    loadFriendRequests();

    // Rafraîchissement périodique
    setInterval(loadFriendRequests, 30000); // Toutes les 30 secondes
});