package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// This annotation informs Spring that this is a data access integration test, and it should configure the test environment to support database access.
@DataJpaTest
class CurrencyServiceIntegrationTests {

    // This field is automatically injected by Spring and represents the currency repository, which will be used in the tests.
    @Autowired
    private CurrencyRepository currencyRepository;

    // This field will be used to instantiate the CurrencyService class that will be tested.
    private CurrencyService currencyService;

    // This method is executed before each test and is used to initialize the instance of CurrencyService.
    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService(currencyRepository);
    }

    // This test verifies if the get method returns a list of currencies as expected.
    @Test
    void testGetCurrencies() {
        // It creates two example currencies, saves them in the database, and verifies if the returned list contains both currencies.
        Currency currency1 = new Currency(1L, "USD", "US Dollar", new HashMap<>());
        Currency currency2 = new Currency(2L, "EUR", "Euro", new HashMap<>());
        currencyRepository.save(currency1);
        currencyRepository.save(currency2);

        List<CurrencyResponse> currencies = currencyService.get();

        assertEquals(2, currencies.size());
        assertEquals("1 - USD", currencies.get(0).getLabel());
        assertEquals("2 - EUR", currencies.get(1).getLabel());
    }

    // This test verifies if the create method creates a new currency as expected.
    @Test
    void testCreateCurrency() throws CurrencyException {
        // It creates a new example currency, calls the create method to create the currency, and verifies if the details of the created currency match the provided data.
        CurrencyRequest request = new CurrencyRequest("USD", "US Dollar", new HashMap<>());

        Long id = currencyService.create(request);

        Currency currency = currencyRepository.findById(id).orElse(null);
        assertEquals("USD", currency.getName());
        assertEquals("US Dollar", currency.getDescription());
    }

    // This test verifies if the create method throws an exception when a currency with the same name already exists in the database.
    @Test
    void testCreateExistingCurrency() {
        Currency existingCurrency = new Currency(1L,"USD", "US Dollar", new HashMap<>());
        currencyRepository.save(existingCurrency);
        CurrencyRequest request = new CurrencyRequest("USD", "US Dollar", new HashMap<>());

        assertThrows(CurrencyException.class, () -> currencyService.create(request));
    }

    // This test verifies if the delete method deletes a currency from the database as expected.
    @Test
    void testDeleteCurrency() {
        Currency currency = new Currency(1L, "USD", "US Dollar", new HashMap<>());
        currencyRepository.save(currency);

        currencyService.delete(currency.getId());

        assertEquals(0, currencyRepository.count());
    }

    // This test verifies if the convert method converts an amount from one currency to another as expected.
    @Test
    void testConvertCurrency() throws CoinNotFoundException {
        // It creates an example currency with exchange rates, calls the convert method, and verifies if the converted amount is correct.
        Map<String, BigDecimal> exchanges = new HashMap<>();
        exchanges.put("EUR", BigDecimal.valueOf(0.9));
        Currency currency = new Currency(1L, "USD", "US Dollar", exchanges);
        currencyRepository.save(currency);
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("USD", "EUR", BigDecimal.valueOf(100));

        BigDecimal convertedAmount = currencyService.convert(request).getAmount();

        assertEquals(BigDecimal.valueOf(90), convertedAmount);
    }

    // This test verifies if the convert method throws an exception when one of the currencies involved in the conversion does not exist in the database.
    @Test
    void testConvertNonExistingCurrency() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("USD", "EUR", BigDecimal.valueOf(100));

        assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));
    }

    // This test verifies if the convert method throws an exception when the exchange rate between the currencies is not available.
    @Test
    void testInvalidConversion() {
        Map<String, BigDecimal> exchanges = new HashMap<>();
        exchanges.put("EUR", BigDecimal.valueOf(0.9));
        Currency currency = new Currency(1L, "USD", "US Dollar", exchanges);
        currencyRepository.save(currency);
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("USD", "GBP", BigDecimal.valueOf(100));

        assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));
    }
}
