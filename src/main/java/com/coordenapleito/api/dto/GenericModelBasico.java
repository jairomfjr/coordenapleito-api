package com.coordenapleito.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GenericModelBasico {
    private Long id;
	private UUID codigo;
	private String descricao;
}