package com.scb.API.Client.dto;

import lombok.Data;
import java.util.Map;

@Data
public class EncryptionResponse {
    private String status;
    private String message;
    private Map<String, Object> data;
    private Object encryptedFields;
}
