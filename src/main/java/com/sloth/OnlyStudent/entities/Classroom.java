package com.sloth.OnlyStudent.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.sloth.OnlyStudent.entities.DTO.StudentIdDTO;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
	
	// Variável para armazenar a quantidade de alunos
    @Transient
    private int totalAlunos;
    
 // Variável para armazenar a quantidade de materiais
    @Transient
    private int totalMateriais;
    
	@ManyToOne
    @JoinColumn(name = "educator_id", nullable = false)
	private Educator educator;
	
	@OneToMany(mappedBy = "turma", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Material> materials;
	
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
	
    @PostLoad
    public void calculateTotalAlunos() {
        this.totalAlunos = (alunos != null) ? alunos.size() : 0;
        this.totalMateriais = (materials != null) ? materials.size() : 0;
    }
    
 // Método para obter o ID e o nome dos alunos
    public Set<StudentIdDTO> getAlunosInfo() {
        return alunos.stream()
                     .map(aluno -> new StudentIdDTO(aluno.getId(), aluno.getName())) // Mapeia cada Student para um StudentDTO
                     .collect(Collectors.toSet()); // Coleta os DTOs em um Set
    }
		
}
