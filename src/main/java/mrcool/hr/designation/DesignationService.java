package mrcool.hr.designation;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import mrcool.hr.common.exception.DuplicateResourceException;
import mrcool.hr.common.exception.ResourceNotFoundException;
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

    public DesignationResponseDTO getDesignationById(UUID id) {
        return designationRepository.findById(id)
                .map(DesignationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + id + " not found"));
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

    public DesignationResponseDTO updateDesignation(UUID id, DesignationRequestDTO request) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + id + " not found"));

        designation.setName(request.name());
        try {
            Designation updated = designationRepository.saveAndFlush(designation);
            return DesignationMapper.toResponse(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Designation with name " + request.name() + " already exists");
        }
    }

    public DesignationResponseDTO deleteDesignation(UUID id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + id + " not found"));
        try {
            designationRepository.deleteById(id);
            designationRepository.flush();
            return DesignationMapper.toResponse(designation);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Cannot delete designation with ID " + id
                    + " because it is currently assigned to one or more employees.");
        }
    }

    public List<DesignationResponseDTO> deleteAllDesignations() {
        List<DesignationResponseDTO> designations = getAllDesignations();
        designationRepository.deleteAll();
        return designations;
    }

}
