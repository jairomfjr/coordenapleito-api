package com.coordenapleito.domain.service.grupo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.core.security.UsuarioPermissaoResolver;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.GrupoRepository;
import com.coordenapleito.domain.repository.PermissaoRepository;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.infrastructure.util.CpfUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncGrupoPermissoesService {

    /** Permissões mínimas ao editar o próprio grupo — evita auto-bloqueio na sessão corrente. */
    private static final List<String> PERMISSOES_MINIMAS_PROPRIO_GRUPO = List.of(
            Permissoes.Grupo.MENU,
            Permissoes.Grupo.PAGINA,
            Permissoes.Grupo.LISTAR,
            Permissoes.Grupo.VISUALIZAR,
            Permissoes.Grupo.EDITAR,
            Permissoes.Grupo.GERENCIAR_PERMISSOES);

    private final GetGrupoService getGrupoService;
    private final GrupoRepository grupoRepository;
    private final PermissaoRepository permissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPermissaoResolver usuarioPermissaoResolver;

    /**
     * Substitui as permissões do grupo pela lista de códigos informada.
     * Se permissoesCodigos for null, não altera. Se vazia, remove todas.
     */
    @Transactional
    public void syncPermissoes(UUID codigoGrupo, List<UUID> permissoesCodigos) {
        Grupo grupo = getGrupoService.findByCode(codigoGrupo);
        Set<UUID> codigos = permissoesCodigos == null
                ? Set.of()
                : permissoesCodigos.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));

        if (usuarioPertenceAoGrupo(grupo)) {
            if (codigos.isEmpty()) {
                throw negocioProprioGrupoVazio();
            }
            List<Permissao> minimas = permissaoRepository.findByChaveIn(new LinkedHashSet<>(PERMISSOES_MINIMAS_PROPRIO_GRUPO));
            for (Permissao p : minimas) {
                if (p.getCodigo() != null) {
                    codigos.add(p.getCodigo());
                }
            }
            validarMinimasPersistidas(minimas);
        }

        grupo.getPermissoes().clear();

        if (!codigos.isEmpty()) {
            List<Permissao> permissoes = permissaoRepository.findByCodigoIn(codigos);
            if (permissoes.isEmpty()) {
                throw new NegocioException(
                        "Nenhuma permissão válida foi encontrada. As permissões do grupo não foram alteradas.");
            }
            permissoes.forEach(grupo::adicionarPermissao);
        }

        grupoRepository.save(grupo);
    }

    /**
     * Substitui permissões do grupo pelas chaves funcionais informadas.
     */
    @Transactional
    public void syncPermissoesPorChaves(UUID codigoGrupo, List<String> chaves) {
        Grupo grupo = getGrupoService.findByCode(codigoGrupo);
        Set<String> distinct = normalizarChaves(chaves);
        boolean proprioGrupo = usuarioPertenceAoGrupo(grupo);

        if (proprioGrupo) {
            if (distinct.isEmpty()) {
                throw negocioProprioGrupoVazio();
            }
            distinct.addAll(PERMISSOES_MINIMAS_PROPRIO_GRUPO);
            usuarioAtual()
                    .map(usuarioPermissaoResolver::resolverChaves)
                    .ifPresent(atuais -> atuais.stream()
                            .filter(PERMISSOES_MINIMAS_PROPRIO_GRUPO::contains)
                            .forEach(distinct::add));
        }

        if (distinct.isEmpty()) {
            grupo.getPermissoes().clear();
            grupoRepository.save(grupo);
            return;
        }

        List<Permissao> permissoes = permissaoRepository.findByChaveIn(distinct);
        if (permissoes.isEmpty()) {
            throw new NegocioException(
                    "Nenhuma permissão válida foi encontrada para as chaves informadas. "
                            + "As permissões do grupo não foram alteradas.");
        }

        if (proprioGrupo) {
            validarMinimasPersistidas(permissoes);
        }

        grupo.getPermissoes().clear();
        permissoes.forEach(grupo::adicionarPermissao);
        grupoRepository.save(grupo);
    }

    private static Set<String> normalizarChaves(List<String> chaves) {
        if (chaves == null || chaves.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return chaves.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static NegocioException negocioProprioGrupoVazio() {
        return new NegocioException(
                "Não é possível remover todas as permissões do grupo ao qual você pertence. "
                        + "Peça a outro administrador para alterar seu perfil ou edite outro grupo.");
    }

    private static void validarMinimasPersistidas(List<Permissao> persistidas) {
        Set<String> chaves = persistidas.stream()
                .map(Permissao::getChave)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!chaves.contains(Permissoes.Grupo.GERENCIAR_PERMISSOES)) {
            throw new NegocioException(
                    "Permissão obrigatória \"Gerenciar permissões do grupo\" não está cadastrada no sistema. "
                            + "Contate o suporte.");
        }
    }

    private Optional<Usuario> usuarioAtual() {
        String cpf = cpfAutenticado();
        if (cpf == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByCpfWithGruposAndPermissoes(cpf);
    }

    private boolean usuarioPertenceAoGrupo(Grupo grupo) {
        if (grupo == null || grupo.getCodigo() == null) {
            return false;
        }
        String cpf = cpfAutenticado();
        if (cpf == null) {
            return false;
        }
        return usuarioRepository
                .findByCpfWithGruposAndPermissoes(cpf)
                .map(usuario -> pertenceAoGrupo(usuario, grupo))
                .orElse(false);
    }

    private static boolean pertenceAoGrupo(Usuario usuario, Grupo grupo) {
        if (usuario.getGrupos() == null) {
            return false;
        }
        for (Grupo g : usuario.getGrupos()) {
            if (g == null) {
                continue;
            }
            if (grupo.getCodigo().equals(g.getCodigo())) {
                return true;
            }
            if (nomesGrupoIguais(grupo.getNome(), g.getNome())) {
                return true;
            }
        }
        return false;
    }

    private static boolean nomesGrupoIguais(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }

    private static String cpfAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            return sub != null && !sub.isBlank() ? CpfUtils.normalizar(sub) : null;
        }
        if (principal instanceof UserDetails ud) {
            return CpfUtils.normalizar(ud.getUsername());
        }
        return null;
    }
}
