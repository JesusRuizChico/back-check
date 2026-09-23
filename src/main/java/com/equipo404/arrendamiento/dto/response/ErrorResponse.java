package com.equipo404.arrendamiento.dto.response;

import java.util.Map;

public class ErrorResponse {

    private String codigo;
    private String mensaje;
    private Map<String, String> errores;

    public ErrorResponse() {
    }

    public ErrorResponse(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public ErrorResponse(
            String codigo,
            String mensaje,
            Map<String, String> errores) {

        this.codigo = codigo;
        this.mensaje = mensaje;
        this.errores = errores;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    public void setErrores(Map<String, String> errores) {
        this.errores = errores;
    }
}