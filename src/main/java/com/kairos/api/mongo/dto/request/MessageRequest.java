package com.kairos.api.mongo.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
public record MessageRequest(String messageId, @NotBlank String sender, @NotBlank String content, Instant timestamp,
                             @Size(max=3) List<@NotNull @Pattern(regexp="pdf|database|memory") String> sources) { }
