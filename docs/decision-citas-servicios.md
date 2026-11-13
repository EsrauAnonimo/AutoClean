# Decisión de Cardinalidad: Cita (`Appointment`) vs. Servicio (`ServiceType`)

## Resumen ejecutivo

La revisión del código fuente legacy en `legacy-javafx/` confirma de forma inequívoca que cada cita (`Appointment`) referencia **exactamente un único** tipo de servicio (`ServiceType`) mediante el campo escalar `private String serviceTypeId`. Ni el modelo de dominio, ni los servicios, ni los repositorios, ni la interfaz gráfica ni la documentación en `docs/` contemplan listas de servicios por cita o tablas puente. Por tanto, la cardinalidad migrada es **N:1 (`Appointment` $\rightarrow$ `ServiceType`)** y se adopta la **Opción A**.

---

## Evidencia en el código legacy y documentación

### 1. Modelo de dominio (`Appointment` y `ServiceType`)

- **Campo escalar único en `Appointment`:**
  - `legacy-javafx/src/main/java/com/autoclean/model/Appointment.java:13` — declara `private String serviceTypeId;` (tipo `String`, no `List`, `Set` ni arreglo).
  - `legacy-javafx/src/main/java/com/autoclean/model/Appointment.java:35-44` — el constructor completo recibe un único `String serviceTypeId` y ejecuta `setServiceTypeId(serviceTypeId);` en la línea `40`.
  - `legacy-javafx/src/main/java/com/autoclean/model/Appointment.java:76-85` — `getServiceTypeId()` retorna un `String` y `setServiceTypeId(String serviceTypeId)` exige que el único identificador no sea nulo ni vacío (`IllegalArgumentException`).
- **Catálogo independiente en `ServiceType`:**
  - `legacy-javafx/src/main/java/com/autoclean/model/ServiceType.java:6-11` — solo define `id`, `name`, `description` y `price`; no almacena colección ni referencia inversa hacia `Appointment`.

### 2. Capa de lógica y persistencia (`AppointmentServiceImpl` y `AppointmentRepository`)

- **Firma de creación y actualización:**
  - `legacy-javafx/src/main/java/com/autoclean/service/impl/AppointmentServiceImpl.java:35-43` — `public Appointment create(Appointment appointment)` recibe únicamente la entidad `Appointment` (con su único `serviceTypeId`), asigna `"PENDING"` si falta estado y delega en `repository.save(appointment)`.
  - `legacy-javafx/src/main/java/com/autoclean/service/impl/AppointmentServiceImpl.java:46-55` — `public Appointment update(Appointment appointment)` recibe únicamente una instancia de `Appointment`.
- **Almacenamiento en repositorio:**
  - `legacy-javafx/src/main/java/com/autoclean/repository/AppointmentRepository.java:17` — almacena citas planas en `Map<String, Appointment> storage = new LinkedHashMap<>()`.
  - `legacy-javafx/src/main/java/com/autoclean/repository/AppointmentRepository.java:25-34` — `save(Appointment)` persiste el objeto directo sin tabla ni mapa de asociación M:N.

### 3. Selección y visualización en la interfaz gráfica (`AppointmentPanel` y consumidores)

- **Selección única en el formulario de citas:**
  - `legacy-javafx/src/main/java/com/autoclean/ui/panels/AppointmentPanel.java:56` — declara `private final ComboBox<ServiceType> cbService;` (selector desplegable de un solo valor, sin selección múltiple).
  - `legacy-javafx/src/main/java/com/autoclean/ui/panels/AppointmentPanel.java:127-136` — al seleccionar en `cbService`, copia un único `st.getId()` en el campo de texto `"ID Tipo Servicio"`.
  - `legacy-javafx/src/main/java/com/autoclean/ui/panels/AppointmentPanel.java:337,342,354-356` — `handleCreate()` lee un único `String serviceTypeId` y construye `new Appointment(id.isEmpty() ? null : id, customerId, vehicleId, serviceTypeId, employeeId, dateTime, status)`.
  - `legacy-javafx/src/main/java/com/autoclean/ui/panels/AppointmentPanel.java:371,388-389` — `handleUpdate()` opera igualmente con un único `String serviceTypeId`.
  - `legacy-javafx/src/main/java/com/autoclean/ui/panels/AppointmentPanel.java:260-266,309` — la tabla y la selección de fila leen/cargan un único `getServiceTypeId()`.
- **Consumidores en reportes, costeo y dashboard:**
  - `legacy-javafx/src/main/java/com/autoclean/service/impl/ReportServiceImpl.java:92-93` — `servicesByPeriod` resuelve un único `ServiceType` por cita vía `serviceTypeRepository.findById(app.getServiceTypeId())`.
  - `legacy-javafx/src/main/java/com/autoclean/service/impl/ReportServiceImpl.java:183-188` — `findEstimateForAppointment` utiliza `app.getServiceTypeId()` como respaldo escalar.
  - `legacy-javafx/src/main/java/com/autoclean/ui/panels/CostingPanel.java:101` — al elegir una cita, precarga `"ID Tipo Servicio"` con el valor único `app.getServiceTypeId()`.
  - `legacy-javafx/src/main/java/com/autoclean/ui/panels/DashboardPanel.java:289-290,341,375` — cada turno muestra un único servicio obtenido por `app.getServiceTypeId()`.

### 4. Revisión de `docs/`

- `docs/archive/DOCUMENTACION_AUTOCLEAN.docx` y `docs/archive/DOCUMENTACION_AUTOCLEAN_MEJORADA.docx` — no contienen ningún esquema SQL previo ni mencionan `cita_servicios`; únicamente documentan el uso de `serviceTypeId` en el motor de costeo.

---

## Conclusión

**Opción A — columna `tipo_servicio_id` en citas, NO crear tabla `cita_servicios`**

Cada registro de cita (`Appointment`) pertenece a un único tipo de servicio (`ServiceType`) mediante relación **Muchos a Uno (`N:1`)**.

---

## Implicaciones para el esquema de BD (PostgreSQL / Flyway + JPA)

1. **Sin tabla intermedia:** No se debe crear `cita_servicios` (ni relación `@ManyToMany` / `@OneToMany` intermedia).
2. **Clave foránea directa obligatoria (`NOT NULL`):** La tabla de citas debe incluir una columna `FK` única hacia la tabla de tipos de servicio (por ejemplo, `tipo_servicio_id` o `service_type_id` según convención de nombres inglés acordada en `AGENTS.md` §4) con restricción `NOT NULL`, replicando la validación de `Appointment.java:81-83`.
3. **Entidad JPA (`Appointment`) y DTOs:**
   - En `com.autoclean.model.Appointment`, mapear `@ManyToOne(optional = false, fetch = FetchType.LAZY)` hacia `ServiceType` (no una colección).
   - En los DTOs de creación/actualización/respuesta (`dto/appointment/`), usar un único identificador `serviceTypeId` (no `List<...> serviceTypeIds`), manteniendo compatibilidad 1:1 con `ReportServiceImpl` (`servicesByPeriod` y `revenueVsCost`) y `CostingServiceImpl`.
4. **Índice recomendado:** Crear índice sobre la columna FK de servicio en la tabla de citas para acelerar consultas de reportes por servicio y periodo.
