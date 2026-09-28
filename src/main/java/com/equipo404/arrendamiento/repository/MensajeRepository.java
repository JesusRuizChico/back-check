package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Mensaje;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByConversacion_IdConversacionAndIdMensajeLessThanOrderByIdMensajeDesc(
            Long idConversacion, Long antesDeIdMensaje, Pageable pageable);

    List<Mensaje> findByConversacion_IdConversacionOrderByIdMensajeDesc(
            Long idConversacion, Pageable pageable);

    Mensaje findTopByConversacion_IdConversacionOrderByIdMensajeDesc(Long idConversacion);

    Mensaje findTopByConversacion_IdConversacionOrderByIdMensajeAsc(Long idConversacion);

    long countByConversacion_IdConversacionAndEmisor_IdUsuarioNotAndFechaLecturaIsNull(
            Long idConversacion, Long idUsuario);

    @Modifying
    @Query("""
            update Mensaje m set m.fechaLectura = :fechaLectura
            where m.conversacion.idConversacion = :idConversacion
              and m.emisor.idUsuario <> :idUsuario
              and m.fechaLectura is null
            """)
    int marcarComoLeidos(
            @Param("idConversacion") Long idConversacion,
            @Param("idUsuario") Long idUsuario,
            @Param("fechaLectura") OffsetDateTime fechaLectura);
}
