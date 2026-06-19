package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResourceController {

    @GetMapping("/home")
    public Map<String, String> home() {
        return Map.of("message", "Welcome to the Home Page!");
    }
}