(function() {
    var canvas = document.createElement('canvas');
    canvas.id = 'particles-canvas';
    canvas.style.cssText = 'position:fixed; top:0; left:0; width:100%; height:100%; z-index:0; pointer-events:none;';
    document.body.prepend(canvas);

    var ctx = canvas.getContext('2d');
    var particles = [];
    var particleCount = 100;

    // Функция определения текущей темы
    function isLightTheme() {
        return document.body.classList.contains('light-theme');
    }

    // Получить цвет точки в зависимости от темы
    function getParticleColor(alpha) {
        if (isLightTheme()) {
            return 'rgba(30, 30, 35, ' + alpha + ')';
        } else {
            return 'rgba(88, 166, 255, ' + alpha + ')';
        }
    }

    // Получить цвет линии
    function getLineColor(opacity) {
        if (isLightTheme()) {
            return 'rgba(30, 30, 35, ' + opacity + ')';
        } else {
            return 'rgba(88, 166, 255, ' + opacity + ')';
        }
    }

    // Получить цвет свечения
    function getShadowColor(alpha) {
        if (isLightTheme()) {
            return 'rgba(30, 30, 35, ' + alpha + ')';
        } else {
            return 'rgba(88, 166, 255, ' + alpha + ')';
        }
    }

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }

    function createParticle(x, y) {
        return {
            x: x || Math.random() * canvas.width,
            y: y || Math.random() * canvas.height,
            vx: (Math.random() - 0.5) * 0.3,
            vy: (Math.random() - 0.5) * 0.3,
            radius: Math.random() * 3 + 2
        };
    }

    function createParticles() {
        particles = [];
        for (var i = 0; i < particleCount; i++) {
            particles.push(createParticle());
        }
    }

    function attractBetweenParticles() {
        for (var i = 0; i < particles.length; i++) {
            for (var j = i + 1; j < particles.length; j++) {
                var a = particles[i];
                var b = particles[j];
                var dx = a.x - b.x;
                var dy = a.y - b.y;
                var dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 120 && dist > 1) {
                    var force = 0.002 * (1 - dist / 120);
                    a.vx -= dx / dist * force;
                    a.vy -= dy / dist * force;
                    b.vx += dx / dist * force;
                    b.vy += dy / dist * force;
                }
            }
        }
    }

    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        attractBetweenParticles();

        // Линии
        for (var i = 0; i < particles.length; i++) {
            for (var j = i + 1; j < particles.length; j++) {
                var a = particles[i];
                var b = particles[j];
                var dx = a.x - b.x;
                var dy = a.y - b.y;
                var dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 120) {
                    var opacity = (1 - dist / 120) * 0.15;
                    ctx.beginPath();
                    ctx.moveTo(a.x, a.y);
                    ctx.lineTo(b.x, b.y);
                    ctx.strokeStyle = getLineColor(opacity);
                    ctx.lineWidth = 0.5;
                    ctx.stroke();
                }
            }
        }

        // Частицы
        for (var i = 0; i < particles.length; i++) {
            var p = particles[i];
            p.x += p.vx;
            p.y += p.vy;
            if (p.x < 0 || p.x > canvas.width) p.vx = -p.vx;
            if (p.y < 0 || p.y > canvas.height) p.vy = -p.vy;
            p.vx *= 0.999;
            p.vy *= 0.999;

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            ctx.fillStyle = getParticleColor(0.7);
            ctx.shadowColor = getShadowColor(0.5);
            ctx.shadowBlur = 4;
            ctx.fill();
            ctx.shadowBlur = 0;
        }

        requestAnimationFrame(draw);
    }

    window.addEventListener('resize', function() {
        resize();
        createParticles();
    });

    resize();
    createParticles();
    draw();
})();