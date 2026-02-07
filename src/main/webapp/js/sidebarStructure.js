// webapp/js/sidebarStructure.js
document.addEventListener('DOMContentLoaded', () => {
    console.log('▶ sidebarStructure.js démarré');
    if (!window.APP) {
        console.warn('  pas de window.APP, arrêt');
        return;
    }
    const ctx = window.APP.contextPath;
    const treeContainer = document.getElementById('folder-tree');
    console.log('  contextPath =', ctx, '; folder-tree =', treeContainer);
    if (!treeContainer) {
        console.warn('  #folder-tree introuvable');
        return;
    }

    // Variables globales
    let selectedFolderId = null;

    // Fonction pour obtenir l'ID du projet actuel
    function getCurrentProjectId() {
        const storedProjectId = sessionStorage.getItem('lastProjectId');
        if (storedProjectId) {
            console.log("ProjectId récupéré depuis sessionStorage:", storedProjectId);
            return parseInt(storedProjectId);
        }

        // 1. Essayer d'abord fileId -> on doit déterminer à quel projet il appartient
        if (window.APP.fileId) {
            // Cette information doit être obtenue du serveur si elle n'est pas déjà disponible
            // Pour l'instant, on se base sur la structure chargée
            if (window.APP.projectId) {
                console.log("Utilisation du projectId existant:", window.APP.projectId);
                return window.APP.projectId;
            }
        }

        // 2. Vérifier projectId dans l'URL
        const urlParams = new URLSearchParams(window.location.search);
        const projectId = urlParams.get('projectId');
        if (projectId) {
            console.log("ProjectId récupéré depuis l'URL:", projectId);
            return parseInt(projectId);
        }

        // 3. Si window.APP a un projectId, l'utiliser
        if (window.APP.projectId) {
            console.log("ProjectId récupéré depuis window.APP:", window.APP.projectId);
            return window.APP.projectId;
        }

        console.warn("Aucun projectId trouvé");
        return null;
    }

    // Fonction pour obtenir l'icône appropriée selon le type de fichier
    function getFileIcon(fileName) {
        const extension = fileName.split('.').pop().toLowerCase();

        switch (extension) {
            case 'java':
                return `<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-orange-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>`;
            case 'js':
                return `<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-yellow-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>`;
            case 'py':
                return `<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-blue-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>`;
            case 'html':
            case 'htm':
                return `<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-blue-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>`;
            case 'css':
                return `<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-indigo-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>`;
            case 'json':
                return `<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-green-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>`;
            default:
                return `<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-gray-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>`;
        }
    }

    // Fonction pour effacer la sélection visuelle actuelle
    function clearSelection() {
        document.querySelectorAll('.folder-item > div:first-child').forEach(item => {
            item.classList.remove('bg-gray-300', 'dark:bg-gray-700');
        });
    }

    // Construire un nœud (dossier ou fichier) dans l'arborescence
    function buildNode(node, parentId) {
        console.log('    buildNode pour', node, 'parent:', parentId);

        const li = document.createElement('li');
        li.className = 'my-1';

        if (node.type === 'folder') {
            // Configuration pour un dossier
            li.classList.add('folder-item');
            li.dataset.id = node.id;

            // Conteneur pour le dossier et ses boutons
            const folderContainer = document.createElement('div');
            folderContainer.className = 'flex items-center justify-between';

            // Élément de dossier avec icône
            const folderDiv = document.createElement('div');
            folderDiv.className = 'flex items-center py-1 px-2 rounded hover:bg-gray-300 dark:hover:bg-gray-700 cursor-pointer transition duration-150 flex-grow';

            // Icône de dossier
            const folderIcon = document.createElement('div');
            folderIcon.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-blue-500 dark:text-blue-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                </svg>
            `;

            // Nom du dossier
            const label = document.createElement('span');
            label.textContent = node.nom;
            label.className = 'text-sm font-medium truncate';

            folderDiv.appendChild(folderIcon);
            folderDiv.appendChild(label);

            // Boutons pour ajouter fichier/dossier
            const actionButtons = document.createElement('div');
            actionButtons.className = 'flex space-x-1 mr-1';

            // Bouton ajouter fichier
            const addFileBtn = document.createElement('button');
            addFileBtn.className = 'p-1 text-xs rounded hover:bg-gray-400 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300';
            addFileBtn.title = 'Nouveau fichier';
            addFileBtn.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
            `;
            addFileBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                createNewFile(node.id);
            });

            // Bouton ajouter dossier
            const addFolderBtn = document.createElement('button');
            addFolderBtn.className = 'p-1 text-xs rounded hover:bg-gray-400 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300';
            addFolderBtn.title = 'Nouveau dossier';
            addFolderBtn.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-2 5h8a2 2 0 002-2v-5m-2 2h-8a2 2 0 01-2-2V5a2 2 0 012-2h4l2 2h4a2 2 0 012 2v2m-2 4h.01" />
                </svg>
            `;
            addFolderBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                createNewFolder(node.id);
            });

            actionButtons.appendChild(addFileBtn);
            actionButtons.appendChild(addFolderBtn);

            // Ajouter tous les éléments au conteneur principal
            folderContainer.appendChild(folderDiv);
            folderContainer.appendChild(actionButtons);
            li.appendChild(folderContainer);

            // Contenu du dossier (sous-dossiers et fichiers)
            const childrenContainer = document.createElement('div');
            childrenContainer.className = 'ml-4 mt-1';
            childrenContainer.id = `folder-children-${node.id}`;


            // Gérer les enfants (sous-dossiers et fichiers)
            if (node.children && node.children.length) {
                const ul = document.createElement('ul');
                ul.className = 'space-y-1';
                node.children.forEach(child => {
                    ul.appendChild(buildNode(child, node.id));
                });
                childrenContainer.appendChild(ul);
            }

            li.appendChild(childrenContainer);

            // Gestion de clic sur le dossier
            folderDiv.addEventListener('click', (e) => {
                // Ouvrir/fermer le dossier
                childrenContainer.classList.toggle('hidden');

                // Sélectionner ce dossier
                clearSelection();
                folderDiv.classList.add('bg-gray-300', 'dark:bg-gray-700');
                selectedFolderId = node.id;
                console.log('Dossier sélectionné:', node.id, node.nom);

                // Empêcher la propagation de l'événement
                e.stopPropagation();
            });
        } else {
            // Configuration pour un fichier
            li.classList.add('file-item');
            li.dataset.id = node.id;

            // Élément de fichier avec icône
            const fileDiv = document.createElement('div');
            fileDiv.className = 'flex items-center py-1 px-2 rounded hover:bg-gray-300 dark:hover:bg-gray-700 cursor-pointer transition duration-150';

            // Icône selon le type de fichier
            const fileIconDiv = document.createElement('div');
            fileIconDiv.innerHTML = getFileIcon(node.nom);

            // Nom du fichier
            const fileName = document.createElement('span');
            fileName.textContent = node.nom;
            fileName.className = 'text-sm truncate';

            fileDiv.appendChild(fileIconDiv);
            fileDiv.appendChild(fileName);

            // Mettre en évidence le fichier actuel
            if (window.APP.fileId && window.APP.fileId == node.id) {
                fileDiv.classList.add('bg-blue-100', 'dark:bg-blue-900', 'font-medium');
            }

            // Rendre le fichier cliquable
            fileDiv.addEventListener('click', () => {
                // MODIFICATION: Si on a l'ID du projet, l'ajouter dans la redirection
                // pour maintenir le contexte du projet
                const currentProjectId = getCurrentProjectId();
                let url = `${ctx}/EditorServlet?fileId=${node.id}`;

                if (currentProjectId) {
                    url += `&projectId=${currentProjectId}`;

                    // Mettre à jour le projectId dans window.APP
                    if (!window.APP.projectId) {
                        window.APP.projectId = currentProjectId;
                    }

                    // Stocker le projectId dans sessionStorage pour la persistance
                    sessionStorage.setItem('lastProjectId', currentProjectId);
                }

                window.location.href = url;
            });

            li.appendChild(fileDiv);
        }

        return li;
    }

    // Fonction pour créer un nouveau fichier dans le dossier spécifié
    function createNewFile(folderId) {
        const fileName = prompt('Nom du nouveau fichier:');
        if (!fileName) return; // L'utilisateur a annulé

        console.log('Création de fichier dans le dossier ID:', folderId);

        // MODIFICATION: Conserver le projectId lors de la création d'un fichier
        const currentProjectId = getCurrentProjectId();

        fetch(`${ctx}/CreateFileServlet`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `parentId=${folderId}&name=${encodeURIComponent(fileName)}`
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Réponse du serveur:', data);
                if (data.fileId) {
                    // Redirection vers l'éditeur avec le nouveau fichier
                    // MODIFICATION: Ajouter le projectId dans l'URL s'il est disponible
                    let url = `${ctx}/EditorServlet?fileId=${data.fileId}`;

                    if (currentProjectId) {
                        url += `&projectId=${currentProjectId}`;
                        sessionStorage.setItem('lastProjectId', currentProjectId);
                    }

                    window.location.href = url;
                } else {
                    alert('Erreur: Le serveur n\'a pas renvoyé d\'ID de fichier');
                }
            })
            .catch(error => {
                console.error('Erreur lors de la création du fichier:', error);
                alert(`Erreur lors de la création du fichier: ${error.message}`);
            });
    }

    // Fonction pour créer un nouveau dossier dans le dossier spécifié
    function createNewFolder(folderId) {
        const folderName = prompt('Nom du nouveau dossier:');
        if (!folderName) return; // L'utilisateur a annulé

        console.log('Création de dossier dans le dossier parent ID:', folderId);

        // Ouvrir le dossier parent s'il est fermé
        const parentFolder = document.getElementById(`folder-children-${folderId}`);
        if (parentFolder && parentFolder.classList.contains('hidden')) {
            parentFolder.classList.remove('hidden');
        }

        fetch(`${ctx}/CreateFolderServlet`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `parentId=${folderId}&nom=${encodeURIComponent(folderName)}`
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Erreur HTTP ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Réponse du serveur:', data);
                if (data.id) {
                    // MODIFICATION: Mémoriser le dossier parent comme projectId si c'est un dossier racine
                    if (!folderId.parentId) {
                        sessionStorage.setItem('lastProjectId', folderId);
                    }

                    // Recharger l'arborescence
                    loadStructure();
                } else {
                    alert('Erreur: Le serveur n\'a pas renvoyé d\'ID de dossier');
                }
            })
            .catch(error => {
                console.error('Erreur lors de la création du dossier:', error);
                alert(`Erreur lors de la création du dossier: ${error.message}`);
            });
    }

    function renderHeader(projectName) {
        // Créer un en-tête simple
        const header = document.createElement('div');
        header.className = 'flex justify-between items-center mb-3 p-2 bg-gray-200 dark:bg-gray-700 rounded';

        const title = document.createElement('h3');
        title.textContent = projectName ? `Projet: ${projectName}` : 'Fichiers du projet';
        title.className = 'font-medium';

        header.appendChild(title);

        return header;
    }

    function findProjectOfFile(data, fileId) {
        // Fonction récursive pour trouver le projet auquel appartient un fichier
        function findInNode(node, fid) {
            // Vérifier si le fichier est un enfant direct de ce nœud
            if (node.children) {
                for (const child of node.children) {
                    if (child.type === 'file' && child.id == fid) {
                        return node;
                    }

                    // Rechercher de manière récursive dans les sous-dossiers
                    if (child.type === 'folder') {
                        const result = findInNode(child, fid);
                        if (result) {
                            return node.parent_id ? result : node; // Retourner le projet racine
                        }
                    }
                }
            }
            return null;
        }

        // Parcourir tous les nœuds racines (projets)
        for (const project of data) {
            const result = findInNode(project, fileId);
            if (result) {
                return result;
            }
        }

        return null;
    }

    function loadStructure() {
        console.log('  fetch →', ctx + '/ListStructureServlet');
        fetch(ctx + '/ListStructureServlet')
            .then(r => {
                console.log('  statut ListStructureServlet =', r.status);
                if (!r.ok) throw new Error(r.statusText);
                return r.json();
            })
            .then(data => {
                console.log('  JSON reçu pour arborescence', data);

                // Déterminer le projet actuel
                let currentProject = null;
                const currentProjectId = getCurrentProjectId();

                if (window.APP.fileId) {
                    // Cas 1: On a un fileId, trouver le projet correspondant
                    currentProject = findProjectOfFile(data, window.APP.fileId);
                }

                if (!currentProject && currentProjectId) {
                    // Cas 2: On a un projectId, trouver le projet correspondant
                    currentProject = data.find(p => p.id == currentProjectId);

                    // MODIFICATION: Si le projet est trouvé, mettre à jour window.APP.projectId
                    if (currentProject) {
                        window.APP.projectId = currentProjectId;
                        console.log("Projet trouvé et window.APP.projectId mis à jour:", currentProjectId);
                    }
                }

                if (!currentProject && data.length > 0) {
                    // Cas 3: Pas de projet spécifié, prendre le premier
                    currentProject = data[0];

                    // MODIFICATION: Mettre à jour window.APP.projectId et sessionStorage
                    if (currentProject) {
                        window.APP.projectId = currentProject.id;
                        sessionStorage.setItem('lastProjectId', currentProject.id);
                        console.log("Premier projet utilisé par défaut, ID:", currentProject.id);
                    }
                }

                treeContainer.innerHTML = '';  // Vider le contenu

                // Conteneur principal pour l'arborescence
                const treeContent = document.createElement('div');
                treeContent.className = 'space-y-2';

                if (!currentProject) {
                    // Message quand aucun projet n'est disponible
                    const emptyState = document.createElement('div');
                    emptyState.className = 'p-4 text-gray-500 dark:text-gray-400 text-sm italic border border-gray-300 dark:border-gray-700 rounded bg-gray-100 dark:bg-gray-800';
                    emptyState.innerHTML = `
                        Aucun projet disponible
                        <div class="mt-2">
                            <a href="${ctx}/EditorServlet" class="text-blue-600 dark:text-blue-400 hover:underline">
                                Créer un nouveau projet
                            </a>
                        </div>
                    `;
                    treeContent.appendChild(emptyState);
                } else {
                    // Stocker l'ID du projet courant
                    window.APP.projectId = currentProject.id;
                    // MODIFICATION: Enregistrer aussi dans sessionStorage
                    sessionStorage.setItem('lastProjectId', currentProject.id);
                    console.log(`Projet courant identifié et mémorisé: ${currentProject.id} (${currentProject.nom})`);

                    // Ajouter l'en-tête avec le nom du projet
                    treeContent.appendChild(renderHeader(currentProject.nom));

                    // Boutons pour ajouter au niveau racine du projet
                    const rootButtons = document.createElement('div');
                    rootButtons.className = 'flex space-x-1 mb-2 px-2';

                    // Bouton pour ajouter un fichier au niveau racine
                    const addRootFileBtn = document.createElement('button');
                    addRootFileBtn.className = 'px-2 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-500 flex items-center';
                    addRootFileBtn.innerHTML = `
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        Nouveau fichier
                    `;
                    addRootFileBtn.addEventListener('click', () => {
                        createNewFile(currentProject.id);
                    });

                    // Bouton pour ajouter un dossier au niveau racine
                    const addRootFolderBtn = document.createElement('button');
                    addRootFolderBtn.className = 'px-2 py-1 text-xs bg-green-600 text-white rounded hover:bg-green-500 flex items-center';
                    addRootFolderBtn.innerHTML = `
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-2 5h8a2 2 0 002-2v-5m-2 2h-8a2 2 0 01-2-2V5a2 2 0 012-2h4l2 2h4a2 2 0 012 2v2m-2 4h.01" />
                        </svg>
                        Nouveau dossier
                    `;
                    addRootFolderBtn.addEventListener('click', () => {
                        createNewFolder(currentProject.id);
                    });

                    rootButtons.appendChild(addRootFileBtn);
                    rootButtons.appendChild(addRootFolderBtn);
                    treeContent.appendChild(rootButtons);

                    // Structure du projet
                    const projectList = document.createElement('div');
                    projectList.className = 'mt-2';

                    // Afficher le contenu du projet (fichiers et dossiers)
                    const projectContent = document.createElement('ul');
                    projectContent.className = 'space-y-1';

                    // Parcourir les fichiers et dossiers du projet
                    if (currentProject.children && currentProject.children.length) {
                        currentProject.children.forEach(child => {
                            projectContent.appendChild(buildNode(child, currentProject.id));
                        });
                    } else {
                        const emptyProject = document.createElement('li');
                        emptyProject.className = 'text-gray-500 dark:text-gray-400 text-sm italic px-2';
                        emptyProject.textContent = 'Projet vide';
                        projectContent.appendChild(emptyProject);
                    }

                    projectList.appendChild(projectContent);
                    treeContent.appendChild(projectList);
                }

                treeContainer.appendChild(treeContent);
            })
            .catch(err => {
                console.error('  Erreur chargement arborescence :', err);
                treeContainer.innerHTML = `
                    <div class="p-4 text-red-500 border border-red-300 rounded bg-red-50 dark:bg-red-900 dark:border-red-700">
                        Erreur de chargement: ${err.message}
                    </div>
                `;
            });
    }

    window.loadStructure = loadStructure;

    // Chargement initial
    loadStructure();
});