package com.datasetapi.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "dataset_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_dataset_record", columnNames = {"dataset_name", "record_key"}),
        indexes = @Index(name = "idx_dataset_name", columnList = "dataset_name"))
public class DatasetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_name", nullable = false, length = 100)
    private String datasetName;

    // Value of the "id" field inside the JSON record (unique per dataset).
    @Column(name = "record_key", nullable = false)
    private String recordKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> payload;

    protected DatasetRecord() {
    }

    public DatasetRecord(String datasetName, String recordKey, Map<String, Object> payload) {
        this.datasetName = datasetName;
        this.recordKey = recordKey;
        this.payload = payload;
    }

    public Long getId() { return id; }
    public String getDatasetName() { return datasetName; }
    public String getRecordKey() { return recordKey; }
    public Map<String, Object> getPayload() { return payload; }
}
