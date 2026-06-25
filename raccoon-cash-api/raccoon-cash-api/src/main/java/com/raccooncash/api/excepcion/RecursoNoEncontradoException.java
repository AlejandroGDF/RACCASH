package com.raccooncash.api.excepcion;
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
