// webapp/js/realtime.js
document.addEventListener('DOMContentLoaded', () => {
    if (!window.APP || !window.editor) return;
    const editor = window.editor;
    const fileId = window.APP.fileId;
    const userId = window.APP.userId;
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    const ws = new WebSocket(`${protocol}://${location.host}${window.APP.contextPath}/links-websocket`);
    let lastSentDelta = null; // Pour éviter l'écho des deltas envoyés

    // 1) Subscription
    ws.addEventListener('open', () => {
        console.log('[WS] open, subscribe to file', fileId);
        ws.send(JSON.stringify({ type:'subscribe', fileId, userId }));
    });
    ws.addEventListener('error', e => console.error('[WS] error', e));
    ws.addEventListener('close', () => console.log('[WS] closed'));

    // 2) Receive deltas & cursors
    ws.addEventListener('message', evt => {
        let msg;
        try { msg = JSON.parse(evt.data); }
        catch { return; }
        if (msg.fileId !== fileId) return;

        if (msg.type === 'change') {
            // Vérifier si ce delta a été envoyé par cet utilisateur
            // Si c'est le cas, ne pas l'appliquer (éviter l'écho)
            if (msg.userId === userId) {
                return; // Ignorer les deltas envoyés par l'utilisateur actuel
            }

            // On bloque autosave + rebroadcast
            window.isApplyingRemote = true;
            editor.session.getDocument().applyDelta(msg.delta, true);
            window.isApplyingRemote = false;
        }
        else if (msg.type === 'cursor') {
            // Ignorer les mises à jour de curseur de l'utilisateur actuel
            if (msg.userId === userId) return;

            const { userId: uid, row, column } = msg;
            // affiche le marqueur pour uid
            if (!window.remoteCursors) window.remoteCursors = {};
            const prev = window.remoteCursors[uid];
            if (prev != null) editor.session.removeMarker(prev);
            const Range = ace.require('ace/range').Range;
            const range = new Range(row, column, row, column+1);
            const marker = editor.session.addMarker(range, 'remote-cursor', true);
            window.remoteCursors[uid] = marker;
        }
    });

    // 3) Broadcast local edits
    editor.session.on('change', delta => {
        if (window.isApplyingRemote) return;

        // Envoi du delta avec l'ID utilisateur
        ws.send(JSON.stringify({
            type: 'change',
            fileId,
            userId,
            delta
        }));
    });

    // 4) Broadcast cursor moves
    editor.selection.on('changeCursor', () => {
        const pos = editor.getCursorPosition();
        ws.send(JSON.stringify({
            type:'cursor',
            fileId,
            userId,
            row:pos.row,
            column:pos.column
        }));
    });
});