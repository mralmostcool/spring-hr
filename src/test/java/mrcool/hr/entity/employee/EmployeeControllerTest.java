package mrcool.hr.entity.employee;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import mrcool.hr.entity.designation.Designation;
import mrcool.hr.entity.designation.DesignationRepository;
import mrcool.hr.entity.employee.dto.EmployeeRequestDTO;
import mrcool.hr.entity.certificate.CertificateActionRepository;
import mrcool.hr.entity.certificate.CertificateStateRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private CertificateActionRepository certificateActionRepository;

    @Autowired
    private CertificateStateRepository certificateStateRepository;

    @org.junit.jupiter.api.BeforeEach
    void cleanWorkflowTables() {
        certificateActionRepository.deleteAll();
        certificateStateRepository.deleteAll();
        employeeRepository.deleteAll();
        designationRepository.deleteAll();
    }

    @Test
    void testGetAllEmployees() throws Exception {
        employeeRepository.deleteAll();

        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Manager").build());
        employeeRepository.saveAndFlush(Employee.builder().name("Alice").designation(des).build());
        employeeRepository.saveAndFlush(Employee.builder().name("Bob").designation(des).build());

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", isOneOf("Alice", "Bob")))
                .andExpect(jsonPath("$[1].name", isOneOf("Alice", "Bob")))
                .andExpect(jsonPath("$[0].designation.name", is("Manager")));
    }

    @Test
    void testGetEmployeeByIdSuccess() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("HR Specialist").build());
        Employee emp = employeeRepository.saveAndFlush(Employee.builder().name("Charlie").designation(des).build());

        mockMvc.perform(get("/api/v1/employees/{id}", emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(emp.getId().toString())))
                .andExpect(jsonPath("$.name", is("Charlie")))
                .andExpect(jsonPath("$.designation.id", is(des.getId().toString())));
    }

    @Test
    void testGetEmployeeByIdNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/employees/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Employee with ID " + randomId + " not found")));
    }

    @Test
    void testCreateEmployeeSuccess() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Consultant").build());
        EmployeeRequestDTO request = new EmployeeRequestDTO("David", des.getId());

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("David")))
                .andExpect(jsonPath("$.designation.id", is(des.getId().toString())));
    }

    @Test
    void testCreateEmployeeDesignationNotFound() throws Exception {
        UUID randomDesignationId = UUID.randomUUID();
        EmployeeRequestDTO request = new EmployeeRequestDTO("Eve", randomDesignationId);

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(
                        content().string(containsString("Designation with ID " + randomDesignationId + " not found")));
    }

    @Test
    void testCreateEmployeeInvalidRequest() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Lead").build());
        EmployeeRequestDTO request = new EmployeeRequestDTO("", des.getId()); // Blank name

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateEmployeeSuccess() throws Exception {
        Designation des1 = designationRepository.saveAndFlush(Designation.builder().name("Junior").build());
        Designation des2 = designationRepository.saveAndFlush(Designation.builder().name("Senior").build());
        Employee emp = employeeRepository.saveAndFlush(Employee.builder().name("Frank").designation(des1).build());

        EmployeeRequestDTO request = new EmployeeRequestDTO("Frank Updated", des2.getId());

        mockMvc.perform(put("/api/v1/employees/{id}", emp.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(emp.getId().toString())))
                .andExpect(jsonPath("$.name", is("Frank Updated")))
                .andExpect(jsonPath("$.designation.id", is(des2.getId().toString())));
    }

    @Test
    void testUpdateEmployeeNotFound() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Junior").build());
        UUID randomId = UUID.randomUUID();
        EmployeeRequestDTO request = new EmployeeRequestDTO("No Body", des.getId());

        mockMvc.perform(put("/api/v1/employees/{id}", randomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateEmployeeDesignationNotFound() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Junior").build());
        Employee emp = employeeRepository.saveAndFlush(Employee.builder().name("Grace").designation(des).build());
        UUID randomDesignationId = UUID.randomUUID();
        EmployeeRequestDTO request = new EmployeeRequestDTO("Grace", randomDesignationId);

        mockMvc.perform(put("/api/v1/employees/{id}", emp.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEmployeeSuccess() throws Exception {
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Intern").build());
        Employee emp = employeeRepository.saveAndFlush(Employee.builder().name("Heidi").designation(des).build());

        mockMvc.perform(delete("/api/v1/employees/{id}", emp.getId()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(emp.getId().toString())))
                .andExpect(jsonPath("$.name", is("Heidi")));

        assertFalse(employeeRepository.findById(emp.getId()).map(Employee::getIsActive).orElse(true));
    }

    @Test
    void testDeleteEmployeeNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/employees/{id}", randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAllEmployees() throws Exception {
        employeeRepository.deleteAll();
        Designation des = designationRepository.saveAndFlush(Designation.builder().name("Developer").build());
        Employee emp1 = employeeRepository.saveAndFlush(Employee.builder().name("Ivan").designation(des).build());
        Employee emp2 = employeeRepository.saveAndFlush(Employee.builder().name("Judy").designation(des).build());

        mockMvc.perform(delete("/api/v1/employees/all"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", isOneOf("Ivan", "Judy")))
                .andExpect(jsonPath("$[1].name", isOneOf("Ivan", "Judy")));

        assertFalse(employeeRepository.findById(emp1.getId()).map(Employee::getIsActive).orElse(true));
        assertFalse(employeeRepository.findById(emp2.getId()).map(Employee::getIsActive).orElse(true));
    }
}
