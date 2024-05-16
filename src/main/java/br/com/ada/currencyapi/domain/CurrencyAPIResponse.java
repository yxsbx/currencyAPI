package br.com.ada.currencyapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("code")
    private String code;

    @JsonProperty("codein")
    private String codein;

    @JsonProperty("name")
    private String name;

    @JsonProperty("high")
    private BigDecimal high;

    @JsonProperty("low")
    private BigDecimal low;

    @JsonProperty("varBid")
    private BigDecimal varBid;

    @JsonProperty("pctChange")
    private BigDecimal pctChange;

    @JsonProperty("bid")
    private BigDecimal bid;

    @JsonProperty("ask")
    private BigDecimal ask;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("create_date")
    private String createDate;
}