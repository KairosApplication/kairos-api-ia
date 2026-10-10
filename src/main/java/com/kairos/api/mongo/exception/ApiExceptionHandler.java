package com.kairos.api.mongo.exception;
import com.kairos.api.mongo.dto.ApiError;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handle(ApiException error) {
        return ResponseEntity.status(error.status).body(new ApiError(error.code,error.getMessage()));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> invalidInput(Exception error) {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST","Invalid JSON or request parameter."));
    }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiError> persistenceFailure(DataAccessException error) {
        return ResponseEntity.status(503).body(new ApiError("DATABASE_UNAVAILABLE","Database operation unavailable."));
    }
}
