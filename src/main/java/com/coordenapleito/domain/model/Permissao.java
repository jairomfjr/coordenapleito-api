package com.coordenapleito.domain.model;

import com.fasterxml.jackson.annotation.JsonRootName;

import com.coordenapleito.domain.model.generic.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@JsonRootName("permissao")
@Entity
@Getter
@Setter
@EqualsAndHashCode
@SequenceGenerator(name = "seq", sequenceName = "permissao_id_seq", allocationSize = 1)
public class Permissao extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "nome")
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ativo")
    private Boolean ativo = Boolean.TRUE;

    @Column(name = "chave", length = 120)
    private String chave;

    @Column(name = "modulo", length = 60)
    private String modulo;

    @Column(name = "recurso", length = 60)
    private String recurso;

    @Column(name = "acao", length = 60)
    private String acao;

    @Column(name = "ordem")
    private Integer ordem;

    @Column(name = "sistema")
    private Boolean sistema = Boolean.FALSE;

}
