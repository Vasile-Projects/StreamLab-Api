package it.labforweb.streamlabapi.exceptions;

import java.time.LocalDateTime;
import java.util.List;

public class Errore {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<ErroreValidazione> erroriValidazione;

    public Errore(int status, String error, String message, String path) {
        this(status, error, message, path, List.of());
    }

    public Errore(int status, String error, String message, String path, List<ErroreValidazione> erroriValidazione) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.erroriValidazione = erroriValidazione;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<ErroreValidazione> getErroriValidazione() { return erroriValidazione; }
}