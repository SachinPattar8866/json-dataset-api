package com.datasetapi.operator;

import com.datasetapi.exception.InvalidRequestException;

public enum SortOrder {
    ASC, DESC;

    public static SortOrder from(String value) {
        if (value == null || value.isBlank()) {
            return ASC;
        }
        if (value.equalsIgnoreCase("asc")) {
            return ASC;
        }
        if (value.equalsIgnoreCase("desc")) {
            return DESC;
        }
        throw new InvalidRequestException("Invalid order '" + value + "'. Allowed values: asc, desc");
    }
}
