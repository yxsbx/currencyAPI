package br.com.ada.currencyapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<CurrencyResponse> get() {
        List<Currency> currencies = currencyRepository.findAll();
        List<CurrencyResponse> dtos = new ArrayList<>();

        currencies.forEach((currency) -> dtos.add(CurrencyResponse.builder()
                .label("%s - %s".formatted(currency.getId(), currency.getName()))
                .build()));

        return dtos;
    }

    public Long create(CurrencyRequest request) throws CurrencyException {
        validateCurrencyRequest(request);
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

    public void delete(Long id) {
        validateCurrencyId(id);
        Currency from = currencyRepository.findById(id).orElseThrow(
                () -> new CoinNotFoundException(String.format(COIN_NOT_FOUND, id))
        );

        currencyRepository.deleteById(from.getId());
    }

    private BigDecimal calculateAmount(ConvertCurrencyRequest request, boolean useAPI) throws CoinNotFoundException {
        Currency from = currencyRepository.findByName(request.getFrom());
        String errorMessage = String.format(COIN_NOT_FOUND, request.getFrom());

        if (Objects.isNull(from)) {
            throw new CoinNotFoundException(errorMessage);
        }

        String tag = request.getTo() + "_" + request.getFrom();
        Map<String, CurrencyAPIResponse> responseMap = useAPI ? awesomeApiClient.getLastCurrencyAPI(tag) : null;

        if (responseMap != null && !responseMap.isEmpty()) {
            CurrencyAPIResponse currencyAPIResponse = responseMap.values().iterator().next();
            BigDecimal exchangeRate = currencyAPIResponse.getBid();
            errorMessage = String.format(EXCHANGE_RATE_NOT_FOUND, request.getFrom(), request.getTo());

            if (Objects.isNull(exchangeRate)) {
                throw new CoinNotFoundException(errorMessage);
            }

            return request.getAmount().multiply(exchangeRate);
        } else {
            errorMessage = String.format(CONVERSION_DATA_NOT_FOUND, request.getFrom(), request.getTo());
            throw new CoinNotFoundException(errorMessage);
        }
    }

    public ConvertCurrencyResponse convert(ConvertCurrencyRequest request) throws CoinNotFoundException {
        validateConvertRequest(request);
        BigDecimal amount = calculateAmount(request, false); // Use repository
        return ConvertCurrencyResponse.builder()
                .amount(amount)
                .build();
    }

    public ConvertCurrencyResponse convertCurrencyAPI(ConvertCurrencyRequest request) throws CoinNotFoundException {
        validateConvertRequest(request);
        BigDecimal amount = calculateAmount(request, true);
        return ConvertCurrencyResponse.builder()
                .amount(amount)
                .build();
    }

    public void update(Long id, CurrencyRequest request) throws CurrencyException {
        validateCurrencyId(id);
        validateCurrencyRequest(request);

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

    private void validateCurrencyRequest(CurrencyRequest request) throws IllegalArgumentException {
        if (Objects.isNull(request) || StringUtils.isEmpty(request.getName())) {
            throw new IllegalArgumentException(INVALID_CURRENCY_REQUEST);
        }
        // Add more validations if needed
    }

    private void validateCurrencyId(Long id) throws IllegalArgumentException {
        if (Objects.isNull(id) || id <= 0) {
            throw new IllegalArgumentException(INVALID_CURRENCY_ID);
        }
    }

    private void validateConvertRequest(ConvertCurrencyRequest request) throws IllegalArgumentException {
        if (Objects.isNull(request) || StringUtils.isEmpty(request.getFrom()) || StringUtils.isEmpty(request.getTo()) || Objects.isNull(request.getAmount())) {
            throw new IllegalArgumentException(INVALID_CONVERT_REQUEST);
        }
    }

    public List<CurrencyResponse> getCurrencyAPI(String currencies) {
    }
}
