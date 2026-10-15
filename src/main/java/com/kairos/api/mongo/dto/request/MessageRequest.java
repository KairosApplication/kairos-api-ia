package com.kairos.api.mongo.dto.request;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
public record MessageRequest(String messageId, @NotBlank String sender, @NotBlank String content, Instant timestamp) { }
