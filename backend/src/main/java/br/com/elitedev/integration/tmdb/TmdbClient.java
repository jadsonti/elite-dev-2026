package br.com.elitedev.integration.tmdb;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.elitedev.dto.catalog.MovieCatalogItemResponse;
import br.com.elitedev.dto.catalog.MovieCatalogPageResponse;
import br.com.elitedev.exception.ExternalCatalogException;

@Component
public class TmdbClient {
    private final RestClient restClient;
    private final String readToken;
    private final String imageBaseUrl;

    public TmdbClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.tmdb.base-url}") String baseUrl,
            @Value("${app.tmdb.image-base-url}") String imageBaseUrl,
            @Value("${app.tmdb.read-token:}") String readToken) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.imageBaseUrl = imageBaseUrl;
        this.readToken = readToken;
    }

    public MovieCatalogPageResponse listPopular(int page) {
        return fetchPage("/movie/now_playing", null, page);
    }

    public MovieCatalogPageResponse search(String query, int page) {
        return StringUtils.hasText(query)
                ? fetchPage("/search/movie", query.trim(), page)
                : listPopular(page);
    }

    public MovieCatalogItemResponse findMovie(Long movieId) {
        validateConfiguration();
        try {
            TmdbMovie movie = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/movie/{movieId}")
                            .queryParam("language", "pt-BR").build(movieId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + readToken)
                    .retrieve().body(TmdbMovie.class);
            if (movie == null) {
                throw new ExternalCatalogException("A TMDb retornou uma resposta vazia.");
            }
            return toResponse(movie);
        } catch (RestClientException exception) {
            throw new ExternalCatalogException("Não foi possível consultar o filme na TMDb.", exception);
        }
    }

    private MovieCatalogPageResponse fetchPage(String path, String query, int page) {
        validateConfiguration();
        try {
            TmdbPage response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(path)
                                .queryParam("language", "pt-BR")
                                .queryParam("page", page);
                        if (query != null) {
                            builder.queryParam("query", query);
                        }
                        return builder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + readToken)
                    .retrieve().body(TmdbPage.class);
            if (response == null) {
                throw new ExternalCatalogException("A TMDb retornou uma resposta vazia.");
            }
            List<MovieCatalogItemResponse> movies = response.results() == null
                    ? List.of()
                    : response.results().stream().map(this::toResponse).toList();
            return new MovieCatalogPageResponse(
                    response.page(), response.totalPages(), response.totalResults(), movies);
        } catch (RestClientException exception) {
            throw new ExternalCatalogException("Não foi possível consultar o catálogo da TMDb.", exception);
        }
    }

    private MovieCatalogItemResponse toResponse(TmdbMovie movie) {
        String imageUrl = StringUtils.hasText(movie.posterPath())
                ? imageBaseUrl + movie.posterPath()
                : null;
        return new MovieCatalogItemResponse(
                movie.id(), movie.title(), movie.overview(), imageUrl, movie.releaseDate());
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(readToken)) {
            throw new ExternalCatalogException(
                    "Integração TMDb não configurada. Defina a variável TMDB_READ_TOKEN.");
        }
    }

    private record TmdbPage(
            int page,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("total_results") long totalResults,
            List<TmdbMovie> results) {
    }

    private record TmdbMovie(
            Long id,
            String title,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("release_date") String releaseDate) {
    }
}
