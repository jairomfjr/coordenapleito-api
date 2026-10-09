package com.coordenapleito.api.controller;

import com.coordenapleito.api.dto.CoordenadorVinculoResumoModel;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.domain.service.coordenador.CoordenadorVinculoResumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inicio")
@RequiredArgsConstructor
public class InicioController {

    private final CoordenadorVinculoResumoService coordenadorVinculoResumoService;

    @PreAuthorize("hasAuthority('" + Permissoes.Inicio.GRAFICOS_COORDENADORES + "')")
    @GetMapping("/graficos-coordenadores")
    public CoordenadorVinculoResumoModel graficosCoordenadores() {
        return coordenadorVinculoResumoService.resumir();
    }
}
