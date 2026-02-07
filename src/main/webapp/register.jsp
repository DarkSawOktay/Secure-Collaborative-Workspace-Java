<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="fr" class="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inscription - IDE Collaboratif</title>
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

        .btn-effect:hover:after {
            left: 100%;
        }

        .btn-effect:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .card-animation {
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }

        .card-animation:hover {
            transform: translateY(-5px);
        }

        .toggle-darkmode {
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .toggle-darkmode:hover {
            transform: rotate(15deg);
        }

        .input-group {
            position: relative;
        }

        .input-icon {
            position: absolute;
            left: 1rem;
            top: 50%;
            transform: translateY(-50%);
            color: #a3a3a3;
        }

        .input-with-icon {
            padding-left: 2.5rem;
        }

        /* Animations for form validation */
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
            20%, 40%, 60%, 80% { transform: translateX(5px); }
        }

        .shake {
            animation: shake 0.6s;
        }

        /* Animation du message flash */
        .flash-message {
            animation: flash-in 0.5s ease-out forwards;
        }

        @keyframes flash-in {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .flash-out {
            animation: flash-out 0.5s ease-in forwards;
        }

        @keyframes flash-out {
            from {
                opacity: 1;
                transform: translateY(0);
            }
            to {
                opacity: 0;
                transform: translateY(-20px);
            }
        }

        /* Étapes de progression */
        .progress-step {
            transition: all 0.3s ease;
        }

        .progress-step.active {
            background-color: #0ea5e9;
            border-color: #0ea5e9;
        }

        .progress-line {
            transition: width 0.4s ease;
        }
    </style>
</head>
<body class="bg-neutral-50 dark:bg-neutral-900 text-neutral-800 dark:text-neutral-200 min-h-screen flex flex-col transition-colors duration-300">

<%@include file="navbar.jsp"%>

<!-- Main Content -->
<main class="flex-grow flex items-center justify-center py-12 px-4">
    <div class="card-animation w-full max-w-md">
        <!-- Card Header -->
        <div class="gradient-bg p-6 rounded-t-xl shadow-soft relative overflow-hidden">
            <h1 class="text-2xl md:text-3xl font-semibold text-neutral-900 dark:text-white">Créer un compte</h1>
            <p class="text-neutral-700 dark:text-neutral-300 mt-2">Rejoignez notre communauté de développeurs</p>

            <!-- Étapes de progression -->
            <div class="mt-6 flex items-center">
                <div class="progress-step active h-8 w-8 rounded-full border-2 border-neutral-300 dark:border-neutral-600 flex items-center justify-center bg-primary-500 text-white">
                    1
                </div>
                <div class="relative flex-1 mx-2 h-1 bg-neutral-200 dark:bg-neutral-700 rounded">
                    <div class="progress-line absolute h-1 bg-primary-500 rounded" style="width: 0%;"></div>
                </div>
                <div class="progress-step h-8 w-8 rounded-full border-2 border-neutral-300 dark:border-neutral-600 flex items-center justify-center text-neutral-500 dark:text-neutral-400">
                    2
                </div>
                <div class="relative flex-1 mx-2 h-1 bg-neutral-200 dark:bg-neutral-700 rounded">
                    <div class="progress-line absolute h-1 bg-primary-500 rounded" style="width: 0%;"></div>
                </div>
                <div class="progress-step h-8 w-8 rounded-full border-2 border-neutral-300 dark:border-neutral-600 flex items-center justify-center text-neutral-500 dark:text-neutral-400">
                    3
                </div>
            </div>
        </div>

        <!-- Card Body -->
        <div class="bg-white dark:bg-neutral-800 p-6 rounded-b-xl shadow-soft border border-neutral-100 dark:border-neutral-700">
            <%@ include file="includes/flash.jsp" %>

            <form id="register-form" action="RegisterServlet" method="post" class="space-y-5">
                <div id="step1" class="space-y-5">
                    <div class="input-group">
                        <label for="username" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Nom d'utilisateur</label>
                        <div class="relative">
                            <span class="input-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path>
                                </svg>
                            </span>
                            <input type="text" id="username" name="username" required
                                   class="form-input input-with-icon bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400"
                                   placeholder="JohnDev42">
                        </div>
                        <p class="mt-1 text-xs text-neutral-500 dark:text-neutral-400">Votre identifiant unique visible par les autres membres</p>
                    </div>

                    <div class="input-group">
                        <label for="email" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Adresse e-mail</label>
                        <div class="relative">
                            <span class="input-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path>
                                    <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path>
                                </svg>
                            </span>
                            <input type="email" id="email" name="email" required
                                   class="form-input input-with-icon bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400"
                                   placeholder="john@exemple.com">
                        </div>
                        <p class="mt-1 text-xs text-neutral-500 dark:text-neutral-400">Nous ne partagerons jamais votre email</p>
                    </div>

                    <div class="input-group">
                        <label for="password" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Mot de passe</label>
                        <div class="relative">
                            <span class="input-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"></path>
                                </svg>
                            </span>
                            <input type="password" id="password" name="password" required
                                   class="form-input input-with-icon bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400"
                                   placeholder="••••••••">
                            <button type="button" id="togglePassword" class="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-500 dark:text-neutral-400">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"></path>
                                    <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"></path>
                                </svg>
                            </button>
                        </div>
                        <div class="mt-1 h-1 w-full bg-neutral-200 dark:bg-neutral-700 rounded-full overflow-hidden">
                            <div id="password-strength" class="h-full bg-red-500 transition-all duration-300" style="width: 0%"></div>
                        </div>
                        <p id="password-feedback" class="mt-1 text-xs text-neutral-500 dark:text-neutral-400">Utilisez au moins 8 caractères</p>
                    </div>

                    <div>
                        <button type="button" id="continue-btn"
                                class="btn-effect w-full bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white font-medium py-3 px-4 rounded-lg transition shadow-md hover:shadow-lg flex items-center justify-center mt-4">
                            <span>Continuer</span>
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd"></path>
                            </svg>
                        </button>
                    </div>
                </div>

                <div id="step2" class="space-y-5 hidden">
                    <div class="input-group">
                        <label for="firstName" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Prénom (optionnel)</label>
                        <input type="text" id="firstName" name="firstName"
                               class="form-input bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400"
                               placeholder="John">
                    </div>

                    <div class="input-group">
                        <label for="lastName" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Nom (optionnel)</label>
                        <input type="text" id="lastName" name="lastName"
                               class="form-input bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400"
                               placeholder="Doe">
                    </div>

                    <div class="flex space-x-3">
                        <button type="button" id="back-btn-1"
                                class="btn-effect flex-1 bg-white dark:bg-neutral-700 hover:bg-neutral-100 dark:hover:bg-neutral-600 text-neutral-800 dark:text-neutral-200 font-medium py-3 px-4 rounded-lg transition border border-neutral-200 dark:border-neutral-600 shadow-sm hover:shadow flex items-center justify-center mt-4">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clip-rule="evenodd"></path>
                            </svg>
                            <span>Retour</span>
                        </button>
                        <button type="button" id="continue-btn-2"
                                class="btn-effect flex-1 bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white font-medium py-3 px-4 rounded-lg transition shadow-md hover:shadow-lg flex items-center justify-center mt-4">
                            <span>Continuer</span>
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd"></path>
                            </svg>
                        </button>
                    </div>
                </div>

                <div id="step3" class="space-y-5 hidden">
                    <div class="input-group">
                        <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Votre niveau en programmation</label>
                        <div class="grid grid-cols-3 gap-2 mt-1">
                            <label class="cursor-pointer">
                                <input type="radio" name="level" value="beginner" class="sr-only peer">
                                <div class="flex flex-col items-center p-4 rounded-lg border-2 border-neutral-200 dark:border-neutral-600 peer-checked:border-primary-500 peer-checked:bg-primary-50 dark:peer-checked:bg-primary-900/20 transition">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-neutral-400 peer-checked:text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10"></path>
                                    </svg>
                                    <span class="mt-2 font-medium text-sm text-center">Débutant</span>
                                </div>
                            </label>
                            <label class="cursor-pointer">
                                <input type="radio" name="level" value="intermediate" class="sr-only peer" checked>
                                <div class="flex flex-col items-center p-4 rounded-lg border-2 border-neutral-200 dark:border-neutral-600 peer-checked:border-primary-500 peer-checked:bg-primary-50 dark:peer-checked:bg-primary-900/20 transition">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-neutral-400 peer-checked:text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                                    </svg>
                                    <span class="mt-2 font-medium text-sm text-center">Intermédiaire</span>
                                </div>
                            </label>
                            <label class="cursor-pointer">
                                <input type="radio" name="level" value="advanced" class="sr-only peer">
                                <div class="flex flex-col items-center p-4 rounded-lg border-2 border-neutral-200 dark:border-neutral-600 peer-checked:border-primary-500 peer-checked:bg-primary-50 dark:peer-checked:bg-primary-900/20 transition">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-neutral-400 peer-checked:text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"></path>
                                    </svg>
                                    <span class="mt-2 font-medium text-sm text-center">Avancé</span>
                                </div>
                            </label>
                        </div>
                    </div>

                    <div class="input-group">
                        <label for="interests" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Centres d'intérêt (optionnel)</label>
                        <select id="interests" name="interests" multiple
                                class="form-input bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400 h-24">
                            <option value="web">Développement Web</option>
                            <option value="mobile">App Mobile</option>
                            <option value="game">Jeux Vidéo</option>
                            <option value="data">Data Science</option>
                            <option value="ai">Intelligence Artificielle</option>
                            <option value="security">Cybersécurité</option>
                        </select>
                        <p class="mt-1 text-xs text-neutral-500 dark:text-neutral-400">Maintenez Ctrl/Cmd pour sélectionner plusieurs options</p>
                    </div>

                    <div class="flex items-center mt-4">
                        <input type="checkbox" id="terms" name="terms" required
                               class="h-4 w-4 text-primary-600 border-neutral-300 dark:border-neutral-600 rounded focus:ring-primary-500 dark:focus:ring-primary-400">
                        <label for="terms" class="ml-2 block text-sm text-neutral-700 dark:text-neutral-300">
                            J'accepte les <a href="#" class="text-primary-600 dark:text-primary-400 hover:underline">conditions d'utilisation</a> et la <a href="#" class="text-primary-600 dark:text-primary-400 hover:underline">politique de confidentialité</a>
                        </label>
                    </div>

                    <div class="flex space-x-3">
                        <button type="button" id="back-btn-2"
                                class="btn-effect flex-1 bg-white dark:bg-neutral-700 hover:bg-neutral-100 dark:hover:bg-neutral-600 text-neutral-800 dark:text-neutral-200 font-medium py-3 px-4 rounded-lg transition border border-neutral-200 dark:border-neutral-600 shadow-sm hover:shadow flex items-center justify-center mt-4">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clip-rule="evenodd"></path>
                            </svg>
                            <span>Retour</span>
                        </button>
                        <button type="submit"
                                class="btn-effect flex-1 bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white font-medium py-3 px-4 rounded-lg transition shadow-md hover:shadow-lg flex items-center justify-center mt-4">
                            <span>S'inscrire</span>
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
                            </svg>
                        </button>
                    </div>
                </div>
            </form>

            <div class="mt-6 text-center">
                <p class="text-sm text-neutral-600 dark:text-neutral-400">
                    Déjà inscrit?
                    <a href="${pageContext.request.contextPath}/login.jsp" class="font-medium text-primary-600 dark:text-primary-400 hover:text-primary-500 dark:hover:text-primary-300 transition">
                        Se connecter
                    </a>
                </p>
            </div>
        </div>
    </div>
</main>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Toggle password visibility
        const togglePassword = document.getElementById('togglePassword');
        if (togglePassword) {
            togglePassword.addEventListener('click', function() {
                const passwordInput = document.getElementById('password');
                const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
                passwordInput.setAttribute('type', type);

                // Change the eye icon
                const eyeIcon = this.querySelector('svg');
                if (type === 'text') {
                    eyeIcon.innerHTML = '<path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd" /><path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z" />';
                } else {
                    eyeIcon.innerHTML = '<path d="M10 12a2 2 0 100-4 2 2 0 000 4z" /><path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd" />';
                }
            });
        }

        // Password strength meter
        const passwordInput = document.getElementById('password');
        const passwordStrength = document.getElementById('password-strength');
        const passwordFeedback = document.getElementById('password-feedback');

        if (passwordInput && passwordStrength && passwordFeedback) {
            passwordInput.addEventListener('input', function() {
                const password = this.value;
                let strength = 0;
                let feedback = '';

                if (password.length >= 8) {
                    strength += 25;
                }

                if (password.match(/[A-Z]/)) {
                    strength += 25;
                }

                if (password.match(/[0-9]/)) {
                    strength += 25;
                }

                if (password.match(/[^A-Za-z0-9]/)) {
                    strength += 25;
                }

                passwordStrength.style.width = strength + '%';

                if (strength <= 25) {
                    passwordStrength.className = 'h-full bg-red-500 transition-all duration-300';
                    feedback = 'Mot de passe très faible';
                } else if (strength <= 50) {
                    passwordStrength.className = 'h-full bg-orange-500 transition-all duration-300';
                    feedback = 'Mot de passe faible';
                } else if (strength <= 75) {
                    passwordStrength.className = 'h-full bg-yellow-500 transition-all duration-300';
                    feedback = 'Mot de passe moyen';
                } else {
                    passwordStrength.className = 'h-full bg-green-500 transition-all duration-300';
                    feedback = 'Mot de passe fort';
                }

                passwordFeedback.textContent = feedback;
            });
        }

        // Form navigation
        const step1 = document.getElementById('step1');
        const step2 = document.getElementById('step2');
        const step3 = document.getElementById('step3');
        const continueBtn = document.getElementById('continue-btn');
        const continueBtn2 = document.getElementById('continue-btn-2');
        const backBtn1 = document.getElementById('back-btn-1');
        const backBtn2 = document.getElementById('back-btn-2');
        const progressSteps = document.querySelectorAll('.progress-step');
        const progressLines = document.querySelectorAll('.progress-line');

        if (continueBtn) {
            continueBtn.addEventListener('click', function() {
                // Validate first step
                const username = document.getElementById('username').value;
                const email = document.getElementById('email').value;
                const password = document.getElementById('password').value;

                if (!username || !email || !password) {
                    // Shake animation for validation error
                    step1.classList.add('shake');
                    setTimeout(() => {
                        step1.classList.remove('shake');
                    }, 600);
                    return;
                }

                // Move to step 2
                step1.classList.add('hidden');
                step2.classList.remove('hidden');

                // Update progress
                progressSteps[0].classList.add('active');
                progressSteps[1].classList.add('active');
                progressLines[0].style.width = '100%';
            });
        }

        if (continueBtn2) {
            continueBtn2.addEventListener('click', function() {
                // Move to step 3
                step2.classList.add('hidden');
                step3.classList.remove('hidden');

                // Update progress
                progressSteps[2].classList.add('active');
                progressLines[1].style.width = '100%';
            });
        }

        if (backBtn1) {
            backBtn1.addEventListener('click', function() {
                // Back to step 1
                step2.classList.add('hidden');
                step1.classList.remove('hidden');

                // Update progress
                progressSteps[1].classList.remove('active');
                progressLines[0].style.width = '0%';
            });
        }

        if (backBtn2) {
            backBtn2.addEventListener('click', function() {
                // Back to step 2
                step3.classList.add('hidden');
                step2.classList.remove('hidden');

                // Update progress
                progressSteps[2].classList.remove('active');
                progressLines[1].style.width = '0%';
            });
        }

        // Disparition automatique du message flash
        setTimeout(() => {
            const flash = document.querySelector('.flash-message');
            if (flash) {
                flash.classList.add('flash-out');
                setTimeout(() => flash.remove(), 500);
            }
        }, 4000);
    });
</script>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>