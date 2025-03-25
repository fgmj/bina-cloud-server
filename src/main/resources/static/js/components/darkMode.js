// Dark Mode Toggle
document.addEventListener('DOMContentLoaded', function () {
    const themeToggle = document.getElementById('themeToggle');
    const darkModeSwitch = document.getElementById('darkModeSwitch');
    const html = document.documentElement;

    // Carregar preferência do usuário
    const userTheme = localStorage.getItem('theme') || 'light';
    html.setAttribute('data-bs-theme', userTheme);
    themeToggle.checked = userTheme === 'dark';
    darkModeSwitch.checked = userTheme === 'dark';

    // Alternar tema
    function toggleTheme() {
        const currentTheme = html.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';

        html.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('theme', newTheme);
    }

    // Event listeners para os toggles
    themeToggle.addEventListener('change', toggleTheme);
    darkModeSwitch.addEventListener('change', toggleTheme);

    // Sidebar Toggle
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');

    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function () {
            sidebar.classList.toggle('show');
        });

        // Fechar sidebar ao clicar fora em telas menores
        document.addEventListener('click', function (event) {
            if (window.innerWidth <= 768) {
                const isClickInside = sidebar.contains(event.target) || sidebarToggle.contains(event.target);
                if (!isClickInside && sidebar.classList.contains('show')) {
                    sidebar.classList.remove('show');
                }
            }
        });
    }

    // Atualizar hora atual
    function updateCurrentTime() {
        const now = new Date();
        const timeString = now.toLocaleTimeString('pt-BR');
        const dateString = now.toLocaleDateString('pt-BR');
        const currentTimeElement = document.getElementById('currentTime');
        if (currentTimeElement) {
            currentTimeElement.textContent = `${dateString} ${timeString}`;
        }
    }

    // Atualizar hora a cada segundo
    updateCurrentTime();
    setInterval(updateCurrentTime, 1000);
}); 