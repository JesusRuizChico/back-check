package com.equipo404.arrendamiento.controller;

import com.equipo404.arrendamiento.dto.response.ServicioResponse;
import com.equipo404.arrendamiento.service.ServicioIncluidoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servicios")
public class ServicioIncluidoController {

    private final ServicioIncluidoService servicioIncluidoService;

    public ServicioIncluidoController(ServicioIncluidoService servicioIncluidoService) {
        this.servicioIncluidoService = servicioIncluidoService;
    }

    @GetMapping
    public List<ServicioResponse> listar() {
        return servicioIncluidoService.listarActivos();
    }
}
