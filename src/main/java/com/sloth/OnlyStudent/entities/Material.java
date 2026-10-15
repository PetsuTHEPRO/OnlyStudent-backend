package com.sloth.OnlyStudent.entities;

import java.net.URI;
import java.util.Locale;

import com.sloth.OnlyStudent.shared.errors.InvalidMaterialException;

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

    public static Material criar(String tipo, String name, String url, Classroom classroom) {
        if (name == null || name.isBlank()) {
            throw new InvalidMaterialException("O nome do material é obrigatório.");
        }
        if (name.length() > 255) {
            throw new InvalidMaterialException("O nome do material deve ter no máximo 255 caracteres.");
        }
        if (url == null || url.isBlank()) {
            throw new InvalidMaterialException("A URL do material é obrigatória.");
        }
        if (!isHttpUrl(url)) {
            throw new InvalidMaterialException("A URL do material deve começar com http ou https.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new InvalidMaterialException("O tipo do material é obrigatório.");
        }
        if (classroom == null) {
            throw new InvalidMaterialException("Turma não encontrada.");
        }

        MaterialType materialType;
        try {
            materialType = MaterialType.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidMaterialException("Tipo de material inválido.");
        }

        Material material = new Material();
        material.setTipo(materialType);
        material.setName(name);
        material.setUrl(url);
        material.setTurma(classroom);
        return material;
    }

    private static boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            return uri.getHost() != null && scheme != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}