package br.com.elitedev.exception;

public class ExternalCatalogException extends RuntimeException {
    public ExternalCatalogException(String message) {
        super(message);
    }

    public ExternalCatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
