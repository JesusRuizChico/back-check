CREATE TABLE roles (
    id_rol BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(400) NOT NULL UNIQUE,
    descripcion TEXT
);

INSERT INTO roles (nombre, descripcion) VALUES
('arrendatario', 'Usuario que renta una propiedad.'),
('arrendador', 'Usuario que publica y administra propiedades en renta.'),
('proveedor', 'Usuario que ofrece servicios y atiende incidencias.'),
('administrador', 'Usuario encargado de administrar la plataforma.');


CREATE TABLE usuario_rol(
    id_usuario_rol BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    estado VARCHAR (20) NOT NULL,
    fecha_asignacion TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_usuario_rol_usuario
    FOREIGN KEY (id_usuario)
    REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,

    CONSTRAINT fk_usuario_rol_rol
    FOREIGN KEY (id_rol)
    REFERENCES roles(id_rol)
    ON DELETE RESTRICT,

    CONSTRAINT uq_usuario_rol
    UNIQUE (id_usuario, id_rol),

    CONSTRAINT chk_usuario_rol_estado
    CHECK (estado IN ('activo', 'pendiente', 'revocado'))

);