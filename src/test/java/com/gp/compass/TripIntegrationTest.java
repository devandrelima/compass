package com.gp.compass;

import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TripIntegrationTest extends BaseIntegrationTest {

    private UUID tripId;

    private Map<String, Object> buildTripBody() {
        List<Map<String, Object>> stops = List.of(
                Map.of("stopType", "PERSON", "priority", "NORMAL",
                        "embarque", snapshotAddress("Rua de Partida", "1")),
                Map.of("stopType", "PERSON", "priority", "NORMAL",
                        "embarque", snapshotAddress("Rua de Chegada", "99"))
        );
        return Map.of("title", "Viagem de Teste", "stops", stops);
    }

    @Test
    @Order(1)
    @SuppressWarnings("unchecked")
    void deveCriarViagem() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/trips"),
                HttpMethod.POST,
                new HttpEntity<>(buildTripBody(), authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().get("title")).isEqualTo("Viagem de Teste");
        assertThat(resp.getBody().get("status")).isEqualTo("PENDING");

        List<Map<String, Object>> stops = (List<Map<String, Object>>) resp.getBody().get("stops");
        assertThat(stops).hasSize(2);

        tripId = UUID.fromString((String) resp.getBody().get("id"));
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void deveBuscarViagemPorId() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/trips/" + tripId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("id")).isEqualTo(tripId.toString());
        assertThat(resp.getBody().get("title")).isEqualTo("Viagem de Teste");

        List<Map<String, Object>> stops = (List<Map<String, Object>>) resp.getBody().get("stops");
        assertThat(stops).hasSize(2);
    }

    @Test
    @Order(3)
    void deveListarViagens() {
        ResponseEntity<List> resp = restTemplate.exchange(url("/api/trips"),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    @Order(4)
    void deveAtualizarStatusParaFinalizado() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/trips/" + tripId + "/status?status=FINISHED"),
                HttpMethod.PATCH,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("status")).isEqualTo("FINISHED");
    }

    @Test
    @Order(5)
    void deveConfirmarStatusAtualizado() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/trips/" + tripId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getBody().get("status")).isEqualTo("FINISHED");
    }

    @Test
    @Order(6)
    void deveDeletarViagem() {
        ResponseEntity<Void> resp = restTemplate.exchange(url("/api/trips/" + tripId),
                HttpMethod.DELETE,
                new HttpEntity<>(null, authHeaders()),
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(7)
    void deveRetornar404ParaViagemDeletada() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/trips/" + tripId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
