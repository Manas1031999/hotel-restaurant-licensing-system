package in.wb.nhls.business.dto;

public record PaginationResponse(
        int page,
        int size,
        long totalElements,
        int totalPages) {
}