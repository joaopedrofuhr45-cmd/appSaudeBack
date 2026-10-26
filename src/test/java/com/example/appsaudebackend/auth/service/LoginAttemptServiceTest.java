package com.example.appsaudebackend.auth.service;

import com.example.appsaudebackend.modules.auth.service.LoginAttemptService;
import com.example.appsaudebackend.shared.exception.MuitasTentativasException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    /** Relógio que a gente controla, para testar a janela sem esperar de verdade. */
    static class RelogioFalso extends Clock {
        Instant agora = Instant.parse("2026-01-01T12:00:00Z");
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
        void avancar(Duration d) { agora = agora.plus(d); }
    }

    private LoginAttemptService criar(int conta, int ip, RelogioFalso relogio) throws Exception {
        Constructor<LoginAttemptService> c = LoginAttemptService.class
                .getDeclaredConstructor(int.class, int.class, Duration.class, Clock.class);
        c.setAccessible(true);
        return c.newInstance(conta, ip, Duration.ofMinutes(15), relogio);
    }

    @Test
    void naoBloqueiaAbaixoDoLimite() throws Exception {
        LoginAttemptService s = criar(3, 30, new RelogioFalso());
        s.registrarFalha("1.1.1.1", "ana@x.com");
        s.registrarFalha("1.1.1.1", "ana@x.com");
        assertDoesNotThrow(() -> s.verificarBloqueio("1.1.1.1", "ana@x.com"));
    }

    @Test
    void bloqueiaAoAtingirLimiteDaConta() throws Exception {
        LoginAttemptService s = criar(3, 30, new RelogioFalso());
        for (int i = 0; i < 3; i++) s.registrarFalha("1.1.1.1", "ana@x.com");
        MuitasTentativasException e = assertThrows(MuitasTentativasException.class,
                () -> s.verificarBloqueio("1.1.1.1", "ANA@x.com "));
        assertEquals(15 * 60, e.getRetryAfterSegundos());
    }

    @Test
    void bloqueioDeUmaContaNaoAfetaOutroIp() throws Exception {
        LoginAttemptService s = criar(3, 30, new RelogioFalso());
        for (int i = 0; i < 3; i++) s.registrarFalha("1.1.1.1", "ana@x.com");
        assertDoesNotThrow(() -> s.verificarBloqueio("2.2.2.2", "ana@x.com"));
    }

    @Test
    void bloqueiaIpQueTestaMuitosEmails() throws Exception {
        LoginAttemptService s = criar(5, 4, new RelogioFalso());
        for (int i = 0; i < 4; i++) s.registrarFalha("1.1.1.1", "pessoa" + i + "@x.com");
        assertThrows(MuitasTentativasException.class, () -> s.verificarBloqueio("1.1.1.1", "outra@x.com"));
    }

    @Test
    void desbloqueiaDepoisDaJanela() throws Exception {
        RelogioFalso relogio = new RelogioFalso();
        LoginAttemptService s = criar(3, 30, relogio);
        for (int i = 0; i < 3; i++) s.registrarFalha("1.1.1.1", "ana@x.com");
        assertThrows(MuitasTentativasException.class, () -> s.verificarBloqueio("1.1.1.1", "ana@x.com"));
        relogio.avancar(Duration.ofMinutes(16));
        assertDoesNotThrow(() -> s.verificarBloqueio("1.1.1.1", "ana@x.com"));
    }

    @Test
    void falhasAntigasNaoContamDepoisDaJanela() throws Exception {
        RelogioFalso relogio = new RelogioFalso();
        LoginAttemptService s = criar(3, 30, relogio);
        s.registrarFalha("1.1.1.1", "ana@x.com");
        s.registrarFalha("1.1.1.1", "ana@x.com");
        relogio.avancar(Duration.ofMinutes(16));
        s.registrarFalha("1.1.1.1", "ana@x.com"); // recomeça a contagem
        assertDoesNotThrow(() -> s.verificarBloqueio("1.1.1.1", "ana@x.com"));
    }

    @Test
    void loginCorretoZeraContadorDaConta() throws Exception {
        LoginAttemptService s = criar(3, 30, new RelogioFalso());
        s.registrarFalha("1.1.1.1", "ana@x.com");
        s.registrarFalha("1.1.1.1", "ana@x.com");
        s.registrarSucesso("1.1.1.1", "ana@x.com");
        s.registrarFalha("1.1.1.1", "ana@x.com");
        assertDoesNotThrow(() -> s.verificarBloqueio("1.1.1.1", "ana@x.com"));
    }
}
