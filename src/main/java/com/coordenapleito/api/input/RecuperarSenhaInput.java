package com.coordenapleito.api.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecuperarSenhaInput {

    @NotBlank
    private String cpf;

    @Email
    @NotBlank
    private String email;
}