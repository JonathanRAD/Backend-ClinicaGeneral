// RUTA PROPUESTA: src/main/java/com/clinicabienestar/api/exception/ForbiddenException.java
package com.clinicabienestar.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}