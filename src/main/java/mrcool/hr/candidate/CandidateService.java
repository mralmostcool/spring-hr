package mrcool.hr.candidate;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mrcool.hr.common.exception.ResourceNotFoundException;
import mrcool.hr.candidate.dto.CandidateMapper;
import mrcool.hr.candidate.dto.CandidateRequestDTO;
import mrcool.hr.candidate.dto.CandidateResponseDTO;

@Service
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional(readOnly = true)
    public List<CandidateResponseDTO> getAllCandidates() {
        return candidateRepository.findAll()
                .stream()
                .map(CandidateMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CandidateResponseDTO getCandidateById(UUID id) {
        return candidateRepository.findById(id)
                .map(CandidateMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate with ID " + id + " not found"));
    }

    public CandidateResponseDTO createCandidate(CandidateRequestDTO request) {
        Candidate candidate = CandidateMapper.toEntity(request);
        Candidate saved = candidateRepository.saveAndFlush(candidate);
        return CandidateMapper.toResponse(saved);
    }

    public CandidateResponseDTO updateCandidate(UUID id, CandidateRequestDTO request) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate with ID " + id + " not found"));

        candidate.setName(request.name());
        Candidate updated = candidateRepository.saveAndFlush(candidate);
        return CandidateMapper.toResponse(updated);
    }

    public CandidateResponseDTO deleteCandidate(UUID id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate with ID " + id + " not found"));

        candidateRepository.deleteById(id);
        candidateRepository.flush();
        return CandidateMapper.toResponse(candidate);
    }

    public List<CandidateResponseDTO> deleteAllCandidates() {
        List<CandidateResponseDTO> candidates = getAllCandidates();
        candidateRepository.deleteAll();
        return candidates;
    }
}
