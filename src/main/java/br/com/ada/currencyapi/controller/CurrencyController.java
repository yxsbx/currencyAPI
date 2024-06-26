package br.com.ada.currencyapi.controller;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import javax.validation.Valid;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> get() {
        return new ResponseEntity<>(currencyService.get(), HttpStatus.OK);
    }

    @PostMapping("/convert")
    public ResponseEntity<ConvertCurrencyResponse> convert(@RequestBody @Valid ConvertCurrencyRequest request) throws CoinNotFoundException {
        return new ResponseEntity<>(currencyService.convert(request), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody @Valid CurrencyRequest request) throws CurrencyException {
        return new ResponseEntity<>(currencyService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid CurrencyRequest request) throws CurrencyException {
        currencyService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        currencyService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<CurrencyResponse>> listCurrencies() {
        List<CurrencyResponse> currencies = currencyService.getCurrencies();
        return new ResponseEntity<>(currencies, HttpStatus.OK);
    }
}