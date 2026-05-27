# Menify

A Spring Boot 4 application for electronic menus for small businesses.

---

## Prerequisites ✅

Make sure the following tools are installed on your system:

* [Git](https://git-scm.com/)
* [Java 21 JDK](https://adoptium.net/)
* [Gradle](https://gradle.org/) (or use the Gradle wrapper included)
* [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/)

---

## 1️⃣ Clone the repository

```bash
git clone git@github.com:EmanueleMottola/menify.git
cd menify
```

---

## 2️⃣ Build the Spring Boot application

Run Gradle to create the jar:

```bash
./gradlew clean bootJar
```

> **Note:** In Spring Boot 4, the jar produced by `bootJar` does **not** have `-boot` in its name.
> The resulting file will be located at: `build/libs/menify-0.0.1-SNAPSHOT.jar`.

---

## 3️⃣ Build and start Docker containers

Build the images without using the cache to ensure the latest jar is included:

```bash
docker-compose build --no-cache
```

Start the containers:

```bash
docker-compose up
```

This will start two services:

1. **PostgreSQL** database container 🐘
2. **menify** Spring Boot application container 🚀

The Spring Boot application will be available on:

```
http://localhost:8080
```

---

## 4️⃣ Verify the application is running

You can check that the endpoints are responding. For example, on the `/users` endpoint:

```bash
curl http://localhost:8080/users
```

Expected output:

```
[{"id":"1","username":"pippo"}]
```

---

## 5️⃣ Stop the application

To stop the containers gracefully:

```bash
docker-compose down
```

> Database data will remain if Docker volumes are used.

---

## 🔹 Notes

* Always run `./gradlew clean bootJar` before rebuilding Docker to include the latest code changes.
* The Dockerfile copies the jar into the container and runs it automatically.
* Docker Compose defines the interaction between the Spring Boot application and PostgreSQL.
* The application is accessible on your host machine via `localhost:8080`.

---

## Optional: List all endpoints

If you want to see all REST endpoints, you can enable Spring Boot Actuator in your project and access:

```
http://localhost:8080/actuator/mappings
```

This provides a full map of all available endpoints.
