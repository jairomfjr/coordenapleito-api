package com.coordenapleito.domain.service.usuario;

import com.coordenapleito.domain.model.Usuario;

/**
 * Ponto de extensão para regras de cadastro de usuário.
 * O recorte atual não exige equipamento nem coordenação.
 */
public final class UsuarioEscopoCadastroValidator {

    private UsuarioEscopoCadastroValidator() {}

    public static void validar(Usuario usuario) {
        // Sem vínculos extras além de grupo.
    }
}
