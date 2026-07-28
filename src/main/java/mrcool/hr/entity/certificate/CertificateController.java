package mrcool.hr.entity.certificate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    public void createNewCertificate() {
    }

    @GetMapping
    public void allCertificates() {
    }

    @GetMapping("/{id}")
    public void specificCertificate() {
    }

    @PutMapping("/{id}/accept")
    public void acceptCertificate() {
    }

    @PutMapping("/{id}/reject")
    public void rejectCertificate() {
    }

}