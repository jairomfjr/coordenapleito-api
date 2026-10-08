package com.coordenapleito.core.security;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AuthenticationModel {

    private String accessToken;
    private Long id;
    private UUID codigo;
    private String username;
    private String nome;
    /**
     * ID da coordenação vinculada ao usuário (se houver).
     * Usado para regras de visibilidade de menus e filtros contextuais.
     */
    private Long coordenacaoId;
    /** Nomes dos grupos (somente exibição; não usar em regras funcionais). */
    private List<String> roles;
    /** Códigos UUID dos grupos do usuário (detecção de edição do próprio perfil no SPA). */
    private List<String> gruposCodigos;
    /** @deprecated Preferir {@link #permissoes}. Mantido para compatibilidade temporária. */
    private List<String> authorities;
    /** Chaves funcionais {@code recurso.acao} — fonte para autorização no SPA. */
    private List<String> permissoes;
    /**
     * Módulos operacionais liberados pela coordenação do usuário
     * (ex.: vapt-vupt, casa-cidadao). Perfis com visão ampla usam só RBAC.
     */
    private List<String> modulosOperacionais;
    private String tokenType = "Bearer";
}