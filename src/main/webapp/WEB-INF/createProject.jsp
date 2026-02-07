<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr" class="light">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Créer un nouveau projet - IDE Collaboratif</title>
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

        .card-animation {
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }

        .card-animation:hover {
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
            border: 1px solid;
            padding: 0.75rem 1rem;
            border-radius: 0.5rem;
            width: 100%;
            outline: none;
        }

        .form-input:focus {
            box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.3);
        }

        .toggle-darkmode {
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .toggle-darkmode:hover {
            transform: rotate(15deg);
        }

        /* Animation du message d'erreur */
        .error-message {
            animation: shake 0.6s ease-in-out;
        }

        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
            20%, 40%, 60%, 80% { transform: translateX(5px); }
        }
    </style>
</head>
<body class="bg-neutral-50 dark:bg-neutral-900 text-neutral-800 dark:text-neutral-200 min-h-screen flex items-center justify-center p-4 transition-colors duration-300">

<!-- Dark Mode Toggle -->
<div class="fixed top-4 right-4 z-50">
    <button id="darkModeToggle" class="toggle-darkmode p-2 bg-white dark:bg-neutral-800 rounded-full shadow-md">
        <!-- Sun Icon (Light Mode) -->
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-neutral-800 dark:hidden" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clip-rule="evenodd" />
        </svg>
        <!-- Moon Icon (Dark Mode) -->
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary-300 hidden dark:block" viewBox="0 0 20 20" fill="currentColor">
            <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
        </svg>
    </button>
</div>

<div class="card-animation w-full max-w-md">
    <!-- Card Header -->
    <div class="gradient-bg p-6 rounded-t-xl shadow-soft relative overflow-hidden">
        <h1 class="text-2xl md:text-3xl font-semibold text-neutral-900 dark:text-white flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7 mr-2 text-primary-600 dark:text-primary-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
            </svg>
            Nouveau projet
        </h1>
        <p class="text-neutral-700 dark:text-neutral-300 mt-2">Créez un nouveau projet pour commencer à collaborer</p>
    </div>

    <!-- Card Body -->
    <div class="bg-white dark:bg-neutral-800 p-6 rounded-b-xl shadow-soft border border-neutral-100 dark:border-neutral-700">
        <c:if test="${not empty error}">
            <div class="mb-5 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg text-red-600 dark:text-red-400 text-sm error-message flex items-start">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 flex-shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
                <span>${error}</span>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/EditorServlet" class="space-y-5">
            <div>
                <label for="projectName" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                    Nom du dossier projet
                </label>
                <div class="relative">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-neutral-400 dark:text-neutral-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                        </svg>
                    </div>
                    <input type="text" id="projectName" name="projectName" required
                           class="form-input pl-10 bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400"
                           placeholder="mon-super-projet">
                </div>
                <p class="mt-1 text-xs text-neutral-500 dark:text-neutral-400">Le nom doit être unique et ne contenir que des lettres, des chiffres, des tirets ou des underscores.</p>
            </div>

            <div class="pt-2">
                <button type="submit"
                        class="btn-effect w-full bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white font-medium py-3 px-4 rounded-lg transition shadow-md hover:shadow-lg flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                    </svg>
                    <span>Créer le projet</span>
                </button>
            </div>

            <div class="mt-4 text-center">
                <a href="${pageContext.request.contextPath}/EditorServlet" class="text-sm text-primary-600 dark:text-primary-400 hover:text-primary-500 dark:hover:text-primary-300 transition">
                    Retour à la liste des projets
                </a>
            </div>
        </form>
    </div>
</div>

<script>
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

</body>
</html>