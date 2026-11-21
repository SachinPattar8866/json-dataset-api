package com.datasetapi.controller;

import com.datasetapi.dto.InsertRecordResponse;
import com.datasetapi.dto.QueryResponse;
import com.datasetapi.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.datasetapi.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Map;

@RestController
@RequestMapping("/api/dataset/{datasetName}")
@Tag(name = "Dataset", description = "Insert JSON records and query them with group-by / sort-by")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @PostMapping("/record")
    @Operation(summary = "Insert a JSON record into a dataset",
            description = "The record must contain an 'id' field, unique within the dataset.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Record added"),
            @ApiResponse(responseCode = "400", description = "Invalid body or dataset name",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate id in this dataset",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InsertRecordResponse> insertRecord(@PathVariable String datasetName,
                                                             @RequestBody Map<String, Object> record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insert(datasetName, record));
    }

    @GetMapping("/query")
    @Operation(summary = "Query a dataset with group-by and/or sort-by",
            description = "Use groupBy, sortBy (with optional order=asc|desc), or both. With both, records are sorted first and then grouped.")

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grouped and/or sorted records"),
            @ApiResponse(responseCode = "400", description = "Missing params, bad order, or unknown field",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Dataset has no records",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    public QueryResponse query(@PathVariable String datasetName,
                               @Parameter(description = "Field to group by, e.g. department")
                               @RequestParam(required = false) String groupBy,
                               @Parameter(description = "Field to sort by, e.g. age")
                               @RequestParam(required = false) String sortBy,
                               @Parameter(description = "asc (default) or desc")
                               @RequestParam(required = false) String order) {
        return service.query(datasetName, groupBy, sortBy, order);
    }
}
