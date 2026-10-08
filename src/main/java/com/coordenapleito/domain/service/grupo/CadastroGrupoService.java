package com.coordenapleito.domain.service.grupo;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.coordenapleito.api.assembler.GenericAssembler;
import com.coordenapleito.api.disassembler.GenericDisassembler;
import com.coordenapleito.api.dto.GrupoModelBasico;
import com.coordenapleito.api.input.GrupoInput;
import com.coordenapleito.domain.exception.EntidadeEmUsoException;
import com.coordenapleito.domain.exception.EntidadeNaoEncontradaException;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.GrupoRepository;
import com.coordenapleito.domain.service.permissao.GetPermissaoService;
import com.coordenapleito.infrastructure.util.PageableUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CadastroGrupoService {

    private static String MSG_GRUPO_NAO_ENCONTRADO = "";
    private static String MSG_GRUPO_EM_USO = "";

    private GrupoRepository grupoRepository;

    private GetPermissaoService getPermissaoService;

    private GenericAssembler genericAssembler;

    private GenericDisassembler genericDisassembler;

    public Set<GrupoModelBasico> listar() {
        List<Grupo> ordenados = grupoRepository.findAll(PageableUtil.sortNomeAlfabetico());
        Set<Grupo> grupos = new LinkedHashSet<>(ordenados);

        return genericAssembler.toCollectionModelSet(grupos, GrupoModelBasico.class);
    }

    public Grupo buscarOuFalhar(UUID codigoGrupo) {
        return grupoRepository.findByCodigo(codigoGrupo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(String.format(MSG_GRUPO_NAO_ENCONTRADO, codigoGrupo)));
    }

    public Grupo buscarPorId(Long idGrupo) {
        return grupoRepository.getReferenceById(idGrupo);
    }

    public GrupoModelBasico buscar(UUID codigoGrupo) {
        Grupo grupo = buscarOuFalhar(codigoGrupo);

        return genericAssembler.toModel(grupo, GrupoModelBasico.class);
    }

    @Transactional
    public GrupoModelBasico salvar(GrupoInput grupoInput) {
        Grupo grupo = genericDisassembler.toDomainObject(grupoInput, Grupo.class);
        grupo = grupoRepository.save(grupo);

        return genericAssembler.toModel(grupo, GrupoModelBasico.class);
    }

    @Transactional
    public GrupoModelBasico atualizar(UUID codigoGrupo, GrupoInput grupoInput) {
        Grupo grupoAtual = buscarOuFalhar(codigoGrupo);

        genericDisassembler.copyToDomainObject(grupoInput, grupoAtual);
        grupoAtual = grupoRepository.save(grupoAtual);

        return genericAssembler.toModel(grupoAtual, GrupoModelBasico.class);
    }

    @Transactional
    public void excluir(UUID codigoGrupo) {
        try {
            Grupo grupo = buscarOuFalhar(codigoGrupo);
            grupoRepository.delete(grupo);
        } catch (EmptyResultDataAccessException e) {
            throw new EntidadeNaoEncontradaException(String.format(MSG_GRUPO_NAO_ENCONTRADO, codigoGrupo));
        } catch (DataIntegrityViolationException e) {
            throw new EntidadeEmUsoException(String.format(MSG_GRUPO_EM_USO, codigoGrupo));
        }
    }

    @Transactional
    public void associarPermissao(UUID codigoGrupo, UUID codigoPermissao) {
        Grupo grupo = buscarOuFalhar(codigoGrupo);
        Permissao permissao = getPermissaoService.findByCode(codigoPermissao);

        grupo.adicionarPermissao(permissao);
    }

    @Transactional
    public void desassociarPermissao(UUID codigoGrupo, UUID codigoPermissao) {
        Grupo grupo = buscarOuFalhar(codigoGrupo);
        Permissao permissao = getPermissaoService.findByCode(codigoPermissao);

        grupo.removerPermissao(permissao);
    }
}