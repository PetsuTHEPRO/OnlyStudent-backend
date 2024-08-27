package com.sloth.OnlyStudent.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.DTO.ClassroomNameDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;

import jakarta.transaction.Transactional;

@Service
public class ClassroomService {


    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    public List<ClassroomsDTO> getClassroomsTop3ByEducatorId(Long codigo) {
    	List<Classroom> classrooms = classroomRepository.findTop3ByEducatorIdOrderByCodigoAsc(codigo);
        
    	List<ClassroomsDTO> dtoList = new ArrayList<>();

        for (Classroom classroom : classrooms) {
            ClassroomsDTO dto = new ClassroomsDTO(
                    classroom.getCodigo(),
                    classroom.getName(),
                    classroom.getDescription(),
                    classroom.getStatus(),
                    classroom.getCusto(),
                    classroom.getPrice(),
                    classroom.getTotalAlunos(),
                    classroom.getTotalMateriais()
            );
            dtoList.add(dto);
        }

        return dtoList;
    }
    
    public List<ClassroomsDTO> getClassroomsTop3ByStudentId(Long codigo) {
    	List<Classroom> classrooms = classroomRepository.findTop3ByAlunosIdOrderByCodigoAsc(codigo);
    	List<ClassroomsDTO> dtoList = new ArrayList<>();

        for (Classroom classroom : classrooms) {
            ClassroomsDTO dto = new ClassroomsDTO(
                    classroom.getCodigo(),
                    classroom.getName(),
                    classroom.getDescription(),
                    classroom.getStatus(),
                    classroom.getCusto(),
                    classroom.getPrice(),
                    classroom.getTotalAlunos(),
                    classroom.getTotalMateriais()
            );
            dtoList.add(dto);
        }

        return dtoList;
    }
    
    public Page<ClassroomsDTO> getClassroomsByEducatorId(Long educatorId, Pageable pageable){
    	
    	Page<Classroom> classrooms = classroomRepository.findByEducatorId(educatorId, pageable);
    	
    	return classrooms.map(classroom -> new ClassroomsDTO(
                classroom.getCodigo(),
                classroom.getName(),
                classroom.getDescription(),
                classroom.getStatus(),
                classroom.getCusto(),
                classroom.getPrice(),
                classroom.getTotalAlunos(),
                classroom.getTotalMateriais()
            ));
    }
    
    public Page<ClassroomsDTO> getClassroomsByStudentId(Long studentId, Pageable pageable){
    	
    	Page<Classroom> classrooms = classroomRepository.findByStudentId(studentId, pageable);
    	
    	return classrooms.map(classroom -> new ClassroomsDTO(
                classroom.getCodigo(),
                classroom.getName(),
                classroom.getDescription(),
                classroom.getStatus(),
                classroom.getCusto(),
                classroom.getPrice(),
                classroom.getTotalAlunos(),
                classroom.getTotalMateriais()
            ));
    }

    @Transactional
	public Classroom updateStatus(Long id, Status status) {
		// TODO Auto-generated method stub
		
		Classroom classroom = classroomRepository.findById(id)
	            .orElseThrow();
	        classroom.setStatus(status);
	    return classroomRepository.save(classroom);
	}

	public List<ClassroomsDTO> getClassroomsTop3Destaque() {
		// TODO Auto-generated method stub
		List<Classroom> classrooms = classroomRepository.findAll();
		
		List<Classroom> topClassrooms = classrooms.stream()
		        .peek(classroom -> {
		            // Atualiza os valores de totalAlunos e totalMateriais
		            classroom.getTotalAlunos();
		            classroom.getTotalMateriais();
		        })
		        .sorted(Comparator.comparingInt(classroom -> 
		            ((Classroom) classroom).getTotalAlunos() + ((Classroom) classroom).getTotalMateriais()
		        ).reversed())
		        .limit(3) // Seleciona as 3 primeiras turmas
		        .collect(Collectors.toList());
		
	    // Mapear as turmas para DTOs
	    List<ClassroomsDTO> topClassroomsDTOs = topClassrooms.stream()
	        .map(classroom -> new ClassroomsDTO(
	            classroom.getCodigo(),
	            classroom.getName(),
	            classroom.getDescription(),
	            classroom.getStatus(),
	            classroom.getCusto(),
	            classroom.getPrice(),
	            classroom.getTotalAlunos(),
	            classroom.getTotalMateriais()
	        ))
	        .collect(Collectors.toList());

	    return topClassroomsDTOs;
	    
	}

	public List<ClassroomNameDTO> searchClassrooms(String query) {		
	    List<Classroom> classrooms = classroomRepository.findByNameContainingIgnoreCase(query);

	    return classrooms.stream()
	        .map(classroom -> new ClassroomNameDTO(classroom.getName()))
	        .collect(Collectors.toList());
	}
	
	public Page<ClassroomsDTO> getAllClassrooms(String search, Pageable pageable) {
		
		Page<Classroom> classrooms = classroomRepository.findByNameStartingWithIgnoreCase(search, pageable);

        return classrooms.map(classroom -> new ClassroomsDTO(
                classroom.getCodigo(),
                classroom.getName(),
                classroom.getDescription(),
                classroom.getStatus(),
                classroom.getCusto(),
                classroom.getPrice(),
                classroom.getTotalAlunos(),
                classroom.getTotalMateriais()
            ));
    }
	
	public boolean addStudentInClassroom(Long codigoClassroom, Long idAluno) {
        Optional<Student> optionalStudent = studentRepository.findById(idAluno);
        Optional<Classroom> optionalClassroom = classroomRepository.findByCodigo(codigoClassroom);

        if (optionalStudent.isPresent() && optionalClassroom.isPresent()) {
            Student student = optionalStudent.get();
            Classroom classroom = optionalClassroom.get();

            // Adiciona a turma ao conjunto de turmas do aluno
            student.getClassrooms().add(classroom);

            // Adiciona o aluno ao conjunto de alunos da turma
            classroom.getAlunos().add(student);

            // Salva as entidades no banco de dados
            studentRepository.save(student);
            classroomRepository.save(classroom);

            return true;
        }

        return false;
    }
	
}
