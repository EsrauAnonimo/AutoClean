# Modelo Entidad-Relación (ERD) — AutoClean

> Cardinalidad citas↔servicios: Opción A (N:1), decidida en `docs/decision-citas-servicios.md`. No existe tabla `cita_servicios`.

## Diagrama ER

```mermaid
erDiagram
    clientes {
        uuid id PK
        text nombre
        text telefono
        text email UK
        timestamptz created_at
    }

    vehiculos {
        uuid id PK
        uuid cliente_id FK
        text placa
        text marca
        text modelo
        integer anio
        timestamptz created_at
    }

    tipos_servicio {
        uuid id PK
        text nombre
        text descripcion
        numeric precio
        integer duracion_min
        timestamptz created_at
    }

    empleados {
        uuid id PK
        text nombre
        text cargo
        timestamptz created_at
    }

    citas {
        uuid id PK
        uuid cliente_id FK
        uuid vehiculo_id FK
        uuid empleado_id FK
        uuid tipo_servicio_id FK
        timestamptz fecha_hora
        text estado
        timestamptz created_at
        timestamptz updated_at
    }

    insumos {
        uuid id PK
        text nombre
        integer stock
        text unidad
        timestamptz created_at
    }

    calculos_costo {
        uuid id PK
        uuid tipo_servicio_id FK
        uuid cita_id FK
        numeric costo_estimado
        numeric precio
        numeric diferencia
        timestamptz created_at
    }

    lineas_costo {
        uuid id PK
        uuid calculo_id FK
        text concepto
        numeric monto
        timestamptz created_at
    }

    usuarios {
        uuid id PK
        text email UK
        text password_hash
        text rol
        uuid cliente_id FK
        timestamptz created_at
    }

    clientes ||--o{ vehiculos : "posee"
    clientes ||--o{ citas : "agenda"
    vehiculos ||--o{ citas : "recibe"
    empleados ||--o{ citas : "atiende"
    tipos_servicio ||--o{ citas : "define servicio (N:1)"
    tipos_servicio ||--o{ calculos_costo : "se costea para"
    citas |o--o{ calculos_costo : "respalda opcionalmente"
    calculos_costo ||--o{ lineas_costo : "detalla"
    clientes |o--o{ usuarios : "vincula cuenta CLIENTE"
```

## Explicación de llaves foráneas (`FOREIGN KEY`) y políticas `ON DELETE`

- `vehiculos.cliente_id → clientes(id)` (`ON DELETE RESTRICT`): todo vehículo pertenece obligatoriamente a un cliente; `RESTRICT` impide borrar un cliente mientras conserve vehículos registrados.
- `citas.cliente_id → clientes(id)` (`ON DELETE RESTRICT`): vincula la cita con el cliente que solicita el servicio; `RESTRICT` protege el historial de citas y reportes por periodo.
- `citas.vehiculo_id → vehiculos(id)` (`ON DELETE RESTRICT`): identifica el vehículo específico atendido en el turno; `RESTRICT` evita eliminar vehículos con citas históricas.
- `citas.empleado_id → empleados(id)` (`ON DELETE RESTRICT`): asigna el empleado responsable del turno; `RESTRICT` impide eliminar empleados que ya tienen citas asociadas.
- `citas.tipo_servicio_id → tipos_servicio(id)` (`ON DELETE RESTRICT`): materializa la Opción A (N:1, un servicio por cita sin tabla `cita_servicios`); `RESTRICT` impide borrar servicios usados en citas existentes.
- `calculos_costo.tipo_servicio_id → tipos_servicio(id)` (`ON DELETE RESTRICT`): todo cálculo toma como referencia el precio de un tipo de servicio; `RESTRICT` protege los servicios con estimaciones registradas.
- `calculos_costo.cita_id → citas(id)` (`ON DELETE SET NULL`): relaciona opcionalmente una estimación con una cita concreta (`appointmentId` opcional); `SET NULL` conserva el registro contable del costeo si la cita se elimina.
- `lineas_costo.calculo_id → calculos_costo(id)` (`ON DELETE CASCADE`): agrupa los rubros de costo bajo su estimación cabecera; `CASCADE` elimina automáticamente las líneas huérfanas al borrar el cálculo padre.
- `usuarios.cliente_id → clientes(id)` (`ON DELETE SET NULL`): enlaza únicamente a los usuarios con rol `CLIENTE` con su ficha en `clientes`; `SET NULL` permite desvincular la referencia sin destruir el usuario si el cliente se elimina.

