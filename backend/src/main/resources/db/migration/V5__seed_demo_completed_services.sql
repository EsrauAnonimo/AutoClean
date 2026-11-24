-- Datos de demostración para reportes: clientes, vehículos y servicios finalizados.
-- Los UUID fijos y ON CONFLICT hacen que las inserciones sean seguras ante reintentos.
INSERT INTO clientes (id, nombre, telefono, email)
VALUES
    ('a1000000-0000-4000-8000-000000000001', 'Mariana Torres', '3001234567', 'mariana.torres.demo@autoclean.com'),
    ('a1000000-0000-4000-8000-000000000002', 'Andrés Gómez', '3012345678', 'andres.gomez.demo@autoclean.com'),
    ('a1000000-0000-4000-8000-000000000003', 'Valentina Rojas', '3023456789', 'valentina.rojas.demo@autoclean.com')
ON CONFLICT (id) DO NOTHING;

INSERT INTO vehiculos (id, cliente_id, placa, marca, modelo, anio)
VALUES
    ('a2000000-0000-4000-8000-000000000001', 'a1000000-0000-4000-8000-000000000001', 'DEM101', 'Mazda', 'CX-30', 2022),
    ('a2000000-0000-4000-8000-000000000002', 'a1000000-0000-4000-8000-000000000002', 'DEM202', 'Toyota', 'Corolla', 2021),
    ('a2000000-0000-4000-8000-000000000003', 'a1000000-0000-4000-8000-000000000003', 'DEM303', 'Renault', 'Duster', 2023)
ON CONFLICT (id) DO NOTHING;

INSERT INTO citas (
    id, cliente_id, vehiculo_id, empleado_id, tipo_servicio_id,
    fecha_hora, estado, estado_vehiculo
)
SELECT demo.id, demo.cliente_id, demo.vehiculo_id, empleado.id, servicio.id,
       demo.fecha_hora, 'FINISHED', 'DELIVERED'
FROM (VALUES
    ('a3000000-0000-4000-8000-000000000001'::UUID, 'a1000000-0000-4000-8000-000000000001'::UUID, 'a2000000-0000-4000-8000-000000000001'::UUID, 'Lavado Básico Exterior', 'Carlos Mendoza', TIMESTAMPTZ '2026-10-12 09:00:00-05'),
    ('a3000000-0000-4000-8000-000000000002'::UUID, 'a1000000-0000-4000-8000-000000000002'::UUID, 'a2000000-0000-4000-8000-000000000002'::UUID, 'Lavado Full Interior & Exterior', 'Laura Gómez', TIMESTAMPTZ '2026-10-18 11:00:00-05'),
    ('a3000000-0000-4000-8000-000000000003'::UUID, 'a1000000-0000-4000-8000-000000000003'::UUID, 'a2000000-0000-4000-8000-000000000003'::UUID, 'Full Detailing Cerámico', 'Andrés Ríos', TIMESTAMPTZ '2026-11-03 14:00:00-05'),
    ('a3000000-0000-4000-8000-000000000004'::UUID, 'a1000000-0000-4000-8000-000000000001'::UUID, 'a2000000-0000-4000-8000-000000000001'::UUID, 'Lavado de Motor y Chasis', 'Carlos Mendoza', TIMESTAMPTZ '2026-11-10 10:00:00-05')
) AS demo(id, cliente_id, vehiculo_id, servicio_nombre, empleado_nombre, fecha_hora)
JOIN tipos_servicio servicio ON servicio.nombre = demo.servicio_nombre
JOIN empleados empleado ON empleado.nombre = demo.empleado_nombre
ON CONFLICT (id) DO NOTHING;
