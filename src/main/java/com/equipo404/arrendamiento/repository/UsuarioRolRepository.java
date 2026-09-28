package com.equipo404.arrendamiento.repository;

import com.equipo404.arrendamiento.entity.UsuarioRol;
import com.equipo404.arrendamiento.entity.Usuario;
import com.equipo404.arrendamiento.entity.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

    List<UsuarioRol> findByUsuario(Usuario usuario);

    boolean existsByUsuarioAndRol(Usuario usuario, Rol rol);

    @Query("""
            select count(ur) > 0 from UsuarioRol ur
            where ur.usuario.idUsuario = :idUsuario
              and lower(ur.rol.nombre) = lower(:nombreRol)
            """)
    boolean tieneRol(@Param("idUsuario") Long idUsuario, @Param("nombreRol") String nombreRol);

}
