(function() {
    // Проверяем, находимся ли мы на странице сравнения
    if (!document.body.classList.contains('compare-page-body')) {
        return;
    }

    // Создаём canvas для рисования ромбов
    var canvas = document.createElement('canvas');
    canvas.style.cssText = 'position:fixed; top:0; left:0; width:100%; height:100%; z-index:0; pointer-events:none;';
    document.body.prepend(canvas);

    var ctx = canvas.getContext('2d');
    var width, height;
    var diamonds = [];
    var connections = [];
    var pulses = []; // Пульсации (волны) для передачи активности
    var pulsePhase = 0; // Фаза базовой пульсации

    // Функция определения текущей темы
    function isLightTheme() {
        return document.body.classList.contains('light-theme');
    }

    // Получить цвета в зависимости от темы
    function getColors() {
        if (isLightTheme()) {
            return {
                line: 'rgba(0, 0, 0, 0.4)',
                point: 'rgba(0, 0, 0, 0.6)',
                pointGlow: 'rgba(0, 0, 0, 0.2)'
            };
        } else {
            return {
                line: 'rgba(88, 166, 255, 0.4)',
                point: 'rgba(88, 166, 255, 0.7)',
                pointGlow: 'rgba(88, 166, 255, 0.3)'
            };
        }
    }

    // Функция изменения размера canvas
    function resize() {
        width = window.innerWidth;
        height = window.innerHeight;
        canvas.width = width;
        canvas.height = height;
        generateDiamonds(); // Перегенерируем при изменении размера
        draw();
    }

    // Генерация рандомных ромбов с равномерным распределением
    function generateDiamonds() {
        diamonds = [];
        
        // Количество ромбов зависит от размера экрана (уменьшено так как ромбы стали крупнее)
        var area = width * height;
        var numDiamonds = Math.floor(area / 50000); // Примерно 1 ромб на 50000 пикселей
        numDiamonds = Math.max(12, Math.min(numDiamonds, 25)); // От 12 до 25 ромбов

        // Создаём сетку для равномерного распределения
        var gridSize = Math.ceil(Math.sqrt(numDiamonds));
        var cellWidth = width / gridSize;
        var cellHeight = height / gridSize;

        for (var i = 0; i < numDiamonds; i++) {
            var size = 60 + Math.random() * 100; // Размер от 60 до 160 (крупнее)
            
            // Определяем ячейку сетки
            var gridX = i % gridSize;
            var gridY = Math.floor(i / gridSize);
            
            // Добавляем случайное смещение внутри ячейки
            var offsetX = (Math.random() - 0.5) * cellWidth * 0.6;
            var offsetY = (Math.random() - 0.5) * cellHeight * 0.6;
            
            var centerX = gridX * cellWidth + cellWidth / 2 + offsetX;
            var centerY = gridY * cellHeight + cellHeight / 2 + offsetY;
            var rotation = Math.random() * Math.PI * 2; // Случайный поворот

            // Вычисляем углы ромба
            var halfSize = size / 2;
            var points = [
                {
                    x: centerX + Math.cos(rotation) * halfSize,
                    y: centerY + Math.sin(rotation) * halfSize
                },
                {
                    x: centerX + Math.cos(rotation + Math.PI / 2) * halfSize,
                    y: centerY + Math.sin(rotation + Math.PI / 2) * halfSize
                },
                {
                    x: centerX + Math.cos(rotation + Math.PI) * halfSize,
                    y: centerY + Math.sin(rotation + Math.PI) * halfSize
                },
                {
                    x: centerX + Math.cos(rotation + Math.PI * 1.5) * halfSize,
                    y: centerY + Math.sin(rotation + Math.PI * 1.5) * halfSize
                }
            ];

            diamonds.push({
                points: points,
                size: size
            });
        }

        connections = findClosestPoints(diamonds);
    }

    // Найти ближайшие точки между ромбами (единая сеть)
    function findClosestPoints(diamonds) {
        var allPoints = [];
        diamonds.forEach(function(diamond) {
            allPoints = allPoints.concat(diamond.points);
        });

        var connections = [];
        var maxDistance = 250; // Максимальное расстояние для соединения

        // Сначала создаём базовые соединения (каждая точка с 1-2 ближайшими)
        var connectedPairs = {};
        
        for (var i = 0; i < allPoints.length; i++) {
            var distances = [];
            
            for (var j = 0; j < allPoints.length; j++) {
                if (i === j) continue;
                
                var p1 = allPoints[i];
                var p2 = allPoints[j];
                var dx = p1.x - p2.x;
                var dy = p1.y - p2.y;
                var distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < maxDistance) {
                    distances.push({
                        p1: p1,
                        p2: p2,
                        distance: distance,
                        index: j
                    });
                }
            }

            // Сортируем по расстоянию
            distances.sort(function(a, b) {
                return a.distance - b.distance;
            });

            // Соединяем с 1-2 ближайшими
            var connectionsMade = 0;
            for (var k = 0; k < distances.length && connectionsMade < 2; k++) {
                var conn = distances[k];
                var pairKey = [Math.min(i, conn.index), Math.max(i, conn.index)].join('-');
                
                if (!connectedPairs[pairKey]) {
                    connections.push(conn);
                    connectedPairs[pairKey] = true;
                    connectionsMade++;
                }
            }
        }

        // Проверяем связность графа и добавляем недостающие соединения
        ensureConnectivity(allPoints, connections, connectedPairs, maxDistance);

        return connections;
    }

    // Проверка связности и добавление соединений для единой сети
    function ensureConnectivity(allPoints, connections, connectedPairs, maxDistance) {
        var visited = {};
        var queue = [0]; // Начинаем с первой точки
        visited[0] = true;
        var visitedCount = 1;

        // BFS для поиска связных компонент
        while (queue.length > 0) {
            var currentIndex = queue.shift();
            
            // Находим все соединения текущей точки
            for (var i = 0; i < connections.length; i++) {
                var conn = connections[i];
                var neighborIndex = -1;
                
                if (conn.p1 === allPoints[currentIndex]) {
                    neighborIndex = allPoints.indexOf(conn.p2);
                } else if (conn.p2 === allPoints[currentIndex]) {
                    neighborIndex = allPoints.indexOf(conn.p1);
                }
                
                if (neighborIndex !== -1 && !visited[neighborIndex]) {
                    visited[neighborIndex] = true;
                    visitedCount++;
                    queue.push(neighborIndex);
                }
            }
        }

        // Если не все точки связаны, добавляем соединения
        while (visitedCount < allPoints.length) {
            // Находим ближайшую не посещённую точку к посещённым
            var minDistance = Infinity;
            var bestConnection = null;
            var unvisitedIndex = -1;

            for (var i = 0; i < allPoints.length; i++) {
                if (!visited[i]) {
                    unvisitedIndex = i;
                    
                    for (var j = 0; j < allPoints.length; j++) {
                        if (visited[j]) {
                            var p1 = allPoints[i];
                            var p2 = allPoints[j];
                            var dx = p1.x - p2.x;
                            var dy = p1.y - p2.y;
                            var distance = Math.sqrt(dx * dx + dy * dy);

                            if (distance < minDistance && distance < maxDistance) {
                                minDistance = distance;
                                bestConnection = {
                                    p1: p1,
                                    p2: p2,
                                    distance: distance
                                };
                            }
                        }
                    }
                }
            }

            if (bestConnection) {
                var pairKey = [allPoints.indexOf(bestConnection.p1), allPoints.indexOf(bestConnection.p2)].join('-');
                if (!connectedPairs[pairKey]) {
                    connections.push(bestConnection);
                    connectedPairs[pairKey] = true;
                }
                visited[unvisitedIndex] = true;
                visitedCount++;
                queue.push(unvisitedIndex);
            } else {
                // Если не удалось найти соединение в пределах maxDistance, увеличиваем радиус
                maxDistance += 50;
            }
        }
    }

    // Создать новую пульсацию (волна активности)
    function createPulse() {
        if (diamonds.length === 0) return;
        
        // Выбираем случайный ромб как источник
        var sourceDiamond = diamonds[Math.floor(Math.random() * diamonds.length)];
        var sourcePoint = sourceDiamond.points[Math.floor(Math.random() * sourceDiamond.points.length)];
        
        // Находим все соединения от этого ромба
        var diamondConnections = connections.filter(function(conn) {
            return sourceDiamond.points.indexOf(conn.p1) !== -1 || sourceDiamond.points.indexOf(conn.p2) !== -1;
        });
        
        if (diamondConnections.length > 0) {
            var targetConnection = diamondConnections[Math.floor(Math.random() * diamondConnections.length)];
            var targetPoint = (targetConnection.p1 === sourcePoint) ? targetConnection.p2 : targetConnection.p1;
            
            // Находим ромб, которому принадлежит целевая точка
            var targetDiamond = diamonds.find(function(d) {
                return d.points.indexOf(targetPoint) !== -1;
            });
            
            if (targetDiamond) {
                pulses.push({
                    sourceDiamond: sourceDiamond,
                    targetDiamond: targetDiamond,
                    connection: targetConnection,
                    sourcePoint: sourcePoint,
                    targetPoint: targetPoint,
                    progress: 0,
                    speed: 0.008 + Math.random() * 0.012 // Случайная скорость
                });
            }
        }
    }

    // Функция рисования
    function draw() {
        ctx.clearRect(0, 0, width, height);

        var colors = getColors();
        pulsePhase += 0.008; // Очень медленная базовая пульсация

        // Отслеживаем какие ромбы и линии сейчас пульсируют
        var pulsingDiamonds = {};
        var pulsingConnections = {};

        // Обновляем и отрисовываем пульсации
        pulses.forEach(function(pulse, index) {
            pulse.progress += pulse.speed;
            
            if (pulse.progress >= 1) {
                pulses.splice(index, 1); // Удаляем завершённые пульсации
                return;
            }

            // Этап 1: пульсация исходного ромба (0-33%)
            if (pulse.progress < 0.33) {
                var stageProgress = pulse.progress / 0.33;
                var pulseIntensity = Math.sin(stageProgress * Math.PI) * 0.25 + 0.25; // Уменьшена амплитуда
                pulsingDiamonds[pulse.sourceDiamond] = pulseIntensity;
            }
            // Этап 2: пульсация линии (33-66%)
            else if (pulse.progress < 0.66) {
                var stageProgress = (pulse.progress - 0.33) / 0.33;
                var pulseIntensity = Math.sin(stageProgress * Math.PI) * 0.25 + 0.25; // Уменьшена амплитуда
                pulsingConnections[pulse.connection] = pulseIntensity;
            }
            // Этап 3: пульсация целевого ромба (66-100%)
            else {
                var stageProgress = (pulse.progress - 0.66) / 0.34;
                var pulseIntensity = Math.sin(stageProgress * Math.PI) * 0.25 + 0.25; // Уменьшена амплитуда
                pulsingDiamonds[pulse.targetDiamond] = pulseIntensity;
            }
        });

        // Рисуем линии соединения
        connections.forEach(function(conn, index) {
            var pulse = pulsingConnections[conn] || 0;
            var baseAlpha = 0.35 + Math.sin(pulsePhase + index * 0.15) * 0.05; // Очень слабая базовая пульсация
            var alpha = baseAlpha + pulse * 0.2; // Уменьшена активная пульсация
            var lineWidth = 1 + (pulse * 0.5); // Уменьшена толщина при пульсации
            
            ctx.strokeStyle = colors.line;
            ctx.lineWidth = lineWidth;
            ctx.globalAlpha = alpha;
            ctx.beginPath();
            ctx.moveTo(conn.p1.x, conn.p1.y);
            ctx.lineTo(conn.p2.x, conn.p2.y);
            ctx.stroke();
            ctx.globalAlpha = 1;
        });

        // Рисуем ромбы
        diamonds.forEach(function(diamond) {
            var pulse = pulsingDiamonds[diamond] || 0;
            var baseAlpha = 0.45 + Math.sin(pulsePhase + diamonds.indexOf(diamond) * 0.2) * 0.05; // Очень слабая базовая пульсация
            var alpha = baseAlpha + pulse * 0.15; // Уменьшена активная пульсация
            var lineWidth = 1.5 + (pulse * 0.5); // Уменьшена толщина при пульсации
            
            ctx.strokeStyle = colors.line;
            ctx.lineWidth = lineWidth;
            ctx.globalAlpha = alpha;
            ctx.beginPath();
            ctx.moveTo(diamond.points[0].x, diamond.points[0].y);
            for (var i = 1; i < diamond.points.length; i++) {
                ctx.lineTo(diamond.points[i].x, diamond.points[i].y);
            }
            ctx.closePath();
            ctx.stroke();
            ctx.globalAlpha = 1;

            // Рисуем точки на углах
            diamond.points.forEach(function(point) {
                // Свечение точки
                ctx.beginPath();
                ctx.arc(point.x, point.y, 3, 0, Math.PI * 2);
                ctx.fillStyle = colors.pointGlow;
                ctx.fill();

                // Точка
                ctx.beginPath();
                ctx.arc(point.x, point.y, 1.5, 0, Math.PI * 2);
                ctx.fillStyle = colors.point;
                ctx.fill();
            });
        });
    }

    // Обработчик изменения темы
    function handleThemeChange() {
        draw(); // Перерисовываем с новыми цветами
    }

    // Инициализация
    window.addEventListener('resize', resize);
    window.addEventListener('themeChanged', handleThemeChange);

    // Следим за изменением класса light-theme
    var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.attributeName === 'class') {
                handleThemeChange();
            }
        });
    });
    observer.observe(document.body, { attributes: true });

    // Анимационный цикл
    function animate() {
        draw();
        
        // Случайно создаем пульсации (активность сети)
        if (Math.random() < 0.03) { // 3% шанс каждый кадр
            createPulse();
        }
        
        requestAnimationFrame(animate);
    }

    resize();
    animate(); // Запускаем анимацию
})();
