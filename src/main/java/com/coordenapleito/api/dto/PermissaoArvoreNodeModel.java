package com.coordenapleito.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PermissaoArvoreNodeModel {
    private String chave;
    private String descricao;
    private String modulo;
    private String recurso;
    private String acao;
    private List<PermissaoArvoreNodeModel> filhos = new ArrayList<>();
}
