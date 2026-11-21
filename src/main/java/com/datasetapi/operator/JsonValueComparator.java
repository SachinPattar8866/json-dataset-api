package com.datasetapi.operator;

import java.math.BigDecimal;
import java.util.Comparator;

 //Compares JSON scalar values numbers numerically (so 9 < 10), strings case-insensitively
 // booleans false < true. Mixed types are ordered by type (number, boolean, string, other)

public final class JsonValueComparator implements Comparator<Object> {

    public static final JsonValueComparator INSTANCE = new JsonValueComparator();

    private JsonValueComparator() {
    }

    @Override
    public int compare(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) {
            return new BigDecimal(x.toString()).compareTo(new BigDecimal(y.toString()));
        }
        if (a instanceof Boolean x && b instanceof Boolean y) {
            return Boolean.compare(x, y);
        }
        int rankA = rank(a);
        int rankB = rank(b);
        if (rankA != rankB) {
            return Integer.compare(rankA, rankB);
        }
        return a.toString().compareToIgnoreCase(b.toString());
    }

    private int rank(Object value) {
        if (value instanceof Number) return 0;
        if (value instanceof Boolean) return 1;
        if (value instanceof String) return 2;
        return 3;
    }
}
