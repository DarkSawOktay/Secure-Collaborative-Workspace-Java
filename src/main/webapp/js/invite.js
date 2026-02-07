document.addEventListener('DOMContentLoaded', () => {
    if (!window.APP || window.APP.fileId == null) return;
    const ctx       = window.APP.contextPath;
    const fid       = window.APP.fileId;
    const selUser   = document.getElementById('user-invite');
    const selRole   = document.getElementById('invite-role');
    const btnInvite = document.getElementById('invite-btn');
    if (!selUser || !selRole || !btnInvite) return;

    function loadInviteList(){
        fetch(`${ctx}/ListUtilisateursServlet?fichier_id=${fid}`)
            .then(r=>r.json())
            .then(users=>{
                selUser.innerHTML = '<option value="">— choisir —</option>';
                users.forEach(u=>{
                    if(u.id!==window.APP.userId){
                        const o = document.createElement('option');
                        o.value = u.id;
                        o.textContent = `${u.nomUtilisateur} (${u.email})`;
                        selUser.appendChild(o);
                    }
                });
            });
    }

    btnInvite.addEventListener('click', ()=>{
        const uid  = selUser.value;
        const role = selRole.value;
        if(!uid) return;
        fetch(`${ctx}/CreateInvitationServlet`, {
            method:'POST',
            headers:{'Content-Type':'application/x-www-form-urlencoded'},
            body:`fichier_id=${fid}&invitee_id=${uid}&role=${role}`
        })
            .then(r=>r.json())
            .then(j=>{
                if(j.success){
                    btnInvite.textContent = '✅';
                    btnInvite.disabled = true;
                } else {
                    alert(j.message||'Erreur');
                }
                setTimeout(()=>{
                    btnInvite.textContent = 'Inviter';
                    btnInvite.disabled = false;
                    loadInviteList();
                    window.loadCollaborators && window.loadCollaborators();
                },1500);
            });
    });

    loadInviteList();
});
