package com.example.appsaudebackend.shared.exception;

/** Lançada quando um cliente passa do limite de tentativas de login. Vira HTTP 429. */
public class MuitasTentativasException extends RuntimeException {

    private final long retryAfterSegundos;

    public MuitasTentativasException(long retryAfterSegundos) {
        super("Muitas tentativas de login. Tente novamente em alguns minutos.");
        this.retryAfterSegundos = retryAfterSegundos;
    }

    public long getRetryAfterSegundos() {
        return retryAfterSegundos;
    }
}
