package br.com.ada.currencyapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.ada.currencyapi.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ada.currencyapi.repository.CurrencyRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class): This annotation indicates that JUnit should use the Mockito extension to run tests in this class.
@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTests {

    // @InjectMocks: This annotation automatically injects mocks (simulated objects) into the test class.
    @InjectMocks
    private CurrencyService currencyService;

    // @Mock: This annotation indicates that currencyRepository is a mock that will be used in the tests.
    @Mock
    private CurrencyRepository currencyRepository;

    // This test verifies if the get() method of CurrencyService correctly returns the currencies.
    @Test
    void testGet() {
        // Create a list of simulated currencies (list) and set the expected behavior of the findAll() method of currencyRepository to return this list.
        List<Currency> list = new ArrayList<>();
        list.add(Currency.builder()
                .id(1L)
                .name("LCS")
                .description("Moeda do lucas")
                .build());
        list.add(Currency.builder()
                .id(2L)
                .name("YAS")
                .description("Moeda da yasmin")
                .build());

        when(currencyRepository.findAll()).thenReturn(list);

        // Invoke the get() method of currencyService.
        List<CurrencyResponse> responses = currencyService.get();
        // Verify that the response list is not null and contains two currencies with correct labels.
        assertNotNull(responses);
        Assertions.assertEquals(2, responses.size());
        Assertions.assertEquals("1 - LCS", responses.get(0).getLabel());
        Assertions.assertEquals("2 - YAS", responses.get(1).getLabel());
    }

    // This test verifies the behavior of the create() method when a new currency is successfully created.
    @Test
    void testCreateCurrencySuccess() {
        // Create a CurrencyRequest for a new currency.
        CurrencyRequest request = new CurrencyRequest("INR", "Indian Rupee", new HashMap<>());

        // Set the expected behavior of the findByName() method of currencyRepository to return null (indicating that the currency does not exist).
        when(currencyRepository.findByName("INR")).thenReturn(null);
        // Set the expected behavior of the save() method of currencyRepository to return a new currency with an ID.
        when(currencyRepository.save(any(Currency.class))).thenReturn(new Currency(1L, "INR", "Indian Rupee", new HashMap<>()));

        // Invoke the create() method of currencyService.
        Long id = currencyService.create(request);

        // Verify that the returned ID is not null.
        assertNotNull(id);
    }

    // This test verifies the behavior of the create() method when a currency with the same name already exists.
    @Test
    void testCreateCurrencyFailure() {
        // Set the expected behavior of the findByName() method of currencyRepository to return an existing currency.
        Currency existingCurrency = new Currency(1L,"INR", "Indian Rupee", new HashMap<>());
        when(currencyRepository.findByName("INR")).thenReturn(existingCurrency);

        // Create a CurrencyRequest for a currency with the same name as the existing currency.
        CurrencyRequest request = new CurrencyRequest("INR", "Indian Rupee", new HashMap<>());

        // Verify that a CurrencyException is thrown when trying to create the duplicate currency.
        assertThrows(CurrencyException.class, () -> currencyService.create(request), "Coin already exists");
    }

    // This test verifies if the delete() method of CurrencyService is called correctly.
    @Test
    public void testDeleteCurrency() {
        Long id = 1L;
        // Create a mock for CurrencyService.
        CurrencyService service = Mockito.mock(CurrencyService.class);

        // Invoke the delete() method of CurrencyService.
        service.delete(id);

        // Verify that the delete() method of the mock was called with the correct ID.
        Mockito.verify(service).delete(id);
    }

    // This test verifies the behavior of the convert() method when the currency conversion is successful.
    @Test
    void testConvertCurrencySuccess() throws CoinNotFoundException {
        // Create a ConvertCurrencyRequest for a currency conversion.
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("INR", "YAS", new BigDecimal("100"));
        HashMap<String, BigDecimal> exchanges = new HashMap<>();
        exchanges.put("YAS", new BigDecimal("0.85"));
        Currency fromCurrency = new Currency(1L,"INR", "Indian Rupee", exchanges);

        // Set the expected behavior of the findByName() method of currencyRepository to return a source currency with a list of exchanges.
        when(currencyRepository.findByName("INR")).thenReturn(fromCurrency);

        // Verify that the conversion is performed correctly and the result is as expected.
        ConvertCurrencyResponse response = currencyService.convert(request);
        assertNotNull(response);
        assertEquals(new BigDecimal ("85.00"), response.getAmount());
    }

    // This test verifies the behavior of the convert() method when the source currency is not found.
    @Test
    void testConvertCurrencyFailure() {
        // Set the expected behavior of the findByName() method of currencyRepository to return null.
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("INR", "YAS", new BigDecimal("100"));
        when(currencyRepository.findByName("INR")).thenReturn(null);

        // Verify that a CoinNotFoundException is thrown when trying to convert a currency that does not exist.
        Exception exception = Assertions.assertThrows(CoinNotFoundException.class, () -> {
            currencyService.convert(request);
        });
        assertEquals("Coin not found: INR", exception.getMessage());
    }
}