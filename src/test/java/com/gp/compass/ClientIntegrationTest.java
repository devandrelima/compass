package com.gp.compass;

import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientIntegrationTest extends BaseIntegrationTest {

    private UUID clientId;
    private UUID addressId;

    // ── Cliente ──────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void deveCriarCliente() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/clients"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "João da Silva"), authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).containsKey("id");
        assertThat(resp.getBody().get("name")).isEqualTo("João da Silva");

        clientId = UUID.fromString((String) resp.getBody().get("id"));
    }

    @Test
    @Order(2)
    void deveBuscarClientePorId() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/clients/" + clientId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("id")).isEqualTo(clientId.toString());
        assertThat(resp.getBody().get("name")).isEqualTo("João da Silva");
    }

    @Test
    @Order(3)
    void deveListarClientes() {
        ResponseEntity<List> resp = restTemplate.exchange(url("/api/clients"),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                List.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    @Order(4)
    void deveAtualizarCliente() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/clients/" + clientId),
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("name", "João Atualizado"), authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("name")).isEqualTo("João Atualizado");
    }

    @Test
    @Order(5)
    void deveConfirmarAtualizacaoDoCliente() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/clients/" + clientId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getBody().get("name")).isEqualTo("João Atualizado");
    }

    // ── Endereço ─────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    void deveAdicionarEnderecoAoCliente() {
        Map<String, Object> body = Map.of(
                "cep", "01310-100",
                "street", "Av. Paulista",
                "neighborhood", "Bela Vista",
                "number", "1000",
                "city", "São Paulo",
                "state", "SP"
        );

        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/clients/" + clientId + "/addresses"),
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).containsKey("id");

        addressId = UUID.fromString((String) resp.getBody().get("id"));
    }

    @Test
    @Order(7)
    void deveBuscarEnderecoPorId() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/addresses/" + addressId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("street")).isEqualTo("Av. Paulista");
    }

    @Test
    @Order(8)
    void deveAtualizarEndereco() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/addresses/" + addressId),
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("street", "Rua Augusta", "number", "500"), authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("street")).isEqualTo("Rua Augusta");
        assertThat(resp.getBody().get("number")).isEqualTo("500");
    }

    @Test
    @Order(9)
    void deveConfirmarAtualizacaoDoEndereco() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/addresses/" + addressId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getBody().get("street")).isEqualTo("Rua Augusta");
    }

    @Test
    @Order(10)
    void deveDeletarEndereco() {
        ResponseEntity<Void> resp = restTemplate.exchange(url("/api/addresses/" + addressId),
                HttpMethod.DELETE,
                new HttpEntity<>(null, authHeaders()),
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(11)
    void deveDeletarCliente() {
        ResponseEntity<Void> resp = restTemplate.exchange(url("/api/clients/" + clientId),
                HttpMethod.DELETE,
                new HttpEntity<>(null, authHeaders()),
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(12)
    void deveRetornar404ParaClienteDeletado() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/clients/" + clientId),
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
