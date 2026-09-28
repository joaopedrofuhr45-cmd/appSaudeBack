package com.example.appsaudebackend.Modules.Atendente.Service;
import com.example.appsaudebackend.Modules.Atendente.Dto.Request.AtualizarAtendenteRequestDto; import com.example.appsaudebackend.Modules.Atendente.Dto.Response.AtendentePerfilResponseDto; import com.example.appsaudebackend.Modules.Atendente.Pesistencia.*; import com.example.appsaudebackend.Shared.Exception.*; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor public class AtendentePerfilService {
 private final AtendenteRepository repo;
 @Transactional(readOnly=true) public AtendentePerfilResponseDto obter(String cpf){return toResponse(find(cpf));}
 @Transactional public AtendentePerfilResponseDto atualizar(String cpf,AtualizarAtendenteRequestDto dto){AtendenteModel a=find(cpf); repo.findByEmail(dto.email()).filter(o->!o.getId().equals(a.getId())).ifPresent(o->{throw new ConflitoException("E-mail já cadastrado.");}); a.setNome(dto.nome());a.setEmail(dto.email());return toResponse(repo.save(a));}
 private AtendenteModel find(String cpf){return repo.findByUsuarioAuthCpf(cpf).orElseThrow(()->new RecursoNaoEncontradoException("Atendente não encontrado."));}
 private AtendentePerfilResponseDto toResponse(AtendenteModel a){return new AtendentePerfilResponseDto(a.getNome(),a.getEmail(),a.getSetor());}
}