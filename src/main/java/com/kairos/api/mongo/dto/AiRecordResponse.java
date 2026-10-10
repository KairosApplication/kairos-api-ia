package com.kairos.api.mongo.dto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
public record AiRecordResponse(String id,@JsonIgnore Map<String,Object> data) {
    @JsonAnyGetter public Map<String,Object> attributes() { return data; }
}
