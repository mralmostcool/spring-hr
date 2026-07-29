package mrcool.hr.entity.certificate;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mrcool.hr.entity.certificate.dto.CertificateActionRequestDTO;
import mrcool.hr.entity.certificate.dto.CertificateRequestDTO;
import mrcool.hr.entity.certificate.dto.CertificateResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    public ResponseEntity<CertificateResponseDTO> createNewCertificate(@Valid @RequestBody CertificateRequestDTO request) {
        CertificateResponseDTO created = certificateService.createNewCertificate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CertificateResponseDTO>> allCertificates() {
        return ResponseEntity.ok(certificateService.getAllCertificates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponseDTO> specificCertificate(@PathVariable UUID id) {
        return ResponseEntity.ok(certificateService.getSpecificCertificate(id));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<CertificateResponseDTO> acceptCertificate(
            @PathVariable UUID id,
            @Valid @RequestBody CertificateActionRequestDTO actionRequest) {
        return ResponseEntity.ok(certificateService.acceptCertificate(id, actionRequest));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<CertificateResponseDTO> rejectCertificate(
            @PathVariable UUID id,
            @Valid @RequestBody CertificateActionRequestDTO actionRequest) {
        return ResponseEntity.ok(certificateService.rejectCertificate(id, actionRequest));
    }

}