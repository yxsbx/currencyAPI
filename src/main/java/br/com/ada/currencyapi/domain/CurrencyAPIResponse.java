package br.com.ada.currencyapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyAPIResponse {
    private String code;
    private String codein;
    private String name;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal varBid;
    private BigDecimal pctChange;
    private BigDecimal bid;
    private BigDecimal ask;
    private String timestamp;
    private String createDate;
}
