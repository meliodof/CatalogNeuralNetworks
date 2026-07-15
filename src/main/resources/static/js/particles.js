(function() {
    var canvas = document.createElement('canvas');
    canvas.id = 'particles-canvas';
    canvas.style.cssText = 'position:fixed; top:0; left:0; width:100%; height:100%; z-index:0; pointer-events:all;';
    document.body.prepend(canvas);

    var ctx = canvas.getContext('2d');
    var particles = [];
    var particleCount = 70;
    var clusterRadius = 45;
    var score = 0;
    var multiplier = 1;
    var lastClickTime = 0;
    var comboWindow = 1500;
    var gameStarted = false;
    var hideTimer = null;

    // Счётчик
    var scoreEl = document.createElement('div');
    scoreEl.id = 'game-score';
    scoreEl.style.cssText = 'position:fixed; top:80px; left:20px; z-index:10; color:#e6edf3; font-family:Inter,sans-serif; font-size:14px; font-weight:500; background:rgba(24,31,38,0.9); padding:8px 14px; border-radius:8px; border:1px solid #252d36; pointer-events:none; user-select:none; transition:opacity 0.3s; opacity:0;';
    scoreEl.innerHTML = 'Очки: 0';
    document.body.appendChild(scoreEl);

    function showScore() {
        gameStarted = true;
        scoreEl.style.opacity = '1';
        resetHideTimer();
    }

    function hideScore() {
        scoreEl.style.opacity = '0';
        gameStarted = false;
    }

    function resetHideTimer() {
        if (hideTimer) clearTimeout(hideTimer);
        hideTimer = setTimeout(hideScore, 5000);
    }

    function updateScore(points) {
        if (!gameStarted) showScore();
        score += points;
        scoreEl.innerHTML = 'Очки: ' + score;
        scoreEl.style.transform = 'scale(1.15)';
        setTimeout(function() { scoreEl.style.transform = 'scale(1)'; }, 100);
        resetHideTimer();
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

    // Притяжение между частицами (для образования кластеров)
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

    function getCluster(particleIndex) {
        var cluster = [particleIndex];
        var p = particles[particleIndex];
        for (var i = 0; i < particles.length; i++) {
            if (i === particleIndex) continue;
            var q = particles[i];
            var dx = p.x - q.x;
            var dy = p.y - q.y;
            var dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < clusterRadius) {
                cluster.push(i);
            }
        }
        return cluster;
    }

    canvas.addEventListener('click', function(e) {
        var rect = canvas.getBoundingClientRect();
        var clickX = e.clientX - rect.left;
        var clickY = e.clientY - rect.top;

        var now = Date.now();
        if (now - lastClickTime < comboWindow) {
            multiplier += 0.5;
        } else {
            multiplier = 1;
        }
        lastClickTime = now;

        var hitIndex = -1;
        for (var i = particles.length - 1; i >= 0; i--) {
            var p = particles[i];
            var dx = clickX - p.x;
            var dy = clickY - p.y;
            var dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < p.radius + 6) {
                hitIndex = i;
                break;
            }
        }

        if (hitIndex !== -1) {
            var clusterIndices = getCluster(hitIndex);
            var count = clusterIndices.length;
            if (count > 0) {
                var points = Math.floor(count * multiplier);
                updateScore(points);
                clusterIndices.sort(function(a,b){ return b - a; });
                for (var k = 0; k < clusterIndices.length; k++) {
                    particles.splice(clusterIndices[k], 1);
                }
                for (var n = 0; n < count; n++) {
                    particles.push(createParticle());
                }
            }
        }
    });

    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Убрано отталкивание от мыши (pushFromMouse)
        attractBetweenParticles();

        for (var i = 0; i < particles.length; i++) {
            var p = particles[i];
            p.x += p.vx;
            p.y += p.vy;
            if (p.x < 0 || p.x > canvas.width) p.vx = -p.vx;
            if (p.y < 0 || p.y > canvas.height) p.vy = -p.vy;
            p.vx *= 0.999;
            p.vy *= 0.999;
        }

        for (var i = 0; i < particles.length; i++) {
            var p = particles[i];
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(88, 166, 255, 0.7)';
            ctx.shadowColor = 'rgba(88, 166, 255, 0.5)';
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

    document.addEventListener('mousemove', function(e) {

    });

    resize();
    createParticles();
    draw();
})();