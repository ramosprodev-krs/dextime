package com.dextime.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "CNPJ is required")
    @CNPJ(message = "CNPJ has to be in the format: XX.XXX.XXX/XXXX-XX")
    private String cnpj;
}