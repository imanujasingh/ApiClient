package com.scb.API.Client.Controller;

import com.scb.API.Client.dto.EncryptionResponse;
import com.scb.API.Client.Service.EncryptionApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final EncryptionApiClient encryptionApiClient;

    /**
     * Encrypt sensitive fields in the provided JSON data
     */
    @PostMapping("/encrypt")
    public ResponseEntity<EncryptionResponse> encryptData(@RequestBody Map<String, Object> data) {
        log.info("Received encrypt request for data: {}", data);
        try {
            EncryptionResponse response = encryptionApiClient.encryptData(data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            EncryptionResponse errorResponse = new EncryptionResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("Encryption failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Decrypt encrypted fields in the provided JSON data
     */
    @PostMapping("/decrypt")
    public ResponseEntity<EncryptionResponse> decryptData(@RequestBody Map<String, Object> encryptedData) {
        log.info("Received decrypt request");
        try {
            EncryptionResponse response = encryptionApiClient.decryptData(encryptedData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            EncryptionResponse errorResponse = new EncryptionResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("Decryption failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get the list of fields that will be encrypted
     */
    @GetMapping("/fields/encryptable")
    public ResponseEntity<EncryptionResponse> getEncryptableFields() {
        log.info("Fetching encryptable fields");
        try {
            EncryptionResponse response = encryptionApiClient.getEncryptableFields();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get encryptable fields: {}", e.getMessage());
            EncryptionResponse errorResponse = new EncryptionResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("Failed to get encryptable fields: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check - verifies connection to encryption API
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Performing health check");
        try {
            String healthStatus = encryptionApiClient.healthCheck();
            return ResponseEntity.ok("✅ Client API Healthy | Encryption API: " + healthStatus);
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("❌ Health check failed: " + e.getMessage());
        }
    }

    /**
     * Encrypt a single field value
     */
    @PostMapping("/encrypt/field")
    public ResponseEntity<Map<String, Object>> encryptField(
            @RequestParam String fieldName,
            @RequestParam String value) {
        log.info("Encrypting field: {}", fieldName);
        try {
            String encryptedValue = encryptionApiClient.encryptField(fieldName, value);
            return ResponseEntity.ok(Map.of(
                    "field", fieldName,
                    "originalValue", value,
                    "encryptedValue", encryptedValue,
                    "status", "success"
            ));
        } catch (Exception e) {
            log.error("Field encryption failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * Decrypt a single field value
     */
    @PostMapping("/decrypt/field")
    public ResponseEntity<Map<String, Object>> decryptField(
            @RequestParam String fieldName,
            @RequestParam String encryptedValue) {
        log.info("Decrypting field: {}", fieldName);
        try {
            String decryptedValue = encryptionApiClient.decryptField(fieldName, encryptedValue);
            return ResponseEntity.ok(Map.of(
                    "field", fieldName,
                    "encryptedValue", encryptedValue,
                    "decryptedValue", decryptedValue,
                    "status", "success"
            ));
        } catch (Exception e) {
            log.error("Field decryption failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
