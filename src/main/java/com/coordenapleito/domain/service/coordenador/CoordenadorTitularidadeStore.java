package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.domain.exception.NegocioException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CoordenadorTitularidadeStore {

    private static final int TTL_CODIGO_SEGUNDOS = 10 * 60;
    private static final int TTL_SESSAO_SEGUNDOS = 20 * 60;
    private static final int INTERVALO_REENVIO_SEGUNDOS = 45;
    private static final int MAX_TENTATIVAS = 5;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Desafio> desafios = new ConcurrentHashMap<>();
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    public String gerarCodigo(String cpf) {
        limparExpirados();
        Desafio atual = desafios.get(cpf);
        if (atual != null && atual.proximoEnvio().isAfter(Instant.now())) {
            throw new NegocioException("Aguarde alguns segundos para solicitar um novo código");
        }
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        Instant agora = Instant.now();
        desafios.put(cpf, new Desafio(
                codigo,
                agora.plusSeconds(TTL_CODIGO_SEGUNDOS),
                agora.plusSeconds(INTERVALO_REENVIO_SEGUNDOS),
                0));
        return codigo;
    }

    public String confirmarCodigo(String cpf, String codigoInformado, Long coordenadorId) {
        limparExpirados();
        Desafio desafio = desafios.get(cpf);
        if (desafio == null || desafio.expira().isBefore(Instant.now())) {
            desafios.remove(cpf);
            throw new NegocioException("Código expirado. Solicite um novo código");
        }
        if (desafio.tentativas() >= MAX_TENTATIVAS) {
            desafios.remove(cpf);
            throw new NegocioException("Número de tentativas excedido. Solicite um novo código");
        }
        if (!desafio.codigo().equals(codigoInformado == null ? "" : codigoInformado.trim())) {
            desafios.put(cpf, desafio.incrementarTentativa());
            throw new NegocioException("Código de verificação inválido");
        }
        desafios.remove(cpf);
        String token = UUID.randomUUID().toString();
        sessoes.put(token, new Sessao(cpf, coordenadorId, Instant.now().plusSeconds(TTL_SESSAO_SEGUNDOS)));
        return token;
    }

    public Sessao exigirSessao(String token) {
        limparExpirados();
        Sessao sessao = token == null ? null : sessoes.get(token);
        if (sessao == null || sessao.expira().isBefore(Instant.now())) {
            if (token != null) {
                sessoes.remove(token);
            }
            throw new NegocioException("Confirme sua identidade novamente para atualizar o cadastro");
        }
        return sessao;
    }

    private void limparExpirados() {
        Instant agora = Instant.now();
        desafios.entrySet().removeIf(e -> e.getValue().expira().isBefore(agora));
        sessoes.entrySet().removeIf(e -> e.getValue().expira().isBefore(agora));
    }

    public record Sessao(String cpf, Long coordenadorId, Instant expira) {}

    private record Desafio(String codigo, Instant expira, Instant proximoEnvio, int tentativas) {
        private Desafio incrementarTentativa() {
            return new Desafio(codigo, expira, proximoEnvio, tentativas + 1);
        }
    }
}
