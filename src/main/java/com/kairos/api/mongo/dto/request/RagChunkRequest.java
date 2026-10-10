package com.kairos.api.mongo.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record RagChunkRequest(
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") @Pattern(regexp="[a-fA-F0-9]{24}") String documentId,
    @NotNull(groups=OnCreate.class) @PositiveOrZero Integer chunkIndex,
    @NotNull(groups=OnCreate.class) @Pattern(regexp="(?s).*\\S.*") String content,
    @NotNull(groups=OnCreate.class) List<Double> embedding
) { }
