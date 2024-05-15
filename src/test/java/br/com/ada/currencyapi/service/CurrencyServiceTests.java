package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.feign.AwesomeAPIClient;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTests {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private AwesomeAPIClient awesomeApiClient;

    @InjectMocks
    private CurrencyService currencyService;

    private final List<Currency> currencyList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        currencyList.add(Currency.builder().id(1L).name("LCS").description("Lucas").exchanges(new HashMap<>()).build());
        currencyList.add(Currency.builder().id(2L).name("YAS").description("Yasmin").exchanges(new HashMap<>()).build());
    }

    @Test
    void testGetCurrencyAPI() {
        Map<String, CurrencyAPIResponse> mockResponse = new HashMap<>();

        mockResponse.put("USDBRL", new CurrencyAPIResponse(
                "USD", "BRL", "Dólar Americano/Real Brasileiro",
                new BigDecimal("5.4"), new BigDecimal("5.3"),
                new BigDecimal("-0.1"), convertPercentageToBigDecimal("-0.02%"),
                new BigDecimal("5.35"), new BigDecimal("5.36"),
                "1609459200", "2021-01-01T00:00:00Z"
        ));
        mockResponse.put("EURBRL", new CurrencyAPIResponse(
                "EUR", "BRL", "Euro/Real Brasileiro",
                new BigDecimal("6.2"), new BigDecimal("6.1"),
                new BigDecimal("-0.05"), convertPercentageToBigDecimal("-0.08%"),
                new BigDecimal("6.15"), new BigDecimal("6.16"),
                "1609459200", "2021-01-01T00:00:00Z"
        ));

        when(awesomeApiClient.getLastCurrencyAPI("USD-BRL,EUR-BRL")).thenReturn(mockResponse);

        List<CurrencyResponse> responses = currencyService.getCurrencyAPI("USD-BRL,EUR-BRL");

        responses.forEach(response -> System.out.println(response.getLabel()));

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.getLabel().contains("USD") && r.getLabel().contains("5.36")));
        assertTrue(responses.stream().anyMatch(r -> r.getLabel().contains("EUR") && r.getLabel().contains("6.16")));

        verify(awesomeApiClient).getLastCurrencyAPI("USD-BRL,EUR-BRL");
    }

    private BigDecimal convertPercentageToBigDecimal(String percentage) {
        if (percentage == null) return null;
        return new BigDecimal(percentage.replace("%", "")).divide(BigDecimal.valueOf(100));
    }

    /**
     * Checks that all currencies are retrieved and returned as DTOs.
     */

    @Test
    void testGetCurrenciesSuccess() {
        when(currencyRepository.findAll()).thenReturn(currencyList);
        List<CurrencyResponse> responses = currencyService.get();
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(currencyRepository).findAll();
    }

    /**
     * Ensures the service handles and returns an empty list correctly when no currencies are available.
     */

    @Test
    void testGetCurrenciesFailureEmpty() {
        when(currencyRepository.findAll()).thenReturn(new ArrayList<>());

        List<CurrencyResponse> responses = currencyService.get();
        assertEquals(0, responses.size());
    }

    /**
     * Confirms that a currency can be successfully created if it does not already exist.
     */

    @Test
    void testCreateCurrenciesSuccess() {
        when(currencyRepository.findByName(Mockito.anyString())).thenReturn(null);
        when(currencyRepository.save(Mockito.any(Currency.class))).thenReturn(currencyList.get(0));

        CurrencyRequest request = CurrencyRequest.builder().name("BTC").description("Bitcoin").exchanges(new HashMap<>()).build();

        assertEquals(1L, currencyService.create(request));
    }

    /**
     * Ensures the service throws an exception when attempting to create a currency that already exists.
     */

    @Test
    void testCreateCurrenciesFailureAlreadyExists() {
        when(currencyRepository.findByName(Mockito.anyString())).thenReturn(currencyList.get(0));

        CurrencyRequest request = CurrencyRequest.builder().name("YAS").description("Yasmin").exchanges(new HashMap<>()).build();

        assertThrows(CurrencyException.class, () -> currencyService.create(request));
    }

    /**
     * Tests the deletion functionality by confirming the repository's delete method is called.
     */

    @Test
    void testDeleteCurrenciesSuccess() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.ofNullable(currencyList.get(0)));
        currencyService.delete(1L);
        verify(currencyRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

    /**
     * Checks the appropriate exception is thrown when trying to delete a non-existing currency.
     */

    @Test
    void testDeleteCurrenciesNotExists() {
        when(currencyRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        CoinNotFoundException exception = assertThrows(CoinNotFoundException.class, () -> currencyService.delete(1L));
        assertEquals("Coin not found: 1", exception.getMessage());
    }

    /**
     * Verifies conversion logic by using the repository to fetch currency and exchange rates.
     */

    @Test
    void testConvertCurrencies() {
        currencyList.get(0).getExchanges().put("BTC", BigDecimal.TEN);

        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder().to("BTC").from("YAS").amount(BigDecimal.ONE).build();

        when(currencyRepository.findByName("YAS")).thenReturn(currencyList.get(0));

        Assertions.assertEquals(BigDecimal.TEN, currencyService.convert(request).getAmount());

    }

    /**
     * Test error handling when necessary data is missing or incorrect in conversion requests.
     */

    @Test
    void convertCurrenciesExchangeNotFound() {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder().to("BTC").from("YAS").amount(BigDecimal.ONE).build();

        when(currencyRepository.findByName("YAS")).thenReturn(currencyList.get(0));

        CoinNotFoundException exception = assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));
        assertEquals("Exchange BTC not found for YAS", exception.getMessage());
    }

    /**
     * Test error handling when necessary data is missing or incorrect in conversion requests.
     */

    @Test
    void convertCurrenciesCoinNotFound() {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder().to("BTC").from("YAS").amount(BigDecimal.ONE).build();

        CoinNotFoundException exception = assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));
        assertEquals("Coin not found: YAS", exception.getMessage());
    }
}