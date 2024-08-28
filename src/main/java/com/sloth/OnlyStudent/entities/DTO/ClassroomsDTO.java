package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Status;

public record ClassroomsDTO(Long codigo, String name, String educator, Status status, Custo custo, double price, int totalAlunos, int totalMateriais) {

}
