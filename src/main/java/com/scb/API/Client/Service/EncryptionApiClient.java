package com.scb.API.Client.Service;



import com.scb.API.Client.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class EncryptionApiClient {

    private final WebClient webClient;

    @Value("${encryption.api.base-url}")
    private String baseUrl;

    @Value("${encryption.api.encrypt-endpoint}")
    private String encryptEndpoint;

    @Value("${encryption.api.decrypt-endpoint}")
    private String decryptEndpoint;

    @Value("${encryption.api.fields-endpoint}")
    private String fieldsEndpoint;

    @Value("${encryption.api.health-endpoint}")
    private String healthEndpoint;

    @Value("${encryption.api.connect-timeout:5000}")
    private long connectTimeout;

    @Value("${encryption.api.read-timeout:10000}")
    private long readTimeout;

    public EncryptionApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Encrypt sensitive fields in the provided data
     */
    public EncryptionResponse encryptData(Map<String, Object> data) {
        log.info("Sending encryption request for data: {}", data);

        EncryptionRequest request = new EncryptionRequest();
        request.setData(data);

        try {
            return webClient.post()
                    .uri(baseUrl + encryptEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Encryption API error: " + error)))
                    )
                    .bodyToMono(EncryptionResponse.class)
                    .timeout(Duration.ofMillis(readTimeout))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Encryption API returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Encryption failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to call encryption API: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to encryption service: " + e.getMessage());
        }
    }

    /**
     * Decrypt encrypted fields in the provided data
     */
    public EncryptionResponse decryptData(Map<String, Object> encryptedData) {
        log.info("Sending decryption request");

        EncryptionRequest request = new EncryptionRequest();
        request.setData(encryptedData);

        try {
            return webClient.post()
                    .uri(baseUrl + decryptEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Decryption API error: " + error)))
                    )
                    .bodyToMono(EncryptionResponse.class)
                    .timeout(Duration.ofMillis(readTimeout))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Decryption API returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Decryption failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to call decryption API: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to decryption service: " + e.getMessage());
        }
    }

    /**
     * Get the list of fields that will be encrypted
     */
    public EncryptionResponse getEncryptableFields() {
        log.info("Fetching encryptable fields");

        try {
            return webClient.get()
                    .uri(baseUrl + fieldsEndpoint)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Fields API error: " + error)))
                    )
                    .bodyToMono(EncryptionResponse.class)
                    .timeout(Duration.ofMillis(connectTimeout))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Fields API returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get encryptable fields: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to call fields API: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to fields service: " + e.getMessage());
        }
    }

    /**
     * Check if the encryption API is healthy
     */
    public String healthCheck() {
        log.info("Performing health check");

        try {
            return webClient.get()
                    .uri(baseUrl + healthEndpoint)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Health check failed: " + error)))
                    )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(connectTimeout))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Health check failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Encryption API health check failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to perform health check: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to encryption service for health check: " + e.getMessage());
        }
    }

    /**
     * Helper method to encrypt a single field
     */
    public String encryptField(String fieldName, String value) {
        Map<String, Object> data = Map.of(fieldName, value);
        EncryptionResponse response = encryptData(data);
        return (String) response.getData().get(fieldName);
    }

    /**
     * Helper method to decrypt a single field
     */
    public String decryptField(String fieldName, String encryptedValue) {
        Map<String, Object> data = Map.of(fieldName, encryptedValue);
        EncryptionResponse response = decryptData(data);
        return (String) response.getData().get(fieldName);
    }
}
