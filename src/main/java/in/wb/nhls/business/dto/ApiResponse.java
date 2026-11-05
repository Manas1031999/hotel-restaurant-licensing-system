package in.wb.nhls.business.dto;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        PaginationResponse pagination) {
}