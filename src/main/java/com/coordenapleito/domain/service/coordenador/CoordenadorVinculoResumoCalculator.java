package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.dto.CoordenadorVinculoResumoModel;
import com.coordenapleito.api.dto.VinculoItemModel;
import com.coordenapleito.domain.repository.LocalTrabalhoOcupacaoProjection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CoordenadorVinculoResumoCalculator {

    private static final int LOCAIS_DESTAQUE = 8;

    private CoordenadorVinculoResumoCalculator() {}

    static CoordenadorVinculoResumoModel de(List<LocalTrabalhoOcupacaoProjection> ocupacoes) {
        List<VinculoItemModel> locais = ocupacoes.stream()
                .map(CoordenadorVinculoResumoCalculator::toLocal)
                .toList();

        long capacidadeTotal = locais.stream().mapToLong(VinculoItemModel::getCapacidade).sum();
        long vinculados = locais.stream().mapToLong(VinculoItemModel::getVinculados).sum();
        long vagas = locais.stream().mapToLong(VinculoItemModel::getVagasDisponiveis).sum();
        long esgotados = locais.stream().filter(item -> item.getVagasDisponiveis() == 0 && item.getCapacidade() > 0).count();
        long comVaga = locais.stream().filter(item -> item.getVagasDisponiveis() > 0).count();

        List<VinculoItemModel> porZona = agregarZonas(locais);
        Comparator<VinculoItemModel> porVinculos = Comparator
                .comparingLong(VinculoItemModel::getVinculados)
                .thenComparing(item -> item.getZona() == null ? 0 : item.getZona());
        VinculoItemModel mais = porZona.stream().max(porVinculos).orElse(null);
        VinculoItemModel menos = porZona.stream()
                .filter(item -> item.getCapacidade() > 0)
                .min(porVinculos)
                .orElse(null);

        List<VinculoItemModel> destaque = locais.stream()
                .sorted(Comparator.comparingLong(VinculoItemModel::getVinculados).reversed()
                        .thenComparing(VinculoItemModel::getNome, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(LOCAIS_DESTAQUE)
                .toList();
        List<VinculoItemModel> comMaisVagas = locais.stream()
                .filter(item -> item.getVagasDisponiveis() > 0)
                .sorted(Comparator.comparingLong(VinculoItemModel::getVagasDisponiveis).reversed()
                        .thenComparing(VinculoItemModel::getNome, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(LOCAIS_DESTAQUE)
                .toList();

        return CoordenadorVinculoResumoModel.builder()
                .capacidadeTotal(capacidadeTotal)
                .vinculados(vinculados)
                .vagasDisponiveis(vagas)
                .locaisEsgotados(esgotados)
                .locaisComVaga(comVaga)
                .totalLocais(locais.size())
                .totalZonas(porZona.size())
                .percentualOcupacao(percentual(vinculados, capacidadeTotal))
                .zonaMaisVinculos(mais)
                .zonaMenosVinculos(menos)
                .porZona(porZona)
                .locaisDestaque(destaque)
                .locaisComMaisVagas(comMaisVagas)
                .build();
    }

    private static VinculoItemModel toLocal(LocalTrabalhoOcupacaoProjection row) {
        long capacidade = row.getCapacidade() == null ? 0 : row.getCapacidade();
        long ocupados = row.getOcupados() == null ? 0 : row.getOcupados();
        long vagas = Math.max(0, capacidade - ocupados);
        return VinculoItemModel.builder()
                .zona(row.getZona())
                .nome(row.getLocalVotacao())
                .capacidade(capacidade)
                .vinculados(ocupados)
                .vagasDisponiveis(vagas)
                .percentualOcupacao(percentual(ocupados, capacidade))
                .build();
    }

    private static List<VinculoItemModel> agregarZonas(List<VinculoItemModel> locais) {
        Map<Integer, Acc> zonas = new LinkedHashMap<>();
        for (VinculoItemModel local : locais) {
            int zona = local.getZona() == null ? 0 : local.getZona();
            Acc acc = zonas.computeIfAbsent(zona, key -> new Acc());
            acc.capacidade += local.getCapacidade();
            acc.vinculados += local.getVinculados();
            acc.vagas += local.getVagasDisponiveis();
        }
        List<VinculoItemModel> resultado = new ArrayList<>();
        for (Map.Entry<Integer, Acc> entry : zonas.entrySet()) {
            Acc acc = entry.getValue();
            resultado.add(VinculoItemModel.builder()
                    .zona(entry.getKey())
                    .nome("Zona " + entry.getKey())
                    .capacidade(acc.capacidade)
                    .vinculados(acc.vinculados)
                    .vagasDisponiveis(acc.vagas)
                    .percentualOcupacao(percentual(acc.vinculados, acc.capacidade))
                    .build());
        }
        resultado.sort(Comparator.comparingInt(item -> item.getZona() == null ? 0 : item.getZona()));
        return resultado;
    }

    static double percentual(long parte, long total) {
        if (total <= 0) {
            return 0;
        }
        return Math.round(parte * 1000.0 / total) / 10.0;
    }

    private static final class Acc {
        private long capacidade;
        private long vinculados;
        private long vagas;
    }
}
