package com.coordenapleito.infrastructure.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Garante ordenação padrão nas listagens paginadas quando o cliente não envia {@code ?sort=}.
 */
public final class PageableUtil {

    private PageableUtil() {
    }

    public static Pageable comSortPadraoSeAusente(Pageable pageable, Sort sortPadrao) {
        if (pageable == null) {
            return PageRequest.of(0, 10, sortPadrao);
        }
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortPadrao);
    }

    public static Sort sortNomeAlfabetico() {
        return Sort.by(Sort.Order.asc("nome").ignoreCase());
    }

    public static Sort sortDescricaoAlfabetica() {
        return Sort.by(Sort.Order.asc("descricao").ignoreCase());
    }

    public static Sort sortSiglaAlfabetica() {
        return Sort.by(Sort.Order.asc("sigla").ignoreCase());
    }

    /** Período: primeiro ano, depois mês (ordem cronológica). */
    public static Sort sortPeriodoCronologico() {
        return Sort.by(Sort.Order.asc("ano"), Sort.Order.asc("mes"));
    }

    /** Vínculo período × ação: ordena pela descrição da ação. */
    public static Sort sortPeriodoAcaoPorDescricaoAcao() {
        return Sort.by(Sort.Order.asc("acao.descricao").ignoreCase());
    }

    /** Equipamento-serviço: equipamento, serviço, depois período. */
    public static Sort sortEquipamentoServico() {
        return Sort.by(
                Sort.Order.asc("equipamento.nome").ignoreCase(),
                Sort.Order.asc("servico.nome").ignoreCase(),
                Sort.Order.asc("ano"),
                Sort.Order.asc("mes"));
    }

    /** Estatística: por nome do equipamento. */
    public static Sort sortEstatisticaPorEquipamento() {
        return Sort.by(Sort.Order.asc("equipamento.nome").ignoreCase());
    }

    /** Acolhimento: por nome do cidadão. */
    public static Sort sortAcolhimentoPorCidadao() {
        return Sort.by(Sort.Order.asc("cidadao.nome").ignoreCase());
    }

    /** Demanda município: mais recentes primeiro, depois data prevista. */
    public static Sort sortDemandaMunicipioPadrao() {
        return Sort.by(Sort.Order.desc("dataCadastro"), Sort.Order.asc("dataPrevistaAtendimento"));
    }

    /** Quantificação por atendimento VAPT VUPT: período mais recente primeiro, depois órgão/serviço. */
    public static Sort sortVaptVuptAtendimentoCidadaoPadrao() {
        return Sort.by(
                Sort.Order.desc("vaptVuptAtendimento.ano"),
                Sort.Order.desc("vaptVuptAtendimento.mes"),
                Sort.Order.desc("vaptVuptAtendimento.dataHoraAtendimento"),
                Sort.Order.asc("orgaoVaptVupt.idOrgaoOrigem"),
                Sort.Order.asc("servicoVaptVupt.idOrigem"));
    }
}
