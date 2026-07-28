
(function() {
    // При загрузке проверяем сохранённую тему
    var savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    }

    // Функция переключения (должна быть глобальной)
    window.toggleTheme = function() {
        document.body.classList.toggle('light-theme');
        var isLight = document.body.classList.contains('light-theme');
        localStorage.setItem('theme', isLight ? 'light' : 'dark');
    };
})();