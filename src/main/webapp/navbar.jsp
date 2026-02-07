<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String uri = request.getRequestURI();
    boolean isLoginPage = uri.contains("login.jsp");
    boolean isRegisterPage = uri.contains("register.jsp");
    boolean isEditor = uri.contains("EditorServlet");
    boolean isHome = uri.contains("IndexServlet") || uri.endsWith("/");
    boolean isProfile = uri.contains("ProfileServlet");
%>

<!-- Navbar -->
<nav class="bg-white dark:bg-neutral-900 border-b border-neutral-200 dark:border-neutral-700 shadow-sm transition-colors duration-300">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
            <!-- Logo -->
            <div class="flex-shrink-0 flex items-center">
                <a href="${pageContext.request.contextPath}/IndexServlet" class="flex items-center">
                    <div class="bg-gradient-to-r from-primary-600 to-primary-400 dark:from-primary-500 dark:to-primary-300 text-white p-2 rounded-lg mr-2">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                        </svg>
                    </div>
                    <span class="text-xl font-bold text-neutral-900 dark:text-white">IDE Collaboratif</span>
                </a>
            </div>

            <!-- Navigation Links - Desktop -->
            <div class="hidden md:ml-6 md:flex md:items-center md:space-x-4">
                <a href="${pageContext.request.contextPath}/IndexServlet"
                   class="relative px-3 py-2 rounded-md text-sm font-medium <%= isHome ? "text-primary-600 dark:text-primary-400" : "text-neutral-700 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400" %> transition-colors duration-200">
                    <span>Accueil</span>
                    <% if (isHome) { %>
                    <span class="absolute bottom-0 left-0 w-full h-0.5 bg-primary-600 dark:bg-primary-400 rounded-full"></span>
                    <% } %>
                </a>

                <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true"
                   class="relative px-3 py-2 rounded-md text-sm font-medium <%= isEditor ? "text-primary-600 dark:text-primary-400" : "text-neutral-700 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400" %> transition-colors duration-200">
                    <span>Éditeur</span>
                    <% if (isEditor) { %>
                    <span class="absolute bottom-0 left-0 w-full h-0.5 bg-primary-600 dark:bg-primary-400 rounded-full"></span>
                    <% } %>
                </a>

                <a href="${pageContext.request.contextPath}/ProfileServlet"
                   class="relative px-3 py-2 rounded-md text-sm font-medium <%= isProfile ? "text-primary-600 dark:text-primary-400" : "text-neutral-700 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400" %> transition-colors duration-200">
                    <span>Mon Profil</span>
                    <% if (isProfile) { %>
                    <span class="absolute bottom-0 left-0 w-full h-0.5 bg-primary-600 dark:bg-primary-400 rounded-full"></span>
                    <% } %>
                </a>
            </div>

            <!-- Right side buttons/links -->
            <div class="hidden md:flex md:items-center">
                <%
                    if (isLoginPage) {
                %>
                <a href="${pageContext.request.contextPath}/register.jsp"
                   class="ml-4 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 dark:focus:ring-offset-neutral-900 transition-colors duration-200">
                    Inscription
                </a>
                <%
                } else if (isRegisterPage) {
                %>
                <a href="${pageContext.request.contextPath}/login.jsp"
                   class="ml-4 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 dark:bg-primary-700 dark:hover:bg-primary-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 dark:focus:ring-offset-neutral-900 transition-colors duration-200">
                    Connexion
                </a>
                <%
                } else {
                %>
                <div class="relative ml-4 group">
                    <button class="flex items-center text-sm font-medium text-neutral-700 dark:text-neutral-300 hover:text-primary-600 dark:hover:text-primary-400 focus:outline-none">
                        <span class="sr-only">Menu utilisateur</span>
                        <div class="h-8 w-8 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center text-primary-600 dark:text-primary-400">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                            </svg>
                        </div>
                        <svg xmlns="http://www.w3.org/2000/svg" class="ml-1 h-5 w-5 text-neutral-400 group-hover:text-neutral-500 dark:text-neutral-500 dark:group-hover:text-neutral-400" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
                        </svg>
                    </button>

                    <!-- Dropdown menu -->
                    <div class="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-white dark:bg-neutral-800 ring-1 ring-black ring-opacity-5 focus:outline-none hidden group-hover:block z-50 transition-all duration-200">
                        <a href="${pageContext.request.contextPath}/ProfileServlet"
                           class="block px-4 py-2 text-sm text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-700">
                            Mon profil
                        </a>
                        <a href="${pageContext.request.contextPath}/SettingsServlet"
                           class="block px-4 py-2 text-sm text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-700">
                            Paramètres
                        </a>
                        <div class="border-t border-neutral-200 dark:border-neutral-700"></div>
                        <a href="${pageContext.request.contextPath}/LogoutServlet"
                           class="block px-4 py-2 text-sm text-red-500 hover:bg-neutral-100 dark:hover:bg-neutral-700">
                            Déconnexion
                        </a>
                    </div>
                </div>
                <%
                    }
                %>

                <!-- Dark Mode Toggle Button - Desktop -->
                <button id="darkModeToggle-desktop" class="ml-4 p-2 rounded-full text-neutral-600 dark:text-neutral-400 hover:text-primary-600 dark:hover:text-primary-400 focus:outline-none">
                    <!-- Sun icon (light mode) -->
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 dark:hidden" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clip-rule="evenodd" />
                    </svg>
                    <!-- Moon icon (dark mode) -->
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 hidden dark:block" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
                    </svg>
                </button>
            </div>

            <!-- Mobile menu button -->
            <div class="flex items-center md:hidden">
                <!-- Dark Mode Toggle Button - Mobile -->
                <button id="darkModeToggle-mobile" class="p-2 rounded-full text-neutral-600 dark:text-neutral-400 hover:text-primary-600 dark:hover:text-primary-400 focus:outline-none mr-2">
                    <!-- Sun icon (light mode) -->
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 dark:hidden" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clip-rule="evenodd" />
                    </svg>
                    <!-- Moon icon (dark mode) -->
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 hidden dark:block" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
                    </svg>
                </button>

                <button id="navbar-toggle" class="p-2 rounded-md text-neutral-600 dark:text-neutral-400 hover:text-primary-600 dark:hover:text-primary-400 focus:outline-none transition-colors">
                    <span class="sr-only">Ouvrir le menu</span>
                    <svg id="menu-icon" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                    <svg id="close-icon" xmlns="http://www.w3.org/2000/svg" class="hidden h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>
        </div>
    </div>

    <!-- Mobile menu -->
    <div id="navbar-links" class="hidden md:hidden border-t border-neutral-200 dark:border-neutral-700 transition-all duration-300">
        <div class="py-3 px-4 space-y-1">
            <a href="${pageContext.request.contextPath}/IndexServlet"
               class="block px-3 py-2 rounded-md text-base font-medium <%= isHome ? "text-primary-600 dark:text-primary-400 bg-neutral-100 dark:bg-neutral-800" : "text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-800 hover:text-primary-600 dark:hover:text-primary-400" %> transition-colors">
                Accueil
            </a>
            <a href="${pageContext.request.contextPath}/EditorServlet?showSelect=true"
               class="block px-3 py-2 rounded-md text-base font-medium <%= isEditor ? "text-primary-600 dark:text-primary-400 bg-neutral-100 dark:bg-neutral-800" : "text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-800 hover:text-primary-600 dark:hover:text-primary-400" %> transition-colors">
                Éditeur
            </a>
            <a href="${pageContext.request.contextPath}/ProfileServlet"
               class="block px-3 py-2 rounded-md text-base font-medium <%= isProfile ? "text-primary-600 dark:text-primary-400 bg-neutral-100 dark:bg-neutral-800" : "text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-800 hover:text-primary-600 dark:hover:text-primary-400" %> transition-colors">
                Mon Profil
            </a>

            <%
                if (isLoginPage) {
            %>
            <a href="${pageContext.request.contextPath}/register.jsp"
               class="block px-3 py-2 rounded-md text-base font-medium text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-800 hover:text-primary-600 dark:hover:text-primary-400 transition-colors">
                Inscription
            </a>
            <%
            } else if (isRegisterPage) {
            %>
            <a href="${pageContext.request.contextPath}/login.jsp"
               class="block px-3 py-2 rounded-md text-base font-medium text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-800 hover:text-primary-600 dark:hover:text-primary-400 transition-colors">
                Connexion
            </a>
            <%
            } else {
            %>
            <div class="border-t border-neutral-200 dark:border-neutral-700 pt-2 mt-2">
                <a href="${pageContext.request.contextPath}/SettingsServlet"
                   class="block px-3 py-2 rounded-md text-base font-medium text-neutral-700 dark:text-neutral-300 hover:bg-neutral-100 dark:hover:bg-neutral-800 hover:text-primary-600 dark:hover:text-primary-400 transition-colors">
                    Paramètres
                </a>
                <a href="${pageContext.request.contextPath}/LogoutServlet"
                   class="block px-3 py-2 rounded-md text-base font-medium text-red-500 hover:bg-neutral-100 dark:hover:bg-neutral-800 transition-colors">
                    Déconnexion
                </a>
            </div>
            <%
                }
            %>
        </div>
    </div>
</nav>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        // Mobile menu toggle
        const toggleBtn = document.getElementById('navbar-toggle');
        const navLinks = document.getElementById('navbar-links');
        const menuIcon = document.getElementById('menu-icon');
        const closeIcon = document.getElementById('close-icon');

        toggleBtn.addEventListener('click', () => {
            navLinks.classList.toggle('hidden');
            menuIcon.classList.toggle('hidden');
            closeIcon.classList.toggle('hidden');
        });

        // Dark mode toggle functionality
        const darkModeToggleDesktop = document.getElementById('darkModeToggle-desktop');
        const darkModeToggleMobile = document.getElementById('darkModeToggle-mobile');

        // Function to toggle dark mode
        function toggleDarkMode() {
            if (document.documentElement.classList.contains('dark')) {
                document.documentElement.classList.remove('dark');
                localStorage.setItem('darkMode', 'false');
            } else {
                document.documentElement.classList.add('dark');
                localStorage.setItem('darkMode', 'true');
            }
        }

        // Check for saved theme preference or use the system preference
        if (localStorage.getItem('darkMode') === 'true' ||
            (!localStorage.getItem('darkMode') && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
            document.documentElement.classList.add('dark');
        }

        // Add event listeners for dark mode toggles
        if (darkModeToggleDesktop) {
            darkModeToggleDesktop.addEventListener('click', toggleDarkMode);
        }

        if (darkModeToggleMobile) {
            darkModeToggleMobile.addEventListener('click', toggleDarkMode);
        }
    });
</script>