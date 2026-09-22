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
    var syncing = false;

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
        syncCheckboxes();
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

    function toggle(idRaw, nameRaw, forceChecked) {
        var id = Number(idRaw);
        if (!Number.isFinite(id)) return;

        var ids = getIds();
        var idx = ids.indexOf(id);
        var shouldSelect = typeof forceChecked === 'boolean' ? forceChecked : idx < 0;

        if (shouldSelect) {
            if (idx >= 0) {
                syncCheckboxes();
                return;
            }
            if (ids.length >= MAX_ITEMS) {
                alert('Можно сравнить не более ' + MAX_ITEMS + ' нейросетей одновременно. Сначала уберите одну из списка.');
                syncCheckboxes();
                return;
            }
            ids.push(id);
            if (nameRaw) rememberName(id, nameRaw);
        } else if (idx >= 0) {
            ids.splice(idx, 1);
        }

        setIds(ids);
    }

    function remove(id) {
        var ids = getIds().filter(function (x) { return x !== Number(id); });
        setIds(ids);
    }

    function clearAll() {
        setIds([]);
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

    function markCard(cb, on) {
        var card = cb.closest('.card');
        if (card) card.classList.toggle('is-compare-selected', on);
        var wrap = cb.closest('.card__compare');
        if (wrap) wrap.classList.toggle('is-checked', on);
    }

    function syncCheckboxes() {
        var ids = getIds();
        syncing = true;
        document.querySelectorAll('.compare-checkbox').forEach(function (cb) {
            var on = ids.indexOf(Number(cb.value)) >= 0;
            if (cb.checked !== on) cb.checked = on;
            markCard(cb, on);
        });
        syncing = false;
    }

    // Меню "Сравнения": ссылка всегда ведёт на актуальный набор выбранных id,
    // а счётчик показывает, сколько нейросетей сейчас выбрано.
    function syncCompareMenu() {
        var ids = getIds();
        var url = ids.length ? '/neuronets/compare?ids=' + ids.join(',') : '/neuronets/compare';
        document.querySelectorAll('a[data-compare-link]').forEach(function (a) {
            a.setAttribute('href', url);
        });
        document.querySelectorAll('[data-compare-count]').forEach(function (el) {
            el.textContent = String(ids.length);
            el.hidden = ids.length === 0;
        });
    }

    function render() {
        syncCompareMenu();
        document.body.classList.toggle('has-compare-bar', getIds().length > 0);

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
                chip.setAttribute('data-id', String(id));

                var name = document.createElement('span');
                name.className = 'compare-bar__chip-name';
                name.textContent = nameFor(id);
                chip.appendChild(name);

                var btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'compare-bar__chip-remove';
                btn.setAttribute('aria-label', 'Убрать из сравнения');
                btn.setAttribute('data-compare-remove', String(id));
                btn.textContent = '×';

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

    function cellSignature(td) {
        return (td.innerText || '').replace(/\s+/g, ' ').trim();
    }

    function markCompareDiffs() {
        var table = document.querySelector('.compare-table');
        if (!table) return;

        table.querySelectorAll('tbody tr.compare-row').forEach(function (tr) {
            var cells = tr.querySelectorAll('td');
            if (cells.length < 2) return;
            var texts = [];
            cells.forEach(function (td) { texts.push(cellSignature(td)); });
            var same = texts.every(function (t) { return t === texts[0]; });
            tr.classList.toggle('compare-row--same', same);
            tr.classList.toggle('compare-row--diff', !same);
            cells.forEach(function (td) {
                td.classList.toggle('compare-cell--diff', !same);
            });
        });
    }

    function bindDiffsToggle() {
        var toggleEl = document.getElementById('compare-diffs-only');
        var table = document.querySelector('.compare-table');
        if (!toggleEl || !table) return;
        var apply = function () {
            table.classList.toggle('compare-table--diffs-only', toggleEl.checked);
        };
        toggleEl.addEventListener('change', apply);
        apply();
    }

    function init() {
        cacheNamesFromDom();
        render();
        syncCheckboxes();
        markCompareDiffs();
        bindDiffsToggle();
    }

    document.addEventListener('change', function (e) {
        var cb = e.target && e.target.classList && e.target.classList.contains('compare-checkbox')
            ? e.target
            : null;
        if (!cb || syncing) return;
        toggle(cb.value, cb.dataset.name, cb.checked);
    });

    document.addEventListener('click', function (e) {
        var btn = e.target.closest ? e.target.closest('[data-compare-remove]') : null;
        if (!btn) return;
        e.preventDefault();
        e.stopPropagation();
        remove(btn.getAttribute('data-compare-remove'));
    }, true);

    document.addEventListener('DOMContentLoaded', init);
    window.addEventListener('pageshow', function () {
        cacheNamesFromDom();
        render();
        syncCheckboxes();
    });

    document.body.addEventListener('htmx:afterSwap', function () {
        cacheNamesFromDom();
        syncCheckboxes();
        syncCompareMenu();
    });

    window.CompareWidget = {
        toggle: function (id, name) { toggle(id, name); },
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
