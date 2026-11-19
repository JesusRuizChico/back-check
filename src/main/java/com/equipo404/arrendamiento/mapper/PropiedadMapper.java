package com.equipo404.arrendamiento.mapper;

import com.equipo404.arrendamiento.dto.response.ArrendadorResponse;
import com.equipo404.arrendamiento.dto.response.PropiedadResponse;
import com.equipo404.arrendamiento.dto.response.ServicioResponse;
import com.equipo404.arrendamiento.entity.Propiedad;
import com.equipo404.arrendamiento.entity.PropiedadFotografia;
import com.equipo404.arrendamiento.entity.Servicio;
import com.equipo404.arrendamiento.entity.Usuario;
import java.util.Comparator;
import java.util.List;

public final class PropiedadMapper {

    private PropiedadMapper() {
    }

    public static PropiedadResponse toResponse(Propiedad propiedad, boolean esFavorito) {
        List<String> imagenes = propiedad.getFotografias().stream()
                .map(PropiedadFotografia::getUrl)
                .toList();
        List<ServicioResponse> servicios = propiedad.getServicios().stream()
                .map(propiedadServicio -> toResponse(propiedadServicio.getServicio()))
                .sorted(Comparator.comparing(ServicioResponse::idServicio))
                .toList();

        return new PropiedadResponse(
                propiedad.getIdPropiedad(),
                propiedad.getTitulo(),
                propiedad.getDescripcion(),
                propiedad.getPrecioMensual(),
                propiedad.getCalle(),
                propiedad.getNumeroExterior(),
                propiedad.getNumeroInterior(),
                propiedad.getColonia(),
                propiedad.getMunicipio(),
                propiedad.getEstadoUbicacion(),
                propiedad.getCodigoPostal(),
                propiedad.getLatitud(),
                propiedad.getLongitud(),
                propiedad.getHabitaciones(),
                propiedad.getBanos(),
                propiedad.getEstacionamientos(),
                propiedad.getMetrosCuadrados(),
                propiedad.getEstado(),
                propiedad.getVerificada(),
                propiedad.getFechaPublicacion(),
                imagenes,
                servicios,
                toArrendador(propiedad.getArrendador()),
                esFavorito);
    }

    public static ServicioResponse toResponse(Servicio servicio) {
        return new ServicioResponse(servicio.getIdServicio(), servicio.getNombre());
    }

    private static ArrendadorResponse toArrendador(Usuario arrendador) {
        return new ArrendadorResponse(
                arrendador.getIdUsuario(),
                arrendador.getNombre(),
                arrendador.getTelefono(),
                arrendador.getFotoPerfil());
    }
}
