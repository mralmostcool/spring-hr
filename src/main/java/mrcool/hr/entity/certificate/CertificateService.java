package mrcool.hr.entity.certificate;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateStateRepository certificateStateRepository;
    private final CertificateActionRepository certificateActionRepository;

}
