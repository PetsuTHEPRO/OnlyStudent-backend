package com.sloth.OnlyStudent.entities.DTO;

import java.util.List;
import java.util.Set;

import com.sloth.OnlyStudent.entities.Material;

public record ClassroomDetailsDTO(String name, String description, String educator, Set<Material> materais, List<String> nomeAlunos) {

}
