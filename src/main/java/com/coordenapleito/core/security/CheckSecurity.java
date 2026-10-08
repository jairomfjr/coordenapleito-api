package com.coordenapleito.core.security;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public @interface CheckSecurity {

	public @interface Beneficiario {

		@PreAuthorize("hasAuthority('CONSULTAR_TODOS_BENEFICIARIOS') or " +
				"(hasAuthority('CONSULTAR_TODOS_BENEFICIARIOS_MUNICIPIO'))"
		)
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeConsultarTodosBeneficiarios {
		}

		@PreAuthorize("(hasAuthority('ALTERAR_STATUS_BENEFICIARIOS_MUNICIPIO') and @esportesuperacaoSecurity.isBeneficiarioDoMunicipioDaInstituicaoDoUsuario(#codigoMunicipioInstituicaoUsuario))")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeAlterarStatusBeneficiario {
		}
	}

	public @interface Instituicao {

		@PreAuthorize("hasAuthority('GERENCIAR_INSTITUICOES')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeGerenciar {
		}

		@PreAuthorize("hasAuthority('ARQUIVAR_INSTITUICAO')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeArquivar {
		}

		@PreAuthorize("hasAuthority('GERENCIAR_INSTITUICOES') or hasAuthority('CONSULTAR_TODAS_INSTITUICOES') ")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeConsultarTodas {
		}
	}

	public @interface Candidato {

		@PostAuthorize("hasAuthority('CADASTRAR_BENEFICIARIO')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeCadastrarBeneficiario {
		}

		@PreAuthorize("hasAuthority('CONSULTAR_CANDIDATOS')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeConsultar {
		}

//		@PreAuthorize("hasAuthority('CONSULTAR_PESSOAS') or "
//				+ "(hasAuthority('CONSULTAR_DETALHE_DENUNCIA') and @oisolSecurityPermissions.isPessoaDeDenunciaEncaminhadaParaInstituicao(#codigoPessoa))")
//		@Retention(RUNTIME)
//		@Target(METHOD)
//		public @interface PodeConsultarDetalhe {
//		}

//		@PreAuthorize("hasAuthority('GERENCIAR_FUNCIONARIO')")
//		@Retention(RUNTIME)
//		@Target(METHOD)
//		public @interface PodeGerenciar {
//		}

	}

	public @interface Edicao {

		@PostAuthorize("hasAuthority('CADASTRAR_EDICAO')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeCadastrarEdicao {
		}

		@PreAuthorize("hasAuthority('CONSULTAR_EDICOES')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeConsultarTodasEdicoes {
		}

		@PreAuthorize("hasAuthority('CONSULTAR_DETALHES_EDICAO')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeConsultarDetalhesEdicao {
		}

		@PreAuthorize("hasAuthority('GERENCIAR_EDICAO')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeAtualizarEdicao {
		}

	}

	public @interface Funcionario {

		@PreAuthorize("hasAuthority('GERENCIAR_USUARIOS')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeGerenciar {
		}

		@PreAuthorize("hasAuthority('ALTERAR_SENHA') and @esportesuperacaoSecurity.getUsuarioId() == #funcionarioId")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeAlterarSenha {
		}

		@PreAuthorize("hasAuthority('GERENCIAR_USUARIOS') or @esportesuperacaoSecurity.getUsuarioId() == #funcionarioId")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeGerenciarTermo {
		}

	}

	public @interface PrestacaoContas{
		@PreAuthorize("hasAuthority('CONFIRMAR_PRESTACAO_CONTAS')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeConfimarPrestacaoDeContas {
		}

		@PreAuthorize("hasAuthority('CADASTRAR_PRESTACAO_CONTAS')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeCadastrarPrestacaoDeContas {
		}
	}
	public @interface CadastrosBasicos {

		@PreAuthorize("hasAuthority('GERENCIAR_CADASTROS_BASICOS')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeGerenciar {
		}

	}

	public @interface CadastrosVitais {

		@PreAuthorize("hasAuthority('ADMINISTRADOR')")
		@Retention(RUNTIME)
		@Target(METHOD)
		public @interface PodeExcluir {
		}
	}
}