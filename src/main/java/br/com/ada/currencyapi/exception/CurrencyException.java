package br.com.ada.currencyapi.exception;

public class CurrencyException extends RuntimeException {

    public CurrencyException(String message) {
        super(message);
    }

    public CurrencyException() {
    }
}
