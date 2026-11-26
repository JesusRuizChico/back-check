-- V3: eliminacion logica de mensajes del chat.
-- El emisor puede eliminar sus propios mensajes; la fila se conserva y se marca con la fecha
-- de eliminacion para no romper el historial ni el orden de la conversacion.

ALTER TABLE mensajes
    ADD COLUMN fecha_eliminacion TIMESTAMPTZ;
