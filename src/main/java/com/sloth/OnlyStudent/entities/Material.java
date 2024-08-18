package com.sloth.OnlyStudent.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    private MaterialType tipo;

    private String url;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Classroom turma;
        
    public Material(Long id, String name, MaterialType tipo, String url, Classroom turma) {
        this.id = id;
        this.name = name;
        this.tipo = tipo;
        this.url = url;
        this.turma = turma;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MaterialType getTipo() {
		return tipo;
	}

	public void setTipo(MaterialType tipo) {
		this.tipo = tipo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Classroom getTurma() {
		return turma;
	}

	public void setTurma(Classroom turma) {
		this.turma = turma;
	}

}