
// При загрузке проверяем сохранённую тему
var savedTheme = localStorage.getItem('theme');
if (savedTheme === 'light') {
    document.body.classList.add('light-theme');
}

// При загрузке проверяем согласие на cookies
if (localStorage.getItem('cookiesAccepted') === 'true') {
    var banner = document.getElementById('cookie-banner');
    if (banner) banner.classList.add('hidden');
}

// Функция переключения (глобальная)
function toggleTheme() {
    document.body.classList.toggle('light-theme');
    var isLight = document.body.classList.contains('light-theme');
    localStorage.setItem('theme', isLight ? 'light' : 'dark');
}

// Принятие cookies (глобальная функция)
function acceptCookies() {
    localStorage.setItem('cookiesAccepted', 'true');
    var banner = document.getElementById('cookie-banner');
    if (banner) banner.classList.add('hidden');
}
