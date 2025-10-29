package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.exception.ResourceNotFoundException; // Importar
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest; // Importar

import java.util.Date; // Importar
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // --- MANEJADORES ESPECÍFICOS ---

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        // Mensaje personalizado para el frontend
        return new ResponseEntity<>(Map.of("message", "Usuario o contraseña incorrectos."), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, String>> handleLockedException(LockedException ex) {
         // Mensaje personalizado para el frontend
        return new ResponseEntity<>(Map.of("message", "La cuenta está bloqueada temporalmente debido a múltiples intentos fallidos."), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date()); // Añadir timestamp
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Error de validación."); // Mensaje más genérico
        body.put("errors", errors); // Detalles de los campos

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Devuelve el mensaje específico de la excepción (ej. "Token inválido", "Contraseña insegura")
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    // --- NUEVO: MANEJADOR PARA ResourceNotFoundException ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        // Loguear internamente si es necesario
         System.err.println("Recurso no encontrado: " + ex.getMessage() + " en " + request.getDescription(false));
        // Devuelve un mensaje genérico al cliente
        return new ResponseEntity<>(Map.of("message", "El recurso solicitado no fue encontrado."), HttpStatus.NOT_FOUND);
        // NOTA: Para requestPasswordReset, el Controller lo captura antes y devuelve OK,
        // este handler serviría para otros casos donde ResourceNotFound sí deba ser 404.
    }


    // --- NUEVO: MANEJADOR GENÉRICO PARA CUALQUIER OTRA EXCEPCIÓN ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllOtherExceptions(Exception ex, WebRequest request) {
        // Loguear el stack trace completo para depuración interna
        ex.printStackTrace(); // ¡Importante para ver qué pasó realmente!

        // Devuelve un mensaje genérico al cliente para no exponer detalles internos
        return new ResponseEntity<>(Map.of("message", "Ocurrió un error interno inesperado en el servidor."), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // Clase interna para detalles del error (Opcional, si prefieres un formato consistente)
    /*
    public static class ErrorDetails {
        private Date timestamp;
        private String message;
        private String details;
        // Constructor, Getters...
    }
    */
}