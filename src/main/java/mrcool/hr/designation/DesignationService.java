package mrcool.hr.designation;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import mrcool.hr.common.exception.DuplicateResourceException;
import mrcool.hr.designation.dto.DesignationMapper;
import mrcool.hr.designation.dto.DesignationRequestDTO;
import mrcool.hr.designation.dto.DesignationResponseDTO;

@Service
public class DesignationService {

    private final DesignationRepository designationRepository;

    public DesignationService(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;
    }

    public List<DesignationResponseDTO> getAllDesignations() {
        return designationRepository
                .findAll()
                .stream()
                .map(DesignationMapper::toResponse)
                .toList();
    }

    public DesignationResponseDTO createDesignation(DesignationRequestDTO request) {
        try {
            Designation designation = DesignationMapper.toEntity(request);
            Designation saved = designationRepository.save(designation);
            return DesignationMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Designation with name " + request.name() + " already exists");
        }
    }

}
