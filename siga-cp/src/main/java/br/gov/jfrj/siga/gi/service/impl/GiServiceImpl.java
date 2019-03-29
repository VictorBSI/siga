/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.gi.service.impl;

import br.gov.jfrj.siga.gi.integracao.IntegracaoLdapViaWebService;

import java.util.List;

import javax.jws.WebService;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import br.gov.jfrj.siga.acesso.ConfiguracaoAcesso;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.GeraMessageDigest;
import br.gov.jfrj.siga.cp.CpIdentidade;
import br.gov.jfrj.siga.cp.CpServico;
import br.gov.jfrj.siga.cp.bl.Cp;
import br.gov.jfrj.siga.cp.bl.CpPropriedadeBL;
import br.gov.jfrj.siga.dp.DpCargo;
import br.gov.jfrj.siga.dp.DpFuncaoConfianca;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.dp.dao.DpLotacaoDaoFiltro;
import br.gov.jfrj.siga.dp.dao.DpPessoaDaoFiltro;
import br.gov.jfrj.siga.gi.service.GiService;

/**
 * Esta classe implementa os métodos de gestão de identidade O acesso à esta
 * classe é realizado via web-services, com interfaces definidas no módulo
 * siga-ws, conforme o padrão adotados para o SIGA.
 * 
 * @author tah
 * 
 */
@WebService(serviceName = "GiService", endpointInterface = "br.gov.jfrj.siga.gi.service.GiService", targetNamespace = "http://impl.service.gi.siga.jfrj.gov.br/")
public class GiServiceImpl implements GiService {
	
	private static final String _MODO_AUTENTICACAO_BANCO = "banco";
	private static final String _MODO_AUTENTICACAO_LDAP = "ldap";
	//TODO caso nao exista a propriedade no JBOSS, autenticar via banco
	private static final String _MODO_AUTENTICACAO_DEFAULT = _MODO_AUTENTICACAO_BANCO;
	
    private boolean autenticaViaBanco(CpIdentidade identidade, String senha) {
    	try {
    		final String hashAtual = GeraMessageDigest.executaHash(senha.getBytes(), "MD5");
    		if (identidade != null && identidade.getDscSenhaIdentidade().equals(hashAtual)) return true;
		} catch (Exception e) {
			return false;
		}
    	return false;
    }
    
    private boolean autenticaViaLdap(String matricula, String senha) {
    	try {
			return IntegracaoLdapViaWebService.getInstancia().autenticarUsuario(matricula, senha);
		} catch (Exception e) {
			return false;
		}
    }
    
    private String buscarModoAutenticacao(String orgao) {
    	String retorno = _MODO_AUTENTICACAO_DEFAULT;
    	CpPropriedadeBL props = new CpPropriedadeBL();
    	try {
			String modo = props.getModoAutenticacao(orgao);
			if(modo != null) 
				retorno = modo;
		} catch (Exception e) {
		}
    	return retorno;
    }
    
    @Override
    public String login(String matricula, String senha) {
		String resultado = "";

		CpIdentidade id = null;
		CpDao dao = CpDao.getInstance();
		id = dao.consultaIdentidadeCadastrante(matricula, true);
		String orgaoLogin = id.getCpOrgaoUsuario().getSiglaOrgaoUsu();
    	
		String modoAut = buscarModoAutenticacao(orgaoLogin);

		try {
			if(modoAut.equals(_MODO_AUTENTICACAO_BANCO)) {
				if (autenticaViaBanco(id, senha)) {
					resultado = parseLoginResult(id);
				}
			} else if(modoAut.equals(_MODO_AUTENTICACAO_LDAP)) {
				if(autenticaViaLdap(matricula, senha)) {
					resultado = parseLoginResult(id);
				}
			}

		} catch (AplicacaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultado;
	}

    @Override
	public String dadosUsuario(String matricula) {
		String resultado = "";
		try {
			CpDao dao = CpDao.getInstance();

			CpIdentidade id = null;
			id = dao.consultaIdentidadeCadastrante(matricula, true);
			if (id != null) {
				resultado = parseLoginResult(id);
			}
		} catch (AplicacaoException e) {
			e.printStackTrace();
		}
		return resultado;
	}

	private String parseLoginResult(CpIdentidade id) {
		JSONObject pessoa = new JSONObject();
		JSONObject lotacao = new JSONObject();
		JSONObject cargo = new JSONObject();
		JSONObject funcao = new JSONObject();

		try {
			DpPessoa p = id.getPessoaAtual();
			pessoa.put("idPessoa", p.getId());
			pessoa.put("idExternaPessoa", p.getIdExterna());
			pessoa.put("matriculaPessoa", p.getMatricula());
			pessoa.put("cpf", p.getCpfPessoa());
			pessoa.put("siglaPessoa", p.getSiglaCompleta());
			pessoa.put("nomePessoa", p.getNomePessoa());
			pessoa.put("emailPessoa", p.getEmailPessoaAtual());
			pessoa.put("siglaPessoaWEmul", p.getSiglaPessoa());
			pessoa.put("tipoServidor", p.getCpTipoPessoa() != null ? p
					.getCpTipoPessoa().getIdTpPessoa() : "null");

			DpLotacao l = p.getLotacao();
			lotacao.put("idLotacao", l.getId());
			lotacao.put("idExternaLotacao", l.getIdExterna());
			lotacao.put("nomeLotacao", l.getNomeLotacao());
			lotacao.put("siglaLotacao", l.getSigla());
			lotacao.put("idLotacaoPai", l.getIdLotacaoPai());
			lotacao.put("idTpLotacao", l.getCpTipoLotacao() != null ? l
					.getCpTipoLotacao().getIdTpLotacao() : "null");
			lotacao.put("siglaTpLotacao", l.getCpTipoLotacao() != null ? l
					.getCpTipoLotacao().getSiglaTpLotacao() : "null");

			DpCargo c = p.getCargo();
			if (c!=null){
				cargo.put("idCargo", c.getId());
				cargo.put("idExternaCargo", c.getIdExterna());
				cargo.put("nomeCargo", c.getNomeCargo());
				cargo.put("siglaCargo", c.getSigla());
			}

			DpFuncaoConfianca f = p.getFuncaoConfianca();
			if (f !=null){
					funcao.put("idFuncaoConfianca", f.getId());
					funcao.put("idExternaFuncaoConfianca", f.getIdeFuncao());
					funcao.put("nomeFuncaoConfianca", f.getNomeFuncao());
					funcao.put("siglaFuncaoConfianca", f.getSigla());
					funcao.put("idPaiFuncaoConfianca", f.getIdFuncaoPai());
			}

			pessoa.put("lotacao", lotacao);
			pessoa.put("cargo", cargo);
			pessoa.put("funcaoConfianca", funcao);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			return pessoa.toString(2);
		} catch (JSONException e) {
			return "";
		}
	}

	@SuppressWarnings("unused")
	public String acesso(String matricula, String lotacao, String servico) {
		JSONObject servicos = new JSONObject();
		String resultado = "";
		try {
			CpDao dao = CpDao.getInstance();

			DpPessoaDaoFiltro flt = new DpPessoaDaoFiltro();
			flt.setSigla(matricula);
			DpPessoa p = (DpPessoa) dao.consultarPorSigla(flt);

			DpLotacao lot = null;
			if (lotacao != null) {
				DpLotacaoDaoFiltro fltLot = new DpLotacaoDaoFiltro();
				fltLot.setSiglaCompleta(lotacao);
				lot = (DpLotacao) dao.consultarPorSigla(fltLot);
			}

			boolean pode = Cp.getInstance().getConf().podeUtilizarServicoPorConfiguracao(p, lot, servico);

			CpServico srv = dao.consultarCpServicoPorChave(servico);

			if (p != null) {
				ConfiguracaoAcesso ac;
				ac = ConfiguracaoAcesso.gerar(null, p, lot, null, srv, null);
				if (ac != null)
					servicos.put(ac.getServico().getSigla(), ac.getSituacao()
							.getDscSitConfiguracao());
			}
			resultado = servicos.toString(2);
		} catch (AplicacaoException e) {
			return "";
		} catch (JSONException e) {
			return "";
		} catch (Exception e) {
			return "";
		}
		return resultado;
	}

	public String acessos(String matricula, String lotacao) {
		JSONObject servicos = new JSONObject();
		String resultado = "";
		try {
			CpDao dao = CpDao.getInstance();

			DpPessoaDaoFiltro flt = new DpPessoaDaoFiltro();
			flt.setSigla(matricula);
			DpPessoa p = (DpPessoa) dao.consultarPorSigla(flt);

			DpLotacao lot = null;
			if (lotacao != null) {
				DpLotacaoDaoFiltro fltLot = new DpLotacaoDaoFiltro();
				fltLot.setSiglaCompleta(lotacao);
				lot = (DpLotacao) dao.consultarPorSigla(fltLot);
			}

			if (p != null) {
				List<CpServico> l = dao.listarServicos();
				for (CpServico srv : l) {
					ConfiguracaoAcesso ac;
					try {
						ac = ConfiguracaoAcesso.gerar(null, p, lot, null, srv,
								null);
						if (ac != null)
							servicos.put(ac.getServico().getSigla(), ac
									.getSituacao().getDscSitConfiguracao());
					} catch (Exception e) {
					}
				}
			}
			resultado = servicos.toString(2);
		} catch (AplicacaoException e) {
			return "";
		} catch (JSONException e) {
			return "";
		}
		return resultado;
	}
}