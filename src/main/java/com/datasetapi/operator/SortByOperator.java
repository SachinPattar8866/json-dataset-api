package com.datasetapi.operator;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class SortByOperator {

    public List<Map<String, Object>> apply(List<Map<String, Object>> records, String field, SortOrder order) {
        Comparator<Object> direction = order == SortOrder.DESC
                ? JsonValueComparator.INSTANCE.reversed()
                : JsonValueComparator.INSTANCE;
        Comparator<Map<String, Object>> comparator =
                Comparator.comparing((Map<String, Object> record) -> record.get(field), Comparator.nullsLast(direction));
        return records.stream().sorted(comparator).toList();
    }
}
