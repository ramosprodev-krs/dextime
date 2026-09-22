package com.dextime.api.controller;

import com.dextime.api.dto.ProcedureDto;
import com.dextime.api.entity.Procedure;
import com.dextime.api.service.ProcedureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/procedures")
public class ProcedureController {

    private final ProcedureService procedureService;

    public ProcedureController(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    public ResponseEntity<Procedure> createProcedure(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProcedureDto dto) {

        return ResponseEntity.ok(procedureService.createProcedure(userDetails.getUsername(), dto));
    }
}
