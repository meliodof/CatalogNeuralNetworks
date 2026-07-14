(function() {
    let neuronetData = [];
    let input, form, suggestionEl;

    function initData() {
        const dataEl = document.getElementById('neuronet-data');
        if (dataEl) {
            try {
                neuronetData = JSON.parse(dataEl.textContent);
            } catch(e) {
                neuronetData = [];
            }
        }
    }

    function createSuggestionElement() {
        const el = document.createElement('span');
        el.className = 'search-inline-suggestion';
        el.setAttribute('aria-hidden', 'true');
        return el;
    }

    function getBestMatch(val) {
        if (!val) return null;
        const lowerVal = val.toLowerCase();
        const matches = neuronetData.filter(item =>
            item.name.toLowerCase().startsWith(lowerVal) && item.name.length > val.length
        );
        if (matches.length === 0) return null;
        matches.sort((a, b) => b.reviews - a.reviews);
        return matches[0].name;
    }

    function showSuggestion() {
        if (!input || !suggestionEl) return;
        const val = input.value;
        const match = getBestMatch(val);
        if (match) {
            // Показываем только несовпадающую часть
            suggestionEl.textContent = match.substring(val.length);
            suggestionEl.style.display = 'block';
        } else {
            suggestionEl.style.display = 'none';
        }
    }

    function acceptSuggestion() {
        if (!input || !suggestionEl || suggestionEl.style.display === 'none') return;
        input.value = input.value + suggestionEl.textContent;
        suggestionEl.style.display = 'none';
        htmx.trigger(form, 'submit');
    }

    function attachEvents() {
        if (!input) return;

        input.addEventListener('input', showSuggestion);

        input.addEventListener('keydown', function(e) {
            if (e.key === 'Tab' || e.key === 'ArrowRight' || e.key === 'Enter') {
                if (suggestionEl && suggestionEl.style.display !== 'none') {
                    e.preventDefault();
                    acceptSuggestion();
                }
            }
        });

        input.addEventListener('blur', function() {
            setTimeout(function() {
                if (suggestionEl) suggestionEl.style.display = 'none';
            }, 150);
        });

        input.addEventListener('focus', function() {
            showSuggestion();
        });

        document.body.addEventListener('click', function(e) {
            if (e.target.closest('#search-clear')) {
                if (input) {
                    input.value = '';
                    if (suggestionEl) suggestionEl.style.display = 'none';
                    htmx.trigger(form, 'submit');
                }
            }
        });
    }

    function setupUI() {
        input = document.getElementById('search-input');
        form = document.getElementById('search-form');
        if (!input) return;

        if (!document.querySelector('.search-input-wrapper')) {
            const wrapper = document.createElement('div');
            wrapper.className = 'search-input-wrapper';
            input.parentNode.insertBefore(wrapper, input);
            wrapper.appendChild(input);

            suggestionEl = createSuggestionElement();
            wrapper.appendChild(suggestionEl);
        } else {
            suggestionEl = document.querySelector('.search-inline-suggestion');
        }
    }

    function init() {
        initData();
        setupUI();
        attachEvents();
        showSuggestion();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    document.body.addEventListener('htmx:afterSwap', function() {
        init();
    });
})();