package com.sloth.OnlyStudent.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    private MaterialType tipo;

    private String url;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Classroom turma;
    
    public Material(Long id, String name, MaterialType tipo, String url, Classroom turma) {
        this.id = id;
        this.name = name;
        this.tipo = tipo;
        this.url = url;
        this.turma = turma;
    }

}