package com.hiresense;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresense.domain.AppUser;
import com.hiresense.domain.Role;
import com.hiresense.domain.enums.RoleName;
import com.hiresense.repository.AppUserRepository;
import com.hiresense.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RoleRepository roles;
    @Autowired AppUserRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedRoles() {
        for (RoleName name : RoleName.values()) {
            if (roles.findByName(name).isEmpty()) {
                Role role = new Role();
                role.setName(name);
                role.setDescription(name.name());
                roles.save(role);
            }
        }
    }

    @Test
    void candidateCanRegisterLoginReadProfileAndViewEmptyJobs() throws Exception {
        String email = "candidate." + System.nanoTime() + "@hiresense.test";
        String register = "{\"fullName\":\"Integration Candidate\",\"email\":\"" + email + "\",\"password\":\"StrongPassword123!\"}";
        String created = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.roles[0]").value("CANDIDATE"))
                .andReturn().getResponse().getContentAsString();
        JsonNode createdJson = objectMapper.readTree(created);
        String token = createdJson.get("token").asText();

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"StrongPassword123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(get("/api/profile/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Integration Candidate"));
        mvc.perform(get("/api/jobs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mvc.perform(get("/api/jobs/recommended").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void candidateCannotAccessEmployeeDirectory() throws Exception {
        mvc.perform(get("/api/employees").with(user("candidate").roles("CANDIDATE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanCreateJobWithRequiredAndPreferredSkills() throws Exception {
        String email = "hr.integration@hiresense.test";
        AppUser hr = new AppUser();
        hr.setFullName("Integration HR");
        hr.setEmail(email);
        hr.setPasswordHash(passwordEncoder.encode("StrongPassword123!"));
        hr.setActive(true);
        hr.setRoles(java.util.Set.of(roles.findByName(RoleName.HR).orElseThrow()));
        users.save(hr);

        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"StrongPassword123!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("token").asText();
        String job = "{\"title\":\"Integration Backend Engineer\",\"description\":\"Build reliable services\","
                + "\"minExperienceYears\":2,\"location\":\"Remote\",\"active\":true,"
                + "\"requiredSkills\":[\"Spring Boot\",\"Kotlin\"],\"preferredSkills\":[\"Docker\"]}";
        mvc.perform(post("/api/jobs").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(job))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills.length()").value(3))
                .andExpect(jsonPath("$.skills[0].name").value("Spring Boot"))
                .andExpect(jsonPath("$.skills[0].type").value("REQUIRED"))
                .andExpect(jsonPath("$.skills[2].type").value("PREFERRED"));
    }
}
