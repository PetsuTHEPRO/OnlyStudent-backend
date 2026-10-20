package com.sloth.OnlyStudent.application.classroom;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sloth.OnlyStudent.entities.Aviso;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.DTO.AvisoResponseDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDetailsDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.entities.DTO.MaterialResponseDTO;

@Component
public class ClassroomResponseMapper {

    public ClassroomsDTO toSummary(Classroom classroom) {
        return new ClassroomsDTO(classroom.getCodigo(), classroom.getName(), classroom.getEducator().getName(),
                classroom.getStatus(), classroom.getCusto(), classroom.getPrice(),
                classroom.getAlunos() == null ? 0 : classroom.getAlunos().size(),
                classroom.getMaterials() == null ? 0 : classroom.getMaterials().size());
    }

    public ClassroomDetailsDTO toDetails(Classroom classroom) {
        Set<MaterialResponseDTO> materials = classroom.getMaterials().stream()
                .map(this::toMaterialResponse)
                .collect(Collectors.toSet());
        List<String> studentNames = classroom.getAlunos().stream()
                .map(Student::getName)
                .toList();
        List<AvisoResponseDTO> avisos = classroom.getAvisos().stream()
                .map(this::toAvisoResponse)
                .collect(Collectors.toList());
                
        return new ClassroomDetailsDTO(classroom.getCodigo(), classroom.getName(), classroom.getDescription(),
                classroom.getEducator().getName(), materials, studentNames, avisos);
    }

    private MaterialResponseDTO toMaterialResponse(Material material) {
        return new MaterialResponseDTO(material.getId(), material.getTipo(), material.getName(), material.getUrl());
    }

    private AvisoResponseDTO toAvisoResponse(Aviso aviso) {
        return new AvisoResponseDTO(aviso.getId(), aviso.getMensagem(), aviso.getDataCriacao());
    }
}