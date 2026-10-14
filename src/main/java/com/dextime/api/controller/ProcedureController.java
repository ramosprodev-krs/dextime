package com.dextime.api.controller;

import com.dextime.api.dto.AddDatesDto;
import com.dextime.api.dto.ProcedureDto;
import com.dextime.api.dto.ProcedureResponseDto;
import com.dextime.api.service.ProcedureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procedures")
public class ProcedureController {

    private final ProcedureService procedureService;

    public ProcedureController(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    public ResponseEntity<ProcedureResponseDto> createProcedure(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ProcedureDto dto) {

        return ResponseEntity.ok(procedureService.createProcedure(userDetails.getUsername(), dto));
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PatchMapping("/{procedureId}/dates")
    public ResponseEntity<ProcedureResponseDto> addDatesToProcedure(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String procedureId,
            @RequestBody @Valid AddDatesDto dto) {

        String username = userDetails.getUsername();

        return ResponseEntity.ok(procedureService.addDatesToProcedure(username, procedureId, dto));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProcedureResponseDto>> getProceduresByStore(@PathVariable String storeId) {
        return ResponseEntity.ok(procedureService.getProceduresByStore(storeId));
    }


}
