package com.equipo404.arrendamiento.exception;

import com.equipo404.arrendamiento.dto.response.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CorreoYaRegistradoException.class)
    public ResponseEntity<ErrorResponse> manejarCorreoYaRegistrado(
            CorreoYaRegistradoException exception) {

        ErrorResponse error = new ErrorResponse(
                "CORREO_YA_REGISTRADO",
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ErrorResponse> manejarRolNoPermitido(
            RolNoPermitidoException exception) {

        ErrorResponse error = new ErrorResponse(
                "ROL_NO_PERMITIDO",
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarErroresValidacion(
            MethodArgumentNotValidException exception) {

        Map<String, String> errores = new HashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            errores.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ErrorResponse error = new ErrorResponse(
                "DATOS_INVALIDOS",
                "Existen errores en los datos enviados.",
                errores
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarAutenticacion() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarArgumento(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse("DATOS_INVALIDOS", exception.getMessage()));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido() {
        return ResponseEntity.badRequest().body(
                new ErrorResponse("JSON_INVALIDO", "El cuerpo de la solicitud no es válido"));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarConflicto() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse("CONFLICTO", "Los datos entran en conflicto con un registro existente"));
    }
}