
package in.wb.nhls.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.business.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle application-specific HTTP errors.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(
                ex.getStatusCode().value());

        String message = ex.getReason();

        if (message == null || message.isBlank()) {
            message = getDefaultMessage(status);
        }

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                message,
                null,
                null);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Handle unexpected application errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(
            Exception ex) {

        // Log the actual exception for debugging.
        log.error("Unhandled exception", ex);

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                "সার্ভারে একটি অপ্রত্যাশিত সমস্যা হয়েছে",
                null,
                null);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private String getDefaultMessage(HttpStatus status) {

        return switch (status) {

            case NOT_FOUND ->
                "অনুরোধ করা তথ্য পাওয়া যায়নি";

            case BAD_REQUEST ->
                "আপনার অনুরোধটি সঠিক নয়";

            case FORBIDDEN ->
                "এই কাজ করার অনুমতি আপনার নেই";

            case UNAUTHORIZED ->
                "অনুগ্রহ করে লগইন করুন";

            default ->
                "একটি সমস্যা হয়েছে";
        };
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                message,
                null,
                null);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidJson(
            HttpMessageNotReadableException ex) {

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                "অনুরোধের JSON অথবা কোনো field-এর value সঠিক নয়",
                null,
                null);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
