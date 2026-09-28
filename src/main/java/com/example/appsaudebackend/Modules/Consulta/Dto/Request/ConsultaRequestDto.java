package com.example.appsaudebackend.Modules.Consulta.Dto.Request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaRequestDto {

    // só é usado quando quem cria é o atendente
    @Positive(message = "Paciente inválido")
    private Long pacienteId;

    @NotNull(message = "Médico é obrigatório")
    @Positive(message = "Médico inválido")
    private Long medicoId;

    @NotBlank(message = "Tipo de consulta é obrigatório")
    @Size(max = 100, message = "Tipo de consulta deve ter no máximo 100 caracteres")
    private String tipoConsulta;

    @NotNull(message = "Data e horário são obrigatórios")
    @Future(message = "Data e horário devem ser no futuro")
    private LocalDateTime dataHora;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;
}