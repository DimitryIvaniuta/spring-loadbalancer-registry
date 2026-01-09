package com.github.dimitryivaniuta.loadbalancer.demo.exception;

import com.github.dimitryivaniuta.loadbalancer.exceptions.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DuplicateAddressException.class, CapacityExceededException.class, InstanceNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> badRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getClass().getSimpleName(),
                "message", ex.getMessage()
        ));
    }
}
