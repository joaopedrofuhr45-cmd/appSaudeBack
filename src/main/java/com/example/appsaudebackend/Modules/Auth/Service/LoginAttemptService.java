package com.example.appsaudebackend.Modules.Auth.Service;

import com.example.appsaudebackend.Shared.Exception.MuitasTentativasException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita tentativas de login erradas (força bruta), em memória.
 *
 * Dois contadores independentes:
 *  - IP + e-mail: poucas tentativas (padrão 5). Barra quem insiste numa conta.
 *    Usar o IP junto evita que um desconhecido bloqueie a conta de outra pessoa de propósito.
 *  - IP sozinho: mais folgado (padrão 30). Barra quem testa muitos e-mails diferentes.
 *
 * Ao estourar o limite dentro da janela, a chave fica bloqueada pelo mesmo tempo da janela.
 * Login correto zera só o contador de IP + e-mail.
 *
 * Limitação: o estado vive na memória de UMA instância e zera quando o servidor reinicia.
 * Com várias instâncias, o ideal é trocar por Redis ou tabela no banco.
 */
@Service
public class LoginAttemptService {

    private static final int LIMPEZA_ACIMA_DE = 10_000;

    private final int maxPorConta;
    private final int maxPorIp;
    private final Duration janela;
    private final Clock clock;
    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(
            @Value("${app.login.max-tentativas-conta:5}") int maxPorConta,
            @Value("${app.login.max-tentativas-ip:30}") int maxPorIp,
            @Value("${app.login.janela-minutos:15}") long janelaMinutos) {
        this(maxPorConta, maxPorIp, Duration.ofMinutes(janelaMinutos), Clock.systemUTC());
    }

    LoginAttemptService(int maxPorConta, int maxPorIp, Duration janela, Clock clock) {
        this.maxPorConta = maxPorConta;
        this.maxPorIp = maxPorIp;
        this.janela = janela;
        this.clock = clock;
    }

    /** Chame ANTES de tentar autenticar. Lança MuitasTentativasException se estiver bloqueado. */
    public void verificarBloqueio(String ip, String email) {
        Instant agora = clock.instant();
        for (String chave : chaves(ip, email)) {
            Registro r = registros.get(chave);
            if (r != null && r.bloqueadoAte != null && agora.isBefore(r.bloqueadoAte)) {
                long restante = Math.max(1, Duration.between(agora, r.bloqueadoAte).toSeconds());
                throw new MuitasTentativasException(restante);
            }
        }
    }

    /** Chame quando as credenciais estiverem erradas. */
    public void registrarFalha(String ip, String email) {
        Instant agora = clock.instant();
        if (registros.size() > LIMPEZA_ACIMA_DE) limpar(agora);
        registrar(chaveConta(ip, email), maxPorConta, agora);
        registrar(chaveIp(ip), maxPorIp, agora);
    }

    /** Chame quando o login der certo. */
    public void registrarSucesso(String ip, String email) {
        registros.remove(chaveConta(ip, email));
    }

    private void registrar(String chave, int limite, Instant agora) {
        registros.compute(chave, (k, atual) -> {
            Registro r = atual;
            boolean expirou = r == null || agora.isAfter(r.inicio.plus(janela));
            boolean bloqueado = r != null && r.bloqueadoAte != null && agora.isBefore(r.bloqueadoAte);
            if (bloqueado) return r;
            if (expirou) r = new Registro(agora);
            r.falhas++;
            if (r.falhas >= limite) r.bloqueadoAte = agora.plus(janela);
            return r;
        });
    }

    private void limpar(Instant agora) {
        registros.entrySet().removeIf(e -> {
            Registro r = e.getValue();
            Instant fim = r.bloqueadoAte != null ? r.bloqueadoAte : r.inicio.plus(janela);
            return agora.isAfter(fim);
        });
    }

    private List<String> chaves(String ip, String email) {
        return List.of(chaveConta(ip, email), chaveIp(ip));
    }

    private String chaveConta(String ip, String email) {
        return "conta|" + ip + "|" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    private String chaveIp(String ip) {
        return "ip|" + ip;
    }

    private static final class Registro {
        final Instant inicio;
        int falhas;
        Instant bloqueadoAte;

        Registro(Instant inicio) {
            this.inicio = inicio;
        }
    }
}
