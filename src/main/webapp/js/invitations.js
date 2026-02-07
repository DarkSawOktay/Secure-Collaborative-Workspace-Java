// webapp/js/invitations.js
document.addEventListener('DOMContentLoaded', () => {
    const list = document.getElementById('invitation-list');
    const noInvitationsEl = document.getElementById('no-invitations');
    const ctx  = window.APP.contextPath;

    if (!list) return;

    function loadInvitations() {
        list.innerHTML = '';

        fetch(`${ctx}/ListInvitationsServlet`)
            .then(r => r.json())
            .then(invitations => {
                if (invitations.length === 0) {
                    list.innerHTML = '';
                    noInvitationsEl.classList.remove('hidden');
                    return;
                }

                noInvitationsEl.classList.add('hidden');
                list.innerHTML = '';

                invitations.forEach(inv => {
                    const li = document.createElement('li');
                    li.className = 'flex items-center justify-between p-4 bg-white dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600';

                    const date = new Date(inv.date_invite);
                    const formattedDate = `${date.toLocaleDateString()} à ${date.toLocaleTimeString()}`;

                    li.innerHTML = `
                        <div>
                            <strong>${inv.inviter_nom}</strong> vous invite à collaborer sur 
                            <em>${inv.fichier_nom}</em>
                            <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">${formattedDate}</p>
                        </div>
                        <div class="flex space-x-2">
                            <button data-id="${inv.invitation_id}" data-action="accept"
                                    class="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 text-sm">
                                Accepter
                            </button>
                            <button data-id="${inv.invitation_id}" data-action="reject"
                                    class="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 text-sm">
                                Refuser
                            </button>
                        </div>
                    `;

                    list.appendChild(li);
                });

                // Ajouter les événements aux boutons
                setupInvitationButtons();
            })
            .catch(error => {
                console.error('Erreur lors du chargement des invitations:', error);
                list.innerHTML = '<li class="text-red-500">Erreur de chargement</li>';
                noInvitationsEl.classList.add('hidden');
            });
    }

    function setupInvitationButtons() {
        list.addEventListener('click', e => {
            const btn = e.target;
            if (!btn.dataset.id) return;

            const invId = btn.dataset.id;
            const action = btn.dataset.action;
            const url = action === 'accept'
                ? `${ctx}/AcceptInvitationServlet`
                : `${ctx}/RejectInvitationServlet`;

            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `invitation_id=${invId}`
            })
                .then(r => r.json())
                .then(j => {
                    if (j.success) {
                        loadInvitations();
                    } else {
                        alert('Erreur : ' + (j.message || 'Échec de l\'opération'));
                    }
                })
                .catch(error => {
                    console.error('Erreur réseau:', error);
                    alert('Erreur de connexion');
                });
        });
    }

    // Chargement initial
    loadInvitations();

    // Rafraîchissement périodique
    setInterval(loadInvitations, 30000); // Toutes les 30 secondes
});