function applyThemeClass(isLight) {
    document.documentElement.classList.toggle('light-theme', isLight);
    if (document.body) document.body.classList.toggle('light-theme', isLight);
}

// При загрузке проверяем сохранённую тему
var savedTheme = localStorage.getItem('theme');
applyThemeClass(savedTheme === 'light');

// При загрузке проверяем согласие на cookies
if (localStorage.getItem('cookiesAccepted') === 'true') {
    var banner = document.getElementById('cookie-banner');
    if (banner) banner.classList.add('hidden');
}

// Функция переключения (глобальная)
function toggleTheme() {
    var isLight = !document.documentElement.classList.contains('light-theme');
    applyThemeClass(isLight);
    localStorage.setItem('theme', isLight ? 'light' : 'dark');
    
    // Оповещаем об изменении темы
    var event = new CustomEvent('themeChanged', { detail: { isLight: isLight } });
    window.dispatchEvent(event);
}

// Принятие cookies (глобальная функция)
function acceptCookies() {
    localStorage.setItem('cookiesAccepted', 'true');
    var banner = document.getElementById('cookie-banner');
    if (banner) banner.classList.add('hidden');
}
