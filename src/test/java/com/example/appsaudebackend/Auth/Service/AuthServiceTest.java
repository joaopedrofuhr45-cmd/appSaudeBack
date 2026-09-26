package com.example.appsaudebackend.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import com.example.appsaudebackend.Modules.Auth.Service.JwtService;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {
        // Arrange
        // Usuario usuario = ...
        // when(usuarioRepository.findByEmail(...)).thenReturn(Optional.of(usuario));
        // when(passwordEncoder.matches(...)).thenReturn(true);
        // when(jwtService.generateToken(...)).thenReturn("token");

        // Act
        // var resultado = authService.login(...);

        // Assert
        // assertNotNull(resultado);
        // assertEquals("token", resultado.token());

        // verify(usuarioRepository).findByEmail(...);
        // verify(passwordEncoder).matches(...);
        // verify(jwtService).generateToken(...);
    }

    @Test
    void deveRecusarCredenciaisInvalidas() {
        // Arrange
        // ...

        // Act + Assert
        // assertThrows(...);
    }
}