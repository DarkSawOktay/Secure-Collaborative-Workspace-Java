// webapp/js/collaborationsPanel.js
document.addEventListener('DOMContentLoaded', () => {
    if (!window.APP || !window.APP.fileId) return;

    const ctx = window.APP.contextPath;
    const fileId = window.APP.fileId;
    const userId = window.APP.userId;

    // Variables pour les rôles
    let isAdmin = false;
    let isOwner = false;
    let canManageCollaborators = false;

    // Éléments DOM
    const collaboratorsBtn = document.getElementById('collaborators-btn');
    const collaboratorsPanel = document.getElementById('collaborators-panel');
    const closeBtn = document.getElementById('close-collaborators-panel');
    const activeCollaboratorsEl = document.getElementById('active-collaborators');
    const pendingInvitationsEl = document.getElementById('pending-invitations');
    const pseudoInput = document.getElementById('collaborator-pseudo');
    const roleSelect = document.getElementById('collaborator-role');
    const inviteBtn = document.getElementById('invite-collaborator-btn');
    const inviteStatusEl = document.getElementById('invite-status');

    // Vérifier que tous les éléments nécessaires existent
    if (!collaboratorsBtn || !collaboratorsPanel || !closeBtn ||
        !activeCollaboratorsEl || !pendingInvitationsEl ||
        !pseudoInput || !roleSelect || !inviteBtn || !inviteStatusEl) {
        console.error('Elements manquants pour le panneau de collaborations');
        return;
    }

    // Ouvrir/fermer le panneau
    collaboratorsBtn.addEventListener('click', () => {
        collaboratorsPanel.classList.toggle('translate-x-full');
        loadCollaboratorsData();
    });

    closeBtn.addEventListener('click', () => {
        collaboratorsPanel.classList.add('translate-x-full');
    });

    // Fonction pour vérifier les permissions
    function checkPermissions() {
        return fetch(`${ctx}/VerifierPermissionsServlet?fichier_id=${fileId}`)
            .then(response => response.json())
            .then(permissions => {
                isOwner = permissions.estProprietaire;
                return fetch(`${ctx}/DroitsAccesServlet?fichier_id=${fileId}`);
            })
            .then(response => response.json())
            .then(collaborators => {
                const currentUser = collaborators.find(c => c.id === userId);
                isAdmin = currentUser && currentUser.role === 'admin';
                canManageCollaborators = isOwner || isAdmin;

                // Afficher/masquer les éléments d'invitation
                const inviteFormEl = document.getElementById('invite-form');
                if (inviteFormEl) {
                    inviteFormEl.style.display = canManageCollaborators ? 'block' : 'none';
                }

                return { collaborators: collaborators };
            })
            .catch(error => {
                console.error('Erreur lors de la vérification des permissions:', error);
                return { collaborators: [] };
            });
    }

    // Charger la liste des collaborateurs actifs
    function loadActiveCollaborators() {
        checkPermissions()
            .then(result => {
                const collaborators = result.collaborators;
                activeCollaboratorsEl.innerHTML = '';

                if (collaborators.length === 0) {
                    activeCollaboratorsEl.innerHTML = '<div class="text-sm text-gray-500 dark:text-gray-400">Aucun collaborateur</div>';
                    return;
                }

                // Récupérer le propriétaire du fichier d'abord
                fetch(`${ctx}/VerifierPermissionsServlet?fichier_id=${fileId}`)
                    .then(response => response.json())
                    .then(fileInfo => {
                        const proprietaireId = fileInfo.proprietaireId;

                        collaborators.forEach(collab => {
                            const div = document.createElement('div');
                            div.className = 'bg-gray-50 dark:bg-gray-700 p-3 rounded-md';

                            // Déterminer si le collaborateur est en ligne
                            const isOnline = false; // À implémenter

                            // Déterminer si c'est le propriétaire
                            const isProprietaire = collab.estProprietaire || collab.role === 'proprietaire' || collab.id === proprietaireId;

                            // Préparer l'affichage du rôle
                            let roleDisplay;
                            if (isProprietaire) {
                                roleDisplay = `<span class="bg-purple-100 text-purple-800 text-xs font-semibold px-2 py-1 rounded">Propriétaire</span>`;
                            } else {
                                roleDisplay = `
                                    <select class="role-select text-xs p-1 border rounded bg-white dark:bg-gray-600" data-user-id="${collab.id}" ${canManageCollaborators ? '' : 'disabled'}>
                                        <option value="viewer" ${collab.role === 'viewer' ? 'selected' : ''}>Lecteur</option>
                                        <option value="editor" ${collab.role === 'editor' ? 'selected' : ''}>Éditeur</option>
                                        <option value="admin" ${collab.role === 'admin' ? 'selected' : ''}>Admin</option>
                                    </select>
                                `;
                            }

                            div.innerHTML = `
                                <div class="flex items-center justify-between">
                                    <div class="flex items-center">
                                        <span class="inline-block w-2 h-2 rounded-full ${isOnline ? 'bg-green-500' : 'bg-gray-400'} mr-2"></span>
                                        <span>${collab.nom}</span>
                                    </div>
                                    <div class="flex items-center space-x-2">
                                        ${roleDisplay}
                                        ${!isProprietaire && canManageCollaborators ? `
                                        <button class="remove-collaborator-btn text-red-500 hover:text-red-700" data-user-id="${collab.id}">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                                            </svg>
                                        </button>
                                        ` : ''}
                                    </div>
                                </div>
                                <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">${collab.email}</div>
                            `;

                            activeCollaboratorsEl.appendChild(div);
                        });

                        // Ajouter les événements pour les sélecteurs de rôle
                        document.querySelectorAll('.role-select').forEach(select => {
                            select.addEventListener('change', e => {
                                const userId = e.target.dataset.userId;
                                const newRole = e.target.value;
                                updateCollaboratorRole(userId, newRole);
                            });
                        });

                        // Ajouter les événements pour les boutons de suppression
                        document.querySelectorAll('.remove-collaborator-btn').forEach(button => {
                            button.addEventListener('click', e => {
                                const userId = e.currentTarget.dataset.userId;
                                removeCollaborator(userId);
                            });
                        });
                    });
            })
            .catch(error => {
                console.error('Erreur lors du chargement des collaborateurs:', error);
                activeCollaboratorsEl.innerHTML = '<div class="text-sm text-red-500">Erreur de chargement</div>';
            });
    }

    // Charger la liste des invitations en attente
    function loadPendingInvitations() {
        fetch(`${ctx}/GetPendingInvitationsServlet?fichier_id=${fileId}`)
            .then(response => response.json())
            .then(invitations => {
                pendingInvitationsEl.innerHTML = '';

                if (invitations.length === 0) {
                    pendingInvitationsEl.innerHTML = '<div class="text-sm text-gray-500 dark:text-gray-400">Aucune invitation en attente</div>';
                    return;
                }

                invitations.forEach(inv => {
                    const div = document.createElement('div');
                    div.className = 'bg-gray-50 dark:bg-gray-700 p-3 rounded-md';

                    // Déterminer l'affichage du rôle
                    let roleText;
                    if (inv.role === 'proprietaire') {
                        roleText = 'Propriétaire';
                    } else if (inv.role === 'admin') {
                        roleText = 'Admin';
                    } else if (inv.role === 'editor') {
                        roleText = 'Éditeur';
                    } else {
                        roleText = 'Lecteur';
                    }

                    div.innerHTML = `
                        <div class="flex items-center justify-between">
                            <div>
                                <span>${inv.invitee_nom}</span>
                                <div class="text-xs text-gray-500 dark:text-gray-400">
                                    Rôle: ${roleText}
                                </div>
                                <div class="text-xs text-gray-500 dark:text-gray-400">
                                    Invité le ${new Date(inv.date_invite).toLocaleDateString()}
                                </div>
                            </div>
                            ${canManageCollaborators ? `
                            <button class="cancel-invitation-btn text-red-500 hover:text-red-700" data-invitation-id="${inv.id}">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                                </svg>
                            </button>
                            ` : ''}
                        </div>
                    `;

                    pendingInvitationsEl.appendChild(div);
                });

                // Ajouter les événements pour les boutons d'annulation d'invitation
                document.querySelectorAll('.cancel-invitation-btn').forEach(button => {
                    button.addEventListener('click', e => {
                        const invitationId = e.currentTarget.dataset.invitationId;
                        cancelInvitation(invitationId);
                    });
                });
            })
            .catch(error => {
                console.error('Erreur lors du chargement des invitations:', error);
                pendingInvitationsEl.innerHTML = '<div class="text-sm text-red-500">Erreur de chargement</div>';
            });
    }

    // Ajouter l'option admin dans le sélecteur de rôle
    if (roleSelect) {
        // Ajout de l'option admin dans le sélecteur
        const adminOption = document.createElement('option');
        adminOption.value = 'admin';
        adminOption.textContent = 'Admin';
        roleSelect.appendChild(adminOption);
    }

    // Fonction pour mettre à jour le rôle d'un collaborateur
    function updateCollaboratorRole(userId, newRole) {
        fetch(`${ctx}/UpdateRoleServlet`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `fichier_id=${fileId}&utilisateur_id=${userId}&role=${newRole}`
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showNotification('Rôle mis à jour avec succès', 'success');
                    // Recharger les collaborateurs pour refléter les changements
                    loadActiveCollaborators();
                } else {
                    showNotification(data.message || 'Erreur lors de la mise à jour du rôle', 'error');
                }
            })
            .catch(error => {
                console.error('Erreur lors de la mise à jour du rôle:', error);
                showNotification('Erreur de connexion', 'error');
            });
    }

    // Fonction pour supprimer un collaborateur
    function removeCollaborator(userId) {
        if (!confirm('Êtes-vous sûr de vouloir retirer ce collaborateur ?')) return;

        fetch(`${ctx}/RemoveCollaboratorServlet`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `fichier_id=${fileId}&utilisateur_id=${userId}`
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showNotification('Collaborateur supprimé', 'success');
                    loadActiveCollaborators();
                } else {
                    showNotification(data.message || 'Erreur lors de la suppression', 'error');
                }
            })
            .catch(error => {
                console.error('Erreur lors de la suppression du collaborateur:', error);
                showNotification('Erreur de connexion', 'error');
            });
    }

    // Fonction pour annuler une invitation
    function cancelInvitation(invitationId) {
        if (!confirm('Êtes-vous sûr de vouloir annuler cette invitation ?')) return;

        fetch(`${ctx}/CancelInvitationServlet`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `invitation_id=${invitationId}`
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showNotification('Invitation annulée', 'success');
                    loadPendingInvitations();
                } else {
                    showNotification(data.message || 'Erreur lors de l\'annulation', 'error');
                }
            })
            .catch(error => {
                console.error('Erreur lors de l\'annulation de l\'invitation:', error);
                showNotification('Erreur de connexion', 'error');
            });
    }

    // Fonction pour inviter un collaborateur
    inviteBtn.addEventListener('click', () => {
        if (!canManageCollaborators) {
            showInviteStatus('Vous n\'avez pas les droits pour inviter des collaborateurs', 'error');
            return;
        }

        const pseudo = pseudoInput.value.trim();
        const role = roleSelect.value;

        if (!pseudo) {
            showInviteStatus('Veuillez entrer un pseudo', 'error');
            return;
        }

        // Rechercher l'utilisateur par pseudo
        fetch(`${ctx}/FindUserServlet?pseudo=${encodeURIComponent(pseudo)}`)
            .then(response => response.json())
            .then(userData => {
                if (!userData.success || !userData.user) {
                    showInviteStatus('Utilisateur introuvable', 'error');
                    return;
                }

                // Envoyer l'invitation
                return fetch(`${ctx}/CreateInvitationServlet`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: `fichier_id=${fileId}&invitee_id=${userData.user.id}&role=${role}`
                });
            })
            .then(response => {
                if (!response) return null;
                return response.json();
            })
            .then(data => {
                if (!data) return;

                if (data.success) {
                    showInviteStatus('Invitation envoyée avec succès', 'success');
                    pseudoInput.value = '';
                    loadPendingInvitations();
                } else {
                    showInviteStatus(data.message || 'Erreur lors de l\'envoi de l\'invitation', 'error');
                }
            })
            .catch(error => {
                console.error('Erreur lors de l\'invitation:', error);
                showInviteStatus('Erreur de connexion', 'error');
            });
    });

    // Afficher un statut d'invitation
    function showInviteStatus(message, type) {
        inviteStatusEl.textContent = message;
        inviteStatusEl.className = `text-sm ${type === 'success' ? 'text-green-600' : 'text-red-600'}`;

        setTimeout(() => {
            inviteStatusEl.textContent = '';
        }, 5000);
    }

    // Afficher une notification temporaire
    function showNotification(message, type) {
        const notif = document.createElement('div');
        notif.className = `fixed top-4 right-4 px-4 py-2 rounded-md shadow-md ${
            type === 'success' ? 'bg-green-600' : 'bg-red-600'
        } text-white z-50 transition-opacity duration-500`;
        notif.textContent = message;

        document.body.appendChild(notif);

        setTimeout(() => {
            notif.style.opacity = '0';
            setTimeout(() => {
                document.body.removeChild(notif);
            }, 500);
        }, 3000);
    }

    // Charger toutes les données de collaboration
    function loadCollaboratorsData() {
        loadActiveCollaborators();
        loadPendingInvitations();
    }

    // Initialisation
    window.addEventListener('load', () => {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.has('showCollaborators')) {
            collaboratorsPanel.classList.remove('translate-x-full');
            loadCollaboratorsData();
        }
    });
});