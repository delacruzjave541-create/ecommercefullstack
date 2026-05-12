# Lab 8 – E-Commerce API: Database Integration & Fetch API

A full-stack e-commerce application using **Spring Boot + Spring Data JPA + MySQL** for the backend and **Vanilla JS (Fetch API)** for the frontend.

---

## Tech Stack

| Layer     | Technology                              |
|-----------|-----------------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Spring Data JPA, Hibernate |
| Database  | MySQL 8                                 |
| Frontend  | HTML5, CSS3, JavaScript (ES2022, async/await) |
| Build     | Maven                                   |

---

## Database Schema

### Tables & Relationships

```
categories (1) ──────< products (M)
orders     (1) ──────< order_items (M)
products   (1) ──────< order_items (M)
```

#### `categories`
| Column | Type         | Constraints        |
|--------|--------------|--------------------|
| id     | BIGINT       | PK, AUTO_INCREMENT |
| name   | VARCHAR(100) | NOT NULL, UNIQUE   |

#### `products`
| Column      | Type           | Constraints              |
|-------------|----------------|--------------------------|
| id          | BIGINT         | PK, AUTO_INCREMENT       |
| name        | VARCHAR(200)   | NOT NULL                 |
| description | TEXT           |                          |
| price       | DECIMAL(10,2)  | NOT NULL                 |
| stock       | INT            | NOT NULL, DEFAULT 0      |
| image_url   | VARCHAR(500)   |                          |
| category_id | BIGINT         | FK → categories.id (LAZY)|

#### `orders`
| Column         | Type        | Constraints        |
|----------------|-------------|--------------------|
| id             | BIGINT      | PK, AUTO_INCREMENT |
| customer_name  | VARCHAR(150)| NOT NULL           |
| customer_email | VARCHAR(200)| NOT NULL           |
| created_at     | DATETIME    | NOT NULL           |
| status         | VARCHAR(20) | DEFAULT 'PENDING'  |

#### `order_items`
| Column     | Type          | Constraints               |
|------------|---------------|---------------------------|
| id         | BIGINT        | PK, AUTO_INCREMENT        |
| order_id   | BIGINT        | FK → orders.id            |
| product_id | BIGINT        | FK → products.id          |
| quantity   | INT           | NOT NULL                  |
| unit_price | DECIMAL(10,2) | NOT NULL                  |

### Relationship Summary
- **Category → Product**: One-to-Many (`@OneToMany` / `@ManyToOne`), `CascadeType.ALL`, `FetchType.LAZY`
- **Order → OrderItem**: One-to-Many (`@OneToMany` / `@ManyToOne`), `CascadeType.ALL`, `FetchType.LAZY`, `orphanRemoval = true`
- **Product → OrderItem**: Many-to-One (product does NOT cascade to order items)

---

## API Endpoints

### Products — `/api/products`

| Method | Path                      | Description                          |
|--------|---------------------------|--------------------------------------|
| GET    | `/api/products`           | Get all products                     |
| GET    | `/api/products?category=` | Filter by category name              |
| GET    | `/api/products?search=`   | Search by name (case-insensitive)    |
| GET    | `/api/products?minPrice=&maxPrice=` | Filter by price range    |
| GET    | `/api/products/{id}`      | Get single product                   |
| POST   | `/api/products`           | Create new product                   |
| PUT    | `/api/products/{id}`      | Update existing product              |
| DELETE | `/api/products/{id}`      | Delete product                       |

### Categories — `/api/categories`

| Method | Path                    | Description           |
|--------|-------------------------|-----------------------|
| GET    | `/api/categories`       | Get all categories    |
| POST   | `/api/categories`       | Create new category   |
| DELETE | `/api/categories/{id}`  | Delete category       |

### Error Responses (from `GlobalExceptionHandler`)

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 99"
}
```

---

## Setup & Running

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 running locally

### Database Setup

```sql
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update `src/main/resources/application.properties` with your MySQL credentials.

### Backend

```bash
mvn spring-boot:run
# API available at http://localhost:8080
```

### Frontend

Open `frontend/index.html` with **VS Code Live Server** (port 5500) — CORS is pre-configured for that origin.

---

## Git Workflow

```bash
# Feature branch
git checkout -b feat/db-integration

# Commits in order
git commit -m "feat: add JPA dependencies and DB config"
git commit -m "feat: add Category, Product, Order, OrderItem entities with Javadoc"
git commit -m "feat: add JPA repositories with custom queries"
git commit -m "feat: refactor ProductService to use JpaRepository"
git commit -m "feat: update controllers with database-backed endpoints"
git commit -m "feat: add GlobalExceptionHandler for 404 and 400 errors"
git commit -m "feat: add frontend Fetch API integration"
git commit -m "iss1: Resolved CORS error – added WebConfig with CrossOrigin mapping"

# Merge into main (do NOT delete the feature branch)
git checkout main
git merge feat/db-integration
git push origin main
git push origin feat/db-integration
```

---

## Screenshots

> Add your screenshots here after running the application:

- `screenshots/db-tables.png` — DBeaver/MySQL Workbench showing populated tables
- `screenshots/browser-console.png` — DevTools console showing successful fetch response
