# Job4j_Auth - REST API для модели Person

## Описание проекта

REST-сервис для управления пользователями (модель Person) с поддержкой всех CRUD операций.

## Технологии

- Java 11+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Hibernate

## API Endpoints

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/person/` | Получить всех пользователей |
| GET | `/person/{id}` | Получить пользователя по ID |
| POST | `/person/` | Создать нового пользователя |
| PUT | `/person/` | Обновить существующего пользователя |
| DELETE | `/person/{id}` | Удалить пользователя |

## Запуск проекта

```bash
# Клонировать репозиторий
git clone <your-repo-url>

# Перейти в папку проекта
cd job4j_auth

# Запустить приложение
mvn spring-boot:run