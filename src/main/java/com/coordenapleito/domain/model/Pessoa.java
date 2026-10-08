package com.coordenapleito.domain.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.br.CPF;

import com.coordenapleito.domain.model.generic.AbstractEntity;

import java.time.OffsetDateTime;

@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@MappedSuperclass
public class Pessoa extends AbstractEntity {
    
	private String nome;

    private OffsetDateTime dataNascimento;

    @CPF
    private String cpf;

    @Embedded
    private Contato contato;

    public OffsetDateTime getDataDeNascimento() {
        return dataNascimento;
    }

}
