package br.com.ada.currencyapi.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConvertCurrencyRequest {
    private String from;
    private String to;
    private BigDecimal amount;
}
