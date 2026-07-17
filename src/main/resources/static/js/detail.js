
class Starfall {
    constructor() {
        // Получаем все необходимые элементы
        this.canvas = document.getElementById('starfallCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.container = document.getElementById('starfallContainer');
        this.stars = document.querySelectorAll('.star');
        this.ratingInput = document.getElementById('starfallRating');
        this.emojiDisplay = document.getElementById('starfallEmoji');
        this.textDisplay = document.getElementById('starfallText');

        // Состояние
        this.selectedRating = 0;
        this.fallingStars = [];
        this.animationId = null;
        this.isAnimating = false;
        this.isInitialized = false;

        // Инициализация
        this.init();
    }

    /**
     * Инициализация всех компонентов
     */
    init() {
        // Настройка canvas
        this.resizeCanvas();
        window.addEventListener('resize', () => this.resizeCanvas());

        // Настройка обработчиков событий для звёзд
        this.setupStarEvents();

        // Автоматический старт звездопада
        setTimeout(() => {
            this.startStarfall();
            this.isInitialized = true;
        }, 500);
    }

    /**
     * Изменение размера canvas при изменении окна
     */
    resizeCanvas() {
        const rect = this.container.getBoundingClientRect();
        this.canvas.width = rect.width;
        this.canvas.height = 200;
        this.canvas.style.width = '100%';
        this.canvas.style.height = '200px';
        this.canvas.style.position = 'absolute';
        this.canvas.style.top = '0';
        this.canvas.style.left = '0';
        this.canvas.style.pointerEvents = 'none';
        this.canvas.style.borderRadius = 'var(--radius)';
    }

    /**
     * Настройка событий для звёзд
     */
    setupStarEvents() {
        this.stars.forEach(star => {
            star.addEventListener('click', () => {
                const value = parseInt(star.dataset.value);
                this.selectRating(value);
            });

            star.addEventListener('mouseenter', () => {
                const value = parseInt(star.dataset.value);
                this.hoverStar(value);
            });

            star.addEventListener('mouseleave', () => {
                this.resetHover();
            });
        });
    }

    /**
     * Выбор оценки пользователем
     */
    selectRating(value) {
        this.selectedRating = value;
        this.ratingInput.value = value;

        // Обновляем визуал звёзд
        this.stars.forEach((star, index) => {
            if (index < value) {
                star.classList.add('active');
                star.style.transform = 'scale(1.2)';
                setTimeout(() => {
                    star.style.transform = 'scale(1)';
                }, 200);
            } else {
                star.classList.remove('active');
            }
        });

        // Обновляем текст и эмодзи
        const ratingData = {
            1: { emoji: '😠', text: 'Ужасно' },
            2: { emoji: '😕', text: 'Плохо' },
            3: { emoji: '😐', text: 'Нормально' },
            4: { emoji: '😊', text: 'Хорошо' },
            5: { emoji: '🤩', text: 'Отлично!' }
        };

        const data = ratingData[value];
        if (data) {
            this.emojiDisplay.textContent = data.emoji;
            this.textDisplay.textContent = data.text;
            this.emojiDisplay.style.transform = 'scale(1.5)';
            setTimeout(() => {
                this.emojiDisplay.style.transform = 'scale(1)';
            }, 300);
        }

        // Запускаем интенсивный звездопад
        this.startStarfall();
    }

    /**
     * Эффект при наведении на звезду
     */
    hoverStar(value) {
        this.stars.forEach((star, index) => {
            if (index < value) {
                star.style.transform = 'scale(1.15)';
                star.style.color = '#e3b341';
            }
        });
    }

    /**
     * Сброс эффекта наведения
     */
    resetHover() {
        this.stars.forEach((star, index) => {
            if (index < this.selectedRating) {
                star.style.transform = 'scale(1)';
                star.style.color = '#e3b341';
            } else {
                star.style.transform = 'scale(1)';
                star.style.color = 'var(--text-muted)';
            }
        });
    }

    /**
     * Запуск анимации звездопада
     */
    startStarfall() {
        // Очищаем текущие звёзды
        this.fallingStars = [];
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }

        // Создаём новые звёзды (количество зависит от оценки)
        const count = this.selectedRating > 0 ? this.selectedRating * 10 : 20;
        for (let i = 0; i < count; i++) {
            setTimeout(() => {
                this.fallingStars.push(this.createStar());
            }, i * 30);
        }

        this.isAnimating = true;
        this.animate();
    }

    /**
     * Создание одной падающей звезды
     */
    createStar() {
        return {
            x: Math.random() * this.canvas.width,
            y: -10 - Math.random() * 50,
            size: 6 + Math.random() * 14,
            speed: 0.8 + Math.random() * 2.5,
            rotation: Math.random() * 360,
            rotationSpeed: (Math.random() - 0.5) * 5,
            opacity: 0.3 + Math.random() * 0.7,
            color: ['#e3b341', '#f1c40f', '#f39c12', '#e67e22', '#ffd700'][Math.floor(Math.random() * 5)],
            twinkle: Math.random() * Math.PI * 2,
            twinkleSpeed: 0.02 + Math.random() * 0.03
        };
    }

    /**
     * Основной цикл анимации
     */
    animate() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Обновляем и рисуем звёзды
        this.fallingStars = this.fallingStars.filter(star => {
            // Обновляем позицию и вращение
            star.y += star.speed;
            star.rotation += star.rotationSpeed;
            star.twinkle += star.twinkleSpeed;

            // Мерцание
            const twinkleEffect = 0.7 + 0.3 * Math.sin(star.twinkle);

            // Рисуем звезду
            this.ctx.save();
            this.ctx.translate(star.x, star.y);
            this.ctx.rotate((star.rotation * Math.PI) / 180);
            this.ctx.globalAlpha = star.opacity * twinkleEffect;

            // Рисуем 5-конечную звезду
            this.ctx.beginPath();
            for (let i = 0; i < 10; i++) {
                const radius = i % 2 === 0 ? star.size : star.size * 0.4;
                const angle = (i * Math.PI) / 5 - Math.PI / 2;
                const x = radius * Math.cos(angle);
                const y = radius * Math.sin(angle);
                if (i === 0) {
                    this.ctx.moveTo(x, y);
                } else {
                    this.ctx.lineTo(x, y);
                }
            }
            this.ctx.closePath();
            this.ctx.fillStyle = star.color;
            this.ctx.shadowColor = star.color;
            this.ctx.shadowBlur = 20;
            this.ctx.fill();
            this.ctx.restore();

            return star.y < this.canvas.height + 20;
        });

        // Добавляем новые звёзды, если их мало и есть выбранная оценка
        if (this.fallingStars.length < 8 && this.selectedRating > 0) {
            for (let i = 0; i < 2; i++) {
                this.fallingStars.push(this.createStar());
            }
        }

        this.animationId = requestAnimationFrame(() => this.animate());
    }

    /**
     * Остановка анимации (для очистки ресурсов)
     */
    stopStarfall() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.isAnimating = false;
    }

    /**
     * Получение текущей оценки
     */
    getRating() {
        return this.selectedRating;
    }

    /**
     * Сброс оценки
     */
    resetRating() {
        this.selectedRating = 0;
        this.ratingInput.value = 0;
        this.stars.forEach(star => {
            star.classList.remove('active');
            star.style.transform = 'scale(1)';
            star.style.color = 'var(--text-muted)';
        });
        this.emojiDisplay.textContent = '⭐';
        this.textDisplay.textContent = 'Нажмите на звезду, чтобы оценить';
        this.stopStarfall();
        setTimeout(() => this.startStarfall(), 300);
    }
}

/**
 * Инициализация при загрузке DOM
 */
document.addEventListener('DOMContentLoaded', () => {
    const starfall = new Starfall();

    // Добавляем в глобальный объект для отладки (опционально)
    window.starfall = starfall;

    // Автоматический сброс при клике на пустое место (для удобства)
    document.getElementById('starfallContainer').addEventListener('dblclick', () => {
        starfall.resetRating();
    });
});

