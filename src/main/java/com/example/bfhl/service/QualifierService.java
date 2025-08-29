package com.example.bfhl.service;

import com.example.bfhl.config.AppProperties;
import com.example.bfhl.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class QualifierService {

    private static final Logger log = LoggerFactory.getLogger(QualifierService.class);
    private final RestTemplate restTemplate;
    private final AppProperties props;

    public void run() throws Exception {
        // 1) call generateWebhook
        var reqBody = GenerateWebhookRequest.builder()
                .name(props.getUser().getName())
                .regNo(props.getUser().getRegNo())
                .email(props.getUser().getEmail())
                .build();

        log.info("Calling generateWebhook -> {}", props.getEndpoints().getGenerate());
        ResponseEntity<GenerateWebhookResponse> genResp =
                restTemplate.postForEntity(props.getEndpoints().getGenerate(), reqBody, GenerateWebhookResponse.class);

        if (!genResp.getStatusCode().is2xxSuccessful() || genResp.getBody() == null) {
            throw new IllegalStateException("Failed to generate webhook: " + genResp.getStatusCode());
        }

        String webhook = genResp.getBody().getWebhook();
        String accessToken = genResp.getBody().getAccessToken();
        log.info("Received webhook: {}", webhook);
        log.info("Received accessToken (JWT): {}", accessToken != null ? "[REDACTED]" : null);

        // 2) decide which SQL question link you should open (odd/even of last two digits of regNo)
        Integer lastTwo = extractLastTwoDigits(props.getUser().getRegNo());
        String assigned = (lastTwo % 2 == 1) ? "Question 1 (ODD)" : "Question 2 (EVEN)";
        // As per PDF, links:
        // ODD -> https://drive.google.com/file/d/1IeSI6l6KoSQAFfRihIT9tEDICtoz-G_/view?usp=sharing
        // EVEN -> https://drive.google.com/file/d/143MR5cLFrlNEuHzzWJ5RHnEW_uijuM9X/view?usp=sharing
        log.info("regNo last two digits = {} → Assigned {}", lastTwo, assigned);

        // 3) Read your final SQL from a local file (store the result)
        //    Paste your final SQL into the file defined by bfhl.storage.finalQueryFile
        Path sqlPath = Path.of(props.getStorage().getFinalQueryFile());
        if (!Files.exists(sqlPath)) {
            Files.writeString(sqlPath, "-- Paste your final SQL here and re-run the app\n");
            log.warn("Created {}. Paste your SQL and run again.", sqlPath.toAbsolutePath());
            return; // first run just creates the file for you
        }
        String finalSql = Files.readString(sqlPath).trim();
        if (finalSql.isEmpty() || finalSql.startsWith("--")) {
            log.error("final_query.sql is empty. Paste your SQL and run again.");
            return;
        }
        log.info("Loaded final SQL from {} ({} chars)", sqlPath.toAbsolutePath(), finalSql.length());

        // 4) POST final SQL to the returned webhook using JWT in Authorization header
        FinalQueryRequest finalReq = FinalQueryRequest.builder().finalQuery(finalSql).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // The PDF says to use the JWT token returned as the Authorization header value (no 'Bearer' prefix mentioned).
        headers.set("Authorization", accessToken);

        HttpEntity<FinalQueryRequest> entity = new HttpEntity<>(finalReq, headers);

        log.info("Submitting final query to {}", props.getEndpoints().getSubmit());
        ResponseEntity<String> submitResp =
                restTemplate.exchange(props.getEndpoints().getSubmit(), HttpMethod.POST, entity, String.class);

        log.info("Submission response: {} - {}", submitResp.getStatusCode(), submitResp.getBody());
    }

    private Integer extractLastTwoDigits(String regNo) {
        // take trailing digits and compute last two
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.length() >= 2) {
            return Integer.parseInt(digits.substring(digits.length() - 2));
        } else if (!digits.isEmpty()) {
            return Integer.parseInt(digits);
        } else {
            throw new IllegalArgumentException("regNo has no digits: " + regNo);
        }
    }
}
