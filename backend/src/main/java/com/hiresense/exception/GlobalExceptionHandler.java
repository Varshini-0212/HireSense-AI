package com.hiresense.exception;
import jakarta.validation.ConstraintViolationException; import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.stream.Collectors;
@RestControllerAdvice public class GlobalExceptionHandler {
 private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
 @ExceptionHandler(ApiException.class) ResponseEntity<ErrorResponse> api(ApiException e){return ResponseEntity.status(e.status()).body(new ErrorResponse(Instant.now(),e.status().value(),e.getMessage(),null));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e){String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining(", "));return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(),400,"Validation failed",m));}
 @ExceptionHandler(ConstraintViolationException.class) ResponseEntity<ErrorResponse> violations(ConstraintViolationException e){return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(),400,"Validation failed",e.getMessage()));}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> other(Exception e){log.error("Unhandled API exception",e);return ResponseEntity.status(500).body(new ErrorResponse(Instant.now(),500,"Internal server error",null));}
 public record ErrorResponse(Instant timestamp,int status,String error,String details) {}
}
