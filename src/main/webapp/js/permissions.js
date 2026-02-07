// webapp/js/permissions.js
document.addEventListener('DOMContentLoaded', () => {
    if (!window.APP || window.APP.fileId == null) return;
    fetch(`${window.APP.contextPath}/VerifierPermissionsServlet?fichier_id=${window.APP.fileId}`)
        .then(r => {
            if (!r.ok) throw new Error('Non autorisé');
            return r.json();
        })
        .then(p => {
            if (!p.peutModifier) {
                window.editor.setReadOnly(true);
                ['rename-btn','create-file']
                    .map(id=>document.getElementById(id))
                    .filter(btn=>btn)
                    .forEach(btn=>btn.disabled = true);
            }
        })
        .catch(() => {
            console.warn('permissions.js : accès restreint ou fichier inexistant');
        });
});
