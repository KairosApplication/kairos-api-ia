package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record FeedbackRequest(
    @NotNull(groups=OnCreate.class) @Positive Integer userId,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") @Pattern(regexp="[a-fA-F0-9]{24}") String executionId,
    @NotNull(groups=OnCreate.class) @Min(1) @Max(5) Integer rating,
     String comment,
     Instant createdAt
) { }
