package com.gp.compass;

import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest extends BaseIntegrationTest {

    private static final String EMAIL = "auth-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    private static final String SENHA = "senha123";

    @Test
    @Order(1)
    void deveRegistrarNovoUsuario() {
        ResponseEntity<Map> resp = restTemplate.postForEntity(url("/api/auth/register"),
                new HttpEntity<>(Map.of("name", "Motorista", "email", EMAIL, "password", SENHA), jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).containsKey("accessToken");
        assertThat(resp.getBody().get("accessToken")).isNotNull();
    }

    @Test
    @Order(2)
    void deveFazerLoginERetornarToken() {
        ResponseEntity<Map> resp = restTemplate.postForEntity(url("/api/auth/login"),
                new HttpEntity<>(Map.of("email", EMAIL, "password", SENHA), jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("accessToken")).isNotNull();
        assertThat(resp.getBody().get("tokenType")).isEqualTo("Bearer");
    }

    @Test
    @Order(3)
    void deveRejeitarLoginComSenhaErrada() {
        ResponseEntity<Map> resp = restTemplate.postForEntity(url("/api/auth/login"),
                new HttpEntity<>(Map.of("email", EMAIL, "password", "senhaerrada"), jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @Order(4)
    void deveRejeitarAcessoSemToken() {
        ResponseEntity<Map> resp = restTemplate.exchange(url("/api/clients"),
                HttpMethod.GET,
                new HttpEntity<>(null, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
