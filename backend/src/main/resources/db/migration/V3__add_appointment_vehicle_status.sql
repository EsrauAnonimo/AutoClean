-- Estado operativo en vivo del vehículo dentro de la cita
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS estado_vehiculo TEXT NOT NULL DEFAULT 'RECEIVED';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_citas_estado_vehiculo'
    ) THEN
        ALTER TABLE citas
            ADD CONSTRAINT chk_citas_estado_vehiculo
                CHECK (estado_vehiculo IN ('RECEIVED', 'WASHING', 'DETAILING', 'READY', 'DELIVERED'));
    END IF;
END $$;

