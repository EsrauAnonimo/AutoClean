# AutoClean Backend (Spring Boot)

API REST en Java 21 + Spring Boot 3 para la gestión del lavadero de autos y centro de detailing **AutoClean**.

## Requisitos

- **Java:** 21 LTS
- **Maven:** 3.9+
- **Base de datos (producción / entorno completo):** PostgreSQL (Supabase)

## Cómo correr en local

1. Copia la plantilla de variables de entorno:

```bash
cp .env.example .env
# llenar .env
mvn spring-boot:run
```

> **Nota:** La conexión real a la base de datos y las migraciones Flyway se activan cuando exista `V1__init_schema.sql` (`agente-db`). Mientras tanto, el contexto puede fallar al arrancar sin `DB_URL` configurada.

## Cómo correr tests

Los tests del esqueleto utilizan el perfil `test` (`application-test.yml`), que excluye la autoconfiguración de `DataSource`, `HibernateJpa` y `Flyway` para validar la capa web sin requerir una base de datos activa:

```bash
mvn test
```

Desde la raíz del monorepo:

```bash
mvn -f backend/pom.xml clean test
```

## Despliegue en Render

Configura el servicio Web en Render con **Root Directory** `backend`, comando de build `mvn clean package -DskipTests`, comando de inicio `java -jar target/autoclean-backend-0.1.0-SNAPSHOT.jar` y define las siguientes variables de entorno en el panel de Render:

- `DB_URL`: cadena JDBC de conexión a PostgreSQL (usar la cadena del **pooler** de Supabase si la conexión directa falla; nunca incluir secretos en el repo).
- `DB_USER`: usuario de PostgreSQL asignado al backend.
- `DB_PASSWORD`: contraseña del usuario de PostgreSQL.
- `JWT_SECRET`: clave secreta para firma y verificación de tokens JWT.
- `JWT_EXPIRATION`: tiempo de expiración del token JWT en milisegundos.
