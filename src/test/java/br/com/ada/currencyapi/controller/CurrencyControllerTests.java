package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CurrencyService currencyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetCurrencies() throws Exception {
        CurrencyResponse currency1 = CurrencyResponse.builder().label("USD - US Dollar").build();
        CurrencyResponse currency2 = CurrencyResponse.builder().label("EUR - Euro").build();
        List<CurrencyResponse> currencyList = Arrays.asList(currency1, currency2);

        when(currencyService.get()).thenReturn(currencyList);

        mockMvc.perform(get("/currency"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].label").value("USD - US Dollar"))
                .andExpect(jsonPath("$[1].label").value("EUR - Euro"));
    }

    @Test
    void testConvertCurrency() throws Exception {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.valueOf(100))
                .build();

        ConvertCurrencyResponse expectedResponse = ConvertCurrencyResponse.builder()
                .amount(BigDecimal.valueOf(85))
                .build();

        when(currencyService.convert(any(ConvertCurrencyRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/currency/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(85));
    }

    @Test
    void testConvertCurrency_CoinNotFoundException() throws Exception {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.valueOf(100))
                .build();

        when(currencyService.convert(any(ConvertCurrencyRequest.class))).thenThrow(new CoinNotFoundException("Coin not found: USD"));

        mockMvc.perform(post("/currency/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCurrency() throws Exception {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        Long expectedId = 1L;

        when(currencyService.create(any(CurrencyRequest.class))).thenReturn(expectedId);

        mockMvc.perform(post("/currency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(expectedId));
    }

    @Test
    void testCreateCurrency_CurrencyException() throws Exception {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        when(currencyService.create(any(CurrencyRequest.class))).thenThrow(new CurrencyException("Coin already exists"));

        mockMvc.perform(post("/currency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateCurrency() throws Exception {
        Long id = 1L;
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        doNothing().when(currencyService).update(id, request);

        mockMvc.perform(put("/currency/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCurrency_CurrencyException() throws Exception {
        Long id = 1L;
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        doThrow(new CurrencyException("Coin already exists")).when(currencyService).update(id, request);

        mockMvc.perform(put("/currency/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCurrency() throws Exception {
        Long id = 1L;

        doNothing().when(currencyService).delete(id);

        mockMvc.perform(delete("/currency/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCurrency_CoinNotFoundException() throws Exception {
        Long id = 1L;

        doThrow(new CoinNotFoundException("Coin not found: " + id)).when(currencyService).delete(id);

        mockMvc.perform(delete("/currency/{id}", id))
                .andExpect(status().isNotFound());
    }
}
