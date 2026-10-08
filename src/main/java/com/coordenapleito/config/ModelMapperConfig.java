package com.coordenapleito.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coordenapleito.api.input.GrupoInput;
import com.coordenapleito.api.input.UsuarioInput;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Usuario;

@Configuration
public class ModelMapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setSkipNullEnabled(true);

		TypeMap<UsuarioInput, Usuario> usuarioInputMap = modelMapper.emptyTypeMap(UsuarioInput.class, Usuario.class);
		usuarioInputMap.addMappings(mapper -> {
			mapper.skip(Usuario::setId);
			mapper.skip(Usuario::setCodigo);
			mapper.skip(Usuario::setGrupos);
		});
		usuarioInputMap.implicitMappings();

		TypeMap<GrupoInput, Grupo> grupoInputMap = modelMapper.emptyTypeMap(GrupoInput.class, Grupo.class);
		grupoInputMap.addMappings(mapper -> {
			mapper.skip(Grupo::setId);
			mapper.skip(Grupo::setCodigo);
			mapper.skip(Grupo::setPermissoes);
		});
		grupoInputMap.implicitMappings();

		return modelMapper;
	}
}
