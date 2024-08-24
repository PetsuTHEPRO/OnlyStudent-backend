package com.sloth.OnlyStudent.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.repository.EducatorRepository;

@Service
public class EducatorService {

	@Autowired
    private EducatorRepository educatorRepository;

    public Long getEducatorIdByEmail(String email) {
    	
    	Educator educator = educatorRepository.findByEmail(email);
    	
    	if(educator != null) {
    		return educator.getId();
    	}
    	
        return -1L;
    }
}
