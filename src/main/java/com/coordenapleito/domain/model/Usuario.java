package com.coordenapleito.domain.model;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Audited
@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "usuario")
@SequenceGenerator(name = "seq", sequenceName = "usuario_id_seq", allocationSize = 1)
public class Usuario extends Pessoa implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Column(name = "senha")
    private String senha;

    @JoinColumn(name = "cargo")
    private String cargo;

    @Column(name = "recebe_email")
    private Boolean recebeEmail = Boolean.TRUE;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    /** Grupos são entidades de referência; sem cascade para não persistir/remover Grupo ao salvar usuário. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_grupo", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "grupo_id"))
    private List<Grupo> grupos = new ArrayList<>();

    @Column(name = "ativo")
    private Boolean ativo = Boolean.TRUE;

    public boolean adicionarGrupo(Grupo grupo) {
        return getGrupos().add(grupo);
    }

    public boolean removerGrupo(Grupo grupo) {
        return getGrupos().remove(grupo);
    }

    public List<String> getGruposListString() {
        return getGrupos().stream().map(Grupo::getNome).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "read");
    }

    @Override
    public String getPassword() {
        return this.getSenha();
    }

    @Override
    public String getUsername() {
        return this.getCpf();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Conta ativa para login e uso da API ({@code usuario.ativo}). */
    public boolean isContaAtiva() {
        return !Boolean.FALSE.equals(ativo);
    }

    @Override
    public boolean isEnabled() {
        return isContaAtiva();
    }
}
