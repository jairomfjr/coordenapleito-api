package com.coordenapleito.domain.service.permissao;

import com.coordenapleito.api.dto.PermissaoArvoreNodeModel;
import com.coordenapleito.core.security.permission.NavigationCatalogRegistry;
import com.coordenapleito.core.security.permission.NavigationNode;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.PermissaoRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPermissaoArvoreService {

    private final PermissaoRepository permissaoRepository;

    public List<PermissaoArvoreNodeModel> montarArvore() {
        Map<String, List<Permissao>> porRecurso = indexarPorRecurso();
        List<PermissaoArvoreNodeModel> raiz = new ArrayList<>();
        for (NavigationNode nav : NavigationCatalogRegistry.getRoots()) {
            PermissaoArvoreNodeModel no = montarNoNavegacao(nav, porRecurso);
            if (no != null) {
                raiz.add(no);
            }
        }
        List<Permissao> orfas = coletarOrfas(porRecurso);
        if (!orfas.isEmpty()) {
            raiz.add(montarGrupoOrfas(orfas));
        }
        return raiz;
    }

    private Map<String, List<Permissao>> indexarPorRecurso() {
        Map<String, List<Permissao>> map = new LinkedHashMap<>();
        for (Permissao p : permissaoRepository.findByAtivoTrueOrderByModuloAscOrdemAsc()) {
            if (p.getChave() == null || p.getChave().isBlank()) {
                continue;
            }
            String recurso = p.getRecurso() != null ? p.getRecurso() : extrairRecursoDaChave(p.getChave());
            map.computeIfAbsent(recurso, k -> new ArrayList<>()).add(p);
        }
        map.values().forEach(list -> list.sort(Comparator.comparingInt(p -> p.getOrdem() != null ? p.getOrdem() : 0)));
        return map;
    }

    private PermissaoArvoreNodeModel montarNoNavegacao(NavigationNode nav, Map<String, List<Permissao>> porRecurso) {
        if (nav.isGrupo()) {
            PermissaoArvoreNodeModel grupo = noGrupo(nav.id(), nav.label());
            for (NavigationNode filho : nav.children()) {
                PermissaoArvoreNodeModel montado = montarNoNavegacao(filho, porRecurso);
                if (montado != null) {
                    grupo.getFilhos().add(montado);
                }
            }
            return grupo.getFilhos().isEmpty() ? null : grupo;
        }

        PermissaoArvoreNodeModel recursoNode = noRecurso(nav.recurso(), nav.label());
        List<Permissao> permissoes = porRecurso.remove(nav.recurso());
        if (permissoes != null) {
            for (Permissao p : permissoes) {
                recursoNode.getFilhos().add(folha(p));
            }
        }

        for (NavigationNode sub : nav.children()) {
            PermissaoArvoreNodeModel subNode = montarNoNavegacao(sub, porRecurso);
            if (subNode != null) {
                recursoNode.getFilhos().add(subNode);
            }
        }

        return recursoNode.getFilhos().isEmpty() ? null : recursoNode;
    }

    private List<Permissao> coletarOrfas(Map<String, List<Permissao>> restantes) {
        List<Permissao> todas = new ArrayList<>();
        for (List<Permissao> list : restantes.values()) {
            todas.addAll(list);
        }
        todas.sort(Comparator.comparingInt(p -> p.getOrdem() != null ? p.getOrdem() : 0));
        return todas;
    }

    private PermissaoArvoreNodeModel montarGrupoOrfas(List<Permissao> orfas) {
        PermissaoArvoreNodeModel outros = noGrupo("outros", "Outros (não mapeados no menu)");
        Map<String, PermissaoArvoreNodeModel> porRecurso = new LinkedHashMap<>();
        for (Permissao p : orfas) {
            String recurso = p.getRecurso() != null ? p.getRecurso() : extrairRecursoDaChave(p.getChave());
            PermissaoArvoreNodeModel recursoNode = porRecurso.computeIfAbsent(recurso, r -> {
                PermissaoArvoreNodeModel n = noRecurso(r, labelRecurso(r));
                outros.getFilhos().add(n);
                return n;
            });
            recursoNode.getFilhos().add(folha(p));
        }
        return outros;
    }

    private static PermissaoArvoreNodeModel noGrupo(String id, String label) {
        PermissaoArvoreNodeModel n = new PermissaoArvoreNodeModel();
        n.setModulo(id);
        n.setDescricao(label);
        return n;
    }

    private static PermissaoArvoreNodeModel noRecurso(String recurso, String label) {
        PermissaoArvoreNodeModel n = new PermissaoArvoreNodeModel();
        n.setRecurso(recurso);
        n.setDescricao(label);
        return n;
    }

    private static PermissaoArvoreNodeModel folha(Permissao p) {
        PermissaoArvoreNodeModel folha = new PermissaoArvoreNodeModel();
        folha.setChave(p.getChave());
        folha.setDescricao(p.getDescricao());
        folha.setModulo(p.getModulo());
        folha.setRecurso(p.getRecurso());
        folha.setAcao(p.getAcao());
        return folha;
    }

    private static String extrairRecursoDaChave(String chave) {
        int i = chave.lastIndexOf('.');
        return i > 0 ? chave.substring(0, i) : chave;
    }

    private static String labelRecurso(String recurso) {
        if (recurso == null) {
            return "Permissões";
        }
        return recurso.replace('-', ' ');
    }
}
