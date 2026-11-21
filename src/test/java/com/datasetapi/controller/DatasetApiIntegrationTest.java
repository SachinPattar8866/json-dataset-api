package com.datasetapi.controller;

import com.datasetapi.repository.DatasetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DatasetApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatasetRecordRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private void insert(String json) throws Exception {
        mockMvc.perform(post("/api/dataset/emp/record").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    private void seed() throws Exception {
        insert("{\"id\":1,\"name\":\"John Doe\",\"age\":30,\"department\":\"Engineering\"}");
        insert("{\"id\":2,\"name\":\"Jane Smith\",\"age\":25,\"department\":\"Engineering\"}");
        insert("{\"id\":3,\"name\":\"Alice Brown\",\"age\":28,\"department\":\"Marketing\"}");
    }

    @Test
    void insertReturnsCreatedWithMessage() throws Exception {
        mockMvc.perform(post("/api/dataset/emp/record").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"John Doe\",\"age\":30,\"department\":\"Engineering\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record added successfully"))
                .andExpect(jsonPath("$.dataset").value("emp"))
                .andExpect(jsonPath("$.recordId").value(1));
    }

    @Test
    void duplicateIdReturnsConflict() throws Exception {
        insert("{\"id\":1,\"name\":\"John\"}");
        mockMvc.perform(post("/api/dataset/emp/record").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Again\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void sameIdInDifferentDatasetsIsAllowed() throws Exception {
        insert("{\"id\":1,\"name\":\"John\"}");
        mockMvc.perform(post("/api/dataset/other/record").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"John\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void missingIdReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/dataset/emp/record").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/dataset/emp/record").contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void groupByReturnsGroupedRecords() throws Exception {
        seed();
        mockMvc.perform(get("/api/dataset/emp/query").param("groupBy", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Engineering.length()").value(2))
                .andExpect(jsonPath("$.groupedRecords.Marketing.length()").value(1))
                .andExpect(jsonPath("$.sortedRecords").doesNotExist());
    }

    @Test
    void sortByAscendingReturnsSortedRecords() throws Exception {
        seed();
        mockMvc.perform(get("/api/dataset/emp/query").param("sortBy", "age").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords[0].name").value("Jane Smith"))
                .andExpect(jsonPath("$.sortedRecords[1].name").value("Alice Brown"))
                .andExpect(jsonPath("$.sortedRecords[2].name").value("John Doe"));
    }

    @Test
    void sortByDescendingReturnsReversedOrder() throws Exception {
        seed();
        mockMvc.perform(get("/api/dataset/emp/query").param("sortBy", "age").param("order", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords[0].name").value("John Doe"));
    }

    @Test
    void invalidOrderReturnsBadRequest() throws Exception {
        seed();
        mockMvc.perform(get("/api/dataset/emp/query").param("sortBy", "age").param("order", "up"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownFieldReturnsBadRequest() throws Exception {
        seed();
        mockMvc.perform(get("/api/dataset/emp/query").param("groupBy", "salary"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noQueryParamsReturnsBadRequest() throws Exception {
        seed();
        mockMvc.perform(get("/api/dataset/emp/query"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownDatasetReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/dataset/ghost/query").param("groupBy", "department"))
                .andExpect(status().isNotFound());
    }
}
