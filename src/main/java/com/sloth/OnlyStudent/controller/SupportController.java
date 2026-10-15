package com.sloth.OnlyStudent.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.application.support.SendSupportEmailUseCase;
import com.sloth.OnlyStudent.entities.DTO.SupportDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
@Validated
public class SupportController {

    private final SendSupportEmailUseCase sendSupportEmailUseCase;

    @PostMapping("/send")
    public String sendSupportEmail(@Valid @RequestBody SupportDTO supportDTO) {
        sendSupportEmailUseCase.execute(supportDTO);
        return "Email sent successfully";
    }
}
