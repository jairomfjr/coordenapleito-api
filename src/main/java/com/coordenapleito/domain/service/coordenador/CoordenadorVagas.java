package com.coordenapleito.domain.service.coordenador;

public record CoordenadorVagas(int capacidade, long ocupados, int disponiveis, boolean esgotado) {

    public static CoordenadorVagas de(Integer capacidade, long ocupados) {
        int cap = capacidade == null ? 0 : Math.max(capacidade, 0);
        long ocup = Math.max(ocupados, 0);
        int livres = (int) Math.max(cap - ocup, 0);
        return new CoordenadorVagas(cap, ocup, livres, livres == 0);
    }
}
