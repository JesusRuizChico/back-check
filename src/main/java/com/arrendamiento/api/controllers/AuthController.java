package com.arrendamiento.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> userData) {
        // En un proyecto real, se procesaría con Firebase Firestore aquí o usando FirestoreService
        return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente en backend (Spring Boot)"));
    }
}
