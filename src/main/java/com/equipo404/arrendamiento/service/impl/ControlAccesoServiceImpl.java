package com.equipo404.arrendamiento.service.impl;

import com.equipo404.arrendamiento.exception.CuentaBloqueadaException;
import com.equipo404.arrendamiento.repository.UsuarioRepository;
import com.equipo404.arrendamiento.service.ControlAccesoService;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bloquea la cuenta durante unos minutos tras varios intentos fallidos. */
@Service
public class ControlAccesoServiceImpl implements ControlAccesoService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 5;

    private final UsuarioRepository usuarioRepository;

    public ControlAccesoServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(noRollbackFor = CuentaBloqueadaException.class)
    public void verificarCuentaNoBloqueada(String correo) {
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(usuario -> {
            OffsetDateTime bloqueadoHasta = usuario.getBloqueadoHasta();
            if (bloqueadoHasta == null) {
                return;
            }
            if (bloqueadoHasta.isAfter(OffsetDateTime.now())) {
                throw new CuentaBloqueadaException("Cuenta bloqueada. Intente de nuevo más tarde.");
            }
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        });
    }

    @Override
    @Transactional(noRollbackFor = CuentaBloqueadaException.class)
    public void registrarIntentoFallido(String correo) {
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(usuario -> {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= MAX_INTENTOS_FALLIDOS) {
                usuario.setBloqueadoHasta(OffsetDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
                usuarioRepository.saveAndFlush(usuario);
                throw new CuentaBloqueadaException("Cuenta bloqueada por " + MINUTOS_BLOQUEO + " minutos.");
            }
        });
    }

    @Override
    @Transactional
    public void registrarAccesoExitoso(Long idUsuario) {
        usuarioRepository.findById(idUsuario).ifPresent(usuario -> {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuario.setUltimoAcceso(OffsetDateTime.now());
        });
    }
}
