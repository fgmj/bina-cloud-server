package com.bina.cloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@RestController
@Slf4j
public class HomeController {

    @Value("${project.version:1.0.1}")
    private String version;

    @GetMapping("/api/version")
    public Map<String, String> getVersion() {
        return Map.of("version", version);
    }
}