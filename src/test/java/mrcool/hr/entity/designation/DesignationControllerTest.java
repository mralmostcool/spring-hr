package mrcool.hr.entity.designation;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import mrcool.hr.entity.designation.dto.DesignationRequestDTO;
import mrcool.hr.entity.designation.dto.DesignationReorderRequestDTO;
import mrcool.hr.entity.employee.Employee;
import mrcool.hr.entity.employee.EmployeeRepository;
import mrcool.hr.entity.certificate.CertificateActionRepository;
import mrcool.hr.entity.certificate.CertificateStateRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DesignationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CertificateActionRepository certificateActionRepository;

    @Autowired
    private CertificateStateRepository certificateStateRepository;

    @Autowired
    private EntityManager entityManager;

    @org.junit.jupiter.api.BeforeEach
    void cleanWorkflowTables() {
        certificateActionRepository.deleteAll();
        certificateStateRepository.deleteAll();
        employeeRepository.deleteAll();
        designationRepository.deleteAll();
    }

    @Test
    void testGetAllDesignations() throws Exception {
        employeeRepository.deleteAll();
        designationRepository.deleteAll();

        Designation des1 = designationRepository.saveAndFlush(Designation.builder().name("Manager").build());
        Designation des2 = designationRepository.saveAndFlush(Designation.builder().name("Developer").build());

        mockMvc.perform(get("/api/v1/designations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", isOneOf("Manager", "Developer")))
                .andExpect(jsonPath("$[1].name", isOneOf("Manager", "Developer")));
    }

    @Test
    void testGetDesignationByIdSuccess() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Tester").build());

        mockMvc.perform(get("/api/v1/designations/{id}", des.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(des.getId().toString())))
                .andExpect(jsonPath("$.name", is("Tester")));
    }

    @Test
    void testGetDesignationByIdNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/designations/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Designation with ID " + randomId + " not found")));
    }

    @Test
    void testCreateDesignationSuccess() throws Exception {
        DesignationRequestDTO request = new DesignationRequestDTO("Architect");

        mockMvc.perform(post("/api/v1/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Architect")));
    }

    @Test
    void testCreateDesignationAutoIncrementRank() throws Exception {
        employeeRepository.deleteAll();
        designationRepository.deleteAll();

        // 1st designation with null rank -> gets rank 1
        DesignationRequestDTO request1 = new DesignationRequestDTO("Role X");
        mockMvc.perform(post("/api/v1/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rank", is(1)));

        // 2nd designation with null rank -> gets rank 2 (max 1 + 1)
        DesignationRequestDTO request2 = new DesignationRequestDTO("Role Y");
        mockMvc.perform(post("/api/v1/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rank", is(2)));

        // 3rd designation with explicit rank 10 -> gets rank 10
        DesignationRequestDTO request3 = new DesignationRequestDTO("Role Z", 10);
        mockMvc.perform(post("/api/v1/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rank", is(10)));

        // 4th designation with null rank -> gets rank 11 (max 10 + 1)
        DesignationRequestDTO request4 = new DesignationRequestDTO("Role W");
        mockMvc.perform(post("/api/v1/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rank", is(11)));
    }

    @Test
    void testCreateDesignationDuplicateName() throws Exception {
        designationRepository.saveAndFlush(Designation.builder().name("Duplicate").build());
        DesignationRequestDTO request = new DesignationRequestDTO("Duplicate");

        mockMvc.perform(post("/api/v1/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Designation with name Duplicate already exists")));
    }

    @Test
    void testCreateDesignationInvalidRequest() throws Exception {
        DesignationRequestDTO request = new DesignationRequestDTO(""); // Invalid blank name

        mockMvc.perform(post("/api/v1/designations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateDesignationSuccess() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Lead").build());
        DesignationRequestDTO request = new DesignationRequestDTO("Tech Lead");

        mockMvc.perform(put("/api/v1/designations/{id}", des.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(des.getId().toString())))
                .andExpect(jsonPath("$.name", is("Tech Lead")));
    }

    @Test
    void testUpdateDesignationRankRules() throws Exception {
        employeeRepository.deleteAllInBatch();
        designationRepository.deleteAllInBatch();

        // 1. If request.rank() is not null, update it to requested rank
        Designation des1 = designationRepository.saveAndFlush(Designation.builder().name("Manager").rank(5).build());
        DesignationRequestDTO req1 = new DesignationRequestDTO("Manager Updated", 8);
        mockMvc.perform(put("/api/v1/designations/{id}", des1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank", is(8)));

        // 2. If request.rank() is null, but designation has rank, preserve it
        DesignationRequestDTO req2 = new DesignationRequestDTO("Manager Preserved", null);
        mockMvc.perform(put("/api/v1/designations/{id}", des1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Manager Preserved")))
                .andExpect(jsonPath("$.rank", is(8)));

        // 3. If request.rank() is null and designation has no rank yet, auto-assign
        Designation des2 = designationRepository.saveAndFlush(Designation.builder().name("Developer").build()); // rank is null
        DesignationRequestDTO req3 = new DesignationRequestDTO("Developer Updated", null);
        mockMvc.perform(put("/api/v1/designations/{id}", des2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank", is(9))); // max rank was 8, so 8 + 1 = 9
    }

    @Test
    void testUpdateDesignationNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        DesignationRequestDTO request = new DesignationRequestDTO("New Name");

        mockMvc.perform(put("/api/v1/designations/{id}", randomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDesignationDuplicateName() throws Exception {
        Designation des1 = designationRepository.saveAndFlush(Designation.builder().name("Engineer").build());
        designationRepository.saveAndFlush(Designation.builder().name("Senior Engineer").build());
        DesignationRequestDTO request = new DesignationRequestDTO("Senior Engineer");

        mockMvc.perform(put("/api/v1/designations/{id}", des1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testDeleteDesignationSuccess() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Intern").build());

        mockMvc.perform(delete("/api/v1/designations/{id}", des.getId()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(des.getId().toString())))
                .andExpect(jsonPath("$.name", is("Intern")));

        assertFalse(designationRepository.existsById(des.getId()));
    }

    @Test
    void testDeleteDesignationNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/designations/{id}", randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDesignationAssignedToEmployee() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("DBA").build());
        employeeRepository.saveAndFlush(Employee.builder().name("John Doe").designation(des).build());

        entityManager.clear();

        mockMvc.perform(delete("/api/v1/designations/{id}", des.getId()))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Cannot delete designation with ID " + des.getId())));
    }

    @Test
    void testDeleteAllDesignations() throws Exception {
        employeeRepository.deleteAll();
        designationRepository.deleteAll();
        Designation des1 = designationRepository.saveAndFlush(Designation.builder().name("Role A").build());
        Designation des2 = designationRepository.saveAndFlush(Designation.builder().name("Role B").build());

        mockMvc.perform(delete("/api/v1/designations/all"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", isOneOf("Role A", "Role B")))
                .andExpect(jsonPath("$[1].name", isOneOf("Role A", "Role B")));

        assertFalse(designationRepository.existsById(des1.getId()));
        assertFalse(designationRepository.existsById(des2.getId()));
    }

    @Test
    void testDeleteAllDesignationsWithEmployees() throws Exception {
        employeeRepository.deleteAll();
        designationRepository.deleteAll();
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Role A").build());
        employeeRepository.saveAndFlush(Employee.builder().name("John").designation(des).build());

        mockMvc.perform(delete("/api/v1/designations/all"))
                .andExpect(status().isAccepted());

        assertFalse(designationRepository.existsById(des.getId()));
        assertFalse(employeeRepository.findAll().iterator().hasNext());
    }

    @Test
    void testReorderDesignations() throws Exception {
        employeeRepository.deleteAllInBatch();
        designationRepository.deleteAllInBatch();

        Designation des1 = designationRepository.saveAndFlush(Designation.builder().name("Role A").rank(1).build());
        Designation des2 = designationRepository.saveAndFlush(Designation.builder().name("Role B").rank(2).build());

        java.util.List<DesignationReorderRequestDTO> request = java.util.List.of(
                new DesignationReorderRequestDTO(des1.getId(), 2),
                new DesignationReorderRequestDTO(des2.getId(), 1)
        );

        mockMvc.perform(put("/api/v1/designations/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(des2.getId().toString())))
                .andExpect(jsonPath("$[0].rank", is(1)))
                .andExpect(jsonPath("$[1].id", is(des1.getId().toString())))
                .andExpect(jsonPath("$[1].rank", is(2)));
    }
}

