package com.example.bfhl.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GenerateWebhookRequest {
    private String name;
    private String regNo;
    private String email;
}
