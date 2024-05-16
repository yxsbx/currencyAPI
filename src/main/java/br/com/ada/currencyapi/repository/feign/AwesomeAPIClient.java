package br.com.ada.currencyapi.repository.feign;

import br.com.ada.currencyapi.domain.CurrencyAPIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "awesomeApiClient", url = "https://economia.awesomeapi.com.br/json")
public interface AwesomeAPIClient {
    @GetMapping("last/{currencies}")
    Map<String, CurrencyAPIResponse> getLastCurrency(@PathVariable("currencies") String currencies);
}
