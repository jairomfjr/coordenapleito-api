package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.dto.publico.CoordenadorCpfConsultaModel;
import com.coordenapleito.api.dto.publico.CoordenadorPublicoModel;
import com.coordenapleito.api.dto.publico.CoordenadorVerificacaoModel;
import com.coordenapleito.api.dto.publico.LocalVotacaoPublicoModel;
import com.coordenapleito.api.input.CoordenadorInput;
import com.coordenapleito.api.input.publico.CoordenadorPublicoAtualizaInput;
import com.coordenapleito.api.input.publico.VerificarTitularidadeInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.infrastructure.service.email.EmailException;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.CoordenadorRepository;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import com.coordenapleito.domain.service.EnvioEmailService;
import com.coordenapleito.infrastructure.util.CpfUtils;
import com.coordenapleito.infrastructure.util.CpfValidador;
import com.coordenapleito.infrastructure.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicCadastroCoordenadorService {

    private final CoordenadorRepository coordenadorRepository;
    private final LocalVotacaoRepository localVotacaoRepository;
    private final CadastroCoordenadorService cadastroCoordenadorService;
    private final CoordenadorVagasService coordenadorVagasService;
    private final CoordenadorTitularidadeStore titularidadeStore;
    private final EnvioEmailService envioEmailService;
    private final VinculosCoordenadorEventos vinculosCoordenadorEventos;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Transactional(readOnly = true)
    public List<LocalVotacaoPublicoModel> listarLocais() {
        return localVotacaoRepository.findAll(PageableUtil.sortLocalVotacaoPadrao()).stream()
                .map(this::toLocalPublico)
                .toList();
    }

    public CoordenadorCpfConsultaModel consultarCpf(String cpfInformado) {
        String cpf = exigirCpfValido(cpfInformado);
        return coordenadorRepository.findByCpf(cpf)
                .map(this::iniciarDesafio)
                .orElseGet(() -> CoordenadorCpfConsultaModel.builder()
                        .existe(false)
                        .mensagem("CPF não cadastrado. Preencha os dados para se cadastrar.")
                        .build());
    }

    @Transactional(readOnly = true)
    public CoordenadorVerificacaoModel verificarTitularidade(VerificarTitularidadeInput input) {
        String cpf = exigirCpfValido(input.getCpf());
        Coordenador coordenador = coordenadorRepository.findByCpfComLocais(cpf)
                .orElseThrow(() -> new NegocioException("Não existe cadastro para este CPF"));
        String token = titularidadeStore.confirmarCodigo(cpf, input.getCodigo(), coordenador.getId());
        return CoordenadorVerificacaoModel.builder()
                .tokenAtualizacao(token)
                .cadastro(toPublico(coordenador))
                .build();
    }

    @Transactional
    public CoordenadorPublicoModel cadastrar(CoordenadorInput input) {
        CoordenadorRegras.validar(input);
        String cpf = CpfUtils.normalizar(input.getCpf());
        if (coordenadorRepository.existsByCpf(cpf)) {
            throw new NegocioException(
                    "Já existe cadastro para este CPF. Confirme sua identidade para atualizar os dados.");
        }
        coordenadorVagasService.bloquearEValidarVaga(input.getLocalTrabalhoCodigo(), null);
        Coordenador entidade = new Coordenador();
        cadastroCoordenadorService.aplicarVinculos(entidade, input);
        CoordenadorPublicoModel criado = toPublico(coordenadorRepository.save(entidade));
        vinculosCoordenadorEventos.notificar();
        return criado;
    }

    @Transactional
    public CoordenadorPublicoModel atualizar(CoordenadorPublicoAtualizaInput input) {
        CoordenadorTitularidadeStore.Sessao sessao = titularidadeStore.exigirSessao(input.getTokenAtualizacao());
        Coordenador entidade = coordenadorRepository.findById(sessao.coordenadorId())
                .orElseThrow(() -> new NegocioException("Cadastro não encontrado"));
        if (!sessao.cpf().equals(entidade.getCpf())) {
            throw new NegocioException("Confirme sua identidade novamente para atualizar o cadastro");
        }
        UUID localTrabalhoAtual = entidade.getLocalTrabalho().getCodigo();
        boolean mudouLocalTrabalho = !localTrabalhoAtual.equals(input.getLocalTrabalhoCodigo());
        if (mudouLocalTrabalho) {
            coordenadorVagasService.bloquearEValidarVaga(input.getLocalTrabalhoCodigo(), entidade.getId());
        }
        CoordenadorInput dados = new CoordenadorInput();
        dados.setNome(input.getNome());
        dados.setCpf(entidade.getCpf());
        dados.setTelefone(input.getTelefone());
        dados.setEmail(input.getEmail());
        dados.setLocalTrabalhoCodigo(input.getLocalTrabalhoCodigo());
        dados.setLocalVotacaoCodigo(input.getLocalVotacaoCodigo());
        CoordenadorRegras.validar(dados);
        cadastroCoordenadorService.aplicarVinculos(entidade, dados);
        CoordenadorPublicoModel atualizado = toPublico(coordenadorRepository.save(entidade));
        vinculosCoordenadorEventos.notificar();
        return atualizado;
    }

    private CoordenadorCpfConsultaModel iniciarDesafio(Coordenador coordenador) {
        if (coordenador.getEmail() == null || coordenador.getEmail().isBlank()) {
            throw new NegocioException("Este cadastro não possui e-mail. Procure a coordenação para atualizar.");
        }
        String codigo = titularidadeStore.gerarCodigo(coordenador.getCpf());
        try {
            envioEmailService.enviar(EnvioEmailService.Mensagem.builder()
                    .assunto("Código de verificação — Cadastro de coordenador")
                    .corpo("coordenador-codigo-verificacao.html")
                    .variavel("nome", coordenador.getNome())
                    .variavel("codigo", codigo)
                    .variavel("urlCadastro", urlCadastroPublico())
                    .destinatario(coordenador.getEmail())
                    .build());
        } catch (EmailException e) {
            throw new NegocioException(
                    "Não foi possível enviar o código de verificação. Tente novamente em instantes.");
        }
        return CoordenadorCpfConsultaModel.builder()
                .existe(true)
                .contatoMascarado(mascararEmail(coordenador.getEmail()))
                .mensagem("Enviamos um código de verificação para o e-mail cadastrado.")
                .build();
    }

    private LocalVotacaoPublicoModel toLocalPublico(LocalVotacao local) {
        CoordenadorVagas vagas = coordenadorVagasService.consultar(local, null);
        return LocalVotacaoPublicoModel.builder()
                .codigo(local.getCodigo())
                .zona(local.getZona())
                .localVotacao(local.getLocalVotacao())
                .endereco(local.getEndereco())
                .bairro(local.getBairro())
                .capacidade(vagas.capacidade())
                .ocupados(vagas.ocupados())
                .vagasDisponiveis(vagas.disponiveis())
                .esgotado(vagas.esgotado())
                .build();
    }

    private static CoordenadorPublicoModel toPublico(Coordenador coordenador) {
        return CoordenadorPublicoModel.builder()
                .nome(coordenador.getNome())
                .cpf(coordenador.getCpf())
                .telefone(coordenador.getTelefone())
                .email(coordenador.getEmail())
                .localTrabalhoCodigo(coordenador.getLocalTrabalho() == null ? null : coordenador.getLocalTrabalho().getCodigo())
                .localVotacaoCodigo(coordenador.getLocalVotacao() == null ? null : coordenador.getLocalVotacao().getCodigo())
                .build();
    }

    private String urlCadastroPublico() {
        String base = frontendBaseUrl == null ? "http://localhost:3000" : frontendBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/cadastro-coordenador";
    }

    private static String exigirCpfValido(String cpfInformado) {
        String cpf = CpfUtils.normalizar(cpfInformado);
        if (!CpfValidador.isValido(cpf)) {
            throw new NegocioException("CPF inválido");
        }
        return cpf;
    }

    static String mascararEmail(String email) {
        int arroba = email.indexOf('@');
        if (arroba <= 0) {
            return "***";
        }
        String user = email.substring(0, arroba);
        String dominio = email.substring(arroba + 1);
        String userMask = user.length() <= 2 ? user.charAt(0) + "***" : user.substring(0, 2) + "***";
        int ponto = dominio.lastIndexOf('.');
        String dominioMask = ponto > 0
                ? dominio.charAt(0) + "***" + dominio.substring(ponto)
                : dominio.charAt(0) + "***";
        return userMask + "@" + dominioMask;
    }
}
