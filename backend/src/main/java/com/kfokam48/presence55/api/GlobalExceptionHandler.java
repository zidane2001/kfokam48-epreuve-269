package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.ApiError;
import com.kfokam48.presence55.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** B4 : toute erreur sort au format impose { code, message }, sans stack trace. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> metier(BusinessException e) {
        return ResponseEntity.status(mapStatut(e.getCode()))
                .body(new ApiError(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .findFirst().orElse("Requete invalide.");
        return ResponseEntity.badRequest().body(new ApiError("CHAMP_MANQUANT", msg));
    }

    /** Issue #24 : collision de contrainte unique (course concurrente) -> 409 propre, jamais 500. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrite(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("DEJA_PRESENT",
                        "Cette presence a deja ete enregistree par une autre requete."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inattendue(Exception e) {
        // B4 : stack trace cote serveur uniquement, le client ne recoit jamais le detail
        log.error("Erreur inattendue", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("ERREUR_INATTENDUE", "Une erreur interne est survenue."));
    }

    private HttpStatus mapStatut(String code) {
        return switch (code) {
            case "CODE_EXPIRE" -> HttpStatus.GONE;                    // 410
            case "DEJA_PRESENT", "EXERCICE_DEJA_DEPOSE",
                 "RELECTURE_DEJA_RENDUE", "SESSION_DEJA_CLOTUREE",
                 "SESSION_DEJA_ACTIVE", "RELECTURE_COMMENCEE",
                 "SESSION_CLOTUREE" -> HttpStatus.CONFLICT;        // 409
            case "AUTO_RELECTURE" -> HttpStatus.FORBIDDEN;            // 403
            case "PROMOTION_INCONNUE", "SESSION_INCONNUE",
                 "ETUDIANT_INCONNU", "EXERCICE_INCONNU",
                 "RELECTURE_INCONNUE" -> HttpStatus.NOT_FOUND;        // 404
            case "RELECTURE_AUTRE_ETUDIANT", "EXERCICE_AUTRE_ETUDIANT" -> HttpStatus.FORBIDDEN; // 403
            case "TROP_DE_TENTATIVES" -> HttpStatus.TOO_MANY_REQUESTS;// 429
            default -> HttpStatus.BAD_REQUEST;                        // 400 (CODE_INCONNU, LIEN_INVALIDE, NOTE_INVALIDE...)
        };
    }
}
