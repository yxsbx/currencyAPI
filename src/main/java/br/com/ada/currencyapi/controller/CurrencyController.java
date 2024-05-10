package br.com.ada.currencyapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> get() {
        log.info("Fetching all currencies");
        List<CurrencyResponse> response = currencyService.get();
        log.info("Fetched {} currencies", response.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/convert")
    public ResponseEntity<ConvertCurrencyResponse> convert(@RequestBody ConvertCurrencyRequest request) throws CoinNotFoundException {
        log.info("Converting currency from {} to {}", request.getFrom(), request.getTo());
        ConvertCurrencyResponse response = currencyService.convert(request);
        log.info("Converted amount: {}", response.getAmount());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CurrencyRequest request) throws CurrencyException {
        return new ResponseEntity<>(currencyService.create(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        log.info("Deleting currency with ID {}", id);
        currencyService.delete(id);
        log.info("Deleted currency with ID {}", id);
        return ResponseEntity.ok().build();
    }
}