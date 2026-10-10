package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record RagDocumentRequest(
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String title,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String description,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String sourceType,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String sourceUrl,
    @NotNull(groups=OnCreate.class) Boolean active,
     Instant createdAt
) { }
