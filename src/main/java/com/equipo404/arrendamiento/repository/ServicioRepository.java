package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Servicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByEstadoOrderByIdServicioAsc(String estado);
}
