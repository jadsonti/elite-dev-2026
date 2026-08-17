package br.com.elitedev.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.elitedev.dto.catalog.MovieCatalogItemResponse;
import br.com.elitedev.dto.catalog.MovieCatalogPageResponse;
import br.com.elitedev.integration.tmdb.TmdbClient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/catalog/movies")
public class CatalogController {
    private final TmdbClient tmdbClient;

    public CatalogController(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    @GetMapping
    public ResponseEntity<MovieCatalogPageResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return ResponseEntity.ok(tmdbClient.search(query, page));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieCatalogItemResponse> detail(
            @PathVariable @Min(1) Long movieId) {
        return ResponseEntity.ok(tmdbClient.findMovie(movieId));
    }
}
