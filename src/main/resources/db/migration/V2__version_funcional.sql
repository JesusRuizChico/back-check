-- V2: ajustes para la version funcional de HabitaCheck.
-- La aplicacion queda con dos perfiles publicos: arrendatario (inquilino) y arrendador.

-- 1. Caracteristicas fisicas que el arrendador captura al publicar un inmueble.

ALTER TABLE propiedades
    ADD COLUMN habitaciones SMALLINT NOT NULL DEFAULT 1,
    ADD COLUMN banos SMALLINT NOT NULL DEFAULT 1,
    ADD COLUMN estacionamientos SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN metros_cuadrados NUMERIC(8,2),
    ADD CONSTRAINT ck_propiedades_habitaciones
        CHECK (habitaciones BETWEEN 0 AND 50),
    ADD CONSTRAINT ck_propiedades_banos
        CHECK (banos BETWEEN 0 AND 50),
    ADD CONSTRAINT ck_propiedades_estacionamientos
        CHECK (estacionamientos BETWEEN 0 AND 50),
    ADD CONSTRAINT ck_propiedades_metros_cuadrados
        CHECK (metros_cuadrados IS NULL OR metros_cuadrados > 0);

-- 2. Biografia editable desde el perfil.

ALTER TABLE usuarios
    ADD COLUMN biografia VARCHAR(500);

-- 3. Propiedades guardadas como favoritas por el arrendatario.

CREATE TABLE favoritos (
    id_usuario BIGINT NOT NULL,
    id_propiedad BIGINT NOT NULL,
    fecha_agregado TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_favoritos
        PRIMARY KEY (id_usuario, id_propiedad),
    CONSTRAINT fk_favoritos_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_favoritos_propiedad
        FOREIGN KEY (id_propiedad) REFERENCES propiedades (id_propiedad) ON DELETE CASCADE
);

CREATE INDEX idx_favoritos_propiedad
    ON favoritos (id_propiedad);

-- 4. Catalogo de servicios incluidos (amenidades) que se eligen al publicar.

INSERT INTO servicios (nombre, descripcion)
SELECT v.nombre, v.descripcion
  FROM (VALUES
        (1, 'Agua potable', 'Suministro de agua incluido.'),
        (2, 'Electricidad', 'Servicio de energía eléctrica incluido.'),
        (3, 'Internet / Wi-Fi', 'Conexión a internet incluida.'),
        (4, 'Gas natural', 'Suministro de gas incluido.'),
        (5, 'Seguridad 24/7', 'Vigilancia permanente.'),
        (6, 'Estacionamiento techado', 'Cajón de estacionamiento cubierto.'),
        (7, 'Elevador', 'Edificio con elevador.'),
        (8, 'Gimnasio', 'Acceso a gimnasio.'),
        (9, 'Alberca / Piscina', 'Acceso a alberca.'),
        (10, 'Pet Friendly (Mascotas)', 'Se permiten mascotas.'),
        (11, 'Amueblado', 'El inmueble se entrega amueblado.'),
        (12, 'Terraza / Balcón', 'Cuenta con terraza o balcón.')
       ) AS v (orden, nombre, descripcion)
 WHERE NOT EXISTS (
        SELECT 1 FROM servicios s WHERE lower(s.nombre) = lower(v.nombre))
 ORDER BY v.orden;

-- 5. Se retira el perfil de prestador de servicios y sus tablas asociadas.

DROP TABLE IF EXISTS ticket_comentario;
DROP TABLE IF EXISTS tickets;
DROP FUNCTION IF EXISTS actualizar_fecha_ticket();
DROP TABLE IF EXISTS usuario_especialidad;
DROP TABLE IF EXISTS especialidades;
DROP TABLE IF EXISTS categorias;

DELETE FROM roles r
 WHERE r.nombre = 'proveedor'
   AND NOT EXISTS (SELECT 1 FROM usuario_rol ur WHERE ur.id_rol = r.id_rol);
