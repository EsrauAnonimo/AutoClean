# AutoClean

Sistema de gestión para lavaderos de autos y detailing. Proyecto de
Programación 3 (Java + POO).

## Stack

- **Legacy:** Java 21 + JavaFX 17 (carpeta `legacy-javafx/`, solo referencia).
- **Backend:** Java 21 + Spring Boot 3 + JPA + Security + JWT + Flyway (`backend/`).
- **Base de datos:** PostgreSQL en Supabase.
- **Frontend:** panel admin + portal cliente en Vercel (`web/`).

## Antes de tocar código

Lee [`AGENTS.md`](./AGENTS.md). Contiene el reparto de módulos, las
convenciones (código en inglés, comentarios en español, GRASP + SOLID),
las reglas de negocio intocables y las reglas de Git.

## Cómo correr el legacy (referencia)

    cd legacy-javafx
    mvn clean javafx:run

## Cómo correr el backend (a partir de la Fase 2)

    cd backend
    cp .env.example .env   # llenar variables
    mvn spring-boot:run

## Estructura del repo

    legacy-javafx/   app JavaFX original (referencia)
    backend/         API REST Spring Boot
    web/             frontend (admin + portal cliente)
    db/              scripts auxiliares (las migraciones viven en backend/src/main/resources/db/migration/)
    docs/            documentación técnica y decisiones
    AGENTS.md        reglas del proyecto para agentes y humanos
