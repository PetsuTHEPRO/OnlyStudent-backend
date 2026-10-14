package com.sloth.OnlyStudent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentDTO;
import com.sloth.OnlyStudent.repository.StudentRepository;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("student")
@RequiredArgsConstructor
public class StudentController {

	
    @Autowired
    private StudentRepository studentRepository;
	
	@GetMapping("/atualUser")
	public ResponseEntity<StudentDTO> getUser(@RequestParam(required = false) Long id,
			@AuthenticationPrincipal User authenticatedUser) {
		return studentRepository.findById(authenticatedUser.getId())
				.map(student -> ResponseEntity.ok(
						new StudentDTO(student.getName(), student.getTelephone(), student.getEmail())))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
	
	@PutMapping("/updateProfile")
    public ResponseEntity<?> updateUserByEmail(@RequestParam(required = false) String login,
            @RequestBody EducatorDTO user, @AuthenticationPrincipal User authenticatedUser) {
		return studentRepository.findById(authenticatedUser.getId())
				.map(student -> {
			student.setName(user.name());
			student.setTelephone(user.telefone());
			studentRepository.save(student);
			return ResponseEntity.ok("Salvado com Sucesso!");
		}).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
