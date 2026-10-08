package com.coordenapleito.api.input;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.coordenapleito.infrastructure.util.CpfUtils;

@Getter
@Setter
public class UsuarioInput {
    private String nome;
    private String cpf;
    private LocalDateTime dataNascimento;
    private ContatoInput contato;
    private String cargo;
    private List<UUID> grupos;

    public void setCpf(String cpf) {
        this.cpf = CpfUtils.normalizar(cpf);
    }
}