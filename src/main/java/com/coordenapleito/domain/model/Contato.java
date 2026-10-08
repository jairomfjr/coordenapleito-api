package com.coordenapleito.domain.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Audited(targetAuditMode=RelationTargetAuditMode.NOT_AUDITED)
@Getter
@Setter
@Embeddable
public class Contato {

    private String telefone;

    private String celular;

    @Email
    private String email;

}
