<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8"/>
    <title>Mon Profil</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
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

        .card {
            transition: all 0.2s ease;
            border: 1px solid rgba(0, 0, 0, 0.05);
        }

        .card:hover {
            box-shadow: 0 4px 20px -5px rgba(0, 0, 0, 0.1);
        }

        .badge-counter {
            position: relative;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }

        .badge-counter::after {
            content: attr(data-count);
            position: absolute;
            top: -5px;
            right: -8px;
            font-size: 11px;
            height: 18px;
            width: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background-color: #ef4444;
            color: white;
            font-weight: 600;
        }

        .notification-item {
            transition: background-color 0.2s ease;
        }

        .notification-item:hover {
            background-color: rgba(0, 0, 0, 0.02);
        }

        .dark .notification-item:hover {
            background-color: rgba(255, 255, 255, 0.05);
        }

        .card-unread {
            border-left: 3px solid #0ea5e9;
        }

        .card-read {
            border-left: 3px solid transparent;
        }

        .animate-fadeIn {
            animation: fadeIn 0.3s ease-in;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>

<body class="bg-neutral-50 dark:bg-neutral-900 text-neutral-800 dark:text-neutral-100 min-h-screen">

<%@ include file="/navbar.jsp" %>

<script>
    window.APP = {
        contextPath: '${pageContext.request.contextPath}',
        userId: ${sessionScope.utilisateur.idUtilisateur}
    };
</script>

<div class="container mx-auto px-4 py-8 max-w-6xl">
    <div class="flex flex-col md:flex-row items-start gap-6">
        <!-- Sidebar du profil -->
        <div class="w-full md:w-64 mb-8 md:mb-0">
            <div class="bg-white dark:bg-neutral-800 rounded-xl shadow-soft p-6">
                <div class="text-center mb-4">
                    <div class="inline-block p-2 bg-primary-100 dark:bg-primary-900 rounded-full mb-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </div>
                    <h1 class="text-xl font-semibold text-neutral-900 dark:text-neutral-100">
                        <c:out value="${sessionScope.utilisateur.nomUtilisateur}"/>
                    </h1>
                    <p class="text-neutral-500 dark:text-neutral-400 text-sm mt-1">Membre actif</p>
                </div>

                <hr class="border-neutral-200 dark:border-neutral-700 my-4">

                <div class="space-y-3">
                    <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true"
                       class="flex items-center text-neutral-700 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400 transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                        Mes projets
                    </a>
                    <a href="#" class="flex items-center text-neutral-700 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400 transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
                        </svg>
                        Amis
                    </a>
                    <a href="#" class="flex items-center text-neutral-700 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400 transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        </svg>
                        Paramètres
                    </a>
                    <a href="${pageContext.request.contextPath}/LogoutServlet"
                       class="flex items-center text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 transition-colors mt-6">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                        </svg>
                        Déconnexion
                    </a>
                </div>
            </div>
        </div>

        <!-- Contenu principal -->
        <div class="flex-1">
            <div class="mb-8">
                <h1 class="text-2xl font-bold flex items-center mb-1">
                    <span>Tableau de bord</span>
                    <span id="unread-badge" class="badge-counter ml-2 text-sm hidden" data-count="0"></span>
                </h1>
                <p class="text-neutral-600 dark:text-neutral-400">Bienvenue, <c:out value="${sessionScope.utilisateur.nomUtilisateur}"/></p>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <!-- Section Notifications -->
                <div class="card bg-white dark:bg-neutral-800 rounded-xl shadow-soft p-6 animate-fadeIn">
                    <div class="flex items-center justify-between mb-4">
                        <h2 class="text-xl font-semibold text-neutral-900 dark:text-neutral-100 flex items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                            </svg>
                            Notifications
                        </h2>
                        <div class="flex space-x-1">
                            <button id="all-notifications-btn" class="px-3 py-1 bg-primary-600 text-white rounded-md text-sm hover:bg-primary-700 transition-colors">Toutes</button>
                            <button id="unread-notifications-btn" class="px-3 py-1 bg-neutral-200 text-neutral-800 dark:bg-neutral-700 dark:text-neutral-300 rounded-md text-sm hover:bg-neutral-300 dark:hover:bg-neutral-600 transition-colors">Non lues</button>
                            <button id="mark-all-read-btn" class="text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 px-2 transition-colors">
                                Tout marquer comme lu
                            </button>
                        </div>
                    </div>

                    <div class="bg-neutral-50 dark:bg-neutral-900 rounded-lg p-4 border border-neutral-200 dark:border-neutral-700">
                        <ul id="notifications-list" class="space-y-3 max-h-80 overflow-y-auto"></ul>
                        <div id="no-notifications" class="text-neutral-500 dark:text-neutral-400 italic text-center py-8 hidden">
                            Aucune notification
                        </div>
                        <div id="loading-notifications" class="text-center py-8">
                            <div class="inline-flex items-center justify-center">
                                <svg class="animate-spin h-5 w-5 text-primary-600 dark:text-primary-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                <span class="ml-2 text-neutral-600 dark:text-neutral-400">Chargement...</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Section Invitations à collaborer -->
                <div class="card bg-white dark:bg-neutral-800 rounded-xl shadow-soft p-6 animate-fadeIn">
                    <h2 class="text-xl font-semibold text-neutral-900 dark:text-neutral-100 mb-4 flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                        </svg>
                        Invitations à collaborer
                    </h2>

                    <div class="bg-neutral-50 dark:bg-neutral-900 rounded-lg p-4 border border-neutral-200 dark:border-neutral-700">
                        <ul id="invitation-list" class="space-y-3 max-h-80 overflow-y-auto">
                            <li class="text-neutral-500 dark:text-neutral-400 italic text-center py-6">
                                <div class="inline-flex items-center justify-center">
                                    <svg class="animate-spin h-5 w-5 text-primary-600 dark:text-primary-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    <span class="ml-2">Chargement des invitations...</span>
                                </div>
                            </li>
                        </ul>
                        <div id="no-invitations" class="text-neutral-500 dark:text-neutral-400 italic text-center py-8 hidden">
                            Aucune invitation à collaborer
                        </div>
                    </div>
                </div>

                <!-- Section Demandes d'ami -->
                <div class="card bg-white dark:bg-neutral-800 rounded-xl shadow-soft p-6 animate-fadeIn">
                    <h2 class="text-xl font-semibold text-neutral-900 dark:text-neutral-100 mb-4 flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                        </svg>
                        Demandes d'ami
                    </h2>

                    <div class="bg-neutral-50 dark:bg-neutral-900 rounded-lg p-4 border border-neutral-200 dark:border-neutral-700">
                        <ul id="friend-requests-list" class="space-y-3 max-h-80 overflow-y-auto">
                            <li class="text-neutral-500 dark:text-neutral-400 italic text-center py-6">
                                <div class="inline-flex items-center justify-center">
                                    <svg class="animate-spin h-5 w-5 text-primary-600 dark:text-primary-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    <span class="ml-2">Chargement des demandes...</span>
                                </div>
                            </li>
                        </ul>
                        <div id="no-friend-requests" class="text-neutral-500 dark:text-neutral-400 italic text-center py-8 hidden">
                            Aucune demande d'ami
                        </div>
                    </div>
                </div>

                <!-- Section Projets récents -->
                <div class="card bg-white dark:bg-neutral-800 rounded-xl shadow-soft p-6 animate-fadeIn">
                    <h2 class="text-xl font-semibold text-neutral-900 dark:text-neutral-100 mb-4 flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2" />
                        </svg>
                        Projets récents
                    </h2>

                    <div class="bg-neutral-50 dark:bg-neutral-900 rounded-lg p-4 border border-neutral-200 dark:border-neutral-700">
                        <div class="grid grid-cols-1 gap-3">
                            <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true" class="flex items-center justify-between p-3 bg-white dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700 rounded-lg hover:border-primary-300 dark:hover:border-primary-700 transition-colors">
                                <div class="flex items-center">
                                    <div class="p-2 bg-primary-100 dark:bg-primary-900 rounded mr-3">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                        </svg>
                                    </div>
                                    <div>
                                        <h3 class="font-medium text-neutral-900 dark:text-neutral-100">Créer un nouveau projet</h3>
                                        <p class="text-sm text-neutral-500 dark:text-neutral-400">Commencer à coder dès maintenant</p>
                                    </div>
                                </div>
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-neutral-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                                </svg>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- JS -->
<script src="${pageContext.request.contextPath}/js/notifications.js"></script>
<script src="${pageContext.request.contextPath}/js/invitations.js"></script>
<script src="${pageContext.request.contextPath}/js/friendRequests.js"></script>

</body>
</html>
