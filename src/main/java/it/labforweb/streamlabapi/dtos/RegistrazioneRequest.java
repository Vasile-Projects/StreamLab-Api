package it.labforweb.streamlabapi.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrazioneRequest(

        @NotBlank(message = "Il nome è obbligatorio")
        @Size(min = 1, max = 50, message = "Il nome deve avere tra 1 e 50 caratteri")
        @Pattern(regexp = "^[\\p{L}' -]+$", message = "Il nome non può contenere numeri o caratteri speciali")
        String nome,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(min = 1, max = 100, message = "Il cognome deve avere tra 1 e 100 caratteri")
        @Pattern(regexp = "^[\\p{L}' -]+$", message = "Il cognome non può contenere numeri o caratteri speciali")
        String cognome,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Formato email non valido")
        @Size(max = 100)
        String email,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 6, max = 18, message = "La password deve avere tra 6 e 18 caratteri")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "La password deve contenere almeno una lettera maiuscola, un numero e un carattere speciale"
        )
        String password,

        @NotNull(message = "L'abbonamento è obbligatorio")
        Integer tipoAbbonamentoId
) {}