package com.coordenapleito.core.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.coordenapleito.core.security.AuthenticationModel;
import com.coordenapleito.core.security.JwtService;
import com.coordenapleito.core.security.UsuarioSecurityMessages;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.core.security.UsuarioPermissaoResolver;
import com.coordenapleito.infrastructure.util.CpfUtils;

import org.springframework.security.authentication.DisabledException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;

    private final UsuarioRepository usuarioRepository;

    private final JwtDecoder jwtDecoder;

    private final UsuarioPermissaoResolver usuarioPermissaoResolver;

    public AuthenticationModel authenticate(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(authentication);
        Usuario usuario = usuarioRepository
                .findByCpfWithGruposAndPermissoes(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<String> permissoes = new ArrayList<>(usuarioPermissaoResolver.resolverChaves(usuario));

        AuthenticationModel response = new AuthenticationModel();
        response.setUsername(userDetails.getUsername());
        response.setNome(usuario.getNome());
        response.setId(usuario.getId());
        response.setCodigo(usuario.getCodigo());
        response.setRoles(rolesNomesLimpos(usuario));
        response.setGruposCodigos(gruposCodigosLimpos(usuario));
        response.setPermissoes(permissoes);
        response.setAuthorities(permissoes);
        response.setModulosOperacionais(List.of());
        response.setAccessToken(jwt);

        return response;
    }

    /** Nomes de grupo para o SPA (menus); ignora nulos e em branco. */
    private static List<String> rolesNomesLimpos(Usuario usuario) {
        if (usuario.getGrupos() == null) {
            return Collections.emptyList();
        }
        return usuario.getGrupos().stream()
                .map(Grupo::getNome)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<String> gruposCodigosLimpos(Usuario usuario) {
        if (usuario.getGrupos() == null) {
            return Collections.emptyList();
        }
        return usuario.getGrupos().stream()
                .filter(Objects::nonNull)
                .map(Grupo::getCodigo)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .collect(Collectors.toList());
    }

    public ResponseEntity<AuthenticationModel> checkToken(String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final Jwt decodedJWT;
        try {
            decodedJWT = jwtDecoder.decode(token.trim());
            jwtService.validateToken(decodedJWT.getTokenValue());
        } catch (JwtException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario usuarioAutenticado = usuarioRepository
                .findByCpfWithGruposAndPermissoes(CpfUtils.normalizar(decodedJWT.getSubject()))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        validarContaAtiva(usuarioAutenticado);

        List<String> permissoes = new ArrayList<>(usuarioPermissaoResolver.resolverChaves(usuarioAutenticado));
        String jwtAtualizado = jwtService.generateToken(usuarioAutenticado);

        AuthenticationModel authenticationModel = new AuthenticationModel();
        authenticationModel.setAccessToken(jwtAtualizado);
        authenticationModel.setUsername(usuarioAutenticado.getUsername());
        authenticationModel.setNome(usuarioAutenticado.getNome());
        authenticationModel.setId(usuarioAutenticado.getId());
        authenticationModel.setCodigo(usuarioAutenticado.getCodigo());
        authenticationModel.setRoles(rolesNomesLimpos(usuarioAutenticado));
        authenticationModel.setGruposCodigos(gruposCodigosLimpos(usuarioAutenticado));
        authenticationModel.setPermissoes(permissoes);
        authenticationModel.setAuthorities(permissoes);
        authenticationModel.setModulosOperacionais(List.of());

        return ResponseEntity.ok(authenticationModel);
    }

    private static void validarContaAtiva(Usuario usuario) {
        if (!usuario.isContaAtiva()) {
            throw new DisabledException(UsuarioSecurityMessages.USUARIO_INATIVO);
        }
    }
}