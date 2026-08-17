package br.com.elitedev.integration.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.com.elitedev.exception.ExternalCatalogException;

class TmdbClientTest {
    @Test
    void shouldSearchMoviesAndMapPosterUrl() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TmdbClient client = new TmdbClient(
                builder, "https://api.example.test", "https://images.example.test/w780", "token");

        server.expect(requestTo(
                        "https://api.example.test/search/movie?language=pt-BR&page=2&query=Matrix"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("""
                        {"page":2,"total_pages":3,"total_results":41,"results":[{
                          "id":603,"title":"Matrix","overview":"Um hacker descobre a verdade.",
                          "poster_path":"/matrix.jpg","release_date":"1999-03-30"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        var result = client.search(" Matrix ", 2);

        assertThat(result.totalResults()).isEqualTo(41);
        assertThat(result.results()).singleElement().satisfies(movie -> {
            assertThat(movie.id()).isEqualTo(603L);
            assertThat(movie.imageUrl()).isEqualTo("https://images.example.test/w780/matrix.jpg");
        });
        server.verify();
    }

    @Test
    void shouldFailFastWhenTokenIsMissing() {
        TmdbClient client = new TmdbClient(
                RestClient.builder(), "https://api.example.test", "https://images.example.test", " ");

        assertThatThrownBy(() -> client.listPopular(1))
                .isInstanceOf(ExternalCatalogException.class)
                .hasMessageContaining("TMDB_READ_TOKEN");
    }
}
