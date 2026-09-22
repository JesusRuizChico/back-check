package com.equipo404.arrendamiento.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UsuarioPrincipal implements UserDetails, org.springframework.security.core.CredentialsContainer {

    private final Long idUsuario;
    private final String correo;
    private String passwordHash;
    private final String estado;
    private final List<GrantedAuthority> authorities;

    public UsuarioPrincipal(
            Long idUsuario,
            String correo,
            String passwordHash,
            String estado,
            List<GrantedAuthority> authorities) {

        this.idUsuario = idUsuario;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.estado = estado;
        this.authorities = authorities;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getEstado() {
        return estado;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "activo".equals(estado);
    }
    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}