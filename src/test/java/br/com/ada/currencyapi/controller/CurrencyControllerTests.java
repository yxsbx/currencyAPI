package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTests {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    private MockMvc mockMvc;

    private final List<CurrencyResponse> currencies = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();

        currencies.add(CurrencyResponse.builder().label("1 - LCS").build());
        currencies.add(CurrencyResponse.builder().label("2 - YAS").build());
    }

    @Test
    public void testGetAPICurrencyRates() throws Exception {
        when(currencyService.getCurrencyAPI("USD-BRL")).thenReturn(List.of(new CurrencyResponse("USD - 5.36")));

        mockMvc.perform(get("/json/last/USD-BRL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("USD - 5.36"));
    }

    @Test
    void testGetCurrencyAPIRatesFailure() throws Exception {
        when(currencyService.getCurrencyAPI("USD-BRL")).thenThrow(new RuntimeException("Failed to fetch data"));

        mockMvc.perform(get("/json/last/USD-BRL"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Ensures that the endpoint returns all currencies correctly formatted and with a status of 200 OK.
     */

    @Test
    void testGetCurrenciesSuccess() throws Exception {
        when(currencyService.get()).thenReturn(currencies);

        mockMvc.perform(get("/get")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label", is("1 - LCS")))
                .andExpect(jsonPath("$[1].label", is("2 - YAS")))
                .andReturn();
    }

    /**
     * Validates that the creation endpoint returns the correct ID of the created currency and responds with 201 Created.
     */

    @Test
    void testCreateCurrenciesSuccess() throws Exception {
        when(currencyService.create(Mockito.any(CurrencyRequest.class))).thenReturn(1L);

        CurrencyRequest request = CurrencyRequest.builder().build();
        String jsonRequest = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", is(1)))
                .andReturn();
    }

    /**
     * Checks that currency conversion returns the expected converted amount and handles the request correctly.
     */

    @Test
    void testConvertCurrencies() throws Exception {
        ConvertCurrencyResponse response = new ConvertCurrencyResponse(BigDecimal.TEN);

        when(currencyService.convert(Mockito.any(ConvertCurrencyRequest.class))).thenReturn(response);

        mockMvc.perform(get("/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(10)))
                .andReturn();
    }

    /**
     * Tests the deletion process to ensure the endpoint responds with 200 OK and the service method is called as expected.
     */

    @Test
    void testDeleteCurrenciesSuccess() throws Exception {
        Mockito.doNothing().when(currencyService).delete(Mockito.anyLong());

        mockMvc.perform(delete("/{id}", Mockito.anyLong()))
                .andExpect(status().isOk())
                .andReturn();

        Mockito.verify(currencyService, Mockito.times(1)).delete(Mockito.anyLong());
    }
}
