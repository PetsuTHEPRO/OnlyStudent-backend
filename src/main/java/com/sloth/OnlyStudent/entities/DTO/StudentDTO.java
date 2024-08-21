package com.sloth.OnlyStudent.entities.DTO;

import java.util.Set;

import com.sloth.OnlyStudent.entities.Classroom;

public record StudentDTO(String name, String telefone, String login, Set<Classroom> classroom) {

}
