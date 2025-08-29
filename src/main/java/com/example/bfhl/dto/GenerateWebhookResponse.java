package com.example.bfhl.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GenerateWebhookResponse {
    private String webhook;      // returned URL where we submit final SQL
    private String accessToken;  // JWT for Authorization header
}
