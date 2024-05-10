package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

// Indicates that the test should load the complete configuration of the Spring Boot application.
@SpringBootTest
// Requests that MockMvc be configured automatically for the tests.
@AutoConfigureMockMvc
class CurrencyControllerIntegrationTests {

    // Indicates that the attribute should be injected by Spring.
    // Instance of MockMvc, which is a class provided by Spring to test controllers without the need to start a real HTTP server.
    @Autowired
    private MockMvc mockMvc;

    // Creates a mock of the specified type to be used in the test.
    // Mock of the CurrencyService type, used to simulate the behavior of the real service during testing.
    @Mock
    private CurrencyService currencyService;

    // List of currency responses used in the tests.
    private List<CurrencyResponse> currencies;

    // Indicates that the method will be executed before each test.
    // Method executed before each test.
    @BeforeEach
    void setUp() {
        // Configures MockMvc using the CurrencyController controller.
        mockMvc = MockMvcBuilders.standaloneSetup(new CurrencyController(currencyService)).build();

        // Initializes the currencies list with some simulated currencies.
        currencies = new ArrayList<>();
        currencies.add(CurrencyResponse.builder().label("1 - LCS").build());
        currencies.add(CurrencyResponse.builder().label("2 - YAS").build());
    }

    // Tests the GET endpoint /currency, which returns a list of currencies.
    @Test
    void testGet() throws Exception {
        // Uses Mockito to simulate the response from the CurrencyService.
        when(currencyService.get()).thenReturn(currencies);

        // Performs a simulated GET request to the /currency endpoint using MockMvc.
        ResultActions resultActions = mockMvc.perform(get("/currency")
                .contentType(MediaType.APPLICATION_JSON));

        // Verifies if the received response contains the correct number of currencies and if the currency labels are correct.
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label", is("1 - LCS")))
                .andExpect(jsonPath("$[1].label", is("2 - YAS")));
    }

    // Tests the POST endpoint /currency/convert, which converts a value from one currency to another based on exchange rates.
    @Test
    void testConvert() throws Exception {
        ConvertCurrencyResponse response = new ConvertCurrencyResponse(BigDecimal.valueOf(90.0));
        // Configures Mockito to simulate the response from the CurrencyService.
        when(currencyService.convert(any(ConvertCurrencyRequest.class))).thenReturn(response);

        // Performs a simulated POST request to the /currency/convert endpoint using MockMvc.
        ResultActions resultActions = mockMvc.perform(post("/currency/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"from\":\"USD\",\"to\":\"EUR\",\"amount\":100.0}"));

        // Verifies if the received response contains the correct conversion value.
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(90.0));
    }

    // Tests the POST endpoint /currency, which creates a new currency in the system.
    @Test
    void testCreate() throws Exception {
        // Configures Mockito to simulate the response from the CurrencyService.
        when(currencyService.create(any(CurrencyRequest.class))).thenReturn(1L);

        // Performs a simulated POST request to the /currency endpoint using MockMvc.
        ResultActions resultActions = mockMvc.perform(post("/currency")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"USD\",\"description\":\"US Dollar\",\"exchanges\":{}}"));

        // Verifies if the received response has the correct status code for successful creation of a currency.
        resultActions.andExpect(status().isCreated());
    }

    // Tests the DELETE endpoint /currency/{id}, which deletes an existing currency based on its ID.
    @Test
    void testDelete() throws Exception {
        Long id = 1L;

        // Performs a simulated DELETE request to the /currency/{id} endpoint using MockMvc.
        ResultActions resultActions = mockMvc.perform(delete("/currency/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));

        // Verifies if the received response has the correct status code for successful deletion of a currency.
        resultActions.andExpect(status().isOk());
    }
}
