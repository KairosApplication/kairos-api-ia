package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record AiExecutionRequest(
    @NotNull(groups=OnCreate.class) @Positive Integer userId,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") @Pattern(regexp="[a-fA-F0-9]{24}") String conversationId,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String input,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String status,
     List<String> agentsUsed,
     Instant startedAt,
     Instant completedAt,
     String finalResponse,
    @PositiveOrZero Integer totalLatencyMs,
    @PositiveOrZero Double estimatedCostUsd,
    @PositiveOrZero Double knownCostUsd,
    Boolean costComplete
) { }
