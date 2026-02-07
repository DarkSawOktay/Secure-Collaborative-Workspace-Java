// webapp/js/notifications.js
document.addEventListener('DOMContentLoaded', () => {
    const ctx = window.APP.contextPath;
    const notificationsList = document.getElementById('notifications-list');
    const noNotificationsEl = document.getElementById('no-notifications');
    const loadingEl = document.getElementById('loading-notifications');
    const unreadBadgeEl = document.getElementById('unread-badge');
    const allBtn = document.getElementById('all-notifications-btn');
    const unreadBtn = document.getElementById('unread-notifications-btn');
    const markAllReadBtn = document.getElementById('mark-all-read-btn');

    let notifications = [];
    let showOnlyUnread = false;

    // Chargement des notifications
    function loadNotifications() {
        loadingEl.classList.remove('hidden');
        notificationsList.innerHTML = '';
        noNotificationsEl.classList.add('hidden');

        fetch(`${ctx}/GetNotificationsServlet`)
            .then(response => response.json())
            .then(data => {
                loadingEl.classList.add('hidden');
                if (data.success) {
                    notifications = data.notifications || [];
                    updateUnreadBadge(data.unreadCount || 0);
                    renderNotifications();
                } else {
                    console.error('Erreur lors du chargement des notifications:', data.message);
                    notificationsList.innerHTML = `<li class="text-red-500">Erreur: ${data.message || 'Impossible de charger les notifications'}</li>`;
                }
            })
            .catch(error => {
                loadingEl.classList.add('hidden');
                console.error('Erreur réseau:', error);
                notificationsList.innerHTML = '<li class="text-red-500">Erreur de connexion</li>';
            });
    }

    // Mise à jour du badge des notifications non lues
    function updateUnreadBadge(count) {
        if (count > 0) {
            unreadBadgeEl.textContent = count;
            unreadBadgeEl.classList.remove('hidden');
        } else {
            unreadBadgeEl.classList.add('hidden');
        }
    }

    // Affichage des notifications avec filtrage
    function renderNotifications() {
        notificationsList.innerHTML = '';

        const filteredNotifications = showOnlyUnread
            ? notifications.filter(n => !n.lu)
            : notifications;

        if (filteredNotifications.length === 0) {
            noNotificationsEl.classList.remove('hidden');
            return;
        }

        noNotificationsEl.classList.add('hidden');

        filteredNotifications.forEach(notification => {
            const li = document.createElement('li');
            li.className = `relative p-4 rounded-lg border ${notification.lu
                ? 'bg-gray-50 dark:bg-gray-700 border-gray-200 dark:border-gray-600'
                : 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800'}`;

            // Détermine l'icône en fonction du type
            let icon = '🔔';
            if (notification.type === 'DEMANDE_AMI') icon = '👋';
            else if (notification.type === 'DEMANDE_ACCEPTEE') icon = '✅';
            else if (notification.type === 'DEMANDE_REFUSEE') icon = '❌';
            else if (notification.type === 'COLLABORATION') icon = '🤝';

            const date = new Date(notification.dateCreation);
            const formattedDate = `${date.toLocaleDateString()} à ${date.toLocaleTimeString()}`;

            li.innerHTML = `
                <div class="flex items-start">
                    <div class="text-xl mr-3">${icon}</div>
                    <div class="flex-1">
                        <p class="text-gray-800 dark:text-gray-200">${notification.message}</p>
                        <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">${formattedDate}</p>
                    </div>
                    ${!notification.lu ? `
                    <button class="mark-read-btn text-xs text-blue-600 hover:underline" 
                            data-id="${notification.id}">
                        Marquer comme lu
                    </button>` : ''}
                </div>
            `;

            notificationsList.appendChild(li);
        });

        // Activer les boutons pour marquer comme lu
        document.querySelectorAll('.mark-read-btn').forEach(btn => {
            btn.addEventListener('click', e => {
                const notificationId = e.target.dataset.id;
                markAsRead(notificationId);
            });
        });
    }

    // Marquer une notification comme lue
    function markAsRead(notificationId) {
        fetch(`${ctx}/GetNotificationsServlet`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `notificationId=${notificationId}`
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Mettre à jour localement
                    const notification = notifications.find(n => n.id == notificationId);
                    if (notification) {
                        notification.lu = true;
                        renderNotifications();
                        updateUnreadBadge(notifications.filter(n => !n.lu).length);
                    }
                } else {
                    console.error('Erreur:', data.message);
                }
            })
            .catch(error => console.error('Erreur réseau:', error));
    }

    // Marquer toutes les notifications comme lues
    function markAllAsRead() {
        // Cette fonctionnalité nécessiterait d'ajouter un point d'API pour marquer toutes les notifications
        // Pour l'instant, simulons le changement localement
        const unreadNotifications = notifications.filter(n => !n.lu);

        if (unreadNotifications.length === 0) return;

        if (confirm(`Marquer ${unreadNotifications.length} notification(s) comme lues ?`)) {
            // Idéalement, appeler une API pour marquer toutes comme lues
            // Pour l'instant, marquer chaque notification une par une
            const markPromises = unreadNotifications.map(notification =>
                fetch(`${ctx}/GetNotificationsServlet`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `notificationId=${notification.id}`
                })
            );

            Promise.all(markPromises)
                .then(() => {
                    // Mettre à jour localement
                    notifications.forEach(n => n.lu = true);
                    renderNotifications();
                    updateUnreadBadge(0);
                })
                .catch(error => console.error('Erreur réseau:', error));
        }
    }

    // Gestion des boutons de filtrage
    allBtn.addEventListener('click', () => {
        if (showOnlyUnread) {
            showOnlyUnread = false;
            allBtn.classList.replace('bg-gray-200', 'bg-blue-600');
            allBtn.classList.replace('text-gray-800', 'text-white');
            unreadBtn.classList.replace('bg-blue-600', 'bg-gray-200');
            unreadBtn.classList.replace('text-white', 'text-gray-800');
            renderNotifications();
        }
    });

    unreadBtn.addEventListener('click', () => {
        if (!showOnlyUnread) {
            showOnlyUnread = true;
            unreadBtn.classList.replace('bg-gray-200', 'bg-blue-600');
            unreadBtn.classList.replace('text-gray-800', 'text-white');
            allBtn.classList.replace('bg-blue-600', 'bg-gray-200');
            allBtn.classList.replace('text-white', 'text-gray-800');
            renderNotifications();
        }
    });

    markAllReadBtn.addEventListener('click', markAllAsRead);

    // Chargement initial
    loadNotifications();

    // Rafraîchissement périodique
    setInterval(loadNotifications, 30000); // Toutes les 30 secondes
});