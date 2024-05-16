package br.com.ada.currencyapi.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrencyResponse {
    private String label;
}
