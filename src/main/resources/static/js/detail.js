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

        // Линии
        ctx.strokeStyle = 'rgba(88, 166, 255, 0.5)';
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
            ctx.fillStyle = 'rgba(255, 255, 255, 1)';
            ctx.shadowColor = 'rgba(88, 166, 255, 1)';
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
            ctx.fillStyle = 'rgba(88, 166, 255, ' + alpha + ')';
            ctx.shadowColor = 'rgba(88, 166, 255, ' + alpha + ')';
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