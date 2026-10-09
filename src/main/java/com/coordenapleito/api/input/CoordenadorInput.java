package com.coordenapleito.api.input;

import com.coordenapleito.infrastructure.util.CpfUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CoordenadorInput {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20)
    private String telefone;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 255)
    private String email;

    @NotNull(message = "Local de trabalho é obrigatório")
    private UUID localTrabalhoCodigo;

    @NotNull(message = "Local de votação é obrigatório")
    private UUID localVotacaoCodigo;

    public void setCpf(String cpf) {
        this.cpf = CpfUtils.normalizar(cpf);
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone == null ? null : telefone.replaceAll("\\D", "");
    }
}
