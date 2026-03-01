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

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void seedExampleData() {
        // PUT /transactions/10 { amount:5000, type:"cars" }
        putTransaction(10, new TransactionUpsertRequestDTO(5000.0, "cars", null));

        // PUT /transactions/11 { amount:10000, type:"shopping", parent_id:10 }
        putTransaction(11, new TransactionUpsertRequestDTO(10000.0, "shopping", 10L));

        // PUT /transactions/12 { amount:5000, type:"shopping", parent_id:11 }
        putTransaction(12, new TransactionUpsertRequestDTO(5000.0, "shopping", 11L));
    }

    @Test
    void shouldReturnIdsByType() {
        ResponseEntity<Long[]> response =
                restTemplate.getForEntity(baseUrl() + "/transactions/types/cars", Long[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.asList(response.getBody())).containsExactly(10L);
    }

    @Test
    void shouldReturnSumForTransaction10() {
        ResponseEntity<SumResponseDTO> response =
                restTemplate.getForEntity(baseUrl() + "/transactions/sum/10", SumResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sum()).isEqualTo(20000.0);
    }

    @Test
    void shouldReturnSumForTransaction11() {
        ResponseEntity<SumResponseDTO> response =
                restTemplate.getForEntity(baseUrl() + "/transactions/sum/11", SumResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sum()).isEqualTo(15000.0);
    }

    private void putTransaction(long id, TransactionUpsertRequestDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TransactionUpsertRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<StatusResponseDTO> response = restTemplate.exchange(
                baseUrl() + "/transactions/" + id,
                HttpMethod.PUT,
                entity,
                StatusResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ok");
    }
}