package com.progressive.banking.moneytransfer.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    
    private String errorCode; 

    private String message; // Custom message

    private String path; // Request URI
    private Map<String, String> details;
}
