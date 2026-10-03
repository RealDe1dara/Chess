# Chess

Chess is a web-based platform for playing and managing chess games. It combines
a React frontend, a Spring Boot backend, and a PostgreSQL database to provide a
complete online chess experience.

## Game modes

Players can choose from several ways to play:

- **Online multiplayer** — play against another player in real time over the
  network.
- **Local multiplayer** — play against another person on the same device.
- **Computer opponent** — play against an AI opponent powered by the
  **Stockfish chess engine**.

## Features

- User registration, login, and profile management.
- Real-time games with move validation and game-state updates.
- In-game chat and draw offer handling.
- Player ratings and Elo rankings.
- Leaderboards and game results.
- Comments and community feedback.

## Requirements

### Recommended: Docker

- Git
- Docker and Docker Compose

### Local development

- Java 17 or later
- Maven
- Node.js
- PostgreSQL

## Running with Docker

Clone the GitHub repository:

```bash
git clone https://github.com/RealDe1dara/Chess.git
cd Chess
```

Build and start the application and PostgreSQL database:

```bash
docker compose up --build
```

Open [http://localhost:8080](http://localhost:8080) in a browser. To stop the
services, press `Ctrl+C` or run:

```bash
docker compose down
```

The PostgreSQL data is stored in the `postgres_data` Docker volume, so it
survives container restarts. To remove the data as well, run:

```bash
docker compose down -v
```

## Local development

Start PostgreSQL and create a database named `gamestudio`, then run the Spring
Boot backend:

```bash
mvn spring-boot:run
```

In a separate terminal, install the frontend dependencies and start Vite:

```bash
cd frontend
npm ci
npm run dev
```

The frontend development server runs at
[http://localhost:5173](http://localhost:5173).

## Project structure

```text
src/       Spring Boot backend and chess game logic
frontend/  React frontend
design/    Design diagrams and documentation
```

## Useful commands

Run backend tests:

```bash
mvn test
```

Build the frontend:

```bash
cd frontend
npm run build
```

Lint the frontend:

```bash
cd frontend
npm run lint
```
