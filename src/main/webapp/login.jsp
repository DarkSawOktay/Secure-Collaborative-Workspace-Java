<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="fr" class="light">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Connexion - IDE Collaboratif</title>
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
  </style>
</head>
<body class="bg-neutral-50 dark:bg-neutral-900 text-neutral-800 dark:text-neutral-200 min-h-screen flex flex-col transition-colors duration-300">

<%@include file="navbar.jsp"%>

<!-- Main Content -->
<main class="flex-grow flex items-center justify-center py-12 px-4">
  <div class="card-animation w-full max-w-md">
    <!-- Card Header -->
    <div class="gradient-bg p-6 rounded-t-xl shadow-soft relative overflow-hidden">
      <h1 class="text-2xl md:text-3xl font-semibold text-neutral-900 dark:text-white">Bienvenue</h1>
      <p class="text-neutral-700 dark:text-neutral-300 mt-2">Connectez-vous pour accéder à votre espace</p>
    </div>

    <!-- Card Body -->
    <div class="bg-white dark:bg-neutral-800 p-6 rounded-b-xl shadow-soft border border-neutral-100 dark:border-neutral-700">
      <%@ include file="includes/flash.jsp" %>

      <form action="LoginServlet" method="post" class="space-y-5">
        <div>
          <label for="username" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Nom d'utilisateur</label>
          <input type="text" id="username" name="username" required
                 class="form-input bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400">
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">Mot de passe</label>
          <div class="relative">
            <input type="password" id="password" name="password" required
                   class="form-input bg-neutral-50 dark:bg-neutral-700 border-neutral-200 dark:border-neutral-600 text-neutral-800 dark:text-neutral-200 dark:placeholder-neutral-400 focus:border-primary-500 dark:focus:border-primary-400">
            <button type="button" id="togglePassword" class="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-500 dark:text-neutral-400">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"></path>
              </svg>
            </button>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <input id="remember-me" name="remember-me" type="checkbox"
                   class="h-4 w-4 text-primary-600 dark:text-primary-500 border-neutral-300 dark:border-neutral-600 rounded focus:ring-primary-500 dark:focus:ring-primary-400">
            <label for="remember-me" class="ml-2 block text-sm text-neutral-700 dark:text-neutral-300">
              Se souvenir de moi
            </label>
          </div>
          <a href="#" class="text-sm font-medium text-primary-600 dark:text-primary-400 hover:text-primary-500 dark:hover:text-primary-300 transition">
            Mot de passe oublié?
          </a>
        </div>

        <button type="submit"
                class="btn-effect w-full bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 text-white font-medium py-3 px-4 rounded-lg transition shadow-md hover:shadow-lg flex items-center justify-center">
          <span>Se connecter</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd"></path>
          </svg>
        </button>
      </form>

      <div class="mt-6 text-center">
        <p class="text-sm text-neutral-600 dark:text-neutral-400">
          Pas encore inscrit?
          <a href="${pageContext.request.contextPath}/register.jsp" class="font-medium text-primary-600 dark:text-primary-400 hover:text-primary-500 dark:hover:text-primary-300 transition">
            Créer un compte
          </a>
        </p>
      </div>
    </div>
  </div>
</main>

<script>
  // Toggle Dark Mode
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

    // Toggle password visibility
    document.getElementById('togglePassword').addEventListener('click', function() {
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

</body>
</html>