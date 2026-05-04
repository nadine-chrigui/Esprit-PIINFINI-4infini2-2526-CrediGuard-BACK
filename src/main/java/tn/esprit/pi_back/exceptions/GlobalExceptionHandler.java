package tn.esprit.pi_back.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        System.out.println("=== ERREUR DE VALIDATION ===");
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            System.out.println("Champ: " + fe.getField() + " | Erreur: " + fe.getDefaultMessage());
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        System.out.println("Erreurs complètes: " + errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        System.out.println("=== RUNTIME EXCEPTION ===");
        System.out.println("Message: " + ex.getMessage());
        ex.printStackTrace();
        String message = ex.getMessage() != null ? ex.getMessage() : "Runtime error occurred";
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        System.out.println("=== EXCEPTION GENERALE ===");
        System.out.println("Message: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> forbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "FORBIDDEN",
                "message", ex.getMessage()
        ));
    }
}
