
-- 1. Acceso y cuentas

CREATE TABLE usuarios (
    id_usuario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(254) NOT NULL,
    telefono VARCHAR(25),
    foto_perfil TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo',
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMPTZ,
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMPTZ,
    CONSTRAINT ck_usuarios_correo_sin_espacios
        CHECK (correo = btrim(correo)),
    CONSTRAINT ck_usuarios_estado
        CHECK (estado IN ('activo', 'suspendido', 'eliminado')),
    CONSTRAINT ck_usuarios_intentos_fallidos_no_negativos
        CHECK (intentos_fallidos >= 0)
);

CREATE UNIQUE INDEX uq_usuarios_correo_lower
    ON usuarios (lower(correo));
CREATE INDEX idx_usuarios_estado
    ON usuarios (estado);

CREATE TABLE roles (
    id_rol BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE,
    descripcion TEXT,
    CONSTRAINT ck_roles_nombre_no_vacio
        CHECK (btrim(nombre) <> '')
);

INSERT INTO roles (nombre, descripcion) VALUES
    ('arrendatario', 'Usuario que ocupa un inmueble.'),
    ('arrendador', 'Usuario que publica y administra inmuebles.'),
    ('proveedor', 'Usuario que ofrece servicios a la plataforma.'),
    ('administrador', 'Usuario con permisos administrativos.');

CREATE TABLE usuario_rol (
    id_usuario_rol BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_rol_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_rol_rol
        FOREIGN KEY (id_rol) REFERENCES roles (id_rol) ON DELETE RESTRICT,
    CONSTRAINT uq_usuario_rol_usuario_rol
        UNIQUE (id_usuario, id_rol)
);

CREATE INDEX idx_usuario_rol_rol
    ON usuario_rol (id_rol);

-- 2. Categorias y especialidades de proveedores

CREATE TABLE categorias (
    id_categoria BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activa',
    CONSTRAINT ck_categorias_nombre_no_vacio
        CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_categorias_estado
        CHECK (estado IN ('activa', 'inactiva'))
);

CREATE INDEX idx_categorias_estado
    ON categorias (estado);

CREATE TABLE especialidades (
    id_especialidad BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_categoria BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activa',
    CONSTRAINT fk_especialidades_categoria
        FOREIGN KEY (id_categoria) REFERENCES categorias (id_categoria) ON DELETE RESTRICT,
    CONSTRAINT ck_especialidades_nombre_no_vacio
        CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_especialidades_estado
        CHECK (estado IN ('activa', 'inactiva')),
    CONSTRAINT uq_especialidades_categoria_nombre
        UNIQUE (id_categoria, nombre)
);

CREATE INDEX idx_especialidades_categoria_estado
    ON especialidades (id_categoria, estado);

CREATE TABLE usuario_especialidad (
    id_usuario_especialidad BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_especialidad BIGINT NOT NULL,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_especialidad_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_especialidad_especialidad
        FOREIGN KEY (id_especialidad) REFERENCES especialidades (id_especialidad) ON DELETE RESTRICT,
    CONSTRAINT uq_usuario_especialidad_usuario_especialidad
        UNIQUE (id_usuario, id_especialidad)
);

CREATE INDEX idx_usuario_especialidad_especialidad
    ON usuario_especialidad (id_especialidad);

-- 3. Inmuebles, fotografias y servicios incluidos

CREATE TABLE propiedades (
    id_propiedad BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_arrendador BIGINT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    precio_mensual NUMERIC(12,2) NOT NULL,
    calle VARCHAR(150) NOT NULL,
    numero_exterior VARCHAR(20) NOT NULL,
    numero_interior VARCHAR(20),
    colonia VARCHAR(100) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    estado_ubicacion VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(5) NOT NULL,
    latitud NUMERIC(9,6),
    longitud NUMERIC(9,6),
    estado VARCHAR(20) NOT NULL DEFAULT 'disponible',
    verificada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_publicacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_propiedades_arrendador
        FOREIGN KEY (id_arrendador) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_propiedades_precio_positivo
        CHECK (precio_mensual > 0),
    CONSTRAINT ck_propiedades_codigo_postal
        CHECK (codigo_postal ~ '^[0-9]{5}$'),
    CONSTRAINT ck_propiedades_latitud
        CHECK (latitud IS NULL OR latitud BETWEEN -90 AND 90),
    CONSTRAINT ck_propiedades_longitud
        CHECK (longitud IS NULL OR longitud BETWEEN -180 AND 180),
    CONSTRAINT ck_propiedades_coordenadas_pareja
        CHECK ((latitud IS NULL) = (longitud IS NULL)),
    CONSTRAINT ck_propiedades_estado
        CHECK (estado IN ('disponible', 'rentada', 'pausada', 'eliminada'))
);

CREATE INDEX idx_propiedades_arrendador
    ON propiedades (id_arrendador);
CREATE INDEX idx_propiedades_busqueda
    ON propiedades (estado, municipio, precio_mensual);

CREATE TABLE propiedad_fotografia (
    id_fotografia BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_propiedad BIGINT NOT NULL,
    url TEXT NOT NULL,
    orden SMALLINT NOT NULL,
    fecha_carga TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_propiedad_fotografia_propiedad
        FOREIGN KEY (id_propiedad) REFERENCES propiedades (id_propiedad) ON DELETE CASCADE,
    CONSTRAINT ck_propiedad_fotografia_url_no_vacia
        CHECK (btrim(url) <> ''),
    CONSTRAINT ck_propiedad_fotografia_orden_positivo
        CHECK (orden > 0),
    CONSTRAINT uq_propiedad_fotografia_orden
        UNIQUE (id_propiedad, orden)
);

CREATE TABLE servicios (
    id_servicio BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo',
    CONSTRAINT ck_servicios_nombre_no_vacio
        CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_servicios_estado
        CHECK (estado IN ('activo', 'inactivo'))
);

CREATE INDEX idx_servicios_estado
    ON servicios (estado);

CREATE TABLE propiedad_servicio (
    id_propiedad BIGINT NOT NULL,
    id_servicio BIGINT NOT NULL,
    CONSTRAINT pk_propiedad_servicio
        PRIMARY KEY (id_propiedad, id_servicio),
    CONSTRAINT fk_propiedad_servicio_propiedad
        FOREIGN KEY (id_propiedad) REFERENCES propiedades (id_propiedad) ON DELETE CASCADE,
    CONSTRAINT fk_propiedad_servicio_servicio
        FOREIGN KEY (id_servicio) REFERENCES servicios (id_servicio) ON DELETE RESTRICT
);

CREATE INDEX idx_propiedad_servicio_servicio
    ON propiedad_servicio (id_servicio);

-- 4. Arrendamientos y tickets de soporte

CREATE TABLE arrendamientos (
    id_arrendamiento BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_propiedad BIGINT NOT NULL,
    id_arrendatario BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    renta_mensual NUMERIC(12,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo',
    CONSTRAINT fk_arrendamientos_propiedad
        FOREIGN KEY (id_propiedad) REFERENCES propiedades (id_propiedad) ON DELETE RESTRICT,
    CONSTRAINT fk_arrendamientos_arrendatario
        FOREIGN KEY (id_arrendatario) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_arrendamientos_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin > fecha_inicio),
    CONSTRAINT ck_arrendamientos_renta_positiva
        CHECK (renta_mensual > 0),
    CONSTRAINT ck_arrendamientos_estado
        CHECK (estado IN ('activo', 'finalizado'))
);

CREATE UNIQUE INDEX uq_arrendamientos_propiedad_activo
    ON arrendamientos (id_propiedad)
    WHERE estado = 'activo';
CREATE INDEX idx_arrendamientos_arrendatario
    ON arrendamientos (id_arrendatario);

CREATE TABLE tickets (
    id_ticket BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_arrendamiento BIGINT NOT NULL,
    id_categoria BIGINT NOT NULL,
    id_proveedor BIGINT,
    descripcion TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'abierto',
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMPTZ,
    CONSTRAINT fk_tickets_arrendamiento
        FOREIGN KEY (id_arrendamiento) REFERENCES arrendamientos (id_arrendamiento) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_categoria
        FOREIGN KEY (id_categoria) REFERENCES categorias (id_categoria) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_proveedor
        FOREIGN KEY (id_proveedor) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_tickets_descripcion_no_vacia
        CHECK (btrim(descripcion) <> ''),
    CONSTRAINT ck_tickets_estado
        CHECK (estado IN ('abierto', 'asignado', 'en_proceso', 'resuelto', 'cerrado')),
    CONSTRAINT ck_tickets_proveedor_requerido
        CHECK (estado NOT IN ('asignado', 'en_proceso') OR id_proveedor IS NOT NULL),
    CONSTRAINT ck_tickets_fecha_cierre
        CHECK ((estado = 'cerrado' AND fecha_cierre IS NOT NULL)
            OR (estado <> 'cerrado' AND fecha_cierre IS NULL))
);

CREATE INDEX idx_tickets_arrendamiento
    ON tickets (id_arrendamiento);
CREATE INDEX idx_tickets_categoria
    ON tickets (id_categoria);
CREATE INDEX idx_tickets_proveedor_estado
    ON tickets (id_proveedor, estado);

CREATE TABLE ticket_comentario (
    id_comentario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_ticket BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    fecha TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_comentario_ticket
        FOREIGN KEY (id_ticket) REFERENCES tickets (id_ticket) ON DELETE RESTRICT,
    CONSTRAINT fk_ticket_comentario_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_ticket_comentario_contenido_no_vacio
        CHECK (btrim(contenido) <> '')
);

CREATE INDEX idx_ticket_comentario_ticket_fecha
    ON ticket_comentario (id_ticket, fecha);
CREATE INDEX idx_ticket_comentario_usuario
    ON ticket_comentario (id_usuario);

-- 5. Conversaciones y mensajes

CREATE TABLE conversaciones (
    id_conversacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario_1 BIGINT NOT NULL,
    id_usuario_2 BIGINT NOT NULL,
    resumen VARCHAR(4000),
    fecha_resumen TIMESTAMPTZ,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultimo_mensaje TIMESTAMPTZ,
    estado VARCHAR(20) NOT NULL DEFAULT 'activa',
    CONSTRAINT fk_conversaciones_usuario_1
        FOREIGN KEY (id_usuario_1) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_conversaciones_usuario_2
        FOREIGN KEY (id_usuario_2) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_conversaciones_orden_usuarios
        CHECK (id_usuario_1 < id_usuario_2),
    CONSTRAINT ck_conversaciones_estado
        CHECK (estado IN ('activa', 'resumida'))
);

CREATE UNIQUE INDEX uq_conversaciones_pareja
    ON conversaciones (id_usuario_1, id_usuario_2);
CREATE INDEX idx_conversaciones_usuario_1_actividad
    ON conversaciones (id_usuario_1, fecha_ultimo_mensaje DESC);
CREATE INDEX idx_conversaciones_usuario_2_actividad
    ON conversaciones (id_usuario_2, fecha_ultimo_mensaje DESC);

CREATE TABLE mensajes (
    id_mensaje BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_conversacion BIGINT NOT NULL,
    id_emisor BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    fecha_envio TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura TIMESTAMPTZ,
    CONSTRAINT fk_mensajes_conversacion
        FOREIGN KEY (id_conversacion) REFERENCES conversaciones (id_conversacion) ON DELETE CASCADE,
    CONSTRAINT fk_mensajes_emisor
        FOREIGN KEY (id_emisor) REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_mensajes_contenido
        CHECK (btrim(contenido) <> '' AND char_length(contenido) <= 4000)
);

CREATE INDEX idx_mensajes_conversacion_fecha
    ON mensajes (id_conversacion, fecha_envio);
CREATE INDEX idx_mensajes_emisor
    ON mensajes (id_emisor);

-- 6. Triggers de integridad descritos por el modelo

CREATE FUNCTION actualizar_fecha_ticket()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.fecha_actualizacion := clock_timestamp();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_tickets_fecha_actualizacion
BEFORE UPDATE ON tickets
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_ticket();

CREATE FUNCTION impedir_cambio_participantes_conversacion()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.id_usuario_1 IS DISTINCT FROM OLD.id_usuario_1
       OR NEW.id_usuario_2 IS DISTINCT FROM OLD.id_usuario_2 THEN
        RAISE EXCEPTION 'Los participantes de una conversacion no se pueden cambiar'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_conversaciones_participantes_inmutables
BEFORE UPDATE OF id_usuario_1, id_usuario_2 ON conversaciones
FOR EACH ROW
EXECUTE FUNCTION impedir_cambio_participantes_conversacion();

CREATE FUNCTION validar_insertar_mensaje()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    participante_1 BIGINT;
    participante_2 BIGINT;
BEGIN
    SELECT id_usuario_1, id_usuario_2
      INTO participante_1, participante_2
      FROM conversaciones
     WHERE id_conversacion = NEW.id_conversacion
     FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La conversacion % no existe', NEW.id_conversacion
            USING ERRCODE = '23503';
    END IF;

    IF NEW.id_emisor <> participante_1 AND NEW.id_emisor <> participante_2 THEN
        RAISE EXCEPTION 'El emisor debe participar en la conversacion'
            USING ERRCODE = '23514';
    END IF;

    NEW.fecha_envio := clock_timestamp();

    UPDATE conversaciones
       SET fecha_ultimo_mensaje = NEW.fecha_envio,
           estado = 'activa'
     WHERE id_conversacion = NEW.id_conversacion;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_mensajes_validar_y_actualizar_conversacion
BEFORE INSERT ON mensajes
FOR EACH ROW
EXECUTE FUNCTION validar_insertar_mensaje();

CREATE FUNCTION bloquear_y_proteger_mensaje()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    PERFORM 1
      FROM conversaciones
     WHERE id_conversacion = OLD.id_conversacion
     FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La conversacion % no existe', OLD.id_conversacion
            USING ERRCODE = '23503';
    END IF;

    IF TG_OP = 'UPDATE' THEN
        IF NEW.id_conversacion IS DISTINCT FROM OLD.id_conversacion
           OR NEW.id_emisor IS DISTINCT FROM OLD.id_emisor
           OR NEW.fecha_envio IS DISTINCT FROM OLD.fecha_envio THEN
            RAISE EXCEPTION 'No se puede cambiar la conversacion, emisor o fecha de un mensaje'
                USING ERRCODE = '23514';
        END IF;
        RETURN NEW;
    END IF;

    RETURN OLD;
END;
$$;

CREATE TRIGGER trg_mensajes_bloquear_y_proteger
BEFORE UPDATE OR DELETE ON mensajes
FOR EACH ROW
EXECUTE FUNCTION bloquear_y_proteger_mensaje();
