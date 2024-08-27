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
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.services.EducatorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("educator")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class EducatorController {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EducatorRepository educatorRepository;
	
    @Autowired
    private EducatorService educatorService;
    
	@GetMapping("/idByEmail")
	public ResponseEntity<Long> getEducatorIdByEmail(@RequestParam String email) {
        Long idEducator = educatorService.getEducatorIdByEmail(email);
        
        if (idEducator >= 0) {
            return ResponseEntity.ok(idEducator); // Retorna o ID do Educator
        }
        
        return ResponseEntity.notFound().build();
    }
	
	@GetMapping("/atualUser")
	public ResponseEntity<EducatorDTO> getUser(@RequestParam Long id){

		Educator educator = educatorRepository.findById(id).get();
		
		if(educator != null) {
			return ResponseEntity.ok(new EducatorDTO(educator.getName(), educator.getTelephone(), educator.getEmail(), educator.getEspecialidade()));
		}
		return ResponseEntity.notFound().build();
	}
	
	@PutMapping("/updateProfile")
    public ResponseEntity<?> updateUserByEmail(@RequestParam String login, @RequestBody EducatorDTO user) {

		Educator educator = educatorRepository.findByEmail(login);
		
		if(educator != null) {
			educator.setName(user.name());
			educator.setTelephone(user.telefone());
			educator.setEspecialidade(user.especialidade());
			
			educatorRepository.save(educator);
			return ResponseEntity.ok("Salvado com Sucesso!");
        }
		
		return ResponseEntity.notFound().build();
    }
}
