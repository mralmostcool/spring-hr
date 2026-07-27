package mrcool.hr.designation;

import java.io.ObjectInputFilter.Status;
import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mrcool.hr.designation.dto.DesignationMapper;
import mrcool.hr.designation.dto.DesignationRequestDTO;
import mrcool.hr.designation.dto.DesignationResponseDTO;

@RestController
@RequestMapping("/api/v1/designations")
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(DesignationService designationService) {
        this.designationService = designationService;
    }

    /**
     * Funcationality we provide
     * -------------------------------------------
     * GET /designations -> List<DesignationResponse>
     * POST /designations -> @RequestBody DesignationRequest -> DesignationResponse
     * PUT /designations/{id} -> @RequestBody DesignationRequest ->
     * DesignationResponse
     * DELETE /designations/{id} -> no body needed, no DTO needed
     * -------------------------------------------
     */

    @GetMapping
    public ResponseEntity<List<DesignationResponseDTO>> getAll() {
        return ResponseEntity.ok(designationService.getAllDesignations());
    }

    @PostMapping
    public ResponseEntity<DesignationResponseDTO> create(@Valid @RequestBody DesignationRequestDTO request) {
        DesignationResponseDTO created = designationService.createDesignation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

}
