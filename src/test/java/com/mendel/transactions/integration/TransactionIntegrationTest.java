package com.mendel.transactions.integration;

import com.mendel.transactions.api.dto.StatusResponseDTO;
import com.mendel.transactions.api.dto.SumResponseDTO;
import com.mendel.transactions.api.dto.TransactionUpsertRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionIntegrationTest {

    @LocalServerPort
    int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void seedExampleData() {
        putTransaction(10, new TransactionUpsertRequestDTO(5000.0, "cars", null));
        putTransaction(11, new TransactionUpsertRequestDTO(10000.0, "shopping", 10L));
        putTransaction(12, new TransactionUpsertRequestDTO(5000.0, "shopping", 11L));
    }

    @Test
    void shouldReturnIdsByType() {
        ResponseEntity<Long[]> response =
                restTemplate.getForEntity(baseUrl("/transactions/types/cars"), Long[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.asList(response.getBody())).containsExactly(10L);
    }

    @Test
    void shouldReturnSumForTransaction10() {
        ResponseEntity<SumResponseDTO> response =
                restTemplate.getForEntity(baseUrl("/transactions/sum/10"), SumResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sum()).isEqualTo(20000.0);
    }

    @Test
    void shouldReturnSumForTransaction11() {
        ResponseEntity<SumResponseDTO> response =
                restTemplate.getForEntity(baseUrl("/transactions/sum/11") , SumResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sum()).isEqualTo(15000.0);
    }

    @Test
    void shouldReturn400_whenTypeIsBlank() {
        ResponseEntity<String> response = putRaw(20,
                """
                { "amount": 10.0, "type": "" }
                """
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldReturn400_whenAmountIsMissing() {
        ResponseEntity<String> response = putRaw(21,
                """
                { "type": "cars" }
                """
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldReturn404_whenGettingSumForUnknownTransaction() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(baseUrl("/transactions/sum/999999"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldReturn400_whenParentDoesNotExist() {
        ResponseEntity<String> response = putRaw(30,
                """
                { "amount": 1.0, "type": "x", "parent_id": 999999 }
                """
        );

        // Yo recomiendo 400 (bad request) porque el request es inválido
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldRejectCycles_whenUpdatingParentToDescendant() {
        // Caso borde: ya existe 10 -> 11 -> 12
        // Intento: actualizar 10 para que su parent sea 12 (crea ciclo 10 -> 11 -> 12 -> 10)
        ResponseEntity<String> response = putRaw(10,
                """
                { "amount": 5000.0, "type": "cars", "parent_id": 12 }
                """
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    private ResponseEntity<String> putRaw(long id, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        return restTemplate.exchange(
                baseUrl("/transactions/" + id),
                HttpMethod.PUT,
                entity,
                String.class
        );
    }

    private void putTransaction(long id, TransactionUpsertRequestDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TransactionUpsertRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<StatusResponseDTO> response = restTemplate.exchange(
                baseUrl("/transactions/" + id),
                HttpMethod.PUT,
                entity,
                StatusResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ok");
    }
}