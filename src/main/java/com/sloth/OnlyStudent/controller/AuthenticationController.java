package com.sloth.OnlyStudent.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.entities.DTO.ResponseDTO;
import com.sloth.OnlyStudent.infra.security.ErrorResponse;
import com.sloth.OnlyStudent.infra.security.SecurityFilter;
import com.sloth.OnlyStudent.infra.security.TokenService;
import com.sloth.OnlyStudent.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class AuthenticationController {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	@Autowired
    private UserRepository repository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationDTO body){
    	try {
	        User user = this.repository.findByEmail(body.email());
	        if(passwordEncoder.matches(body.password(), user.getPassword())) {
	            String token = this.tokenService.generateToken(user);
	            return ResponseEntity.ok(new ResponseDTO(user.getName(), user.getEmail(), user.getRoles().getRole(), token, user.getId()));
	        }
	        logger.info("Incorrect Password");
	        return ResponseEntity.badRequest().body("Incorrect password.");
    	}catch(RuntimeException e) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email não cadastrado, acesse a página de cadastro!");
    	}
	        
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO body) {
        boolean user = repository.existsByEmail(body.email());

        logger.info("User:" + user);
        logger.info("Especialidade" + body.especialidade());
        
        if (!user) {
            User newUser;

            // Instanciando o tipo correto de usuário baseado na role
            if (body.role().getRole().equalsIgnoreCase("admin")) {
                newUser = new Administrator(null, body.name(), body.telephone(), body.email(), passwordEncoder.encode(body.password()), body.role());
            } else if (body.role().getRole().equalsIgnoreCase("educator")) {
                newUser = new Educator(null, body.name(), body.telephone(), body.email(), passwordEncoder.encode(body.password()), body.role(), body.especialidade());
            } else if (body.role().getRole().equalsIgnoreCase("student")) {
                newUser = new Student(null, body.name(), body.telephone(), body.email(), passwordEncoder.encode(body.password()), body.role());
            } else {
                return ResponseEntity.badRequest().body("Invalid role provided!");
            }
            
            repository.save(newUser);

            String token = tokenService.generateToken(newUser);
            return new ResponseEntity<>(token, HttpStatus.OK);
        }else{
            logger.info("Já existe amigao!");
        	return new ResponseEntity<>("User already exists in the database.", HttpStatus.CONFLICT);        	
        }
        
    }

    @GetMapping("/educator")
    @PreAuthorize("hasRole('EDUCATOR')")
    public String getEducatorResource() {
        return "Resource for Educators";
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public String getStudentResource() {
        return "Resource for Students";
    }

    @GetMapping("/common")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDUCATOR', 'STUDENT')")
    public String getCommonResource() {
        return "Resource for All Users";
    }
    
}
