package com.dextime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDto {
    private String to;
    private String subject;
    private String template;
    private Map<String, Object> variables;
}
