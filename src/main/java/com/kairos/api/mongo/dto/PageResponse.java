package com.kairos.api.mongo.dto;
import java.util.List;
public record PageResponse(List<AiRecordResponse> items,int page,int size,boolean hasNext) { }
