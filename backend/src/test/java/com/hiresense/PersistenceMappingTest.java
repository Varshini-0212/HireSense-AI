package com.hiresense;

import com.hiresense.domain.AppUser;
import com.hiresense.domain.CandidateProfile;
import com.hiresense.domain.Role;
import com.hiresense.domain.enums.RoleName;
import com.hiresense.repository.AppUserRepository;
import com.hiresense.repository.CandidateProfileRepository;
import com.hiresense.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PersistenceMappingTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Test
    @Transactional
    void persistsUserRoleAndCandidateProfileRelationships() {
        Role candidateRole = new Role();
        candidateRole.setName(RoleName.CANDIDATE);
        candidateRole.setDescription("Job seeker");
        candidateRole = roleRepository.save(candidateRole);

        AppUser user = new AppUser();
        user.setFullName("Test Candidate");
        user.setEmail("test-candidate@example.test");
        user.setPasswordHash("$2a$10$test-only-not-a-real-password-hash");
        user.setActive(true);
        user.setRoles(Set.of(candidateRole));
        user = appUserRepository.saveAndFlush(user);

        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        profile.setLocation("Test City");
        profile.setYearsExperience(new BigDecimal("2.5"));
        candidateProfileRepository.saveAndFlush(profile);

        assertThat(appUserRepository.findByEmailIgnoreCase("TEST-CANDIDATE@example.test"))
                .isPresent()
                .get()
                .satisfies(saved -> assertThat(saved.getRoles()).extracting(Role::getName)
                        .containsExactly(RoleName.CANDIDATE));
        assertThat(candidateProfileRepository.findByUserId(user.getId()))
                .isPresent()
                .get()
                .extracting(CandidateProfile::getLocation)
                .isEqualTo("Test City");
    }
}
