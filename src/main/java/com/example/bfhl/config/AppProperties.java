package com.example.bfhl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.*;

@Component
@ConfigurationProperties(prefix = "bfhl")
@Getter @Setter
public class AppProperties {
    private User user = new User();
    private Endpoints endpoints = new Endpoints();
    private Storage storage = new Storage();

    @Getter @Setter
    public static class User { private String name; private String regNo; private String email; }

    @Getter @Setter
    public static class Endpoints { private String generate; private String submit; }

    @Getter @Setter
    public static class Storage { private String finalQueryFile; }
}
