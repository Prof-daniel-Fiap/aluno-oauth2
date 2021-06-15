package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.TipoCurso;
import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.*;
import org.hibernate.annotations.Type;

/**
 * A Curso.
 */
@Entity
@Table(name = "curso")
public class Curso implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "nome_curso")
    private String nomeCurso;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "descricao")
    private String descricao;

    @Lob
    @Column(name = "logo_curso")
    private byte[] logoCurso;

    @Column(name = "logo_curso_content_type")
    private String logoCursoContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoCurso tipo;

    @ManyToOne
    @JsonIgnoreProperties(value = { "cursos", "alunos" }, allowSetters = true)
    private Turma turma;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Curso id(Long id) {
        this.id = id;
        return this;
    }

    public String getNomeCurso() {
        return this.nomeCurso;
    }

    public Curso nomeCurso(String nomeCurso) {
        this.nomeCurso = nomeCurso;
        return this;
    }

    public void setNomeCurso(String nomeCurso) {
        this.nomeCurso = nomeCurso;
    }

    public LocalDate getDataCriacao() {
        return this.dataCriacao;
    }

    public Curso dataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
        return this;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public Curso descricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public byte[] getLogoCurso() {
        return this.logoCurso;
    }

    public Curso logoCurso(byte[] logoCurso) {
        this.logoCurso = logoCurso;
        return this;
    }

    public void setLogoCurso(byte[] logoCurso) {
        this.logoCurso = logoCurso;
    }

    public String getLogoCursoContentType() {
        return this.logoCursoContentType;
    }

    public Curso logoCursoContentType(String logoCursoContentType) {
        this.logoCursoContentType = logoCursoContentType;
        return this;
    }

    public void setLogoCursoContentType(String logoCursoContentType) {
        this.logoCursoContentType = logoCursoContentType;
    }

    public TipoCurso getTipo() {
        return this.tipo;
    }

    public Curso tipo(TipoCurso tipo) {
        this.tipo = tipo;
        return this;
    }

    public void setTipo(TipoCurso tipo) {
        this.tipo = tipo;
    }

    public Turma getTurma() {
        return this.turma;
    }

    public Curso turma(Turma turma) {
        this.setTurma(turma);
        return this;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Curso)) {
            return false;
        }
        return id != null && id.equals(((Curso) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Curso{" +
            "id=" + getId() +
            ", nomeCurso='" + getNomeCurso() + "'" +
            ", dataCriacao='" + getDataCriacao() + "'" +
            ", descricao='" + getDescricao() + "'" +
            ", logoCurso='" + getLogoCurso() + "'" +
            ", logoCursoContentType='" + getLogoCursoContentType() + "'" +
            ", tipo='" + getTipo() + "'" +
            "}";
    }
}
