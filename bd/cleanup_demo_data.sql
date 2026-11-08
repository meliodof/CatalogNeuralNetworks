-- Очистка БД CatologistNeuralNetworks от данных нагрузочного теста и восстановление демо-данных
BEGIN;
TRUNCATE review_votes, reviews, neuronet_tags, neuronets, tags, tag_groups, users, categories RESTART IDENTITY;
INSERT INTO categories (id_categories, name) VALUES
(1, 'Генерация текста'),
(2, 'Генерация изображений'),
(3, 'Работа с кодом'),
(4, 'Аудио и речь'),
(5, 'Видео');
INSERT INTO tag_groups (id_tag_group, name, slug) VALUES
(1, 'Цена', 'pricing'),
(2, 'Доступность', 'availability'),
(3, 'Особенности', 'features');
INSERT INTO tags (id_tag, id_tag_group, name) VALUES
(1, 1, 'Бесплатно'),
(2, 1, 'Платно'),
(4, 1, 'Пробный период'),
(6, 2, 'Доступна в России'),
(7, 2, 'Требуется VPN'),
(9, 3, 'Есть API'),
(10, 3, 'Мобильное приложение'),
(11, 3, 'Веб-интерфейс'),
(3, 1, 'Частично бесплатно');
INSERT INTO users (id_user, email, password_hash, username) VALUES
(1, 'test@example.com', '{noop}password123', 'testuser'),
(2, 'antonlarin122@gmail.com', '$2a$10$7Y2vQJw6mX4sYJkYdU/iC.jy46uteOxTwU6Yua9WXeXtzctExOneG', 'a'),
(3, 'an7on.lar@yandex.ru', '$2a$10$DKHDkfiUjb5WEpsJ5.MYBeOW3IMVxy1a57mRHDkWx/3HDI6ScQJzi', 'testuser1'),
(4, 'stalker2.aretem@mail.ru', '$2a$10$VDjo8a5ZrAb4VViJeu9Iv.kOqr1yd2YemqVfimL3RlAoHe20E7Ju.', 'test2');
INSERT INTO neuronets (available_in_russia, created_at, id_categories, id_neuronet, description_network, extended_description, name, neuronet_icon, usage_registration, usage_setup, usage_general, official_url, api_url, github_url, pricing_info) VALUES
(FALSE, NULL, 1, 1, 'Универсальный чат-бот от OpenAI', 'Большая языковая модель', 'ChatGPT', '/images/chatgpt.png', 'Перейдите на сайт, нажмите Sign Up, введите email и пароль.', 'После регистрации выберите тарифный план и получите API-ключ в личном кабинете.', 'Отправляйте запросы через веб-интерфейс или API. Модель поддерживает много языков.', 'https://openai.com/chatgpt', 'https://platform.openai.com/docs', 'https://github.com/openai', 'Бесплатный тариф: 10 запросов в день.<br>Pro: от 1 500 ₽/мес — безлимитные запросы, доступ к API, приоритетная поддержка.<br>Enterprise: индивидуальные условия, выделенный сервер, SLA 99.9%.'),
(FALSE, NULL, 1, 2, 'Ассистент от Anthropic', 'Безопасная языковая модель', 'Claude', '/images/claude.png', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(TRUE, NULL, 1, 3, 'Российский чат-бот от Сбера', 'Мультимодальная нейросеть', 'GigaChat', '/images/gigachat.png', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(TRUE, NULL, 2, 4, 'Генерация изображений', 'Художественные изображения', 'Midjourney', '/images/midjourney.png', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(TRUE, NULL, 2, 5, 'Российская генерация картинок', 'Нейросеть от Сбера', 'Kandinsky', '/images/kandinsky.png', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(TRUE, NULL, 3, 6, 'Помощник программиста', 'ИИ для написания кода', 'GitHub Copilot', '/images/copilot.png', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(TRUE, NULL, 3, 7, 'Российский ассистент для кода', 'Помощник от Яндекса', 'Yandex Code Assistant', '/images/yandex_code.png', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(TRUE, NULL, 4, 8, 'Распознавание речи', 'Распознавание и перевод речи', 'Whisper', '/images/whisper.png', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO neuronet_tags (id_neuronet, id_tag) VALUES
(1, 7),
(1, 9),
(1, 11),
(2, 7),
(2, 9),
(2, 11),
(3, 6),
(3, 9),
(3, 10),
(3, 11),
(4, 6),
(5, 6),
(5, 11),
(6, 9),
(7, 6),
(7, 10),
(8, 6),
(8, 9),
(1, 3),
(2, 2),
(3, 1),
(4, 2),
(5, 1),
(6, 3),
(7, 1),
(8, 1);
INSERT INTO reviews (rating, created_at, id_neuronet, id_review, id_user, comment) VALUES
(5, NULL, 1, 17, 1, 'Отличный помощник!'),
(5, NULL, 3, 18, 1, 'Лучший российский аналог'),
(4, NULL, 4, 19, 1, 'Красивые картинки'),
(5, NULL, 5, 20, 1, 'Бесплатно и на русском'),
(5, '2026-07-27 16:47:36.925118', 1, 21, 4, 'Ночь. Улица. Фонарь. А нейросеть топ.'),
(4, '2026-07-29 14:42:42.727905', 2, 22, 4, 'Класс');
INSERT INTO review_votes (id, id_review, id_user, vote) VALUES
(25, 21, 1, 1),
(26, 21, 4, 1);
SELECT setval(pg_get_serial_sequence('neuronets','id_neuronet'), (SELECT max(id_neuronet) FROM neuronets));
SELECT setval(pg_get_serial_sequence('reviews','id_review'), (SELECT max(id_review) FROM reviews));
SELECT setval(pg_get_serial_sequence('review_votes','id'), (SELECT max(id) FROM review_votes));
SELECT setval(pg_get_serial_sequence('tags','id_tag'), (SELECT max(id_tag) FROM tags));
SELECT setval(pg_get_serial_sequence('tag_groups','id_tag_group'), (SELECT max(id_tag_group) FROM tag_groups));
SELECT setval(pg_get_serial_sequence('users','id_user'), (SELECT max(id_user) FROM users));
-- Дополнительные отзывы, чтобы у каждой нейросети была оценка
INSERT INTO reviews (rating, created_at, id_neuronet, id_user, comment) VALUES
(4, now() - interval '20 days', 2, 2, 'Хорошо пишет код и длинные тексты'),
(5, now() - interval '18 days', 3, 3, 'Работает без VPN, удобно'),
(3, now() - interval '15 days', 4, 2, 'Картинки красивые, но дорого'),
(4, now() - interval '12 days', 5, 3, 'Хорошо понимает запросы на русском'),
(5, now() - interval '10 days', 6, 2, 'Экономит кучу времени при разработке'),
(4, now() - interval '8 days',  7, 3, 'Неплохие подсказки в IDE'),
(5, now() - interval '6 days',  8, 2, 'Точно распознаёт речь даже с шумом'),
(4, now() - interval '4 days',  6, 4, 'Иногда ошибается, но в целом полезен'),
(5, now() - interval '2 days',  8, 1, 'Использую для расшифровки лекций');
INSERT INTO review_votes (id_review, id_user, vote) VALUES
(17, 2, 1), (18, 2, 1), (19, 3, -1), (22, 1, 1),
((SELECT id_review FROM reviews WHERE id_user = 2 AND id_neuronet = 6), 3, 1),
((SELECT id_review FROM reviews WHERE id_user = 3 AND id_neuronet = 3), 4, 1);
SELECT setval(pg_get_serial_sequence('reviews','id_review'), (SELECT max(id_review) FROM reviews));
SELECT setval(pg_get_serial_sequence('review_votes','id'), (SELECT max(id) FROM review_votes));
COMMIT;
