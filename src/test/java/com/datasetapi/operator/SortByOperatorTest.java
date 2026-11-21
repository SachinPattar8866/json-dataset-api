package com.datasetapi.operator;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortByOperatorTest {

    private final SortByOperator operator = new SortByOperator();

    private Map<String, Object> rec(Object id, Object age) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        if (age != null) {
            m.put("age", age);
        }
        return m;
    }

    @Test
    void sortsNumbersNumericallyNotLexicographically() {
        List<Map<String, Object>> sorted = operator.apply(
                List.of(rec(1, 10), rec(2, 9), rec(3, 100)), "age", SortOrder.ASC);
        assertEquals(List.of(2, 1, 3), sorted.stream().map(r -> r.get("id")).toList());
    }

    @Test
    void sortsDescending() {
        List<Map<String, Object>> sorted = operator.apply(
                List.of(rec(1, 30), rec(2, 25), rec(3, 28)), "age", SortOrder.DESC);
        assertEquals(List.of(1, 3, 2), sorted.stream().map(r -> r.get("id")).toList());
    }

    @Test
    void recordsWithoutFieldAreLastInBothDirections() {
        List<Map<String, Object>> records = List.of(rec(1, null), rec(2, 5), rec(3, 1));
        assertEquals(List.of(3, 2, 1),
                operator.apply(records, "age", SortOrder.ASC).stream().map(r -> r.get("id")).toList());
        assertEquals(List.of(2, 3, 1),
                operator.apply(records, "age", SortOrder.DESC).stream().map(r -> r.get("id")).toList());
    }

    @Test
    void sortsStringsCaseInsensitively() {
        List<Map<String, Object>> sorted = operator.apply(
                List.of(Map.of("name", "bob"), Map.of("name", "Alice"), Map.of("name", "carol")),
                "name", SortOrder.ASC);
        assertEquals(List.of("Alice", "bob", "carol"), sorted.stream().map(r -> r.get("name")).toList());
    }
}
