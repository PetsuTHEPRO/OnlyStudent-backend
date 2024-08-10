package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.UserRole;

public record UserDTO(Long id, String name, String telephone, String email, String password, UserRole role){

}
