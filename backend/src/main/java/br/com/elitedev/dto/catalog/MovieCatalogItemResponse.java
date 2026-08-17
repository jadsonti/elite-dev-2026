package br.com.elitedev.dto.catalog;

public record MovieCatalogItemResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        String releaseDate
) {
}
