package com.sloth.OnlyStudent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.MaterialType;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;

import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("material")
@RequiredArgsConstructor
public class MaterialController {
	
	private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);
	
	@Autowired
	private MaterialRepository materialRepository;
	
	@Autowired
	private ClassroomRepository classroomRepository;
	
	@PostMapping("/register")
	public ResponseEntity<?> registerMaterial(@RequestBody MaterialDTO body,
			@AuthenticationPrincipal User authenticatedUser) {
		Classroom classroom = classroomRepository.findByCodigo(body.turmaCodigo()).orElse(null);
		if (classroom == null) {
			return new ResponseEntity<>("Turma não encontrada.", HttpStatus.NOT_FOUND);
		}
		if (!Objects.equals(classroom.getEducator().getId(), authenticatedUser.getId())) {
			return new ResponseEntity<>("Apenas o educador responsável pode alterar esta turma.", HttpStatus.FORBIDDEN);
		}

	    // Verifica se o material já existe para a mesma turma pelo nome
	    boolean exists = materialRepository.existsByNameAndTurmaCodigo(body.name(), body.turmaCodigo());
	    logger.info("Codigo: " + body.turmaCodigo());
	    if (!exists) {
	        // Cria um novo material
	        Material newMaterial = new Material(
	            null, // id gerado automaticamente
	            body.name(),
	            MaterialType.valueOf(body.tipo().toUpperCase()), // Converte o tipo de string para enum
	            body.url(),
	            classroom
	        );

	        // Salva o material no banco de dados
	        materialRepository.save(newMaterial);

	        return new ResponseEntity<>("Material registered successfully!", HttpStatus.OK);
	    } else {
	        return new ResponseEntity<>("Material already exists for this classroom.", HttpStatus.CONFLICT);
	    }
	}
	
	// Novo endpoint para excluir material
		@DeleteMapping("/delete/{id}")
		public ResponseEntity<?> deleteMaterial(@PathVariable long id,
				@AuthenticationPrincipal User authenticatedUser) {
		    Material material = materialRepository.findById(id).orElse(null);
		    if (material == null) {
		        return new ResponseEntity<>("Material not found.", HttpStatus.NOT_FOUND);
		    }
		    logger.info("ID:" + id);
		    if (material.getTurma() == null
		            || !Objects.equals(material.getTurma().getEducator().getId(), authenticatedUser.getId())) {
		        return new ResponseEntity<>("Apenas o educador responsável pode alterar esta turma.", HttpStatus.FORBIDDEN);
		    }

		    materialRepository.deleteById(id);
		    return new ResponseEntity<>("Material deleted successfully.", HttpStatus.OK);
		}

}
