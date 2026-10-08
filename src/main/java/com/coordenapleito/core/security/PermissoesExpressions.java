package com.coordenapleito.core.security;

/**
 * Expressões SpEL reutilizáveis para {@code @PreAuthorize} (evita strings duplicadas).
 * Valores devem ser constantes em tempo de compilação (sem chamadas de método).
 * <p>
 * Módulos operacionais: escopo por coordenação via {@code moduloOperacionalAuthorization}
 * (RBAC + vínculo em {@code modulo_operacional_coordenacao}). Operações de atendimento delegam
 * ao bean para manter uma única fonte de verdade (permissão funcional + escopo).
 */
public final class PermissoesExpressions {

    private static final String AUTH_VAPT_VUPT =
            "@moduloOperacionalAuthorization.podeAcessar('vapt-vupt')";
    private static final String AUTH_CASA_CIDADAO =
            "@moduloOperacionalAuthorization.podeAcessar('casa-cidadao')";
    private static final String AUTH_CAMINHAO_CIDADAO =
            "@moduloOperacionalAuthorization.podeAcessar('caminhao-cidadao')";
    private static final String AUTH_COORDENACAO_BASICA =
            "@moduloOperacionalAuthorization.podeAcessar('coordenacao-basica')";
    private static final String AUTH_INCLUSAO_SOCIAL =
            "@moduloOperacionalAuthorization.podeAcessar('inclusao-social')";

    public static final String VAPT_VUPT = AUTH_VAPT_VUPT;

    public static final String VAPT_VUPT_ATENDIMENTO_LISTAR =
            "@moduloOperacionalAuthorization.podeListarAtendimentos('vapt-vupt')";

    /** GET {@code dashboard-resumo} dos projetos na tela {@code /dashboard-projetos}. */
    public static final String VAPT_VUPT_DASHBOARD =
            VAPT_VUPT_ATENDIMENTO_LISTAR
                    + " or @moduloOperacionalAuthorization.podeAcessarDashboardProjeto('vapt-vupt')"
                    + " or hasAuthority('" + Permissoes.DashboardProjetosVaptVupt.VISUALIZAR + "')"
                    + " or hasAuthority('" + Permissoes.DashboardProjetosVaptVupt.LISTAR + "')";

    public static final String DASHBOARD_QUALIFICACAO_VISUALIZAR =
            "hasAuthority('" + Permissoes.DashboardQualificacao.VISUALIZAR + "')";

    public static final String DASHBOARD_PROJETOS_REPROCESSAR =
            "hasAuthority('" + Permissoes.DashboardProjetos.REPROCESSAR + "')";

    public static final String CASA_CIDADAO = AUTH_CASA_CIDADAO;

    public static final String CASA_CIDADAO_DASHBOARD =
            AUTH_CASA_CIDADAO
                    + " or @moduloOperacionalAuthorization.podeAcessarDashboardProjeto('casa-cidadao')"
                    + " or hasAuthority('" + Permissoes.DashboardProjetosCasaCidadao.VISUALIZAR + "')"
                    + " or hasAuthority('" + Permissoes.DashboardProjetosCasaCidadao.LISTAR + "')";

    public static final String CAMINHAO_CIDADAO = AUTH_CAMINHAO_CIDADAO;

    public static final String CAMINHAO_CIDADAO_DASHBOARD =
            AUTH_CAMINHAO_CIDADAO
                    + " or @moduloOperacionalAuthorization.podeAcessarDashboardProjeto('caminhao-cidadao')"
                    + " or hasAuthority('" + Permissoes.DashboardProjetosCaminhaoCidadao.VISUALIZAR + "')"
                    + " or hasAuthority('" + Permissoes.DashboardProjetosCaminhaoCidadao.LISTAR + "')";

    public static final String COORDENACAO_BASICA = AUTH_COORDENACAO_BASICA;

    public static final String INCLUSAO_SOCIAL = AUTH_INCLUSAO_SOCIAL;

    public static final String CASA_CIDADAO_ATENDIMENTO_LISTAR =
            "@moduloOperacionalAuthorization.podeListarAtendimentos('casa-cidadao')";

    public static final String CASA_CIDADAO_ATENDIMENTO_VISUALIZAR =
            "@moduloOperacionalAuthorization.podeVisualizarAtendimento('casa-cidadao')";

    public static final String CASA_CIDADAO_ATENDIMENTO_CRIAR =
            "@moduloOperacionalAuthorization.podeCriarAtendimento('casa-cidadao')";

    public static final String CASA_CIDADAO_ATENDIMENTO_EDITAR =
            "@moduloOperacionalAuthorization.podeEditarAtendimento('casa-cidadao')";

    public static final String CASA_CIDADAO_ATENDIMENTO_EXCLUIR =
            "@moduloOperacionalAuthorization.podeExcluirAtendimento('casa-cidadao')";

    public static final String CAMINHAO_CIDADAO_ATENDIMENTO_LISTAR =
            "@moduloOperacionalAuthorization.podeListarAtendimentos('caminhao-cidadao')";

    public static final String CAMINHAO_CIDADAO_ATENDIMENTO_VISUALIZAR =
            "@moduloOperacionalAuthorization.podeVisualizarAtendimento('caminhao-cidadao')";

    public static final String CAMINHAO_CIDADAO_ATENDIMENTO_CRIAR =
            "@moduloOperacionalAuthorization.podeCriarAtendimento('caminhao-cidadao')";

    public static final String CAMINHAO_CIDADAO_ATENDIMENTO_EDITAR =
            "@moduloOperacionalAuthorization.podeEditarAtendimento('caminhao-cidadao')";

    public static final String CAMINHAO_CIDADAO_ATENDIMENTO_EXCLUIR =
            "@moduloOperacionalAuthorization.podeExcluirAtendimento('caminhao-cidadao')";

    public static final String SERVICO_VAPT_VUPT_LEITURA =
            "hasAuthority('" + Permissoes.ServicoVaptVupt.LISTAR + "') and " + AUTH_VAPT_VUPT;

    public static final String SERVICO_CAMINHAO_LEITURA =
            "((hasAuthority('" + Permissoes.ServicoCaminhao.LISTAR + "') or hasAuthority('"
                    + Permissoes.ServicoCaminhao.PAGINA + "') or hasAuthority('"
                    + Permissoes.ServicoCaminhao.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.ServicoCaminhao.MENU + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadao.LISTAR + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadao.PAGINA + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadao.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadao.MENU + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadaoAtendimento.LISTAR + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadaoAtendimento.PAGINA + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadaoAtendimento.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.CaminhaoCidadaoAtendimento.MENU + "'))) and " + AUTH_CAMINHAO_CIDADAO;

    public static final String ANALISTA_OU_ADMIN =
            "hasAuthority('" + Permissoes.Usuario.LISTAR + "') or hasAuthority('"
                    + Permissoes.Grupo.LISTAR + "')";

    public static final String MAPAS_INTERATIVOS =
            "hasAuthority('" + Permissoes.MapaInterativo.LISTAR + "') or hasAuthority('"
                    + Permissoes.MapaInterativo.EDITAR + "')";

    public static final String DASHBOARD_LEITURA =
            "hasAuthority('" + Permissoes.Dashboard.LISTAR + "') or hasAuthority('"
                    + Permissoes.Dashboard.PAGINA + "') or hasAuthority('"
                    + Permissoes.Dashboard.VISUALIZAR + "')";

    public static final String DASHBOARD_PROGRAMAS_SOCIAIS_LEITURA =
            "hasAuthority('" + Permissoes.DashboardProgramasSociais.LISTAR + "') or hasAuthority('"
                    + Permissoes.DashboardProgramasSociais.PAGINA + "') or hasAuthority('"
                    + Permissoes.DashboardProgramasSociais.VISUALIZAR + "')";

    public static final String MAPA_SOCIAL_LEITURA =
            "hasAuthority('" + Permissoes.MapaSocial.LISTAR + "') or hasAuthority('"
                    + Permissoes.MapaSocial.PAGINA + "') or hasAuthority('"
                    + Permissoes.MapaSocial.VISUALIZAR + "')";

    /** Árvore do organograma na página inicial ({@code /}) e leitura administrativa. */
    public static final String INICIO_ORGANOGRAMA_ARVORE =
            "hasAuthority('" + Permissoes.Inicio.LISTAR + "') or hasAuthority('"
                    + Permissoes.Inicio.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.Inicio.PAGINA + "') or hasAuthority('"
                    + Permissoes.Organograma.LISTAR + "')";

    /**
     * Cadastro/edição de cidadão a partir dos fluxos de projeto (Caminhão/Casa), sem liberar a listagem
     * administrativa {@code /cidadaos} para quem só tem {@code *-atendimento.criar}.
     */
    private static final String CIDADAO_CRIAR_VIA_PROJETO =
            "(@moduloOperacionalAuthorization.podeCriarAtendimento('caminhao-cidadao')) or "
                    + "(@moduloOperacionalAuthorization.podeCriarAtendimento('casa-cidadao'))";

    /**
     * Atualização de cidadão no fluxo de projeto: aceita {@code cidadao.editar} do grupo ou permissões
     * de edição do módulo/atendimento ({@code *-atendimento.editar}, {@code caminhao-cidadao.editar}, etc.).
     */
    private static final String CIDADAO_EDITAR_VIA_PROJETO =
            "(@moduloOperacionalAuthorization.podeEditarAtendimento('caminhao-cidadao')) or "
                    + "(@moduloOperacionalAuthorization.podeEditarAtendimento('casa-cidadao')) or "
                    + CIDADAO_CRIAR_VIA_PROJETO;

    public static final String CIDADAO_CRIAR =
            "hasAuthority('" + Permissoes.Cidadao.CRIAR + "') or " + CIDADAO_CRIAR_VIA_PROJETO;

    public static final String CIDADAO_EDITAR =
            "hasAuthority('" + Permissoes.Cidadao.EDITAR + "') or " + CIDADAO_EDITAR_VIA_PROJETO;

    private static final String RELATORIO_SECAO =
            "hasAuthority('" + Permissoes.Relatorio.MENU + "') or hasAuthority('"
                    + Permissoes.Relatorio.PAGINA + "') or hasAuthority('"
                    + Permissoes.Relatorio.LISTAR + "') or hasAuthority('"
                    + Permissoes.Relatorio.VISUALIZAR + "')";

    private static final String RELATORIO_CIDADAO_ACESSO_BASE =
            "hasAuthority('" + Permissoes.RelatorioCidadao.MENU + "') or hasAuthority('"
                    + Permissoes.RelatorioCidadao.PAGINA + "') or hasAuthority('"
                    + Permissoes.RelatorioCidadao.LISTAR + "') or hasAuthority('"
                    + Permissoes.RelatorioCidadao.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.RelatorioCidadao.GERAR + "')";

    private static final String RELATORIO_CASA_CIDADAO_ACESSO_BASE =
            "hasAuthority('" + Permissoes.RelatorioCasaCidadao.MENU + "') or hasAuthority('"
                    + Permissoes.RelatorioCasaCidadao.PAGINA + "') or hasAuthority('"
                    + Permissoes.RelatorioCasaCidadao.LISTAR + "') or hasAuthority('"
                    + Permissoes.RelatorioCasaCidadao.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.RelatorioCasaCidadao.GERAR_SINTETICO + "') or hasAuthority('"
                    + Permissoes.RelatorioCasaCidadao.GERAR_ANALITICO + "')";

    private static final String RELATORIO_CAMINHAO_CIDADAO_ACESSO_BASE =
            "hasAuthority('" + Permissoes.RelatorioCaminhaoCidadao.MENU + "') or hasAuthority('"
                    + Permissoes.RelatorioCaminhaoCidadao.PAGINA + "') or hasAuthority('"
                    + Permissoes.RelatorioCaminhaoCidadao.LISTAR + "') or hasAuthority('"
                    + Permissoes.RelatorioCaminhaoCidadao.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.RelatorioCaminhaoCidadao.GERAR_SINTETICO + "') or hasAuthority('"
                    + Permissoes.RelatorioCaminhaoCidadao.GERAR_ANALITICO + "')";

    private static final String RELATORIO_DASHBOARD_ACESSO_BASE =
            "hasAuthority('" + Permissoes.RelatorioDashboard.MENU + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.PAGINA + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.LISTAR + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.VISUALIZAR + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.GERAR_EQUIPAMENTOS + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.GERAR_VAPT_VUPT + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.GERAR_CASA_CIDADAO + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.GERAR_CAMINHAO_CIDADAO + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.GERAR_PROGRAMAS_SOCIAIS + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.GERAR_QUALIFICACAO + "') or hasAuthority('"
                    + Permissoes.RelatorioDashboard.GERAR_TODOS + "')";

    /** Acesso à seção ou a algum relatório (inclui chaves legadas {@code relatorio.gerar}). */
    public static final String RELATORIO_ACESSO =
            RELATORIO_SECAO + " or " + RELATORIO_CIDADAO_ACESSO_BASE + " or "
                    + RELATORIO_CASA_CIDADAO_ACESSO_BASE + " or " + RELATORIO_CAMINHAO_CIDADAO_ACESSO_BASE
                    + " or " + RELATORIO_DASHBOARD_ACESSO_BASE
                    + " or hasAuthority('" + Permissoes.Relatorio.GERAR + "')";

    /** Acesso ao relatório Caminhão do Cidadão (tela e geração). */
    public static final String RELATORIO_CAMINHAO_CIDADAO_ACESSO = RELATORIO_CAMINHAO_CIDADAO_ACESSO_BASE;

    /** Geração de PDF sintético — Caminhão do Cidadão. */
    public static final String RELATORIO_CAMINHAO_CIDADAO_GERAR_SINTETICO =
            "hasAuthority('" + Permissoes.RelatorioCaminhaoCidadao.GERAR_SINTETICO + "')";

    /** Geração de PDF analítico — Caminhão do Cidadão. */
    public static final String RELATORIO_CAMINHAO_CIDADAO_GERAR_ANALITICO =
            "hasAuthority('" + Permissoes.RelatorioCaminhaoCidadao.GERAR_ANALITICO + "')";

    /** Acesso ao relatório Casa do Cidadão (tela e geração). */
    public static final String RELATORIO_CASA_CIDADAO_ACESSO = RELATORIO_CASA_CIDADAO_ACESSO_BASE;

    /** Geração de PDF sintético — Casa do Cidadão. */
    public static final String RELATORIO_CASA_CIDADAO_GERAR_SINTETICO =
            "hasAuthority('" + Permissoes.RelatorioCasaCidadao.GERAR_SINTETICO + "')";

    /** Geração de PDF analítico — Casa do Cidadão. */
    public static final String RELATORIO_CASA_CIDADAO_GERAR_ANALITICO =
            "hasAuthority('" + Permissoes.RelatorioCasaCidadao.GERAR_ANALITICO + "')";

    /** Acesso ao relatório de dashboards (tela e opções). */
    public static final String RELATORIO_DASHBOARD_ACESSO = RELATORIO_DASHBOARD_ACESSO_BASE;

    /** Acesso ao relatório de cidadãos (tela e resumo). */
    public static final String RELATORIO_CIDADAO_ACESSO =
            RELATORIO_CIDADAO_ACESSO_BASE + " or hasAuthority('" + Permissoes.Relatorio.GERAR + "')";

    /** Geração de PDF de cidadãos — exige {@code relatorio-cidadao.gerar} (ou legado). */
    public static final String RELATORIO_CIDADAO_GERAR =
            "hasAuthority('" + Permissoes.RelatorioCidadao.GERAR + "') or hasAuthority('"
                    + Permissoes.Relatorio.GERAR + "')";

    private PermissoesExpressions() {}
}
