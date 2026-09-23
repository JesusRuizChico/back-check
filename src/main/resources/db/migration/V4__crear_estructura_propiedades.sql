-- V4__crear_estructura_propiedades.sql

CREATE TABLE propiedades (
    id_propiedad BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_arrendador BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    ubicacion VARCHAR(255) NOT NULL,
    habitaciones INT NOT NULL DEFAULT 1,
    servicios TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activa',
    fecha_publicacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_propiedades_arrendador FOREIGN KEY (id_arrendador) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT chk_propiedades_estado CHECK (estado IN ('activa', 'inactiva', 'rentada'))
);

CREATE TABLE propiedad_imagenes (
    id_imagen BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_propiedad BIGINT NOT NULL,
    url_imagen VARCHAR(500) NOT NULL,

    CONSTRAINT fk_imagenes_propiedad FOREIGN KEY (id_propiedad) REFERENCES propiedades (id_propiedad) ON DELETE CASCADE
);
