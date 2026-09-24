package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {
    List<Propiedad> findByPropietario_IdUsuario(Long idUsuario);
}
