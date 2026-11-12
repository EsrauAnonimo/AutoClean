# AGENTS.md — Reglas del proyecto AutoClean

> Fuente de verdad para cualquier agente de IA o humano que toque este repo.
> Léelo completo antes de escribir código. Si algo aquí contradice `docs/`, gana `docs/` y se abre un PR para corregir este archivo.

## 1. Qué es AutoClean

Sistema de gestión para lavaderos de autos y detailing. Proyecto de **Programación 3** (Java + POO).
Equipo: **Esteban (Líder)** y **Dev1**. Ambos actúan como **tutores y revisores**; los agentes escriben el código.
Repositorio: monorepo con `legacy-javafx/` (referencia), `backend/` (Spring Boot), `web/` (frontend), `docs/`.

## 2. Stack objetivo

- **Backend:** Java 21 + Spring Boot 3 + Spring Data JPA + Spring Security + JWT + Flyway. Desplegado en Render / Railway / Fly.io.
- **Base de datos:** PostgreSQL en **Supabase** (plan Free).
- **Frontend:** panel admin + portal de clientes. Desplegado en **Vercel**.
- **Legacy:** la app JavaFX en `legacy-javafx/` es **solo referencia**. No se modifica.

## 3. Fuente de verdad

- `docs/` — reportes técnicos, decisiones, contexto de sesión.
- `legacy-javafx/src/main/java/...` — lógica de negocio original que **se migra, no se reinventa**.
- Este archivo — convenciones y reglas duras.

Antes de implementar un módulo, **lee la clase JavaFX correspondiente** y replica su comportamiento.

## 4. Convenciones de código

- **Código en inglés.** Identificadores, clases, métodos, paquetes, variables, constantes, nombres de columnas en la BD: todo en inglés.
- **Comentarios y documentación en español** permitido.
- **Principios obligatorios:**
  - **GRASP:** bajo acoplamiento, alta cohesión, controlador, experto en información, creador, indirección, polimorfismo, variaciones protegidas.
  - **SOLID-1 (SRP):** una clase, una razón para cambiar.
  - **SOLID-2 (OCP):** abierto a extensión, cerrado a modificación.
  - **SOLID (resto):** LSP, ISP, DIP siempre que aplique.
- **Capas obligatorias:** `controller → service → repository`. Un controlador nunca llama a un repositorio directamente.
- **DTOs obligatorios.** Ningún controlador devuelve entidades JPA. Se usan `record` o clases en `dto/`.
- **Inyección por constructor.** No `@Autowired` en campos.
- **`ServiceFactory` desaparece.** Spring inyecta.
- **Paquete base:** `com.autoclean`.
- **Estructura:** `model/`, `repository/`, `service/` + `service/impl/`, `controller/`, `dto/`, `security/`, `config/`, `exception/`.
- **Migraciones Flyway:** `backend/src/main/resources/db/migration/`, versionadas `V1__init_schema.sql`, `V2__seed_demo.sql`, etc.
- **Excepciones:** manejadas en `exception/GlobalExceptionHandler`. Respuestas de error con código HTTP correcto.
- **Sin secretos en el repo.** Credenciales en `.env` (ignorado). Nunca en `application.yml`.

## 5. Reparto de obligaciones

Cada módulo tiene un humano dueño (tutor) y un agente con nombre. **Un agente solo toca los archivos de su módulo.**

| Módulo | Dueño | Agente | Paquete / archivos |
|---|---|---|---|
| Infra, `pom.xml`, `application*.yml`, `config/`, CORS | Esteban | `agente-infra` | `backend/pom.xml`, `application*.yml`, `AutoCleanApplication`, `config/` |
| Entidades JPA | Esteban | `agente-modelo` | `com.autoclean.model/` |
| Clientes + Vehículos | Esteban | `agente-crud` | `service/impl/{Customer,Vehicle}ServiceImpl`, `controller/{Customer,Vehicle}Controller`, `dto/customer/`, `dto/vehicle/` |
| Seguridad JWT, roles, `usuarios` | Esteban | `agente-seguridad` | `security/`, `model/User`, `controller/AuthController`, `dto/auth/`, `config/SecurityConfig` |
| Catálogo + Empleados | Dev1 | `agente-catalogo` | `service/impl/{ServiceType,Employee}ServiceImpl`, `controller/{ServiceType,Employee}Controller`, DTOs |
| Citas + máquina de estados | Dev1 | `agente-citas` | `service/impl/AppointmentServiceImpl`, `controller/AppointmentController`, `dto/appointment/`, `model/AppointmentStatus` |
| Motor de costeo | Dev1 | `agente-costeo` | `service/impl/CostingServiceImpl`, `controller/CostingController`, `dto/costing/` |
| Reportes | Dev1 | `agente-reportes` | `service/impl/ReportServiceImpl`, `controller/ReportController`, `dto/report/` |
| Insumos (nuevo) | Dev1 / Esteban (UI) | `agente-insumos` | `model/Supply`, `service/impl/SupplyServiceImpl`, `controller/SupplyController`, DTOs, migración |
| Migraciones SQL | Esteban + Dev1 | `agente-db` | `backend/src/main/resources/db/migration/` |
| Tests de reglas de negocio | Dev1 | `agente-tests` | `backend/src/test/java/com/autoclean/rules/` |
| Portal cliente (frontend) | Esteban | `agente-front-cliente` | `web/cliente/` |
| Panel admin (frontend) | Esteban | `agente-front-admin` | `web/admin/` |
| Docs, `AGENTS.md`, repo, CI | Esteban | — | `AGENTS.md`, `docs/`, `README.md`, `.github/`, `.gitignore` |

## 6. Reglas de revisión de PRs

- **Módulo de Esteban:** revisa Esteban.
- **Módulo de Dev1:** revisa Dev1.
- **PR que toca `model/` o `db/migration/`:** revisan **ambos**.
- **Cambios a `pom.xml`, `application*.yml`, `SecurityConfig`, `AGENTS.md`:** revisa **Esteban**.
- Nadie mergea a `main` sin aprobación del revisor primario.

## 7. Reglas anti-conflicto

1. Un agente solo toca su paquete. Si necesita algo de otro módulo, lo pide en el PR; no lo edita.
2. Archivos compartidos (`pom.xml`, `application*.yml`, `SecurityConfig`, `AutoCleanApplication`, `AGENTS.md`) solo los toca Esteban o un PR conjunto.
3. Una rama por módulo: `feat/citas-maquina-estados`, `feat/catalogo-servicios`, etc. Merge frecuente.
4. `model/` es de Esteban. Campos nuevos se piden vía `agente-modelo` y se aprueban entre los dos.
5. Los tests no se tocan para que pasen. Si un test falla, se arregla el código.

## 8. Reglas de negocio intocables (no se reinventan)

Se migran tal cual desde `legacy-javafx/`.

**Validaciones de modelo:**
- `Customer`: nombre, teléfono y correo no vacíos (regex en el servicio).
- `Vehicle`: placa, marca y modelo obligatorios; año entre 1900 y 2100.
- `ServiceType` y `CostLine`: no admiten precios ni montos negativos.
- CRUD rechaza nulos y valida existencia previa antes de actualizar o eliminar (`IllegalArgumentException`).

**Máquina de estados de citas** (`AppointmentServiceImpl.updateStatus`):
- `PENDING → IN_PROGRESS | CANCELLED`
- `IN_PROGRESS → FINISHED | CANCELLED`
- `FINISHED` y `CANCELLED` son finales.
- Transición ilegal → `IllegalStateException`.

**Motor de costeo** (`CostingServiceImpl`):
- Entrada: `serviceTypeId`, `appointmentId` (opcional), `List<CostLine>`.
- `estimatedCost = suma(line.getAmount())`.
- `difference = price - estimatedCost`.
- Guarda un `CostEstimate` con UUID.

**Reportes** (`ReportServiceImpl`):
- `validateDateRange(from, to)` falla si el rango es incoherente.
- `servicesByPeriod(from, to)` cruza citas, clientes y servicios.
- `revenueVsCost(from, to)` cruza citas con estimaciones.
- Sin registros → título `"Sin datos en el periodo"`.

## 9. Seguridad

- Contraseñas con **BCrypt**.
- Autenticación con **JWT**. Roles: `ADMIN`, `OPERATOR`, `MANAGER`, `CLIENTE`.
- Un usuario `CLIENTE` se vincula a su fila en `clientes` vía `cliente_id`.
- **RLS de Supabase activo por defecto.** El backend se conecta con un rol que lo bypassea; no exponer la Data API a clientes.
- La integración **GitHub ↔ Supabase está desactivada a propósito.** Las migraciones se aplican con Flyway.

## 10. Git — reglas duras

- Una rama por tarea. Nombres: `feat/...`, `fix/...`, `chore/...`, `docs/...`, `test/...`.
- **PR obligatorio.** `main` protegido. Nadie pushea directo a `main`.
- `.env` en `.gitignore` desde el primer commit.
- Los agentes **no modifican tests para que pasen**. Se arregla el código.
- Commits en español, imperativos: `agrega endpoint de citas`, `corrige validación de año`.

## 10.bis Git operado por agentes

- Los agentes crean ramas, commitean, pushean y abren PRs. No esperan a que un humano lo haga.
- Convención de ramas: `feat/...`, `fix/...`, `chore/...`, `docs/...`, `test/...`. Siempre desde `main` actualizado.
- Cada PR incluye en el cuerpo: resumen, criterios de terminado cumplidos, comandos ejecutados con su salida, y checklist marcado.
- **Un agente nunca mergea su propio PR.** El merge lo hace el humano tras la aprobación.
- Un agente nunca lee ni escribe `.env`. Solo placeholders (`${DB_PASSWORD}`).
- Un agente nunca ejecuta SQL contra Supabase real. Solo escribe el archivo de migración; el humano lo aplica desde el panel.

## 11. Qué debe entregar cada agente en un PR

1. Código que corre.
2. Tests que pasan.
3. Explicación breve de decisiones no obvias.
4. Lista de criterios de terminado cumplidos, con evidencia (salida de comandos).

Si el agente no puede explicar una clase nueva, el PR se rechaza hasta que la explique.

## 12. Checklist de revisión (para el humano)

- [ ] ¿Corre y pasan los tests?
- [ ] ¿Entiendo cada clase nueva?
- [ ] ¿Respeta `controller → service → repository`?
- [ ] ¿El controlador devuelve DTOs, no entidades?
- [ ] ¿Código en inglés, comentarios en español?
- [ ] ¿Aplica SRP y OCP?
- [ ] ¿Sin contraseñas ni claves en el código?
- [ ] ¿Probé los casos de error, no solo el camino feliz?
- [ ] ¿La lógica de negocio sigue igual que en `legacy-javafx/`?
- [ ] ¿El agente solo tocó archivos de su módulo?

## 13. Riesgos conocidos

- **Supabase Free** pausa proyectos tras una semana de inactividad. Abrir todo un minuto antes de la demo.
- **Render Free** duerme el backend; la primera petición tarda 30–50 s.
- Si Spring no conecta a Supabase, usar la cadena del **pooler**, no la conexión directa.
- Dos agentes editando los mismos archivos: dividir por módulos y mergear seguido.
- Dejar el diseño del frontend para el final.
