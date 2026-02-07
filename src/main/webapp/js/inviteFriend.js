// inviteFriend.js
document.addEventListener('DOMContentLoaded', () => {
    const btn    = document.getElementById('add-friend-btn');
    const input  = document.getElementById('friend-pseudo');
    const status = document.getElementById('add-friend-status');
    const ctx    = window.APP.contextPath;

    if (!btn || !input || !status) {
        console.error('inviteFriend.js : éléments manquants');
        return;
    }

    btn.addEventListener('click', () => {
        const pseudo = input.value.trim();
        status.textContent = '';
        status.classList.remove('text-green-500', 'text-red-500');

        if (!pseudo) {
            status.textContent = 'Entrez un pseudo valide.';
            status.classList.add('text-red-500');
            return;
        }

        fetch(`${ctx}/SendFriendRequestServlet`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `pseudo=${encodeURIComponent(pseudo)}`
        })
            .then(res => res.json())
            .then(json => {
                if (json.success) {
                    status.textContent = 'Ami ajouté !';
                    status.classList.add('text-green-500');
                    if (window.loadFriendsList) loadFriendsList();
                } else {
                    status.textContent = json.message || 'Erreur lors de l’ajout';
                    status.classList.add('text-red-500');
                }
            })
            .catch(err => {
                console.error('inviteFriend.js fetch error', err);
                status.textContent = 'Erreur réseaue.';
                status.classList.add('text-red-500');
            });
    });
});
