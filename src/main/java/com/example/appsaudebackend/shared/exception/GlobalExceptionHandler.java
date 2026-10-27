package com.example.appsaudebackend.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> recursoNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.NOT_FOUND,
                "RECURSO_NAO_ENCONTRADO",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> regraNegocio(
            RegraNegocioException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "REGRA_DE_NEGOCIO",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorResponse> conflito(
            ConflitoException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.CONFLICT,
                "CONFLITO",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> credenciaisInvalidas(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.UNAUTHORIZED,
                "CREDENCIAIS_INVALIDAS",
                "E-mail ou senha inválidos.",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String mensagem = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dados inválidos.");

        return responder(
                HttpStatus.BAD_REQUEST,
                "DADOS_INVALIDOS",
                mensagem,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> violacaoDeRestricao(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "DADOS_INVALIDOS",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<ErrorResponse> muitasTentativas(MuitasTentativasException exception, HttpServletRequest request) {
        ResponseEntity<ErrorResponse> base = responder(
                HttpStatus.TOO_MANY_REQUESTS, "MUITAS_TENTATIVAS", exception.getMessage(), request);
        return ResponseEntity.status(base.getStatusCode())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSegundos()))
                .body(base.getBody());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> arquivoGrande(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, "ARQUIVO_GRANDE", "A imagem deve ter no máximo 2 MB.", request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> parteAusente(MissingServletRequestPartException exception, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Envie uma imagem no campo 'foto'.", request);
    }

    // JSON malformado, parâmetro obrigatório ausente, id/data em formato errado
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> requisicaoInvalida(Exception exception, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Requisição inválida.", request);
    }

    // Ex.: duas requisições simultâneas tentando criar o mesmo e-mail (unique no banco)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridade(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Violação de integridade em {}", request.getRequestURI(), exception);
        return responder(HttpStatus.CONFLICT, "CONFLITO", "Registro já existe.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rotaInexistente(NoResourceFoundException exception, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "RECURSO_NAO_ENCONTRADO", "Rota não encontrada.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNaoPermitido(
            HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NAO_PERMITIDO", "Método não permitido.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> erroInterno(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Erro não tratado em {} {}", request.getMethod(), request.getRequestURI(), exception);
        return responder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERRO_INTERNO",
                "Ocorreu um erro interno no servidor.",
                request
        );
    }

    private ResponseEntity<ErrorResponse> responder(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}
