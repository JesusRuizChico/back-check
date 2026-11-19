package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Propiedad;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    @EntityGraph(attributePaths = "arrendador")
    List<Propiedad> findByArrendador_IdUsuarioAndEstadoNotOrderByFechaPublicacionDesc(
            Long idUsuario, String estadoExcluido);

    @EntityGraph(attributePaths = "arrendador")
    List<Propiedad> findByEstadoOrderByFechaPublicacionDesc(String estado);
}
