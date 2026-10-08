package com.coordenapleito.core.security;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.infrastructure.util.CpfUtils;

@Component
public class SecurityUtil {

	private final ObjectProvider<UsuarioRepository> usuarioRepositoryProvider;

	public SecurityUtil(ObjectProvider<UsuarioRepository> usuarioRepositoryProvider) {
		this.usuarioRepositoryProvider = usuarioRepositoryProvider;
	}

	public Optional<Usuario> getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}

		Object principal = authentication.getPrincipal();
		UsuarioRepository repo = usuarioRepositoryProvider.getIfAvailable();
		if (repo == null) {
			return Optional.empty();
		}

		if (principal instanceof UserDetails) {
			return repo.findByCpf(CpfUtils.normalizar(((UserDetails) principal).getUsername()));
		}
		if (principal instanceof Jwt) {
			String sub = ((Jwt) principal).getClaim("sub");
			return repo.findByCpf(CpfUtils.normalizar(sub));
		}

		return Optional.empty();
	}
}