package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record AgentTraceRequest(
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") @Pattern(regexp="[a-fA-F0-9]{24}") String executionId,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String agentName,
    @NotNull(groups=OnCreate.class) Map<String,Object> input,
    @NotNull(groups=OnCreate.class) Map<String,Object> output,
     Instant startedAt,
     Instant completedAt,
    @NotNull(groups=OnCreate.class) @PositiveOrZero Integer latencyMs,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String status,
     String model,
    @PositiveOrZero Integer estimatedTokens,
    @PositiveOrZero Double estimatedCostUsd
) { }
