package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.UsuarioRol;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.Rol;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

    List<UsuarioRol> findByUsuario(Usuario usuario);

    boolean existsByUsuarioAndRol(Usuario usuario, Rol rol);

}