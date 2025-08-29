package com.example.bfhl;

import com.example.bfhl.service.QualifierService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BfhlApplication {

    public static void main(String[] args) {
        SpringApplication.run(BfhlApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(QualifierService service) {
        // Triggers the whole flow on startup (no controller).
        return args -> service.run();
    }
}
