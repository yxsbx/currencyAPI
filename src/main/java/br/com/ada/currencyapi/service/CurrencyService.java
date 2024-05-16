package br.com.ada.currencyapi.service;

import java.math.BigDecimal;
import java.util.*;

import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.repository.feign.AwesomeAPIClient;
import br.com.ada.currencyapi.domain.CurrencyAPIResponse;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    private static final String COIN_ALREADY_EXISTS = "Coin already exists";
    private static final String EXCHANGE_RATE_NOT_FOUND = "Exchange rate not found for %s to %s";

    public List<CurrencyResponse> get() {
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
            throw new CurrencyException(COIN_ALREADY_EXISTS);
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
                throw new CurrencyException(COIN_ALREADY_EXISTS);
            }
        }
        currency.setName(request.getName());
        currency.setDescription(request.getDescription());
        currency.setExchanges(request.getExchanges());

        currencyRepository.save(currency);
    }

    private void validateGetStoredCurrencies(CurrencyRequest request) throws IllegalArgumentException {
        if (Objects.isNull(request) || StringUtils.isEmpty(request.getName())) {
            throw new CurrencyException(INVALID_CURRENCY_REQUEST);
        }
    }

    private void validateCurrencyId(Long id) throws IllegalArgumentException {
        if (Objects.isNull(id) || id <= 0) {
            throw new CurrencyException(INVALID_CURRENCY_ID);
        }
    }

    public void delete(Long id) {
        validateCurrencyId(id);
        Currency from = currencyRepository.findById(id).orElseThrow(
                () -> new CoinNotFoundException(String.format(COIN_NOT_FOUND, id))
        );
        currencyRepository.deleteById(from.getId());
    }

    public ConvertCurrencyResponse convert(ConvertCurrencyRequest request) throws CoinNotFoundException {
        BigDecimal amount = getAmountWithAwesomeApi(request);
        return ConvertCurrencyResponse.builder()
                .amount(amount)
                .build();
    }

    private BigDecimal getAmountWithAwesomeApi(ConvertCurrencyRequest request) throws CoinNotFoundException {
        validateConvertRequest(request);
        String code = request.getFrom() + "-" + request.getTo();
        Map<String, CurrencyAPIResponse> response = awesomeApiClient.getLastCurrency(Collections.singletonList(code));
        CurrencyAPIResponse currencyApiResponse = response.get(code);

        if (Objects.isNull(currencyApiResponse) || Objects.isNull(currencyApiResponse.getLow())) {
            throw new CoinNotFoundException(String.format(EXCHANGE_RATE_NOT_FOUND, request.getTo(), request.getFrom()));
        }

        return request.getAmount().multiply(currencyApiResponse.getLow());
    }

    private void validateConvertRequest(ConvertCurrencyRequest request) throws IllegalArgumentException {
        if (Objects.isNull(request) || StringUtils.isEmpty(request.getFrom()) || StringUtils.isEmpty(request.getTo()) || Objects.isNull(request.getAmount())) {
            throw new CurrencyException(INVALID_CONVERT_REQUEST);
        }
    }
}
