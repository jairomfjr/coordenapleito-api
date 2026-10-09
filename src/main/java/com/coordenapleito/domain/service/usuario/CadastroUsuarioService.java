package com.coordenapleito.domain.service.usuario;

import com.coordenapleito.api.input.UsuarioInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Contato;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.domain.service.EnvioEmailService;
import com.coordenapleito.domain.service.grupo.GetGrupoService;
import com.coordenapleito.infrastructure.util.CpfUtils;
import com.coordenapleito.infrastructure.util.GerarSenhaRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CadastroUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GetGrupoService getGrupoService;
    private final EnvioEmailService envioEmailService;

    @Transactional
    public Usuario salvar(UsuarioInput input) {
        if (input == null || input.getNome() == null || input.getNome().isBlank()) {
            throw new NegocioException("Nome do usuário é obrigatório");
        }

        Contato contato = new Contato();
        if (input.getContato() != null) {
            contato.setTelefone(input.getContato().getTelefone());
            contato.setEmail(input.getContato().getEmail());
        }

        List<Grupo> grupos = input.getGrupos() != null && !input.getGrupos().isEmpty()
                ? getGrupoService.findAllByUUID(input.getGrupos())
                : new ArrayList<>();

        Usuario usuario = new Usuario();
        usuario.setNome(input.getNome().trim());
        usuario.setCpf(input.getCpf() != null ? CpfUtils.normalizar(input.getCpf()) : null);
        usuario.setDataNascimento(input.getDataNascimento() != null ? input.getDataNascimento().atOffset(ZoneOffset.UTC) : null);
        usuario.setContato(contato);
        usuario.setCargo(input.getCargo());
        usuario.setGrupos(grupos);
        return salvar(usuario);
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        String cpfNormalizado = CpfUtils.normalizar(usuario.getCpf());
        usuario.setCpf(cpfNormalizado);

        UsuarioEscopoCadastroValidator.validar(usuario);

        Optional<Usuario> usuarioExiste = usuarioRepository.findByCpf(cpfNormalizado);

        if (usuarioExiste.isPresent() && !usuarioExiste.get().getId().equals(usuario.getId())) {
            throw new NegocioException("Já existe um usuário cadastrado com esse CPF");
        }

        String senhaAleatoria = null;
        if (usuarioExiste.isEmpty()) {
            senhaAleatoria = GerarSenhaRandom.gerarSenhaAleatoria();
            usuario.setSenha(passwordEncoder.encode(senhaAleatoria));
        }

        Usuario salvo = usuarioRepository.save(usuario);
        if (senhaAleatoria != null) {
            agendarEmailDeCadastro(salvo, senhaAleatoria);
        }
        return salvo;
    }

    /**
     * O SMTP não participa da transação. O e-mail só pode sair depois do commit,
     * para não confirmar um cadastro que o banco desfez.
     */
    private void agendarEmailDeCadastro(Usuario usuario, String senhaAleatoria) {
        EnvioEmailService.Mensagem mensagem = EnvioEmailService.Mensagem.builder()
                .assunto("Cadastro de usuário")
                .corpo("usuario-cadastrado.html")
                .variavel("usuario", usuario)
                .variavel("senha", senhaAleatoria)
                .destinatario(usuario.getContato().getEmail())
                .build();

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            envioEmailService.enviar(mensagem);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                envioEmailService.enviar(mensagem);
            }
        });
    }

}
