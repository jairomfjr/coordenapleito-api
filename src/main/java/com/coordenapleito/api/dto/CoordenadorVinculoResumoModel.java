package com.coordenapleito.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CoordenadorVinculoResumoModel {
    private long capacidadeTotal;
    private long vinculados;
    private long vagasDisponiveis;
    private long locaisEsgotados;
    private long locaisComVaga;
    private long totalLocais;
    private long totalZonas;
    private double percentualOcupacao;
    private VinculoItemModel zonaMaisVinculos;
    private VinculoItemModel zonaMenosVinculos;
    private List<VinculoItemModel> porZona;
    private List<VinculoItemModel> locaisDestaque;
    private List<VinculoItemModel> locaisComMaisVagas;
}
