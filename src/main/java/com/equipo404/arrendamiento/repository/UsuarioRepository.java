package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    Optional<Usuario> findByCorreo(String Correo);
    boolean existsByCorreo(String Correo);
}