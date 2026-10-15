package com.kairos.api.mongo.exception;
import org.springframework.http.HttpStatus;
public final class ApiException extends RuntimeException {
    public final HttpStatus status;
    public final String code;
    public ApiException(HttpStatus status,String code,String message) { super(message); this.status=status; this.code=code; }
    public static ApiException badRequest(String message) { return new ApiException(HttpStatus.BAD_REQUEST,"INVALID_REQUEST",message); }
    public static ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND",message); }
    public static ApiException conflict(String message) { return new ApiException(HttpStatus.CONFLICT,"CONFLICT",message); }
}
