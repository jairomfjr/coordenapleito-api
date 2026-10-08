package com.coordenapleito.domain.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.api.input.RecuperarSenhaInput;
import com.coordenapleito.api.input.SenhaInput;
import com.coordenapleito.domain.exception.EntidadeNaoEncontradaException;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.domain.service.EnvioEmailService;
import com.coordenapleito.infrastructure.util.CpfUtils;
import com.coordenapleito.infrastructure.util.GerarSenhaRandom;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SenhaUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GetUsuarioService getUsuarioService;

    private final EnvioEmailService envioEmailService;

    public void recuperarSenha(RecuperarSenhaInput recuperarSenhaInput) {
        String cpfNormalizado = CpfUtils.normalizar(recuperarSenhaInput.getCpf());
        Usuario usuario = usuarioRepository.findByCpf(cpfNormalizado).orElseThrow(
                () -> new EntidadeNaoEncontradaException(String.format("Não existe cadastro de Usuário com este CPF " + recuperarSenhaInput.getCpf())));

        if (!usuario.getContato().getEmail().equalsIgnoreCase(recuperarSenhaInput.getEmail())) {
            throw new NegocioException("Email informado não coincide com o cadastro para o usuário");
        }
        gerarNovaSenhaEEnviarPorEmail(usuario);
    }

    /**
     * Gera nova senha para o usuário (administração) e envia ao e-mail cadastrado.
     * Mesmo fluxo de e-mail da recuperação pública ({@code senha-recuperada.html}).
     */
    @Transactional
    public void reenviarSenha(UUID codigo) {
        Usuario usuario = getUsuarioService.findByCode(codigo);
        gerarNovaSenhaEEnviarPorEmail(usuario);
    }

    private void gerarNovaSenhaEEnviarPorEmail(Usuario usuario) {
        if (usuario.getContato() == null || usuario.getContato().getEmail() == null
                || usuario.getContato().getEmail().isBlank()) {
            throw new NegocioException("Usuário não possui e-mail cadastrado para envio da senha");
        }
        String senha = GerarSenhaRandom.gerarSenhaAleatoria();
        usuario.setSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(usuario);

        envioEmailService.enviar(EnvioEmailService.Mensagem.builder().assunto("Recuperação de Senha").corpo("senha-recuperada.html")
                .variavel("usuario", usuario).variavel("senha", senha)
                .destinatario(usuario.getContato().getEmail()).build());
    }

    @Transactional
    public void alterarSenha(UUID codigo, SenhaInput senhaInput) {
        Usuario usuario = getUsuarioService.findByCode(codigo);
        try {
            if (!passwordEncoder.matches(senhaInput.getSenhaAtual(), usuario.getSenha())) {
                throw new NegocioException("Senha atual informada não coincide com a senha do usuário");
            }
            usuario.setSenha(passwordEncoder.encode(senhaInput.getNovaSenha()));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Formato inválido de dado");
        }
    }

}
