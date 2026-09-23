# My Khata — Ledger System

A full-stack ledger management application for tracking customers, credits, debits, and account balances.

My Khata provides a simple way to maintain customer records and monitor financial transactions through a web-based dashboard. It includes separate user and administrator workflows, a React frontend, and a Spring Boot backend backed by MySQL and Redis.

## Features

### User Features

- User registration and login
- Session-based user persistence
- User profile access
- Add and manage customers
- Record credit transactions
- Record debit transactions
- Add optional transaction descriptions
- View customer-wise transaction history
- Calculate customer balances automatically
- Filter ledger entries by customer
- Responsive dashboard interface

### Administrator Features

- Dedicated administrator login
- View all registered users
- Add new users
- Edit existing users
- Delete users
- Validate user details before submission
- View user information in a table
- Manage users through an administrator dashboard

## Technology Stack

### Frontend

- React 19
- React Router DOM
- Vite
- Axios
- JavaScript
- CSS
- ESLint

### Backend

- Java 17
- Spring Boot 3.4.4
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Data Redis
- Spring Mail
- MySQL Connector/J
- Lombok
- Twilio SDK
- Maven

### Infrastructure

- MySQL 8
- Redis 7
- Docker
- Docker Compose

## Project Structure

```text
My-Khata--Ledger-System/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   ├── test/
│   │   ├── Dockerfile
│   │   ├── mvnw
│   │   ├── mvnw.cmd
│   │   └── pom.xml
│   │
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Footer.jsx
│   │   │   └── Header.jsx
│   │   ├── pages/
│   │   │   ├── Login.jsx
│   │   │   ├── Signup.jsx
│   │   │   ├── AdminLogin.jsx
│   │   │   └── UserDashboard/
│   │   │       ├── Dashboard.jsx
│   │   │       ├── Home.jsx
│   │   │       ├── AddCustomer.jsx
│   │   │       ├── AddTransaction.jsx
│   │   │       ├── Ledger.jsx
│   │   │       ├── Profile.jsx
│   │   │       └── AdminDashboard.jsx
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── main.jsx
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   ├── vite.config.js
│   └── eslint.config.js
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

## Application Workflow

### User Workflow

1. Open the frontend application.
2. Register a new account or log in.
3. Access the user dashboard.
4. Add customer records.
5. Add credit or debit transactions for customers.
6. Open the ledger to view transaction history.
7. Review calculated balances for each customer.
8. Access the profile page when required.

### Administrator Workflow

1. Open the administrator login page.
2. Authenticate as an administrator.
3. View all registered users.
4. Add, edit, or delete users.
5. Log out of the administrator dashboard.

## Transaction Logic

Transactions support two types:

- **Credit** — money received from a customer
- **Debit** — money given to a customer

Customer balances are calculated using the following logic:

```text
Balance = Total Credits - Total Debits
```

For example:

```text
Credit: ₹5,000
Debit:  ₹1,500
Balance: ₹3,500
```

## Prerequisites

Install the following software before running the project manually:

- Node.js 18 or later
- npm
- Java 17
- Maven, or use the included Maven Wrapper
- MySQL 8 or later
- Redis 7 or later
- Docker and Docker Compose, if using the containerized setup

## Running with Docker Compose

The project includes a Docker Compose configuration for MySQL, Redis, and the Spring Boot backend.

From the repository root, run:

```bash
docker compose up --build
```

The services use the following ports:

| Service | Port |
|---|---:|
| MySQL | `3307` on the host |
| Redis | `6379` |
| Backend API | `1014` |

The frontend should be started separately.

To stop the services:

```bash
docker compose down
```

To stop the services and remove the persisted MySQL volume:

```bash
docker compose down -v
```

> Removing the volume deletes the MySQL data stored by Docker.

## Running the Backend Manually

Navigate to the backend directory:

```bash
cd backend
```

Run the application using the Maven Wrapper.

### Linux and macOS

```bash
./mvnw spring-boot:run
```

### Windows

```bash
mvnw.cmd spring-boot:run
```

Alternatively, if Maven is installed globally:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:1014
```

To build the backend:

```bash
./mvnw clean package
```

The generated JAR file will be available in:

```text
backend/target/
```

## Running the Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The Vite development server will display the local URL in the terminal, usually:

```text
http://localhost:5173
```

Create a production build:

```bash
npm run build
```

Preview the production build locally:

```bash
npm run preview
```

Run ESLint:

```bash
npm run lint
```

## Configuration

The backend reads configuration from environment variables and provides local development defaults.

### Database Configuration

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql:3306/project_db` |
| `SPRING_DATASOURCE_USERNAME` | `root` |
| `SPRING_DATASOURCE_PASSWORD` | `root123` |

### Redis Configuration

| Variable | Default |
|---|---|
| `SPRING_DATA_REDIS_HOST` | `redis` |
| `SPRING_DATA_REDIS_PORT` | `6379` |

### Mail Configuration

| Variable | Default |
|---|---|
| `SPRING_MAIL_HOST` | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | `587` |
| `SPRING_MAIL_USERNAME` | Empty |
| `SPRING_MAIL_PASSWORD` | Empty |
| `APP_MAIL_FROM` | Uses the mail username |

For local development, the Docker Compose file provides the database and Redis connection values automatically.

For production deployments, replace the default credentials and provide mail credentials through environment variables rather than committing them to source control.

## Backend API

The frontend currently communicates with the backend on port `1014`.

Examples of currently used authentication and administration endpoints include:

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/auth/signup` | Register a new user |
| `GET` | `/api/auth/getAllUsers` | Retrieve all users |
| `PUT` | `/api/auth/users/{mobile}` | Update a user |
| `DELETE` | `/api/auth/users/{mobile}` | Delete a user |

The frontend currently uses the backend base URL:

```text
http://localhost:1014
```

## Frontend Routes

| Route | Description |
|---|---|
| `/` | User login |
| `/signup` | User registration |
| `/user` | User dashboard |
| `/user/add-customer` | Add a customer |
| `/user/add-transaction` | Add a transaction |
| `/user/ledger` | View the ledger |
| `/user/profile` | View the user profile |
| `/admin` | Administrator login |
| `/admin-dashboard` | Administrator dashboard |
| `/admin-dashboard/display-users` | Display all users |
| `/admin-dashboard/add-users` | Add a user |
| `/admin-dashboard/edit-user/:mobile` | Edit a user |

## Data and State Management

The frontend currently manages customer and transaction state using React state hooks.

User session information is stored in browser `sessionStorage`:

```text
user
```

Customer and transaction data are maintained in the frontend application state. This means the current implementation should be reviewed before production use if persistent, multi-user ledger storage is required.

## Database

The backend uses:

- MySQL for relational data persistence
- Spring Data JPA for database access
- Hibernate for ORM
- `spring.jpa.hibernate.ddl-auto=update` for local schema updates

The default database configuration is:

```text
Database: project_db
Username: root
Password: root123
Host: mysql
Port: 3306 inside Docker
Port: 3307 from the host
```

## Docker Services

The `docker-compose.yml` file defines the following services:

### MySQL

- Image: `mysql:8.0`
- Database: `project_db`
- Host port: `3307`
- Container port: `3306`
- Persistent volume: `mysql_data`

### Redis

- Image: `redis:7`
- Host port: `6379`

### Backend

- Built from `backend/Dockerfile`
- Depends on MySQL and Redis
- Runs on port `1014`

## Development Notes

- The frontend uses Vite for development and production builds.
- React Router handles user and administrator navigation.
- Axios is used for HTTP communication with the backend.
- The backend uses Spring Boot and Maven.
- The application includes Spring Security, email, Redis, and Twilio dependencies.
- Default credentials in Docker Compose are intended for local development only.
- Configure secrets through environment variables in production.
- The frontend currently contains direct references to the local backend URL and may require environment-based configuration for deployment.

## Testing

Backend tests can be run with:

```bash
cd backend
./mvnw test
```

On Windows:

```bash
cd backend
mvnw.cmd test
```

Frontend linting can be run with:

```bash
cd frontend
npm run lint
```

## Security Considerations

Before deploying this project to production, consider:

- Replacing default database credentials
- Avoiding hard-coded secrets
- Using environment variables for all sensitive configuration
- Configuring a production frontend API URL
- Enabling HTTPS
- Reviewing authentication and authorization rules
- Hashing and securely handling passwords
- Adding server-side validation for all input
- Implementing proper session or token expiration
- Restricting administrator endpoints
- Disabling verbose SQL logging
- Reviewing CORS configuration
- Adding automated security and integration tests

## License

No license has currently been specified for this repository.

If you intend to make the project open source, add an appropriate license file such as MIT, Apache-2.0, or GPL-3.0.

## Author

Developed by [KVRPavanShanmukh](https://github.com/KVRPavanShanmukh).

## Repository

[View My Khata on GitHub](https://github.com/KVRPavanShanmukh/My-Khata--Ledger-System)
