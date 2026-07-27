package mrcool.hr.designation;

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
import mrcool.hr.designation.dto.DesignationRequestDTO;
import mrcool.hr.employee.Employee;
import mrcool.hr.employee.EmployeeRepository;

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
    private EntityManager entityManager;

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
}
