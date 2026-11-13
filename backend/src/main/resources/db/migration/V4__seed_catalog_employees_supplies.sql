-- Seed inicial idempotente de catálogo de servicios, especialistas e insumos operativos
INSERT INTO tipos_servicio (nombre, descripcion, precio, duracion_min)
SELECT 'Lavado Básico Exterior', 'Espuma activa, enjuague a presión, secado con microfibra y brillo en llantas.', 30000.00, 45
WHERE NOT EXISTS (SELECT 1 FROM tipos_servicio WHERE nombre = 'Lavado Básico Exterior');

INSERT INTO tipos_servicio (nombre, descripcion, precio, duracion_min)
SELECT 'Lavado Full Interior & Exterior', 'Lavado completo de carrocería, aspirado profundo, limpieza de tablero y tapicería.', 55000.00, 60
WHERE NOT EXISTS (SELECT 1 FROM tipos_servicio WHERE nombre = 'Lavado Full Interior & Exterior');

INSERT INTO tipos_servicio (nombre, descripcion, precio, duracion_min)
SELECT 'Full Detailing Cerámico', 'Descontaminado de pintura, corrección ligera de brillo y sellador cerámico.', 160000.00, 120
WHERE NOT EXISTS (SELECT 1 FROM tipos_servicio WHERE nombre = 'Full Detailing Cerámico');

INSERT INTO tipos_servicio (nombre, descripcion, precio, duracion_min)
SELECT 'Lavado de Motor y Chasis', 'Desengrase seguro de compartimiento de motor, guardabarros y parte inferior.', 45000.00, 45
WHERE NOT EXISTS (SELECT 1 FROM tipos_servicio WHERE nombre = 'Lavado de Motor y Chasis');

INSERT INTO empleados (nombre, cargo)
SELECT 'Carlos Mendoza', 'Detailer Senior'
WHERE NOT EXISTS (SELECT 1 FROM empleados WHERE nombre = 'Carlos Mendoza');

INSERT INTO empleados (nombre, cargo)
SELECT 'Laura Gómez', 'Especialista en Lavado'
WHERE NOT EXISTS (SELECT 1 FROM empleados WHERE nombre = 'Laura Gómez');

INSERT INTO empleados (nombre, cargo)
SELECT 'Andrés Ríos', 'Técnico de Acabado y Polichado'
WHERE NOT EXISTS (SELECT 1 FROM empleados WHERE nombre = 'Andrés Ríos');

INSERT INTO insumos (nombre, stock, unidad)
SELECT 'Shampoo pH Neutro', 18, 'litros'
WHERE NOT EXISTS (SELECT 1 FROM insumos WHERE nombre = 'Shampoo pH Neutro');

INSERT INTO insumos (nombre, stock, unidad)
SELECT 'Cera Carnauba Líquida', 10, 'litros'
WHERE NOT EXISTS (SELECT 1 FROM insumos WHERE nombre = 'Cera Carnauba Líquida');

INSERT INTO insumos (nombre, stock, unidad)
SELECT 'Paños Microfibra 40x40', 25, 'unidades'
WHERE NOT EXISTS (SELECT 1 FROM insumos WHERE nombre = 'Paños Microfibra 40x40');

INSERT INTO insumos (nombre, stock, unidad)
SELECT 'Desengrasante Multiusos', 12, 'litros'
WHERE NOT EXISTS (SELECT 1 FROM insumos WHERE nombre = 'Desengrasante Multiusos');
