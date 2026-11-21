package com.datasetapi.operator;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Groups records by the value of a field, keeping the incoming order inside and across groups
@Component
public class GroupByOperator {

    public Map<String, List<Map<String, Object>>> apply(List<Map<String, Object>> records, String field) {
        return records.stream().collect(Collectors.groupingBy(
                record -> String.valueOf(record.get(field)),
                LinkedHashMap::new,
                Collectors.toList()));
    }
}
