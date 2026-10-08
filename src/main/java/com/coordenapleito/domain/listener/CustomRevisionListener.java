package com.coordenapleito.domain.listener;

import org.hibernate.envers.RevisionListener;

import com.coordenapleito.core.security.SecurityUtil;
import com.coordenapleito.domain.model.CustomRevisionEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomRevisionListener implements RevisionListener {

	private final SecurityUtil securityUtil;

	@Override
	public void newRevision(Object revisionEntity) {
		CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
		securityUtil.getAuthenticatedUser().ifPresentOrElse(user -> {
			revision.setLoginUsuario(user.getCpf());
			revision.setNomeUsuario(user.getNome());
		}, () -> {
			revision.setLoginUsuario("NAO_AUTENTICADO");
			revision.setNomeUsuario("NAO_AUTENTICADO");
		});
	}
}