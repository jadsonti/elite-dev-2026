package br.com.elitedev.dto.catalog;

import java.util.List;

public record MovieCatalogPageResponse(
        int page,
        int totalPages,
        long totalResults,
        List<MovieCatalogItemResponse> results
) {
}
