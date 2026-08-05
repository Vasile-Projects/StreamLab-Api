package it.labforweb.streamlabapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(

        @NotBlank(message = "La password attuale è obbligatoria")
        String vecchiaPassword,

        @NotBlank(message = "La nuova password è obbligatoria")
        @Size(min = 6, max = 18, message = "La password deve avere tra 6 e 18 caratteri")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "La password deve contenere almeno una lettera maiuscola, un numero e un carattere speciale"
        )
        String nuovaPassword
) {}