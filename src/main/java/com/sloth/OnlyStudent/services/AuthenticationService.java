package com.sloth.OnlyStudent.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sloth.OnlyStudent.controller.ClassroomController;
import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.entities.DTO.ResponseDTO;
import com.sloth.OnlyStudent.infra.security.TokenService;
import com.sloth.OnlyStudent.repository.UserRepository;

@Service
public class AuthenticationService {
	
	private static final Logger logger = LoggerFactory.getLogger(ClassroomController.class);

	@Autowired
    private UserRepository repository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private TokenService tokenService;

	public ResponseEntity<?> authenticateUser(AuthenticationDTO body){
		try {
	        User user = this.repository.findByEmail(body.email());
	     // Verifica se o user é uma instância de Educator
            String especialidade = null;
            if (user instanceof Educator) {
                especialidade = ((Educator) user).getEspecialidade();
            }
            logger.info("Especialidade" + especialidade);
	        if(passwordEncoder.matches(body.password(), user.getPassword())) {
	            String token = this.tokenService.generateToken(user);
	            return ResponseEntity.ok(new ResponseDTO(user.getName(), user.getEmail(), user.getRoles().getRole(), user.getTelephone(), especialidade, token, user.getId()));
	        }
	        return ResponseEntity.badRequest().body("Incorrect password.");
    	}catch(RuntimeException e) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email não cadastrado, acesse a página de cadastro!");
    	}
	}
	
	public ResponseEntity<String> createUser(RegisterDTO body) {
        boolean userExists = repository.existsByEmail(body.email());
        
        if (!userExists) {
            User newUser;

            // Instanciando o tipo correto de usuário baseado na role
            switch (body.role().getRole().toLowerCase()) {
                case "admin":
                    newUser = new Administrator(null, body.name(), body.telephone(), body.email(), passwordEncoder.encode(body.password()), body.role());
                    break;
                case "educator":
                    newUser = new Educator(null, body.name(), body.telephone(), body.email(), passwordEncoder.encode(body.password()), body.role(), body.especialidade());
                    break;
                case "student":
                    newUser = new Student(null, body.name(), body.telephone(), body.email(), passwordEncoder.encode(body.password()), body.role());
                    break;
                default:
                    return new ResponseEntity<>("Invalid role provided!", HttpStatus.BAD_REQUEST);
            }
            
            repository.save(newUser);

            String token = tokenService.generateToken(newUser);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Email já cadastrado.", HttpStatus.CONFLICT);        	
        }
    }
}
