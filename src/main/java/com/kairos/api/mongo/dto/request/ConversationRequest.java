package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record ConversationRequest(
    @NotNull(groups=OnCreate.class) @Positive Integer userId,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String status,
    @Valid List<MessageRequest> messages,
     String summary,
    @PositiveOrZero Integer summarizedCount,
     Instant createdAt,
     Instant updatedAt,
     Instant summaryUpdatedAt
) { }
