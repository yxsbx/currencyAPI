package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import br.com.ada.currencyapi.service.CurrencyService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Transactional
public class CurrencyControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyRepository currencyRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetCurrencies200() throws Exception {
        assertEquals(0, currencyRepository.count());

        currencyRepository.save(new Currency(1L, "BRL", "BRL", null));

        mockMvc.perform(get("/currency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testCreateCurrenciesSuccess() throws Exception {
        HashMap<String, BigDecimal> exchangeRates = new HashMap<>();
        exchangeRates.put("USD", new BigDecimal("1.1"));
        CurrencyRequest request = new CurrencyRequest("EUR", "Euro", exchangeRates);

        mockMvc.perform(post("/currency/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertEquals(1, currencyRepository.count());
        currencyRepository.deleteAll();
    }

    @Test
    void testCreateCurrencies200() throws Exception {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .build();

        var content = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/currency/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$").isNotEmpty())
                .andDo(MockMvcResultHandlers.print());

        currencyRepository.deleteAll();
    }

    @Test
    void testCreateCurrencies500() throws Exception {
        currencyRepository.save(new Currency(null, "USD", "Dollars", null));
        CurrencyRequest request = new CurrencyRequest("USD", null, null);

        mockMvc.perform(post("/currency/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Coin already exists"))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testConvertCurrencies() throws Exception {
        Currency currency = new Currency(null, "USD", "US Dollar", Collections.singletonMap("EUR", new BigDecimal("0.85")));
        currencyRepository.save(currency);

        mockMvc.perform(get("/currency/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(85.0)))
                .andReturn();

        currencyRepository.deleteAll();
    }

    @Test
    void testConvertCurrenciesCoinNotFound() throws Exception {
        mockMvc.perform(get("/currency/convert?from=BRL&to=USD&amount=5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Coin not found: BRL"))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testConvertCurrenciesExchangeNotFound() throws Exception {
        currencyRepository.save(new Currency(1L, "BRL", "BRL", Map.of("USD", BigDecimal.TEN)));

        mockMvc.perform(get("/currency/convert?from=BRL&to=EUR&amount=5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Exchange EUR not found for BRL"))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testConvertCurrencies200() throws Exception {
        currencyRepository.save(new Currency(1L, "BRL", "BRL", Map.of("USD", BigDecimal.TEN)));

        mockMvc.perform(get("/currency/convert?from=BRL&to=USD&amount=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(new BigDecimal("50")))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testDeleteCurrenciesSuccess() throws Exception {
        Currency currency = new Currency(null, "JPY", "Japanese Yen", new HashMap<>());
        Currency savedCurrency = currencyRepository.save(currency);

        mockMvc.perform(delete("/currency/delete/{id}", savedCurrency.getId()))
                .andExpect(status().isOk());

        assertFalse(currencyRepository.findById(savedCurrency.getId()).isPresent());
    }

    @Test
    void testDeleteCurrencies200() throws Exception {
        assertEquals(0, currencyRepository.count());

        Currency currency = currencyRepository.save(new Currency(null, "BRL", "BRL", null));
        mockMvc.perform(delete("/currency/delete/{id}", currency.getId()))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
