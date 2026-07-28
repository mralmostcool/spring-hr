package mrcool.hr.entity.candidate;

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
import mrcool.hr.entity.candidate.dto.CandidateRequestDTO;
import mrcool.hr.entity.candidate.dto.CandidateResponseDTO;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponseDTO>> getAll() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @PostMapping
    public ResponseEntity<CandidateResponseDTO> create(@Valid @RequestBody CandidateRequestDTO request) {
        CandidateResponseDTO created = candidateService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody CandidateRequestDTO request) {
        CandidateResponseDTO updated = candidateService.updateCandidate(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> deleteById(@PathVariable UUID id) {
        CandidateResponseDTO response = candidateService.deleteCandidate(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/all")
    public ResponseEntity<List<CandidateResponseDTO>> deleteAll() {
        List<CandidateResponseDTO> deleted = candidateService.deleteAllCandidates();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(deleted);
    }

}
