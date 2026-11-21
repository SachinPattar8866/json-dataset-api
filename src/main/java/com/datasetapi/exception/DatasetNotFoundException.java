package com.datasetapi.exception;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(String datasetName) {
        super("Dataset '" + datasetName + "' does not exist or has no records");
    }
}
