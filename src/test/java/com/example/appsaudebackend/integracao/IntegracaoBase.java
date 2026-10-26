package com.example.appsaudebackend.integracao;

import com.example.appsaudebackend.modules.atendente.persistencia.AtendenteModel;
import com.example.appsaudebackend.modules.atendente.persistencia.AtendenteRepository;
import com.example.appsaudebackend.modules.auth.model.Role;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.auth.repository.UsuarioAuthRepository;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoModel;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoRepository;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteModel;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração: sobe a aplicação inteira (filtros de segurança, JWT em cookie,
 * CSRF, controllers, services e JPA em H2) e conversa com ela só por HTTP via MockMvc.
 * Cada teste usa e-mails/CRMs únicos, então não depende da ordem nem de limpeza de banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class IntegracaoBase {

    protected static final String SENHA = "senha123";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UsuarioAuthRepository usuarioAuthRepository;
    @Autowired protected PacienteRepository pacienteRepository;
    @Autowired protected MedicoRepository medicoRepository;
    @Autowired protected AtendenteRepository atendenteRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    /** Credenciais + cookies de uma sessão já autenticada, com o token CSRF correspondente. */
    protected record Sessao(String email, Cookie auth, Cookie xsrf) {
        String token() { return xsrf.getValue(); }
    }

    protected static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
    }

    // ---------- criação de contas ----------

    protected String cadastrarPaciente(String nome) throws Exception {
        String email = emailUnico("paciente");
        mockMvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("nome", nome, "email", email, "senha", SENHA)))
                .andExpect(status().isCreated());
        return email;
    }

    protected MedicoModel criarMedico(String nome, String especialidade) {
        String email = emailUnico("medico");
        UsuarioAuth auth = novaConta(email, Role.MEDICO);
        MedicoModel medico = new MedicoModel();
        medico.setNome(nome);
        medico.setCrm("CRM-" + UUID.randomUUID().toString().substring(0, 8));
        medico.setEspecialidade(especialidade);
        medico.setEmail(email);
        medico.setUsuarioAuth(auth);
        return medicoRepository.save(medico);
    }

    protected AtendenteModel criarAtendente(String nome) {
        String email = emailUnico("atendente");
        UsuarioAuth auth = novaConta(email, Role.ATENDENTE);
        AtendenteModel atendente = new AtendenteModel();
        atendente.setNome(nome);
        atendente.setSetor("Recepção");
        atendente.setEmail(email);
        atendente.setUsuarioAuth(auth);
        return atendenteRepository.save(atendente);
    }

    protected PacienteModel buscarPaciente(String email) {
        return pacienteRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    private UsuarioAuth novaConta(String email, Role role) {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setInternalKey(UUID.randomUUID().toString());
        auth.setEmail(email);
        auth.setSenha(passwordEncoder.encode(SENHA));
        auth.setRole(role);
        return usuarioAuthRepository.save(auth);
    }

    // ---------- sessão (login + csrf) ----------

    protected Cookie login(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", email, "senha", senha)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = result.getResponse().getCookie("auth_token");
        if (cookie == null) throw new AssertionError("login não devolveu o cookie auth_token");
        return cookie;
    }

    protected Cookie csrfCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        if (cookie == null) throw new AssertionError("/auth/csrf não emitiu o cookie XSRF-TOKEN");
        return cookie;
    }

    protected Sessao entrar(String email) throws Exception {
        return new Sessao(email, login(email, SENHA), csrfCookie());
    }

    // ---------- requisições ----------

    /** Requisição que altera dados, já com cookie de login + cookie e header CSRF. */
    protected MockHttpServletRequestBuilder comCsrf(MockHttpServletRequestBuilder req, Sessao sessao) {
        return req.cookie(sessao.auth(), sessao.xsrf()).header("X-XSRF-TOKEN", sessao.token());
    }

    protected static String json(String... paresChaveValor) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < paresChaveValor.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append('"').append(paresChaveValor[i]).append("\":\"").append(paresChaveValor[i + 1]).append('"');
        }
        return sb.append('}').toString();
    }

    protected static long idDe(MvcResult result, String path) throws Exception {
        Number id = JsonPath.read(result.getResponse().getContentAsString(), path);
        return id.longValue();
    }
}
