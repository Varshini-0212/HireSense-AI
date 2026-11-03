package com.hiresense.controller;

import com.hiresense.domain.AppUser;
import com.hiresense.domain.enums.RoleName;
import com.hiresense.dto.AuthDtos.UserResponse;
import com.hiresense.exception.ApiException;
import com.hiresense.repository.AppUserRepository;
import com.hiresense.repository.AttritionPredictionRepository;
import com.hiresense.repository.EmployeeRepository;
import com.hiresense.repository.JobRepository;
import com.hiresense.repository.MatchResultRepository;
import com.hiresense.repository.RoleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AppUserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final EmployeeRepository employees;
    private final JobRepository jobs;
    private final MatchResultRepository matches;
    private final AttritionPredictionRepository predictions;

    public AdminController(AppUserRepository users, RoleRepository roles, PasswordEncoder encoder,
                           EmployeeRepository employees, JobRepository jobs, MatchResultRepository matches,
                           AttritionPredictionRepository predictions) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.employees = employees;
        this.jobs = jobs;
        this.matches = matches;
        this.predictions = predictions;
    }

    @GetMapping("/users")
    public Page<UserResponse> users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return users.findAll(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))).map(this::map);
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminUserRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().toLowerCase(Locale.ROOT));
        user.setPasswordHash(encoder.encode(request.password()));
        user.setActive(true);
        RoleName roleName;
        try { roleName = RoleName.valueOf(request.role().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw ApiException.bad("Unknown role"); }
        user.setRoles(Set.of(roles.findByName(roleName).orElseThrow(() -> ApiException.bad("Unknown role"))));
        return ResponseEntity.status(HttpStatus.CREATED).body(map(users.save(user)));
    }

    @PatchMapping("/users/{id}/active")
    public UserResponse active(@PathVariable Long id, @RequestParam boolean value) {
        AppUser user = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        user.setActive(value);
        return map(users.save(user));
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of("totalUsers", users.count(),
                "activeUsers", users.findAllByActive(true, PageRequest.of(0, 1)).getTotalElements(),
                "employees", employees.count(), "jobs", jobs.count(),
                "matchAnalyses", matches.count(), "attritionPredictions", predictions.count());
    }

    private UserResponse map(AppUser user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.isActive(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()));
    }

    public record AdminUserRequest(@NotBlank @Size(max = 120) String fullName,
                                   @NotBlank @Email String email,
                                   @NotBlank @Size(min = 8, max = 100) String password,
                                   @NotBlank String role) {}
}
