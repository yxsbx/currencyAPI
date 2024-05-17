package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Anotação: @WebMvcTest(CurrencyController.class) - Esta anotação carrega apenas o contexto Web MVC e escaneia apenas o controlador especificado (CurrencyController).
// Ideal para testar controladores Spring Boot isoladamente.
// @Autowired - Injeta a instância do MockMvc que simula requisições HTTP.
// @MockBean - Cria um mock do CurrencyService para ser usado no teste.

@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    /**
     * Mockar o método get() do currencyService para retornar uma lista vazia.
     * Fazer uma requisição GET para o endpoint /currency.
     * Verificar se o status da resposta é 200 (OK).
     * Verificar se a resposta é um array JSON vazio.
     */

    @Test
    void testGet() throws Exception {
        Mockito.when(currencyService.get()).thenReturn(Collections.emptyList());

        // andExpect(jsonPath("$").isArray()): Verifica se o corpo da resposta JSON é um array.
        // andExpect(status().isOk()): Verifica se o status da resposta HTTP é 200 OK.
        mockMvc.perform(get("/currency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Criar uma instância de ConvertCurrencyRequest e configurar os valores.
     * Fazer uma requisição POST para o endpoint /currency/convert com o corpo JSON especificado.
     * Verificar se o status da resposta é 200 (OK).
     */

    @Test
    void testConvert() throws Exception {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/currency/convert")
                        .contentType("application/json")
                        .content("{\"from\": \"USD\", \"to\": \"BRL\", \"amount\": 100}"))
                .andExpect(status().isOk());
    }

    /**
     * Criar uma instância de CurrencyRequest e configurar os valores.
     * Fazer uma requisição POST para o endpoint /currency com o corpo JSON especificado.
     * Verificar se o status da resposta é 201 (Created).
     */

    @Test
    void testCreate() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        // andExpect(status().isCreated()): Verifica se o status da resposta HTTP é 201 Created.
        mockMvc.perform(post("/currency")
                        .contentType("application/json")
                        .content("{\"name\": \"Dólar Americano\", \"code\": \"USD\"}"))
                .andExpect(status().isCreated());
    }

    /**
     * Criar uma instância de CurrencyRequest e configurar os valores.
     * Fazer uma requisição PUT para o endpoint /currency/1 com o corpo JSON especificado.
     * Verificar se o status da resposta é 200 (OK).
     */

    @Test
    void testUpdate() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        mockMvc.perform(put("/currency/1")
                        .contentType("application/json")
                        .content("{\"name\": \"Dólar Americano\", \"code\": \"USD\"}"))
                .andExpect(status().isOk());
    }

    /**
     * Fazer uma requisição DELETE para o endpoint /currency/1.
     * Verificar se o status da resposta é 200 (OK).
     */

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/currency/1"))
                .andExpect(status().isOk());
    }
}