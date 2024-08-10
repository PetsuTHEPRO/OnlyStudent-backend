package com.sloth.OnlyStudent.controller;

import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.EducatorRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("turma")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class ClassroomController {
	
	@Autowired
	private static final Logger logger = LoggerFactory.getLogger(ClassroomController.class);

    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private EducatorRepository educatorRepository;
    
    // Listar 3 primeiras turmas
    @GetMapping
    public List<Classroom> getClassrooms(@RequestParam Long codigo) {
    	logger.info(codigo + " Codigo");
        return classroomRepository.findTop3ByEducatorIdOrderByCodigoAsc(codigo);
    }
    
    @GetMapping("/turma")
    public Page<Classroom> getClassrooms(@RequestParam Long codigo, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "12") int size) {	
    	logger.info(codigo + " = " + page + " = " + size);
    	Pageable pageable = PageRequest.of(page, size);
        return classroomRepository.findByEducatorId(codigo, pageable);
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerClassroom(@RequestBody ClassroomDTO body) {
        boolean exists = classroomRepository.existsByName(body.name());

        logger.info("Classroom exists: " + exists);
        logger.info("Educator: " + body.idEducator());
        
        if (!exists) {
        	Custo custo = (body.price() > 0.0)? Custo.PAGA : Custo.GRATUITA;
            Classroom newClassroom = new Classroom(
                null,
                body.name(),
                body.description(),
                Status.ATIVA,
                custo,
                body.price(),
                new HashSet<>(),
                educatorRepository.findById(body.idEducator()).get(),
                new HashSet<>()
            );

            classroomRepository.save(newClassroom);

            return new ResponseEntity<>("Classroom registered successfully!", HttpStatus.OK);
        } else {
            logger.info("Classroom already exists!");
            return new ResponseEntity<>("Classroom already exists in the database.", HttpStatus.CONFLICT);
        }
    }
    
}