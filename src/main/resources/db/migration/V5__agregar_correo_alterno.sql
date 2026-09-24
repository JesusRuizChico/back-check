--V5__agregar_correo_alterno.sql

ALTER TABLE usuarios
ADD COLUMN IF NOT EXISTS correo_alterno VARCHAR(254);

ALTER TABLE usuarios
ADD CONSTRAINT uq_usuarios_correo_alterno UNIQUE (correo_alterno);

