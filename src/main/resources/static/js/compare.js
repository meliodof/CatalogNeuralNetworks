/**
 * Виджет "Сравнение нейросетей".
 * Выбор хранится в localStorage на клиенте (id нейросетей), т.к. это
 * сессионная UI-функция, не требующая отдельной таблицы в БД.
 * Работает поверх HTMX-свопов: плавающая панель живёт вне #main-area,
 * поэтому переживает подмену контента каталога.
 */
(function () {
    var STORAGE_KEY = 'ai-catalog:compare-ids';
    var NAMES_KEY = 'ai-catalog:compare-names';
    var MAX_ITEMS = 4;

    function readJson(key, fallback) {
        try {
            var raw = localStorage.getItem(key);
            return raw ? JSON.parse(raw) : fallback;
        } catch (e) {
            return fallback;
        }
    }

    function getIds() {
        var arr = readJson(STORAGE_KEY, []);
        return Array.isArray(arr) ? arr.map(Number).filter(Number.isFinite) : [];
    }

    function setIds(ids) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(ids));
        render();
    }

    function getNamesCache() {
        var obj = readJson(NAMES_KEY, {});
        return obj && typeof obj === 'object' ? obj : {};
    }

    function rememberName(id, name) {
        if (!name) return;
        var cache = getNamesCache();
        cache[id] = name;
        localStorage.setItem(NAMES_KEY, JSON.stringify(cache));
    }

    function toggle(idRaw, nameRaw) {
        var id = Number(idRaw);
        if (!Number.isFinite(id)) return;

        var ids = getIds();
        var idx = ids.indexOf(id);

        if (idx >= 0) {
            ids.splice(idx, 1);
        } else {
            if (ids.length >= MAX_ITEMS) {
                alert('Можно сравнить не более ' + MAX_ITEMS + ' нейросетей одновременно. Сначала уберите одну из списка.');
                syncCheckboxes();
                return;
            }
            ids.push(id);
            if (nameRaw) rememberName(id, nameRaw);
        }

        setIds(ids);
        syncCheckboxes();
    }

    function remove(id) {
        var ids = getIds().filter(function (x) { return x !== Number(id); });
        setIds(ids);
        syncCheckboxes();
    }

    function clearAll() {
        setIds([]);
        syncCheckboxes();
    }

    function nameFor(id) {
        var cb = document.querySelector('.compare-checkbox[value="' + id + '"]');
        if (cb && cb.dataset.name) {
            rememberName(id, cb.dataset.name);
            return cb.dataset.name;
        }
        var cache = getNamesCache();
        return cache[id] || ('Нейросеть #' + id);
    }

    function cacheNamesFromDom() {
        document.querySelectorAll('.compare-checkbox[data-name]').forEach(function (cb) {
            rememberName(cb.value, cb.dataset.name);
        });
    }

    function syncCheckboxes() {
        var ids = getIds();
        document.querySelectorAll('.compare-checkbox').forEach(function (cb) {
            cb.checked = ids.indexOf(Number(cb.value)) >= 0;
        });
    }

    function render() {
        var ids = getIds();
        var bar = document.getElementById('compare-bar');
        if (!bar) return;

        if (ids.length === 0) {
            bar.hidden = true;
            return;
        }

        bar.hidden = false;

        var itemsEl = document.getElementById('compare-bar__items');
        var goEl = document.getElementById('compare-bar__go');
        var countEl = document.getElementById('compare-bar__count');

        if (itemsEl) {
            itemsEl.innerHTML = '';
            ids.forEach(function (id) {
                var chip = document.createElement('span');
                chip.className = 'compare-bar__chip';
                chip.textContent = nameFor(id);

                var btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'compare-bar__chip-remove';
                btn.setAttribute('aria-label', 'Убрать из сравнения');
                btn.textContent = '×';
                btn.onclick = function () { remove(id); };

                chip.appendChild(btn);
                itemsEl.appendChild(chip);
            });
        }

        if (countEl) countEl.textContent = String(ids.length);
        if (goEl) {
            if (ids.length >= 2) {
                goEl.setAttribute('href', '/neuronets/compare?ids=' + ids.join(','));
                goEl.classList.remove('btn--disabled');
            } else {
                goEl.setAttribute('href', '#');
                goEl.classList.add('btn--disabled');
            }
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        cacheNamesFromDom();
        render();
        syncCheckboxes();
    });

    // После HTMX-подмены #main-area карточки/чекбоксы новые — синхронизируем их состояние
    document.body.addEventListener('htmx:afterSwap', function () {
        cacheNamesFromDom();
        syncCheckboxes();
    });

    window.CompareWidget = {
        toggle: toggle,
        remove: remove,
        clearAll: clearAll,
        getIds: getIds
    };

    // Используется кнопкой "убрать" прямо на странице /neuronets/compare:
    // убирает id из выбора и переходит на актуальный URL сравнения.
    window.removeFromComparePage = function (id) {
        remove(id);
        var ids = getIds();
        if (ids.length === 0) {
            window.location.href = '/';
        } else {
            window.location.href = '/neuronets/compare?ids=' + ids.join(',');
        }
    };
})();
