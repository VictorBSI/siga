package br.gov.jfrj.siga.sr.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.io.IOUtils;

import br.gov.jfrj.siga.model.ActiveRecord;
import br.gov.jfrj.siga.model.Objeto;

@Entity
@Table(name = "SR_ARQUIVO", schema="SIGASR")
public class SrArquivo extends Objeto {

	public static ActiveRecord<SrArquivo> AR = new ActiveRecord<>(SrArquivo.class);
	
	@Id
	@SequenceGenerator(sequenceName = "SIGASR.SR_ARQUIVO_SEQ", name = "srArquivoSeq")
	@GeneratedValue(generator = "srArquivoSeq")
	@Column(name = "ID_ARQUIVO")
	private Long idArquivo;

	@Lob
	private byte[] blob;

	@Column(name = "MIME")
	private String mime;

	@Column(name = "NOME_ARQUIVO")
	private String nomeArquivo;

	@Column(name = "DESCRICAO")
	private String descricao;

	
	
	public Long getIdArquivo() {
		return idArquivo;
	}

	public void setIdArquivo(Long idArquivo) {
		this.idArquivo = idArquivo;
	}

	public byte[] getBlob() {
		return blob;
	}

	public void setBlob(byte[] blob) {
		this.blob = blob;
	}

	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	private SrArquivo() {

	}

	private SrArquivo(File file) {
		try {

			nomeArquivo = file.getName();
			blob = IOUtils.toByteArray(new FileInputStream(file));
			mime = new javax.activation.MimetypesFileTypeMap()
					.getContentType(file);
		} catch (IOException ioe) {
			// Ver o que fazer aqui
		}
	}

	// Edson: Necessário porque é preciso garantir
	// que o SrArquivo não seja instanciado a não ser que realmente tenha sido
	// selecionado um arquivo no form (para que não surja um registro SrArquivo
	// sem conteúdo no banco)
	public static SrArquivo newInstance(File file) {
		if (file != null)
			return new SrArquivo(file);
		else
			return null;
	}

}