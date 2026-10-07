package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Conversacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    Optional<Conversacion> findByUsuario1_IdUsuarioAndUsuario2_IdUsuario(
            Long idUsuario1, Long idUsuario2);

    @Query("""
            select c from Conversacion c
            join fetch c.usuario1
            join fetch c.usuario2
            where c.usuario1.idUsuario = :idUsuario or c.usuario2.idUsuario = :idUsuario
            order by coalesce(c.fechaUltimoMensaje, c.fechaCreacion) desc
            """)
    List<Conversacion> listarDelUsuario(@Param("idUsuario") Long idUsuario);

    @Modifying
    @Query(value = """
            insert into conversaciones (id_usuario_1, id_usuario_2)
            values (:idUsuario1, :idUsuario2)
            on conflict (id_usuario_1, id_usuario_2) do nothing
            """, nativeQuery = true)
    int crearSiNoExiste(
            @Param("idUsuario1") Long idUsuario1,
            @Param("idUsuario2") Long idUsuario2);
}
