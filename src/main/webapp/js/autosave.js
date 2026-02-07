// webapp/js/autosave.js
document.addEventListener('DOMContentLoaded', () => {
    if (!window.APP || !window.editor) return;
    const editor = window.editor;
    const fileId = window.APP.fileId;
    const ctx = window.APP.contextPath;
    const status = document.getElementById('save-status');
    let saveTimeout;

    function markSaved(msg) {
        status.textContent = msg;
        setTimeout(() => status.textContent = '', 1500);
    }
    function saveContent() {
        const content = editor.getValue();
        status.textContent = 'Sauvegarde…';
        fetch(`${ctx}/SaveFileServlet`, {
            method:'POST',
            headers:{'Content-Type':'application/x-www-form-urlencoded'},
            body:`fileId=${fileId}&content=${encodeURIComponent(content)}`
        }).then(r => r.ok && markSaved('Enregistré'));
    }

    // on n'auto-save que si ce n'est pas un delta distant
    editor.session.on('change', () => {
        if (window.isApplyingRemote) return;
        if (fileId > 0) {
            clearTimeout(saveTimeout);
            saveTimeout = setTimeout(saveContent, 1000);
        }
    });

    // Ctrl+S
    editor.commands.addCommand({
        name:'save', bindKey:{win:'Ctrl-S',mac:'Command-S'},
        exec:saveContent, readOnly:false
    });
});
