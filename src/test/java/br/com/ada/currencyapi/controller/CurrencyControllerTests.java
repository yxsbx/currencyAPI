package br.com.ada.currencyapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.service.CurrencyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// GET /currency: Returns a list of available currencies.
// POST /currency/convert: Converts an amount from one currency to another based on exchange rates.
// POST /currency: Creates a new currency in the system.
// DELETE /currency/{id}: Deletes an existing currency based on its ID.
// Indicates that JUnit should use the Mockito extension to execute tests in this class. This sets up the environment to use Mockito in the test.
@ExtendWith(MockitoExtension.class)
class CurrencyControllerTests {

    // An instance of MockMvc, which is a class provided by Spring for testing controllers without the need to start an HTTP server.
    private MockMvc mockMvc;

    // A mock for the CurrencyService class, used to simulate the behavior of the service during tests.
    @Mock
    private CurrencyService currencyService;

    // An instance of the controller being tested. The previously defined mocks will be injected into this controller.
    @InjectMocks
    private CurrencyController currencyController;

    // Indicates that the method should be executed before each test. In the setUp() method, the MockMvc object is configured to test the controller's endpoints.
    @BeforeEach
    // Test environment setup. Initializes MockMvc to test the controller's endpoints.
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();
    }

    // Tests the GET /currency endpoint, which returns a list of currencies.
    @Test
    void testGet() throws Exception{
        List<CurrencyResponse> list = new ArrayList<>();
        list.add(CurrencyResponse.builder().label("1 - LCS").build());
        list.add(CurrencyResponse.builder().label("2 - YAS").build());
        when(currencyService.get()).thenReturn(list);

        // Configures the behavior of the currencyService mock to return a list of simulated currencies.
        // Uses MockMvc to simulate a GET request to the /currency endpoint.
        mockMvc.perform(get("/currency")
                .contentType(MediaType.APPLICATION_JSON))
                // Verifies if the response is correct, comparing the response status, the size of the list, and the labels of the currencies.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label", is("1 - LCS")))
                .andExpect(jsonPath("$[1].label", is("2 - YAS")))
                .andDo(MockMvcResultHandlers.print());

        // Verifies if the get() method of currencyService was called exactly once.
        verify(currencyService, times(1)).get();
    }

    // Tests the POST /currency/convert endpoint, which converts an amount from one currency to another.
    @Test
    void testConvert() throws Exception {
        ConvertCurrencyResponse response = new ConvertCurrencyResponse(BigDecimal.valueOf(90.0));
        // Configures the behavior of the currencyService mock to return a simulated response.
        when(currencyService.convert(any(ConvertCurrencyRequest.class))).thenReturn(response);

        // Verifies if the response is correct, comparing the response status and the converted value.
        mockMvc.perform(post("/currency/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"USD\",\"to\":\"EUR\",\"amount\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(90.0));

        // Verifies if the convert() method of currencyService was called exactly once.
        verify(currencyService, times(1)).convert(any(ConvertCurrencyRequest.class));
    }

    // Tests the POST /currency endpoint, which creates a new currency.
    @Test
    void testCreate() throws Exception {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .build();

        // Configures the behavior of the currencyService mock to return the ID of the created currency.
        when(currencyService.create(any(CurrencyRequest.class))).thenReturn(1L);

        // Uses MockMvc to simulate a POST request to the /currency endpoint.
        mockMvc.perform(post("/currency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"USD\",\"description\":\"US Dollar\",\"exchanges\":{}}"))
                // Verifies if the response is correct, comparing the response status.
                .andExpect(status().isCreated());

        // Verifies if the create() method of currencyService was called exactly once.
        verify(currencyService, times(1)).create(any(CurrencyRequest.class));
    }

    // Tests the DELETE /currency/{id} endpoint, which deletes a currency by its ID.
    @Test
    void testDelete() throws Exception {
        Long id = 1L;

        // Uses MockMvc to simulate a DELETE request to the /currency/{id} endpoint.
        mockMvc.perform(delete("/currency/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                // Verifies if the response is correct, comparing the response status.
                .andExpect(status().isOk());

        // Verifies if the delete() method of currencyService was called exactly once.
        verify(currencyService, times(1)).delete(id);
    }
}
