package it.labforweb.streamlabapi.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.ArrayList;
import java.util.List;


//@RestControllerAdvice registra la classe come gestore globale degli errori per tutti i RestController
@RestControllerAdvice
public class GestoreEccezioniGlobale {

    private static final Logger log = LoggerFactory.getLogger(GestoreEccezioniGlobale.class);

    @ExceptionHandler(NonAutorizzatoException.class)
    public ResponseEntity<Errore> gestisciNonAutorizzato(NonAutorizzatoException ex, HttpServletRequest request) {
        Errore errore = new Errore(
                HttpStatus.UNAUTHORIZED.value(),
                "Non autorizzato",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errore);
    }

    @ExceptionHandler(RisorsaNonTrovataException.class)
    public ResponseEntity<Errore> gestisciNonTrovato(RisorsaNonTrovataException ex, HttpServletRequest request) {
        Errore errore = new Errore(
                HttpStatus.NOT_FOUND.value(),
                "Non trovato",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errore);
    }

    @ExceptionHandler(RisorsaGiaEsistenteException.class)
    public ResponseEntity<Errore> gestisciConflitto(RisorsaGiaEsistenteException ex, HttpServletRequest request) {
        Errore errore = new Errore(
                HttpStatus.CONFLICT.value(),
                "Conflitto",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errore);
    }

    @ExceptionHandler(RichiestaNonValidaException.class)
    public ResponseEntity<Errore> gestisciRichiestaNonValida(RichiestaNonValidaException ex, HttpServletRequest request) {
        Errore errore = new Errore(
                HttpStatus.BAD_REQUEST.value(),
                "Richiesta non valida",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Errore> gestisciVincoloDb(DataIntegrityViolationException ex, HttpServletRequest request) {

        String causaMessaggio = ex.getMostSpecificCause().getMessage().toLowerCase();
        String messaggio;

        if (causaMessaggio.contains("email_unique")) {
            messaggio = "Email già registrata";
        } else if (causaMessaggio.contains("user_id_tmdb_id_tipo_unique")) {
            messaggio = "Il titolo è già presente nei preferiti";
        } else {
            messaggio = "Operazione non consentita: dato duplicato";
        }

        Errore errore = new Errore(
                HttpStatus.CONFLICT.value(),
                "Conflitto",
                messaggio,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errore);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Errore> gestisciValidazione(MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErroreValidazione> erroriValidazione = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErroreValidazione(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        Errore errore = new Errore(
                HttpStatus.BAD_REQUEST.value(),
                "Errore di validazione",
                "Uno o più campi non sono validi",
                request.getRequestURI(),
                erroriValidazione
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Errore> gestisciParametroNonValido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String messaggio = "Valore non valido per il parametro '" + ex.getName() + "'";

        Errore errore = new Errore(
                HttpStatus.BAD_REQUEST.value(),
                "Richiesta non valida",
                messaggio,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Errore> gestisciJsonNonLeggibile(HttpMessageNotReadableException ex, HttpServletRequest request) {

        String messaggio = "Corpo della richiesta malformato";
        List<ErroreValidazione> erroriValidazione = new ArrayList<>();

        Throwable causa = ex.getCause();

        if (causa instanceof InvalidFormatException ife) {

            String campo = "sconosciuto";
            var path = ife.getPath();

            if (!path.isEmpty()) {
                var riferimento = path.get(path.size() - 1);
                if (riferimento != null) {
                    campo = riferimento.getPropertyName();
                }
            }

            erroriValidazione.add(new ErroreValidazione(campo, "Valore non valido: " + ife.getValue()));
            messaggio = "Uno o più campi hanno un formato non valido";
        }

        Errore errore = new Errore(
                HttpStatus.BAD_REQUEST.value(),
                "Richiesta non valida",
                messaggio,
                request.getRequestURI(),
                erroriValidazione
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Errore> gestisciVincoloParametro(ConstraintViolationException ex, HttpServletRequest request) {

        List<ErroreValidazione> erroriValidazione = ex.getConstraintViolations().stream()
                .map(v -> new ErroreValidazione(
                        v.getPropertyPath().toString(),
                        v.getMessage()
                ))
                .toList();

        Errore errore = new Errore(
                HttpStatus.BAD_REQUEST.value(),
                "Richiesta non valida",
                "Uno o più parametri non sono validi",
                request.getRequestURI(),
                erroriValidazione
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Errore> gestisciErroreGenerico(Exception ex, HttpServletRequest request) {
        log.error("Errore imprevisto su {}", request.getRequestURI(), ex);

        Errore errore = new Errore(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Errore interno",
                "Si è verificato un errore imprevisto",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errore);
    }
}