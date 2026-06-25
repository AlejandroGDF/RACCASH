package com.raccooncash.api.excepcion;
public class SolicitudIncorrectaException extends RuntimeException {
    public SolicitudIncorrectaException(String message) {
        super(message);
    }
}
