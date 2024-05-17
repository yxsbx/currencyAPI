package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import br.com.ada.currencyapi.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class CurrencyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setup() {
        currencyRepository.deleteAll();
    }

    /**
     * Arrange: Cria um objeto CurrencyResponse com o rótulo "USD - Dólar Americano" e o coloca em uma lista.
     * Mock: Configura o mock currencyService para retornar essa lista quando o método get() for chamado.
     * Act: Faz uma requisição GET para o endpoint /currency usando o MockMvc.
     * Assert: Verifica se a resposta tem o status HTTP 200 (OK) e se o JSON retornado contém um objeto com o rótulo "USD - Dólar Americano".
     */

    @Test
    void testGet() throws Exception {
        CurrencyResponse response1 = CurrencyResponse.builder().label("USD - Dólar Americano").build();
        List<CurrencyResponse> responses = Collections.singletonList(response1);

        Mockito.when(currencyService.get()).thenReturn(responses);

        mockMvc.perform(get("/currency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label", is("USD - Dólar Americano")));
    }

    /**
     * Arrange: Cria um objeto ConvertCurrencyResponse com o valor de 500.
     * Mock: Configura o mock currencyService para retornar esse objeto quando o método convert for chamado com qualquer ConvertCurrencyRequest.
     * Act: Faz uma requisição POST para o endpoint /currency/convert com um corpo JSON contendo os detalhes da conversão.
     * Assert: Verifica se a resposta tem o status HTTP 200 (OK) e se o JSON retornado contém o valor amount igual a 500.
     */

    @Test
    void testConvert() throws Exception {
        ConvertCurrencyResponse convertResponse = ConvertCurrencyResponse.builder().amount(BigDecimal.valueOf(500)).build();

        Mockito.when(currencyService.convert(any(ConvertCurrencyRequest.class))).thenReturn(convertResponse);

        mockMvc.perform(post("/currency/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\": \"USD\", \"to\": \"BRL\", \"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(500)));
    }

    /**
     * Arrange: Configura o mock currencyService para retornar 1L quando o método create for chamado com qualquer CurrencyRequest.
     * Act: Faz uma requisição POST para o endpoint /currency com um corpo JSON contendo o nome e o código da moeda.
     * Assert: Verifica se a resposta tem o status HTTP 201 (Created) e se o corpo da resposta é a string "1".
     */

    @Test
    void testCreate() throws Exception {
        Mockito.when(currencyService.create(any(CurrencyRequest.class))).thenReturn(1L);

        mockMvc.perform(post("/currency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Dólar Americano\", \"code\": \"USD\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    /**
     * Arrange: Cria um objeto ConvertCurrencyResponse com o valor de 500.
     * Mock: Configura o mock currencyService para retornar esse objeto quando o método convert for chamado com qualquer ConvertCurrencyRequest.
     * Act: Faz uma requisição POST para o endpoint /currency/convert com um corpo JSON contendo os detalhes da conversão.
     * Assert: Verifica se a resposta tem o status HTTP 200 (OK) e se o JSON retornado contém o valor amount igual a 500.
     */

    @Test
    void testUpdate() throws Exception {
        Mockito.doNothing().when(currencyService).update(anyLong(), any(CurrencyRequest.class));

        mockMvc.perform(put("/currency/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Dólar Americano\", \"code\": \"USD\"}"))
                .andExpect(status().isOk());
    }

    /**
     * Arrange: Configura o mock currencyService para não fazer nada quando o método update for chamado com qualquer Long e CurrencyRequest.
     * Act: Faz uma requisição PUT para o endpoint /currency/1 com um corpo JSON contendo o nome e o código da moeda.
     * Assert: Verifica se a resposta tem o status HTTP 200 (OK).
     */

    @Test
    void testDelete() throws Exception {
        Mockito.doNothing().when(currencyService).delete(anyLong());

        mockMvc.perform(delete("/currency/1"))
                .andExpect(status().isOk());
    }
}