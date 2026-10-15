package com.sloth.OnlyStudent.controller;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.application.classroom.CreateClassroomUseCase;
import com.sloth.OnlyStudent.application.classroom.GetClassroomDetailsUseCase;
import com.sloth.OnlyStudent.application.classroom.JoinClassroomUseCase;
import com.sloth.OnlyStudent.application.classroom.ListEducatorClassroomsUseCase;
import com.sloth.OnlyStudent.application.classroom.ListStudentClassroomsUseCase;
import com.sloth.OnlyStudent.application.classroom.SearchClassroomsUseCase;
import com.sloth.OnlyStudent.application.material.ListMaterialsUseCase;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDetailsDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomNameDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.entities.DTO.MaterialResponseDTO;
import com.sloth.OnlyStudent.entities.DTO.StatusDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentIdDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentRequestDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.services.ClassroomService;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("turma")
@RequiredArgsConstructor
@Validated
public class ClassroomController {

    private final ClassroomRepository classroomRepository;
    private final MaterialRepository materialRepository;
    private final ClassroomService classroomService;
    private final CreateClassroomUseCase createClassroomUseCase;
    private final JoinClassroomUseCase joinClassroomUseCase;
    private final ListStudentClassroomsUseCase listStudentClassroomsUseCase;
    private final ListEducatorClassroomsUseCase listEducatorClassroomsUseCase;
    private final GetClassroomDetailsUseCase getClassroomDetailsUseCase;
    private final SearchClassroomsUseCase searchClassroomsUseCase;
    private final ListMaterialsUseCase listMaterialsUseCase;

    @GetMapping
    public List<ClassroomsDTO> getClassrooms(@RequestParam Long codigo) {
        return classroomService.getClassroomsTop3ByEducatorId(codigo);
    }

    @GetMapping("/allClassroom")
    public Page<ClassroomsDTO> getAllClassrooms(
            @RequestParam(defaultValue = "") String search,
            @PageableDefault(size = 50) Pageable pageable) {
        return searchClassroomsUseCase.execute(search, pageable);
    }

    @GetMapping("turmasAluno")
    public List<ClassroomsDTO> getClassroomsAlunos(@RequestParam Long codigo,
            @AuthenticationPrincipal User principal) {
        return classroomService.getClassroomsTop3ByStudentId(principal.getId());
    }

    @GetMapping("destaque")
    public List<ClassroomsDTO> getClassroomsDestaque() {
        return classroomService.getClassroomsTop3Destaque();
    }

    @GetMapping("search")
    public List<ClassroomNameDTO> searchClassrooms(@RequestParam String search) {
        return classroomService.searchClassrooms(search);
    }

    @GetMapping("/educatorTurmas")
    public Page<ClassroomsDTO> getClassroomsEducator(@RequestParam(required = false) Long codigo,
            @PageableDefault(size = 50) Pageable pageable,
            @AuthenticationPrincipal User principal) {
        return listEducatorClassroomsUseCase.execute(principal.getId(), pageable);
    }

    @GetMapping("/studentTurmas")
    public Page<ClassroomsDTO> getClassroomsStudent(@RequestParam(required = false) Long codigo,
            @PageableDefault(size = 50) Pageable pageable,
            @AuthenticationPrincipal User principal) {
        return listStudentClassroomsUseCase.execute(principal.getId(), pageable);
    }

    @GetMapping("/{codigo}/students/info")
    public ResponseEntity<Set<StudentIdDTO>> getStudentsInfo(@PathVariable Long codigo,
            @AuthenticationPrincipal User principal) {
        Classroom classroom = findAccessibleClassroom(codigo, principal);
        return ResponseEntity.ok(classroom.getAlunosInfo());
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long codigo,
            @AuthenticationPrincipal User principal) {
        Classroom classroom = classroomRepository.findByCodigo(codigo)
                .orElseThrow(ClassroomNotFoundException::new);
        requireEducatorOwner(classroom, principal);
        classroomRepository.delete(classroom);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{classroomId}/material/{materialId}")
    @Transactional
    public ResponseEntity<String> removeMaterialFromClassroom(@PathVariable Long classroomId,
            @PathVariable Long materialId, @AuthenticationPrincipal User principal) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found"));
        requireEducatorOwner(classroom, principal);
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));
        if (!classroom.getMaterials().remove(material)) {
            return ResponseEntity.notFound().build();
        }
        material.setTurma(null);
        materialRepository.save(material);
        classroomRepository.save(classroom);
        return ResponseEntity.ok("Material removed from classroom successfully!");
    }

    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestBody StatusDTO body,
            @AuthenticationPrincipal User principal) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(ClassroomNotFoundException::new);
        requireEducatorOwner(classroom, principal);
        classroomService.updateStatus(id, body.status());
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerClassroom(@Valid @RequestBody ClassroomDTO body,
            @AuthenticationPrincipal User principal) {
        createClassroomUseCase.execute(body, principal.getId(), principal.getRoles());
        return ResponseEntity.ok("Turma criada com sucesso!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDetailsDTO> getClassroomById(@PathVariable Long id,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(getClassroomDetailsUseCase.execute(id, principal));
    }

    @PostMapping("/{codigo}/entrar")
    public ResponseEntity<String> entrarNaTurma(@PathVariable Long codigo,
            @Valid @RequestBody StudentRequestDTO body,
            @AuthenticationPrincipal User principal) {
        joinClassroomUseCase.execute(codigo, principal.getId(), principal.getRoles());
        return ResponseEntity.ok("Entrada confirmada com sucesso.");
    }

    @GetMapping("/{codigo}/materials")
    public List<MaterialResponseDTO> getMaterialsByClassroomCódigo(@PathVariable Long codigo,
            @AuthenticationPrincipal User principal) {
        return listMaterialsUseCase.execute(codigo, principal.getId(), Pageable.unpaged());
    }

    private Classroom findAccessibleClassroom(Long code, User principal) {
        Classroom classroom = classroomRepository.findByCodigo(code)
                .orElseThrow(ClassroomNotFoundException::new);
        if (!classroom.podeSerAcessadaPor(principal)) {
            throw new ClassroomAccessDeniedException();
        }
        return classroom;
    }

    private void requireEducatorOwner(Classroom classroom, User principal) {
        if (principal == null || principal.getRoles() != UserRole.EDUCATOR
                || classroom.getEducator() == null
                || !classroom.getEducator().getId().equals(principal.getId())) {
            throw new ClassroomAccessDeniedException();
        }
    }
}
