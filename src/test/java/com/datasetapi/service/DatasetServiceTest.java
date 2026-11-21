package com.datasetapi.service;

import com.datasetapi.dto.InsertRecordResponse;
import com.datasetapi.dto.QueryResponse;
import com.datasetapi.exception.DatasetNotFoundException;
import com.datasetapi.exception.DuplicateRecordException;
import com.datasetapi.exception.InvalidRequestException;
import com.datasetapi.model.DatasetRecord;
import com.datasetapi.operator.GroupByOperator;
import com.datasetapi.operator.SortByOperator;
import com.datasetapi.repository.DatasetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock
    private DatasetRecordRepository repository;

    private DatasetService service;

    @BeforeEach
    void setUp() {
        service = new DatasetService(repository, new GroupByOperator(), new SortByOperator());
    }

    private DatasetRecord record(int id, String name, int age, String dept) {
        return new DatasetRecord("emp", String.valueOf(id),
                Map.of("id", id, "name", name, "age", age, "department", dept));
    }

    @Test
    void insertSavesRecordAndReturnsResponse() {
        when(repository.existsByDatasetNameAndRecordKey("emp", "1")).thenReturn(false);

        InsertRecordResponse response = service.insert("emp", Map.of("id", 1, "name", "John"));

        assertEquals("Record added successfully", response.message());
        assertEquals("emp", response.dataset());
        assertEquals(1, response.recordId());
        verify(repository).save(any(DatasetRecord.class));
    }

    @Test
    void insertRejectsDuplicateId() {
        when(repository.existsByDatasetNameAndRecordKey("emp", "1")).thenReturn(true);

        assertThrows(DuplicateRecordException.class, () -> service.insert("emp", Map.of("id", 1)));
        verify(repository, never()).save(any());
    }

    @Test
    void insertRejectsMissingId() {
        assertThrows(InvalidRequestException.class, () -> service.insert("emp", Map.of("name", "John")));
    }

    @Test
    void insertRejectsEmptyBody() {
        assertThrows(InvalidRequestException.class, () -> service.insert("emp", Map.of()));
    }

    @Test
    void rejectsInvalidDatasetName() {
        assertThrows(InvalidRequestException.class, () -> service.insert("bad name!", Map.of("id", 1)));
    }

    @Test
    void queryRequiresGroupByOrSortBy() {
        assertThrows(InvalidRequestException.class, () -> service.query("emp", null, null, null));
    }

    @Test
    void queryOnUnknownDatasetThrowsNotFound() {
        when(repository.findByDatasetNameOrderByIdAsc("ghost")).thenReturn(List.of());
        assertThrows(DatasetNotFoundException.class, () -> service.query("ghost", "department", null, null));
    }

    @Test
    void queryRejectsUnknownField() {
        when(repository.findByDatasetNameOrderByIdAsc("emp"))
                .thenReturn(List.of(record(1, "John", 30, "Engineering")));
        assertThrows(InvalidRequestException.class, () -> service.query("emp", "salary", null, null));
    }

    @Test
    void queryRejectsInvalidOrder() {
        assertThrows(InvalidRequestException.class, () -> service.query("emp", null, "age", "sideways"));
    }

    @Test
    void queryGroupsAndSortsWithinGroups() {
        when(repository.findByDatasetNameOrderByIdAsc("emp")).thenReturn(List.of(
                record(1, "John", 30, "Engineering"),
                record(2, "Jane", 25, "Engineering"),
                record(3, "Alice", 28, "Marketing")));

        QueryResponse response = service.query("emp", "department", "age", "asc");

        assertNull(response.sortedRecords());
        List<Map<String, Object>> engineering = response.groupedRecords().get("Engineering");
        assertEquals(List.of(2, 1), engineering.stream().map(r -> r.get("id")).toList());
    }
}
