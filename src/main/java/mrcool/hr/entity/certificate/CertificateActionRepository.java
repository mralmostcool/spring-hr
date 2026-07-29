package mrcool.hr.entity.certificate;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateActionRepository extends JpaRepository<CertificateAction, UUID> {

    List<CertificateAction> findAllByCertificateStateIdOrderByCreatedAtAsc(UUID certificateStateId);

}

