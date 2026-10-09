package com.coordenapleito.domain.model;

import com.coordenapleito.domain.model.generic.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@JsonRootName("localVotacao")
@Entity
@Table(
        name = "local_votacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_local_votacao_zona_nome", columnNames = {"zona", "local_votacao"})
)
@Getter
@Setter
@SequenceGenerator(name = "seq", sequenceName = "local_votacao_id_seq", allocationSize = 1)
public class LocalVotacao extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "zona", nullable = false)
    private Integer zona;

    @Column(name = "local_votacao", nullable = false)
    private String localVotacao;

    @Column(name = "endereco", nullable = false)
    private String endereco;

    @Column(name = "bairro", nullable = false)
    private String bairro;

    @Column(name = "qtd_secoes", nullable = false)
    private Integer qtdSecoes;

    @Column(name = "qtd_eleitores", nullable = false)
    private Integer qtdEleitores;

    @Column(name = "qtd_coordenadores", nullable = false)
    private Integer qtdCoordenadores;
}
