package com.coordenapleito.api.exceptionhandler;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;

import com.coordenapleito.core.security.UsuarioSecurityMessages;
import com.coordenapleito.domain.exception.EntidadeEmUsoException;
import com.coordenapleito.domain.exception.EntidadeNaoEncontradaException;
import com.coordenapleito.domain.exception.NegocioException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.LazyInitializationException;
import org.modelmapper.MappingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.expression.ExpressionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.Locale;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String MSG_ERRO_GENERICA_USUARIO_FINAL
            = "Ocorreu um erro interno inesperado no sistema. Tente novamente e se "
            + "o problema persistir, entre em contato com o administrador do sistema.";

    private final MessageSource messageSource;

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.status(status).headers(headers).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleValidationInternal(ex, headers, status, request, ex.getBindingResult());
    }

    private ResponseEntity<Object> handleValidationInternal(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request, BindingResult bindingResult) {
        ProblemType problemType = ProblemType.DADOS_INVALIDOS;
        String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";

        List<Problem.Object> problemObjects = bindingResult.getAllErrors().stream()
                .map(objectError -> {
                    String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
                    String name = objectError.getObjectName();

                    if (objectError instanceof FieldError) {
                        name = ((FieldError) objectError).getField();
                    }

                    return Problem.Object.builder()
                            .name(name)
                            .userMessage(message)
                            .build();
                })
                .collect(Collectors.toList());

        Problem problem = createProblemBuilder((HttpStatus) status, problemType, detail)
                .userMessage(detail)
                .objects(problemObjects)
                .build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /**
     * Negação de autorização (método ou URL): resposta 403 sem tratar como erro de sistema (evita log ERROR + stack e corpo 500).
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleUsuarioInativo(DisabledException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemType problemType = ProblemType.ACESSO_NEGADO;
        String userMessage = mensagemUsuarioInativo(ex);

        if (log.isDebugEnabled()) {
            log.debug("Login negado — usuário inativo: {}", ex.getMessage());
        }

        Problem problem = createProblemBuilder(status, problemType, userMessage)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Object> handleAccessDenied(RuntimeException ex, WebRequest request) {
        return buildAcessoNegadoProblem(ex, request);
    }

    private static String mensagemUsuarioInativo(DisabledException ex) {
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage().trim();
        }
        return UsuarioSecurityMessages.USUARIO_INATIVO;
    }

    /**
     * Falha ao avaliar expressão {@code @PreAuthorize} (SpEL malformada ou erro interno de segurança).
     */
    @ExceptionHandler(ExpressionException.class)
    public ResponseEntity<Object> handleSecurityExpression(ExpressionException ex, WebRequest request) {
        log.warn("Erro ao avaliar expressão de segurança: {}", ex.getMessage());
        return buildAcessoNegadoProblem(ex, request);
    }

    private ResponseEntity<Object> buildAcessoNegadoProblem(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemType problemType = ProblemType.ACESSO_NEGADO;
        String userMessage = SecurityExpressionMessageResolver.resolveUserMessage(ex);
        String detail = SecurityExpressionMessageResolver.resolveDetail(ex);

        if (SecurityExpressionMessageResolver.isSecurityExpressionFailure(ex)) {
            log.error("Falha na expressão @PreAuthorize (SpEL): {}", detail, ex);
        } else {
            log.warn("Acesso negado em {} — {}", request.getDescription(false), detail);
        }

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<Object> handleLazyInitialization(LazyInitializationException ex, WebRequest request) {
        return buildLazyOrMappingProblem(ex, request, ex);
    }

    @ExceptionHandler(MappingException.class)
    public ResponseEntity<Object> handleModelMapper(MappingException ex, WebRequest request) {
        Throwable root = ExceptionUtils.getRootCause(ex);
        if (root instanceof LazyInitializationException lazy) {
            return buildLazyOrMappingProblem(ex, request, lazy);
        }
        return handleUncaught(ex, request);
    }

    private ResponseEntity<Object> buildLazyOrMappingProblem(
            Exception ex, WebRequest request, LazyInitializationException lazy) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;
        String detail = LazyInitializationMessageResolver.resolve(lazy);

        String requestInfo = request != null ? request.getDescription(false) : "uri=desconhecida";
        log.warn(
                "Associação lazy acessada fora da sessão Hibernate em {}: {}",
                requestInfo,
                lazy.getMessage(),
                lazy
        );

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<Object> handleInvalidDataAccessResourceUsage(
            InvalidDataAccessResourceUsageException ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;
        String detail = SqlErrorMessageResolver.resolve(ex);
        if (detail == null) {
            detail = resolverMensagemExcecaoNaoMapeada(ex);
            log.error("Erro de acesso ao banco (SQL)", ex);
        } else {
            log.warn("Erro de SQL: {}", detail);
        }

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    /**
     * Cliente encerrou a conexão (SSE, navegação, timeout) — não registrar como erro da API.
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Void> handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex, WebRequest request) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "Conexão encerrada pelo cliente em {}: {}",
                    request.getDescription(false),
                    ex.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
        if (ClientDisconnectDetector.isClientDisconnected(ex)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Conexão encerrada pelo cliente em {}: {}",
                        request.getDescription(false),
                        ex.getMessage());
            }
            return ResponseEntity.noContent().build();
        }

        if (SecurityExpressionMessageResolver.isSecurityExpressionFailure(ex)) {
            return buildAcessoNegadoProblem(ex, request);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;
        String detail = resolverMensagemExcecaoNaoMapeada(ex);

        log.error("Erro não mapeado na API", ex);

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<?> handleJpaSystemException(JpaSystemException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;
        String detail = resolverMensagemExcecaoNaoMapeada(ex);

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemType problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
        String detail = String.format("O recurso %s, que você tentou acessar, é inexistente.", ex.getRequestURL());

        Problem problem = createProblemBuilder((HttpStatus) status, problemType, detail)
                .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL).build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ex instanceof MethodArgumentTypeMismatchException) {
            return handleMethodArgumentTypeMismatch(
                    (MethodArgumentTypeMismatchException) ex, headers, (HttpStatus) status, request);
        }

        return super.handleTypeMismatch(ex, headers, status, request);
    }

    private ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ProblemType problemType = ProblemType.PARAMETRO_INVALIDO;

        String detail = String.format("O parâmetro de URL '%s' recebeu o valor '%s', "
                        + "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo %s.",
                ex.getName(), ex.getValue(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());

        Problem problem = createProblemBuilder(status, problemType, detail).userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL).build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);

        if (rootCause instanceof InvalidFormatException) {
            return handleInvalidFormat((InvalidFormatException) rootCause, headers, (HttpStatus) status, request);
        } else if (rootCause instanceof PropertyBindingException) {
            return handlePropertyBinding((PropertyBindingException) rootCause, headers, (HttpStatus) status, request);
        }

        ProblemType problemType = ProblemType.MENSAGEM_INCOMPREENSIVEL;
        String detail = "O corpo da requisição está inválido. Verifique erro de sintaxe.";

        Problem problem = createProblemBuilder((HttpStatus) status, problemType, detail)
                .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL).build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    private ResponseEntity<Object> handlePropertyBinding(PropertyBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String path = joinPath(ex.getPath());

        ProblemType problemType = ProblemType.MENSAGEM_INCOMPREENSIVEL;
        String detail = String.format("A propriedade '%s' não existe. "
                + "Corrija ou remova essa propriedade e tente novamente.", path);

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL).build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    private ResponseEntity<Object> handleInvalidFormat(InvalidFormatException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String path = joinPath(ex.getPath());

        ProblemType problemType = ProblemType.MENSAGEM_INCOMPREENSIVEL;
        String detail = String.format("A propriedade '%s' recebeu o valor '%s', "
                        + "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo %s.",
                path, ex.getValue(), ex.getTargetType().getSimpleName());

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL).build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<?> handleEntidadeNaoEncontrada(EntidadeNaoEncontradaException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemType problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemType problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(EntidadeEmUsoException.class)
    public ResponseEntity<?> handleEntidadeEmUso(EntidadeEmUsoException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemType problemType = ProblemType.ENTIDADE_EM_USO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<?> handleNegocio(NegocioException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.DADOS_INVALIDOS;
        String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";

        List<Problem.Object> problemObjects = ex.getConstraintViolations().stream()
                .map(constraintViolation -> {
                    String message = constraintViolation.getMessage();
                    String propertyPath = constraintViolation.getPropertyPath().toString();

                    return Problem.Object.builder()
                            .name(propertyPath)
                            .userMessage(message)
                            .build();
                })
                .collect(Collectors.toList());

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .objects(problemObjects)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    /**
     * Trata violações de integridade (FK, unique, etc.) e devolve mensagem amigável
     * conforme a tabela/constraint envolvida.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemType problemType = ProblemType.ENTIDADE_EM_USO;
        String detail = traduzirMensagemIntegridade(ex);

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    private String traduzirMensagemIntegridade(DataIntegrityViolationException ex) {
        String sqlMsg = SqlErrorMessageResolver.resolve(ex);
        if (sqlMsg != null) {
            return sqlMsg;
        }

        Throwable cause = ex.getCause();
        while (cause != null) {
            String causeMsg = cause.getMessage();
            if (causeMsg != null) {
                String lower = causeMsg.toLowerCase(Locale.ROOT);
                // Chave duplicada / unique (ex.: sequência PostgreSQL defasada ao inserir equipamento).
                // Deve vir antes de qualquer regra que olhe "equipamento" + "coordenacao" — o INSERT lista coordenacao_id.
                if (lower.contains("duplicate key") || lower.contains("unique constraint")) {
                    if (lower.contains("equipamento")) {
                        return "Não foi possível salvar o equipamento: o banco tentou usar um identificador que já existe. "
                                + "Isso costuma indicar sequência numérica desatualizada no banco; contate o suporte técnico.";
                    }
                    return "Não foi possível salvar: registro duplicado ou conflito de identificador.";
                }
                // FK ao inserir/atualizar equipamento com coordenação inexistente
                if (lower.contains("insert or update on table") && lower.contains("equipamento")
                        && lower.contains("not present in table") && lower.contains("coordenacao")) {
                    return "A coordenação selecionada não existe ou foi removida. Escolha outra coordenação.";
                }
                // Só exclusão na tabela coordenacao bloqueada por equipamentos (não confundir com INSERT em equipamento)
                if ((lower.contains("update or delete on table \"coordenacao\"")
                        || lower.contains("delete from table \"coordenacao\""))
                        && (lower.contains("equipamento") || lower.contains("still referenced"))) {
                    return "Não é possível excluir a coordenação pois existem equipamentos vinculados.";
                }
                if (lower.contains("usuario_grupo")) {
                    if (lower.contains("table \"usuario\"") || lower.contains("on table \"usuario\"")) {
                        return "Não é possível excluir o usuário pois está vinculado a um ou mais grupos. Remova os vínculos do usuário nos grupos antes de excluir.";
                    }
                    return "Não é possível excluir o grupo pois existem usuários vinculados. Remova os usuários do grupo antes de excluir.";
                }
                if (lower.contains("usuario_equipamento") || (lower.contains("fk") && lower.contains("usuario") && lower.contains("equipamento"))) {
                    return "Não é possível excluir o usuário pois está vinculado a um ou mais equipamentos. Remova os vínculos antes de excluir.";
                }
                if (lower.contains("grupo_permissao") || (lower.contains("fk") && lower.contains("grupo") && lower.contains("permissao"))) {
                    return "Não é possível excluir o grupo pois existem permissões vinculadas.";
                }
                if (lower.contains("bairro") || lower.contains("endereco")) {
                    return "Não é possível excluir: existem registros que utilizam este endereço ou bairro.";
                }
                if (lower.contains("municipio") && (lower.contains("bairro") || lower.contains("equipamento"))) {
                    return "Não é possível excluir o município pois existem bairros ou equipamentos vinculados.";
                }
                if (lower.contains("estado") && lower.contains("municipio")) {
                    return "Não é possível excluir o estado pois existem municípios vinculados.";
                }
                if (lower.contains("periodo") || lower.contains("periodo_acao")) {
                    return "Não é possível excluir: existem vínculos com período ou ações/indicadores.";
                }
                if (lower.contains("acao") && lower.contains("periodo")) {
                    return "Não é possível excluir a ação/indicador pois existem vínculos com períodos.";
                }
                if (lower.contains("estatistica") || lower.contains("acolhimento")) {
                    return "Não é possível excluir: existem registros dependentes (estatísticas ou acolhimentos).";
                }
            }
            cause = cause.getCause();
        }
        return "Não é possível excluir ou alterar: existem registros associados a este item. Remova os vínculos antes de tentar novamente.";
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ClientDisconnectDetector.isClientDisconnected(ex)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Cliente desconectou; resposta de erro omitida em {}: {}",
                        request.getDescription(false),
                        ex.getMessage());
            }
            return null;
        }

        if (body == null) {
            body = Problem.builder()
                    .timestamp(OffsetDateTime.now())
                    .title(HttpStatus.valueOf(status.value()).getReasonPhrase())
                    .status(status.value())
                    .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
                    .build();
        } else if (body instanceof String) {
            body = Problem.builder()
                    .timestamp(OffsetDateTime.now())
                    .title((String) body)
                    .status(status.value())
                    .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
                    .build();
        }

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    private Problem.ProblemBuilder createProblemBuilder(HttpStatus status, ProblemType problemType, String detail) {
        return Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .detail(detail);
    }

    private String joinPath(List<Reference> references) {
        return references.stream()
                .map(Reference::getFieldName)
                .collect(Collectors.joining("."));
    }

    private String resolverMensagemExcecaoNaoMapeada(Throwable ex) {
        String lazyMsg = LazyInitializationMessageResolver.resolveFromThrowable(ex);
        if (lazyMsg != null) {
            return lazyMsg;
        }

        String sqlMsg = SqlErrorMessageResolver.resolve(ex);
        if (sqlMsg != null) {
            return sqlMsg;
        }

        Throwable root = ExceptionUtils.getRootCause(ex);
        String rootMsg = root != null ? root.getMessage() : ex.getMessage();

        if (rootMsg != null) {
            String msg = rootMsg.trim();
            String lower = msg.toLowerCase(Locale.ROOT);

            if (lower.contains("identifier of an instance of")
                    && lower.contains("coordenacao")
                    && lower.contains("was altered from")) {
                return "Falha ao atualizar usuário: a coordenação enviada é incompatível com o estado atual da entidade. "
                        + "Atualize os dados e tente novamente.";
            }

            if (!msg.isBlank()) {
                return msg;
            }
        }

        return MSG_ERRO_GENERICA_USUARIO_FINAL;
    }
}