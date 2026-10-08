package com.coordenapleito.config;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelMapperConfigTest {

    @Test
    void modelMapperBeanConfiguresWithoutErrors() {
        ModelMapper modelMapper = new ModelMapperConfig().modelMapper();
        assertNotNull(modelMapper);
        assertNotNull(modelMapper.getTypeMap(
                com.coordenapleito.api.input.UsuarioInput.class,
                com.coordenapleito.domain.model.Usuario.class));
    }
}
