package com.coordenapleito.api.input;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalVotacaoInput {

    @NotNull(message = "Zona é obrigatória")
    @Min(value = 1, message = "Zona deve ser maior que zero")
    private Integer zona;

    @NotBlank(message = "Local de votação é obrigatório")
    @Size(max = 255)
    private String localVotacao;

    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 255)
    private String endereco;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 255)
    private String bairro;

    @NotNull(message = "Quantidade de seções é obrigatória")
    @Min(value = 0, message = "Quantidade de seções não pode ser negativa")
    private Integer qtdSecoes;

    @NotNull(message = "Quantidade de eleitores é obrigatória")
    @Min(value = 0, message = "Quantidade de eleitores não pode ser negativa")
    private Integer qtdEleitores;

    @NotNull(message = "Quantidade de coordenadores é obrigatória")
    @Min(value = 0, message = "Quantidade de coordenadores não pode ser negativa")
    private Integer qtdCoordenadores;
}
