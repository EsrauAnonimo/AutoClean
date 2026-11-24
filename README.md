# AutoClean

Sistema de gestión para lavaderos de autos y detailing. Proyecto de
Programación 3 (Java + POO).

## Stack

- **Backend:** Java 21 + Spring Boot 3 + JPA + Security + JWT + Flyway (`backend/`).
- **Base de datos:** PostgreSQL en Supabase.
- **Frontend:** panel admin + portal cliente en Vercel (`web/`).

## Antes de tocar código

Lee [`AGENTS.md`](./AGENTS.md). Contiene el reparto de módulos, las
convenciones (código en inglés, comentarios en español, GRASP + SOLID),
las reglas de negocio intocables y las reglas de Git.

## Cómo correr el backend

    cd backend
    cp .env.example .env   # llenar variables
    mvn spring-boot:run

## Cómo correr el frontend

    cd web
    npm install
    npm run dev

## Estructura del repo

    backend/         API REST Spring Boot
    web/             frontend (admin + portal cliente)
    docs/            documentación técnica y decisiones
    AGENTS.md        reglas del proyecto para agentes y humanos
