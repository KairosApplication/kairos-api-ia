package com.kairos.api.mongo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** Internal chatbot operations; caller identity is supplied by the trusted backend. */
public final class RuntimeRequests {
    private RuntimeRequests() { }
    public record Turn(@NotBlank @Size(max=64) String turnId,
                       @NotBlank @Size(max=200000) String question,
                       @NotBlank @Size(max=200000) String answer) { }
    public record Summary(@NotBlank @Size(max=200000) String summary,
                          @Size(max=200000) String previousSummary,
                          @NotNull @PositiveOrZero Integer previousCount,
                          @NotNull @Positive Integer summarizedCount) { }
    public record Memories(@NotEmpty @Size(max=100) List<@NotNull @Valid MemoryRequest> memories) { }
    public record Lease(@NotBlank @Size(max=64) String owner) { }
}
