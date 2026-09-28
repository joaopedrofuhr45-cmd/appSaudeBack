package com.example.appsaudebackend.Shared.Exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

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
                "CPF ou senha inválidos.",
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> erroInterno(
            Exception exception,
            HttpServletRequest request
    ) {
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
