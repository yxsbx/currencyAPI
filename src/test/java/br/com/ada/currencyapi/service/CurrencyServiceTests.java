package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import br.com.ada.currencyapi.repository.feign.AwesomeAPIClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTests {

    @Mock
    private AwesomeAPIClient awesomeApiClient;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @Captor
    private ArgumentCaptor<Currency> currencyCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        reset(currencyRepository);
    }

    @Test
    void testGetCurrencies() {
        Currency currency1 = Currency.builder().id(1L).name("USD").description("US Dollar").build();
        Currency currency2 = Currency.builder().id(2L).name("EUR").description("Euro").build();
        List<Currency> currencyList = Arrays.asList(currency1, currency2);

        when(currencyRepository.findAll()).thenReturn(currencyList);

        List<CurrencyResponse> response = currencyService.get();

        assertEquals(2, response.size());
        assertEquals("1 - USD", response.get(0).getLabel());
        assertEquals("2 - EUR", response.get(1).getLabel());

        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    void testCreateCurrency() throws CurrencyException {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        Currency savedCurrency = Currency.builder()
                .id(1L)
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        when(currencyRepository.findByName(request.getName())).thenReturn(null);
        when(currencyRepository.save(any(Currency.class))).thenReturn(savedCurrency);

        Long id = currencyService.create(request);

        assertEquals(savedCurrency.getId(), id);
        verify(currencyRepository).save(currencyCaptor.capture());
        assertEquals(request.getName(), currencyCaptor.getValue().getName());
    }

    @Test
    void testCreateCurrency_AlreadyExists() {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        Currency existingCurrency = Currency.builder()
                .id(1L)
                .name("USD")
                .description("US Dollar")
                .build();

        when(currencyRepository.findByName(request.getName())).thenReturn(existingCurrency);

        CurrencyException exception = assertThrows(CurrencyException.class, () -> {
            currencyService.create(request);
        });

        assertEquals("Coin already exists", exception.getMessage());
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void testUpdateCurrency() throws CurrencyException {
        Long id = 1L;
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        Currency existingCurrency = Currency.builder()
                .id(id)
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        when(currencyRepository.findById(id)).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.findByName(request.getName())).thenReturn(null);
        when(currencyRepository.save(any(Currency.class))).thenReturn(existingCurrency);

        currencyService.update(id, request);

        verify(currencyRepository).save(currencyCaptor.capture());
        assertEquals(request.getName(), currencyCaptor.getValue().getName());
    }

    @Test
    void testUpdateCurrency_CoinNotFoundException() {
        Long id = 1L;
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        when(currencyRepository.findById(id)).thenReturn(Optional.empty());

        CoinNotFoundException exception = assertThrows(CoinNotFoundException.class, () -> {
            currencyService.update(id, request);
        });

        assertEquals("Coin not found: 1", exception.getMessage());
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void testUpdateCurrency_CoinAlreadyExistsException() {
        Long id = 1L;
        CurrencyRequest request = CurrencyRequest.builder()
                .name("EUR")
                .description("Euro")
                .exchanges(null)
                .build();

        Currency existingCurrency = Currency.builder()
                .id(id)
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        Currency anotherCurrency = Currency.builder()
                .id(2L)
                .name("EUR")
                .description("Euro")
                .build();

        when(currencyRepository.findById(id)).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.findByName(request.getName())).thenReturn(anotherCurrency);

        CurrencyException exception = assertThrows(CurrencyException.class, () -> {
            currencyService.update(id, request);
        });

        assertEquals("Coin already exists", exception.getMessage());
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void testDeleteCurrency() {
        Long id = 1L;
        Currency existingCurrency = Currency.builder()
                .id(id)
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        when(currencyRepository.findById(id)).thenReturn(Optional.of(existingCurrency));
        doNothing().when(currencyRepository).deleteById(id);

        currencyService.delete(id);

        verify(currencyRepository).deleteById(id);
    }

    @Test
    void testDeleteCurrency_CoinNotFoundException() {
        Long id = 1L;

        when(currencyRepository.findById(id)).thenReturn(Optional.empty());

        CoinNotFoundException exception = assertThrows(CoinNotFoundException.class, () -> {
            currencyService.delete(id);
        });

        assertEquals("Coin not found: 1", exception.getMessage());
        verify(currencyRepository, never()).deleteById(id);
    }

    @Test
    void testConvertCurrency() throws CoinNotFoundException {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.valueOf(100))
                .build();

        CurrencyAPIResponse apiResponse = new CurrencyAPIResponse();
        apiResponse.setLow(BigDecimal.valueOf(0.85));

        Map<String, CurrencyAPIResponse> apiResponseMap = new HashMap<>();
        apiResponseMap.put("USDEUR", apiResponse);

        when(awesomeApiClient.getLastCurrency(Collections.singletonList("USD-EUR"))).thenReturn(apiResponseMap);

        ConvertCurrencyResponse response = currencyService.convert(request);

        assertEquals(BigDecimal.valueOf(85).stripTrailingZeros(), response.getAmount().stripTrailingZeros());
    }

    @Test
    void testConvertCurrency_CoinNotFoundException() {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.valueOf(100))
                .build();

        when(awesomeApiClient.getLastCurrency(Collections.singletonList("USD-EUR"))).thenReturn(new HashMap<>());

        CoinNotFoundException exception = assertThrows(CoinNotFoundException.class, () -> {
            currencyService.convert(request);
        });

        assertEquals("Exchange rate not found for EUR to USD", exception.getMessage());
    }

    @Test
    void testConvertCurrency_ExchangeRateNotFoundException() {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.valueOf(100))
                .build();

        CurrencyAPIResponse apiResponse = new CurrencyAPIResponse();
        apiResponse.setLow(null);

        Map<String, CurrencyAPIResponse> apiResponseMap = new HashMap<>();
        apiResponseMap.put("USDEUR", apiResponse);

        when(awesomeApiClient.getLastCurrency(Collections.singletonList("USD-EUR"))).thenReturn(apiResponseMap);

        CoinNotFoundException exception = assertThrows(CoinNotFoundException.class, () -> {
            currencyService.convert(request);
        });

        assertEquals("Exchange rate not found for EUR to USD", exception.getMessage());
    }

    @Test
    void testValidateGetStoredCurrencies() {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .description("US Dollar")
                .exchanges(null)
                .build();

        assertDoesNotThrow(() -> {
            currencyService.create(request);
        });
    }

    @Test
    void testValidateGetStoredCurrencies_InvalidRequest() {
        CurrencyRequest request = CurrencyRequest.builder()
                .name("")
                .description("US Dollar")
                .exchanges(null)
                .build();

        CurrencyException exception = assertThrows(CurrencyException.class, () -> {
            currencyService.create(request);
        });

        assertEquals("Invalid CurrencyRequest", exception.getMessage());
    }

    @Test
    void testValidateCurrencyId() {
        assertDoesNotThrow(() -> {
            currencyService.delete(1L);
        });
    }

    @Test
    void testValidateCurrencyId_InvalidId() {
        CurrencyException exception = assertThrows(CurrencyException.class, () -> {
            currencyService.delete(0L);
        });

        assertEquals("Invalid Currency ID", exception.getMessage());
    }

    @Test
    void testValidateConvertRequest() {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.valueOf(100))
                .build();

        assertDoesNotThrow(() -> {
            currencyService.convert(request);
        });
    }

    @Test
    void testValidateConvertRequest_InvalidRequest() {
        ConvertCurrencyRequest request = ConvertCurrencyRequest.builder()
                .from("")
                .to("EUR")
                .amount(BigDecimal.valueOf(100))
                .build();

        CurrencyException exception = assertThrows(CurrencyException.class, () -> {
            currencyService.convert(request);
        });

        assertEquals("Invalid ConvertCurrencyRequest", exception.getMessage());
    }
}