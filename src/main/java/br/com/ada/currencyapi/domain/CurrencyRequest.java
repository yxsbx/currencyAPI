package br.com.ada.currencyapi.domain;

import lombok.Data;

@Data
public class CurrencyRequest {
    private String name;
    private String code;
}
