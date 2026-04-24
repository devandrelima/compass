package com.gp.compass;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    protected int port;

    protected RestTemplate restTemplate;
    protected String token;

    @BeforeAll
    void setup() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });

        String email = getClass().getSimpleName().toLowerCase()
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8)
                + "@test.com";

        restTemplate.postForEntity(url("/api/auth/register"),
                new HttpEntity<>(Map.of("name", "Motorista Teste", "email", email, "password", "senha123"), jsonHeaders()),
                Map.class);

        ResponseEntity<Map> login = restTemplate.postForEntity(url("/api/auth/login"),
                new HttpEntity<>(Map.of("email", email, "password", "senha123"), jsonHeaders()),
                Map.class);

        token = (String) login.getBody().get("accessToken");
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    protected HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    protected Map<String, Object> snapshotAddress(String street, String number) {
        return Map.of(
                "cep", "01310-100",
                "street", street,
                "neighborhood", "Centro",
                "number", number,
                "city", "São Paulo",
                "state", "SP"
        );
    }
}
