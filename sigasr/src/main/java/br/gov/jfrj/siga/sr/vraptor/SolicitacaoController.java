package br.gov.jfrj.siga.sr.vraptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.interceptor.download.Download;
import br.com.caelum.vraptor.interceptor.download.InputStreamDownload;
import br.com.caelum.vraptor.validator.ValidationMessage;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpComplexo;
import br.gov.jfrj.siga.cp.CpConfiguracao;
import br.gov.jfrj.siga.cp.CpSituacaoConfiguracao;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.cp.model.DpCargoSelecao;
import br.gov.jfrj.siga.cp.model.DpFuncaoConfiancaSelecao;
import br.gov.jfrj.siga.cp.model.DpLotacaoSelecao;
import br.gov.jfrj.siga.cp.model.DpPessoaSelecao;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.model.ContextoPersistencia;
import br.gov.jfrj.siga.sr.model.SrAcao;
import br.gov.jfrj.siga.sr.model.SrArquivo;
import br.gov.jfrj.siga.sr.model.SrAtributo;
import br.gov.jfrj.siga.sr.model.SrAtributoSolicitacao;
import br.gov.jfrj.siga.sr.model.SrAtributoSolicitacaoMap;
import br.gov.jfrj.siga.sr.model.SrConfiguracao;
import br.gov.jfrj.siga.sr.model.SrConfiguracaoBL;
import br.gov.jfrj.siga.sr.model.SrFormaAcompanhamento;
import br.gov.jfrj.siga.sr.model.SrGravidade;
import br.gov.jfrj.siga.sr.model.SrItemConfiguracao;
import br.gov.jfrj.siga.sr.model.SrLista;
import br.gov.jfrj.siga.sr.model.SrMeioComunicacao;
import br.gov.jfrj.siga.sr.model.SrMovimentacao;
import br.gov.jfrj.siga.sr.model.SrPrioridade;
import br.gov.jfrj.siga.sr.model.SrPrioridadeSolicitacao;
import br.gov.jfrj.siga.sr.model.SrSolicitacao;
import br.gov.jfrj.siga.sr.model.SrSolicitacao.SrTarefa;
import br.gov.jfrj.siga.sr.model.SrTendencia;
import br.gov.jfrj.siga.sr.model.SrTipoMotivoEscalonamento;
import br.gov.jfrj.siga.sr.model.SrTipoMotivoPendencia;
import br.gov.jfrj.siga.sr.model.SrTipoMovimentacao;
import br.gov.jfrj.siga.sr.model.SrTipoPermissaoLista;
import br.gov.jfrj.siga.sr.model.SrUrgencia;
import br.gov.jfrj.siga.sr.model.vo.SrListaVO;
import br.gov.jfrj.siga.sr.model.vo.SrSolicitacaoListaVO;
import br.gov.jfrj.siga.sr.util.SrSolicitacaoFiltro;
import br.gov.jfrj.siga.sr.validator.SrValidator;
import br.gov.jfrj.siga.uteis.PessoaLotaFuncCargoSelecaoHelper;
import br.gov.jfrj.siga.vraptor.SigaObjects;

import com.google.gson.Gson;

@Resource
@Path("app/solicitacao")
public class SolicitacaoController extends SrController {
    private static final String TITULAR = "titular";
    private static final String ACOES_E_ATENDENTES = "acoesEAtendentes";
    private static final String SOLICITACAO = "solicitacao";
    private static final String TIPOS_PERMISSAO_JSON = "tiposPermissaoJson";
    private static final String CADASTRANTE = "cadastrante";
    private static final String LOTA_TITULAR = "lotaTitular";
    private static final String MOSTRAR_DESATIVADOS = "mostrarDesativados";
    private static final String LISTAS = "listas";
    private static final String SOLICITACAO_LISTA_VO = "solicitacaoListaVO";
    private static final String TIPOS_PERMISSAO = "tiposPermissao";
    private static final String LOCAIS = "locais";
    private static final String LISTA = "lista";
    private static final String ORGAOS = "orgaos";
	private static final String PODE_REMOVER = "podeRemover";
	private static final String PODE_EDITAR = "podeEditar";
	private static final String PODE_PRIORIZAR = "podePriorizar";
	private static final String FILTRO = "filtro";
	private static final String PRIORIDADE_LIST = "prioridadeList";
	private static final String TIPO_MOTIVO_ESCALONAMENTO_LIST = "tipoMotivoEscalonamentoList";

    private Validator validator;

    public SolicitacaoController(HttpServletRequest request, Result result, CpDao dao, SigaObjects so, EntityManager em,  SrValidator srValidator, Validator validator) {
        super(request, result, dao, so, em, srValidator);
        this.validator = validator;
    }

    @SuppressWarnings("unchecked")
    @Path("/listarLista")
    public void listarLista(boolean mostrarDesativados) throws Exception {
        List<CpOrgaoUsuario> orgaos = ContextoPersistencia.em().createQuery("from CpOrgaoUsuario").getResultList();
        List<CpComplexo> locais = CpComplexo.AR.all().fetch();
        List<SrTipoPermissaoLista> tiposPermissao = SrTipoPermissaoLista.AR.all().fetch();
        List<SrLista> listas = SrLista.listar(mostrarDesativados);
        String tiposPermissaoJson = new Gson().toJson(tiposPermissao);

        result.include(ORGAOS, orgaos);
        result.include(LOCAIS, locais);
        result.include(TIPOS_PERMISSAO, tiposPermissao);
        result.include(LISTAS, listas);
        result.include(MOSTRAR_DESATIVADOS, mostrarDesativados);
        result.include(LOTA_TITULAR, getLotaTitular());
        result.include(CADASTRANTE, getCadastrante());
        result.include(TIPOS_PERMISSAO_JSON, tiposPermissaoJson);
        result.include("prioridades", SrPrioridade.getValoresEmOrdem());

        PessoaLotaFuncCargoSelecaoHelper.adicionarCamposSelecao(result);

		result.include("lotacaolotacaoAtualSel", new DpLotacaoSelecao());
        result.include("dpPessoapessoaAtualSel", new DpPessoaSelecao());
        result.include("funcaoConfiancafuncaoAtualSel", new DpFuncaoConfiancaSelecao());
        result.include("cargocargoAtualSel", new DpCargoSelecao());
    }

    @Path("/gravarPermissaoUsoLista")
    public void gravarPermissaoUsoLista(SrConfiguracao permissao) throws Exception {
        permissao.salvarComoPermissaoUsoLista();
        result.use(Results.http()).body(permissao.toVO().toJson());
    }

    @Path("/listarPermissaoUsoLista")
    public void listarPermissaoUsoLista(Long idLista, boolean mostrarDesativados) throws Exception {

        SrLista lista = new SrLista();
        if (idLista != null)
            lista = SrLista.AR.findById(idLista);
        List<SrConfiguracao> associacoes = SrConfiguracao.listarPermissoesUsoLista(lista, mostrarDesativados);

        result.use(Results.http()).body(SrConfiguracao.convertToJSon(associacoes));
    }

    @Path("/desativarPermissaoUsoListaEdicao")
    public void desativarPermissaoUsoListaEdicao(Long idLista, Long idPermissao) throws Exception {
        SrConfiguracao configuracao = ContextoPersistencia.em().find(SrConfiguracao.class, idPermissao);
        configuracao.finalizar();

        result.use(Results.http()).body(configuracao.getSrConfiguracaoJson());
    }

    @Path("/configuracoesParaInclusaoAutomatica")
    public void configuracoesParaInclusaoAutomatica(Long idLista, boolean mostrarDesativados) throws Exception {
        SrLista lista = SrLista.AR.findById(idLista);

        result.use(Results.http()).body(SrConfiguracao.buscaParaConfiguracaoInsercaoAutomaticaListaJSON(lista.getListaAtual(), mostrarDesativados));
    }

    @Path("/configuracaoAutomaticaGravar")
    public void configuracaoAutomaticaGravar(SrConfiguracao configuracao, List<SrItemConfiguracao> itemConfiguracaoSet, List<SrAcao> acoesSet) throws Exception {
        configuracao.setAcoesSet(acoesSet);
        configuracao.setItemConfiguracaoSet(itemConfiguracaoSet);
        configuracao.salvarComoInclusaoAutomaticaLista(configuracao.getListaPrioridade());
        result.use(Results.http()).body(configuracao.toVO().toJson());
    }

    @Path("/desativarConfiguracaoAutomaticaGravar")
    public void desativarConfiguracaoAutomaticaGravar(Long id) throws Exception {
        SrConfiguracao configuracao = ContextoPersistencia.em().find(SrConfiguracao.class, id);
        configuracao.finalizar();
        result.use(Results.http()).body(configuracao.toVO().toJson());
    }

    @Path("/reativarConfiguracaoAutomaticaGravar")
    public void reativarConfiguracaoAutomaticaGravar(Long id) throws Exception {
        SrConfiguracao configuracao = ContextoPersistencia.em().find(SrConfiguracao.class, id);
        configuracao.salvarComHistorico();
        result.use(Results.http()).body(configuracao.toVO().toJson());
    }

    @Path("/buscarPermissoesLista")
    public void buscarPermissoesLista(Long idLista) throws Exception {
        List<SrConfiguracao> permissoes;

        if (idLista != null) {
            SrLista lista = SrLista.AR.findById(idLista);
            permissoes = new ArrayList<SrConfiguracao>(lista.getPermissoes(getTitular().getLotacao(), getCadastrante()));
            permissoes = SrConfiguracao.listarPermissoesUsoLista(lista, false);
        } else
            permissoes = new ArrayList<SrConfiguracao>();

        result.use(Results.http()).body(SrConfiguracao.convertToJSon(permissoes));
    }

    @Path("/gravarLista")
    public void gravarLista(SrLista lista) throws Exception {
        lista.setLotaCadastrante(getLotaTitular());
        validarFormEditarLista(lista);
        lista.salvarComHistorico();
        SrListaVO srListaVO = getSrListaVOComPermissoes(lista);
        result.use(Results.http()).body(srListaVO.toJson());
    }

    private SrListaVO getSrListaVOComPermissoes(SrLista lista) {
        SrListaVO srListaVO = lista.toVO();
        srListaVO.setPodeConsultar(lista.podeConsultar(getLotaTitular(), getTitular()));
        srListaVO.setPodeEditar(lista.podeEditar(getLotaTitular(), getTitular()));
        return srListaVO;
    }

    private void validarFormEditarLista(SrLista lista) {
        if (lista.getNomeLista() == null || lista.getNomeLista().trim().equals("")) {
            srValidator.addError("lista.nomeLista", "Nome da Lista no informados");
        }

        if (srValidator.hasErrors()) {
            enviarErroValidacao();
        }
    }

    @Path("/desativarLista")
    public void desativarLista(Long id, boolean mostrarDesativados) throws Exception {
        SrLista lista = SrLista.AR.findById(id);
        lista.finalizar();

        result.use(Results.http()).body(lista.toJson());
    }

    @Path("/reativarLista")
    public void reativarLista(Long id, boolean mostrarDesativados) throws Exception {
        SrLista lista = SrLista.AR.findById(id);
        lista.salvarComHistorico();
        result.use(Results.http()).body(lista.toJson());
    }

    @SuppressWarnings("unchecked")
    @Path("/exibirLista/{id}")
    public void exibirLista(Long id) throws Exception {
        SrLista lista = SrLista.AR.findById(id);
        if (lista == null)
        	throw new AplicacaoException("Lista não encontrada");
        List<CpOrgaoUsuario> orgaos = ContextoPersistencia.em().createQuery("from CpOrgaoUsuario").getResultList();
        List<CpComplexo> locais = CpComplexo.AR.all().fetch();
        List<SrTipoPermissaoLista> tiposPermissao = SrTipoPermissaoLista.AR.all().fetch();
        SrSolicitacaoFiltro filtro = new SrSolicitacaoFiltro();
        SrSolicitacaoListaVO solicitacaoListaVO;
        String tiposPermissaoJson = new Gson().toJson(tiposPermissao);
        filtro.setIdListaPrioridade(id);
        lista = lista.getListaAtual();
        String jsonPrioridades = SrPrioridade.getJSON().toString();

        if (!lista.podeConsultar(getLotaTitular(), getTitular())) {
            throw new AplicacaoException("Exibi\u00e7\u00e3o n\u00e3o permitida");
        }

        solicitacaoListaVO = new SrSolicitacaoListaVO(filtro, true, "", false, getLotaTitular(), getCadastrante());

        result.include(LISTA, lista);
        result.include(PODE_REMOVER, lista.podeRemover(getLotaTitular(), getTitular()));
        result.include(PODE_EDITAR, lista.podeEditar(getLotaTitular(), getTitular()));
        result.include(PODE_PRIORIZAR, lista.podePriorizar(getLotaTitular(), getTitular()));
        result.include(ORGAOS, orgaos);
        result.include(LOCAIS, locais);
        result.include(TIPOS_PERMISSAO, tiposPermissao);
        result.include(SOLICITACAO_LISTA_VO, solicitacaoListaVO);
        result.include(FILTRO, filtro);
        result.include(TIPOS_PERMISSAO_JSON, tiposPermissaoJson);
        result.include("jsonPrioridades", jsonPrioridades);
        result.include(PRIORIDADE_LIST, SrPrioridade.values());

        result.include("lotacaoParaInclusaoAutomaticaSel", new DpLotacaoSelecao());
        result.include("prioridades", SrPrioridade.getValoresEmOrdem());
        result.include(LOTA_TITULAR, getLotaTitular());
        result.include(CADASTRANTE, getCadastrante());

        PessoaLotaFuncCargoSelecaoHelper.adicionarCamposSelecao(result);
    }

    @Path("/gravar")
    public void gravar(SrSolicitacao solicitacao) throws Exception {
    	//Edson: por causa do detach no ObjetoObjectInstantiator:
    	if (solicitacao.getSolicitacaoInicial() != null){
    		solicitacao.setSolicitacaoInicial(SrSolicitacao.AR.findById(solicitacao.getSolicitacaoInicial().getId())); 
    		solicitacao.getSolicitacaoFilhaSet();
    	}
    	
    	//Edson: antigamente, ao regravar uma solicitação que já estava em elaboração, o atributo arquivo era
    	//setado como null pelo Play automaticamente, mas agora os atributos vazios são eliminados do request
    	if (solicitacao.getArquivo() != null && solicitacao.getArquivo().getId() != null)
    		solicitacao.setArquivo(null);
    	
        if (!solicitacao.isRascunho() && !validarFormEditar(solicitacao)) {
        	incluirListasEdicaoSolicitacao(solicitacao);
            validator.onErrorUsePageOf(SolicitacaoController.class).editar(solicitacao.getSiglaCompacta(), null, null, null, null);
        	return;
        }
        solicitacao.salvar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
        result.redirectTo(SolicitacaoController.class).exibir(solicitacao.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    private void incluirListasEdicaoSolicitacao(SrSolicitacao solicitacao) throws Exception {
    	result.include(SOLICITACAO, solicitacao);
        result.include("locais", ContextoPersistencia.em().createQuery("from CpComplexo").getResultList());
        result.include("formaAcompanhamentoList", SrFormaAcompanhamento.values());
        result.include("gravidadeList", SrGravidade.values());
        result.include("tipoMotivoEscalonamentoList", SrTipoMotivoEscalonamento.values());
        result.include("urgenciaList", SrUrgencia.values());
        result.include("tendenciaList", SrTendencia.values());
        result.include(PRIORIDADE_LIST, SrPrioridade.values());
        result.include("locaisDisponiveis", solicitacao.getLocaisDisponiveis());
        result.include("meiosComunicadaoList", SrMeioComunicacao.values());
        result.include("podeUtilizarServicoSigaGC", podeUtilizarServico("SIGA;GC"));
	}

	private boolean validarFormEditar(SrSolicitacao solicitacao) throws Exception {
        if (solicitacao.getSolicitante() == null || solicitacao.getSolicitante().getId() == null) {
            validator.add(new ValidationMessage("Solicitante n\u00e3o informado", "solicitacao.solicitante"));
        }
        if (solicitacao.getItemConfiguracao() == null || solicitacao.getItemConfiguracao().getId() == null) {
            validator.add(new ValidationMessage("Item n&atilde;o informado", "solicitacao.itemConfiguracao"));
        }
        if (solicitacao.getAcao() == null || solicitacao.getAcao().getId() == null) {
            validator.add(new ValidationMessage("A&ccedil&atilde;o n&atilde;o informada", "solicitacao.acao"));
        }

        if (solicitacao.getDescrSolicitacao() == null || "".equals(solicitacao.getDescrSolicitacao().trim())) {
            validator.add(new ValidationMessage("Descri&ccedil&atilde;o n&atilde;o informada", "solicitacao.descrSolicitacao"));
        }

        Map<Long, Boolean> obrigatorio = solicitacao.getObrigatoriedadeTiposAtributoAssociados();
        for (int i = 0; i < solicitacao.getAtributoSolicitacaoSet().size(); i++) {
        	SrAtributoSolicitacao att = solicitacao.getAtributoSolicitacaoSet().get(i);

            // Para evitar NullPointerExcetpion quando nao encontrar no Map
            if (Boolean.TRUE.equals(obrigatorio.get(att.getAtributo().getIdAtributo()))) {
                if ((att.getValorAtributoSolicitacao() == null || "".equals(att.getValorAtributoSolicitacao().trim()))) {
                	validator.add(new ValidationMessage(att.getAtributo().getNomeAtributo() + " n&atilde;o informado", "solicitacao.atributoSolicitacaoMap[" + i + "]"));
                }
            }
        }

        return !validator.hasErrors();
    }

	public Boolean todoOContexto() {
         return Boolean.parseBoolean(getRequest().getParameter("todoOContexto"));
    }

    public Boolean ocultas() {
         return Boolean.parseBoolean(getRequest().getParameter("ocultas"));
    }
    
    @Path("/exibir/{sigla}/{todoOContexto}/{ocultas}")
    public void exibirComParametros(String sigla, Boolean todoOContexto, Boolean ocultas) throws Exception {
        result.forwardTo(this).exibir(sigla, todoOContexto, ocultas);
    }

    @Path("/exibir/{sigla}")
    public void exibir(String sigla, Boolean todoOContexto, Boolean ocultas) throws Exception {
        
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
    	
        if (solicitacao == null)
            throw new AplicacaoException("Esta solicita\u00e7\u00e3o foi exclu\u00edda");

        SrMovimentacao movimentacao = new SrMovimentacao(solicitacao);

        List<DpPessoa> atendentes = solicitacao.getPessoasAtendentesDisponiveis();

        if (todoOContexto == null)
            todoOContexto = solicitacao.isParteDeArvore();
        
        if (ocultas == null)
            ocultas = false;

        Set<SrMovimentacao> movs = solicitacao.getMovimentacaoSet(ocultas, null, false, todoOContexto, !ocultas, false);

        result.include(SOLICITACAO, solicitacao);
        result.include("movimentacao", movimentacao);
        if (movimentacao.getAtendente() != null) {
            result.include("idPessoa",movimentacao.getAtendente().getId());
        }
        result.include("todoOContexto", todoOContexto);
        result.include("ocultas", ocultas);
        result.include("movs", movs);
        result.include("atendentes", atendentes);
        result.include("motivosPendencia",SrTipoMotivoPendencia.values());
        result.include(PRIORIDADE_LIST, SrPrioridade.values());
    }

    /*@Path("/listarSolicitacoesRelacionadas")
    public void listarSolicitacoesRelacionadas(SrSolicitacaoFiltro solicitacao, List<SrAtributoSolicitacaoMap> atributoSolicitacaoMap) throws Exception {

        //solicitacao.setAtributoSolicitacaoMap(atributoSolicitacaoMap);
        List<Object[]> solicitacoesRelacionadas = solicitacao.buscarSimplificado();

        result.include("solicitacoesRelacionadas", solicitacoesRelacionadas);
    }*/

    @SuppressWarnings("unchecked")
    @Path("/buscar")
    public void buscar(SrSolicitacaoFiltro filtro, String propriedade, boolean popup, boolean telaDeListas) throws Exception {
        
        if (filtro != null && filtro.isPesquisar()){
        	filtro.carregarSelecao();
        	SrSolicitacaoListaVO solicitacaoListaVO = new SrSolicitacaoListaVO(filtro, telaDeListas, propriedade, popup, getLotaTitular(), getCadastrante());
        	result.use(Results.json()).withoutRoot().from(solicitacaoListaVO).excludeAll().include("recordsFiltered").include("data").serialize();
        } else {
        	if (filtro == null){
        		filtro = new SrSolicitacaoFiltro();
        	}
        	filtro.carregarSelecao();
        	result.include("solicitacaoListaVO", new SrSolicitacaoListaVO(filtro, false, propriedade, popup, getLotaTitular(), getCadastrante()));
        	result.include("tipos", new String[] { "Pessoa", "Lota\u00e7\u00e3o" });
        	result.include("marcadores", ContextoPersistencia.em().createQuery("select distinct cpMarcador from SrMarca").getResultList());
        	result.include("filtro", filtro);
        	result.include("propriedade", propriedade);
        	result.include("popup", popup);
        	result.include("listasPrioridade", SrLista.listar(false));
        	result.include("prioridadesEnum", SrPrioridade.values());
        }
    }

    public List<SrAtributo> atributosDisponiveisAdicaoConsulta(SrSolicitacaoFiltro filtro) throws Exception {
        List<SrAtributo> listaAtributosAdicao = new ArrayList<SrAtributo>();
        List<SrAtributoSolicitacaoMap> atributoMap = filtro.getAtributoSolicitacaoMap();

        for (SrAtributo srAtributo : SrAtributo.listarParaSolicitacao(Boolean.FALSE)) {
        	SrAtributoSolicitacaoMap atrib = new SrAtributoSolicitacaoMap(srAtributo.getIdAtributo(),srAtributo.getDescrAtributo());
            if (!atributoMap.contains(atrib)) {
                listaAtributosAdicao.add(srAtributo);
            }
        }
        return listaAtributosAdicao;
    }

	@Path({ "/editar", "/editar/{sigla}"})
    public void editar(String sigla, SrSolicitacao solicitacao, String item, String acao, String descricao) throws Exception {

		if (sigla != null){
			solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
		} else {
			if (solicitacao != null){
				//Edson: por causa do detach no ObjetoObjectInstantiator:
				if (solicitacao.getSolicitacaoInicial() != null)
					solicitacao.setSolicitacaoInicial(SrSolicitacao.AR.findById(solicitacao.getSolicitacaoInicial().getId()));
			} else {
				solicitacao = new SrSolicitacao();
				solicitacao.setSolicitante(getTitular());
				solicitacao.setLotaCadastrante(getLotaCadastrante());
				solicitacao.setTitular(getTitular());
				solicitacao.setLotaTitular(getLotaTitular());
			}
			
			solicitacao.deduzirLocalRamalEMeioContato();
			
			if (solicitacao.getItemConfiguracao() != null && !solicitacao.getItensDisponiveis().contains(solicitacao.getItemConfiguracao())){
				solicitacao.setItemConfiguracao(null);
			}
			
			if (solicitacao.getAcao() != null){
				boolean containsAcao = false;
				for (List<SrTarefa> tarefas : solicitacao.getAcoesEAtendentes().values())
					for (SrTarefa t : tarefas)
						if (t.getAcao().equivale(solicitacao.getAcao()))
							containsAcao = true;
				if (!containsAcao)
					solicitacao.setAcao(null);
			}
			
			solicitacao.associarPrioridadePeloGUT();
		} 
        	        
        if (solicitacao.getDtOrigem() == null)
            solicitacao.setDtOrigem(new Date());
        if (solicitacao.getDtIniEdicao() == null)
            solicitacao.setDtIniEdicao(new Date());
        
        solicitacao.atualizarAcordos();
        
        incluirListasEdicaoSolicitacao(solicitacao);
    }
	
    @Path("/retirarDeLista")
    public void retirarDeLista(String sigla, Long idLista) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        SrLista lista = SrLista.AR.findById(idLista);
        solicitacao.retirarDeLista(lista, getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
        result.redirectTo(this).exibirLista(idLista);
    }

    private SrSolicitacao criarSolicitacaoComSolicitante() {
        SrSolicitacao solicitacao = new SrSolicitacao();
        solicitacao.setCadastrante(getCadastrante());
        solicitacao.setSolicitante(getTitular());

        return solicitacao;
    }

    @Path("/incluirEmLista")
    public void incluirEmLista(String sigla) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        solicitacao = solicitacao.getSolicitacaoAtual();
        List<SrPrioridade> prioridades = SrPrioridade.getValoresEmOrdem();

        result.include(SOLICITACAO, solicitacao);
        result.include("prioridades", prioridades);
    }

    @Path("/incluirEmListaGravar")
    public void incluirEmListaGravar(String sigla, Long idLista, SrPrioridade prioridade, boolean naoReposicionarAutomatico) throws Exception {
        if (idLista == null) {
            throw new AplicacaoException("Selecione a lista para inclus\u00e3o da solicita\u00e7\u00e3o");
        }
        if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        SrLista lista = SrLista.AR.findById(idLista);
        solicitacao.incluirEmLista(lista, getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), prioridade, naoReposicionarAutomatico);
        result.redirectTo(this).exibir(solicitacao.getSiglaCompacta(), todoOContexto(), ocultas());
    }
    
    @Path("/reclassificar")
    public void reclassificar(String sigla, SrItemConfiguracao itemConfiguracao) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
    	
    	solicitacao.setTitular(getTitular());
        solicitacao.setLotaTitular(getLotaTitular());
        if (itemConfiguracao != null)
        	solicitacao.setItemConfiguracao(itemConfiguracao);
        
        //Edson: para evitar que o JPA tente salvar a solicitação por causa dos set's chamados
        em().detach(solicitacao);
    	if (solicitacao.getSolicitacaoInicial() != null)
    		solicitacao.setSolicitacaoInicial(SrSolicitacao.AR.findById(solicitacao.getSolicitacaoInicial().getId()));
        
        result.include("itemConfiguracao", solicitacao.getItemAtual());
        result.include("acao", solicitacao.getAcaoAtual());
        result.include("siglaCompacta", solicitacao.getSiglaCompacta());
        result.include("solicitante", solicitacao.getSolicitante());
        result.include("local", solicitacao.getLocal());
        result.include("acoesEAtendentes", solicitacao.getAcoesEAtendentes());
    }
        
    @Path("/reclassificarGravar")
    public void reclassificarGravar(String sigla, SrItemConfiguracao itemConfiguracao, SrAcao acao) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.reclassificar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), itemConfiguracao, acao);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/fechar")
    public void fechar(String sigla, SrItemConfiguracao itemConfiguracao) throws Exception {
    	reclassificar(sigla, itemConfiguracao);        
    }
    
    @Path("/fecharGravar")
    public void fecharGravar(String sigla, SrItemConfiguracao itemConfiguracao, SrAcao acao, String motivo) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.fechar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), itemConfiguracao, acao, motivo);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }
    
    @Path("/erPesquisa")
    public void responderPesquisa(String sigla) throws Exception {
        /*
         * SrSolicitacao sol = SrSolicitacao.findById(id); SrPesquisa pesquisa = sol.getPesquisaDesignada(); if (pesquisa == null) throw new
         * Exception("NÃ£o foi encontrada nenhuma pesquisa designada para esta solicitaÃ§Ã£o."); pesquisa = SrPesquisa.findById(pesquisa.idPesquisa); pesquisa = pesquisa.getPesquisaAtual(); render(id,
         * pesquisa);
         */
    }


    @Path("/responderPesquisaGravar")
    public void responderPesquisaGravar(String sigla, Map<Long, String> respostaMap) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.responderPesquisa(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), respostaMap);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/baixar/{idArquivo}")
    public Download baixar(Long idArquivo) throws Exception {
        SrArquivo arq = SrArquivo.AR.findById(idArquivo);
        final InputStream inputStream = new ByteArrayInputStream(arq.getBlob());
        return new InputStreamDownload(inputStream, "text/plain", arq.getNomeArquivo());
    }

    @Path("/escalonar")
    public void escalonar(String sigla, SrItemConfiguracao itemConfiguracao) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
    	
    	solicitacao.setTitular(getTitular());
        solicitacao.setLotaTitular(getLotaTitular());
        if (itemConfiguracao != null)
        	solicitacao.setItemConfiguracao(itemConfiguracao);
        
        //Edson: para evitar que o JPA tente salvar a solicitação por causa dos set's chamados
        em().detach(solicitacao);
    	if (solicitacao.getSolicitacaoInicial() != null)
    		solicitacao.setSolicitacaoInicial(SrSolicitacao.AR.findById(solicitacao.getSolicitacaoInicial().getId()));
    	
    	CpConfiguracao filtro = new CpConfiguracao();
        filtro.setDpPessoa(getTitular());
        filtro.setLotacao(getLotaTitular());
        filtro.setBuscarPorPerfis(true);
        filtro.setCpTipoConfiguracao((CpTipoConfiguracao)CpTipoConfiguracao.AR.findById(CpTipoConfiguracao.TIPO_CONFIG_SR_ESCALONAMENTO_SOL_FILHA));
        CpSituacaoConfiguracao situacao = SrConfiguracaoBL.get().buscaSituacao(filtro,
                        new int[] { 0 }, null);
        boolean criarFilhaDefault = false;
        if (situacao != null
                        && (situacao.getIdSitConfiguracao() == CpSituacaoConfiguracao.SITUACAO_PODE
                        || situacao.getIdSitConfiguracao() == CpSituacaoConfiguracao.SITUACAO_DEFAULT))
                criarFilhaDefault = true;
        
        result.include("itemConfiguracao", solicitacao.getItemAtual());
        result.include("acao", solicitacao.getAcaoAtual());
        result.include("siglaCompacta", solicitacao.getSiglaCompacta());
        result.include("solicitante", solicitacao.getSolicitante());
        result.include("local", solicitacao.getLocal());
        result.include("acoesEAtendentes", solicitacao.getAcoesEAtendentes());
        result.include("criarFilhaDefault", criarFilhaDefault);
        result.include("isFechadoAutomaticamente", solicitacao.isFechadoAutomaticamente());
        result.include("solicitacaoPai", solicitacao.getSolicitacaoPai());
        result.include("isPai", solicitacao.isPai());
        result.include("codigo", solicitacao.isFilha() ? solicitacao.getSolicitacaoPai().getCodigo() : solicitacao.getCodigo());
        result.include(TIPO_MOTIVO_ESCALONAMENTO_LIST, SrTipoMotivoEscalonamento.values());
        
        
    }

    @Path("/escalonarGravar")
    public void escalonarGravar(String sigla, SrItemConfiguracao itemConfiguracao, SrAcao acao, Long idAtendente, Long idAtendenteNaoDesignado, Long idDesignacao, SrTipoMotivoEscalonamento motivo, String descricao,
            Boolean criaFilha, Boolean fechadoAuto) throws Exception {
        if (itemConfiguracao == null || itemConfiguracao.getId() == null || acao == null || acao.getIdAcao() == null || acao.getIdAcao().equals(0L))
            throw new AplicacaoException("Opera\u00e7\u00e3o n\u00e3o permitida. Necessario informar um item de configura\u00e7\u00e3o e uma a\u00e7\u00e3o.");
        if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);

        DpLotacao atendenteNaoDesignado = null;
        DpLotacao atendente = null;
        if (idAtendente != null)
            atendente = ContextoPersistencia.em().find(DpLotacao.class, idAtendente);
        if (idAtendenteNaoDesignado != null)
            atendenteNaoDesignado = ContextoPersistencia.em().find(DpLotacao.class, idAtendenteNaoDesignado);

        if (criaFilha) {
            if (fechadoAuto != null) {
                solicitacao.setFechadoAutomaticamente(fechadoAuto);
                solicitacao.save();
            }
            SrSolicitacao filha = null;
            if (solicitacao.isFilha())
                filha = solicitacao.getSolicitacaoPai().criarFilhaSemSalvar();
            else
                filha = solicitacao.criarFilhaSemSalvar();
            filha.setItemConfiguracao(SrItemConfiguracao.AR.findById(itemConfiguracao.getId()));
            filha.setAcao(SrAcao.AR.findById(acao.getIdAcao()));
            filha.setDesignacao(SrConfiguracao.AR.findById(idDesignacao));
            //filha.setDescrSolicitacao(descricao);
            if (idAtendenteNaoDesignado != null)
                filha.setAtendenteNaoDesignado(atendenteNaoDesignado);
            filha.salvar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
            result.redirectTo(this).exibir(filha.getSiglaCompacta(), todoOContexto(), ocultas());
        } else {
            SrMovimentacao mov = new SrMovimentacao(solicitacao);
            mov.setTipoMov(SrTipoMovimentacao.AR.findById(SrTipoMovimentacao.TIPO_MOVIMENTACAO_ESCALONAMENTO));
            mov.setItemConfiguracao(SrItemConfiguracao.AR.findById(itemConfiguracao.getId()));
            mov.setAcao(SrAcao.AR.findById(acao.getIdAcao()));
            mov.setLotaAtendente(atendenteNaoDesignado != null ? atendenteNaoDesignado : atendente);
            // Edson: isso abaixo talvez pudesse valer pra todas as movimentacoes e ficar la no
            // mov.checarCampos()
            if (solicitacao.getAtendente() != null && !mov.getLotaAtendente().equivale(solicitacao.getLotaAtendente()))
                mov.setAtendente(null);
            mov.setMotivoEscalonamento(motivo);
            mov.setDesignacao(SrConfiguracao.AR.findById(idDesignacao));
            mov.setDescrMovimentacao(descricao + " Motivo: " + mov.getMotivoEscalonamento().getDescrTipoMotivoEscalonamento() + "; Item: " + mov.getItemConfiguracao().getTituloItemConfiguracao() + "; Ação: " + mov.getAcao().getTituloAcao()
                    + "; Atendente: " + mov.getLotaAtendente().getSigla());
            mov.salvar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
            
            solicitacao.setDnmItemConfiguracao(mov.getItemConfiguracao());
            solicitacao.setDnmAcao(mov.getAcao());
            solicitacao.save();
            
            result.redirectTo(this).exibir(solicitacao.getSiglaCompacta(), todoOContexto(), ocultas());
        }
    }

    @Path("/vincular")
    public void vincular(String sigla, SrSolicitacao solRecebeVinculo, String justificativa) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.vincular(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), solRecebeVinculo, justificativa);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/juntar")
    public void juntar(String sigla, SrSolicitacao solRecebeJuntada, String justificativa) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.juntar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), solRecebeJuntada, justificativa);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/desentranhar")
    public void desentranhar(String sigla, String justificativa) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.desentranhar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), justificativa);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/cancelar")
    public void cancelar(String sigla) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.cancelar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/reabrir")
    public void reabrir(String sigla) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.reabrir(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/deixarPendente")
    public void deixarPendente(String sigla, SrTipoMotivoPendencia motivo, String calendario, String horario, String detalheMotivo) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.deixarPendente(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), motivo, calendario, horario, detalheMotivo);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/excluir")
    public void excluir(String sigla) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.excluir();
        result.redirectTo(this).editar(null, null, null, null, null);
    }

    @Path("/anexarArquivo")
    public void anexarArquivo(SrMovimentacao movimentacao) throws Exception {
        movimentacao.salvar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
        result.redirectTo(this).exibir(movimentacao.getSolicitacao().getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/termoAtendimento")
    public void termoAtendimento(String sigla) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao solicitacao = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        result.include(SOLICITACAO, solicitacao);
    }

    @Path("/desfazerUltimaMovimentacao")
    public void desfazerUltimaMovimentacao(String sigla) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    		
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.desfazerUltimaMovimentacao(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/alterarPrioridade")
    public void alterarPrioridade(String sigla, SrPrioridade prioridade) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.alterarPrioridade(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), prioridade);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/terminarPendencia")
    public void terminarPendencia(String sigla, String descricao, Long idMovimentacao) throws Exception {
    	if (sigla == null || sigla.trim().equals(""))
    		throw new AplicacaoException("Número não informado");
    	SrSolicitacao sol = (SrSolicitacao) new SrSolicitacao().setLotaTitular(getLotaTitular()).selecionar(sigla);
        sol.terminarPendencia(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular(), descricao, idMovimentacao);
        result.redirectTo(this).exibir(sol.getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/darAndamento")
    public void darAndamento(SrMovimentacao movimentacao) throws Exception {
    	if (movimentacao == null || movimentacao.getDescrMovimentacao() == null || movimentacao.getDescrMovimentacao().trim().equals("") 
    			&& movimentacao.getAtendente() == null)
    		throw new AplicacaoException("Não foram informados dados para o andamento");
        movimentacao.setTipoMov(SrTipoMovimentacao.AR.findById(SrTipoMovimentacao.TIPO_MOVIMENTACAO_ANDAMENTO));
        if (movimentacao.getDescrMovimentacao() == null || movimentacao.getDescrMovimentacao().trim().equals("") && movimentacao.isTrocaDePessoaAtendente()){
        	if (movimentacao.getAtendente() != null)
        		movimentacao.setDescrMovimentacao("Atribuindo a " + movimentacao.getAtendente().getNomeAbreviado());
        	else movimentacao.setDescrMovimentacao("Retirando atribuição");
        }
        movimentacao.salvar(getCadastrante(), getCadastrante().getLotacao(), getTitular(), getLotaTitular());
        result.redirectTo(this).exibir(movimentacao.getSolicitacao().getSiglaCompacta(), todoOContexto(), ocultas());
    }

    @Path("/priorizarLista")
    public void priorizarLista(List<SrPrioridadeSolicitacao> listaPrioridadeSolicitacao, Long id) throws Exception {
    	for (SrPrioridadeSolicitacao pNova : listaPrioridadeSolicitacao){
    		SrPrioridadeSolicitacao p = SrPrioridadeSolicitacao.AR.findById(pNova.getId());
    		if (p.getNumPosicao() != pNova.getNumPosicao()
    				|| p.getPrioridade() != pNova.getPrioridade()
    				|| p.getNaoReposicionarAutomatico() != pNova.getNaoReposicionarAutomatico()){
    			p.setNumPosicao(pNova.getNumPosicao());
    			p.setPrioridade(pNova.getPrioridade());
    			p.setNaoReposicionarAutomatico(pNova.getNaoReposicionarAutomatico());
    			p.save();
    		} else em().detach(p);
    	}
        //exibirLista(id);
        result.use(Results.http()).setStatusCode(200);
    }

    @Get
	@Post
    @Path("/selecionar")
    public void selecionar(String sigla, boolean retornarCompacta) throws Exception {
        SrSolicitacao sel = new SrSolicitacao();
        sel.setLotaTitular(getLotaTitular());
        sel = (SrSolicitacao) sel.selecionar(sigla);

        result.include("retornarCompacta", retornarCompacta);
        if (sel != null) {
        	result.forwardTo(SelecaoController.class).ajaxRetorno(sel);
        }
        else {
        	result.forwardTo(SelecaoController.class).ajaxVazio();
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Path("/gadget")
    public void gadget() {
        Query query = ContextoPersistencia.em().createNamedQuery("contarSrMarcas");
        query.setParameter("idPessoaIni", getTitular().getIdInicial());
        query.setParameter("idLotacaoIni", getLotaTitular().getIdInicial());
        List contagens = query.getResultList();
        result.include("contagens", contagens);
    }
    
}