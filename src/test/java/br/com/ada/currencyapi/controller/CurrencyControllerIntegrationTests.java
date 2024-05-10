package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyService currencyService;

    private List<CurrencyResponse> currencies;

    @BeforeEach
    void setUp() {
        currencies = new ArrayList<>();
        currencies.add(CurrencyResponse.builder().label("1 - LCS").build());
        currencies.add(CurrencyResponse.builder().label("2 - YAS").build());
    }

    @Test
    void testGet() throws Exception {
        // Mocking the service response
        when(currencyService.get()).thenReturn(currencies);

        // Performing a GET request to /currency endpoint
        ResultActions resultActions = mockMvc.perform(get("/currency")
                .contentType(MediaType.APPLICATION_JSON));

        // Verifying if the response is correct
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label", is("1 - LCS")))
                .andExpect(jsonPath("$[1].label", is("2 - YAS")));
    }

    @Test
    void testConvert() throws Exception {
        ConvertCurrencyResponse response = new ConvertCurrencyResponse(BigDecimal.valueOf(90.0));
        // Mocking the service response
        when(currencyService.convert(any(ConvertCurrencyRequest.class))).thenReturn(response);

        // Performing a POST request to /currency/convert endpoint
        ResultActions resultActions = mockMvc.perform(post("/currency/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"from\":\"USD\",\"to\":\"EUR\",\"amount\":100.0}"));

        // Verifying if the response is correct
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(90.0));
    }
}
