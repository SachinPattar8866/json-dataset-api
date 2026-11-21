package com.datasetapi.exception;

public class DuplicateRecordException extends RuntimeException {
    public DuplicateRecordException(String datasetName, String recordKey) {
        super("Record with id '" + recordKey + "' already exists in dataset '" + datasetName + "'");
    }
}
