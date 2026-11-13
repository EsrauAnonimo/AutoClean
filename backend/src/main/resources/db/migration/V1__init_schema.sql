-- ============================================================================
-- Migración inicial AutoClean (PostgreSQL / Supabase)
-- Decisiones de diseño:
--   * Cardinalidad citas <-> tipos_servicio: Opción A (N:1 vía citas.tipo_servicio_id).
--     Confirmado en docs/decision-citas-servicios.md; NO existe tabla puente cita_servicios.
--   * Todos los identificadores usan UUID generado con gen_random_uuid() (nativo en PostgreSQL 13+).
--   * Todas las marcas de tiempo usan TIMESTAMPTZ para preservar zona horaria en UTC.
-- ============================================================================

-- 1. Clientes del lavadero/detailing
CREATE TABLE clientes (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    telefono TEXT NOT NULL,
    email TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_clientes PRIMARY KEY (id),
    CONSTRAINT uq_clientes_email UNIQUE (email)
);

-- 2. Vehículos asociados a cada cliente
CREATE TABLE vehiculos (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL,
    placa TEXT NOT NULL,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    anio INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_vehiculos PRIMARY KEY (id),
    -- Se restringe el borrado de un cliente si aún tiene vehículos registrados para proteger el historial
    CONSTRAINT fk_vehiculos_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id) ON DELETE RESTRICT,
    -- Regla de negocio heredada de Vehicle.java: año válido entre 1900 y 2100
    CONSTRAINT chk_vehiculos_anio CHECK (anio BETWEEN 1900 AND 2100)
);

-- 3. Catálogo de tipos de servicio ofrecidos
CREATE TABLE tipos_servicio (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    descripcion TEXT,
    precio NUMERIC(12,2) NOT NULL,
    duracion_min INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_tipos_servicio PRIMARY KEY (id),
    -- Regla de negocio heredada de ServiceType.java: no se admiten precios negativos
    CONSTRAINT chk_tipos_servicio_precio CHECK (precio >= 0),
    -- Todo servicio requiere una duración positiva en minutos para poder agendarse
    CONSTRAINT chk_tipos_servicio_duracion_min CHECK (duracion_min > 0)
);

-- 4. Empleados operativos / detailers
CREATE TABLE empleados (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    cargo TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_empleados PRIMARY KEY (id)
);

-- 5. Citas / turnos de servicio
CREATE TABLE citas (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL,
    vehiculo_id UUID NOT NULL,
    empleado_id UUID NOT NULL,
    -- Opción A (docs/decision-citas-servicios.md): relación directa N:1 con tipos_servicio (sin tabla cita_servicios)
    tipo_servicio_id UUID NOT NULL,
    fecha_hora TIMESTAMPTZ NOT NULL,
    estado TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_citas PRIMARY KEY (id),
    -- ON DELETE RESTRICT en todas las entidades maestras para evitar pérdida accidental de historial operativo y reportes
    CONSTRAINT fk_citas_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_citas_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_citas_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleados (id) ON DELETE RESTRICT,
    CONSTRAINT fk_citas_tipo_servicio FOREIGN KEY (tipo_servicio_id)
        REFERENCES tipos_servicio (id) ON DELETE RESTRICT,
    -- Regla de negocio de la máquina de estados (AppointmentServiceImpl.java)
    CONSTRAINT chk_citas_estado CHECK (estado IN ('PENDING', 'IN_PROGRESS', 'FINISHED', 'CANCELLED'))
);

-- 6. Insumos de inventario (módulo nuevo)
CREATE TABLE insumos (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    stock INTEGER NOT NULL,
    unidad TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_insumos PRIMARY KEY (id),
    -- El inventario disponible nunca puede quedar por debajo de cero
    CONSTRAINT chk_insumos_stock CHECK (stock >= 0)
);

-- 7. Estimaciones y cálculos de costo
CREATE TABLE calculos_costo (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    tipo_servicio_id UUID NOT NULL,
    -- cita_id es nullable porque el motor de costeo (CostingServiceImpl) permite simular costos de catálogo sin una cita asociada;
    -- ON DELETE SET NULL conserva la estimación histórica aunque la cita vinculada llegue a eliminarse
    cita_id UUID NULL,
    costo_estimado NUMERIC(12,2) NOT NULL,
    precio NUMERIC(12,2) NOT NULL,
    -- diferencia = precio - costo_estimado; NO tiene CHECK >= 0 porque puede ser negativa cuando el costo supera al precio (pérdida/margen negativo)
    diferencia NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_calculos_costo PRIMARY KEY (id),
    CONSTRAINT fk_calculos_costo_tipo_servicio FOREIGN KEY (tipo_servicio_id)
        REFERENCES tipos_servicio (id) ON DELETE RESTRICT,
    CONSTRAINT fk_calculos_costo_cita FOREIGN KEY (cita_id)
        REFERENCES citas (id) ON DELETE SET NULL,
    CONSTRAINT chk_calculos_costo_costo_estimado CHECK (costo_estimado >= 0),
    CONSTRAINT chk_calculos_costo_precio CHECK (precio >= 0)
);

-- 8. Líneas detalladas de costo asociadas a un cálculo
CREATE TABLE lineas_costo (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    calculo_id UUID NOT NULL,
    concepto TEXT NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_lineas_costo PRIMARY KEY (id),
    -- Las líneas pertenecen exclusivamente a su cálculo padre: si se borra la cabecera, se eliminan en cascada
    CONSTRAINT fk_lineas_costo_calculo FOREIGN KEY (calculo_id)
        REFERENCES calculos_costo (id) ON DELETE CASCADE,
    -- Regla de negocio heredada de CostLine.java: montos negativos no permitidos
    CONSTRAINT chk_lineas_costo_monto CHECK (monto >= 0)
);

-- 9. Usuarios de autenticación y control de acceso (JWT / BCrypt)
CREATE TABLE usuarios (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    rol TEXT NOT NULL,
    -- cliente_id es nullable porque solo aplica a usuarios con rol CLIENTE; staff (ADMIN/OPERATOR/MANAGER) no requiere fila en clientes.
    -- ON DELETE SET NULL desvincula la cuenta sin bloquear ni eliminar credenciales si se depura un registro de cliente
    cliente_id UUID NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id) ON DELETE SET NULL,
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'OPERATOR', 'MANAGER', 'CLIENTE'))
);

-- Índices para llaves foráneas y consultas frecuentes de agenda/reportes
CREATE INDEX idx_vehiculos_cliente_id ON vehiculos (cliente_id);
CREATE INDEX idx_citas_cliente_id ON citas (cliente_id);
CREATE INDEX idx_citas_vehiculo_id ON citas (vehiculo_id);
CREATE INDEX idx_citas_empleado_id ON citas (empleado_id);
CREATE INDEX idx_citas_tipo_servicio_id ON citas (tipo_servicio_id);
CREATE INDEX idx_citas_fecha_hora ON citas (fecha_hora);
CREATE INDEX idx_calculos_costo_tipo_servicio_id ON calculos_costo (tipo_servicio_id);
CREATE INDEX idx_calculos_costo_cita_id ON calculos_costo (cita_id);
CREATE INDEX idx_lineas_costo_calculo_id ON lineas_costo (calculo_id);
CREATE INDEX idx_usuarios_cliente_id ON usuarios (cliente_id);

