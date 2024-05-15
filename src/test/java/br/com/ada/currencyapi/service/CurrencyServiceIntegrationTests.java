package br.com.ada.currencyapi.service;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.openfeign.FeignClientFactory;

import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import br.com.ada.currencyapi.exception.CoinNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

@DataJpaTest
@Import(CurrencyService.class)
public class CurrencyServiceIntegrationTests {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyRepository.deleteAll();
    }

    /**
     * Tests both the creation and retrieval of a currency, ensuring the entire process is cohesive and correct data is stored and retrieved.
     */

    @Test
    void testCreateAndRetrieveCurrencies() {
        CurrencyRequest request = new CurrencyRequest("EUR", "Euro", new HashMap<String, BigDecimal>() {{
            put("USD", new BigDecimal("1.2"));
        }});
        Long id = currencyService.create(request);
        Currency found = currencyRepository.findById(id).orElse(null);

        assertNotNull(found);
        assertEquals("EUR", found.getName());
        assertEquals("Euro", found.getDescription());
        assertTrue(found.getExchanges().containsKey("USD"));
        assertEquals(0, new BigDecimal("1.2").compareTo(found.getExchanges().get("USD")));
    }

    /**
     * Confirms that deleting a currency through the service layer also removes it from the database.
     */

    @Test
    void testDeleteCurrencies() {
        CurrencyRequest request = new CurrencyRequest("BTC", "Bitcoin", new HashMap<>());
        Long id = currencyService.create(request);
        currencyService.delete(id);

        Optional<Currency> found = currencyRepository.findById(id);
        assertFalse(found.isPresent());
    }

    /**
     * Assesses the conversion functionality, particularly focusing on the accuracy of the conversion based on predefined exchange rates.
     */

    @Test
    void testConvertCurrencies() {
        HashMap<String, BigDecimal> exchanges = new HashMap<>();
        exchanges.put("USD", new BigDecimal("1.1"));
        currencyRepository.save(new Currency(null, "EUR", "Euro", exchanges));

        ConvertCurrencyRequest convertRequest = new ConvertCurrencyRequest("EUR", "USD", new BigDecimal("100"));
        ConvertCurrencyResponse response = currencyService.convert(convertRequest);

        assertEquals(0, new BigDecimal("110.0").compareTo(response.getAmount()));
    }

    /**
     * Check error handling in the service when trying to convert non-existent currencies or delete currencies that do not exist.
     */

    @Test
    void testConvertCurrenciesFailure() {
        assertThrows(CoinNotFoundException.class, () -> {
            ConvertCurrencyRequest convertRequest = new ConvertCurrencyRequest("XYZ", "USD", new BigDecimal("100"));
            currencyService.convert(convertRequest);
        });
    }

    /**
     *
     */

    @Test
    void testDeleteNonExistentCurrencies() {
        assertThrows(CoinNotFoundException.class, () -> currencyService.delete(999L));
    }
}
