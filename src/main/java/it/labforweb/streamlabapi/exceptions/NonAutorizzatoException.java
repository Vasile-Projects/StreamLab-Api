package it.labforweb.streamlabapi.exceptions;

public class NonAutorizzatoException extends RuntimeException {
    public NonAutorizzatoException(String message) {
        super(message);
    }
}
