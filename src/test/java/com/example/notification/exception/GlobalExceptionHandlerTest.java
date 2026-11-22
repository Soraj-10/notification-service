package com.example.notification.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void responseStatusException_keepsItsStatusAndMessage() {
        ResponseEntity<Map<String, Object>> response = handler.other(
                new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already exists", response.getBody().get("message"));
    }

    @Test
    void unknownException_returns500() {
        ResponseEntity<Map<String, Object>> response =
                handler.other(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody().get("message"));
    }
}