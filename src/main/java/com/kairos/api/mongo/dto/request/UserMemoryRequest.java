package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record UserMemoryRequest(
    @NotNull(groups=OnCreate.class) @Positive Integer userId,
    @Valid List<MemoryRequest> memories,
     Instant updatedAt
) { }
