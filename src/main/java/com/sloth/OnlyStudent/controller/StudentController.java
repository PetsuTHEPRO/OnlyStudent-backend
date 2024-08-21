package com.sloth.OnlyStudent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentDTO;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("student")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class StudentController {

	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
	
	@GetMapping("/atualUser")
	public ResponseEntity<StudentDTO> getUser(@RequestParam Long id){

		Student student = studentRepository.findById(id).get();
		
		if(student != null) {
			return ResponseEntity.ok(new StudentDTO(student.getName(), student.getTelephone(), student.getEmail(), student.getClassrooms()));
		}
		return ResponseEntity.notFound().build();
	}
	
	@PutMapping("/updateProfile")
    public ResponseEntity<?> updateUserByEmail(@RequestParam String login, @RequestBody EducatorDTO user) {

		logger.info(user.especialidade() + " = " + login);

		Student student = studentRepository.findByEmail(login);
		
		if(student != null) {
			student.setName(user.name());
			student.setTelephone(user.telefone());
			
			studentRepository.save(student);
			return ResponseEntity.ok("Salvado com Sucesso!");
        }
		
		return ResponseEntity.notFound().build();
    }
}
