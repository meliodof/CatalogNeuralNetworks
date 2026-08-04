
(function() {
    // При загрузке проверяем сохранённую тему
    var savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    }

    // При загрузке проверяем согласие на cookies
    if (localStorage.getItem('cookiesAccepted') === 'true') {
        document.getElementById('cookie-banner').classList.add('hidden');
    }

    // Функция переключения (должна быть глобальной)
    window.toggleTheme = function() {
        document.body.classList.toggle('light-theme');
        var isLight = document.body.classList.contains('light-theme');
        localStorage.setItem('theme', isLight ? 'light' : 'dark');
    };

    // Принятие cookies (глобальная функция)
    window.acceptCookies = function() {
        localStorage.setItem('cookiesAccepted', 'true');
        document.getElementById('cookie-banner').classList.add('hidden');
    };
})();