package com.coordenapleito.domain.model;

import com.coordenapleito.domain.model.generic.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@JsonRootName("coordenador")
@Entity
@Table(
        name = "coordenador",
        uniqueConstraints = @UniqueConstraint(name = "uk_coordenador_cpf", columnNames = "cpf"))
@Getter
@Setter
@SequenceGenerator(name = "seq", sequenceName = "coordenador_id_seq", allocationSize = 1)
public class Coordenador extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "email", nullable = false)
    private String email;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "local_trabalho_id", nullable = false)
    private LocalVotacao localTrabalho;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "local_votacao_id", nullable = false)
    private LocalVotacao localVotacao;
}
