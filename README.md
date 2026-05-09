# GreenCar Marketplace

Учебный pet-проект — интернет-магазин на **Spring Boot 3 + PostgreSQL**: REST API, форменная авторизация через Spring Security, корзина с привязкой к пользователю, миграции через Liquibase, контейнеризация в Docker и интеграционные тесты.

> Проект является продолжением и рефакторингом репозитория [b1oodraider/Market](https://github.com/b1oodraider/Market). Из-за рассинхрона локального git с origin историю восстановить не удалось, поэтому работа была перенесена в новый репозиторий, чтобы избежать конфликтов. Исходная версия — Thymeleaf-приложение; в этой ветке добавлен слой REST API, отдельный SPA-фронтенд на vanilla JS, Liquibase-миграции, Docker-окружение и тесты.

---

## Стек

| Слой | Технологии |
| --- | --- |
| Язык / рантайм | Java 21 |
| Backend | Spring Boot 3.3, Spring MVC, Spring Data JPA, Spring Data REST, Spring Security 6, Spring Validation |
| Шаблоны | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| БД | PostgreSQL 16 (prod/dev) · H2 (тесты) |
| Миграции | Liquibase (XML changelog) |
| Mapping / boilerplate | ModelMapper, Lombok |
| Frontend (SPA) | Vanilla JS (hash-router), CSS, fetch API |
| Тесты | JUnit 5, Spring Boot Test, MockMvc, Spring Security Test |
| Сборка / деплой | Maven Wrapper, Docker (multi-stage), Docker Compose |

---

## Возможности

- Регистрация и вход с хешированием паролей **BCrypt**, серверная валидация (`jakarta.validation`).
- Сессионная авторизация Spring Security с разделением ролей `ROLE_USER` / `ROLE_ADMIN`.
- Двойной интерфейс на одном бэкенде: серверный рендер на Thymeleaf и одностраничное приложение, общающееся с REST API.
- REST API для аутентификации, каталога и корзины с единым форматом ошибок.
- Корзина в БД с составным ключом `(user_id, product_id)` и каскадными внешними ключами.
- Админ-роль с эксклюзивным доступом к добавлению товаров (`POST /api/products`).
- Унифицированный `GlobalExceptionHandler` для REST-ошибок.
- Liquibase-миграции с `preConditions`, идемпотентным сидом данных и без ручного DDL.
- Готовый `docker-compose` с Postgres + healthcheck и stage-build образ приложения.

---

## Архитектура

```
src/main/java/WebMarket/Market
├── MarketApplication.java        # точка входа, ModelMapper, ResourceHandler
├── configs/
│   ├── SecurityConfig.java       # SecurityFilterChain, BCrypt, формы + JSON 401
│   └── ThymeleafConfig.java
├── controllers/
│   ├── apiControllers/           # REST API (/api/**) + GlobalExceptionHandler
│   └── htmlControllers/          # Thymeleaf-страницы
├── DTO/                          # UserDTO, DBCartDTO
├── models/                       # JPA-сущности (User, Product, Cart, CartId)
├── repositories/                 # Spring Data JPA репозитории
├── security/                     # UsersDetails, SecurityUtils
├── services/                     # бизнес-логика
└── util/Validators/              # кастомные валидаторы (UserRegValidator)
```

### Поведение Security

`SecurityConfig` различает API и web-маршруты:

- `permitAll`: SPA-статика, `/secure/login`, `/secure/registration`, публичные `GET /api/products/**`, `/api/me`, `/api/login`, `/api/register`.
- `hasRole("ADMIN")`: `POST /api/products`, `/stock/addNew`.
- Всё остальное — `authenticated()`.
- Для запросов под `/api/**` `AuthenticationEntryPoint` отдаёт `401` с JSON, для остальных — редирект на форму логина.
- CSRF отключён только для `/api/**` (SPA общается по тому же origin через session-cookie).

---

## REST API (кратко)

| Метод | Endpoint | Доступ | Назначение |
| --- | --- | --- | --- |
| `GET` | `/api/me` | public | Текущий пользователь или `401` |
| `POST` | `/api/login` | public | Логин по `{username, password}` |
| `POST` | `/api/register` | public | Регистрация |
| `POST` | `/api/logout` | auth | Завершение сессии |
| `GET` | `/api/products` | public | Список товаров |
| `GET` | `/api/products/{id}` | public | Карточка товара |
| `POST` | `/api/products` | `ADMIN` | Создать товар |
| `GET` | `/api/cart` | auth | Содержимое корзины |
| `POST` | `/api/cart/{productId}` | auth | Добавить позицию |
| `PATCH` | `/api/cart/{productId}` | auth | Обновить количество (`{count}`) |
| `DELETE` | `/api/cart/{productId}` | auth | Удалить позицию |
| `DELETE` | `/api/cart` | auth | Очистить корзину |

Дополнительно на `/data-api/**` проброшен Spring Data REST (используется как административная подложка).

---

## Запуск

### Через Docker Compose (рекомендуется)

```bash
cp .env.example .env   # отредактируй пароли при необходимости
docker compose up --build
```

После старта приложение доступно на `http://localhost:8080`. Параметры (имя БД, креды, внешние порты) тянутся из `.env` — реальный `.env` в репозиторий не попадает (см. `.gitignore`).

### Локально через Maven Wrapper

Требуется поднятый PostgreSQL и JDK 21.

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run

# С dev-профилем (включает SQL-логирование)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Подключение настраивается переменными окружения (значения по умолчанию указаны в `application.properties`):

| Переменная | Значение по умолчанию |
| --- | --- |
| `DB_URL`  | `jdbc:postgresql://localhost:5432/webMarketUsers` |
| `DB_USER` | `postgres` |
| `DB_PASS` | `postgres` |

Схема БД создаётся автоматически Liquibase при первом старте (см. `src/main/resources/db/changelog/db.changelog-master.xml`); сидируются три демо-товара.

---

## Тесты

```bash
./mvnw test
```

- **Юнит-тесты сервисов** (`CartServiceTest`, `ProductServiceTest`, `RegistrationServiceTest`, `UserServiceTest`) — Mockito.
- **Интеграционный `ApiControllerIntegrationTest`** — `@SpringBootTest` + MockMvc, поднимается с профилем `test` на встроенной H2 (`spring.liquibase.enabled=false`, `ddl-auto=create-drop`).

---

## Что планируется доработать

- Заменить in-memory сессионную авторизацию SPA на JWT/refresh для разделения фронта и бэка.
- Добавить пагинацию и поиск в `/api/products`.
- Прикрутить CI (GitHub Actions: `mvn verify` + сборка Docker-образа).
- Покрыть тестами `CartApiController` и `AuthApiController` end-to-end.
- Вынести `Docker secrets` поверх `.env` для прода.

---

## Лицензия

Учебный проект, без лицензии. Используется как демонстрационный для подачи на стажировки.
