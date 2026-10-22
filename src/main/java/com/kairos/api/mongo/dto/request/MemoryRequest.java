package com.kairos.api.mongo.dto.request;
import jakarta.validation.constraints.*;
import java.time.Instant;
public record MemoryRequest(@Pattern(regexp="[a-fA-F0-9]{24}") String memoryId, @NotBlank String type,
        @NotBlank String content, @NotNull @PositiveOrZero Double importance, Instant createdAt, Instant updatedAt, @Pattern(regexp="[a-f0-9]{64}") String accessScope) { }
