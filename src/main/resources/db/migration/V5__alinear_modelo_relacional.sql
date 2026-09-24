-- V5__alinear_modelo_relacional.sql

-- 1. Modificar tabla usuarios
ALTER TABLE usuarios DROP CONSTRAINT chk_usuarios_estado;
ALTER TABLE usuarios ADD CONSTRAINT chk_usuarios_estado CHECK (estado IN ('activo', 'suspendido', 'eliminado'));

-- Actualizar el estado de los usuarios existentes (si hay alguno 'pendiente' o 'revocado', pasarlo a 'activo' o 'suspendido' temporalmente para no violar el constraint, aunque en usuarios no deberia haber)
UPDATE usuarios SET estado = 'activo' WHERE estado NOT IN ('activo', 'suspendido', 'eliminado');

-- 2. Modificar tabla propiedades
ALTER TABLE propiedades RENAME COLUMN id_arrendador TO id_propietario;

-- Eliminar constraint anterior
ALTER TABLE propiedades DROP CONSTRAINT fk_propiedades_arrendador;
-- Agregar nueva constraint RESTRICT
ALTER TABLE propiedades ADD CONSTRAINT fk_propiedades_propietario FOREIGN KEY (id_propietario) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT;

ALTER TABLE propiedades RENAME COLUMN precio TO precio_mensual;

ALTER TABLE propiedades DROP COLUMN ubicacion;
ALTER TABLE propiedades ADD COLUMN calle VARCHAR(150);
ALTER TABLE propiedades ADD COLUMN numero_exterior VARCHAR(20);
ALTER TABLE propiedades ADD COLUMN numero_interior VARCHAR(20);
ALTER TABLE propiedades ADD COLUMN colonia VARCHAR(100);
ALTER TABLE propiedades ADD COLUMN municipio VARCHAR(100);
ALTER TABLE propiedades ADD COLUMN estado_ubicacion VARCHAR(100);
ALTER TABLE propiedades ADD COLUMN codigo_postal VARCHAR(5);
ALTER TABLE propiedades ADD COLUMN latitud NUMERIC(9,6);
ALTER TABLE propiedades ADD COLUMN longitud NUMERIC(9,6);
ALTER TABLE propiedades ADD COLUMN verificada BOOLEAN DEFAULT false;

-- Inicializar columnas con datos por defecto para filas existentes
UPDATE propiedades SET 
    calle = 'No especificada', 
    numero_exterior = 'S/N', 
    colonia = 'No especificada', 
    municipio = 'No especificado', 
    estado_ubicacion = 'No especificado', 
    codigo_postal = '00000',
    verificada = false
WHERE calle IS NULL;

-- Hacer las columnas requeridas (NOT NULL) despues del UPDATE
ALTER TABLE propiedades ALTER COLUMN calle SET NOT NULL;
ALTER TABLE propiedades ALTER COLUMN numero_exterior SET NOT NULL;
ALTER TABLE propiedades ALTER COLUMN colonia SET NOT NULL;
ALTER TABLE propiedades ALTER COLUMN municipio SET NOT NULL;
ALTER TABLE propiedades ALTER COLUMN estado_ubicacion SET NOT NULL;
ALTER TABLE propiedades ALTER COLUMN codigo_postal SET NOT NULL;

-- Dropear servicios de propiedades (ahora usaremos tabla)
ALTER TABLE propiedades DROP COLUMN servicios;

-- Alterar estado de propiedades
ALTER TABLE propiedades DROP CONSTRAINT chk_propiedades_estado;
UPDATE propiedades SET estado = 'disponible' WHERE estado = 'activa';
UPDATE propiedades SET estado = 'eliminada' WHERE estado = 'inactiva';
ALTER TABLE propiedades ALTER COLUMN estado SET DEFAULT 'disponible';
ALTER TABLE propiedades ADD CONSTRAINT chk_propiedades_estado CHECK (estado IN ('disponible', 'rentada', 'pausada', 'eliminada'));

-- 3. Renombrar y ajustar tabla de fotografias
ALTER TABLE propiedad_imagenes RENAME TO propiedad_fotografia;
ALTER TABLE propiedad_fotografia RENAME COLUMN id_imagen TO id_fotografia;
ALTER TABLE propiedad_fotografia RENAME COLUMN url_imagen TO url;

ALTER TABLE propiedad_fotografia ADD COLUMN orden SMALLINT;
ALTER TABLE propiedad_fotografia ADD COLUMN fecha_carga TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

-- Actualizar orden para imagenes existentes (simplemente pon 1)
UPDATE propiedad_fotografia SET orden = 1 WHERE orden IS NULL;

-- 4. Crear tabla servicios y propiedad_servicio
CREATE TABLE servicios (
    id_servicio BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo',
    CONSTRAINT chk_servicios_estado CHECK (estado IN ('activo', 'inactivo'))
);

CREATE TABLE propiedad_servicio (
    id_propiedad BIGINT NOT NULL,
    id_servicio BIGINT NOT NULL,
    PRIMARY KEY (id_propiedad, id_servicio),
    CONSTRAINT fk_ps_propiedad FOREIGN KEY (id_propiedad) REFERENCES propiedades(id_propiedad) ON DELETE CASCADE,
    CONSTRAINT fk_ps_servicio FOREIGN KEY (id_servicio) REFERENCES servicios(id_servicio) ON DELETE RESTRICT
);
