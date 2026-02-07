// webapp/js/ui.js
document.addEventListener('DOMContentLoaded', () => {
    if (!window.APP) return;
    const ctx = window.APP.contextPath;
    const fid = window.APP.fileId;


    // Bouton +Nouveau fichier
    const btnCreate = document.getElementById('create-file');
    if (btnCreate) {
        btnCreate.addEventListener('click', () => {
            fetch(`${ctx}/CreateFileServlet`,{ method:'POST' })
                .then(r=>r.json())
                .then(d=> location.href=`${ctx}/EditorServlet?fileId=${d.fileId}`);
        });
    }

    // Bouton Renommer
    const btnRename = document.getElementById('rename-btn');
    if (btnRename && fid != null) {
        btnRename.addEventListener('click', () => {
            const newName = document.getElementById('filename').value.trim();
            fetch(`${ctx}/RenameFileServlet`, {
                method:'POST',
                headers:{'Content-Type':'application/x-www-form-urlencoded'},
                body:`fileId=${fid}&newName=${encodeURIComponent(newName)}`
            }).then(r=>r.ok&&window.location.reload());
        });
    }
});
