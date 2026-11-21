package com.datasetapi.operator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupByOperatorTest {

    private final GroupByOperator operator = new GroupByOperator();

    @Test
    void groupsRecordsByFieldValue() {
        var a = Map.<String, Object>of("id", 1, "department", "Engineering");
        var b = Map.<String, Object>of("id", 2, "department", "Marketing");
        var c = Map.<String, Object>of("id", 3, "department", "Engineering");

        Map<String, List<Map<String, Object>>> grouped = operator.apply(List.of(a, b, c), "department");

        assertEquals(2, grouped.size());
        assertEquals(List.of(a, c), grouped.get("Engineering"));
        assertEquals(List.of(b), grouped.get("Marketing"));
    }

    @Test
    void recordsMissingTheFieldGoIntoNullGroup() {
        var a = Map.<String, Object>of("id", 1);
        Map<String, List<Map<String, Object>>> grouped = operator.apply(List.of(a), "department");
        assertTrue(grouped.containsKey("null"));
    }
}
