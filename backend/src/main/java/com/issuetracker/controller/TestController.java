package com.issuetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller for debugging purposes.
 */
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World!");
    }

    @PostMapping("/echo")
    public ResponseEntity<String> echo(@RequestBody String message) {
        return ResponseEntity.ok("Echo: " + message);
    }
}