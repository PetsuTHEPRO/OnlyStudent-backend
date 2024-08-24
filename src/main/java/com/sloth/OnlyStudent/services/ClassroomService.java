package com.sloth.OnlyStudent.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;

@Service
public class ClassroomService {


    @Autowired
    private ClassroomRepository classroomRepository;

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
}
