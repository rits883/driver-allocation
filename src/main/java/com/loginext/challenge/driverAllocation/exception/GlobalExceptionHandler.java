
package com.loginext.challenge.driverAllocation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                ex.getStatus(),
                ex.getMessage(),
                request
        );

        problem.setProperty("errorCode", ex.getErrorCode());

        return ResponseEntity
                .status(ex.getStatus())
                .body(problem);
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                ex.getStatusCode(),
                ex.getReason() != null
                        ? ex.getReason()
                        : "Request failed",
                request
        );

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request
        );

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": "
                                + error.getDefaultMessage()
                )
                .toList();

        problem.setProperty("errorCode", "VALIDATION_ERROR");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }




    @ExceptionHandler(InvalidOrderSequenceException.class)
    public ResponseEntity<ProblemDetail> handleInvalidOrderSequence(
            InvalidOrderSequenceException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid order sequence");
        problem.setDetail(ex.getMessage());

        return ResponseEntity.badRequest().body(problem);
    }


    @ExceptionHandler(DuplicateOrderConflictException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateOrder(
            DuplicateOrderConflictException ex) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problem.setTitle("Duplicate Order Conflict");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<ProblemDetail> handleArithmetic(
            ArithmeticException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid time calculation");
        problem.setDetail(
                "Order time and travel time exceed the supported range");

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Malformed or missing request body",
                request
        );

        problem.setProperty("errorCode", "INVALID_REQUEST_BODY");

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred",
                request
        );

        problem.setProperty("errorCode", "INTERNAL_SERVER_ERROR");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }



    private ProblemDetail createProblem(
            HttpStatusCode status,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }
}
