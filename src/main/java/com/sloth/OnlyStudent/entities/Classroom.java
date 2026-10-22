package com.sloth.OnlyStudent.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

import com.sloth.OnlyStudent.entities.DTO.StudentIdDTO;
import com.sloth.OnlyStudent.shared.errors.AlunoJaMatriculadoException;
import com.sloth.OnlyStudent.shared.errors.InvalidClassroomException;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "classroom")
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
        name = "classroom_student",
        joinColumns = @JoinColumn(name = "classroom_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_classroom_student",
                columnNames = {"classroom_id", "student_id"})
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

	@OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Aviso> avisos = new ArrayList<>();

	
    public Classroom() {
    }

    public Classroom(Long codigo, String name, String description, Status status, Custo custo, double price, HashSet<Student> alunos,
            Educator educator, HashSet<Material> material) {
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

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Status getStatus() {
		return status;
	}

	public Custo getCusto() {
		return custo;
	}

	public double getPrice() {
		return price;
	}

	public Set<Student> getAlunos() {
		return alunos;
	}

	public int getTotalAlunos() {
		return totalAlunos;
	}

	public int getTotalMateriais() {
		return totalMateriais;
	}

	public Educator getEducator() {
		return educator;
	}

	public Set<Material> getMaterials() {
		return materials;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setCusto(Custo custo) {
		this.custo = custo;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setAlunos(Set<Student> alunos) {
		this.alunos = alunos;
	}

	public void setTotalAlunos(int totalAlunos) {
		this.totalAlunos = totalAlunos;
	}

	public void setTotalMateriais(int totalMateriais) {
		this.totalMateriais = totalMateriais;
	}

	public void setEducator(Educator educator) {
		this.educator = educator;
	}

	public void setMaterials(Set<Material> materials) {
		this.materials = materials;
	}

	public List<Aviso> getAvisos() { return avisos; }
	public void setAvisos(List<Aviso> avisos) { this.avisos = avisos; }

	public static Classroom criar(String name, String description, Educator educator, double price) {
		if (name == null || name.isBlank()) {
			throw new InvalidClassroomException("O nome da turma é obrigatório.");
		}
		if (name.length() > 100) {
			throw new InvalidClassroomException("O nome da turma deve ter no máximo 100 caracteres.");
		}
		if (!Double.isFinite(price) || price < 0) {
			throw new InvalidClassroomException("O preço da turma não pode ser negativo.");
		}
		if (educator == null) {
			throw new InvalidClassroomException("Educador não encontrado.");
		}

		Classroom classroom = new Classroom();
		classroom.setName(name);
		classroom.setDescription(description);
		classroom.setEducator(educator);
		classroom.setPrice(price);
		classroom.setStatus(Status.ATIVA);
		classroom.setCusto(price <= 0 ? Custo.GRATUITA : Custo.PAGA);
		classroom.setAlunos(new HashSet<>());
		classroom.setMaterials(new HashSet<>());
		return classroom;
	}

	public void matricular(Student student) {
		if (student == null) {
			throw new InvalidClassroomException("Aluno não encontrado.");
		}
		if (alunos == null) {
			alunos = new HashSet<>();
		}
		boolean alreadyEnrolled = alunos.stream()
				.anyMatch(existing -> Objects.equals(existing.getId(), student.getId()));
		if (alreadyEnrolled) {
			throw new AlunoJaMatriculadoException();
		}
		alunos.add(student);
		if (student.getClassrooms() == null) {
			student.setClassrooms(new HashSet<>());
		}
		student.getClassrooms().add(this);
	}

	public boolean podeSerAcessadaPor(User user) {
		return user != null && podeSerAcessadaPor(user.getId());
	}

	public boolean podeSerAcessadaPor(Long userId) {
		if (userId == null) {
			return false;
		}
		boolean isEducator = educator != null && Objects.equals(educator.getId(), userId);
		boolean isEnrolled = alunos != null && alunos.stream()
				.anyMatch(student -> Objects.equals(student.getId(), userId));
		return isEducator || isEnrolled;
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
