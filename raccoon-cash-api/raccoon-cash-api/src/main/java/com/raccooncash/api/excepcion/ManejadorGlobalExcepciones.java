package com.raccooncash.api.excepcion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaErrorApi> handleResourceNotFound(RecursoNoEncontradoException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SolicitudIncorrectaException.class)
    public ResponseEntity<RespuestaErrorApi> handleBadRequest(SolicitudIncorrectaException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaErrorApi> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "Datos invalidos";
        }

        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaErrorApi> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildResponse("Parametro invalido: " + ex.getName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespuestaErrorApi> handleMissingParameter(MissingServletRequestParameterException ex) {
        return buildResponse("Parametro obligatorio faltante: " + ex.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaErrorApi> handleInvalidJson(HttpMessageNotReadableException ex) {
        return buildResponse("El cuerpo de la solicitud no es valido", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaErrorApi> handleDataIntegrity(DataIntegrityViolationException ex) {
        LOGGER.warn("Data integrity violation", ex);
        return buildResponse("Los datos no cumplen con las restricciones de la base de datos", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaErrorApi> handleNoResource(NoResourceFoundException ex) {
        return buildResponse("Recurso no encontrado", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaErrorApi> handleGeneral(Exception ex) {
        LOGGER.error("Unhandled API error", ex);
        return buildResponse("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<RespuestaErrorApi> buildResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new RespuestaErrorApi(message, status.value()));
    }
}
