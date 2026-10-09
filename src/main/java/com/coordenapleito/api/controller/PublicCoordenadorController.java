package com.coordenapleito.api.controller;

import com.coordenapleito.api.dto.publico.CoordenadorCpfConsultaModel;
import com.coordenapleito.api.dto.publico.CoordenadorPublicoModel;
import com.coordenapleito.api.dto.publico.CoordenadorVerificacaoModel;
import com.coordenapleito.api.dto.publico.LocalVotacaoPublicoModel;
import com.coordenapleito.api.input.CoordenadorInput;
import com.coordenapleito.api.input.publico.CoordenadorPublicoAtualizaInput;
import com.coordenapleito.api.input.publico.CpfPublicoInput;
import com.coordenapleito.api.input.publico.VerificarTitularidadeInput;
import com.coordenapleito.domain.service.coordenador.PublicCadastroCoordenadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/publico")
@RequiredArgsConstructor
public class PublicCoordenadorController {

    private final PublicCadastroCoordenadorService publicCadastroCoordenadorService;

    @GetMapping("/locais-votacao")
    public List<LocalVotacaoPublicoModel> listarLocais() {
        return publicCadastroCoordenadorService.listarLocais();
    }

    @PostMapping("/coordenadores/consultar-cpf")
    public CoordenadorCpfConsultaModel consultarCpf(@RequestBody @Valid CpfPublicoInput input) {
        return publicCadastroCoordenadorService.consultarCpf(input.getCpf());
    }

    @PostMapping("/coordenadores/verificar-codigo")
    public CoordenadorVerificacaoModel verificarCodigo(@RequestBody @Valid VerificarTitularidadeInput input) {
        return publicCadastroCoordenadorService.verificarTitularidade(input);
    }

    @PostMapping("/coordenadores")
    @ResponseStatus(HttpStatus.CREATED)
    public CoordenadorPublicoModel cadastrar(@RequestBody @Valid CoordenadorInput input) {
        return publicCadastroCoordenadorService.cadastrar(input);
    }

    @PutMapping("/coordenadores")
    public CoordenadorPublicoModel atualizar(@RequestBody @Valid CoordenadorPublicoAtualizaInput input) {
        return publicCadastroCoordenadorService.atualizar(input);
    }
}
