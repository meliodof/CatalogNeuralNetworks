(function() {
    var canvas = document.createElement('canvas');
    canvas.id = 'neuro-network-bg';
    canvas.style.cssText = 'position:fixed; top:0; left:0; width:100%; height:100%; z-index:0; pointer-events:none;';
    document.body.prepend(canvas);

    var ctx = canvas.getContext('2d');
    var nodes = [];
    var signals = [];
    var nodeCount = 100;
    var time = 0;

    // Проверка темы
    function isLightTheme() {
        return document.body.classList.contains('light-theme');
    }

    // Цвета для тёмной темы
    var darkColors = {
        line: 'rgba(88, 166, 255, 0.5)',
        signalFill: 'rgba(255, 255, 255, 1)',
        signalShadow: 'rgba(88, 166, 255, 1)',
        nodeFill: function(alpha) { return 'rgba(88, 166, 255, ' + alpha + ')'; },
        nodeShadow: function(alpha) { return 'rgba(88, 166, 255, ' + alpha + ')'; }
    };

    // Цвета для светлой темы
    var lightColors = {
        line: 'rgba(30, 30, 35, 0.4)',
        signalFill: 'rgba(30, 30, 35, 0.8)',
        signalShadow: 'rgba(30, 30, 35, 0.6)',
        nodeFill: function(alpha) { return 'rgba(30, 30, 35, ' + alpha + ')'; },
        nodeShadow: function(alpha) { return 'rgba(30, 30, 35, ' + alpha + ')'; }
    };

    function getColors() {
        return isLightTheme() ? lightColors : darkColors;
    }

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        generateNodes();
    }

    function generateNodes() {
        nodes = [];
        signals = [];
        for (var i = 0; i < nodeCount; i++) {
            nodes.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                radius: Math.random() * 3 + 2,
                phase: Math.random() * Math.PI * 2
            });
        }
    }

    function draw() {
        time += 0.01;
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        var colors = getColors();

        // Линии
        ctx.strokeStyle = colors.line;
        ctx.lineWidth = 1;
        for (var i = 0; i < nodes.length; i++) {
            for (var j = i + 1; j < nodes.length; j++) {
                var dx = nodes[i].x - nodes[j].x;
                var dy = nodes[i].y - nodes[j].y;
                var dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 250) {
                    ctx.beginPath();
                    ctx.moveTo(nodes[i].x, nodes[i].y);
                    ctx.lineTo(nodes[j].x, nodes[j].y);
                    ctx.stroke();
                }
            }
        }

        // Импульсы
        if (Math.random() < 0.99) {
            var a = nodes[Math.floor(Math.random() * nodes.length)];
            var b = nodes[Math.floor(Math.random() * nodes.length)];
            if (a !== b) {
                var d = Math.sqrt((a.x - b.x) ** 2 + (a.y - b.y) ** 2);
                if (d < 250) {
                    signals.push({ from: a, to: b, progress: 0, speed: 0.005 + Math.random() * 0.01 });
                }
            }
        }

        for (var k = signals.length - 1; k >= 0; k--) {
            var sig = signals[k];
            sig.progress += sig.speed;
            if (sig.progress >= 1) { signals.splice(k, 1); continue; }
            var x = sig.from.x + (sig.to.x - sig.from.x) * sig.progress;
            var y = sig.from.y + (sig.to.y - sig.from.y) * sig.progress;
            ctx.beginPath();
            ctx.arc(x, y, 3, 0, Math.PI * 2);
            ctx.fillStyle = colors.signalFill;
            ctx.shadowColor = colors.signalShadow;
            ctx.shadowBlur = 12;
            ctx.fill();
            ctx.shadowBlur = 0;
        }

        // Точки
        for (var i = 0; i < nodes.length; i++) {
            var pulse = 1 + Math.sin(time * 2 + nodes[i].phase) * 0.5;
            var r = nodes[i].radius * pulse;
            var alpha = 0.6 + Math.sin(time * 2 + nodes[i].phase) * 0.4;
            ctx.beginPath();
            ctx.arc(nodes[i].x, nodes[i].y, r, 0, Math.PI * 2);
            ctx.fillStyle = colors.nodeFill(alpha);
            ctx.shadowColor = colors.nodeShadow(alpha);
            ctx.shadowBlur = 12;
            ctx.fill();
            ctx.shadowBlur = 0;
        }

        requestAnimationFrame(draw);
    }

    window.addEventListener('resize', resize);
    resize();
    draw();
})();

// --- Остальные функции ---

document.body.addEventListener('htmx:afterRequest', function(evt) {
    if (evt.detail.target.id === 'review-form' && evt.detail.successful) {
        var stars = document.querySelectorAll('#star-input input[type="radio"]');
        stars.forEach(function(star) {
            star.checked = false;
        });
        document.getElementById('comment').value = '';
        sessionStorage.removeItem('savedRating');
    }
});

function saveRating(value) {
    sessionStorage.setItem('savedRating', value);
}

document.addEventListener('DOMContentLoaded', function() {
    var saved = sessionStorage.getItem('savedRating');
    if (saved) {
        var star = document.getElementById('star' + saved);
        if (star) {
            star.checked = true;
        }
    }
});

document.body.addEventListener('htmx:afterSwap', function() {
    var saved = sessionStorage.getItem('savedRating');
    if (saved) {
        var star = document.getElementById('star' + saved);
        if (star) {
            star.checked = true;
        }
    }
});

function shakeLock() {
    var overlay = document.getElementById('form-lock-overlay');
    if (overlay) {
        overlay.classList.add('shake');
        setTimeout(function() {
            overlay.classList.remove('shake');
        }, 500);
    }
}