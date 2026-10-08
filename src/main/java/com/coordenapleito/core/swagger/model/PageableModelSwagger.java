package com.coordenapleito.core.swagger.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageableModelSwagger {

	private int page;
	private int size;
	private List<String> sort;
}