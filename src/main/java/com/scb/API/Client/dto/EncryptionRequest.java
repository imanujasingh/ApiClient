package com.scb.API.Client.dto;

import lombok.Data;
import java.util.Map;

@Data
public class EncryptionRequest {
    private Map<String, Object> data;
}
