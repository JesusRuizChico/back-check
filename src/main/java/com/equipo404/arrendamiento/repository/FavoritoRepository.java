package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Favorito;
import com.equipo404.arrendamiento.entity.FavoritoId;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritoRepository extends JpaRepository<Favorito, FavoritoId> {

    @Query("select f.propiedad.idPropiedad from Favorito f where f.usuario.idUsuario = :idUsuario")
    Set<Long> idsPropiedadesFavoritas(@Param("idUsuario") Long idUsuario);

    @EntityGraph(attributePaths = {"propiedad", "propiedad.arrendador"})
    List<Favorito> findByUsuario_IdUsuarioAndPropiedad_EstadoNotOrderByFechaAgregadoDesc(
            Long idUsuario, String estadoExcluido);

    @Modifying
    @Query(value = """
            insert into favoritos (id_usuario, id_propiedad)
            values (:idUsuario, :idPropiedad)
            on conflict (id_usuario, id_propiedad) do nothing
            """, nativeQuery = true)
    int agregarSiNoExiste(@Param("idUsuario") Long idUsuario, @Param("idPropiedad") Long idPropiedad);
}
