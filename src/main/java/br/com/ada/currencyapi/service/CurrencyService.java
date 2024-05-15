package br.com.ada.currencyapi.service;

import java.math.BigDecimal;
import java.util.*;

import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.repository.feign.AwesomeAPIClient;

import br.com.ada.currencyapi.domain.*;
import org.springframework.stereotype.Service;

import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final AwesomeAPIClient awesomeApiClient;
    private final CurrencyRepository currencyRepository;

    private static final String INVALID_CURRENCY_REQUEST = "Invalid CurrencyRequest";
    private static final String INVALID_CURRENCY_ID = "Invalid Currency ID";
    private static final String INVALID_CONVERT_REQUEST = "Invalid ConvertCurrencyRequest";
    private static final String COIN_NOT_FOUND = "Coin not found: %s";
    private static final String EXCHANGE_RATE_NOT_FOUND = "Exchange rate not found for %s to %s";
    private static final String CONVERSION_DATA_NOT_FOUND = "Currency conversion data not found for %s to %s";

    public List<CurrencyResponse> getStoredCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        List<CurrencyResponse> dtos = new ArrayList<>();

        currencies.forEach((currency) -> dtos.add(CurrencyResponse.builder()
                .label("%s - %s".formatted(currency.getId(), currency.getName()))
                .build()));

        return dtos;
    }

    public Long create(CurrencyRequest request) throws CurrencyException {
        validateGetStoredCurrencies(request);
        Currency currency = currencyRepository.findByName(request.getName());

        if (Objects.nonNull(currency)) {
            throw new CurrencyException("Coin already exists");
        }

        Currency saved = currencyRepository.save(Currency.builder()
                .name(request.getName())
                .description(request.getDescription())
                .exchanges(request.getExchanges())
                .build());
        return saved.getId();
    }

    public void update(Long id, CurrencyRequest request) throws CurrencyException {
        validateCurrencyId(id);
        validateGetStoredCurrencies(request);

        Currency currency = currencyRepository.findById(id).orElseThrow(
                () -> new CoinNotFoundException(String.format(COIN_NOT_FOUND, id))
        );

        if (!currency.getName().equals(request.getName())) {
            Currency existingCurrency = currencyRepository.findByName(request.getName());
            if (existingCurrency != null && !existingCurrency.getId().equals(id)) {
                throw new CurrencyException("Coin with the same name already exists");
            }
        }
        currency.setName(request.getName());
        currency.setDescription(request.getDescription());
        currency.setExchanges(request.getExchanges());

        currencyRepository.save(currency);
    }

    private void validateGetStoredCurrencies(CurrencyRequest request) throws IllegalArgumentException {
        if (Objects.isNull(request) || StringUtils.isEmpty(request.getName())) {
            throw new IllegalArgumentException(INVALID_CURRENCY_REQUEST);
        }
    }

    private void validateCurrencyId(Long id) throws IllegalArgumentException {
        if (Objects.isNull(id) || id <= 0) {
            throw new IllegalArgumentException(INVALID_CURRENCY_ID);
        }
    }

    public void delete(Long id) {
        validateCurrencyId(id);
        Currency from = currencyRepository.findById(id).orElseThrow(
                () -> new CoinNotFoundException(String.format(COIN_NOT_FOUND, id))
        );

        currencyRepository.deleteById(from.getId());
    }

    public Map<String, CurrencyAPIResponse> getLastCurrencyAPI(List<String> currenciesAPI) {
        return awesomeApiClient.getLastCurrency(currenciesAPI);
    }

    private BigDecimal calculateAmount(ConvertCurrencyRequest request, boolean getLastCurrencyAPI) throws CoinNotFoundException {
        String currencyCode = null;
        BigDecimal exchangeRate;

        if (getLastCurrencyAPI) {
            Currency currency = currencyRepository.findById(Long.valueOf(request.getFrom())).orElse(null);
            if (currency == null) {
                throw new CoinNotFoundException(String.format(COIN_NOT_FOUND, Long.valueOf(request.getFrom())));
            }
            currencyCode = currency.getName();
        } else {
            Currency from = currencyRepository.findById(Long.valueOf(request.getFrom())).orElse(null);
            Currency to = currencyRepository.findById(Long.valueOf(request.getTo())).orElse(null);
            if (from == null || to == null) {
                throw new CoinNotFoundException(String.format(COIN_NOT_FOUND, Long.valueOf(request.getFrom()) + " or " + Long.valueOf(request.getTo())));
            }
            exchangeRate = from.getExchanges().get(to.getName());
            if (exchangeRate == null) {
                throw new CoinNotFoundException(String.format(EXCHANGE_RATE_NOT_FOUND, from.getName(), to.getName()));
            }
            return request.getAmount().multiply(exchangeRate);
        }

        Map<String, CurrencyAPIResponse> responseMap = awesomeApiClient.getLastCurrency(Arrays.asList(currencyCode));
        if (responseMap != null && !responseMap.isEmpty()) {
            CurrencyAPIResponse currencyAPIResponse = responseMap.get(currencyCode);
            exchangeRate = currencyAPIResponse.getBid();
            if (exchangeRate == null) {
                throw new CoinNotFoundException(String.format(EXCHANGE_RATE_NOT_FOUND, request.getFrom(), request.getTo()));
            }
            return request.getAmount().multiply(exchangeRate);
        } else {
            throw new CoinNotFoundException(String.format(CONVERSION_DATA_NOT_FOUND, request.getFrom(), request.getTo()));
        }
    }

    public ConvertCurrencyResponse convert(ConvertCurrencyRequest request) throws CoinNotFoundException {
        validateConvertRequest(request);
        BigDecimal amount = calculateAmount(request, true);
        return ConvertCurrencyResponse.builder()
                .amount(amount)
                .build();
    }

    private void validateConvertRequest(ConvertCurrencyRequest request) throws IllegalArgumentException {
        if (Objects.isNull(request) || StringUtils.isEmpty(request.getFrom()) || StringUtils.isEmpty(request.getTo()) || Objects.isNull(request.getAmount())) {
            throw new IllegalArgumentException(INVALID_CONVERT_REQUEST);
        }
    }
}
