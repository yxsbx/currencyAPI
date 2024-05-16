package br.com.ada.currencyapi.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConvertCurrencyResponse {
    private BigDecimal amount;
}
