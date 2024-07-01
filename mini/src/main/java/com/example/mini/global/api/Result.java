package com.example.mini.global.api;

import com.example.mini.global.api.exception.error.ErrorCode;
import com.example.mini.global.api.exception.success.SuccessCode;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    private HttpStatusCode resultCode;
    private String resultMessage;
    private String resultDescription;

    public static Result SUCCESS(SuccessCode successCode) {
        return Result.builder()
            .resultCode(successCode.getHttpStatus())
            .resultMessage("성공")
            .resultDescription(successCode.getDescription())
            .build();
    }

    public static Result ERROR(ErrorCode errorCode) {
        return Result.builder()
            .resultCode(errorCode.getCode())
            .resultMessage("error")
            .resultDescription(errorCode.getInfo())
            .build();
    }

    public static Result VALIDATION_ERROR(List<String> errors) {
        return Result.builder()
            .resultCode(HttpStatus.BAD_REQUEST)
            .resultMessage("Validation Error")
            .resultDescription(String.join(", ", errors))
            .build();
    }
}
