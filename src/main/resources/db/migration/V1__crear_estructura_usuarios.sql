CREATE TABLE usuarios (
    id_usuario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(25),
    foto_perfil TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo',
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMPTZ,

    CONSTRAINT uq_usuarios_correo
    UNIQUE (correo),

    CONSTRAINT chk_usuarios_estado
        CHECK (estado IN ('activo', 'pendiente', 'revocado'))
);
