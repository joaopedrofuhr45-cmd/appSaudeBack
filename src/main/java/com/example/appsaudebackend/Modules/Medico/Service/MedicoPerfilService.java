package com.example.appsaudebackend.Modules.Medico.Service;

import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Medico.Dto.Request.AtualizarMedicoRequestDto;
import com.example.appsaudebackend.Modules.Medico.Dto.Request.AlterarSenhaMedicoRequestDto;
import com.example.appsaudebackend.Modules.Medico.Dto.Response.MedicoPerfilResponseDto;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoModel;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import com.example.appsaudebackend.Shared.Exception.RecursoNaoEncontradoException;
import com.example.appsaudebackend.Shared.Exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicoPerfilService {
    private final MedicoRepository repo;
    private final UsuarioAuthRepository authRepository;
    private final AuthAccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MedicoPerfilResponseDto obter(String email) { return toResponse(find(email)); }

    @Transactional
    public MedicoPerfilResponseDto atualizar(String email, AtualizarMedicoRequestDto dto) {
        MedicoModel medico = find(email);
        String novoEmail = accountService.normalizar(dto.email());
        accountService.garantirEmailDisponivel(novoEmail, medico.getUsuarioAuth().getId());
        medico.setNome(dto.nome());
        medico.setEmail(novoEmail);
        medico.setTelefone(dto.telefone());
        medico.getUsuarioAuth().setEmail(novoEmail);
        authRepository.save(medico.getUsuarioAuth());
        return toResponse(repo.save(medico));
    }

    @Transactional
    public void alterarSenha(String email, AlterarSenhaMedicoRequestDto dto) {
        MedicoModel medico = find(email);
        var auth = medico.getUsuarioAuth();
        if (!passwordEncoder.matches(dto.senhaAtual(), auth.getSenha())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
        if (passwordEncoder.matches(dto.novaSenha(), auth.getSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da senha atual.");
        }
        auth.setSenha(passwordEncoder.encode(dto.novaSenha()));
        authRepository.save(auth);
    }

    private MedicoModel find(String email) {
        return repo.findByUsuarioAuthEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
    }

    private MedicoPerfilResponseDto toResponse(MedicoModel m) {
        return new MedicoPerfilResponseDto(m.getNome(), m.getEmail(), m.getTelefone(), m.getCrm(), m.getEspecialidade());
    }
}
