package com.example.appsaudebackend.Modules.Medico.Service;

import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Medico.Dto.Request.AtualizarMedicoRequestDto;
import com.example.appsaudebackend.Modules.Medico.Dto.Response.MedicoPerfilResponseDto;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoModel;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import com.example.appsaudebackend.Shared.Exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicoPerfilService {
    private final MedicoRepository repo;
    private final UsuarioAuthRepository authRepository;
    private final AuthAccountService accountService;

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

    private MedicoModel find(String email) {
        return repo.findByUsuarioAuthEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
    }

    private MedicoPerfilResponseDto toResponse(MedicoModel m) {
        return new MedicoPerfilResponseDto(m.getNome(), m.getEmail(), m.getTelefone(), m.getCrm(), m.getEspecialidade());
    }
}
