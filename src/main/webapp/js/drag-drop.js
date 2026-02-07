// drag-drop.js - Version simplifiée et plus robuste pour éviter les plantages
document.addEventListener('DOMContentLoaded', () => {
    console.log("Initialisation du système drag & drop");

    // Vérifier que les éléments nécessaires sont présents
    if (!window.APP) {
        console.error("Configuration APP non disponible, drag & drop désactivé");
        return;
    }

    const ctx = window.APP.contextPath || '';
    const treeContainer = document.getElementById('folder-tree');

    if (!treeContainer) {
        console.error("Conteneur d'arborescence introuvable, drag & drop désactivé");
        return;
    }

    // Variables pour suivre les opérations de glisser-déposer
    let draggedItem = null;
    let draggedItemType = null; // 'file' ou 'folder'
    let draggedItemId = null;
    let dropTarget = null;

    // Récupérer le projectId de sessionStorage au chargement, s'il existe
    if (sessionStorage.getItem('lastProjectId') && !window.APP.projectId) {
        window.APP.projectId = parseInt(sessionStorage.getItem('lastProjectId'));
        console.log("ProjectId récupéré depuis sessionStorage:", window.APP.projectId);
    }

    // Initialisation des événements de glisser-déposer de manière sécurisée
    function initDragAndDrop() {
        try {
            // Observer les mutations du DOM pour ajouter les événements aux nouveaux éléments
            const observer = new MutationObserver(mutations => {
                try {
                    mutations.forEach(mutation => {
                        if (mutation.addedNodes.length) {
                            attachDragEvents();
                        }
                    });
                } catch (err) {
                    console.error("Erreur dans l'observateur MutationObserver:", err);
                }
            });

            observer.observe(treeContainer, { childList: true, subtree: true });

            // Attacher les événements aux éléments déjà présents
            attachDragEvents();
            console.log("Système drag & drop initialisé avec succès");
        } catch (err) {
            console.error("Échec de l'initialisation du drag & drop:", err);
        }
    }

    // Attacher les événements de glisser-déposer aux éléments de l'arborescence
    function attachDragEvents() {
        try {
            // Récupérer tous les éléments de fichier et dossier
            const fileElements = document.querySelectorAll('.file-item > div:first-child');
            fileElements.forEach(fileEl => {
                if (!fileEl.hasAttribute('draggable')) {
                    fileEl.setAttribute('draggable', 'true');
                    fileEl.addEventListener('dragstart', (e) => handleDragStart(e, fileEl, 'file'));
                    fileEl.addEventListener('dragend', handleDragEnd);
                }
            });

            // Pour les dossiers, on cible l'élément correct qui représente le dossier lui-même
            const folderElements = document.querySelectorAll('.folder-item > div:first-child > div:first-child, .folder-item > div:first-child');
            folderElements.forEach(folderEl => {
                if (!folderEl.hasAttribute('draggable')) {
                    folderEl.setAttribute('draggable', 'true');
                    folderEl.addEventListener('dragstart', (e) => handleDragStart(e, folderEl, 'folder'));
                    folderEl.addEventListener('dragend', handleDragEnd);
                }
            });

            // Activer les zones de dépôt pour les dossiers
            document.querySelectorAll('.folder-item').forEach(folderEl => {
                if (!folderEl.hasAttribute('data-dropzone-attached')) {
                    folderEl.setAttribute('data-dropzone-attached', 'true');
                    folderEl.addEventListener('dragover', (e) => handleDragOver(e, folderEl, 'folder'));
                    folderEl.addEventListener('dragleave', (e) => handleDragLeave(e, folderEl));
                    folderEl.addEventListener('drop', (e) => handleDrop(e, folderEl, 'folder'));
                }
            });

            console.log(`Events attachés à ${fileElements.length} fichiers et ${folderElements.length} dossiers`);
        } catch (err) {
            console.error("Erreur lors de l'attachement des événements drag & drop:", err);
        }
    }

    // Gestionnaire de début de glisser
    function handleDragStart(e, element, type) {
        try {
            // Trouver l'élément parent correct qui contient l'ID
            let item;
            if (type === 'file') {
                item = element.closest('.file-item');
            } else {
                item = element.closest('.folder-item');
            }

            if (!item || !item.dataset.id) {
                console.warn("Élément glissé sans ID valide");
                e.preventDefault();
                return false;
            }

            draggedItem = item;
            draggedItemType = type;
            draggedItemId = item.dataset.id;

            // Indiquer visuellement l'élément en cours de glissement
            setTimeout(() => {
                try {
                    element.classList.add('dragging');
                    draggedItem.classList.add('item-being-dragged');
                } catch (err) {
                    console.error("Erreur lors de l'ajout des classes de glissement:", err);
                }
            }, 0);

            // Ajouter les données au transfert
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', draggedItemId);
            e.dataTransfer.setData('application/x-item-type', type);

            console.log(`Début de glisser: ${type} ID=${draggedItemId}`);

            // Ajuster visuellement les dossiers pour montrer qu'ils sont des cibles potentielles
            document.querySelectorAll('.folder-item').forEach(folder => {
                if (folder !== draggedItem) {
                    folder.classList.add('potential-drop-target');
                }
            });
        } catch (err) {
            console.error("Erreur dans handleDragStart:", err);
            e.preventDefault();
        }
    }

    // Gestionnaire de survol pendant le glisser
    function handleDragOver(e, element, type) {
        try {
            // Empêcher le comportement par défaut pour permettre le dépôt
            e.preventDefault();

            // Vérifier que c'est un dossier
            if (type !== 'folder') {
                e.dataTransfer.dropEffect = 'none';
                return false;
            }

            // Obtenir l'ID de l'élément cible
            const targetId = element.dataset.id;

            // Ignorer si on tente de déplacer un élément sur lui-même
            if (draggedItemType && draggedItemId && draggedItemType === type && draggedItemId === targetId) {
                e.dataTransfer.dropEffect = 'none';
                return false;
            }

            // Ne pas permettre de déposer un dossier dans un de ses enfants (dépendance circulaire)
            if (draggedItemType === 'folder' && isChildOf(element, draggedItem)) {
                e.dataTransfer.dropEffect = 'none';
                element.classList.add('no-drop');
                return false;
            }

            // Définir l'effet de dépôt
            e.dataTransfer.dropEffect = 'move';

            // Mettre à jour la cible de dépôt
            if (dropTarget !== element) {
                if (dropTarget) {
                    dropTarget.classList.remove('drop-target');
                }
                dropTarget = element;
                element.classList.add('drop-target');
            }

            return false;
        } catch (err) {
            console.error("Erreur dans handleDragOver:", err);
            return false;
        }
    }

    // Gestionnaire de sortie de la zone de dépôt
    function handleDragLeave(e, element) {
        try {
            // Vérifier si on sort vraiment de l'élément et pas simplement d'un enfant
            if (e.relatedTarget && element.contains(e.relatedTarget)) {
                return false;
            }

            element.classList.remove('drop-target');
            element.classList.remove('no-drop');

            if (dropTarget === element) {
                dropTarget = null;
            }
        } catch (err) {
            console.error("Erreur dans handleDragLeave:", err);
        }
    }

    // Gestionnaire de fin de glisser
    function handleDragEnd(e) {
        try {
            // Nettoyer toutes les classes visuelles
            document.querySelectorAll('.dragging, .item-being-dragged, .drop-target, .potential-drop-target, .no-drop')
                .forEach(el => {
                    el.classList.remove('dragging');
                    el.classList.remove('item-being-dragged');
                    el.classList.remove('drop-target');
                    el.classList.remove('potential-drop-target');
                    el.classList.remove('no-drop');
                });

            // Réinitialiser les variables
            draggedItem = null;
            draggedItemType = null;
            draggedItemId = null;
            dropTarget = null;
        } catch (err) {
            console.error("Erreur dans handleDragEnd:", err);
        }
    }

    // Gestionnaire de dépôt
    function handleDrop(e, element, type) {
        try {
            e.preventDefault();
            e.stopPropagation();

            // Vérifier si la cible est bien un dossier
            if (type !== 'folder') {
                return false;
            }

            // Obtenir l'ID de l'élément cible
            const targetId = element.dataset.id;

            // Ignorer si on dépose sur le même élément
            if (draggedItemType === type && draggedItemId === targetId) {
                console.log("Dépôt annulé: même élément");
                handleDragEnd(e);
                return false;
            }

            console.log(`Déplacement demandé: ${draggedItemType} ID=${draggedItemId} vers ${type} ID=${targetId}`);

            // Effectuer le déplacement approprié selon le type
            if (draggedItemType === 'file') {
                moveFile(draggedItemId, targetId);
            } else if (draggedItemType === 'folder') {
                moveFolder(draggedItemId, targetId);
            }

            // Nettoyer l'état visuel
            handleDragEnd(e);

            return false;
        } catch (err) {
            console.error("Erreur dans handleDrop:", err);
            handleDragEnd(e);
            return false;
        }
    }

    // Fonction pour déplacer un fichier
    function moveFile(fileId, targetFolderId) {
        console.log(`Envoi de la requête de déplacement du fichier ${fileId} vers ${targetFolderId}`);

        fetch(`${ctx}/MoveFileServlet`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `fileId=${fileId}&targetFolderId=${targetFolderId}&itemType=file`
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    console.log("Fichier déplacé avec succès");
                    showNotification("Fichier déplacé avec succès", "success");

                    if (data.projectId) {
                        console.log("Nouveau projectId reçu:", data.projectId);
                        window.APP.projectId = data.projectId;

                        // Stocker dans sessionStorage pour la persistance entre les navigations
                        sessionStorage.setItem('lastProjectId', data.projectId);
                        console.log("ProjectId stocké dans sessionStorage:", data.projectId);
                    }

                    // Recharger l'arborescence pour refléter les changements
                    if (window.loadStructure) {
                        window.loadStructure();
                    } else {
                        setTimeout(() => location.reload(), 500);
                    }
                } else {
                    console.error("Erreur lors du déplacement:", data.message);
                    showNotification(data.message || "Erreur lors du déplacement du fichier", "error");
                }
            })
            .catch(error => {
                console.error("Erreur lors du déplacement du fichier:", error);
                showNotification("Erreur lors du déplacement du fichier", "error");
            });
    }

    // Fonction pour déplacer un dossier
    function moveFolder(folderId, targetFolderId) {
        console.log(`Envoi de la requête de déplacement du dossier ${folderId} vers ${targetFolderId}`);

        fetch(`${ctx}/MoveFolderServlet`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `folderId=${folderId}&targetFolderId=${targetFolderId}&itemType=folder`
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    console.log("Dossier déplacé avec succès");
                    showNotification("Dossier déplacé avec succès", "success");

                    if (data.projectId) {
                        console.log("Nouveau projectId reçu:", data.projectId);
                        window.APP.projectId = data.projectId;

                        // Stocker dans sessionStorage pour la persistance entre les navigations
                        sessionStorage.setItem('lastProjectId', data.projectId);
                        console.log("ProjectId stocké dans sessionStorage:", data.projectId);
                    }

                    // Recharger l'arborescence pour refléter les changements
                    if (window.loadStructure) {
                        window.loadStructure();
                    } else {
                        setTimeout(() => location.reload(), 500);
                    }
                } else {
                    console.error("Erreur lors du déplacement:", data.message);
                    showNotification(data.message || "Erreur lors du déplacement du dossier", "error");
                }
            })
            .catch(error => {
                console.error("Erreur lors du déplacement du dossier:", error);
                showNotification("Erreur lors du déplacement du dossier", "error");
            });
    }

    // Fonction utilitaire pour vérifier si un élément est enfant d'un autre
    function isChildOf(child, parent) {
        try {
            if (!child || !parent) return false;

            let node = child.parentNode;
            while (node) {
                if (node === parent) {
                    return true;
                }
                node = node.parentNode;
            }
            return false;
        } catch (err) {
            console.error("Erreur dans isChildOf:", err);
            return false;
        }
    }

    // Fonction pour afficher une notification
    function showNotification(message, type = 'info') {
        try {
            const existingNotifications = document.querySelectorAll('.drag-drop-notification');
            existingNotifications.forEach(note => note.remove());

            const notification = document.createElement('div');
            notification.className = `drag-drop-notification fixed top-4 right-4 z-50 px-4 py-2 rounded-md shadow transition-opacity duration-300 ${
                type === 'error' ? 'bg-red-600' : type === 'success' ? 'bg-green-600' : 'bg-blue-600'
            } text-white`;
            notification.textContent = message;

            document.body.appendChild(notification);

            setTimeout(() => {
                notification.style.opacity = '0';
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.parentNode.removeChild(notification);
                    }
                }, 300);
            }, 3000);
        } catch (err) {
            console.error("Erreur dans showNotification:", err);
        }
    }

    // Initialiser le système de glisser-déposer avec gestion d'erreurs
    try {
        initDragAndDrop();

        // Exposer une fonction de rafraîchissement globale
        window.refreshDragDrop = function() {
            try {
                attachDragEvents();
                return true;
            } catch (err) {
                console.error("Erreur lors du rafraîchissement du drag & drop:", err);
                return false;
            }
        };

        console.log("Configuration du drag & drop terminée");
    } catch (err) {
        console.error("Erreur critique dans l'initialisation du drag & drop:", err);
    }
});