package com.sloth.OnlyStudent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.MaterialType;
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("material")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class MaterialController {
	
	@Autowired
	private static final Logger logger = LoggerFactory.getLogger(ClassroomController.class);

	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	private MaterialRepository materialRepository;
	
	@Autowired
	private ClassroomRepository classroomRepository;
	
	@PostMapping("/register")
	public ResponseEntity<?> registerMaterial(@RequestBody MaterialDTO body) {
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
	            classroomRepository.findByCodigo(body.turmaCodigo()).get()
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
		public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
		    // Verifica se o material existe
		    if (!materialRepository.existsById(id)) {
		        return new ResponseEntity<>("Material not found.", HttpStatus.NOT_FOUND);
		    }
		    logger.info("ID:" + id);
		    // Exclui o material
		    materialRepository.deleteById(11l);
		    
		    // Verifica se o material foi excluído com sucesso
		    if (materialRepository.existsById(id)) {
		        return new ResponseEntity<>("Falha ao excluir material.", HttpStatus.INTERNAL_SERVER_ERROR);
		    }

		    return new ResponseEntity<>("Material deleted successfully.", HttpStatus.OK);
		}

}
