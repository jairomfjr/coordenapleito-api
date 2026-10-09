package com.coordenapleito.api.input.publico;

import com.coordenapleito.infrastructure.util.CpfUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CpfPublicoInput {

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    public void setCpf(String cpf) {
        this.cpf = CpfUtils.normalizar(cpf);
    }
}
