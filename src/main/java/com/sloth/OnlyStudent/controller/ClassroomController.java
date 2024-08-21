package com.sloth.OnlyStudent.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentIdDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;

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
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private MaterialRepository materialRepository;
    
    // Listar 3 primeiras turmas
    @GetMapping
    public List<Classroom> getClassrooms(@RequestParam Long codigo) {
    	logger.info(codigo + " Codigo");
        return classroomRepository.findTop3ByEducatorIdOrderByCodigoAsc(codigo);
    }
    
    @GetMapping("turmasAluno")
    public List<Classroom> getClassroomsAlunos(@RequestParam Long codigo){
    	return classroomRepository.findTop3ByAlunosIdOrderByCodigoAsc(codigo);
    }
    
    @GetMapping("/turmaEducator")
    public Page<Classroom> getClassroomsEducator(@RequestParam Long codigo, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "12") int size) {	
    	logger.info(codigo + " = " + page + " = " + size);
    	Pageable pageable = PageRequest.of(page, size);
        return classroomRepository.findByEducatorId(codigo, pageable);
    }
    
    @GetMapping("/{codigo}/students/info")
    public ResponseEntity<Set<StudentIdDTO>> getStudentsInfo(@PathVariable Long codigo) {
        Classroom classroom = classroomRepository.findByCodigo(codigo)
                .orElseThrow();

        Set<StudentIdDTO> alunosInfo = classroom.getAlunosInfo();
        return ResponseEntity.ok(alunosInfo);
    }
    
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long codigo) {
        // Verifica se a turma existe
        Classroom classroom = classroomRepository.findByCodigo(codigo)
                .orElseThrow();

        // Apaga a turma
        classroomRepository.delete(classroom);

        // Retorna status 204 No Content
        return ResponseEntity.noContent().build();
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

            return new ResponseEntity<>("Turma criada com sucesso!", HttpStatus.OK);
        } else {
            logger.info("Classroom already exists!");
            return new ResponseEntity<>("Já existe uma turma com esse nome.", HttpStatus.CONFLICT);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getClassroomById(@PathVariable Long id) {
    	logger.info("Entrou aqui");
        return classroomRepository.findById(id)
            .map(classroom -> ResponseEntity.ok().body(classroom))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{codigo}/materials")
    public List<Material> getMaterialsByClassroomCódigo(@PathVariable Long codigo) {
        return materialRepository.findByTurmaCodigo(codigo);
    }
    
}