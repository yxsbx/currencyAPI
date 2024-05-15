package br.com.ada.currencyapi.controller;


import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> get() {
        List<CurrencyResponse> currencies = currencyService.getStoredCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/convert")
    public ResponseEntity<ConvertCurrencyResponse> convert(@RequestBody ConvertCurrencyRequest request) throws CoinNotFoundException {
        ConvertCurrencyResponse response = currencyService.convert(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CurrencyRequest request) throws CurrencyException {
        Long id = currencyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long id, @RequestBody CurrencyRequest request) throws CurrencyException, CoinNotFoundException {
        currencyService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        currencyService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/json/last")
    public ResponseEntity<Map<String, CurrencyAPIResponse>> getLastCurrencyAPI(@RequestParam List<String> currenciesAPI) {
        Map<String, CurrencyAPIResponse> currenciesAPIResponse = currencyService.getLastCurrencyAPI(currenciesAPI);
        return ResponseEntity.ok(currenciesAPIResponse);
    }
}