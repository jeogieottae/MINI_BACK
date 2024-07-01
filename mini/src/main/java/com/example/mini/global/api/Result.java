package com.example.mini.global.api;

import com.example.mini.global.api.exception.error.ErrorCode;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    private String resultCode;
    private String resultMessage;
    private String resultDescription;

    public static Result SUCCESS(SuccessCode successCode) {
        return Result.builder()
            .resultCode(successCode.name())
            .resultMessage("success")
            .resultDescription(successCode.getDescription())
            .build();
    }

    public static Result ERROR(ErrorCode errorCode) {
        return Result.builder()
            .resultCode(errorCode.getCodeName())
            .resultMessage("error")
            .resultDescription(errorCode.getInfo())
            .build();
    }

    public static Result VALIDATION_ERROR(List<String> errors) {
        return Result.builder()
            .resultCode("VALIDATION_ERROR")
            .resultMessage("Validation Error")
            .resultDescription(String.join(", ", errors))
            .build();
    }
}