// ace-init.js
document.addEventListener('DOMContentLoaded', () => {
    const editorEl = document.getElementById('ace-editor');
    if (!editorEl || !window.APP) return;

    const e = ace.edit('ace-editor');
    window.editor = e;
    window.isApplyingRemote = false;

    // Thème initial
    const dark = document.documentElement.classList.contains('dark');
    e.setTheme(dark?'ace/theme/monokai':'ace/theme/github');

    // Mode par extension
    const fn = window.APP.fileName || '';
    const ext = fn.split('.').pop().toLowerCase();
    const map = { js:'javascript', java:'java', html:'html', css:'css',
        jsp:'java', xml:'xml', py:'python', cpp:'c_cpp', json:'json' };
    e.session.setMode('ace/mode/'+(map[ext]||'text'));

    e.setOptions({
        tabSize: 2,
        useSoftTabs: true,
        wrap: true,
        showPrintMargin: false
    });

    // Contenu initial
    const ta = document.getElementById('initial-content');
    if (ta) e.session.setValue(ta.textContent);
});
