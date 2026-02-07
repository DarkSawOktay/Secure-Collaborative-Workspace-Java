<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="fr" class="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Accueil - IDE Collaboratif</title>
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

        .toggle-darkmode {
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .toggle-darkmode:hover {
            transform: rotate(15deg);
        }
    </style>
</head>
<body class="bg-neutral-50 dark:bg-neutral-900 text-neutral-800 dark:text-neutral-200 min-h-screen flex flex-col transition-colors duration-300">

<%@include file="../navbar.jsp"%>

<!-- Hero Section -->
<section class="gradient-bg py-20 px-4">
    <div class="container mx-auto max-w-5xl">
        <div class="flex flex-col lg:flex-row items-center">
            <div class="lg:w-1/2 mb-10 lg:mb-0 lg:pr-10">
                <h1 class="text-4xl md:text-5xl font-semibold text-neutral-900 dark:text-white leading-tight mb-4">
                    L'IDE collaboratif <span class="text-primary-600 dark:text-primary-400">intuitif</span> pour vos projets
                </h1>
                <p class="text-lg text-neutral-700 dark:text-neutral-300 mb-8">
                    Travaillez ensemble en temps réel sur du code, collaborez efficacement et partagez vos projets simplement.
                </p>
                <div class="flex flex-col sm:flex-row gap-4">
                    <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true"
                       class="btn-effect bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white font-medium px-6 py-3 rounded-lg transition shadow-md hover:shadow-lg flex items-center justify-center">
                        <span>Accéder à l'Éditeur</span>
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd"></path>
                        </svg>
                    </a>
                    <a href="#features"
                       class="bg-white dark:bg-neutral-800 hover:bg-neutral-100 dark:hover:bg-neutral-700 text-primary-600 dark:text-primary-400 font-medium px-6 py-3 rounded-lg transition border border-neutral-200 dark:border-neutral-700 shadow-sm hover:shadow flex items-center justify-center">
                        Découvrir les fonctionnalités
                    </a>
                </div>
            </div>
            <div class="lg:w-1/2">
                <div class="bg-white dark:bg-neutral-800 p-2 rounded-xl shadow-soft dark:shadow-soft-dark">
                    <img src="https://images.unsplash.com/photo-1517694712202-14dd9538aa97?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1470&q=80"
                         alt="Collaborative IDE"
                         class="rounded-lg w-full h-auto">
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Friends System Section -->
<section class="py-16 px-4 bg-white dark:bg-neutral-800">
    <div class="container mx-auto max-w-5xl">
        <div class="mb-12">
            <h2 class="text-3xl font-semibold text-center text-neutral-900 dark:text-white mb-2">Gérez vos collaborations</h2>
            <p class="text-center text-neutral-600 dark:text-neutral-400 max-w-2xl mx-auto">
                Invitez vos amis pour collaborer sur vos projets et maintenez votre réseau directement depuis la plateforme.
            </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-10">
            <!-- Add Friend Card -->
            <div class="bg-neutral-50 dark:bg-neutral-700 rounded-xl shadow-soft dark:shadow-soft-dark p-6 border border-neutral-100 dark:border-neutral-600 card-hover">
                <h3 class="text-xl font-medium text-neutral-900 dark:text-white mb-4 flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-primary-500 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
                    </svg>
                    Ajouter un ami
                </h3>
                <div class="bg-white dark:bg-neutral-800 p-4 rounded-lg border border-neutral-200 dark:border-neutral-600 shadow-sm mb-4">
                    <div class="flex space-x-2">
                        <input id="friend-pseudo" type="text" placeholder="Entrez un pseudo"
                               class="flex-1 p-2 border border-neutral-300 dark:border-neutral-600 dark:bg-neutral-700 dark:text-neutral-200 rounded-lg focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 focus:border-primary-500 dark:focus:border-primary-400 transition outline-none"/>
                        <button id="add-friend-btn"
                                class="btn-effect px-4 py-2 bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white rounded-lg transition shadow-sm hover:shadow-md">
                            Ajouter
                        </button>
                    </div>
                    <p id="add-friend-status" class="mt-2 text-sm text-red-500 dark:text-red-400"></p>
                </div>
                <p class="text-neutral-600 dark:text-neutral-400 text-sm">
                    Ajoutez des amis par leur pseudo pour commencer à collaborer sur des projets partagés.
                </p>
            </div>

            <!-- Friends List Card -->
            <div class="bg-neutral-50 dark:bg-neutral-700 rounded-xl shadow-soft dark:shadow-soft-dark p-6 border border-neutral-100 dark:border-neutral-600 card-hover">
                <h3 class="text-xl font-medium text-neutral-900 dark:text-white mb-4 flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-primary-500 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
                    </svg>
                    Amis en ligne
                </h3>
                <div class="bg-white dark:bg-neutral-800 p-4 rounded-lg border border-neutral-200 dark:border-neutral-600 shadow-sm h-[200px] overflow-y-auto">
                    <ul id="friends-list" class="space-y-2 min-h-[150px]">
                        <li class="text-center text-neutral-400 dark:text-neutral-500 py-10">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 mx-auto mb-2 animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z"></path>
                            </svg>
                            Chargement de la liste d'amis...
                        </li>
                    </ul>
                </div>
                <p class="text-neutral-600 dark:text-neutral-400 text-sm mt-4">
                    Discutez et collaborez en temps réel avec vos amis actuellement connectés.
                </p>
            </div>
        </div>
    </div>
</section>

<!-- Features Section -->
<section id="features" class="py-16 px-4 bg-neutral-50 dark:bg-neutral-900">
    <div class="container mx-auto max-w-5xl">
        <div class="mb-12">
            <h2 class="text-3xl font-semibold text-center text-neutral-900 dark:text-white mb-2">Fonctionnalités principales</h2>
            <p class="text-center text-neutral-600 dark:text-neutral-400 max-w-2xl mx-auto">
                Découvrez les outils qui font de notre IDE collaboratif la solution idéale pour vos projets.
            </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
            <!-- Feature 1 -->
            <div class="bg-white dark:bg-neutral-800 rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-neutral-100 dark:border-neutral-700 card-hover">
                <div class="bg-primary-100 dark:bg-primary-900/30 p-3 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 4a2 2 0 114 0v1a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-1a2 2 0 100 4h1a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 01-1-1v-1a2 2 0 10-4 0v1a1 1 0 01-1 1H7a1 1 0 01-1-1v-3a1 1 0 00-1-1H4a2 2 0 110-4h1a1 1 0 001-1V7a1 1 0 011-1h3a1 1 0 001-1V4z"></path>
                    </svg>
                </div>
                <h3 class="text-xl font-medium text-neutral-900 dark:text-white mb-2">Édition en temps réel</h3>
                <p class="text-neutral-600 dark:text-neutral-400">
                    Collaborez simultanément sur le même fichier avec plusieurs personnes, comme dans Google Docs.
                </p>
            </div>

            <!-- Feature 2 -->
            <div class="bg-white dark:bg-neutral-800 rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-neutral-100 dark:border-neutral-700 card-hover">
                <div class="bg-primary-100 dark:bg-primary-900/30 p-3 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                    </svg>
                </div>
                <h3 class="text-xl font-medium text-neutral-900 dark:text-white mb-2">Chat intégré</h3>
                <p class="text-neutral-600 dark:text-neutral-400">
                    Communiquez directement avec vos collaborateurs depuis l'interface de l'éditeur.
                </p>
            </div>

            <!-- Feature 3 -->
            <div class="bg-white dark:bg-neutral-800 rounded-xl p-6 shadow-soft dark:shadow-soft-dark border border-neutral-100 dark:border-neutral-700 card-hover">
                <div class="bg-primary-100 dark:bg-primary-900/30 p-3 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
                    </svg>
                </div>
                <h3 class="text-xl font-medium text-neutral-900 dark:text-white mb-2">Gestion de projets</h3>
                <p class="text-neutral-600 dark:text-neutral-400">
                    Organisez vos fichiers et dossiers facilement avec notre système de gestion de projets.
                </p>
            </div>
        </div>
    </div>
</section>

<!-- Footer -->
<footer class="bg-neutral-800 dark:bg-neutral-900 text-neutral-300 py-8 px-4 mt-auto border-t border-neutral-700 dark:border-neutral-800">
    <div class="container mx-auto max-w-5xl">
        <div class="flex flex-col md:flex-row justify-between items-center">
            <div class="mb-4 md:mb-0">
                <h2 class="text-xl font-semibold text-white">IDE Collaboratif</h2>
                <p class="text-sm text-neutral-400 mt-1">© 2025 - Tous droits réservés</p>
            </div>
            <nav class="flex space-x-6">
                <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true" class="text-neutral-400 hover:text-white transition">Éditeur</a>
                <a href="${pageContext.request.contextPath}/ProfileServlet" class="text-neutral-400 hover:text-white transition">Mon Profil</a>
                <a href="${pageContext.request.contextPath}/LogoutServlet" class="text-neutral-400 hover:text-white transition">Déconnexion</a>
            </nav>
        </div>
    </div>
</footer>

<script>
    // Accessible partout dans tes JS
    window.APP = {
        contextPath: '${pageContext.request.contextPath}'
    };

    // Dark Mode Toggle
    document.addEventListener('DOMContentLoaded', function() {
        // Check for saved theme preference or use the system preference
        if (localStorage.getItem('darkMode') === 'true' ||
            (!localStorage.getItem('darkMode') && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
            document.documentElement.classList.add('dark');
        }

        // Dark mode toggle
        document.getElementById('darkModeToggle').addEventListener('click', function() {
            if (document.documentElement.classList.contains('dark')) {
                document.documentElement.classList.remove('dark');
                localStorage.setItem('darkMode', 'false');
            } else {
                document.documentElement.classList.add('dark');
                localStorage.setItem('darkMode', 'true');
            }
        });
    });
</script>

<!-- scripts externes -->
<script src="${pageContext.request.contextPath}/js/inviteFriend.js"></script>
<script src="${pageContext.request.contextPath}/js/friendsList.js"></script>

</body>
</html>