package mrcool.hr.designation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mrcool.hr.designation.dto.DesignationRequestDTO;
import mrcool.hr.designation.dto.DesignationResponseDTO;

@RestController
@RequestMapping("/api/v1/designations")
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(DesignationService designationService) {
        this.designationService = designationService;
    }

    @GetMapping
    public ResponseEntity<List<DesignationResponseDTO>> getAll() {
        return ResponseEntity.ok(designationService.getAllDesignations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignationResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(designationService.getDesignationById(id));
    }

    @PostMapping
    public ResponseEntity<DesignationResponseDTO> create(@Valid @RequestBody DesignationRequestDTO request) {
        DesignationResponseDTO created = designationService.createDesignation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignationResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody DesignationRequestDTO request) {
        DesignationResponseDTO updated = designationService.updateDesignation(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DesignationResponseDTO> deleteById(@PathVariable UUID id) {
        DesignationResponseDTO response = designationService.deleteDesignation(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/all")
    public ResponseEntity<List<DesignationResponseDTO>> deleteAll() {
        List<DesignationResponseDTO> deleted = designationService.deleteAllDesignations();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(deleted);
    }

}
