    // Показ/скрытие пароля
    function togglePassword(id) {
        var input = document.getElementById(id);
        var btn = input.parentElement.querySelector('.password-toggle');
        if (input.type === 'password') {
            input.type = 'text';
            btn.innerHTML = '<span class="eye-icon">🔒</span>';
        } else {
            input.type = 'password';
            btn.innerHTML = '<span class="eye-icon">👁</span>';
        }
    }

    // Валидация пароля в реальном времени
    var passwordInput = document.getElementById('password');
    var confirmInput = document.getElementById('confirmPassword');
    var matchHint = document.getElementById('matchHint');

    var reqLength = document.getElementById('req-length');
    var reqUpper = document.getElementById('req-uppercase');
    var reqLower = document.getElementById('req-lowercase');
    var reqDigit = document.getElementById('req-digit');
    var reqSpecial = document.getElementById('req-special');

    var strengthFill = document.getElementById('strengthFill');
    var strengthText = document.getElementById('strengthText');

    passwordInput.addEventListener('input', function() {
        var val = this.value;

        // Проверки
        var hasLength = val.length >= 8;
        var hasUpper = /[A-ZА-ЯЁ]/.test(val);
        var hasLower = /[a-zа-яё]/.test(val);
        var hasDigit = /[0-9]/.test(val);
        var hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(val);

        // Обновление подсказок
        updateRequirement(reqLength, hasLength);
        updateRequirement(reqUpper, hasUpper);
        updateRequirement(reqLower, hasLower);
        updateRequirement(reqDigit, hasDigit);
        updateRequirement(reqSpecial, hasSpecial);

        // Шкала надёжности
        var score = 0;
        if (hasLength) score++;
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;

        var pct = (score / 5) * 100;
        strengthFill.style.width = pct + '%';

        if (score <= 2) {
            strengthFill.style.background = 'var(--red)';
            strengthText.textContent = 'Надёжность: слабая';
        } else if (score <= 4) {
            strengthFill.style.background = 'var(--star)';
            strengthText.textContent = 'Надёжность: средняя';
        } else {
            strengthFill.style.background = 'var(--green)';
            strengthText.textContent = 'Надёжность: высокая';
        }

        // Проверка совпадения паролей
        checkMatch();
    });

    confirmInput.addEventListener('input', checkMatch);

    function checkMatch() {
        if (confirmInput.value === '') {
            matchHint.style.display = 'none';
        } else if (passwordInput.value !== confirmInput.value) {
            matchHint.style.display = 'block';
        } else {
            matchHint.style.display = 'none';
        }
    }

    function updateRequirement(el, valid) {
        if (valid) {
            el.classList.add('valid');
            el.classList.remove('invalid');
        } else {
            el.classList.remove('valid');
            el.classList.add('invalid');
        }
    }

// Проверка доступности имени пользователя
var usernameTimeout = null;

function checkUsername() {
    var input = document.getElementById('username');
    var hint = document.getElementById('username-hint');
    var suggestions = document.getElementById('username-suggestions');
    var val = input.value.trim();

    // Скрываем подсказки, если пусто
    if (val.length < 3) {
        hint.style.display = 'block';
        hint.className = 'form-hint';
        hint.textContent = 'От 3 до 30 символов: буквы, цифры, _ и -';
        suggestions.style.display = 'none';
        return;
    }

    // Задержка перед запросом (debounce)
    if (usernameTimeout) clearTimeout(usernameTimeout);
    usernameTimeout = setTimeout(function() {
        fetch('/users/check-username?username=' + encodeURIComponent(val))
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.available) {
                    hint.className = 'form-hint form-hint--success';
                    hint.textContent = '✓ Имя свободно';
                    suggestions.style.display = 'none';
                } else {
                    hint.className = 'form-hint form-hint--error';
                    hint.textContent = '✗ Имя занято';

                    // Показываем варианты
                    if (data.suggestions && data.suggestions.length > 0) {
                        var html = '<span class="suggestions-title">Свободные варианты:</span><div class="suggestions-list">';
                        data.suggestions.forEach(function(s) {
                            html += '<button type="button" class="suggestion-btn" onclick="useSuggestion(\'' + s + '\')">' + s + '</button>';
                        });
                        html += '</div>';
                        suggestions.innerHTML = html;
                        suggestions.style.display = 'block';
                    }
                }
            });
    }, 500);
}

function useSuggestion(name) {
    document.getElementById('username').value = name;
    document.getElementById('username-suggestions').style.display = 'none';
    checkUsername();
}

// Проверка доступности email
var emailTimeout = null;

function checkEmail() {
    var input = document.getElementById('email');
    var hint = document.getElementById('email-hint');
    var val = input.value.trim();

    if (val.length < 5 || !val.includes('@')) {
        hint.className = 'form-hint';
        hint.textContent = '';
        return;
    }

    if (emailTimeout) clearTimeout(emailTimeout);
    emailTimeout = setTimeout(function() {
        fetch('/users/check-email?email=' + encodeURIComponent(val))
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.available) {
                    hint.className = 'form-hint form-hint--success';
                    hint.textContent = '✓ Email свободен';
                } else {
                    hint.className = 'form-hint form-hint--error';
                    hint.textContent = '✗ Эта почта уже занята';
                }
            });
    }, 500);
}