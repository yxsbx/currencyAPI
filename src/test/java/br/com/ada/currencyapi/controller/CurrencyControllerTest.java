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

@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @Test
    void testGet() throws Exception {
        Mockito.when(currencyService.get()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/currency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

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

    @Test
    void testCreate() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        mockMvc.perform(post("/currency")
                        .contentType("application/json")
                        .content("{\"name\": \"Dólar Americano\", \"code\": \"USD\"}"))
                .andExpect(status().isCreated());
    }

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

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/currency/1"))
                .andExpect(status().isOk());
    }
}