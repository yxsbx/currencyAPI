package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import br.com.ada.currencyapi.repository.feign.AwesomeAPIClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {

    @Mock
    private AwesomeAPIClient awesomeApiClient;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGet() {
        Currency currency = Currency.builder()
                .id(1L)
                .name("Dólar Americano")
                .code("USD")
                .build();

        when(currencyRepository.findAll()).thenReturn(Collections.singletonList(currency));

        var response = currencyService.get();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getLabel()).isEqualTo("USD - Dólar Americano");
        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    void testCreate() throws CurrencyException {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        when(currencyRepository.findByName(anyString())).thenReturn(null);
        when(currencyRepository.save(any(Currency.class))).thenReturn(Currency.builder().id(1L).build());

        Long id = currencyService.create(request);

        assertThat(id).isEqualTo(1L);
        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void testUpdateThrowsCurrencyException() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        Currency existingCurrency = Currency.builder()
                .id(2L)
                .name("Dólar Americano")
                .code("USD")
                .build();

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.of(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build()));
        when(currencyRepository.findByName(anyString())).thenReturn(existingCurrency);

        assertThatThrownBy(() -> currencyService.update(1L, request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin already exists");

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(0)).save(any(Currency.class));
    }

    @Test
    void testUpdate() throws CurrencyException {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        Currency existingCurrency = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build();

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.findByName(anyString())).thenReturn(null);
        when(currencyRepository.save(any(Currency.class))).thenReturn(existingCurrency);

        currencyService.update(1L, request);

        assertThat(existingCurrency.getName()).isEqualTo("Dólar Americano");
        assertThat(existingCurrency.getCode()).isEqualTo("USD");
        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void testUpdateThrowsCoinNotFoundException() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.update(1L, request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Coin not found");

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(0)).findByName(anyString());
        verify(currencyRepository, times(0)).save(any(Currency.class));
    }

    @Test
    void testDelete() {
        Currency existingCurrency = Currency.builder()
                .id(1L)
                .name("Dólar Americano")
                .code("USD")
                .build();

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.of(existingCurrency));
        doNothing().when(currencyRepository).deleteById(anyLong());

        currencyService.delete(1L);

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void testDeleteThrowsCoinNotFoundException() {
        when(currencyRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.delete(1L))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Coin not found");

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(0)).deleteById(anyLong());
    }

    @Test
    void testConvert() throws CoinNotFoundException {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        CurrencyAPIResponse response = new CurrencyAPIResponse();
        response.setLow(BigDecimal.valueOf(5));

        when(awesomeApiClient.getLastCurrency(anyString())).thenReturn(Map.of("USD-BRL", response));

        ConvertCurrencyResponse result = currencyService.convert(request);

        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(500));
        verify(awesomeApiClient, times(1)).getLastCurrency(anyString());
    }

    @Test
    void testConvertThrowsCoinNotFoundException() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        when(awesomeApiClient.getLastCurrency(anyString())).thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> currencyService.convert(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Exchange rate not found");

        verify(awesomeApiClient, times(1)).getLastCurrency(anyString());
    }

    @Test
    void testValidateCurrencyRequestThrowsCurrencyException() {
        CurrencyRequest request = new CurrencyRequest();

        assertThatThrownBy(() -> currencyService.create(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid CurrencyRequest");
    }

    @Test
    void testValidateCurrencyIdThrowsCurrencyException() {
        assertThatThrownBy(() -> currencyService.delete(null))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");

        assertThatThrownBy(() -> currencyService.delete(0L))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");
    }

    @Test
    void testValidateConvertRequestThrowsCurrencyException() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();

        assertThatThrownBy(() -> currencyService.convert(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid ConvertCurrencyRequest");
    }

    @Test
    void testValidateCurrencyRequestThrowsCurrencyExceptionWhenRequestIsNull() {
        CurrencyRequest request = null;

        assertThatThrownBy(() -> currencyService.create(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid CurrencyRequest");
    }

    @Test
    void testValidateCurrencyRequestThrowsCurrencyExceptionWhenNameIsEmpty() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("");
        request.setCode("USD");

        assertThatThrownBy(() -> currencyService.create(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid CurrencyRequest");
    }

    @Test
    void testValidateCurrencyRequestThrowsCurrencyExceptionWhenCodeIsEmpty() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("");

        assertThatThrownBy(() -> currencyService.create(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid CurrencyRequest");
    }

    @Test
    void testValidateCurrencyIdThrowsCurrencyExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> currencyService.delete(null)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");
    }

    @Test
    void testValidateCurrencyIdThrowsCurrencyExceptionWhenIdIsLessThanOrEqualToZero() {
        assertThatThrownBy(() -> currencyService.delete(0L)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");

        assertThatThrownBy(() -> currencyService.delete(-1L)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid Currency ID");
    }

    @Test
    void testValidateConvertRequestThrowsCurrencyExceptionWhenRequestIsNull() {
        ConvertCurrencyRequest request = null;

        assertThatThrownBy(() -> currencyService.convert(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid ConvertCurrencyRequest");
    }

    @Test
    void testValidateConvertRequestThrowsCurrencyExceptionWhenFromIsEmpty() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> currencyService.convert(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid ConvertCurrencyRequest");
    }

    @Test
    void testValidateConvertRequestThrowsCurrencyExceptionWhenToIsEmpty() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("");
        request.setAmount(BigDecimal.valueOf(100));

        assertThatThrownBy(() -> currencyService.convert(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid ConvertCurrencyRequest");
    }

    @Test
    void testValidateConvertRequestThrowsCurrencyExceptionWhenAmountIsNull() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(null);

        assertThatThrownBy(() -> currencyService.convert(request)).isInstanceOf(CurrencyException.class)
                .hasMessage("Invalid ConvertCurrencyRequest");
    }

    @Test
    void testGetAmountWithAwesomeApiThrowsCoinNotFoundExceptionWhenResponseIsNull() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        when(awesomeApiClient.getLastCurrency(anyString())).thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> {
            currencyService.convert(request);
        }).isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Exchange rate not found");

        verify(awesomeApiClient, times(1)).getLastCurrency(anyString());
    }

    @Test
    void testGetAmountWithAwesomeApiThrowsCoinNotFoundExceptionWhenLowIsNull() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("BRL");
        request.setAmount(BigDecimal.valueOf(100));

        CurrencyAPIResponse response = new CurrencyAPIResponse();
        response.setLow(null);

        when(awesomeApiClient.getLastCurrency(anyString())).thenReturn(Map.of("USD-BRL", response));

        assertThatThrownBy(() -> {
            currencyService.convert(request);
        }).isInstanceOf(CoinNotFoundException.class)
                .hasMessageContaining("Exchange rate not found");

        verify(awesomeApiClient, times(1)).getLastCurrency(anyString());
    }

    @Test
    void testUpdateThrowsCurrencyExceptionWhenExistingCurrencyIdIsDifferent() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("Dólar Americano");
        request.setCode("USD");

        Currency existingCurrencyWithSameName = Currency.builder()
                .id(2L)
                .name("Dólar Americano")
                .code("USD")
                .build();

        Currency currencyToBeUpdated = Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build();

        when(currencyRepository.findById(anyLong())).thenReturn(Optional.of(currencyToBeUpdated));
        when(currencyRepository.findByName(anyString())).thenReturn(existingCurrencyWithSameName);

        assertThatThrownBy(() -> currencyService.update(1L, request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin already exists");

        verify(currencyRepository, times(1)).findById(anyLong());
        verify(currencyRepository, times(1)).findByName(anyString());
        verify(currencyRepository, times(0)).save(any(Currency.class));
    }
}