package mrcool.hr.entity.certificate;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateStateRepository extends JpaRepository<CertificateState, UUID> {

}
