package com.gp.compass;

import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TripStopIntegrationTest extends BaseIntegrationTest {

    private UUID tripId;
    private UUID middleStopId;

    @SuppressWarnings("unchecked")
    private UUID criarViagem() {
        List<Map<String, Object>> stops = List.of(
                Map.of("stopType", "PERSON", "priority", "NORMAL",
                        "embarque", snapshotAddress("Rua de Partida", "1")),
                Map.of("stopType", "PERSON", "priority", "NORMAL",
                        "embarque", snapshotAddress("Rua de Chegada", "99"),
                        "desembarque", snapshotAddress("Rua Final", "200"))
        );
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/trips"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("title", "Viagem Paradas Teste", "stops", stops), authHeaders()),
                Map.class);
        return UUID.fromString((String) resp.getBody().get("id"));
    }

    @Test
    @Order(1)
    void deveCriarViagemComDuasParadas() {
        tripId = criarViagem();

        ResponseEntity<List> resp = restTemplate.exchange(url("/api/trips/" + tripId + "/stops"),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(2);
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void deveAdicionarParadaNoMeio() {
        Map<String, Object> novaParada = Map.of(
                "stopType", "PRODUCT",
                "priority", "HIGH",
                "embarque", snapshotAddress("Rua do Meio", "50"),
                "desembarque", snapshotAddress("Rua de Entrega", "75")
        );

        ResponseEntity<List> resp = restTemplate.exchange(url("/api/trips/" + tripId + "/stops"),
                HttpMethod.POST,
                new HttpEntity<>(List.of(novaParada), authHeaders()),
                List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).hasSize(1);

        Map<String, Object> stop = (Map<String, Object>) resp.getBody().get(0);
        middleStopId = UUID.fromString((String) stop.get("id"));
        assertThat(stop.get("stopType")).isEqualTo("PRODUCT");
    }

    @Test
    @Order(3)
    void deveListarTresParadas() {
        ResponseEntity<List> resp = restTemplate.exchange(url("/api/trips/" + tripId + "/stops"),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(3);
    }

    @Test
    @Order(4)
    void deveAtualizarPrioridadeDaParada() {
        ResponseEntity<Map> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops/" + middleStopId),
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("priority", "CRITICAL"), authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("priority")).isEqualTo("CRITICAL");
    }

    @Test
    @Order(5)
    void deveToggleEmbarqueCheckParaTrue() {
        ResponseEntity<Map> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops/" + middleStopId + "/embarque/check"),
                HttpMethod.PATCH,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("embarqueChecked")).isEqualTo(true);
        assertThat(resp.getBody().get("concluida")).isEqualTo(false);
    }

    @Test
    @Order(6)
    void deveToggleDesembarqueCheckParaTrue() {
        ResponseEntity<Map> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops/" + middleStopId + "/desembarque/check"),
                HttpMethod.PATCH,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("desembarqueChecked")).isEqualTo(true);
        assertThat(resp.getBody().get("concluida")).isEqualTo(true);
    }

    @Test
    @Order(7)
    @SuppressWarnings("unchecked")
    void deveFiltrarParadasPendentesExcluindoConcluidas() {
        ResponseEntity<List> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops?somentePendentes=true"),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(2);

        List<Map<String, Object>> stops = (List<Map<String, Object>>) resp.getBody();
        boolean middlePresente = stops.stream()
                .anyMatch(s -> middleStopId.toString().equals(s.get("id")));
        assertThat(middlePresente).isFalse();
    }

    @Test
    @Order(8)
    void deveToggleEmbarqueCheckDeVoltaParaFalse() {
        ResponseEntity<Map> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops/" + middleStopId + "/embarque/check"),
                HttpMethod.PATCH,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getBody().get("embarqueChecked")).isEqualTo(false);
        assertThat(resp.getBody().get("concluida")).isEqualTo(false);
    }

    @Test
    @Order(9)
    void deveLimparDesembarque() {
        ResponseEntity<Map> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops/" + middleStopId + "/desembarque"),
                HttpMethod.DELETE,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("desembarque")).isNull();
        assertThat(resp.getBody().get("desembarqueChecked")).isEqualTo(false);
    }

    @Test
    @Order(10)
    void deveDeletarParadaDoMeio() {
        ResponseEntity<Void> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops"),
                HttpMethod.DELETE,
                new HttpEntity<>(List.of(middleStopId), authHeaders()),
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(11)
    void deveRestarDuasParadasAposDelecao() {
        ResponseEntity<List> resp = restTemplate.exchange(url("/api/trips/" + tripId + "/stops"),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(2);
    }

    @Test
    @Order(12)
    @SuppressWarnings("unchecked")
    void deveRejeitarDeleteDePrimeiraParada() {
        ResponseEntity<List> stopsResp = restTemplate.exchange(url("/api/trips/" + tripId + "/stops"),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                List.class);

        Map<String, Object> firstStop = (Map<String, Object>) stopsResp.getBody().get(0);
        UUID firstStopId = UUID.fromString((String) firstStop.get("id"));

        ResponseEntity<Map> resp = restTemplate.exchange(
                url("/api/trips/" + tripId + "/stops"),
                HttpMethod.DELETE,
                new HttpEntity<>(List.of(firstStopId), authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }
}
