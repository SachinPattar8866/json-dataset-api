package com.datasetapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QueryResponse(Map<String, List<Map<String, Object>>> groupedRecords,
                            List<Map<String, Object>> sortedRecords) {

    public static QueryResponse grouped(Map<String, List<Map<String, Object>>> groups) {
        return new QueryResponse(groups, null);
    }

    public static QueryResponse sorted(List<Map<String, Object>> records) {
        return new QueryResponse(null, records);
    }
}
