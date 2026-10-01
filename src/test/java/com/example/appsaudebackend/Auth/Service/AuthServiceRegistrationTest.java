package com.example.appsaudebackend.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Dto.Request.CadastroPacienteDto;
import com.example.appsaudebackend.Modules.Auth.Model.*;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.*;
import com.example.appsaudebackend.Shared.Exception.ConflitoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistrationTest {
 @Mock UsuarioAuthRepository authRepo; @Mock UsuarioRepository userRepo; @Mock PasswordEncoder encoder;
 @InjectMocks AuthService service;
 private CadastroPacienteDto dto(){var d=new CadastroPacienteDto();d.setNome("Ana");d.setEmail("ana@example.com");d.setCpf("123");d.setTelefone("999");d.setSenha("secret1");return d;}
 @Test void cadastraPacienteEArmazenaSenhaCodificada(){var d=dto();when(authRepo.findByCpf("123")).thenReturn(Optional.empty());when(userRepo.findByEmail("ana@example.com")).thenReturn(Optional.empty());when(encoder.encode("secret1")).thenReturn("hash");service.cadastrarPaciente(d);var captor=org.mockito.ArgumentCaptor.forClass(UsuarioAuth.class);verify(authRepo).save(captor.capture());assertEquals("hash",captor.getValue().getSenha());assertEquals(Role.USUARIO,captor.getValue().getRole());var uc=org.mockito.ArgumentCaptor.forClass(UsuarioModel.class);verify(userRepo).save(uc.capture());assertEquals("Ana",uc.getValue().getNome());assertSame(captor.getValue(),uc.getValue().getUsuarioAuth());}
 @Test void rejeitaCpfDuplicadoSemPersistir(){when(authRepo.findByCpf("123")).thenReturn(Optional.of(new UsuarioAuth()));assertThrows(ConflitoException.class,()->service.cadastrarPaciente(dto()));verify(authRepo,never()).save(any());verify(userRepo,never()).save(any());}
 @Test void rejeitaEmailDuplicadoSemPersistir(){when(authRepo.findByCpf("123")).thenReturn(Optional.empty());when(userRepo.findByEmail("ana@example.com")).thenReturn(Optional.of(new UsuarioModel()));assertThrows(ConflitoException.class,()->service.cadastrarPaciente(dto()));verify(authRepo,never()).save(any());verify(userRepo,never()).save(any());}
}
