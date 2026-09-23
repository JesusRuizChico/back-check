-- V3__agregar_bloqueo_intentos.sql
ALTER TABLE usuarios ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0;
ALTER TABLE usuarios ADD COLUMN bloqueado_hasta TIMESTAMPTZ;
