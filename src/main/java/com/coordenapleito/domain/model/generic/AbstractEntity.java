package com.coordenapleito.domain.model.generic;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
	private Long id;

	private UUID codigo;

	@CreationTimestamp
	@Column(name = "data_cadastro")
	private OffsetDateTime dataCadastro;

	@UpdateTimestamp
	@Column(name = "data_atualizacao")
	private OffsetDateTime dataAtualizacao;

	@PrePersist
	private void gerarCodigo() {
		normalizarCamposTexto();
		setCodigo(UUID.randomUUID());
	}

	@jakarta.persistence.PreUpdate
	private void antesDeAtualizar() {
		normalizarCamposTexto();
	}

	private void normalizarCamposTexto() {
		normalizarObjeto(this);
	}

	private static void normalizarObjeto(Object target) {
		if (target == null) {
			return;
		}
		Class<?> current = target.getClass();
		while (current != null && current != Object.class) {
			for (Field field : current.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				field.setAccessible(true);
				try {
					Object value = field.get(target);
					if (value == null) {
						continue;
					}
					if (value instanceof String texto) {
						if ("senha".equals(field.getName())) {
							continue;
						}
						/* Descrições e textos longos: preservar caixa e acentuação informadas pelo usuário. */
						if ("descricao".equals(field.getName())) {
							continue;
						}
						if ("email".equals(field.getName())) {
							field.set(target, texto.toLowerCase(Locale.ROOT));
							continue;
						}
						/* Caminho relativo no sistema de arquivos (case-sensitive em Linux). */
						if ("fotoCaminhoRelativo".equals(field.getName())) {
							continue;
						}
						/* MIME type: manter em minúsculas (ex.: image/jpeg). */
						if ("fotoContentType".equals(field.getName())) {
							field.set(target, texto.toLowerCase(Locale.ROOT));
							continue;
						}
						/* GeoJSON: chaves e valores são case-sensitive (type, coordinates, etc.). */
						if ("geometria".equals(field.getName())) {
							continue;
						}
						field.set(target, texto.toUpperCase(Locale.ROOT));
						continue;
					}
					if (field.getType().isAnnotationPresent(Embeddable.class)) {
						normalizarObjeto(value);
					}
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Erro ao normalizar campos textuais da entidade", e);
				}
			}
			current = current.getSuperclass();
		}
	}
}