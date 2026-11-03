package com.hiresense.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
public final class AuthDtos {
 private AuthDtos() {}
 public record RegisterRequest(@NotBlank @Size(max=120) String fullName,@NotBlank @Email @Size(max=190) String email,@NotBlank @Size(min=8,max=100) String password) {}
 public record LoginRequest(@NotBlank @Email String email,@NotBlank String password) {}
 public record AuthResponse(String token,long expiresIn,UserResponse user) {}
 public record UserResponse(Long id,String fullName,String email,boolean active,Set<String> roles) {}
}
