package com.equipo404.arrendamiento.service;

import com.equipo404.arrendamiento.dto.request.RegistroRequest;
import com.equipo404.arrendamiento.dto.response.UsuarioResponse;

public interface RegistroService {

    UsuarioResponse registrar(RegistroRequest request);
}
