package com.coordenapleito.domain.service.usuario;

import com.coordenapleito.domain.model.Contato;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.domain.service.EnvioEmailService;
import com.coordenapleito.domain.service.grupo.GetGrupoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GetGrupoService getGrupoService;
    @Mock
    private EnvioEmailService envioEmailService;

    @InjectMocks
    private CadastroUsuarioService cadastroUsuarioService;

    @Test
    void naoEnviaEmailQuandoGravacaoFalha() {
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(DataIntegrityViolationException.class,
                () -> cadastroUsuarioService.salvar(usuarioNovo()));

        verify(envioEmailService, never()).enviar(any());
    }

    @Test
    void soEnviaEmailDepoisDoCommit() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            cadastroUsuarioService.salvar(usuarioNovo());

            verify(envioEmailService, never()).enviar(any());
            for (TransactionSynchronization sincronizacao : TransactionSynchronizationManager.getSynchronizations()) {
                sincronizacao.afterCommit();
            }
            verify(envioEmailService).enviar(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void enviaEmailSomenteDepoisDeGravarUsuarioNovo() {
        when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cadastroUsuarioService.salvar(usuarioNovo());

        verify(usuarioRepository).save(any(Usuario.class));
        verify(envioEmailService).enviar(any());
    }

    private static Usuario usuarioNovo() {
        Contato contato = new Contato();
        contato.setEmail("pessoa@exemplo.com");
        Usuario usuario = new Usuario();
        usuario.setNome("Pessoa Teste");
        usuario.setCpf("123.456.789-00");
        usuario.setContato(contato);
        return usuario;
    }
}
