package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record GuardrailEventRequest(
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") @Pattern(regexp="[a-fA-F0-9]{24}") String executionId,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String guardrailType,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String action,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String reason,
     String category,
     Instant createdAt
) { }
