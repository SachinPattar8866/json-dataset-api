package com.datasetapi.service;

import com.datasetapi.dto.InsertRecordResponse;
import com.datasetapi.dto.QueryResponse;
import com.datasetapi.exception.DatasetNotFoundException;
import com.datasetapi.exception.DuplicateRecordException;
import com.datasetapi.exception.InvalidRequestException;
import com.datasetapi.model.DatasetRecord;
import com.datasetapi.operator.GroupByOperator;
import com.datasetapi.operator.SortByOperator;
import com.datasetapi.operator.SortOrder;
import com.datasetapi.repository.DatasetRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DatasetService {

    private static final Pattern DATASET_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,100}$");

    private final DatasetRecordRepository repository;
    private final GroupByOperator groupByOperator;
    private final SortByOperator sortByOperator;

    public DatasetService(DatasetRecordRepository repository,
                          GroupByOperator groupByOperator,
                          SortByOperator sortByOperator) {
        this.repository = repository;
        this.groupByOperator = groupByOperator;
        this.sortByOperator = sortByOperator;
    }

    @Transactional
    public InsertRecordResponse insert(String datasetName, Map<String, Object> record) {
        validateDatasetName(datasetName);
        if (record == null || record.isEmpty()) {
            throw new InvalidRequestException("Record body must be a non-empty JSON object");
        }
        Object id = record.get("id");
        if (!(id instanceof Number || id instanceof String) || id.toString().isBlank()) {
            throw new InvalidRequestException("Record must contain a non-empty 'id' field (number or string)");
        }
        String recordKey = id.toString();
        if (repository.existsByDatasetNameAndRecordKey(datasetName, recordKey)) {
            throw new DuplicateRecordException(datasetName, recordKey);
        }
        repository.save(new DatasetRecord(datasetName, recordKey, record));
        return new InsertRecordResponse("Record added successfully", datasetName, id);
    }

    @Transactional(readOnly = true)
    public QueryResponse query(String datasetName, String groupBy, String sortBy, String order) {
        validateDatasetName(datasetName);
        boolean grouping = groupBy != null && !groupBy.isBlank();
        boolean sorting = sortBy != null && !sortBy.isBlank();
        if (!grouping && !sorting) {
            throw new InvalidRequestException("Provide at least one query parameter: 'groupBy' or 'sortBy'");
        }
        SortOrder sortOrder = SortOrder.from(order);

        List<Map<String, Object>> records = repository.findByDatasetNameOrderByIdAsc(datasetName).stream()
                .map(DatasetRecord::getPayload)
                .toList();
        if (records.isEmpty()) {
            throw new DatasetNotFoundException(datasetName);
        }
        if (grouping) {
            requireFieldExists(records, groupBy);
        }
        if (sorting) {
            requireFieldExists(records, sortBy);
        }

        List<Map<String, Object>> working = sorting ? sortByOperator.apply(records, sortBy, sortOrder) : records;
        // When both are requested, records are sorted first, so each group comes back sorted.
        return grouping
                ? QueryResponse.grouped(groupByOperator.apply(working, groupBy))
                : QueryResponse.sorted(working);
    }

    private void validateDatasetName(String datasetName) {
        if (datasetName == null || !DATASET_NAME.matcher(datasetName).matches()) {
            throw new InvalidRequestException(
                    "Dataset name must be 1-100 characters: letters, digits, '_' or '-'");
        }
    }

    private void requireFieldExists(List<Map<String, Object>> records, String field) {
        boolean present = records.stream().anyMatch(r -> r.containsKey(field));
        if (!present) {
            throw new InvalidRequestException("Field '" + field + "' does not exist in any record of this dataset");
        }
    }
}
