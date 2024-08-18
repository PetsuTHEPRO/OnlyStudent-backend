package com.sloth.OnlyStudent.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "classroom")
@Getter
@Setter
@NoArgsConstructor
public class Classroom {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo;
	
	private String name;
	private String description;
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private Custo custo;
	
	private double price;
	
	@ManyToMany
    @JoinTable(
        name = "classroom_student",  // Nome da tabela de junção
        joinColumns = @JoinColumn(name = "classroom_id"),  // Coluna que referencia Classroom
        inverseJoinColumns = @JoinColumn(name = "student_id")  // Coluna que referencia Student
    )
    private Set<Student> alunos;
	
	@ManyToOne
    @JoinColumn(name = "educator_id", nullable = false)
	private Educator educator;
	
	@OneToMany(mappedBy = "turma")
    private Set<Material> materials;

    // Construtor padrão
    public Classroom() {
    }
    
	public Classroom(Long codigo, String name, String description, Status status, Custo custo, double price, HashSet alunos,
			Educator educator, HashSet material) {
		super();
		this.codigo = codigo;
		this.name = name;
		this.description = description;
		this.status = status;
		this.custo = custo;
		this.price = price;
		this.alunos = alunos;
		this.educator = educator;
		this.materials = material;
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Custo getCusto() {
		return custo;
	}

	public void setCusto(Custo custo) {
		this.custo = custo;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Set<Student> getAlunos() {
		return alunos;
	}

	public void setAlunos(Set<Student> alunos) {
		this.alunos = alunos;
	}

	public Educator getEducator() {
		return educator;
	}

	public void setEducator(Educator educator) {
		this.educator = educator;
	}
	
	//private ArrayList<Material> materiais;
	
	
}
