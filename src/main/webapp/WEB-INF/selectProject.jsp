<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr" class="light">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Vos Projets - IDE Collaboratif</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      darkMode: 'class',
      theme: {
        extend: {
          colors: {
            primary: {
              50: '#f0f9ff',
              100: '#e0f2fe',
              200: '#bae6fd',
              300: '#7dd3fc',
              400: '#38bdf8',
              500: '#0ea5e9',
              600: '#0284c7',
              700: '#0369a1',
              800: '#075985',
              900: '#0c4a6e',
            },
            neutral: {
              50: '#fafafa',
              100: '#f5f5f5',
              200: '#e5e5e5',
              300: '#d4d4d4',
              400: '#a3a3a3',
              500: '#737373',
              600: '#525252',
              700: '#404040',
              800: '#262626',
              900: '#171717',
            }
          },
          fontFamily: {
            sans: ['Inter', 'sans-serif'],
          },
          boxShadow: {
            'soft': '0 2px 15px -3px rgba(0, 0, 0, 0.07), 0 10px 20px -2px rgba(0, 0, 0, 0.04)',
            'soft-dark': '0 2px 15px -3px rgba(0, 0, 0, 0.2), 0 10px 20px -2px rgba(0, 0, 0, 0.15)',
          }
        }
      }
    }
  </script>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap">
  <style>
    body {
      font-family: 'Inter', sans-serif;
    }

    .gradient-bg {
      background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    }

    .dark .gradient-bg {
      background: linear-gradient(135deg, #075985 0%, #0c4a6e 100%);
    }

    .card-hover {
      transition: all 0.3s ease;
    }

    .card-hover:hover {
      transform: translateY(-5px);
    }

    .btn-effect {
      position: relative;
      overflow: hidden;
      transition: all 0.3s ease;
    }

    .btn-effect:after {
      content: '';
      position: absolute;
      width: 100%;
      height: 100%;
      top: 0;
      left: -100%;
      background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.2) 50%, rgba(255,255,255,0) 100%);
      transition: all 0.8s ease;
    }

    .dark .btn-effect:after {
      background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0) 100%);
    }

    .btn-effect:hover:after {
      left: 100%;
    }

    .btn-effect:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .form-input {
      transition: all 0.3s ease;
    }

    .form-input:focus {
      box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.3);
    }

    .project-card {
      transition: all 0.3s ease;
    }

    .project-card:hover {
      transform: translateY(-2px);
    }

    .create-btn {
      transition: all 0.2s ease;
    }

    .create-btn:hover {
      transform: translateY(-1px);
    }

    .project-action {
      transition: all 0.2s ease;
    }

    .project-action:hover {
      transform: scale(1.15);
    }

    /* Chargement animé */
    .loading-dots:after {
      content: "...";
      animation: loading 1.5s infinite;
      display: inline-block;
      width: 20px;
      text-align: left;
    }

    @keyframes loading {
      0% { content: "."; }
      33% { content: ".."; }
      66% { content: "..."; }
    }

    /* Animation pour le skeleton loader */
    .skeleton {
      position: relative;
      overflow: hidden;
    }

    .skeleton::after {
      content: "";
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      transform: translateX(-100%);
      background-image: linear-gradient(
              90deg,
              rgba(255, 255, 255, 0) 0,
              rgba(255, 255, 255, 0.2) 20%,
              rgba(255, 255, 255, 0.5) 60%,
              rgba(255, 255, 255, 0)
      );
      animation: shimmer 2s infinite;
    }

    .dark .skeleton::after {
      background-image: linear-gradient(
              90deg,
              rgba(255, 255, 255, 0) 0,
              rgba(255, 255, 255, 0.05) 20%,
              rgba(255, 255, 255, 0.1) 60%,
              rgba(255, 255, 255, 0)
      );
    }

    @keyframes shimmer {
      100% {
        transform: translateX(100%);
      }
    }

    /* Tooltips */
    .tooltip {
      position: relative;
    }

    .tooltip .tooltip-text {
      visibility: hidden;
      width: auto;
      white-space: nowrap;
      background-color: rgba(0, 0, 0, 0.8);
      color: #fff;
      text-align: center;
      border-radius: 6px;
      padding: 5px 10px;
      position: absolute;
      z-index: 1;
      bottom: 125%;
      left: 50%;
      transform: translateX(-50%);
      opacity: 0;
      transition: opacity 0.3s;
    }

    .tooltip .tooltip-text::after {
      content: "";
      position: absolute;
      top: 100%;
      left: 50%;
      margin-left: -5px;
      border-width: 5px;
      border-style: solid;
      border-color: rgba(0, 0, 0, 0.8) transparent transparent transparent;
    }

    .tooltip:hover .tooltip-text {
      visibility: visible;
      opacity: 1;
    }
  </style>
</head>
<body class="bg-neutral-50 dark:bg-neutral-900 text-neutral-800 dark:text-neutral-200 min-h-screen flex flex-col transition-colors duration-300">

<%@ include file="/navbar.jsp" %>

<div class="container mx-auto px-4 py-8 flex-grow">
  <div class="max-w-5xl mx-auto space-y-8">
    <!-- En-tête de page -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6">
      <div>
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white">Vos projets</h1>
        <p class="text-neutral-600 dark:text-neutral-400 mt-1">Gérez vos projets de développement collaboratif</p>
      </div>

      <a href="${pageContext.request.contextPath}/ProfileServlet" class="mt-4 md:mt-0 text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 flex items-center group">
        <span>Voir mon profil</span>
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transition-transform group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
        </svg>
      </a>
    </div>

    <!-- Formulaire de création de nouveau projet -->
    <div class="bg-white dark:bg-neutral-800 rounded-xl shadow-soft dark:shadow-soft-dark border border-neutral-100 dark:border-neutral-700 overflow-hidden">
      <div class="gradient-bg p-5 md:p-6">
        <h2 class="text-xl font-semibold text-neutral-900 dark:text-white flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          Créer un nouveau projet
        </h2>
      </div>
      <div class="p-5 md:p-6">
        <form method="post" action="${pageContext.request.contextPath}/EditorServlet" class="flex flex-col sm:flex-row gap-3">
          <div class="relative flex-grow">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-neutral-400 dark:text-neutral-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
              </svg>
            </div>
            <input name="projectName" type="text" required placeholder="Nom du projet (ex: mon-super-projet)"
                   class="form-input pl-10 w-full px-4 py-3 bg-neutral-50 dark:bg-neutral-700 border border-neutral-200 dark:border-neutral-600 rounded-lg focus:outline-none focus:border-primary-500 dark:focus:border-primary-400 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400"/>
          </div>
          <button type="submit"
                  class="btn-effect create-btn bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white font-medium px-6 py-3 rounded-lg transition shadow-md hover:shadow-lg flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
            </svg>
            Créer
          </button>
        </form>
      </div>
    </div>

    <!-- Liste des projets existants -->
    <div class="bg-white dark:bg-neutral-800 rounded-xl shadow-soft dark:shadow-soft-dark border border-neutral-100 dark:border-neutral-700 overflow-hidden">
      <div class="gradient-bg p-5 md:p-6">
        <h2 class="text-xl font-semibold text-neutral-900 dark:text-white flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
          </svg>
          Projets auxquels vous avez accès
        </h2>
      </div>
      <div class="p-5 md:p-6">
        <div id="skeleton-loader" class="space-y-3">
          <div class="bg-neutral-100 dark:bg-neutral-700 h-12 rounded-lg w-full skeleton"></div>
          <div class="bg-neutral-100 dark:bg-neutral-700 h-12 rounded-lg w-full skeleton"></div>
          <div class="bg-neutral-100 dark:bg-neutral-700 h-12 rounded-lg w-full skeleton"></div>
        </div>

        <div id="no-projects" class="hidden py-8 text-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto text-neutral-300 dark:text-neutral-600 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
          </svg>
          <p class="text-neutral-600 dark:text-neutral-400">Vous n'avez pas encore de projets</p>
          <p class="text-neutral-500 dark:text-neutral-500 text-sm mt-1">Créez un projet pour commencer à coder</p>
        </div>

        <ul id="project-list" class="hidden space-y-3"></ul>
      </div>
    </div>
  </div>
</div>

<!-- Script pour le mode sombre -->
<script src="${pageContext.request.contextPath}/js/theme.js"></script>

<script>
  document.addEventListener('DOMContentLoaded', function() {
    // Chargement de la liste de projets
    const ctx = '${pageContext.request.contextPath}';

    fetch(ctx + '/ListProjectsServlet')
            .then(r => {
              if (!r.ok) throw new Error(r.status);
              return r.json();
            })
            .then(arr => {
              // Cacher le loader
              document.getElementById('skeleton-loader').classList.add('hidden');

              // Afficher le message si aucun projet
              if (arr.length === 0) {
                document.getElementById('no-projects').classList.remove('hidden');
                return;
              }

              // Afficher la liste des projets
              const ul = document.getElementById('project-list');
              ul.classList.remove('hidden');
              ul.innerHTML = '';

              arr.forEach(p => {
                const li = document.createElement('li');
                li.className = 'project-card bg-neutral-50 dark:bg-neutral-700 rounded-lg overflow-hidden border border-neutral-200 dark:border-neutral-600 shadow-sm hover:shadow transition-all';

                // Conteneur principal
                const container = document.createElement('div');
                container.className = 'flex items-center';

                // Bouton principal avec le nom du projet
                const btnContainer = document.createElement('div');
                btnContainer.className = 'flex-grow flex items-center';

                const btn = document.createElement('button');
                btn.className = 'flex items-center w-full px-4 py-3 text-left hover:bg-neutral-100 dark:hover:bg-neutral-600 transition-colors';
                btn.onclick = function() {
                  location.href = ctx + '/EditorServlet?projectId=' + p.id;
                };

                // Icône de dossier
                const folderIcon = document.createElement('span');
                folderIcon.className = 'mr-3 text-primary-500 dark:text-primary-400 flex-shrink-0';
                folderIcon.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
            </svg>
          `;

                // Texte du projet avec badge propriétaire si nécessaire
                const textContainer = document.createElement('div');
                textContainer.className = 'flex flex-col';

                const projectName = document.createElement('span');
                projectName.className = 'font-medium text-neutral-800 dark:text-neutral-200';
                projectName.textContent = p.nom;

                textContainer.appendChild(projectName);

                if (p.owner) {
                  const ownerBadge = document.createElement('span');
                  ownerBadge.className = 'text-xs text-neutral-500 dark:text-neutral-400 mt-0.5';
                  ownerBadge.textContent = 'Propriétaire';
                  textContainer.appendChild(ownerBadge);
                }

                btn.appendChild(folderIcon);
                btn.appendChild(textContainer);
                btnContainer.appendChild(btn);
                container.appendChild(btnContainer);

                // Si propriétaire, on peut supprimer ou renommer le projet
                if (p.owner) {
                  const actions = document.createElement('div');
                  actions.className = 'flex items-center px-3';

                  // Renommer
                  const rename = document.createElement('button');
                  rename.className = 'tooltip project-action p-2 text-amber-500 dark:text-amber-400 hover:text-amber-600 dark:hover:text-amber-300 mx-1';
                  rename.innerHTML = `
              <span class="tooltip-text">Renommer</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
              </svg>
            `;
                  rename.onclick = (e) => {
                    e.stopPropagation();
                    const nouveau = prompt('Nouveau nom du projet :', p.nom);
                    if (!nouveau) return;
                    fetch(ctx +`/RenameFolderServlet`, {
                      method: 'POST',
                      headers: { 'Content-Type':'application/x-www-form-urlencoded' },
                      body: 'folderId=' + p.id + '&newName=' + encodeURIComponent(nouveau)
                    }).then(() => location.reload());
                  };
                  actions.appendChild(rename);

                  // Supprimer
                  const del = document.createElement('button');
                  del.className = 'tooltip project-action p-2 text-red-500 hover:text-red-600 dark:text-red-400 dark:hover:text-red-300 mx-1';
                  del.innerHTML = `
              <span class="tooltip-text">Supprimer</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
              </svg>
            `;
                  del.onclick = (e) => {
                    e.stopPropagation();
                    if (!confirm('Supprimer définitivement ce projet et tous ses fichiers ?')) return;
                    fetch(ctx +`/DeleteFolderServlet`, {
                      method:'POST',
                      headers:{'Content-Type':'application/x-www-form-urlencoded'},
                      body: 'folderId=' + p.id
                    }).then(() => location.reload());
                  };
                  actions.appendChild(del);

                  container.appendChild(actions);
                }

                li.appendChild(container);
                ul.appendChild(li);
              });
            })
            .catch(err => {
              console.error('Erreur lors du chargement des projets:', err);
              document.getElementById('skeleton-loader').classList.add('hidden');

              // Afficher un message d'erreur
              const ul = document.getElementById('project-list');
              ul.classList.remove('hidden');
              ul.innerHTML = `
          <li class="p-4 text-center">
            <div class="text-red-500 dark:text-red-400 mb-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <p class="text-neutral-700 dark:text-neutral-300">Impossible de charger les projets</p>
            <button onclick="location.reload()" class="mt-3 px-4 py-2 bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white rounded-md">
              Réessayer
            </button>
          </li>
        `;
            });
  });
</script>
</body>
</html>