-- ============================================
-- 1. ТАБЛИЦА КАТЕГОРИЙ
-- ============================================
CREATE TABLE categories (
    id_categories SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- ============================================
-- 2. ТАБЛИЦА НЕЙРОСЕТЕЙ
-- ============================================
CREATE TABLE neuronets (
    id_neuronet SERIAL PRIMARY KEY,
    id_categories INT REFERENCES categories(id_categories) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description_network TEXT,
    extended_description TEXT,
    neuronet_icon VARCHAR(500),
    available_in_russia BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================
-- 3. ТАБЛИЦА ПОЛЬЗОВАТЕЛЕЙ
-- ============================================
CREATE TABLE users (
    id_user SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

-- ============================================
-- 4. ТАБЛИЦА ОТЗЫВОВ (ОЦЕНКА + КОММЕНТАРИЙ)
-- ============================================
CREATE TABLE reviews (
    id_review SERIAL PRIMARY KEY,
    id_neuronet INT NOT NULL REFERENCES neuronets(id_neuronet) ON DELETE CASCADE,
    id_user INT NOT NULL REFERENCES users(id_user) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (id_neuronet, id_user)
);

-- ============================================
-- 5. ТАБЛИЦА ГРУПП ТЕГОВ
-- ============================================
CREATE TABLE tag_groups (
    id_tag_group SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================
-- 6. ТАБЛИЦА ТЕГОВ
-- ============================================
CREATE TABLE tags (
    id_tag SERIAL PRIMARY KEY,
    id_tag_group INT NOT NULL REFERENCES tag_groups(id_tag_group) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================
-- 7. ТАБЛИЦА СВЯЗИ НЕЙРОСЕТЕЙ И ТЕГОВ
-- ============================================
CREATE TABLE neuronet_tags (
    id_neuronet INT NOT NULL REFERENCES neuronets(id_neuronet) ON DELETE CASCADE,
    id_tag INT NOT NULL REFERENCES tags(id_tag) ON DELETE CASCADE,
    PRIMARY KEY (id_neuronet, id_tag)
);

-- ============================================
-- ИНДЕКСЫ ДЛЯ ПРОИЗВОДИТЕЛЬНОСТИ
-- ============================================
CREATE INDEX idx_neuronets_category ON neuronets(id_categories);
CREATE INDEX idx_neuronets_name ON neuronets(name);
CREATE INDEX idx_neuronets_available ON neuronets(available_in_russia);
CREATE INDEX idx_reviews_neuronet ON reviews(id_neuronet);
CREATE INDEX idx_reviews_user ON reviews(id_user);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_tags_group ON tags(id_tag_group);
CREATE INDEX idx_neuronet_tags_tag ON neuronet_tags(id_tag);

-- ============================================
-- НАПОЛНЕНИЕ ТАБЛИЦ ТЕГОВ
-- ============================================

-- Группы тегов
INSERT INTO tag_groups (name, slug) VALUES
('Цена', 'pricing'),
('Доступность', 'availability'),
('Особенности', 'features');

-- Теги ценовой политики
INSERT INTO tags (id_tag_group, name) VALUES
(1, 'Бесплатно'),
(1, 'Условно-бесплатно'),
(1, 'Платно'),
(1, 'Пробный период'),
(1, 'Открытый исходный код');

-- Теги доступности
INSERT INTO tags (id_tag_group, name) VALUES
(2, 'Доступна в России'),
(2, 'Требуется VPN'),
(2, 'Нужен иностранный номер'),
(2, 'Недоступна в РФ');

-- Теги особенностей
INSERT INTO tags (id_tag_group, name) VALUES
(3, 'Есть API'),
(3, 'Мобильное приложение'),
(3, 'Веб-интерфейс'),
(3, 'Telegram-бот'),
(3, 'Генерация изображений'),
(3, 'Работа с текстом'),
(3, 'Работа с кодом'),
(3, 'Распознавание речи');