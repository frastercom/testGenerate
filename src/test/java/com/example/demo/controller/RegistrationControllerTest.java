package com.example.demo.controller;

import com.example.demo.dto.RegistrationRequest;
import com.example.demo.entity.AppUser;
import com.example.demo.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationController registrationController;

    @Test
    void register_validRequest_returnsCreated() {
        var request = new RegistrationRequest("john", "password123", "John Doe");
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("User registered successfully");

        var captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("john");
        assertThat(captor.getValue().getPassword()).isEqualTo("encodedPass");
        assertThat(captor.getValue().getDisplayName()).isEqualTo("John Doe");
    }

    @Test
    void register_nullUsername_returnsBadRequest() {
        var request = new RegistrationRequest(null, "password123", "John Doe");

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Username is required");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_blankUsername_returnsBadRequest() {
        var request = new RegistrationRequest("  ", "password123", "John Doe");

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Username is required");
    }

    @Test
    void register_nullPassword_returnsBadRequest() {
        var request = new RegistrationRequest("john", null, "John Doe");

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Password must be at least 6 characters");
    }

    @Test
    void register_shortPassword_returnsBadRequest() {
        var request = new RegistrationRequest("john", "12345", "John Doe");

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Password must be at least 6 characters");
    }

    @Test
    void register_nullDisplayName_returnsBadRequest() {
        var request = new RegistrationRequest("john", "password123", null);

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Display name is required");
    }

    @Test
    void register_blankDisplayName_returnsBadRequest() {
        var request = new RegistrationRequest("john", "password123", "  ");

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Display name is required");
    }

    @Test
    void register_duplicateUsername_returnsConflict() {
        var request = new RegistrationRequest("john", "password123", "John Doe");
        when(userRepository.existsByUsername("john")).thenReturn(true);

        var response = registrationController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Username already exists");
        verify(userRepository, never()).save(any());
    }
}
