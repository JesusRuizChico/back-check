package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)")
    Optional<Usuario> findByCorreoIgnoreCase(@Param("correo") String correo);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)")
    boolean existsByCorreoIgnoreCase(@Param("correo") String correo);
}
