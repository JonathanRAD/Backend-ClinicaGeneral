// src/main/java/com/clinicabienestar/api/service/SelfPingService.java
package com.clinicabienestar.api.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SelfPingService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Se ejecuta cada 10 minutos
    @Scheduled(fixedRate = 600000)
    public void ping() {
        try {
            String url = "https://backend-clinicageneral-ek58.onrender.com/health";
            restTemplate.getForObject(url, String.class);
            System.out.println("Self-ping OK");
        } catch (Exception e) {
            System.err.println("Self-ping falló: " + e.getMessage());
        }
    }
}