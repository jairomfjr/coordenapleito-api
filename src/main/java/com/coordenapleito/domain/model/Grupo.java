package com.coordenapleito.domain.model;

import com.fasterxml.jackson.annotation.JsonRootName;

import com.coordenapleito.domain.model.generic.AbstractEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.envers.Audited;

@Audited
@JsonRootName("grupo")
@Entity
@Getter
@Setter
@EqualsAndHashCode
@SequenceGenerator(name = "seq", sequenceName = "grupo_id_seq", allocationSize = 1)
public class Grupo extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "nome")
    private String nome;

    /** Permissões são entidades de referência; sem cascade para não persistir/remover Permissao ao salvar grupo. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "grupo_permissao",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id"))
    private Set<Permissao> permissoes = new HashSet<>();

    @Column(name = "ativo")
    private Boolean ativo = Boolean.TRUE;

    public boolean adicionarPermissao(Permissao permissao) {
        return getPermissoes().add(permissao);
    }

    public boolean removerPermissao(Permissao permissao) {
        return getPermissoes().remove(permissao);
    }

}