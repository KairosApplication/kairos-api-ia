package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record JudgeEvaluationRequest(
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") @Pattern(regexp="[a-fA-F0-9]{24}") String executionId,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String judgeAgent,
    @NotNull(groups=OnCreate.class) @DecimalMin("0.0") @DecimalMax("1.0") Double score,
    Boolean grounded,
    Boolean hallucinationDetected,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String decision,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String justification,
     Instant createdAt
) { }
