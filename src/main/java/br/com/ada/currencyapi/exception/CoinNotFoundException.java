package br.com.ada.currencyapi.exception;

public class CoinNotFoundException extends RuntimeException {
    public CoinNotFoundException(String message) {
        super(message);
    }

    public CoinNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
