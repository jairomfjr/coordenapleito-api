package com.coordenapleito.domain.model;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import com.coordenapleito.domain.listener.CustomRevisionListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name="revinfo")
@Entity
@Getter @Setter
@RevisionEntity(CustomRevisionListener.class)
public class CustomRevisionEntity extends DefaultRevisionEntity {
	
	private static final long serialVersionUID = 1L;

	/*
	Valores revtype
		0 - Criado/Novo
		1 - Editado
		2 - Excluido
	*/	

	@Column(name = "login_usuario")
	private String loginUsuario;
	
	@Column(name = "nome_usuario")
	private String nomeUsuario;
	
	@CreationTimestamp
	@Column(name = "data_cadastro")
	private OffsetDateTime dataCadastro;
}
