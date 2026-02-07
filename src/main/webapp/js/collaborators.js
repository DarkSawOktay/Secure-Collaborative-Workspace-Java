// webapp/js/collaborators.js
document.addEventListener('DOMContentLoaded', () => {
    if (!window.APP || window.APP.fileId == null) return;
    const ctx       = window.APP.contextPath;
    const fid       = window.APP.fileId;
    const container = document.getElementById('collaborators');
    if (!container) return;

    function loadCollaborators(){
        fetch(`${ctx}/DroitsAccesServlet?fichier_id=${fid}`)
            .then(r => r.json())
            .then(users => {
                container.innerHTML = '';
                users.forEach(u => {
                    const box = document.createElement('div');
                    box.className = 'flex items-center justify-between p-2 bg-white dark:bg-gray-700 rounded mb-1';

                    // Nom et rôle
                    const info = document.createElement('div');
                    info.className = 'flex items-center space-x-2';
                    const name  = document.createElement('span');
                    name.textContent = u.nom;
                    info.appendChild(name);

                    const sel   = document.createElement('select');
                    ['viewer','editor'].forEach(r => {
                        const o = document.createElement('option');
                        o.value = r;
                        o.textContent = r==='viewer' ? 'Vue seule' : 'Éditeur';
                        if (u.role === r) o.selected = true;
                        sel.appendChild(o);
                    });
                    sel.addEventListener('change', () => {
                        fetch(`${ctx}/UpdateRoleServlet`, {
                            method: 'POST',
                            headers: {'Content-Type':'application/x-www-form-urlencoded'},
                            body: `fichier_id=${fid}&utilisateur_id=${u.id}&role=${sel.value}`
                        }).then(() => loadCollaborators());
                    });
                    info.appendChild(sel);
                    box.appendChild(info);

                    // Bouton supprimer
                    const rm = document.createElement('button');
                    rm.textContent = '✕';
                    rm.title = 'Supprimer l’accès';
                    rm.className = 'text-red-500 hover:text-red-700';
                    rm.addEventListener('click', () => {
                        if (!confirm(`Retirer ${u.nomUtilisateur} ?`)) return;
                        fetch(`${ctx}/RemoveCollaboratorServlet`, {
                            method: 'POST',
                            headers: {'Content-Type':'application/x-www-form-urlencoded'},
                            body: `fichier_id=${fid}&utilisateur_id=${u.id}`
                        }).then(() => loadCollaborators());
                    });
                    box.appendChild(rm);

                    container.appendChild(box);
                });
            })
            .catch(err => {
                console.error('chargement collaborateurs:', err);
                container.textContent = 'Erreur chargement collaborateurs';
            });
    }

    loadCollaborators();
    window.loadCollaborators = loadCollaborators;
});
